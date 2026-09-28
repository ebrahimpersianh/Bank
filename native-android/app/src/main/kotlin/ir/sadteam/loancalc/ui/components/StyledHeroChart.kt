package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * شش سبکِ خریدنیِ نمودار (بسته‌ی ChatGPT، `JibakChart`) - با همان قراردادِ [HeroChart]:
 * داده قدیمی ← جدید، رنگ فقط سفید با شفافیت‌های مختلف (زمینه مالِ کارتِ والد است).
 *
 * اصلاح‌ها نسبت به بسته:
 * - کمتر از دو عدد کرش نمی‌کند، خطِ ساده می‌کشد.
 * - فقط کشیدنِ **افقی** گرفته می‌شود تا اسکرولِ صفحه و ورق‌زدنِ تب‌ها از روی نمودار کار کند.
 * - «امروز» از `currentIndex` می‌آید نه آخرین نقطه (بودجه روزهای آینده‌ی ماه را هم دارد).
 * - اندازه‌ی نقطه‌ها با ارتفاع کوچک می‌شود تا در کارتِ ۲۶dpِ خانه بریده نشوند.
 * - ستونِ گرادیانی گرادیانِ واقعی دارد (سفید ← شفاف).
 */
@Composable
internal fun StyledHeroChart(
    style: HeroChartStyle,
    values: List<Double>,
    labels: List<String>,
    valueLabel: (Double) -> String,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    slots: Int? = null,
    tooltipBackground: Color = Color.Black.copy(alpha = 0.40f),
    /** رنگِ خط/نقطه‌ها: سفید روی کارتِ قهرمان، رنگِ روند روی کارت‌های روشن (دارایی، ۳ مهر). */
    ink: Color = Color.White,
) {
    val axis = (slots ?: values.size).coerceAtLeast(values.size).coerceAtLeast(2)
    val lead = axis - values.size
    val interactive = values.size >= 2 && labels.size == values.size
    var touchedIndex by remember(values) { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val today = currentIndex.coerceIn(0, (values.size - 1).coerceAtLeast(0))

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(
                    if (!interactive) {
                        Modifier
                    } else {
                        Modifier
                            .pointerInput(values) {
                                detectHorizontalDragGestures(
                                    onDragStart = { touchedIndex = nearest(it.x, size.width.toFloat(), axis, lead) },
                                    onDragEnd = { touchedIndex = null },
                                    onDragCancel = { touchedIndex = null },
                                    onHorizontalDrag = { change, _ ->
                                        change.consume()
                                        touchedIndex = nearest(change.position.x, size.width.toFloat(), axis, lead)
                                    },
                                )
                            }
                            .pointerInput(values) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        touchedIndex = nearest(offset.x, size.width.toFloat(), axis, lead)
                                        tryAwaitRelease()
                                        touchedIndex = null
                                    },
                                )
                            }
                    },
                ),
        ) {
            widthPx = size.width
            // ۴۰dp اندازه‌ی مرجعِ بسته است؛ پایین‌ترش همه‌ی شعاع‌ها کوچک می‌شوند.
            val k = (size.height / 40.dp.toPx()).coerceIn(0.6f, 1f)
            if (values.size < 2) {
                val y = size.height / 2f
                drawLine(ink.copy(alpha = 0.35f), Offset(0f, y), Offset(size.width, y), 2.dp.toPx() * k, StrokeCap.Round)
                return@Canvas
            }
            val points = pointsOf(values, axis, lead, padY = 10.dp.toPx() * k)
            val s = ChartScale(k, today, ink)
            when (style) {
                HeroChartStyle.SOFT_WAVE -> softWave(points, s)
                HeroChartStyle.STEPPED -> stepped(points, s)
                HeroChartStyle.DOTS -> dots(points, s)
                HeroChartStyle.GRADIENT_COLUMNS -> gradientColumns(points, s)
                HeroChartStyle.AREA_WAVE -> areaWave(points, s)
                HeroChartStyle.BUBBLES -> bubbles(points, s)
                HeroChartStyle.BARS, HeroChartStyle.LINE -> Unit
            }
            if (style != HeroChartStyle.BUBBLES) {
                val p = points[today]
                drawCircle(ink.copy(alpha = 0.98f), 5.dp.toPx() * k, p)
                drawCircle(ink.copy(alpha = 0.34f), 9.dp.toPx() * k, p, style = Stroke(2.dp.toPx() * k))
            }
            touchedIndex?.let { i ->
                val p = points[i]
                drawLine(ink.copy(alpha = 0.22f), Offset(p.x, 0f), Offset(p.x, size.height), 1.dp.toPx())
                drawCircle(ink.copy(alpha = 0.95f), 6.dp.toPx() * k, p)
            }
        }
        ChartTooltipHost(visible = touchedIndex != null, modifier = Modifier.align(AbsoluteAlignment.TopLeft)) {
            val index = touchedIndex ?: today
            ChartTooltip(
                title = labels.getOrElse(index) { "" },
                value = valueLabel(values.getOrElse(index) { 0.0 }),
                centerX = widthPx * (lead + index) / (axis - 1),
                containerWidth = widthPx,
                background = tooltipBackground,
                titleColor = Color.White.copy(alpha = 0.75f),
                valueColor = Color.White,
            )
        }
    }
}

