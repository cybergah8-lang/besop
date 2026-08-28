package com.cybergah.securewipe.desktop

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cybergah.securewipe.AppTheme
import com.cybergah.securewipe.FileResult
import com.cybergah.securewipe.Lang
import com.cybergah.securewipe.PASSES
import com.cybergah.securewipe.PickedItem
import com.cybergah.securewipe.ResultKind
import com.cybergah.securewipe.Scheme
import com.cybergah.securewipe.ThemeMode
import com.cybergah.securewipe.WipeScreen
import com.cybergah.securewipe.WipeState
import com.cybergah.securewipe.stringsFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.UIManager

private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun main() = application {
    // Dosya seciciler isletim sisteminin gorunumunu kullansin
    runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }

    var lang by remember { mutableStateOf(DesktopPrefs.lang()) }
    var themeMode by remember { mutableStateOf(DesktopPrefs.theme()) }
    val progress by WipeState.progress.collectAsState()
    val items = remember { mutableStateListOf<PickedItem>() }
    var note by remember { mutableStateOf<String?>(null) }

    // disk modu
    var diskOpen by remember { mutableStateOf(false) }
    var disks by remember { mutableStateOf(emptyList<DiskInfo>()) }
    var elevated by remember { mutableStateOf(false) }

    val s = stringsFor(lang)

    Window(
        onCloseRequest = ::exitApplication,
        title = s.appName,
        state = rememberWindowState(width = 560.dp, height = 860.dp)
    ) {
        AppTheme(themeMode) {
            if (diskOpen) {
                DiskScreen(
                    lang = lang,
                    disks = disks,
                    elevated = elevated,
                    progress = progress,
                    onRefresh = {
                        disks = DiskWipe.list()
                        elevated = DiskWipe.isElevated()
                    },
                    onWipe = { d ->
                        WipeState.reset()
                        ioScope.launch { runDiskWipe(d) }
                    },
                    onStop = { WipeState.requestCancel() },
                    onElevate = { if (DiskWipe.restartElevated()) exitApplication() },
                    onClose = { diskOpen = false }
                )
                return@AppTheme
            }

            WipeScreen(
                lang = lang,
                onLangChange = {
                    lang = it
                    DesktopPrefs.setLang(it)
                },
                items = items,
                progress = progress,
                onPickFiles = {
                    val picked = chooseFiles(s.pickFiles)
                    note = null
                    addAll(items, picked)
                },
                onPickFolder = {
                    val dir = chooseFolder(s.pickFolder)
                    if (dir != null) {
                        val found = DesktopEngine.expandFolder(dir)
                        if (found.isEmpty()) note = s.emptyFolder
                        else {
                            note = null
                            addAll(items, found)
                        }
                    }
                },
                onRemove = { i -> if (i in items.indices) items.removeAt(i) },
                onClear = { items.clear() },
                onWipe = {
                    val targets = items.map { File(it.id) }
                    items.clear()
                    WipeState.reset()
                    ioScope.launch { runWipe(targets) }
                },
                onStop = { WipeState.requestCancel() },
                showBgNote = false,
                note = note,
                themeMode = themeMode,
                onThemeChange = { themeMode = it; DesktopPrefs.setTheme(it) },
                onOpenDisk = {
                    disks = DiskWipe.list()
                    elevated = DiskWipe.isElevated()
                    diskOpen = true
                }
            )
        }
    }
}

private fun addAll(items: MutableList<PickedItem>, files: List<File>) {
    val existing = items.map { it.id }.toHashSet()
    files.forEach { f ->
        val id = f.absolutePath
        if (f.isFile && existing.add(id)) {
            items.add(PickedItem(id, f.name, f.length()))
        }
    }
}

