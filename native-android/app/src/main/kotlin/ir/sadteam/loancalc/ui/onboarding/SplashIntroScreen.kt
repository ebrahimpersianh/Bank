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
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// رنگ‌های عینِ اسپلشِ اپ وب (#splash تو www/index.html)، حالا به‌جز پس‌زمینه با تم هماهنگ (خواسته‌ی
// کاربر: «اسپلش سفید و مشکی باشه بنا به دارک‌مود تغییر کنه» - قبلاً همیشه تیره بود، مستقل از تم).
private val SplashBgDark = Color(0xFF000000) // هم‌رنگِ AppBgِ جدید (کاملاً مشکی، دورِ چهارمِ تمِ تیره)
private val SplashBgLight = Color(0xFFFFFFFF)
private val RingBase = Color(0xFF16211C) // هم‌رنگِ AppSurface2ِ جدید
private val RingGold = Color(0xFFFFB020) // هم‌رنگِ AppAccent (طلایی) - دست‌نخورده موند
private val RingTeal = Color(0xFF00FF9C) // هم‌رنگِ AppPrimaryِ جدید (سبزِ نئونی، دورِ چهارمِ تمِ تیره)
private val FireCore = Color(0xFFFF5A1F) // هسته‌ی گرمِ نارنجی/قرمز برای حسِ «آتیشِ روشن»
private val SplashNameDark = Color(0xFFEEF1F8)
private val SplashNameLight = Color(0xFF1A2033)
private val SplashSubDark = Color(0xFF4A5570)
private val SplashSubLight = Color(0xFF8D96AC)

/**
 * اینتروِ باز شدن اپ - پورت دقیق اسپلشِ اپ وب (`#splash` تو www/index.html): زمینه‌ی هماهنگ با تم، یه
 * حلقه‌ی دونات (پایه‌ی تیره + کمانِ طلایی ۳۰٪ + کمانِ سبز ۷۰٪، عین نمودار دونات وام)، اسم «حسابدار من»، سه
 * نقطه‌ی چشمک‌زن و «Powered By Sad Team». بعد از ~۱.۸ ثانیه [onDone] صدا زده می‌شه.
 *
 * خواسته‌ی آخرِ کاربر (این دور): حلقه‌ی بازتابِ نورِ دورِ دونات (که قبلاً اینجا بود) حذف شد - فقط
 * خودِ «یک حلقه‌ی بسته» (دونات) می‌مونه؛ به‌جاش هاله‌ی پشتِ حلقه بزرگ‌تر و گرم‌تر شد تا حسِ «آتیشِ
 * روشن» بده (هسته‌ی نارنجی/قرمز [FireCore] + طلایی، با یه لایه‌ی سایه‌ی تیره‌ی افتاده‌ی پشتش)، و
 * هم‌زمان با چرخشِ [spin] یه تکونِ ریزِ نفس‌مانند (wobbleScale، مستقیم مشتق از خودِ spin.value، نه یه
 * انیمیشنِ جدا) می‌خوره - نه یه‌ذره پرت از حلقه، عینِ شعله‌ای که با چرخش می‌لرزه.
 */
