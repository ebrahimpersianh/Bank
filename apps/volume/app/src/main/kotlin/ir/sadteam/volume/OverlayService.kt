package ir.sadteam.volume

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * قرصِ شناورِ صدا، همیشه روی صفحه (حتی وسطِ تماس).
 *
 * لمس‌ها:
 * - زدن روی نیمه‌ی بالا/پایین → یک پله بلندتر/آهسته‌تر
 * - کشیدنِ بالا/پایین → هر [STEP_DP] یک پله
 * - دو بار زدن → بی‌صدا / برگشت
 * - نگه‌داشتن تا بلرزد ([HOLD_MS]) → از همان لحظه به هر طرف کشیده می‌شود؛ ول کنی به نزدیک‌ترین لبه می‌چسبد
 * - ۵ ثانیه دست نزنی → به لبه می‌رود و فقط باریکه‌ای پیدا است؛ زدنِ باریکه برش می‌گرداند
 */
class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var view: PillView
    private lateinit var lp: WindowManager.LayoutParams
    private lateinit var volume: Volume
    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())
    private var added = false
    private var docked = false
    private var anim: ValueAnimator? = null

    private val density get() = resources.displayMetrics.density
    private fun dp(v: Float) = (v * density).roundToInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        volume = Volume(this)
        prefs = Prefs(this)
        goForeground()
        if (Settings.canDrawOverlays(this)) addPill() else stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            prefs.enabled = false
            stopSelf()
            return START_NOT_STICKY
        }
        if (!added && Settings.canDrawOverlays(this)) addPill()
        if (intent?.action == ACTION_SHOW && added) {
            // «نشانم بده»: وسطِ لبه، کامل بیرون.
            lp.y = (screenH() - pillH()) / 2
            prefs.y = lp.y
            docked = true
            wake()
        }
        return START_STICKY
    }

    /** اگر کاربر برنامه را از فهرستِ اخیر کنار زد، دوباره بالا بیا. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (prefs.enabled) start(this)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        anim?.cancel()
        if (added) runCatching { wm.removeView(view) }
        added = false
        showing = false
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!added) return
        lp.y = lp.y.coerceIn(0, maxY())
        lp.x = edgeX(docked)
        runCatching { wm.updateViewLayout(view, lp) }
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

    // ── قرص ──────────────────────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun addPill() {
        view = PillView(this)
        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            // LEFT نه START: مختصات در گوشیِ فارسی هم از چپ شمرده شود.
            gravity = Gravity.TOP or Gravity.LEFT
            y = prefs.y.takeIf { it >= 0 } ?: (screenH() * 0.38f).roundToInt()
        }
        lp.x = edgeX(false)
        view.setOnTouchListener { _, e -> onTouch(e) }
        // بعضی گوشی‌ها (شیائومی) بی اجازه‌ی پنجره‌ی بازشو خطا می‌دهند؛ برنامه نباید بسته شود.
        added = runCatching { wm.addView(view, lp) }.isSuccess
        lastError = if (added) null else "add_failed"
        if (!added) return
        showing = true
        refresh()
        scheduleDock()
    }

    private fun screenW(): Int = if (Build.VERSION.SDK_INT >= 30) wm.currentWindowMetrics.bounds.width() else resources.displayMetrics.widthPixels
    private fun screenH(): Int = if (Build.VERSION.SDK_INT >= 30) wm.currentWindowMetrics.bounds.height() else resources.displayMetrics.heightPixels
    private fun pillW() = dp(PillView.WIDTH_DP)
    private fun pillH() = dp(PillView.HEIGHT_DP)
    private fun maxY() = (screenH() - pillH()).coerceAtLeast(0)

    /** x برای لبه‌ی فعلی؛ [dock] = فقط باریکه‌ای پیدا باشد. */
    private fun edgeX(dock: Boolean): Int {
        val edge = dp(EDGE_DP)
        val peek = dp(PEEK_DP)
        return if (prefs.onLeft) {
            if (dock) peek - pillW() else edge
        } else {
            if (dock) screenW() - peek else screenW() - pillW() - edge
        }
    }

    private fun animateX(to: Int, alpha: Float) {
        anim?.cancel()
        view.animate().alpha(alpha).setDuration(ANIM_MS).start()
        anim = ValueAnimator.ofInt(lp.x, to).apply {
            duration = ANIM_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                lp.x = it.animatedValue as Int
                if (added) runCatching { wm.updateViewLayout(view, lp) }
            }
            start()
        }
    }

    private val dockRunnable = Runnable {
        if (!moving) {
            docked = true
            animateX(edgeX(true), 0.85f)
        }
    }

    private fun scheduleDock() {
        handler.removeCallbacks(dockRunnable)
        handler.postDelayed(dockRunnable, IDLE_MS)
    }

    private fun wake() {
        if (docked) {
            docked = false
            animateX(edgeX(false), 1f)
        }
        refresh()
        scheduleDock()
    }

    private fun refresh() {
        view.level = volume.level()
        view.muted = volume.isMuted()
    }

    // ── لمس ──────────────────────────────────────────────────────────────────────

    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var startVol = 0
    private var sliding = false
    private var moving = false
    private var ignore = false
    private var lastTapAt = 0L
    private var pendingTap: Runnable? = null

    private val holdRunnable = Runnable {
        if (!sliding) {
            moving = true
            view.moving = true
            buzz()
        }
    }

    private fun onTouch(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (docked) {
                    // باریکه‌ی کنارِ صفحه: فقط برگرداندن.
                    ignore = true
                    wake()
                    return true
                }
                ignore = false
                handler.removeCallbacks(dockRunnable)
                downX = e.rawX; downY = e.rawY
                startX = lp.x; startY = lp.y
                startVol = volume.current()
                sliding = false
                moving = false
                handler.postDelayed(holdRunnable, HOLD_MS)
            }
            MotionEvent.ACTION_MOVE -> {
                if (ignore) return true
                val dx = e.rawX - downX
                val dy = e.rawY - downY
                if (moving) {
                    lp.x = (startX + dx).roundToInt().coerceIn(-pillW() / 2, screenW() - pillW() / 2)
                    lp.y = (startY + dy).roundToInt().coerceIn(0, maxY())
                    runCatching { wm.updateViewLayout(view, lp) }
                    return true
                }
                if (!sliding && abs(dy) > dp(SLOP_DP)) {
                    sliding = true
                    handler.removeCallbacks(holdRunnable)
                }
                if (sliding) {
                    val steps = (-dy / dp(STEP_DP)).roundToInt()
                    val target = startVol + steps
                    if (target != volume.current()) {
                        volume.set(target)
                        refresh()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(holdRunnable)
                if (ignore) { ignore = false; return true }
                if (moving) {
                    moving = false
                    view.moving = false
                    prefs.onLeft = lp.x + pillW() / 2 < screenW() / 2
                    prefs.y = lp.y
                    animateX(edgeX(false), 1f)
                } else if (!sliding && e.actionMasked == MotionEvent.ACTION_UP) {
                    onTap(e.y < view.height / 2f)
                }
                scheduleDock()
            }
        }
        return true
    }

    /** یک بار زدن = یک پله؛ دو بار زدن (کمتر از [DOUBLE_TAP_MS]) = بی‌صدا. */
    private fun onTap(upperHalf: Boolean) {
        val now = SystemClock.uptimeMillis()
        pendingTap?.let { handler.removeCallbacks(it) }
        if (now - lastTapAt < DOUBLE_TAP_MS) {
            lastTapAt = 0L
            pendingTap = null
            volume.toggleMute()
            refresh()
            return
        }
        lastTapAt = now
        val r = Runnable {
            volume.step(if (upperHalf) 1 else -1)
            refresh()
        }
        pendingTap = r
        handler.postDelayed(r, DOUBLE_TAP_MS)
    }

    private fun buzz() {
        val v: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching { v?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    companion object {
        private const val CHANNEL = "pill"
        private const val NOTIF_ID = 1
        const val ACTION_STOP = "ir.sadteam.volume.STOP"
        const val ACTION_SHOW = "ir.sadteam.volume.SHOW"

        /** برای صفحه‌ی اصلی: دکمه الان روی صفحه است؟ */
        @Volatile var showing = false
        @Volatile var lastError: String? = null

        private const val HOLD_MS = 1200L
        private const val IDLE_MS = 5000L
        private const val DOUBLE_TAP_MS = 280L
        private const val ANIM_MS = 300L
        private const val STEP_DP = 16f
        private const val SLOP_DP = 12f
        private const val EDGE_DP = 6f
        private const val PEEK_DP = 10f

        fun start(context: Context) {
            val i = Intent(context, OverlayService::class.java)
            runCatching { context.startForegroundService(i) }
        }

        fun show(context: Context) {
            val i = Intent(context, OverlayService::class.java).setAction(ACTION_SHOW)
            runCatching { context.startForegroundService(i) }
        }

        fun stop(context: Context) {
            val i = Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
            runCatching { context.startService(i) }
        }
    }
}
