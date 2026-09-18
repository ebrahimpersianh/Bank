package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
 * Warm, minimal splash for the wallet-and-coin Jibak identity.
 *
 * The launcher and splash deliberately share the same stage-0 artwork, so the
 * Android system splash transitions into the in-app intro without a logo jump.
 */
@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            reveal.animateTo(
                1f,
                animationSpec = tween(620, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
            )
        }
        launch { progress.animateTo(1f, animationSpec = tween(1320)) }
        delay(1450)
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
                        radius = size.minDimension * 0.48f,
                    ),
                    radius = size.minDimension * 0.48f,
                    center = center,
                )
            }
            .graphicsLayer { alpha = 1f - exit.value },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    alpha = reveal.value
                    scaleX = 0.82f + 0.18f * reveal.value
                    scaleY = 0.82f + 0.18f * reveal.value
                    translationY = 24f * (1f - reveal.value)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.jibak_stage_0),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(252.dp),
            )
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
