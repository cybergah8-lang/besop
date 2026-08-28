package com.cybergah.securewipe

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sabit ayar: 50 gecis, her gecis rastgele veri. */
const val PASSES = 50

/** Arayuzde gosterilen surum. */
const val APP_VERSION = "1.3"

/** Ustune yazma deseni. */
enum class Scheme { RANDOM, DOD, ZERO }

/** Silme sonucu - metin degil, kod tutulur; dile gore ekranda cevrilir. */
enum class ResultKind { DELETED, NOT_DELETED, STOPPED, NO_WRITE, ERROR }

data class FileResult(
    val name: String,
    val ok: Boolean,
    val kind: ResultKind,
    val passes: Int = 0,
    val error: String? = null
)

fun FileResult.detailText(s: Strings): String = when (kind) {
    ResultKind.DELETED -> s.resDeleted.format(passes)
    ResultKind.NOT_DELETED -> s.resNotDeleted
    ResultKind.STOPPED -> s.resStopped
    ResultKind.NO_WRITE -> s.resNoWrite
    ResultKind.ERROR -> error ?: "?"
}

/** Listede duran, henuz silinmemis dosya. */
data class PickedItem(
    val id: String,
    val name: String,
    val size: Long
)

data class WipeProgress(
    val running: Boolean = false,
    val finished: Boolean = false,
    val cancelled: Boolean = false,
    val currentName: String = "",
    val fileIndex: Int = 0,
    val fileCount: Int = 0,
    val pass: Int = 0,
    val passCount: Int = 0,
    val overall: Float = 0f,
    val bytesDone: Long = 0L,
    val bytesTotal: Long = 0L,
    val results: List<FileResult> = emptyList(),
    /** Disk modunda isletim sisteminin canli ciktisi. */
    val statusLine: String = "",
    /** Islemin basladigi an - gecen sure ve tahmini kalan sure icin. */
    val startedAtMs: Long = 0L,
    /** Disk modunda asama: 0 = sifirlama, 1..N = gecisler. */
    val phase: Int = 0,
    val phaseTotal: Int = 0
)

/** 01:23:45 / 12:34 - dile bagli olmayan sure bicimi. */
fun formatDuration(ms: Long): String {
    if (ms < 0) return "--:--"
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val sec = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, sec)
    else String.format("%02d:%02d", m, sec)
}

/**
 * Ilerleme durumu. Android'de servis ile arayuz arasinda,
 * masaustunde is parcacigi ile arayuz arasinda paylasilir.
 */
object WipeState {
    private val _progress = MutableStateFlow(WipeProgress())
    val progress: StateFlow<WipeProgress> = _progress.asStateFlow()

    @Volatile
    var cancelRequested: Boolean = false
        private set

    fun requestCancel() {
        cancelRequested = true
    }

    fun reset() {
        cancelRequested = false
        _progress.value = WipeProgress()
    }

    fun update(block: (WipeProgress) -> WipeProgress) {
        _progress.value = block(_progress.value)
    }
}

fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var v = bytes.toDouble() / 1024.0
    var i = 0
    while (v >= 1024.0 && i < units.size - 1) {
        v /= 1024.0
        i++
    }
    return String.format("%.1f %s", v, units[i])
}
