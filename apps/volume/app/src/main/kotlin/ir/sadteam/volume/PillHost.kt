package ir.sadteam.volume

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * باریکه‌ی صدا روی صفحه. هم [OverlayService] (پنجره‌ی معمولی) و هم [VolumeA11yService]
 * (پنجره‌ی دسترسی‌پذیری که وسطِ تماس هم دیده می‌شود) از همین استفاده می‌کنند.
 *
 * ۱۴ آبان (کاربر): همیشه همان باریکه‌ی کنارِ صفحه است و هیچ‌وقت بیرون نمی‌آید.
 * - کشیدنِ بالا/پایین روی باریکه → صدا کم و زیاد
 * - زدن روی نیمه‌ی بالا/پایین → یک پله
 * - دو بار زدن → بی‌صدا / برگشت
 * - نگه‌داشتن تا بلرزد → جابه‌جا کردن در طولِ لبه (یا بردن به لبه‌ی دیگر)
 *
 * [context] باید خودِ سرویس باشد تا نوعِ پنجره‌ی دسترسی‌پذیری پذیرفته شود.
 */
class PillHost(private val context: Context, private val windowType: Int) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val volume = Volume(context)
    private val prefs = Prefs(context)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var view: PillView
    private lateinit var lp: WindowManager.LayoutParams
    var added = false
        private set

    private val density get() = context.resources.displayMetrics.density
    private fun dp(v: Float) = (v * density).roundToInt()

    /** true = روی صفحه آمد. */
    @SuppressLint("ClickableViewAccessibility")
    fun add(): Boolean {
        if (added) return true
        view = PillView(context)
        view.onLeft = prefs.onLeft
        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            // LEFT نه START: مختصات در گوشیِ فارسی هم از چپ شمرده شود.
            gravity = Gravity.TOP or Gravity.LEFT
            y = (prefs.y.takeIf { it >= 0 } ?: (screenH() * 0.38f).roundToInt()).coerceIn(0, maxY())
            if (Build.VERSION.SDK_INT >= 28) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        lp.x = edgeX()
        view.setOnTouchListener { _, e -> onTouch(e) }
        // کشیدنِ بالا/پایین نباید «برگشتِ» اشاره‌ای گوشی را فعال کند.
        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (Build.VERSION.SDK_INT >= 29) v.systemGestureExclusionRects = listOf(Rect(0, 0, v.width, v.height))
        }
        // بعضی گوشی‌ها (شیائومی) بی اجازه خطا می‌دهند؛ برنامه نباید بسته شود.
        added = runCatching { wm.addView(view, lp) }.isSuccess
        if (added) refresh()
        return added
    }

    fun remove() {
        handler.removeCallbacksAndMessages(null)
        if (added) runCatching { wm.removeView(view) }
        added = false
    }

    /** «نشانم بده»: وسطِ لبه، و یک لحظه پررنگ. */
    fun show() {
        if (!added) return
        lp.y = (screenH() - pillH()) / 2
        prefs.y = lp.y
        runCatching { wm.updateViewLayout(view, lp) }
        view.active = true
        handler.postDelayed({ if (added) view.active = false }, 1500)
        refresh()
    }

    fun onConfigurationChanged() {
        if (!added) return
        lp.y = lp.y.coerceIn(0, maxY())
        lp.x = edgeX()
        runCatching { wm.updateViewLayout(view, lp) }
    }

    private fun screenW(): Int = if (Build.VERSION.SDK_INT >= 30) wm.currentWindowMetrics.bounds.width() else context.resources.displayMetrics.widthPixels
    private fun screenH(): Int = if (Build.VERSION.SDK_INT >= 30) wm.currentWindowMetrics.bounds.height() else context.resources.displayMetrics.heightPixels
    private fun pillW() = dp(PillView.WIDTH_DP)
    private fun pillH() = dp(PillView.HEIGHT_DP)
    private fun maxY() = (screenH() - pillH()).coerceAtLeast(0)
    private fun edgeX() = if (prefs.onLeft) 0 else screenW() - pillW()

    private fun refresh() {
        view.level = volume.level()
        view.muted = volume.isMuted()
    }

    // ── لمس ──────────────────────────────────────────────────────────────────────

    private var downX = 0f
    private var downY = 0f
    private var startY = 0
    private var startVol = 0
    private var sliding = false
    private var moving = false
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
                downX = e.rawX; downY = e.rawY
                startY = lp.y
                startVol = volume.current()
                sliding = false
                moving = false
                view.active = true
                refresh()
                handler.postDelayed(holdRunnable, HOLD_MS)
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = e.rawY - downY
                if (moving) {
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
                view.active = false
                if (moving) {
                    moving = false
                    view.moving = false
                    // انگشت تا نیمه‌ی دیگرِ صفحه رفته → لبه‌ی دیگر.
                    prefs.onLeft = e.rawX < screenW() / 2f
                    view.onLeft = prefs.onLeft
                    prefs.y = lp.y
                    lp.x = edgeX()
                    runCatching { wm.updateViewLayout(view, lp) }
                } else if (!sliding && e.actionMasked == MotionEvent.ACTION_UP) {
                    onTap(e.y < view.height / 2f)
                }
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
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching { v?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    private companion object {
        const val HOLD_MS = 1200L
        const val DOUBLE_TAP_MS = 280L
        const val STEP_DP = 14f
        const val SLOP_DP = 10f
    }
}
