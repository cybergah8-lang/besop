package com.cybergah.securewipe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat_checkNotifPermission(this) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        setContent {
            var themeMode by remember { mutableStateOf(Prefs.theme(this)) }
            AppTheme(themeMode) {
                AndroidWipeScreen(
                    themeMode = themeMode,
                    onThemeChange = { themeMode = it; Prefs.setTheme(this, it) }
                )
            }
        }
    }
}

private fun ContextCompat_checkNotifPermission(ctx: Context): Int =
    if (Build.VERSION.SDK_INT >= 33) {
        ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        PackageManager.PERMISSION_GRANTED
    }

@Composable
private fun AndroidWipeScreen(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    val vm: WipeViewModel = viewModel()
    val progress by WipeState.progress.collectAsState()

    var lang by remember { mutableStateOf(Prefs.lang(context)) }
    var note by remember { mutableStateOf<String?>(null) }
    val s = stringsFor(lang)

    val pickFiles = rememberLauncherForActivityResult(OpenDocumentsRw()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { persist(context, it) }
        vm.addAll(context, uris)
        note = null
    }

    val pickTree = rememberLauncherForActivityResult(OpenTreeRw()) { tree ->
        if (tree == null) return@rememberLauncherForActivityResult
        persist(context, tree)
        val found = expandTree(context, tree)
        if (found.isEmpty()) {
            note = s.emptyFolder
        } else {
            vm.addAll(context, found)
            note = null
        }
    }

    androidx.compose.foundation.layout.Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        WipeScreen(
            lang = lang,
            onLangChange = {
                lang = it
                Prefs.setLang(context, it)
            },
            items = vm.files,
            progress = progress,
            onPickFiles = { pickFiles.launch(Unit) },
            onPickFolder = { pickTree.launch(Unit) },
            onRemove = { vm.removeAt(it) },
            onClear = { vm.clear() },
            onWipe = {
                WipeState.reset()
                WipeService.start(context, vm.files.map { Uri.parse(it.id) }, lang)
                vm.clear()
            },
            onStop = { WipeService.cancel(context) },
            showBgNote = true,
            note = note,
            themeMode = themeMode,
            onThemeChange = onThemeChange
        )
    }
}

/** Secilen dil kalici olarak saklanir; ilk acilista sistem dilinden tahmin edilir. */
object Prefs {
    private const val FILE = "securewipe"
    private const val KEY_LANG = "lang"
    private const val KEY_THEME = "theme"

    fun lang(ctx: Context): Lang {
        val sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val saved = sp.getString(KEY_LANG, null)
        if (saved != null) return Lang.fromCode(saved)
        val loc: Locale = Locale.getDefault()
        return Lang.fromSystem(loc.language, loc.script)
    }

    fun theme(ctx: Context): ThemeMode = ThemeMode.fromCode(
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_THEME, null)
    )

    fun setTheme(ctx: Context, mode: ThemeMode) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, mode.code).apply()
    }

    fun setLang(ctx: Context, lang: Lang) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang.code)
            .apply()
    }
}

private fun persist(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    } catch (_: SecurityException) {
    }
}
