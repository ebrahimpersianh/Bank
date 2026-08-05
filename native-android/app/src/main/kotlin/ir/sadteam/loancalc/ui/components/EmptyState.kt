package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * حالتِ «هنوز چیزی اینجا نیست» - قبلاً هر لیستِ خالی فقط یه خطِ متنِ خشکِ وسطِ صفحه بود
 * («هنوز وامی ذخیره نشده») که حسِ اپِ ناتمام می‌داد. این کامپوننت به‌جاش یه آیکونِ بزرگ تو یه
 * هاله‌ی نرمِ نفس‌کِش + عنوان + یه جمله‌ی راهنما + (اختیاری) دکمه‌ی اقدام نشون می‌ده.
 *
 * هاله عمداً یه گرادیانِ شعاعیِ خیلی کم‌رنگه، نه بلورِ واقعی - هم‌راستا با تصمیمِ سراسریِ پروژه که
 * هیچ‌جا `RenderEffect` واقعی استفاده نشه (رجوع کن به کامنتِ `AuroraBackground`) تا رو گوشیِ ضعیف
 * هم روان بمونه.
 *
 * @param actionLabel اگه non-null باشه یه [GradientButton] زیرِ متن نشون داده می‌شه.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    // نفس‌کشیدنِ خیلی آروم (۳ ثانیه رفت‌وبرگشت) - فقط اونقدر که صفحه «مُرده» به‌نظر نیاد؛ عمداً
    // ملایم‌تر از حدیه که حواسِ کاربر رو پرت کنه.
    val breathe = rememberInfiniteTransition(label = "emptyStateBreathe")
    val pulse by breathe.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emptyStatePulse",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // هاله‌ی پشتِ آیکون - همون دو رنگِ هویتیِ اپ (سبزآبی + طلایی) که تو Aurora/ProgressRing
            // هم استفاده می‌شن، فقط خیلی رقیق‌تر.
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppPrimary.copy(alpha = 0.16f),
                                AppAccent.copy(alpha = 0.07f),
                                AppPrimary.copy(alpha = 0f),
                            ),
                        ),
                    ),
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppPrimary.copy(alpha = 0.75f),
                modifier = Modifier.size(56.dp),
            )
        }

        Text(
            text = title,
            color = AppText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = description,
            color = AppMuted,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (actionLabel != null && onAction != null) {
            GradientButton(
                onClick = onAction,
                modifier = Modifier.padding(top = 22.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}
