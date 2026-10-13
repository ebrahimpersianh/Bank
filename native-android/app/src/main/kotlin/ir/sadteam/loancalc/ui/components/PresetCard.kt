package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/** پورت .preset-card تو www/index.html (وام‌های پرتکرار). به‌درخواست کاربر همه‌ی کارت‌ها هم‌سایز
 * (عرض و ارتفاع ثابت) و با یه خط نازک مشکی دورشون هستن؛ انتخاب با پُرشدنِ سبز مشخص می‌شه، نه
 * حاشیه‌ی سبز (حاشیه‌ی سبز مخصوص بقیه‌ی باکس‌هاست). */
@Composable
fun PresetCard(
    icon: ImageVector,
    title: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ۲۰ مهر (خواسته‌ی کاربر: «صفحه کمتر اسکرول بخورد»): کارتِ ۹۲dpیِ ایستاده به قرصِ ۴۴dpیِ
    // یک‌خطی تبدیل شد - همان آیکون/نام/نرخ، نصفِ ارتفاع.
    val bg = if (selected) AppPrimary.copy(alpha = 0.12f) else AppSurface2
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .height(44.dp)
            .pressScaleClickable(goldBorderShape = shape, onClick = onClick)
            .background(bg, shape)
            .border(1.dp, if (selected) AppPrimary else AppText.copy(alpha = 0.3f), shape)
            .padding(horizontal = 10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            color = AppText,
            fontSize = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            maxLines = 1,
        )
        if (sub.isNotBlank()) Text(text = sub, fontSize = 10.sp, color = AppMuted, maxLines = 1)
    }
}
