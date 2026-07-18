package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim

/**
 * حلقه‌ی پیشرفتِ گرادیانی (سبزآبی→طلایی، همون هویتِ دوتاییِ اسپلش/Aurora) با پرشدنِ انیمیشنی -
 * برای درصدِ اقساطِ پرداخت‌شده رو کارتِ هر وام و گیجِ «سلامتِ مالی» تو داشبوردِ وام‌های من.
 *
 * گرادیان sweep ئه و از بالای حلقه (ساعت ۱۲) شروع می‌شه؛ چون قوس فقط به‌اندازه‌ی [progress] از
 * دورِ کامل رو نشون می‌ده، رنگِ نوکِ قوس با پیشرفتِ بیشتر به طلایی نزدیک‌تر می‌شه - یعنی وامِ
 * تقریباً تسویه‌شده خودبه‌خود «طلایی‌تر» دیده می‌شه، بدون هیچ منطقِ اضافه.
 *
 * [content] وسطِ حلقه می‌شینه (مثلاً Text درصد) - خود کامپوننت عمداً متنی نمی‌کشه که اندازه/رنگِ
 * متن دستِ هر صفحه بمونه.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeWidth: Dp = 5.dp,
    colors: List<Color> = listOf(AppPrimaryDim, AppPrimary, AppAccent),
    trackColor: Color = AppLine,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progressRing",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val inset = strokeWidth.toPx() / 2
                val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                if (animated > 0f) {
                    // rotate(-90): هم شروعِ قوس می‌ره بالای حلقه، هم صفرِ گرادیانِ sweep (که همیشه
                    // از ساعت ۳ شروع می‌شه) باهاش هم‌راستا می‌شه - بدون درزِ رنگیِ وسطِ قوس.
                    rotate(degrees = -90f) {
                        drawArc(
                            brush = Brush.sweepGradient(colors = colors),
                            startAngle = 0f,
                            sweepAngle = animated * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                }
            },
    ) {
        if (content != null) content()
    }
}
