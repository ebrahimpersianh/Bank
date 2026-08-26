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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.JibakLogo
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
            .background(Brush.linearGradient(listOf(SplashTop, SplashMid, SplashDeep)))
            .drawBehind {
                // هاله‌ی بیضیِ بالا
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width * 0.9f,
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width / 2f, 0f),
                )
                // سه ذره‌ی نورِ ثابت - جاهای نامتقارن، عمق بدونِ شلوغی
                drawCircle(Color.White.copy(alpha = 0.50f), 2.5.dp.toPx(), Offset(size.width * 0.13f, size.height * 0.15f))
                drawCircle(Color.White.copy(alpha = 0.40f), 2.dp.toPx(), Offset(size.width * 0.84f, size.height * 0.23f))
                drawCircle(Color.White.copy(alpha = 0.35f), 2.dp.toPx(), Offset(size.width * 0.18f, size.height * 0.70f))
            },
        contentAlignment = Alignment.Center,
    ) {
        // اسپلش عمداً **عادی**ه؛ خواسته‌ی کاربر «لوگوی تمام‌صفحه» مالِ آیکونِ لانچر بود نه این.
        val logoWidth = 96.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp),
            modifier = Modifier.graphicsLayer {
                val enter = 0.94f + 0.06f * reveal.value
                val leave = 1f + 0.08f * exit.value
                scaleX = enter * leave
                scaleY = enter * leave
                alpha = reveal.value * (1f - exit.value)
            },
        ) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                                radius = size.minDimension * 0.5f,
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                JibakLogo(width = logoWidth)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text("جیبک", color = Color.White, fontSize = 33.sp, fontWeight = FontWeight.Black)
                Text(
                    "کیفِ پولت، مرتب",
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
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
                "نسخه ${toFa(BuildConfig.VERSION_NAME)}",
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
