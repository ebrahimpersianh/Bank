package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * **نمودارِ خطیِ نرم با نقطه‌ی انتها** - طرحِ مرجعِ کاربر (۳۱ شهریور).
 *
 * چرا خطی و نه میله‌ای: این نمودار **روند** را می‌گوید نه مقدارِ تک‌تکِ روزها. میله برای
 * «هر روز چقدر» خوب است (کارتِ خانه)، ولی برای «دارایی‌ام بالا رفته یا پایین» خطِ پیوسته
 * همان یک جمله را می‌گوید و سی میله‌ی باریک نمی‌سازد.
 *
 * ⚠️ خط با **کاردینالِ نرم** کشیده می‌شود نه پاره‌خطِ شکسته: نقاطِ کنترلِ هر قطعه از
 * شیبِ همسایه‌ها می‌آید. همین یک کار فرقِ «نمودارِ نرم» و «خطِ زیگزاگ» است.
 *
 * ⚠️ مقیاسِ عمودی روی **کمینه/بیشینه‌ی خودِ داده** بسته می‌شود نه صفر، وگرنه نوسانِ کوچک
 * روی عددِ بزرگ یک خطِ صاف دیده می‌شود. مسطح‌بودن واقعی هم با `flat` نشان داده می‌شود.
 */
@Composable
fun TrendLineChart(
    values: List<Double>,
    lineColor: Color,
    fillTop: Color,
    dotColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    strokeWidth: Dp = 2.5.dp,
) {
    if (values.size < 2) return
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0.0 } ?: 1.0
        val flat = max - min == 0.0
        val stepX = size.width / (values.size - 1)
        // ۱۰٪ حاشیه‌ی بالا و پایین تا نقطه‌ی انتها به لبه نچسبد.
        val usable = size.height * 0.80f
        val top = size.height * 0.10f
        val points = values.mapIndexed { index, value ->
            val ratio = if (flat) 0.5 else (value - min) / span
            Offset(index * stepX, top + usable - (ratio.toFloat() * usable))
        }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[(i - 1).coerceAtLeast(0)]
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = points[(i + 2).coerceAtMost(points.size - 1)]
                // کاردینال با کششِ ۰٫۱۶: نرم، ولی بی بیش‌زدگی (overshoot) که عددِ
                // موجود در داده را از سقف/کف رد کند و دروغ نشان دهد.
                val c1 = Offset(p1.x + (p2.x - p0.x) * 0.16f, p1.y + (p2.y - p0.y) * 0.16f)
                val c2 = Offset(p2.x - (p3.x - p1.x) * 0.16f, p2.y - (p3.y - p1.y) * 0.16f)
                cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
            }
        }

        // سایه‌ی زیرِ خط: همان مسیر، بسته‌شده تا کفِ کادر.
        val area = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(listOf(fillTop, Color.Transparent)),
        )
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        // نقطه‌ی انتها = «امروز». هاله‌ی کم‌رنگ دورش تا روی خط گم نشود.
        val last = points.last()
        drawCircle(color = dotColor.copy(alpha = 0.28f), radius = 7.dp.toPx(), center = last)
        drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = last)
    }
}
