package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.ui.components.JibakBrandMark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * اسپلشِ باز شدن اپ - **فریمِ `24b`ی فایلِ طراحی**.
 *
 * ⚠️ **کاملاً از نو نوشته شد.** نسخه‌ی قبلی یه عکسِ آماده‌ی تمام‌صفحه (`assets/splash/splash.jpg`)
 * بود با پس‌زمینه‌ی مشکی و لوگوی قدیمی - سبکِ «شیشه‌ای/تیره»ی دورِ قبل. بازطراحیِ Duolingo
 * لوگوی v4 و یه اسپلشِ **سبزِ گرادیانی** می‌خواد، پس دیگه عکس نیست و همه‌چیز کشیده می‌شه:
 *
 * ```
 * پس‌زمینه: گرادیانِ ۱۶۸ درجه‌ی #1FD68C → #0EA968 (۴۲٪) → #076B42
 * هاله‌ی بیضیِ بالای صفحه + سه ذره‌ی نورِ ثابت (بندِ ۸۲)
 * لوگوی v4 **بدونِ کاشی** (بندِ ۸۰) با هاله‌ی گردِ پشتش
 * «جیبک» ۳۳/۹۰۰ · «کیفِ پولت، مرتب» ۱۲٫۵/۷۰۰
 * نوارِ ۱۱۰×۳ به‌جای حلقه‌ی چرخان (بندِ ۸۱) + شماره‌ی نسخه
 * ```
 *
 * چون خودِ صفحه سبزه، دیگه به تمِ روشن/تیره وابسته نیست - تسکِ قدیمیِ «اسپلشِ نسخه‌ی سفید»
 * با همین حل شد.
 *
 * ══════ چهار چیزی که با متنِ بالا نمی‌خوانْد ══════
 *
 * **الف · گرادیان ۱۶۸ درجه نبود.** Brush.linearGradient(listOf(...)) بدونِ start/end از
 * گوشه‌ی بالا-چپ به پایین-راست می‌کشد (۱۳۵ درجه‌ی مورب)، و سه رنگ را روی ۰/۵۰/۱۰۰٪ پخش
 * می‌کند نه ۰/۴۲/۱۰۰. الان colorStops و start/endِ صریح - ۱۶۸ درجه یعنی تقریباً عمودی
 * با یک انحرافِ ملایمِ راست، که همان چیزی است که فریم نشان می‌دهد.
 *
 * **ب · هاله بیضی نبود، دایره بود.** drawCircle با شعاعِ width*0.9 روی صفحه‌ی بلندِ موبایل
 * یک دایره‌ی کشیده می‌شود. الان با scale(1f, 0.55f) واقعاً بیضی است - پهنِ افقی،
 * کم‌ارتفاع، مثلِ نورِ بالای صفحه.
 *
 * **پ · شماره‌ی نسخه رقمِ فارسی می‌گرفت.** toFa(BuildConfig.VERSION_NAME) - ولی «نسخه»
 * سومین موردِ فهرستِ استثناهای لایه‌ی ارقام است: شماره‌ی نسخه شناسه است نه عدد، و کاربری که
 * می‌خواهد آن را در گزارشِ باگ بنویسد باید همان چیزی باشد که در بازار/مایکت می‌بیند.
 *
 * **ت · لوگو و واژه‌نشان دستی چیده شده بودند.** حالا [JibakBrandMark]ِ مشترک - همان تابعی که
 * قفل، گیتِ مجوز، آنبوردینگ و ورود از آن می‌خوانند. پس اندازه‌ی «جیبک» نسبتِ لوگوست و اگر
 * روزی برند عوض شد، یک جا عوض می‌شود. هاله‌ی گردِ پشتِ لوگو مالِ همین صفحه می‌ماند.
 */
