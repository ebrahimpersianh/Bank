package ir.sadteam.volume

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager

/**
 * باریکه‌ی صدا با پنجره‌ی معمولیِ «نمایش روی برنامه‌های دیگر».
 * اگر [VolumeA11yService] روشن باشد، این کنار می‌رود: آن یکی وسطِ تماس هم دیده می‌شود
 * (صفحه‌ی تماسِ گوشی پنجره‌های معمولی را پنهان می‌کند).
 */
class OverlayService : Service() {

    private lateinit var host: PillHost
    private lateinit var prefs: Prefs

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        host = PillHost(this, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        goForeground()
        if (VolumeA11yService.running || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        addPill()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP || VolumeA11yService.running) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!host.added && Settings.canDrawOverlays(this)) addPill()
        if (intent?.action == ACTION_SHOW) host.show()
        return START_STICKY
    }

    private fun addPill() {
        val ok = host.add()
        lastError = if (ok) null else "add_failed"
        showing = ok
    }

    /** اگر کاربر برنامه را از فهرستِ اخیر کنار زد، دوباره بالا بیا. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (prefs.enabled) start(this)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        host.remove()
        showing = false
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        host.onConfigurationChanged()
    }

    // ── اعلانِ ثابت (اندروید بدونِ آن سرویس را می‌بندد) ──────────────────────────────

    private fun goForeground() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "دکمه‌ی صدا", NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
                description = "تا وقتی این اعلان هست، دکمه‌ی صدا روی صفحه می‌ماند."
            },
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentTitle("دکمه‌ی صدا روشن است")
            .setContentText("برای تنظیمات بزن")
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        runCatching {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIF_ID, n)
            }
        }.onFailure { lastError = "fg_failed" }
    }

    companion object {
        private const val CHANNEL = "pill"
        private const val NOTIF_ID = 1
        const val ACTION_STOP = "ir.sadteam.volume.STOP"
        const val ACTION_SHOW = "ir.sadteam.volume.SHOW"

        /** برای صفحه‌ی اصلی: دکمه الان روی صفحه است؟ */
        @Volatile var showing = false
        @Volatile var lastError: String? = null

        /** روشن کردن؛ اگر سرویسِ دسترسی‌پذیری فعال است، همان باریکه را نشان می‌دهد. */
        fun start(context: Context) {
            if (VolumeA11yService.running) {
                VolumeA11yService.instance?.ensurePill()
                return
            }
            runCatching { context.startForegroundService(Intent(context, OverlayService::class.java)) }
        }

        fun show(context: Context) {
            if (VolumeA11yService.running) {
                VolumeA11yService.instance?.let { it.ensurePill(); it.showPill() }
                return
            }
            val i = Intent(context, OverlayService::class.java).setAction(ACTION_SHOW)
            runCatching { context.startForegroundService(i) }
        }

        fun stop(context: Context) {
            VolumeA11yService.instance?.hidePill()
            val i = Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
            runCatching { context.startService(i) }
        }

        /** باریکه الان روی صفحه است (از هر کدام از دو راه)؟ */
        fun isShowing() = showing || VolumeA11yService.instance?.pillShowing == true
    }
}
