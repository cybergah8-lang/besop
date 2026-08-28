package com.cybergah.securewipe.desktop

import com.cybergah.securewipe.FileResult
import com.cybergah.securewipe.ResultKind
import com.cybergah.securewipe.Scheme
import com.cybergah.securewipe.WipeState
import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom
import java.util.Random

/**
 * Masaustu silme motoru.
 *
 * Android'den farkli olarak burada dogrudan dosya sistemine erisimimiz var,
 * bu yuzden silmeden once dosya adini da birkac kez rastgeleye cevirebiliyoruz
 * (dizin kaydindaki eski isim de bozulur). Android'de bu adim SAF izniyle
 * catistigi icin yok.
 *
 * Sira:
 *   1) Icerigin uzerine N kez yaz, her gecisten sonra fsync
 *   2) Boyutu 0'a indir
 *   3) Adi 3 kez rastgeleye cevir
 *   4) Sil ve gercekten gittigini dogrula
 */
object DesktopEngine {

    private const val CHUNK = 8 shl 20 // 8 MiB - disk daha iyi doyar
    private const val NAME_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"

    private val rnd = Random(SecureRandom().nextLong())

    private enum class Fill { RANDOM, ZERO, ONE }

    fun wipe(
        file: File,
        passes: Int,
        scheme: Scheme,
        onChunk: (written: Int, pass: Int) -> Unit
    ): FileResult {
        val name = file.name
        return try {
            if (!file.exists() || !file.isFile) {
                return FileResult(name, false, ResultKind.ERROR, error = "not found")
            }
            // Windows'ta salt-okunur bayragi varsa kaldir
            if (!file.canWrite()) file.setWritable(true)

            val size = file.length()
            if (size > 0) {
                val buf = ByteArray(CHUNK)
                for (pass in 1..passes) {
                    if (WipeState.cancelRequested) {
                        return FileResult(name, false, ResultKind.STOPPED)
                    }
                    RandomAccessFile(file, "rw").use { raf ->
                        val fill = fillFor(pass, scheme)
                        if (fill == Fill.ZERO) buf.fill(0)
                        if (fill == Fill.ONE) buf.fill(0xFF.toByte())

                        raf.seek(0)
                        var written = 0L
                        while (written < size && !WipeState.cancelRequested) {
                            if (fill == Fill.RANDOM) rnd.nextBytes(buf)
                            val n = minOf(CHUNK.toLong(), size - written).toInt()
                            raf.write(buf, 0, n)
                            written += n
                            onChunk(n, pass)
                        }
                        // Onbellekte kalmasin, gercekten diske insin
                        raf.channel.force(true)
                    }
                }
            }
            if (WipeState.cancelRequested) return FileResult(name, false, ResultKind.STOPPED)

            // 2) Boyutu sifirla
            try {
                RandomAccessFile(file, "rw").use { it.setLength(0) }
            } catch (_: Exception) {
            }

            // 3) Adi rastgeleye cevir (dizin kaydindaki eski isim de bozulsun)
            var current = file
            repeat(3) {
                val candidate = File(current.parentFile, randomName())
                if (current.renameTo(candidate)) current = candidate
            }

            // 4) Sil ve dogrula
            val deleted = current.delete()
            if (deleted || !current.exists()) {
                FileResult(name, true, ResultKind.DELETED, passes)
            } else {
                FileResult(name, false, ResultKind.NOT_DELETED, passes)
            }
        } catch (e: SecurityException) {
            FileResult(name, false, ResultKind.NO_WRITE)
        } catch (e: Exception) {
            FileResult(name, false, ResultKind.ERROR, error = e.message ?: e.javaClass.simpleName)
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

    private fun randomName(): String {
        val sb = StringBuilder(12)
        repeat(12) { sb.append(NAME_CHARS[rnd.nextInt(NAME_CHARS.length)]) }
        return sb.toString()
    }

    /** Klasoru duz dosya listesine acar. */
    fun expandFolder(root: File, depth: Int = 0, out: MutableList<File> = mutableListOf()): List<File> {
        if (depth > 12) return out
        root.listFiles()?.forEach { f ->
            if (f.isDirectory) expandFolder(f, depth + 1, out) else if (f.isFile) out.add(f)
        }
        return out
    }
}
