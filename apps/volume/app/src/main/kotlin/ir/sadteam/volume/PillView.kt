package ir.sadteam.volume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * ۱۴ آبان (کاربر: «کلاً همان گوشه باشد، بیرون نیاید»): یک باریکه‌ی همیشگی چسبیده به لبه‌ی صفحه.
 * پنجره کمی پهن‌تر از باریکه است تا انگشت راحت بگیردش؛ بقیه‌اش شفاف است.
 * فقط نقاشی می‌کند؛ لمس‌ها را [PillHost] می‌گیرد.
 *
 * ۱۸ آبان (کاربر): رنگِ صدا مثلِ نوارِ صدای خودِ گوشی سفید است، نه سبز؛ باز و بسته شدن و
 * بالا/پایین رفتنِ خط نرم و پیوسته است (زیرِ انگشت بی پله، بعد از رها کردن با انیمیشن).
 */
class PillView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    /** مقدارِ صدا از ۰ تا ۱؛ تغییرش با انیمیشنِ کوتاه نشان داده می‌شود. */
    var level = 0.5f
        set(value) {
            val v = value.coerceIn(0f, 1f)
            if (v == field && v == shownLevel) return
            field = v
            animateLevelTo(v)
        }

    /** زیرِ انگشت: خط دقیقاً همراهِ انگشت، بی انیمیشن و بی پله. */
    fun dragLevel(v: Float) {
        levelAnim?.cancel(); levelAnim = null
        shownLevel = v.coerceIn(0f, 1f)
        invalidate()
    }

    /** بی‌صدا: خط کم‌رنگ می‌شود. */
    var muted = false
        set(value) { field = value; invalidate() }

    /** حالتِ جابه‌جایی: حاشیه‌ی سفید. */
    var moving = false
        set(value) { if (field != value) { field = value; animateExpand() } }

    /** وقتِ لمس پهن‌تر و پررنگ‌تر می‌شود تا مقدارِ صدا دیده شود. */
    var active = false
        set(value) { if (field != value) { field = value; animateExpand() } }

    /** باریکه به کدام لبه چسبیده. */
    var onLeft = false
        set(value) { field = value; invalidate() }

    private var shownLevel = 0.5f
    private var expand = 0f
    private var levelAnim: ValueAnimator? = null
    private var expandAnim: ValueAnimator? = null

    private fun animateLevelTo(target: Float) {
        levelAnim?.cancel()
        levelAnim = ValueAnimator.ofFloat(shownLevel, target).apply {
            duration = 160
            interpolator = DecelerateInterpolator()
            addUpdateListener { shownLevel = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun animateExpand() {
        val target = if (active || moving) 1f else 0f
        expandAnim?.cancel()
        expandAnim = ValueAnimator.ofFloat(expand, target).apply {
            duration = if (target > expand) 160 else 260
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { expand = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
    }
    private val rect = RectF()
    private val fillRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dp(WIDTH_DP).toInt(), dp(HEIGHT_DP).toInt())
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun alpha(a: Float) = (a.coerceIn(0f, 1f) * 255).toInt()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val e = expand
        val barW = dp(lerp(REST_W_DP, ACTIVE_W_DP, e))
        val barH = dp(lerp(REST_H_DP, ACTIVE_H_DP, e)).coerceAtMost(h)
        val top = (h - barH) / 2f
        // مستطیل از لبه‌ی صفحه بیرون می‌زند تا فقط سمتِ داخلی گرد باشد.
        if (onLeft) rect.set(-barW, top, barW, top + barH) else rect.set(w - barW, top, w + barW, top + barH)
        val r = barW

        // حالتِ آرام (نمونه‌ی ۳): سفیدِ پررنگ با حاشیه‌ی تیره؛ با باز شدن کم‌رنگ می‌شود.
        if (e < 1f) {
            paint.color = Color.argb(alpha(0.92f * (1f - e)), 255, 255, 255)
            canvas.drawRoundRect(rect, r, r, paint)
            outline.color = Color.argb(alpha(0.55f * (1f - e)), 0, 0, 0)
            canvas.drawRoundRect(rect, r, r, outline)
        }
        if (e <= 0f) return

        // حالتِ باز: زمینه‌ی تیره‌ی نیمه‌شفاف و خطِ سفید مثلِ نوارِ صدای خودِ گوشی.
        paint.color = Color.argb(alpha(0.82f * e), 28, 28, 30)
        canvas.drawRoundRect(rect, r, r, paint)
        val lv = if (muted) 0f else shownLevel
        if (lv > 0f) {
            paint.color = Color.argb(alpha(e), 255, 255, 255)
            val fh = (barH * lv).coerceAtLeast(barW * 1.5f)
            fillRect.set(rect.left, rect.bottom - fh, rect.right, rect.bottom)
            canvas.drawRoundRect(fillRect, r, r, paint)
        } else if (muted) {
            paint.color = Color.argb(alpha(0.35f * e), 255, 255, 255)
            fillRect.set(rect.left, rect.bottom - barW * 1.5f, rect.right, rect.bottom)
            canvas.drawRoundRect(fillRect, r, r, paint)
        }
        if (moving) {
            outline.color = Color.argb(alpha(e), 255, 255, 255)
            canvas.drawRoundRect(rect, r, r, outline)
        }
    }

    companion object {
        /** پنجره‌ی لمسی (کمی بزرگ‌تر از خطی که دیده می‌شود تا انگشت راحت بگیرد). */
        const val WIDTH_DP = 22f
        const val HEIGHT_DP = 64f
        const val REST_W_DP = 4f
        const val REST_H_DP = 50f
        const val ACTIVE_W_DP = 7f
        const val ACTIVE_H_DP = 64f
    }
}
