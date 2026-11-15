package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * یه حلقه‌ی نورانیِ آرومِ ضربان‌دار دورِ محتوا - خواسته‌ی «انیمیشن‌های سفارشی»، برای جلبِ توجه به
 * CTAهای مهم (مثل کارتِ ارتقا به اشتراک) بدون جیغ‌جیغو بودن. چون blur واقعی (RenderEffect) فقط از
 * Android 12 به بالا در دسترسه، به‌جاش یه استروکِ رنگی با آلفای متغیر دورِ گوشه‌های گرد شبیه‌سازی
 * می‌کنه - سبک‌تر و روی همه‌ی نسخه‌ها یکسان.
 */
@Composable
fun PulseGlowBox(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD4AF37),
    cornerRadius: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "pulseGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseGlowAlpha",
    )
    Box(
        modifier = modifier
            .padding(4.dp)
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    cornerRadius = CornerRadius((cornerRadius + 4.dp).toPx()),
                    style = Stroke(width = 3.dp.toPx()),
                )
            },
    ) {
        content()
    }
}
