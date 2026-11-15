package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * موجِ نورِ طلاییِ گذرا (sheen): هر ~۴ ثانیه یه نوارِ نورِ مایل از رو محتوا رد می‌شه - برای
 * کارت‌های «ویژه» (CTA خرید اشتراک، بجِ دوره‌ی آزمایشی)، طبق الگوی مصوبِ لهجه‌ی طلایی
 * (پس‌زمینه/افکت، نه رنگِ فونت). متمایز از [shimmerEffect] که یه اسکلتونِ لودینگه و کل سطح رو
 * می‌پوشونه؛ این فقط یه رگه‌ی نورِ کوتاهه رو محتوای واقعی.
 *
 * پیاده‌سازی مثل [PulseGlowBox] به‌صورت Box دورِ محتواست (نه Modifier رو خودِ کارت) چون overlay
 * باید *بعدِ* محتوا کشیده بشه و جدا clip بشه - اگه clip رو خودِ زنجیره‌ی کارت می‌ذاشتیم، سایه‌ی
 * Surface (AppCard) هم ناخواسته بریده می‌شد. overlay هیچ لمسی رو مصرف نمی‌کنه.
 */
@Composable
fun GoldSheenBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "goldSheen")
    // چرخه‌ی ۴.۲ ثانیه‌ای که فقط ۳۰٪ اولش نور رد می‌شه - بقیه‌ش مکثه، که «هر از گاهی برق می‌زنه»
    // حس بشه نه یه لوپِ دائمیِ اعصاب‌خردکن.
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "goldSheenPhase",
    )
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .drawBehind {
                    val prog = phase / 0.30f
                    if (prog <= 1f) {
                        val band = size.width * 0.28f
                        val cx = -band + (size.width + band * 2f) * prog
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFFFE082).copy(alpha = 0.28f),
                                    Color.Transparent,
                                ),
                                start = Offset(cx - band, 0f),
                                end = Offset(cx + band, size.height),
                            ),
                        )
                    }
                },
        )
    }
}
