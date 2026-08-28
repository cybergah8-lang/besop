package com.cybergah.securewipe.desktop

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Locale

enum class DiskKind { REMOVABLE, FIXED, OTHER }

data class DiskInfo(
    /** Windows: "E:"  ·  macOS: "/dev/disk2"  ·  Linux: "/dev/sdb" */
    val id: String,
    val label: String,
    val kind: DiskKind,
    val totalBytes: Long,
    /** Sistem diski silinemez. */
    val isSystem: Boolean,
    /** Kullaniciya gosterilen ad, orn. "E:\  USB Bellek" */
    val display: String
)

enum class Os { WINDOWS, MAC, LINUX, UNKNOWN }

val currentOs: Os by lazy {
    val n = System.getProperty("os.name").lowercase(Locale.ROOT)
    when {
        n.contains("win") -> Os.WINDOWS
        n.contains("mac") || n.contains("darwin") -> Os.MAC
        n.contains("nux") || n.contains("nix") -> Os.LINUX
        else -> Os.UNKNOWN
    }
}

/**
 * DISK SILME
 *
 * Ham sektor yazmayi Java'da taklit etmek yerine isletim sisteminin kendi
 * aracini kullaniyoruz - daha guvenilir ve native kutuphane gerektirmiyor:
 *
 *   Windows : format <surucu> /FS:NTFS /P:3   -> once her sektore sifir,
 *             sonra 3 kez rastgele veri, ardindan yeniden bicimlendirme
 *   macOS   : diskutil secureErase 4 <aygit>  -> US DoE 3 gecis
 *             + diskutil eraseDisk ...        -> yeniden bicimlendirme
 *   Linux   : shred -n 3 <aygit> + mkfs.ext4
 *
 * NOT: Windows disinda test edilmedi (gelistirme makinesi Windows).
 */
object DiskWipe {

    const val PASSES = 3
    const val VOLUME_LABEL = "BESOP"

    // ------------------------------------------------------------------ listeleme

    fun list(): List<DiskInfo> = when (currentOs) {
        Os.WINDOWS -> listWindows()
        Os.MAC -> listMac()
        Os.LINUX -> listLinux()
        Os.UNKNOWN -> emptyList()
    }

    private fun listWindows(): List<DiskInfo> {
        val systemDrive = (System.getenv("SystemDrive") ?: "C:").uppercase(Locale.ROOT)
        val script =
            "Get-CimInstance Win32_LogicalDisk | ForEach-Object { " +
                "'{0}|{1}|{2}|{3}' -f \$_.DeviceID, \$_.VolumeName, \$_.DriveType, \$_.Size }"
        return runLines(powershell(script))
            .mapNotNull { line ->
                val p = line.trim().split("|")
                if (p.size < 4 || p[0].isBlank()) return@mapNotNull null
                val id = p[0].uppercase(Locale.ROOT)
                val name = p[1].ifBlank { "" }
                val kind = when (p[2].trim()) {
                    "2" -> DiskKind.REMOVABLE
                    "3" -> DiskKind.FIXED
                    else -> DiskKind.OTHER
                }
                if (kind == DiskKind.OTHER) return@mapNotNull null
                val size = p[3].trim().toLongOrNull() ?: 0L
                val isSystem = id == systemDrive
                DiskInfo(
                    id = id,
                    label = name,
                    kind = kind,
                    totalBytes = size,
                    isSystem = isSystem,
                    display = buildString {
                        append(id).append('\\')
                        if (name.isNotBlank()) append("  ").append(name)
                    }
                )
            }
    }

    private fun listMac(): List<DiskInfo> {
        // "diskutil list" ciktisindan sadece tum diskleri (disk0, disk1 ...) aliriz
        val out = runLines(listOf("diskutil", "list"))
        val disks = out.filter { it.startsWith("/dev/disk") }
            .map { it.substringBefore(" ").trim() }
        val rootDev = runLines(listOf("sh", "-c", "df / | tail -1 | awk '{print \$1}'"))
            .firstOrNull()?.trim().orEmpty()
        return disks.mapNotNull { dev ->
            val info = runLines(listOf("diskutil", "info", dev)).associate { l ->
                val i = l.indexOf(':')
                if (i > 0) l.substring(0, i).trim() to l.substring(i + 1).trim() else "" to ""
            }
            val size = Regex("\\((\\d+) Bytes\\)").find(info["Disk Size"] ?: "")
                ?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val internal = (info["Device Location"] ?: "").contains("Internal", true)
            val isSystem = rootDev.startsWith(dev)
            DiskInfo(
                id = dev,
                label = info["Volume Name"] ?: info["Device / Media Name"] ?: "",
                kind = if (internal) DiskKind.FIXED else DiskKind.REMOVABLE,
                totalBytes = size,
                isSystem = isSystem,
                display = dev + "  " + (info["Device / Media Name"] ?: "")
            )
        }
    }

    private fun listLinux(): List<DiskInfo> {
        val rootDev = runLines(listOf("sh", "-c", "findmnt -n -o SOURCE /"))
            .firstOrNull()?.trim().orEmpty()
        return runLines(listOf("lsblk", "-dnb", "-o", "NAME,SIZE,RM,MODEL"))
            .mapNotNull { line ->
                val p = line.trim().split(Regex("\\s+"), limit = 4)
                if (p.size < 3) return@mapNotNull null
                val dev = "/dev/" + p[0]
                val size = p[1].toLongOrNull() ?: 0L
                val removable = p[2] == "1"
                DiskInfo(
                    id = dev,
                    label = p.getOrElse(3) { "" },
                    kind = if (removable) DiskKind.REMOVABLE else DiskKind.FIXED,
                    totalBytes = size,
                    isSystem = rootDev.startsWith(dev),
                    display = dev + "  " + p.getOrElse(3) { "" }
                )
            }
    }

