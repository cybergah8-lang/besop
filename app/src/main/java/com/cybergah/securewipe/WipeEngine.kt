package com.cybergah.securewipe

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.Random

/**
 * Tek bir dosyayi kurtarilamaz hale getirir:
 *   1) Icerigin uzerine N kez yazar (her gecisten sonra fsync -> disk onbellegi bosaltilir)
 *   2) Dosyayi 0 byte'a indirir (truncate)
 *   3) Kalici olarak siler (cop kutusuna tasimaz)
 *   4) Gercekten gittigini dogrular
 *
 * NOT: Silmeden once "rastgele isim ver" adimi denenmisti; SAF izni belge kimligine
 * bagli oldugu icin isim degisince izin dusuyor ve silme sessizce basarisiz oluyordu.
 */
class WipeEngine(context: Context) {

    private val cr: ContentResolver = context.contentResolver

    /** Buyuk veri icin SecureRandom yavas; tohumu SecureRandom'dan alan hizli PRNG yeterli. */
    private val rnd = Random(SecureRandom().nextLong())

    private enum class Fill { RANDOM, ZERO, ONE }

    companion object {
        const val CHUNK = 1 shl 20 // 1 MiB
    }

    fun displayName(uri: Uri): String {
        try {
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) return c.getString(0)
            }
        } catch (_: Exception) {
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "?"
    }

    fun sizeOf(uri: Uri): Long {
        try {
            cr.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) return c.getLong(0)
            }
        } catch (_: Exception) {
        }
        return try {
            cr.openFileDescriptor(uri, "r")?.use { it.statSize.coerceAtLeast(0L) } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * @param onChunk her yazilan blok sonrasi cagirilir (yazilanByte, kacinciGecis)
     */
    fun wipe(
        uri: Uri,
        passes: Int,
        scheme: Scheme,
        onChunk: (written: Int, pass: Int) -> Unit
    ): FileResult {
        val name = displayName(uri)
        return try {
            val size = sizeOf(uri)
            if (size > 0) {
                val buf = ByteArray(CHUNK)
                for (pass in 1..passes) {
                    if (WipeState.cancelRequested) {
                        return FileResult(name, false, ResultKind.STOPPED)
                    }

                    val fd = openWritable(uri)
                        ?: return FileResult(name, false, ResultKind.NO_WRITE)

                    fd.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { out ->
                            val fill = fillFor(pass, scheme)
                            if (fill == Fill.ZERO) buf.fill(0)
                            if (fill == Fill.ONE) buf.fill(0xFF.toByte())

                            var written = 0L
                            while (written < size && !WipeState.cancelRequested) {
                                if (fill == Fill.RANDOM) rnd.nextBytes(buf)
                                val n = minOf(CHUNK.toLong(), size - written).toInt()
                                out.write(buf, 0, n)
                                written += n
                                onChunk(n, pass)
                            }
                            out.flush()
                            // Onbellekte kalmasin, gercekten diske insin:
                            try {
                                pfd.fileDescriptor.sync()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
            if (WipeState.cancelRequested) return FileResult(name, false, ResultKind.STOPPED)

            // 2) Boyutu sifirla
            try {
                cr.openFileDescriptor(uri, "rwt")?.close()
            } catch (_: Exception) {
            }

            // 3) Kalici sil
            val deleted = try {
                DocumentsContract.deleteDocument(cr, uri)
            } catch (_: Exception) {
                false
            }

            // 4) Gercekten gitti mi diye dogrula
            val stillExists = exists(uri)

            when {
                !stillExists || deleted -> FileResult(name, true, ResultKind.DELETED, passes)
                else -> FileResult(name, false, ResultKind.NOT_DELETED, passes)
            }
        } catch (e: Exception) {
            FileResult(name, false, ResultKind.ERROR, error = e.message ?: e.javaClass.simpleName)
        }
    }

    /** Bazi saglayicilar "rw" desteklemez; "w" ile devam ederiz (truncate etmez). */
    private fun openWritable(uri: Uri): ParcelFileDescriptor? =
        try {
            cr.openFileDescriptor(uri, "rw")
        } catch (_: Exception) {
            try {
                cr.openFileDescriptor(uri, "w")
            } catch (_: Exception) {
                null
            }
        }

    private fun fillFor(pass: Int, scheme: Scheme): Fill = when (scheme) {
        Scheme.ZERO -> Fill.ZERO
        Scheme.RANDOM -> Fill.RANDOM
        Scheme.DOD -> when ((pass - 1) % 3) {
            0 -> Fill.ZERO
            1 -> Fill.ONE
            else -> Fill.RANDOM
        }
    }

    /** Silme sonrasi belge hala duruyor mu? */
    private fun exists(uri: Uri): Boolean = try {
        cr.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
            ?.use { it.count > 0 } ?: false
    } catch (_: Exception) {
        false
    }
}
