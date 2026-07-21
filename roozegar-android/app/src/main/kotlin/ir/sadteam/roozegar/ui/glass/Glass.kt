package ir.sadteam.roozegar.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.roozegar.ui.theme.Gold
import ir.sadteam.roozegar.ui.theme.NightBg
import ir.sadteam.roozegar.ui.theme.Teal

/**
 * زبان طراحی «شیشه‌ی دولایه» (رجوع کن به README):
 * - رو Android 12+ بلابِ‌های نور با [Modifier.blur] (RenderEffect واقعی) نرم می‌شن؛
 * - رو دستگاه‌های قدیمی‌تر blur به‌صورت خودکار no-op می‌شه و همون گرادیان شعاعی (که خودش لبه‌ی نرم
 *   داره) می‌مونه - یعنی fallback بدون هیچ if/else دستی، و هیچ‌وقت blur تو مسیر اسکرول نیست
 *   (فقط پس‌زمینه‌ی ثابت).
 */
@Composable
fun AuroraGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auroraShift",
    )
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightBg, Color(0xFF0E1D33), NightBg))),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .blur(60.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    // هاله‌ی فیروزه‌ای - آروم بین بالا-راست و وسط جابه‌جا می‌شه
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Teal.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(w * (0.75f - 0.25f * t), h * (0.12f + 0.18f * t)),
                            radius = w * 0.68f,
                        ),
                        radius = w * 0.68f,
                        center = Offset(w * (0.75f - 0.25f * t), h * (0.12f + 0.18f * t)),
                    )
                    // هاله‌ی طلایی گرم - پایین-چپ، خلاف جهت اولی
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Gold.copy(alpha = 0.10f), Color.Transparent),
                            center = Offset(w * (0.15f + 0.2f * t), h * (0.85f - 0.15f * t)),
                            radius = w * 0.62f,
                        ),
                        radius = w * 0.62f,
                        center = Offset(w * (0.15f + 0.2f * t), h * (0.85f - 0.15f * t)),
                    )
                },
        )
        content()
    }
}

/**
 * کارت شیشه‌ای استاندارد اپ: پس‌زمینه‌ی سفیدِ نیمه‌شفافِ گرادیانی + حاشیه‌ی ۱ پیکسلی روشن‌تر تو لبه‌ی
 * بالا (همون جلوه‌ی بازتاب نور لبه‌ی شیشه تو iOS) - بدون blur، پس رو ضعیف‌ترین گوشی هم مفت تمام می‌شه.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.035f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.05f)),
                ),
                shape = shape,
            )
            .padding(contentPadding),
        content = content,
    )
}
