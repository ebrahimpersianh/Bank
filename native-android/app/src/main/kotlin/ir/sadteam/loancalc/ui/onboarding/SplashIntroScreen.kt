package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// رنگ‌های اسپلش - پس‌زمینه با تم هماهنگه (کاربر قبلاً صریح خواسته بود: «سفید و مشکی بنا به
// دارک‌مود تغییر کنه»). خودِ آیکون (assets/splash/icon.png) یه بریدهٔ واقعیِ عکسِ مرجعِ کاربره،
// نه چیزی دستی‌کشیده - رجوع کن به کامنتِ SplashIntroScreen پایین برای دلیل.
private val SplashBgDark = Color(0xFF000000)
private val SplashBgLight = Color(0xFFFFFFFF)
private val RingTeal = Color(0xFF63C37A)
private val SplashNameDark = Color(0xFFEEF1F8)
private val SplashNameLight = Color(0xFF1A2033)
private val SplashSubDark = Color(0xFF4A5570)
private val SplashSubLight = Color(0xFF8D96AC)

/**
 * اینتروِ باز شدن اپ.
 *
 * **تغییرِ مهم (بعد از دو دورِ ناموفقِ بازسازیِ دستی)**: کاربر با اسکرین‌شات نشون داد نسخه‌های
 * قبلی (حلقه‌ی دوناتِ Canvas، بعد پشته‌ی کارتِ دستی‌کشیده با آیکونِ TrendingUp) اصلاً شبیهِ
 * عکسِ مرجعش نبودن - جزئیات (خط‌های متن، میله‌های نمودار، فنِ واقعیِ سه‌کارتی، حلقه‌ی ضخیمِ
 * سه‌بعدی) با شکل‌های ساده‌ی Compose قابلِ بازسازیِ دقیق نبود. به‌جای تلاشِ سومِ حدسی، خودِ
 * آیکونِ عکسِ مرجع (`assets/splash/icon.png` - یه بریدهٔ تمیز از تصویرِ استوری‌بردِ کاربر، با
 * پس‌زمینهٔ سیاهِ کدشده به شفاف) مستقیم به‌عنوانِ عکس نمایش داده می‌شه - دقیقاً همون چیزی که
 * کاربر خواسته، نه یه تفسیرِ برنامه‌نویسی‌شده‌ازش. هم‌الگو با AsyncImage/assets که همین پروژه
 * برای لوگوهای بانک هم استفاده می‌کنه (رجوع کن به BankTile.kt).
 *
 * چون پس‌زمینهٔ خودِ عکس تقریباً مشکیه (فقط جاهای روشن/سبز شفافن)، رو تمِ روشن (که پس‌زمینهٔ
 * اسپلش سفیده) بدونِ کمک لکه‌ای/کثیف دیده می‌شه - برای همین یه هالهٔ تیرهٔ ثابت (نه وابسته به
 * تم) پشتِ خودِ عکس گذاشته شده، مثلِ یه «داغِ نور» که همیشه زیرِ آیکون می‌مونه، چه تمِ روشن چه
 * تیره.
 */
@Composable
fun SplashIntroScreen(isDarkTheme: Boolean, onDone: () -> Unit) {
    val splashBg = if (isDarkTheme) SplashBgDark else SplashBgLight
    val splashName = if (isDarkTheme) SplashNameDark else SplashNameLight
    val splashSub = if (isDarkTheme) SplashSubDark else SplashSubLight

    val rainProgress = remember { Animatable(0f) }
    val vignetteProgress = remember { Animatable(0f) }
    val iconAppear = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val nameOffset = remember { Animatable(10f) }
    val poweredByAlpha = remember { Animatable(0f) }
    val poweredByOffset = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        // مرحله‌ی ۱: بارشِ ذرات
        launch { rainProgress.animateTo(1f, animationSpec = tween(600, easing = LinearEasing)) }
        // مرحله‌ی ۲: هاله‌ی تیره/نوری شکل می‌گیره
        launch {
            delay(300)
            vignetteProgress.animateTo(1f, animationSpec = tween(650, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // مرحله‌ی ۳+۴: ظهور و نورپردازیِ آیکونِ واقعی
        launch {
            delay(800)
            iconAppear.animateTo(1f, animationSpec = tween(650, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // مرحله‌ی ۵: نمایشِ اسم
        launch {
            delay(1550)
            launch { nameAlpha.animateTo(1f, animationSpec = tween(500)) }
            launch { nameOffset.animateTo(0f, animationSpec = tween(500, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) }
        }
        launch {
            delay(1650)
            launch { poweredByAlpha.animateTo(1f, animationSpec = tween(600)) }
            launch { poweredByOffset.animateTo(0f, animationSpec = tween(600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) }
        }
    }
    LaunchedEffect(Unit) {
        delay(2150)
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(splashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                // ۱) بارشِ ذراتِ نورِ عمودی
                Canvas(modifier = Modifier.size(240.dp)) {
                    val particleCount = 12
                    for (i in 0 until particleCount) {
                        val phase = i / particleCount.toFloat() * 0.45f
                        val localT = ((rainProgress.value - phase) / (1f - phase)).coerceIn(0f, 1f)
                        if (localT <= 0f || localT >= 1f) continue
                        val xFrac = (i * 0.618f) % 1f
                        val x = size.width * xFrac
                        val startY = -size.height * 0.15f
                        val endY = size.height * 0.5f
                        val y = startY + (endY - startY) * localT
                        val streakAlpha = sin((localT * PI).toFloat()).coerceIn(0f, 1f) * 0.6f
                        drawLine(
                            color = RingTeal.copy(alpha = streakAlpha),
                            start = Offset(x, y),
                            end = Offset(x, y + 30f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round,
                        )
                    }
                }

                // ۲) هاله‌ی تیره - همیشه تیره (نه وابسته به تم)، چون خودِ عکسِ آیکون رو زمینه‌ی
                // سیاه طراحی شده؛ بدونِ این هاله رو تمِ روشن (پس‌زمینه‌ی سفید) لکه‌ای/کثیف می‌شد.
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .alpha(vignetteProgress.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.9f),
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                // ۳)+۴) خودِ آیکونِ واقعیِ عکسِ مرجع - ظهور از دلِ نور (اسکیل+آلفا)
                AsyncImage(
                    model = "file:///android_asset/splash/icon.png",
                    contentDescription = null,
                    modifier = Modifier
                        .size(216.dp, 144.dp)
                        .graphicsLayer {
                            scaleX = 0.6f + 0.4f * iconAppear.value
                            scaleY = 0.6f + 0.4f * iconAppear.value
                            alpha = iconAppear.value
                        },
                )
            }

            Text(
                "حسابدار من",
                color = splashName,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(nameAlpha.value)
                    .offset(y = nameOffset.value.dp),
            )

            BlinkingDots(modifier = Modifier.padding(top = 26.dp).alpha(nameAlpha.value))
        }

        Text(
            "Powered By SadTeam",
            color = splashSub,
            fontSize = 10.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .offset(y = poweredByOffset.value.dp)
                .alpha(poweredByAlpha.value),
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