@Composable
fun SplashIntroScreen(isDarkTheme: Boolean, onDone: () -> Unit) {
    val splashBg = if (isDarkTheme) SplashBgDark else SplashBgLight
    val splashName = if (isDarkTheme) SplashNameDark else SplashNameLight
    val splashSub = if (isDarkTheme) SplashSubDark else SplashSubLight
    val pop = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, animationSpec = tween(800, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }
    // چرخش کامل حلقه موقع ورود (خواسته‌ی کاربر: «دایره بچرخه، خیلی پریمیوم می‌شه»، بعد «چرخش رو یه
    // دور بیشتر کن») - دو دور کامل (۷۲۰ درجه) با همون easing نرم، همزمان با pop، بعد آروم می‌ایسته.
    // تاخیرِ onDone هم متناسب زیاد شده تا اسپلش قبل از تموم‌شدنِ چرخش قطع نشه.
    val spin = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        spin.animateTo(720f, animationSpec = tween(1600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }
    LaunchedEffect(Unit) {
        delay(1800)
        onDone()
    }
    // «Powered By SadTeam» پایینِ اسپلش قبلاً هیچ افکتی نداشت، همون اول یهو بود. خواسته‌ی کاربر:
    // انگار داره «ظاهر می‌شه» - محو (alpha) + یه‌کم بالا اومدن (offset) با تاخیر بعد از حلقه، تا
    // حسِ لایه‌به‌لایه ظاهرشدن بده، نه یهویی.
    val poweredByAlpha = remember { Animatable(0f) }
    val poweredByOffset = remember { Animatable(12f) }
    LaunchedEffect(Unit) {
        delay(900)
        launch { poweredByAlpha.animateTo(1f, animationSpec = tween(700)) }
        launch {
            poweredByOffset.animateTo(0f, animationSpec = tween(700, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
    }

    // تکونِ نفس‌مانندِ هاله (خواسته‌ی کاربر: «هم‌زمان با چرخش یه تکونی بخوره») - مستقیم از رو
    // spin.value مشتق می‌شه (نه یه Animatable جدا)، پس دقیقاً هم‌زمانِ خودِ چرخشِ حلقه‌ست: تا وقتی
    // spin در حال چرخیدنه هاله هم می‌لرزه، وقتی چرخش می‌ایسته لرزش هم می‌ایسته.
    val wobbleScale = 1f + 0.05f * sin(Math.toRadians(spin.value * 3.0)).toFloat()

    Box(
        modifier = Modifier.fillMaxSize().background(splashBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // قبلاً هاله‌ی نور و حلقه‌ی اصلی هرکدوم مستقیم زیرِ Box بیرونی بودن، پس رو کلِ صفحه
            // (با متن/نقطه‌های پایینش) وسط‌چین می‌شدن، نه رو خودِ حلقه - همین باعث می‌شد هاله و
            // حلقه هم‌مرکز نباشن (باگی که کاربر با اسکرین‌شات نشون داد). حالا همه‌شون تو یه Box
            // جدا و هم‌مرکز کنار همدیگه‌ان.
            Box(contentAlignment = Alignment.Center) {
                // سایه‌ی افتاده‌ی پشتِ آتیش (خواسته‌ی کاربر: «سایه‌اش افتاده پشت») - یه هاله‌ی تیره‌ی
                // بزرگ، کمی به پایین آفست‌شده، طوری که انگار نورِ آتیش از بالا می‌تابه و سایه‌ش پشتِ
                // حلقه می‌افته.
                Box(
                    modifier = Modifier
                        .size(360.dp)
                        .offset(y = 20.dp)
                        .scale(wobbleScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent),
                            ),
                        ),
                )
                // هاله‌ی بیرونیِ آتیش - هسته‌ی گرمِ نارنجی/قرمز (FireCore) که به طلایی محو می‌شه،
                // بزرگ‌تر از قبل (خواسته‌ی کاربر: «بزرگ هم باشه») تا حسِ «آتیشِ روشن» بده.
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .scale(wobbleScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FireCore.copy(alpha = 0.26f),
                                    RingGold.copy(alpha = 0.20f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
                // هاله‌ی میانیِ گرم‌تر و فشرده‌تر، نزدیک‌تر به خودِ حلقه
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .scale(wobbleScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(RingGold.copy(alpha = 0.30f), Color.Transparent),
                            ),
                        ),
                )
                // هاله‌ی محو سبزآبی چسبیده به خودِ حلقه (پورت .glow) - برای این‌که رنگِ برندِ اپ هم
                // تو اسپلش بمونه، نه فقط گرم/آتیشی.
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(RingTeal.copy(alpha = 0.18f), Color.Transparent),
                            ),
                        ),
                )

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
            }

            Text(
                "حسابدار من",
                color = splashName,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 20.dp),
            )

            BlinkingDots(modifier = Modifier.padding(top = 26.dp))
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