private fun runWipe(files: List<File>) {
    val passes = PASSES
    val sizes = files.map { it.length() }
    val bytesTotal = sizes.sumOf { it * passes }
    var bytesDone = 0L
    val results = mutableListOf<FileResult>()

    WipeState.update {
        it.copy(
            running = true,
            finished = false,
            cancelled = false,
            fileCount = files.size,
            passCount = passes,
            bytesTotal = bytesTotal,
            startedAtMs = System.currentTimeMillis()
        )
    }

    files.forEachIndexed { index, f ->
        if (WipeState.cancelRequested) return@forEachIndexed

        WipeState.update { it.copy(currentName = f.name, fileIndex = index + 1, pass = 1) }

        val result = DesktopEngine.wipe(f, passes, Scheme.RANDOM) { written, pass ->
            bytesDone += written
            val frac = if (bytesTotal > 0) (bytesDone.toDouble() / bytesTotal).toFloat() else 0f
            WipeState.update {
                it.copy(pass = pass, bytesDone = bytesDone, overall = frac.coerceIn(0f, 1f))
            }
        }

        bytesDone = bytesDone.coerceAtLeast(sizes.take(index + 1).sumOf { it * passes })
        results += result
        WipeState.update { it.copy(results = results.toList()) }
    }

    val wasCancelled = WipeState.cancelRequested
    WipeState.update {
        it.copy(
            running = false,
            finished = true,
            cancelled = wasCancelled,
            overall = if (wasCancelled) it.overall else 1f,
            results = results.toList()
        )
    }
}

// ---------------------------------------------------------------- dosya seciciler

private fun chooseFiles(title: String): List<File> {
    val fc = JFileChooser().apply {
        dialogTitle = title
        isMultiSelectionEnabled = true
        fileSelectionMode = JFileChooser.FILES_ONLY
    }
    return if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        fc.selectedFiles.toList()
    } else {
        emptyList()
    }
}

private fun chooseFolder(title: String): File? {
    val fc = JFileChooser().apply {
        dialogTitle = title
        isMultiSelectionEnabled = false
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    }
    return if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) fc.selectedFile else null
}

// ---------------------------------------------------------------- dil tercihi

private object DesktopPrefs {
    private val prefs: Preferences = Preferences.userRoot().node("com/cybergah/securewipe")
    private const val KEY = "lang"
    private const val KEY_THEME = "theme"

    fun lang(): Lang {
        val saved = prefs.get(KEY, null)
        if (saved != null) return Lang.fromCode(saved)
        val loc = Locale.getDefault()
        return Lang.fromSystem(loc.language, loc.script)
    }

    fun setLang(lang: Lang) {
        prefs.put(KEY, lang.code)
    }

    fun theme(): ThemeMode = ThemeMode.fromCode(prefs.get(KEY_THEME, null))

    fun setTheme(mode: ThemeMode) {
        prefs.put(KEY_THEME, mode.code)
    }
}


/**
 * Disk silme isi. Isletim sisteminin kendi araci calisir; ciktisi
 * satir satir okunup ilerlemeye cevrilir. Arka planda surer.
 */
private fun runDiskWipe(disk: DiskInfo) {
    // fileCount = 0 -> arayuz "dosya 1/1" satirini gizler
    WipeState.update {
        it.copy(
            running = true, finished = false, cancelled = false,
            currentName = disk.display,
            fileIndex = 0, fileCount = 0,
            pass = 0, passCount = DiskWipe.PHASES,
            phase = 0, phaseTotal = DiskWipe.PHASES,
            overall = 0f, bytesDone = 0L,
            bytesTotal = disk.totalBytes * DiskWipe.PHASES,
            statusLine = "",
            startedAtMs = System.currentTimeMillis()
        )
    }

    val ok = DiskWipe.wipe(
        disk = disk,
        onLine = { line -> WipeState.update { it.copy(statusLine = line) } },
        onProgress = { overall, phase ->
            WipeState.update {
                it.copy(
                    overall = overall,
                    phase = phase,
                    pass = phase + 1,
                    bytesDone = (it.bytesTotal * overall).toLong()
                )
            }
        },
        cancelled = { WipeState.cancelRequested }
    )

    val cancelled = WipeState.cancelRequested
    WipeState.update {
        it.copy(
            running = false, finished = true, cancelled = cancelled,
            overall = if (ok) 1f else it.overall,
            results = listOf(
                FileResult(
                    name = disk.display,
                    ok = ok,
                    kind = if (ok) ResultKind.DELETED else ResultKind.ERROR,
                    passes = DiskWipe.PASSES,
                    error = if (ok) null else it.statusLine
                )
            )
        )
    }
}
