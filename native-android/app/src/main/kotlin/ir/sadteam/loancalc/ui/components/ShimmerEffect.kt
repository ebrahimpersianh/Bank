package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * افکتِ اسکلتونِ درخشان (shimmer) برای وقتی که خدماتِ اعتباری از سرور (GET /api/credit-rates)
 * در حال fetch شدنه - به‌جای پرشِ ناگهانیِ لیستِ خالی به پر، چند تایلِ جای‌گیرنده با یه گرادیانِ
 * روشن که مدام از راست به چپ می‌گذره نشون داده می‌شه.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate",
    )
    background(
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.06f),
            ),
            start = Offset(translate - 200f, 0f),
            end = Offset(translate + 200f, 200f),
        ),
    )
}

@Composable
fun BankTileShimmer(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(78.dp)
            .padding(horizontal = 2.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(15.dp))
                .shimmerEffect(),
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(50.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(),
        )
    }
}
