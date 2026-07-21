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
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppMuted

/**
 * افکتِ اسکلتونِ درخشان (shimmer) برای وقتی که خدماتِ اعتباری از سرور (GET /api/credit-rates)
 * در حال fetch شدنه - به‌جای پرشِ ناگهانیِ لیستِ خالی به پر، چند تایلِ جای‌گیرنده با یه گرادیانِ
 * روشن که مدام از راست به چپ می‌گذره نشون داده می‌شه.
 *
 * **باگِ رفع‌شده**: قبلاً گرادیان از Color.White ثابت ساخته می‌شد - رو تمِ تیره (پس‌زمینه‌ی سرمه‌ای)
 * قابل‌دیدن بود، ولی رو تمِ روشن (پس‌زمینه/کارتِ تقریباً کاملاً سفید، AppSurface = 0xFFFFFFFF تو
 * Color.kt) یه سفیدِ تقریباً شفاف رو یه پس‌زمینه‌ی سفید عملاً نامرئی بود - دقیقاً همون چیزی که کاربر
 * «خدمات اعتباری نیومد» می‌دید (درواقع در حالِ لود بود، فقط اسکلتونش دیده نمی‌شد). الان از AppMuted
 * (خاکستریِ میان‌رنگِ تم‌آگاه) استفاده می‌شه که رو هر دو تمِ روشن/تیره کنتراستِ کافی داره.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate",
    )
    val base = AppMuted
    background(
        Brush.linearGradient(
            colors = listOf(
                base.copy(alpha = 0.12f),
                base.copy(alpha = 0.32f),
                base.copy(alpha = 0.12f),
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
