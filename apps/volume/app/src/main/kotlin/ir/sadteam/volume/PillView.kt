package ir.sadteam.volume

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/**
 * قرصِ نیمه‌شفافِ فلش‌دار (طرحِ «قرصِ فلش‌دار» که کاربر انتخاب کرد): فلشِ بالا، خطِ باریکِ مقدارِ
 * صدا، فلشِ پایین. فقط نقاشی می‌کند؛ لمس‌ها را [OverlayService] می‌گیرد.
 */
class PillView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    /** مقدارِ صدا از ۰ تا ۱. */
    var level = 0.5f
        set(value) { field = value.coerceIn(0f, 1f); invalidate() }

    /** بی‌صدا: خطِ مقدار قرمز می‌شود. */
    var muted = false
        set(value) { field = value; invalidate() }

    /** حالتِ جابه‌جایی: حاشیه‌ی سبز. */
    var moving = false
        set(value) { field = value; invalidate() }

    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 22, 32, 28) }
    // ۱۳ آبان (گوشیِ کاربر): روی پس‌زمینه‌ی تیره قرصِ تیره اصلاً دیده نمی‌شد → حاشیه‌ی سفیدِ نازک.
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
        color = Color.argb(200, 255, 255, 255)
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = GREEN
    }
    private val arrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.4f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(3f)
        color = Color.argb(70, 255, 255, 255)
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(3f)
    }
    private val rect = RectF()
    private val path = Path()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dp(WIDTH_DP).toInt(), dp(HEIGHT_DP).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = w / 2f
        rect.set(0f, 0f, w, h)
        canvas.drawRoundRect(rect, r, r, bg)
        rect.inset(dp(0.6f), dp(0.6f))
        canvas.drawRoundRect(rect, r, r, outline)
        rect.set(0f, 0f, w, h)
        if (moving) {
            rect.inset(dp(1f), dp(1f))
            canvas.drawRoundRect(rect, r, r, ring)
        }

        val cx = w / 2f
        val a = dp(4.5f)
        // فلشِ بالا
        val upY = dp(13f)
        path.reset()
        path.moveTo(cx - a, upY + a / 2f); path.lineTo(cx, upY - a / 2f); path.lineTo(cx + a, upY + a / 2f)
        canvas.drawPath(path, arrow)
        // فلشِ پایین
        val dnY = h - dp(13f)
        path.reset()
        path.moveTo(cx - a, dnY - a / 2f); path.lineTo(cx, dnY + a / 2f); path.lineTo(cx + a, dnY - a / 2f)
        canvas.drawPath(path, arrow)

        // خطِ مقدار
        val top = dp(26f)
        val bottom = h - dp(26f)
        canvas.drawLine(cx, top, cx, bottom, track)
        if (muted) {
            // بی‌صدا: یک نقطه‌ی قرمز تهِ خط.
            fill.color = RED
            canvas.drawCircle(cx, bottom, dp(2.5f), fill)
        } else {
            fill.color = GREEN
            val y = bottom - (bottom - top) * level
            if (bottom - y > 0.5f) canvas.drawLine(cx, y, cx, bottom, fill)
        }
    }

    companion object {
        const val WIDTH_DP = 24f
        const val HEIGHT_DP = 92f
        val GREEN = Color.rgb(61, 220, 150)
        val RED = Color.rgb(255, 107, 99)
    }
}