@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    val bar = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            reveal.animateTo(1f, animationSpec = tween(560, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // نوارِ باریکِ پایین جای اسپینره - تا لحظه‌ی رفتن به اپ پر می‌شه.
        launch { bar.animateTo(1f, animationSpec = tween(1300)) }
        delay(1450)
        exit.animateTo(1f, animationSpec = tween(260, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)))
        onDone()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // ── گرادیانِ ۱۶۸ درجه با ایستگاهِ میانی روی ۴۲٪ ──
                // ۱۶۸° در قراردادِ CSS (۰ = به بالا) یعنی بردارِ (sin168°, -cos168°) ≈ (۰٫۲۱، ۰٫۹۸):
                // تقریباً عمودی، با انحرافِ ملایم به راست. پس start.x و end.x قرینه‌ی مرکزند و
                // فاصله‌ی افقی‌شان ۰٫۲۱ برابرِ ارتفاع.
                val drift = size.height * 0.104f
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to SplashTop,
                            0.42f to SplashMid,
                            1f to SplashDeep,
                        ),
                        start = Offset(size.width / 2f - drift, 0f),
                        end = Offset(size.width / 2f + drift, size.height),
                    ),
                )
                // ── هاله‌ی بیضیِ بالا ──
                // drawCircle بیضی نمی‌دهد، پس محورِ عمودی را ۰٫۵۵ فشرده می‌کنیم: پهنِ افقی و
                // کم‌ارتفاع، همان نورِ بالای صفحه‌ی فریم.
                val haloR = size.width * 0.9f
                scale(scaleX = 1f, scaleY = 0.55f, pivot = Offset(size.width / 2f, 0f)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(size.width / 2f, 0f),
                            radius = haloR,
                        ),
                        radius = haloR,
                        center = Offset(size.width / 2f, 0f),
                    )
                }
                // سه ذره‌ی نورِ ثابت - جاهای نامتقارن، عمق بدونِ شلوغی
                drawCircle(Color.White.copy(alpha = 0.50f), 2.5.dp.toPx(), Offset(size.width * 0.13f, size.height * 0.15f))
                drawCircle(Color.White.copy(alpha = 0.40f), 2.dp.toPx(), Offset(size.width * 0.84f, size.height * 0.23f))
                drawCircle(Color.White.copy(alpha = 0.35f), 2.dp.toPx(), Offset(size.width * 0.18f, size.height * 0.70f))
            },
        contentAlignment = Alignment.Center,
    ) {
        // اسپلش عمداً **عادی**ه؛ خواسته‌ی کاربر «لوگوی تمام‌صفحه» مالِ آیکونِ لانچر بود نه این.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                val enter = 0.94f + 0.06f * reveal.value
                val leave = 1f + 0.08f * exit.value
                scaleX = enter * leave
                scaleY = enter * leave
                alpha = reveal.value * (1f - exit.value)
            },
        ) {
            // هاله‌ی گردِ پشتِ لوگو - مالِ همین صفحه است و به [JibakBrandMark]ِ مشترک نرفت.
            //
            // مرکزش باید روی مرکزِ **لوگو** بیفتد، نه مرکزِ نشان: نشان با واژه‌نشان و شعار
            // بلندتر از لوگوست، پس هاله‌ی وسط‌چینِ ساده روی «جیبک» می‌افتاد نه روی کیف.
            // لوگو ۹۶dp عرض و ۷۶dp ارتفاع دارد (نسبتِ ۹۶:۷۶)، پس مرکزش y=38dp است؛ هاله‌ی
            // ۱۱۶dp که از بالا تراز شود مرکزش y=58dp می‌شد - ۲۰dp پایین‌تر.
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                                radius = size.minDimension * 0.5f,
                            ),
                        )
                    },
            )
            JibakBrandMark(
                width = 96.dp,
                tint = Color.White,
                tagline = "کیفِ پولت، مرتب",
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
                .graphicsLayer { alpha = reveal.value * (1f - exit.value) },
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.1f + 0.9f * bar.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White),
                )
            }
            Text(
                // ⚠️ toFa برداشته شد. «نسخه» سومین موردِ فهرستِ استثناهای لایه‌ی ارقامِ فارسی
                // است - شماره‌ی نسخه شناسه است نه عدد، و باید مو‌به‌مو همان چیزی باشد که در
                // بازار/مایکت نوشته شده تا در گزارشِ باگ قابلِ جست‌وجو بماند.
                "نسخه ${BuildConfig.VERSION_NAME}",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val SplashTop = Color(0xFF1FD68C)
private val SplashMid = Color(0xFF0EA968)
private val SplashDeep = Color(0xFF076B42)
