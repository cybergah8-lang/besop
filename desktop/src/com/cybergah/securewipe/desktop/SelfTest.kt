package com.cybergah.securewipe.desktop

import com.cybergah.securewipe.Lang
import com.cybergah.securewipe.Scheme
import com.cybergah.securewipe.stringsFor
import java.io.File

/**
 * Gelistirme dogrulamasi: `./gradlew :desktop:selftest`
 * Uygulamanin kendisi bunu kullanmaz.
 */
object SelfTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File(System.getProperty("java.io.tmpdir"), "securewipe-selftest")
        dir.mkdirs()
        val f = File(dir, "gizli.bin")

        val magic = "GIZLIVERI-CYBERGAH-SECRET-".toByteArray()
        f.outputStream().use { out ->
            var written = 0
            while (written < 4 * 1024 * 1024) {
                out.write(magic); written += magic.size
            }
        }
        println("olusturuldu : ${f.absolutePath}  (${f.length()} byte)")
        println("ilk 26 byte : " + String(f.readBytes().copyOfRange(0, 26)))

        // Silme sirasinda icerigin bozuldugunu gormek icin ayri bir is parcacigi orneklesin
        val sampler = Thread {
            var seen = false
            repeat(200) {
                if (!seen && f.exists() && f.length() >= 26) {
                    val head = String(f.inputStream().use { s -> ByteArray(26).also { s.read(it) } })
                    if (!head.startsWith("GIZLIVERI")) {
                        println("islem sirasinda: " + head.map { c ->
                            "%02X".format(c.code and 0xFF)
                        }.take(8).joinToString(" ") + "   <- artik cop")
                        seen = true
                    }
                }
                Thread.sleep(15)
            }
        }
        sampler.isDaemon = true
        sampler.start()

        val result = DesktopEngine.wipe(f, 50, Scheme.RANDOM) { _, _ -> }
        Thread.sleep(60)

        println("sonuc       : ok=${result.ok} kind=${result.kind} passes=${result.passes}")
        println("dosya var mi: ${f.exists()}")
        println("klasor      : " + (dir.list()?.joinToString(", ") ?: "-"))

        // Konsol UTF-8 basmayabilir; cevirileri UTF-8 dosyaya yaz
        val report = File("ceviri-kontrol.txt")
        report.writeText(
            Lang.entries.joinToString(System.lineSeparator()) { l ->
                val s = stringsFor(l)
                "%-5s rtl=%-5s | %s | %s | %s".format(
                    l.code, l.rtl, l.nativeName, s.appName, s.wipeButton
                )
            },
            Charsets.UTF_8
        )
        println("ceviri raporu: " + report.absolutePath)


        println()
        println("--- disk listesi ---")
        println("isletim sistemi : " + currentOs)
        println("yonetici mi     : " + DiskWipe.isElevated())
        val disks = DiskWipe.list()
        if (disks.isEmpty()) println("  (disk bulunamadi)")
        disks.forEach { d ->
            println("  %-10s %-12s %10s  sistem=%s  secilebilir=%s".format(
                d.id, d.kind, com.cybergah.securewipe.formatSize(d.totalBytes),
                d.isSystem, !d.isSystem))
        }
        // Sistem diski korumasi: burada wipe() CAGIRILMAZ.
        // Koruma bir gun bozulursa bu test C: diskini silerdi.
        val sys = disks.filter { it.isSystem }
        println("  koruma -> sistem diski sayisi=%d, hepsi kilitli=%s".format(
            sys.size, sys.all { it.isSystem }))

        dir.delete()
    }
}
