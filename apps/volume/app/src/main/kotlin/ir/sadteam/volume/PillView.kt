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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dp(WIDTH_DP).toInt(), dp(HEIGHT_DP).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val barW = dp(if (active || moving) 9f else BAR_DP)
        val inset = dp(2f)
        val left = if (onLeft) inset else w - inset - barW
        val r = barW / 2f
        rect.set(left, 0f, left + barW, h)

        bg.color = Color.argb(if (active || moving) 210 else 150, 22, 32, 28)
        canvas.drawRoundRect(rect, r, r, bg)

        // مقدار: از پایین پر می‌شود.
        val pad = dp(2f)
        val inner = RectF(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)
        val ir = inner.width() / 2f
        if (muted) {
            fill.color = RED
            canvas.drawRoundRect(RectF(inner.left, inner.bottom - inner.width(), inner.right, inner.bottom), ir, ir, fill)
        } else if (level > 0f) {
            fill.color = GREEN
            val top = inner.bottom - (inner.height() * level).coerceAtLeast(inner.width())
            canvas.drawRoundRect(RectF(inner.left, top, inner.right, inner.bottom), ir, ir, fill)
        }

        outline.color = if (moving) GREEN else Color.argb(if (active) 230 else 170, 255, 255, 255)
        rect.inset(dp(0.5f), dp(0.5f))
        canvas.drawRoundRect(rect, r, r, outline)
    }

    companion object {
        /** پهنای پنجره‌ی لمسی (باریکه خودش [BAR_DP] است). */
        const val WIDTH_DP = 22f
        const val HEIGHT_DP = 140f
        const val BAR_DP = 7f
        val GREEN = Color.rgb(61, 220, 150)
        val RED = Color.rgb(255, 107, 99)
    }
}
