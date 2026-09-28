package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppDashedBorder
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * حالتِ «هنوز چیزی اینجا نیست» - **بازطراحیِ سبکِ «جیبک»** (بخشِ ۲۱ فایلِ طراحی، کارت‌های
 * `21a`..`21e`).
 *
 * ⚠️ **تفاوت با دورِ قبل**: هاله‌ی گرادیانِ شعاعیِ نفس‌کِشِ پشتِ آیکون **حذف شد** (سبکِ جدید
 * گرادیانِ نوری نداره). جاش طبقِ طرح:
 * - کارتِ سفید با **حاشیه‌ی نقطه‌چینِ ۲ پیکسلی** (`#C9D6CF`) - نشانه‌ی «اینجا هنوز خالیه»
 * - قابِ آیکونِ ۶۴ با گوشه‌ی ۲۰ و ته‌رنگِ سبزِ کم‌رنگ
 * - عنوانِ ۱۴/۹۰۰ و توضیحِ ۱۱/۷۰۰
 * - دکمه‌ی اصلیِ تمام‌عرض (اختیاری)
 *
 * قاعده‌ی صریحِ طرح: «حاشیه‌ی خط‌چین، یک دکمه‌ی اصلی، و یک میان‌بُرِ متنی زیرش».
 *
 * @param actionLabel اگه non-null باشه یه [GradientButton]ِ تمام‌عرض زیرِ متن نشون داده می‌شه.
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
    // ⚠️ توکن‌های رنگ `@Composable`ان و داخلِ `drawBehind` (که `DrawScope`ه) صدا زده نمی‌شن -
    // قاعده‌ی ماندگارِ پروژه. برای همین اینجا تو یه `val` محلی خونده می‌شن.
    val dashColor = AppDashedBorder
    val cardRadius = AppRadius.card

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardRadius))
            .background(AppSurface)
            .drawBehind {
                // حاشیه‌ی نقطه‌چین - `Modifier.border` الگوی خط‌چین نداره، پس دستی کشیده می‌شه.
                val stroke = 2.dp.toPx()
                val r = cardRadius.toPx()
                drawRoundRect(
                    color = dashColor,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f),
                    ),
                )
            }
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(cardRadius))
                .background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppPrimaryInk,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = title,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = description,
            color = AppMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (actionLabel != null && onAction != null) {
            GradientButton(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}
