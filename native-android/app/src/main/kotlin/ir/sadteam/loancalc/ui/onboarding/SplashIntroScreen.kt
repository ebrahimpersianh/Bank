package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated brand intro: a single gold coin drops into the mark, then the exact
 * launcher logo settles in. The system icon and the first in-app frame are one
 * visual language, not two unrelated illustrations.
 */
@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    val coinDrop = remember { Animatable(0f) }
    val walletReveal = remember { Animatable(0f) }
    val wordsReveal = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            coinDrop.animateTo(
                1f,
                animationSpec = tween(780, easing = CubicBezierEasing(0.18f, 0.82f, 0.22f, 1f)),
            )
        }
        launch {
            // لوگوی نهایی فقط بعد از رسیدن سکه ظاهر می‌شود؛ هم‌پوشانی دو سکه نداریم.
            delay(790)
            walletReveal.animateTo(
                1f,
                animationSpec = tween(420, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
            )
        }
        launch {
            delay(1080)
            wordsReveal.animateTo(1f, animationSpec = tween(360))
        }
        launch { progress.animateTo(1f, animationSpec = tween(1580)) }
        delay(2080)
        exit.animateTo(1f, animationSpec = tween(220))
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashCream)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SplashGlow, Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.50f,
                    ),
                    radius = size.minDimension * 0.50f,
                    center = center,
                )
            }
            .graphicsLayer { alpha = 1f - exit.value },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(276.dp),
        ) {
            // Coin: it drops from above with a small physical bounce.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(96.dp)
                    .graphicsLayer {
                        // فقط یک سکه می‌بینیم: قبل از رسیدن سکه‌ی متحرک و بعدش سکه‌ی خود لوگو.
                        alpha = if (walletReveal.value == 0f) 1f else 0f
                        translationY = 84f - 560f * (1f - coinDrop.value)
                        scaleX = 0.82f + 0.18f * coinDrop.value
                        scaleY = 0.82f + 0.18f * coinDrop.value
                        rotationZ = -10f * (1f - coinDrop.value)
                    }
                    .clip(CircleShape)
                    .background(CoinEdge),
                contentAlignment = Alignment.Center,
            ) {
                // همان زبانِ بصریِ سکه‌ی داخل لوگو: لبه، سطح طلایی و درخشش.
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CoinShine, CoinGold, Color(0xFFE7A51D)),
                                center = androidx.compose.ui.geometry.Offset(26f, 22f),
                                radius = 92f,
                            ),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color(0x22A85E00)),
                    )
                }
            }

            // قاب و نشانِ نهایی دقیقاً از همان وکتورِ لانچر ساخته می‌شوند.
            // این کار اختلافِ اسپلش و آیکونِ صفحهٔ اصلی را از ریشه حذف می‌کند.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(228.dp)
                    .clip(RoundedCornerShape(58.dp))
                    .background(LauncherGreen)
                    .graphicsLayer {
                        alpha = walletReveal.value
                        scaleX = 0.84f + 0.16f * walletReveal.value
                        scaleY = 0.84f + 0.16f * walletReveal.value
                        translationY = 26f * (1f - walletReveal.value)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.jibak_brand_mark),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(228.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 320.dp)
                .graphicsLayer {
                    alpha = wordsReveal.value
                    translationY = 18f * (1f - wordsReveal.value)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "جیبک",
                color = SplashGreen,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "کیف پولت، مرتب",
                color = SplashMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(SplashTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(RoundedCornerShape(99.dp))
                        .background(SplashGreen),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "نسخه " + BuildConfig.VERSION_NAME,
                color = SplashMuted.copy(alpha = 0.72f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private val SplashCream = Color(0xFFFFFCF4)
private val SplashGlow = Color(0x4429C97E)
private val LauncherGreen = Color(0xFF0B3A2A)
private val SplashGreen = Color(0xFF0B8C57)
private val SplashMuted = Color(0xFF83A697)
private val SplashTrack = Color(0x3329C97E)
private val CoinShine = Color(0xFFFFFDF5)
private val CoinGold = Color(0xFFF9C042)
private val CoinEdge = Color(0xFFBC7A08)
