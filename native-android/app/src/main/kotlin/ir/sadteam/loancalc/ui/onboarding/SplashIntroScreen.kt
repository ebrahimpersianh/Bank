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
 * Animated brand intro: a coin drops in first, the wallet catches it, then the
 * wordmark and progress line settle in. The final artwork is the same stage-0
 * image as the launcher, so there is no visual jump between system and app.
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
                animationSpec = spring(dampingRatio = 0.52f, stiffness = 310f),
            )
        }
        launch {
            delay(360)
            walletReveal.animateTo(
                1f,
                animationSpec = tween(440, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
            )
        }
        launch {
            delay(680)
            wordsReveal.animateTo(1f, animationSpec = tween(330))
        }
        launch { progress.animateTo(1f, animationSpec = tween(1450)) }
        delay(1650)
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
                .size(270.dp),
        ) {
            // Coin: it drops from above with a small physical bounce.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(100.dp)
                    .graphicsLayer {
                        alpha = 1f - walletReveal.value
                        translationY = -250f * (1f - coinDrop.value)
                        scaleX = 0.88f + 0.12f * coinDrop.value
                        scaleY = 0.88f + 0.12f * coinDrop.value
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CoinShine, CoinGold, CoinEdge),
                            center = androidx.compose.ui.geometry.Offset(30f, 25f),
                            radius = 110f,
                        ),
                    ),
            )

            // Wallet catches the coin. The asset already contains the final,
            // carefully illustrated coin so the handoff ends in the true logo.
            Image(
                painter = painterResource(R.drawable.jibak_stage_0),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(270.dp)
                    .graphicsLayer {
                        alpha = walletReveal.value
                        scaleX = 0.80f + 0.20f * walletReveal.value
                        scaleY = 0.80f + 0.20f * walletReveal.value
                        translationY = 34f * (1f - walletReveal.value)
                    },
            )
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
private val SplashGlow = Color(0x3329C97E)
private val SplashGreen = Color(0xFF0B8C57)
private val SplashMuted = Color(0xFF83A697)
private val SplashTrack = Color(0x3329C97E)
private val CoinShine = Color(0xFFFFFDF5)
private val CoinGold = Color(0xFFF9C042)
private val CoinEdge = Color(0xFFBC7A08)
