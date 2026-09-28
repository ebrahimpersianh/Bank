package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * ═══════════ حبابِ اطلاعاتِ نمودار ═══════════
 *
 * خواسته‌ی کاربر (۳۱ شهریور): «روی هر میله/نقطه که می‌زنم اطلاعاتش بیاید».
 *
 * 🚨 **لمسِ ساده، نه نگه‌داشتن.** کاربر «چند ثانیه نگه‌داشتن» گفت و پیشنهادِ لمسِ ساده
 * داده شد و پذیرفت: نگه‌داشتن یعنی کاربر باید بداند چنین قابلیتی هست، ولی لمس خودش
 * کشف می‌شود. انگشت را که بردارد حباب هم می‌رود.
 *
 * ⚠️ حباب **داخلِ کارت** کشیده می‌شود نه `Popup`: کارت‌های قهرمان گرادیان دارند و یک
 * پنجره‌ی سیستمیِ جدا روی آن‌ها وصله دیده می‌شود.
 */
@Composable
fun ChartTooltip(
    title: String,
    value: String,
    /**
     * مرکزِ افقیِ هدف، به پیکسلِ **فیزیکی از چپ** (نه «از آغاز»). نمودارها در `Canvas`
     * می‌کشند که راست‌به‌چپ را نمی‌شناسد؛ اگر این‌جا آینه می‌شد، حباب طرفِ مقابلِ انگشت می‌افتاد.
     */
    centerX: Float,
    /** عرضِ کلِ قاب به پیکسل - برای اینکه حباب از لبه بیرون نزند. */
    containerWidth: Float,
    background: Color,
    titleColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.tooltipPlacement(centerX, containerWidth),
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(background)
                .padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, color = titleColor, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(value, color = valueColor, fontSize = 10.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}


/**
 * 🚨 **حباب هیچ جایی در چیدمان نمی‌گیرد** (گزارشِ کاربر: «می‌زنم رو کندل، صفحه کشیده
 * می‌شود»). قبلاً ارتفاعِ حباب به قابِ نمودار اضافه می‌شد و کلِ کارت بلند می‌شد. حالا
 * اندازه‌ی گزارش‌شده صفر است و حباب **بالای** نمودار، روی بقیه‌ی کارت، کشیده می‌شود.
 * `place` نه `placeRelative`: مختصات فیزیکی است (بالا را ببین).
 */
private fun Modifier.tooltipPlacement(centerX: Float, containerWidth: Float): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0, maxWidth = Int.MAX_VALUE))
        // 🚨 گیره با **عرضِ واقعیِ حباب**، نه عددِ حدسی: قبلاً روی میله/نقطه‌ی لبه نیمی از
        // حباب بیرونِ کارت می‌افتاد (گزارشِ کاربر).
        val w = placeable.width.toFloat()
        val left = (centerX - w / 2f).coerceIn(0f, (containerWidth - w).coerceAtLeast(0f))
        layout(0, 0) {
            placeable.place(IntOffset(left.roundToInt(), -placeable.height - 4.dp.roundToPx()))
        }
    }

/** پوششِ ظاهرشدن/محوشدنِ حباب - کوتاه و بی‌پرش. */
@Composable
fun ChartTooltipHost(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier,
    ) { content() }
}