    // ------------------------------------------------------------------ yetki

    fun isElevated(): Boolean = when (currentOs) {
        Os.WINDOWS -> runLines(
            powershell(
                "([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]" +
                    "::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)"
            )
        ).any { it.trim().equals("True", true) }

        Os.MAC, Os.LINUX -> runLines(listOf("id", "-u")).firstOrNull()?.trim() == "0"
        Os.UNKNOWN -> false
    }

    /** Uygulamayi yonetici olarak yeniden baslatir (Windows). */
    fun restartElevated(): Boolean {
        if (currentOs != Os.WINDOWS) return false
        val exe = ProcessHandle.current().info().command().orElse(null) ?: return false
        return try {
            ProcessBuilder(
                powershell("Start-Process -FilePath '" + exe.replace("'", "''") + "' -Verb RunAs")
            ).start()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------ silme

    /**
     * Diski siler. Uzun surer; cagiran taraf arka plan is parcaciginda calistirmali.
     * @param onLine isletim sisteminden gelen her satir
     * @param onPercent 0..100 (yuzde okunabildiginde)
     * @param cancelled durdurma istegi
     */
    /** Toplam asama: 1 sifirlama + PASSES rastgele gecis. */
    const val PHASES = PASSES + 1

    fun wipe(
        disk: DiskInfo,
        onLine: (String) -> Unit,
        onProgress: (overall: Float, phase: Int) -> Unit,
        cancelled: () -> Boolean
    ): Boolean {
        if (disk.isSystem) {
            onLine("REFUSED: system disk")
            return false
        }
        val cmd: List<String> = when (currentOs) {
            Os.WINDOWS -> listOf(
                "cmd", "/c",
                "format ${disk.id} /FS:NTFS /V:$VOLUME_LABEL /P:$PASSES /Y"
            )

            Os.MAC -> listOf(
                "sh", "-c",
                // 4 = US DoE, 3 gecis; ardindan yeniden bicimlendir
                "diskutil secureErase 4 ${disk.id} && " +
                    "diskutil eraseDisk JHFS+ $VOLUME_LABEL ${disk.id}"
            )

            Os.LINUX -> listOf(
                "sh", "-c",
                "shred -v -n $PASSES ${disk.id} && " +
                    "mkfs.ext4 -F -L $VOLUME_LABEL ${disk.id}"
            )

            Os.UNKNOWN -> return false
        }

        return try {
            val pb = ProcessBuilder(cmd).redirectErrorStream(true)
            val proc = pb.start()

            // format komutu cikarilabilir aygitlarda ENTER bekleyebilir
            Thread {
                runCatching {
                    proc.outputStream.bufferedWriter().use { w ->
                        repeat(3) { w.write("\n"); w.flush(); Thread.sleep(400) }
                    }
                }
            }.apply { isDaemon = true }.start()

            val pctRe = Regex("""(\d{1,3})\s*percent""", RegexOption.IGNORE_CASE)
            // format /P:N once tum sektorleri sifirlar, sonra N kez rastgele yazar.
            // Her asamada yuzde 0'dan yeniden baslar; dususu yakalayip asama sayariz.
            var phase = 0
            var lastPct = 0

            BufferedReader(InputStreamReader(proc.inputStream)).use { r ->
                while (true) {
                    if (cancelled()) {
                        killTree(proc)
                        onLine("CANCELLED")
                        return false
                    }
                    val line = r.readLine() ?: break
                    val t = line.trim()
                    if (t.isEmpty()) continue
                    onLine(t)

                    val pct = pctRe.find(t)?.groupValues?.get(1)?.toIntOrNull() ?: continue
                    // yuzde belirgin sekilde geri dustuyse yeni asamaya gecilmis
                    if (pct + 10 < lastPct && phase < PHASES - 1) phase++
                    lastPct = pct
                    val overall = ((phase + pct / 100f) / PHASES).coerceIn(0f, 1f)
                    onProgress(overall, phase)
                }
            }
            val code = proc.waitFor()
            if (code == 0) onProgress(1f, PHASES - 1)
            code == 0
        } catch (e: Exception) {
            onLine("ERROR: " + (e.message ?: e.javaClass.simpleName))
            false
        }
    }

    /**
     * cmd /c format ... seklinde calistirdigimiz icin proc.destroy() sadece
     * cmd.exe'yi oldururdu, asil format.com calismaya devam ederdi.
     * Once cocuk sureclerin hepsini, sonra kendisini zorla oldururuz.
     */
    private fun killTree(proc: Process) {
        runCatching { proc.descendants().forEach { it.destroyForcibly() } }
        runCatching { proc.destroyForcibly() }
        runCatching { proc.waitFor() }
    }

    // ------------------------------------------------------------------ yardimci


    /**
     * PowerShell'i -EncodedCommand ile calistirir.
     * Windows'ta arguman tirnaklamasi ic ice tirnak ve | karakterlerini bozuyor;
     * Base64 (UTF-16LE) kodlama bu sorunu tamamen ortadan kaldirir.
     */
    private fun powershell(script: String): List<String> {
        val b64 = java.util.Base64.getEncoder()
            .encodeToString(script.toByteArray(Charsets.UTF_16LE))
        return listOf("powershell", "-NoProfile", "-NonInteractive", "-EncodedCommand", b64)
    }

    private fun runLines(cmd: List<String>): List<String> = try {
        val p = ProcessBuilder(cmd).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readLines()
        p.waitFor()
        out
    } catch (_: Exception) {
        emptyList()
    }
}
