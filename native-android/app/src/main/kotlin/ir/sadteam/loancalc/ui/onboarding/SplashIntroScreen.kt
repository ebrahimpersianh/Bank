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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// رنگ‌های اسپلش - پس‌زمینه با تم هماهنگه (کاربر قبلاً صریح خواسته بود: «سفید و مشکی بنا به
// دارک‌مود تغییر کنه»)، ولی خودِ نور/آیکون/حلقه همیشه سبزِ برندِ اپه (هم‌رنگِ AppPrimaryِ فعلی)،
// دقیقاً مثلِ عکسِ مرجعِ کاربر که هیچ رنگِ دیگه‌ای (طلایی/نارنجی) نداره.
private val SplashBgDark = Color(0xFF000000)
private val SplashBgLight = Color(0xFFFFFFFF)
private val RingTeal = Color(0xFF63C37A)
private val RingTealDim = Color(0xFF2E7A47)
private val CardDark = Color(0xFF0B0F0C)
private val SplashNameDark = Color(0xFFEEF1F8)
private val SplashNameLight = Color(0xFF1A2033)
private val SplashSubDark = Color(0xFF4A5570)
private val SplashSubLight = Color(0xFF8D96AC)

/**
 * اینتروِ باز شدن اپ - بازسازیِ مستقیمِ استوری‌بردِ ۶مرحله‌ایِ عکسِ مرجعِ کاربر:
 * ۱) بارشِ ذراتِ نور از بالا  ۲) شکل‌گیریِ یه حلقه‌ی بیضی‌مایلِ نوری در مرکز  ۳) ظهورِ آیکون
 * (پشته‌ی سه‌کارتیِ سبزآبی) از دلِ نور  ۴) نورپردازی/درخشان‌ترشدنِ آیکون  ۵) نمایشِ اسمِ
 * «حسابدار من» + شعار  ۶) حالتِ نهایی. بعد از ~۲.۱ ثانیه [onDone] صدا زده می‌شه.
 *
 * آیکونِ داخلِ کارتِ جلو عمداً از [Icons.Filled.TrendingUp] (میله‌های نمودار + فلشِ صعودی، همون
 * مفهومِ چارتِ عکسِ مرجع) استفاده می‌کنه، نه یه مسیرِ سفارشیِ دستی با Path - چون سندباکس نمی‌تونه
 * native-android رو بیلد/تست کنه، یه آیکونِ آماده‌ی استانداردِ Material ریسکِ خطای کامپایل یا
 * شکلِ بدِ یه فلشِ دستی‌کشیده رو خیلی کمتر می‌کنه.
 */
@Composable
fun SplashIntroScreen(isDarkTheme: Boolean, onDone: () -> Unit) {
    val splashBg = if (isDarkTheme) SplashBgDark else SplashBgLight
    val splashName = if (isDarkTheme) SplashNameDark else SplashNameLight
    val splashSub = if (isDarkTheme) SplashSubDark else SplashSubLight

    val rainProgress = remember { Animatable(0f) }
    val ringProgress = remember { Animatable(0f) }
    val iconAppear = remember { Animatable(0f) }
    val iconGlow = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val nameOffset = remember { Animatable(10f) }
    val poweredByAlpha = remember { Animatable(0f) }
    val poweredByOffset = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        // مرحله‌ی ۱: بارش
        launch { rainProgress.animateTo(1f, animationSpec = tween(600, easing = LinearEasing)) }
        // مرحله‌ی ۲: شکل‌گیریِ حلقه
        launch {
            delay(350)
            ringProgress.animateTo(1f, animationSpec = tween(700, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // مرحله‌ی ۳: ظهورِ آیکون
        launch {
            delay(850)
            iconAppear.animateTo(1f, animationSpec = tween(550, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // مرحله‌ی ۴: نورپردازی/درخشان‌ترشدنِ آیکون
        launch {
            delay(1300)
            iconGlow.animateTo(1f, animationSpec = tween(400))
        }
        // مرحله‌ی ۵: نمایشِ اسم
        launch {
            delay(1500)
            launch { nameAlpha.animateTo(1f, animationSpec = tween(500)) }
            launch { nameOffset.animateTo(0f, animationSpec = tween(500, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) }
        }
        launch {
            delay(1600)
            launch { poweredByAlpha.animateTo(1f, animationSpec = tween(600)) }
            launch { poweredByOffset.animateTo(0f, animationSpec = tween(600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) }
        }
    }
    LaunchedEffect(Unit) {
        delay(2100)
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(splashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
                // ۱) بارشِ ذراتِ نورِ عمودی
                Canvas(modifier = Modifier.size(230.dp)) {
                    val particleCount = 12
                    for (i in 0 until particleCount) {
                        val phase = i / particleCount.toFloat() * 0.45f
                        val localT = ((rainProgress.value - phase) / (1f - phase)).coerceIn(0f, 1f)
                        if (localT <= 0f || localT >= 1f) continue
                        // پخشِ شبه‌تصادفیِ x بر اساسِ نسبتِ طلایی - بدونِ نیازِ Random.
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

                // هاله‌ی نرمِ سبزِ پشتِ همه‌چیز - با خودِ حلقه هم‌زمان محو می‌شه تو دید
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .alpha(ringProgress.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(RingTeal.copy(alpha = 0.22f), Color.Transparent),
                            ),
                        ),
                )

                // ۲) حلقه‌ی بیضی‌مایلِ نوری - از یه نقطه (اسکیلِ صفر) باز می‌شه، کمی کج (رجوع کن
                // به عکسِ مرجع - یه حلقه‌ی تخت نیست، زاویه‌دار مثلِ اورویتِ سیاره‌ست).
                Canvas(
                    modifier = Modifier
                        .size(190.dp, 92.dp)
                        .graphicsLayer {
                            rotationZ = -18f
                            scaleX = 0.4f + 0.6f * ringProgress.value
                            scaleY = 0.4f + 0.6f * ringProgress.value
                            alpha = ringProgress.value
                        },
                ) {
                    drawOval(color = RingTeal, style = Stroke(width = 3.dp.toPx()))
                }

                // ۳)+۴) پشته‌ی سه‌کارتیِ آیکون - ظهور از دلِ نور (اسکیل+آلفا) + درخشان‌ترشدنِ
                // تدریجیِ رنگِ خودِ آیکونِ داخلِ کارتِ جلو.
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = 0.55f + 0.45f * iconAppear.value
                            scaleY = 0.55f + 0.45f * iconAppear.value
                            alpha = iconAppear.value
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp, 96.dp)
                            .graphicsLayer { rotationZ = -14f; translationX = -18f }
                            .clip(RoundedCornerShape(16.dp))
                            .background(RingTeal.copy(alpha = 0.85f)),
                    )
                    Box(
                        modifier = Modifier
                            .size(68.dp, 96.dp)
                            .graphicsLayer { rotationZ = 9f; translationX = 16f }
                            .clip(RoundedCornerShape(16.dp))
                            .background(RingTealDim.copy(alpha = 0.9f)),
                    )
                    Box(
                        modifier = Modifier
                            .size(74.dp, 104.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = RingTeal.copy(alpha = 0.65f + 0.35f * iconGlow.value),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
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
