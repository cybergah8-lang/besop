package com.cybergah.securewipe

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WipeService : Service() {

    companion object {
        const val ACTION_START = "com.cybergah.securewipe.START"
        const val ACTION_CANCEL = "com.cybergah.securewipe.CANCEL"
        const val EXTRA_URIS = "uris"
        const val EXTRA_LANG = "lang"

        private const val CHANNEL_ID = "wipe_progress"
        private const val NOTIF_ID = 1001

        fun start(context: Context, uris: List<Uri>, lang: Lang) {
            val i = Intent(context, WipeService::class.java).apply {
                action = ACTION_START
                putParcelableArrayListExtra(EXTRA_URIS, ArrayList(uris))
                putExtra(EXTRA_LANG, lang.code)
            }
            context.startForegroundService(i)
        }

        fun cancel(context: Context) {
            WipeState.requestCancel()
            context.startService(
                Intent(context, WipeService::class.java).apply { action = ACTION_CANCEL }
            )
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastNotify = 0L
    private var s: Strings = stringsFor(Lang.DEFAULT)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            WipeState.requestCancel()
            return START_NOT_STICKY
        }

        s = stringsFor(Lang.fromCode(intent?.getStringExtra(EXTRA_LANG)))
        createChannel()

        val uris: List<Uri> = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableArrayListExtra(EXTRA_URIS, Uri::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: emptyList()
        }

        if (uris.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundCompat(buildNotification(s.notifPreparing, 0, 0))
        acquireWakeLock()
        scope.launch { runWipe(uris) }
        return START_NOT_STICKY
    }

    private fun runWipe(uris: List<Uri>) {
        val engine = WipeEngine(this)
        val passes = PASSES

        val sizes = uris.map { engine.sizeOf(it) }
        val bytesTotal = sizes.sumOf { it * passes }
        var bytesDone = 0L
        val results = mutableListOf<FileResult>()

        WipeState.update {
            it.copy(
                running = true,
                finished = false,
                cancelled = false,
                fileCount = uris.size,
                passCount = passes,
                bytesTotal = bytesTotal,
                startedAtMs = System.currentTimeMillis()
            )
        }

        uris.forEachIndexed { index, uri ->
            if (WipeState.cancelRequested) return@forEachIndexed

            val name = engine.displayName(uri)
            WipeState.update { it.copy(currentName = name, fileIndex = index + 1, pass = 1) }

            val result = engine.wipe(uri, passes, Scheme.RANDOM) { written, pass ->
                bytesDone += written
                val frac =
                    if (bytesTotal > 0) (bytesDone.toDouble() / bytesTotal).toFloat() else 0f
                WipeState.update {
                    it.copy(pass = pass, bytesDone = bytesDone, overall = frac.coerceIn(0f, 1f))
                }
                pushNotification(name, pass, passes, frac)
            }

            // Bos dosyada onChunk hic cagirilmaz; ilerleme cubugunu yine de esitle.
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

        showSummary(results, wasCancelled)
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ---------- bildirimler ----------

    private fun createChannel() {
        val ch = NotificationChannel(
            CHANNEL_ID,
            s.notifChannel,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = s.notifChannelDesc
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun contentIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun buildNotification(text: String, progress: Int, max: Int): Notification {
        val cancelPi = PendingIntent.getService(
            this,
            1,
            Intent(this, WipeService::class.java).apply { action = ACTION_CANCEL },
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(s.notifRunning)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notify)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setProgress(max, progress, max == 0)
            .addAction(Notification.Action.Builder(null, s.notifStop, cancelPi).build())
            .build()
    }

    private fun pushNotification(name: String, pass: Int, passes: Int, frac: Float) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNotify < 400) return
        lastNotify = now
        val text = name + "  ·  " + s.passOf.format(pass, passes)
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(text, (frac * 100).toInt(), 100))
    }

    private fun showSummary(results: List<FileResult>, cancelled: Boolean) {
        val ok = results.count { it.ok }
        val fail = results.size - ok
        val title = when {
            cancelled -> s.notifStopped
            fail == 0 -> s.notifDone
            else -> s.notifFailed.format(fail)
        }
        val n = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(s.notifSummary.format(ok))
            .setSmallIcon(R.drawable.ic_notify)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID + 1, n)
    }

    private fun startForegroundCompat(n: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    // ---------- wakelock ----------

    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GuvenliSilme::wipe").apply {
            setReferenceCounted(false)
            acquire(6 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.takeIf { it.isHeld }?.release()
        } catch (_: Exception) {
        }
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }
}
