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
    /** مرکزِ افقیِ هدف، به پیکسل، نسبت به همان قابی که حباب در آن نشسته. */
    centerX: Float,
    /** عرضِ کلِ قاب به پیکسل - برای اینکه حباب از لبه بیرون نزند. */
    containerWidth: Float,
    background: Color,
    titleColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    // عرضِ تقریبیِ حباب؛ دقیق نیست و لازم هم نیست - فقط برای اینکه به لبه نچسبد.
    val halfWidth = 46f
    val x = centerX.coerceIn(halfWidth, (containerWidth - halfWidth).coerceAtLeast(halfWidth))
    Box(
        modifier = modifier.offsetPx(x - halfWidth),
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

private fun Modifier.offsetPx(x: Float): Modifier =
    this.then(Modifier.offsetLayout(x))

private fun Modifier.offsetLayout(x: Float): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(IntOffset(x.roundToInt(), 0))
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