private class ChartScale(val k: Float, val today: Int, val ink: Color)


private fun nearest(x: Float, width: Float, axis: Int, lead: Int): Int {
    if (axis < 2 || width <= 0f) return 0
    val step = width / (axis - 1)
    return ((x / step).roundToInt() - lead).coerceIn(0, axis - lead - 1)
}

/** مقیاسِ عمودی روی کمینه/بیشینه‌ی خودِ داده؛ داده‌ی مسطح وسطِ کادر می‌نشیند. */
private fun DrawScope.pointsOf(values: List<Double>, axis: Int, lead: Int, padY: Float): List<Offset> {
    val lo = values.min()
    val hi = values.max()
    val flat = hi - lo == 0.0
    val pad = min(padY, size.height * 0.30f)
    val usable = size.height - pad * 2f
    val step = size.width / (axis - 1)
    return values.mapIndexed { i, v ->
        val ratio = if (flat) 0.5f else ((v - lo) / (hi - lo)).toFloat()
        Offset((lead + i) * step, pad + usable - ratio * usable)
    }
}

private fun DrawScope.stroke(path: Path, s: ChartScale) = drawPath(
    path,
    s.ink.copy(alpha = 0.90f),
    style = Stroke(width = 3.dp.toPx() * s.k, cap = StrokeCap.Round, join = StrokeJoin.Round),
)

/** نقطه‌ی کوچکِ هر داده - وقتی نقاط خیلی نزدیک‌اند کشیده نمی‌شود تا خط تسبیح نشود. */
private fun DrawScope.smallDots(points: List<Offset>, s: ChartScale, radius: Dp, alpha: Float) {
    if (points.size > 1 && points[1].x - points[0].x < 6.dp.toPx()) return
    points.forEach { drawCircle(s.ink.copy(alpha = alpha), radius.toPx() * s.k, it) }
}

private fun DrawScope.softWave(points: List<Offset>, s: ChartScale) {
    stroke(smoothPath(points), s)
    smallDots(points, s, 3.dp, 0.72f)
}

private fun DrawScope.stepped(points: List<Offset>, s: ChartScale) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val cur = points[i]
            val midX = (prev.x + cur.x) / 2f
            lineTo(midX, prev.y)
            lineTo(midX, cur.y)
            lineTo(cur.x, cur.y)
        }
    }
    stroke(path, s)
    smallDots(points, s, 2.5.dp, 0.70f)
}

private fun DrawScope.dots(points: List<Offset>, s: ChartScale) {
    val r = if (points.size > 1 && points[1].x - points[0].x < 8.dp.toPx()) 2.dp else 3.5.dp
    points.forEach { drawCircle(s.ink.copy(alpha = 0.76f), r.toPx() * s.k, it) }
}

private fun DrawScope.gradientColumns(points: List<Offset>, s: ChartScale) {
    val colW = (size.width / points.size * 0.58f).coerceIn(2.dp.toPx(), 12.dp.toPx())
    val corner = CornerRadius(min(4.dp.toPx(), colW / 2f))
    points.forEachIndexed { i, p ->
        val top = if (i == s.today) 0.92f else 0.46f
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(s.ink.copy(alpha = top), s.ink.copy(alpha = 0f)), startY = p.y, endY = size.height),
            topLeft = Offset(p.x - colW / 2f, p.y),
            size = Size(colW, size.height - p.y),
            cornerRadius = corner,
        )
    }
}

private fun DrawScope.areaWave(points: List<Offset>, s: ChartScale) {
    val curve = smoothPath(points)
    val area = Path().apply {
        addPath(curve)
        lineTo(points.last().x, size.height)
        lineTo(points.first().x, size.height)
        close()
    }
    drawPath(area, Brush.verticalGradient(listOf(s.ink.copy(alpha = 0.24f), s.ink.copy(alpha = 0.02f))))
    stroke(curve, s)
    smallDots(points, s, 3.dp, 0.70f)
}

private fun DrawScope.bubbles(points: List<Offset>, s: ChartScale) {
    val step = if (points.size > 1) points[1].x - points[0].x else size.width
    // حباب از نصفِ فاصله‌ی دو روز بزرگ‌تر نمی‌شود تا در ۳۰ روز روی هم نیفتند.
    val cap = max(1.5.dp.toPx(), step * 0.45f)
    points.forEachIndexed { i, p ->
        val isToday = i == s.today
        val radius = min(cap, (if (isToday) 7.dp.toPx() else (4.dp.toPx() + (i % 3) * 1.dp.toPx())) * s.k)
        drawLine(
            s.ink.copy(alpha = if (isToday) 0.42f else 0.22f),
            Offset(p.x, p.y + radius),
            Offset(p.x, size.height),
            1.dp.toPx(),
        )
        drawCircle(s.ink.copy(alpha = if (isToday) 0.95f else 0.38f), radius, p)
    }
}

/** منحنیِ کاردینال (کششِ ۱/۶). */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.lastIndex) {
        val p0 = points[max(0, i - 1)]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[min(points.lastIndex, i + 2)]
        path.cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y,
        )
    }
    return path
}
