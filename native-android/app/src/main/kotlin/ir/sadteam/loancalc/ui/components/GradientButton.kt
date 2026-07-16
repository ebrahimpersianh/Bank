package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import kotlinx.coroutines.delay

/**
 * پورت افکت گرادینت رو دکمه‌های اصلی CTA (به‌جای رنگ صاف تخت `Button` معمولی) برای حس «امروزی‌تر» -
 * جایگزین `Button(colors = ButtonDefaults.buttonColors(containerColor = AppPrimary))` تو همه‌ی
 * دکمه‌های اصلی اپ. ظاهر/رفتار (ripple، enabled/disabled، شکل گرد) مثل `Button` معمولیه، فقط
 * پس‌زمینه‌ش یه گرادینت خطی از [AppPrimary] به [AppPrimaryDim]ه؛ محتوای هر call site (معمولاً یه
 * `Text`) بدون تغییر کار می‌کنه چون [content] هم مثل `Button` یه `RowScope.() -> Unit` هست.
 *
 * افکت «شیمر»: هر ~۳ ثانیه یه نوار براقِ محو از عرض دکمه رد می‌شه (حس پریمیوم). مقدار انیمیشن فقط
 * تو فاز draw خونده می‌شه ([drawWithContent])، پس هیچ recomposition ای per-frame اتفاق نمی‌افته.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val gradient = Brush.horizontalGradient(listOf(AppPrimary, AppPrimaryDim))

    val shimmer = remember { Animatable(-0.6f) }
    LaunchedEffect(Unit) {
        while (true) {
            shimmer.snapTo(-0.6f)
            shimmer.animateTo(1.6f, animationSpec = tween(1100, easing = LinearEasing))
            delay(2100)
        }
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = shape,
        color = Color.Transparent,
        contentColor = Color(0xFF04211C),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, shape)
                .clip(shape)
                .drawWithContent {
                    drawContent()
                    if (enabled) {
                        val x = shimmer.value * size.width
                        val band = size.width * 0.28f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.16f),
                                    Color.Transparent,
                                ),
                                start = Offset(x - band, 0f),
                                end = Offset(x + band, size.height),
                            ),
                        )
                    }
                }
                .alpha(if (enabled) 1f else 0.5f),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
