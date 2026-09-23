package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═══════════ میله‌های لمس‌پذیر ═══════════
 *
 * خواسته‌ی کاربر (۳۱ شهریور): «روی میله‌ها می‌زنم هیچی نمیاد؛ می‌خوام اطلاعاتش بیاد».
 *
 * 🚨 **یک کامپوننتِ مشترک برای خانه و گزارش، نه دو کپی.** هر دو تب یک نوار میله دارند
 * (خانه ۷ تایی، گزارش ۷ تا ۳۱ تایی) و پیش از این هر کدام حلقه‌ی `Row`ِ خودش را داشت.
 * دو کپی یعنی روزی که یکی رفتارِ تازه می‌گیرد و دیگری جا می‌ماند.
 *
 * لمس و کشیدن هر دو یک کار می‌کنند و انگشت که برداشته شود حباب می‌رود - همان قاعده‌ی
 * [TrendLineChart]، تا دو نمودارِ یک برنامه دو جور رفتار نکنند.
 */
@Composable
fun InteractiveBars(
    values: List<Double>,
    /** برچسبِ هر میله («۱۲ شهریور»، «امروز»…). اگر اندازه‌اش با داده جور نباشد، لمس خاموش است. */
    labels: List<String>,
    valueLabel: (Double) -> String,
    /** کدام میله «حالا»ست - پررنگ می‌مانَد حتی وقتی انگشت جای دیگری است. */
    currentIndex: Int,
    barColor: Color,
    currentBarColor: Color,
    tooltipBackground: Color,
    tooltipTitleColor: Color,
    tooltipValueColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    spacing: Dp = 3.dp,
) {
    val max = values.maxOrNull()?.takeIf { it > 0.0 } ?: return
    val interactive = labels.size == values.size
    var touchedIndex by remember(values) { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    // 🚨 `Row` در راست‌به‌چپ میله‌ی اول را **راست** می‌گذارد، ولی مختصاتِ لمس همیشه از چپ
    // است. بی این، کشیدن برعکس کار می‌کرد و لمس اطلاعاتِ میله‌ی قرینه را نشان می‌داد.
    // خواسته‌ی کاربر (۱ مهر): «امروز باید سمتِ راست باشد» - پس میله‌ها **فیزیکی چپ‌به‌راست**
    // چیده می‌شوند (قدیمی چپ، امروز راست)، مستقل از راست‌به‌چپِ برنامه.
    val rtl = false

    Box(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(
                    if (!interactive) {
                        Modifier
                    } else {
                        Modifier.pointerInput(values) {
                            widthPx = size.width.toFloat()
                            detectDragGestures(
                                onDragStart = { touchedIndex = barAt(it.x, size.width.toFloat(), values.size, rtl) },
                                onDragEnd = { touchedIndex = null },
                                onDragCancel = { touchedIndex = null },
                                onDrag = { change, _ ->
                                    change.consume()
                                    touchedIndex = barAt(change.position.x, size.width.toFloat(), values.size, rtl)
                                },
                            )
                        }.pointerInput(values) {
                            widthPx = size.width.toFloat()
                            detectTapGestures(
                                onPress = {
                                    touchedIndex = barAt(it.x, size.width.toFloat(), values.size, rtl)
                                    tryAwaitRelease()
                                    touchedIndex = null
                                },
                            )
                        }
                    },
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val selected = index == touchedIndex
                // میله‌ی زیرِ انگشت کمی بلندتر نمی‌شود - فقط پررنگ. بلندشدن ارتفاعِ
                // نمودار را دروغ نشان می‌دهد.
                val target = (value / max).toFloat().coerceIn(0.06f, 1f)
                val fraction by animateFloatAsState(target, tween(520), label = "bar$index")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(
                            when {
                                selected -> currentBarColor
                                index == currentIndex -> currentBarColor
                                else -> barColor
                            },
                        ),
                )
            }
        }
        }
        ChartTooltipHost(visible = touchedIndex != null, modifier = Modifier.align(AbsoluteAlignment.TopLeft)) {
            val index = touchedIndex ?: currentIndex
            ChartTooltip(
                title = labels.getOrElse(index) { "" },
                value = valueLabel(values.getOrElse(index) { 0.0 }),
                // مرکزِ میله: هر میله یک سهمِ مساوی از عرض دارد.
                centerX = if (values.isEmpty()) 0f else {
                    val slot = if (rtl) values.size - 1 - index else index
                    widthPx * (slot + 0.5f) / values.size
                },
                containerWidth = widthPx,
                background = tooltipBackground,
                titleColor = tooltipTitleColor,
                valueColor = tooltipValueColor,
            )
        }
    }
}

/** میله‌ی زیرِ انگشت. برخلافِ نمودارِ خطی این‌جا **سهمِ** میله مهم است نه نزدیک‌ترین نقطه. */
private fun barAt(x: Float, width: Float, count: Int, rtl: Boolean): Int {
    if (count <= 0 || width <= 0f) return 0
    val slot = ((x / width) * count).toInt().coerceIn(0, count - 1)
    return if (rtl) count - 1 - slot else slot
}
