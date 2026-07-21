package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
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
    val bg = if (selected) AppPrimary.copy(alpha = 0.12f) else AppSurface2
    val shape = RoundedCornerShape(12.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .width(72.dp)
            .height(92.dp)
            .pressScaleClickable(goldBorderShape = shape, onClick = onClick)
            .background(bg, shape)
            .border(1.dp, AppText.copy(alpha = 0.5f), shape)
            .padding(horizontal = 5.dp, vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = title, tint = AppPrimary, modifier = Modifier.size(22.dp))
        Text(
            text = title,
            color = AppText,
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = sub,
            fontSize = 9.5.sp,
            color = AppMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
