package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.sadteam.loancalc.data.BankEntry
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim

/**
 * پورت .bank-item/.bank-badge تو www/index.html. لوگو همیشه رو پس‌زمینه‌ی سفید نشون داده
 * می‌شه (استاندارد مطابق اپ‌های مشابه، بنگرید CLAUDE.md) - این رفتار رو عوض نکن.
 */
@Composable
fun BankTile(
    bank: BankEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = if (selected) AppPrimaryDim else Color.Transparent
    val bg = if (selected) AppPrimary.copy(alpha = 0.1f) else Color.Transparent

    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(78.dp)
            .pressScaleClickable(goldBorderShape = shape, onClick = onClick)
            .background(bg, shape)
            .border(1.dp, border, shape)
            .padding(horizontal = 2.dp, vertical = 4.dp),
    ) {
        AsyncImage(
            model = "file:///android_asset/${bank.logoAsset}",
            contentDescription = bank.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .background(Color.White, RoundedCornerShape(AppRadius.row)),
        )
        // اسم کامل نشون داده می‌شه (بدون «...»)؛ اسم‌های بلندتر فونتشون خودکار کوچیک‌تر می‌شه تا
        // تو همون عرضِ ثابتِ تایل جا بشن و نظمِ ردیف بهم نریزه (خواسته‌ی کاربر).
        val nameFontSize = when {
            bank.name.length > 16 -> 9.5.sp
            bank.name.length > 11 -> 10.5.sp
            else -> 12.5.sp
        }
        Text(
            text = bank.name,
            fontSize = nameFontSize,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
