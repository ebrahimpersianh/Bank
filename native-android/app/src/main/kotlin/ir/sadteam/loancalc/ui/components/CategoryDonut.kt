package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppLine

/** یه تکه از نمودارِ دوناتِ [CategoryDonut] - مقدار و رنگش. */
data class DonutSlice(val value: Double, val color: Color)

/**
 * نمودارِ دوناتِ چندبخشی برای سهمِ هر دسته از خرج/درآمد - خواسته‌ی صریحِ کاربر («چه ارتقاهای
 * گرافیکی می‌تونی اضافه کنی؟» → «همه رو انجام بده»)، هم‌الگو با اپِ رفرنس که همین دونات رو داره.
 *
 * فرقش با [ProgressRing]: اون **یک** قوسِ پیشرفته (چند درصدِ یه چیز)، این **چند** قوسِ کنارِ هم
 * (سهمِ هر دسته از کل). برای همین جدا نوشته شد نه پارامترِ اضافه رو اون.
 *
 * قوس‌ها با یه ضریبِ مشترکِ انیمیشنی (۰→۱) باز می‌شن، پس کلِ دونات با هم «رشد» می‌کنه نه تکه‌تکه.
 * بینِ تکه‌ها یه فاصله‌ی کوچیک ([gapDegrees]) گذاشته می‌شه تا مرزها پیدا باشن؛ تکه‌های خیلی کوچیک
 * که از خودِ فاصله باریک‌ترن حذف نمی‌شن بلکه حداقلِ عرض می‌گیرن تا گم نشن.
 *
 * ⚠️ رنگ‌ها از بیرون پاس داده می‌شن (نه از توکنِ تم داخلِ بلوکِ رسم) - رجوع کن به قاعده‌ی
 * «توکن‌های رنگ @Composableان» تو CLAUDE.md.
 */
@Composable
fun CategoryDonut(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    strokeWidth: Dp = 22.dp,
    trackColor: Color = AppLine,
    gapDegrees: Float = 2f,
    /**
     * سبکِ «درخشان» (طرحِ مرجعِ کاربر، ۳ مهر): سرِ گرد، هاله‌ی نورِ دورِ هر تکه، شیبِ روشن‌تر
     * روی تکه، و قرصِ تیره‌ی وسط با لبه‌ی نازکِ هم‌رنگ. بلورِ واقعی نیست (قاعده‌ی پروژه) -
     * هاله با چند خطِ پهن‌ترِ کم‌رنگ ساخته می‌شود.
     */
    glow: Boolean = false,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val total = slices.sumOf { it.value }.takeIf { it > 0.0 } ?: 0.0
    val grow by animateFloatAsState(
        targetValue = if (total > 0.0) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donutGrow",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // ریلِ خالیِ پشتِ همه - وقتی هیچ داده‌ای نیست، همین تنها چیزیه که دیده می‌شه.
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            if (total <= 0.0) return@Canvas

            val strokePx = strokeWidth.toPx()
            val radius = arcSize.width / 2f
            // سرِ گرد به اندازه‌ی نصفِ ضخامت از هر طرف بیرون می‌زند؛ از زاویه کم می‌شود تا
            // فاصله‌ی بینِ تکه‌ها همان [gapDegrees] بماند.
            val capDeg = if (glow) Math.toDegrees((strokePx / 2f / radius).toDouble()).toFloat() else 0f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            if (glow) {
                // قرصِ تیره‌ی وسط با لبه‌ی نازکِ رنگِ تکه‌ی اول.
                val innerR = radius - strokePx / 2f - strokePx * 0.28f
                drawCircle(color = Color(0xFF0B1020).copy(alpha = 0.55f), radius = innerR, center = center)
                drawCircle(
                    color = slices.first { it.value > 0.0 }.color.copy(alpha = 0.45f),
                    radius = innerR,
                    center = center,
                    style = Stroke(width = strokePx * 0.08f),
                )
            }

            var startAngle = -90f // از بالای دایره
            slices.forEach { slice ->
                if (slice.value <= 0.0) return@forEach
                val full = (slice.value / total).toFloat() * 360f
                // حداقلِ عرض تا تکه‌های ریز کاملاً محو نشن
                val sweep = ((full - gapDegrees - capDeg * 2f).coerceAtLeast(1.5f)) * grow
                val arcStart = startAngle + capDeg * grow
                if (glow) {
                    // هاله: سه خطِ پهن‌تر و کم‌رنگ‌تر زیرِ تکه.
                    listOf(2.6f to 0.07f, 1.9f to 0.11f, 1.4f to 0.16f).forEach { (widthMul, alpha) ->
                        drawArc(
                            color = slice.color.copy(alpha = alpha),
                            startAngle = arcStart,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx * widthMul, cap = StrokeCap.Round),
                        )
                    }
                }
                drawArc(
                    color = slice.color,
                    startAngle = arcStart,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = if (glow) Stroke(width = strokePx, cap = StrokeCap.Round) else stroke,
                )
                if (glow) {
                    // برقِ شیشه‌ای: یک خطِ باریکِ روشن روی لبه‌ی بیرونیِ تکه.
                    val shineInset = inset - strokePx * 0.22f
                    drawArc(
                        color = Color.White.copy(alpha = 0.22f),
                        startAngle = arcStart,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(shineInset, shineInset),
                        size = Size(this.size.width - shineInset * 2, this.size.height - shineInset * 2),
                        style = Stroke(width = strokePx * 0.18f, cap = StrokeCap.Round),
                    )
                }
                startAngle += full * grow
            }
        }
        if (content != null) content()
    }
}
