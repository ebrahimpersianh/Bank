package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.roundToInt

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
    /** برچسبِ هر نقطه («۱۲ شهریور»…) - بی این، لمس چیزی برای گفتن ندارد و خاموش می‌مانَد. */
    labels: List<String> = emptyList(),
    /** عددِ هر نقطه، آماده‌ی نمایش. */
    valueLabel: (Double) -> String = { "" },
    tooltipBackground: Color = Color.Black.copy(alpha = 0.55f),
    tooltipTitleColor: Color = Color.White.copy(alpha = 0.75f),
    tooltipValueColor: Color = Color.White,
    /**
     * طولِ کاملِ محور بر حسبِ نقطه (مثلاً ۳۰ روز). اگر داده کمتر باشد، نقاط **سمتِ راست** در
     * جای واقعی‌شان می‌نشینند و بقیه‌ی محور خالی می‌مانَد. `null` یعنی کلِ عرض مالِ داده است.
     */
    slots: Int? = null,
) {
    if (values.size < 2) return
    val interactive = labels.size == values.size
    val axis = (slots ?: values.size).coerceAtLeast(values.size)
    val lead = axis - values.size
    // نقطه‌ی انتخاب‌شده با لمس. `null` یعنی دستی روی نمودار نیست و نقطه‌ی «امروز» فعال است.
    var touchedIndex by remember(values) { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val activeIndex = touchedIndex ?: values.lastIndex
    // 🚨 **حرکتِ نرمِ نقطه** (خواسته‌ی صریح: «با انیمیشن برود آن‌ور، نه پرشی»): خودِ
    // شاخص انیمیت می‌شود نه مختصاتِ پیکسلی، پس نقطه دقیقاً **روی** منحنی می‌لغزد و از
    // آن جدا نمی‌افتد.
    val animatedIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = tween(220),
        label = "trendDot",
    )
    Box(modifier = modifier.fillMaxWidth()) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .then(
                if (!interactive) {
                    Modifier
                } else {
                    Modifier.pointerInput(values) {
                        // لمس و کشیدن هر دو یک کار می‌کنند: نزدیک‌ترین نقطه به انگشت.
                        // برداشتنِ انگشت حباب را می‌بندد و نقطه به «امروز» برمی‌گردد.
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchedIndex = indexAt(offset.x, size.width.toFloat(), axis, lead)
                            },
                            onDragEnd = { touchedIndex = null },
                            onDragCancel = { touchedIndex = null },
                            onDrag = { change, _ ->
                                change.consume()
                                touchedIndex = indexAt(change.position.x, size.width.toFloat(), axis, lead)
                            },
                        )
                    }.pointerInput(values) {
                        detectTapGestures(
                            onPress = { offset ->
                                touchedIndex = indexAt(offset.x, size.width.toFloat(), axis, lead)
                                tryAwaitRelease()
                                touchedIndex = null
                            },
                        )
                    }
                },
            ),
    ) {
        widthPx = size.width
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0.0 } ?: 1.0
        val flat = max - min == 0.0
        val stepX = size.width / (axis - 1)
        // ۱۰٪ حاشیه‌ی بالا و پایین تا نقطه‌ی انتها به لبه نچسبد.
        val usable = size.height * 0.80f
        val top = size.height * 0.10f
        val points = values.mapIndexed { index, value ->
            val ratio = if (flat) 0.5 else (value - min) / span
            Offset((lead + index) * stepX, top + usable - (ratio.toFloat() * usable))
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
            brush = Brush.verticalGradient(
                listOf(fillTop, fillTop.copy(alpha = fillTop.alpha * 0.35f)),
            ),
        )
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        // نقطه‌ی فعال: «امروز»، یا هر نقطه‌ای که انگشت رویش است. بینِ دو نقطه‌ی همسایه
        // درون‌یابی می‌شود تا حرکتش پیوسته دیده شود.
        val lower = animatedIndex.toInt().coerceIn(0, points.lastIndex)
        val upper = (lower + 1).coerceAtMost(points.lastIndex)
        val t = (animatedIndex - lower).coerceIn(0f, 1f)
        val active = Offset(
            x = points[lower].x + (points[upper].x - points[lower].x) * t,
            y = points[lower].y + (points[upper].y - points[lower].y) * t,
        )
        if (touchedIndex != null) {
            // خطِ راهنمای عمودی - بی آن معلوم نیست نقطه دقیقاً روی کدام ستون است.
            drawLine(
                color = dotColor.copy(alpha = 0.35f),
                start = Offset(active.x, 0f),
                end = Offset(active.x, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }
        // 🔘 **نقطه روی هر داده** (خواسته‌ی کاربر: «هر روز/ماه جدا با نقطه مشخص باشد، نه
        // یک خطِ صاف»). وقتی نقاط خیلی نزدیکِ هم‌اند (بیش از ~۴۰ در عرض) کشیده نمی‌شوند
        // تا خط به تسبیح تبدیل نشود؛ لمس همچنان هر نقطه را جدا پیدا می‌کند.
        if (stepX >= 6.dp.toPx()) {
            points.forEach { drawCircle(color = dotColor.copy(alpha = 0.55f), radius = 2.2.dp.toPx(), center = it) }
        }
        drawCircle(color = dotColor.copy(alpha = 0.28f), radius = 7.dp.toPx(), center = active)
        drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = active)
    }
        ChartTooltipHost(visible = touchedIndex != null, modifier = Modifier.align(AbsoluteAlignment.TopLeft)) {
            val index = touchedIndex ?: values.lastIndex
            ChartTooltip(
                title = labels.getOrElse(index) { "" },
                value = valueLabel(values.getOrElse(index) { 0.0 }),
                centerX = widthPx * (lead + index) / (axis - 1),
                containerWidth = widthPx,
                background = tooltipBackground,
                titleColor = tooltipTitleColor,
                valueColor = tooltipValueColor,
            )
        }
    }
}

/** نزدیک‌ترین نقطه به مختصاتِ افقیِ انگشت. */
private fun indexAt(x: Float, width: Float, axis: Int, lead: Int): Int {
    if (axis < 2 || width <= 0f) return 0
    val step = width / (axis - 1)
    // جای خالیِ ابتدای محور به اولین نقطه‌ی واقعی می‌چسبد.
    return ((x / step).roundToInt() - lead).coerceIn(0, axis - lead - 1)
}
