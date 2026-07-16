package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.delay

// رنگ‌های عینِ اسپلشِ اپ وب (#splash تو www/index.html) - عمداً مستقل از تم روشن/تیره، همیشه تیره.
private val SplashBg = Color(0xFF0D1321)
private val RingBase = Color(0xFF1D2A46)
private val RingGold = Color(0xFFF0A857)
private val RingTeal = Color(0xFF2DD8B8)
private val SplashName = Color(0xFFEEF1F8)
private val SplashSub = Color(0xFF4A5570)

/**
 * اینتروِ باز شدن اپ - پورت دقیق اسپلشِ اپ وب (`#splash` تو www/index.html): زمینه‌ی تیره، یه حلقه‌ی
 * دونات (پایه‌ی تیره + کمانِ طلایی ۳۰٪ + کمانِ سبز ۷۰٪، عین نمودار دونات وام)، اسم «وام من»، سه نقطه‌ی
 * چشمک‌زن و «Powered By Sad Team». بعد از ~۱.۶ ثانیه [onDone] صدا زده می‌شه. قبلاً اپ بومی به‌جاش فقط
 * آیکونِ ماشین‌حساب (اسپلشِ سیستمی) رو نشون می‌داد؛ کاربر همین دوناتِ وب رو می‌خواست.
 */
@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    val pop = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, animationSpec = tween(800, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }
    // چرخش کامل حلقه موقع ورود (خواسته‌ی کاربر: «دایره بچرخه، خیلی پریمیوم می‌شه») - یه دور ۳۶۰
    // درجه با همون easing نرم، همزمان با pop، بعد آروم می‌ایسته.
    val spin = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        spin.animateTo(360f, animationSpec = tween(1100, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }
    LaunchedEffect(Unit) {
        delay(1300)
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        // هاله‌ی محو پشتِ حلقه (پورت .glow)
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(RingTeal.copy(alpha = 0.16f), Color.Transparent),
                    ),
                ),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pop.value)
                    .graphicsLayer { rotationZ = spin.value },
            ) {
                val stroke = 12.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(inset, inset)
                // پایه‌ی حلقه (کل دایره)
                drawArc(
                    color = RingBase,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                // کمانِ طلایی: ۳۰٪ از بالا (۱۰۸ درجه)
                drawArc(
                    color = RingGold,
                    startAngle = -90f,
                    sweepAngle = 108f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // کمانِ سبز: ۷۰٪ باقی‌مونده (۲۵۲ درجه)
                drawArc(
                    color = RingTeal,
                    startAngle = -90f + 108f,
                    sweepAngle = 252f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            Text(
                "وام من",
                color = SplashName,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 20.dp),
            )

            BlinkingDots(modifier = Modifier.padding(top = 26.dp))
        }

        Text(
            "SADTeam",
            color = SplashSub,
            fontSize = 10.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
        )
    }
}

@Composable
private fun BlinkingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(RingTeal),
            )
        }
    }
}
