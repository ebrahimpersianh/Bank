package ir.sadteam.volume

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * ۱۴ آبان (کاربر: «کلاً همان گوشه باشد، بیرون نیاید»): یک باریکه‌ی همیشگی چسبیده به لبه‌ی صفحه.
 * پنجره کمی پهن‌تر از باریکه است تا انگشت راحت بگیردش؛ بقیه‌اش شفاف است.
 * خطِ سبزِ داخلش مقدارِ صداست. فقط نقاشی می‌کند؛ لمس‌ها را [PillHost] می‌گیرد.
 */
class PillView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    /** مقدارِ صدا از ۰ تا ۱. */
    var level = 0.5f
        set(value) { field = value.coerceIn(0f, 1f); invalidate() }

    /** بی‌صدا: تهِ باریکه قرمز می‌شود. */
    var muted = false
        set(value) { field = value; invalidate() }

    /** حالتِ جابه‌جایی: حاشیه‌ی سبز. */
    var moving = false
        set(value) { field = value; invalidate() }

    /** وقتِ کشیدن کمی پهن‌تر و پررنگ‌تر می‌شود تا معلوم باشد گرفته شده. */
    var active = false
        set(value) { field = value; invalidate() }

    /** باریکه به کدام لبه چسبیده. */
    var onLeft = false
        set(value) { field = value; invalidate() }

    private val bg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val fillRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dp(WIDTH_DP).toInt(), dp(HEIGHT_DP).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val big = active || moving
        // ۱۴ آبان (کاربر، نمونه‌ی ۴): در حالتِ عادی فقط یک مویِ ریز چسبیده به لبه؛
        // انگشت که رویش برود پهن و سبز می‌شود تا مقدارِ صدا دیده شود.
        val barW = dp(if (big) ACTIVE_W_DP else REST_W_DP)
        val barH = dp(if (big) ACTIVE_H_DP else REST_H_DP).coerceAtMost(h)
        val top = (h - barH) / 2f
        // مستطیل از لبه‌ی صفحه بیرون می‌زند تا فقط سمتِ داخلی گرد باشد.
        if (onLeft) rect.set(-barW, top, barW, top + barH) else rect.set(w - barW, top, w + barW, top + barH)
        val r = barW

        if (!big) {
            bg.color = Color.argb(115, 255, 255, 255)
            canvas.drawRoundRect(rect, r, r, bg)
            return
        }

        bg.color = Color.argb(220, 30, 40, 36)
        canvas.drawRoundRect(rect, r, r, bg)
        if (muted) {
            fill.color = RED
            val fh = barW * 1.5f
            fillRect.set(rect.left, rect.bottom - fh, rect.right, rect.bottom)
            canvas.drawRoundRect(fillRect, r, r, fill)
        } else if (level > 0f) {
            fill.color = GREEN
            val fh = (barH * level).coerceAtLeast(barW * 1.5f)
            fillRect.set(rect.left, rect.bottom - fh, rect.right, rect.bottom)
            canvas.drawRoundRect(fillRect, r, r, fill)
        }
        if (moving) {
            outline.color = GREEN
            canvas.drawRoundRect(rect, r, r, outline)
        }
    }

    companion object {
        /** پنجره‌ی لمسی (کمی بزرگ‌تر از خطی که دیده می‌شود تا انگشت راحت بگیرد). */
        const val WIDTH_DP = 22f
        const val HEIGHT_DP = 64f
        const val REST_W_DP = 1.5f
        const val REST_H_DP = 26f
        const val ACTIVE_W_DP = 6f
        const val ACTIVE_H_DP = 64f
        val GREEN = Color.rgb(61, 220, 150)
        val RED = Color.rgb(255, 107, 99)
    }
}
