package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.creditServices
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim

/**
 * بجِ لوگوی بانک/سرویس - از رو اسم (`bankName`) تو `banks`/`creditServices` لوگو رو پیدا می‌کنه و
 * رو پس‌زمینه‌ی سفید نشون می‌ده (استاندارد اپ، رجوع کن به CLAUDE.md). اگه اسم با هیچ بانکی مچ نشه
 * (وام دستی با نام دلخواه/«مشخص‌نشده»)، fallback به حرف اول اسم رو یه بج رنگ ثابت `AppPrimaryDim`
 * می‌ره - عین رفتار `renderLoansList` تو www/index.html.
 */
@Composable
fun BankBadge(bankName: String, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    val meta = remember(bankName) { (banks + creditServices).firstOrNull { it.name == bankName } }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(if (meta != null) Color.White else AppPrimaryDim),
        contentAlignment = Alignment.Center,
    ) {
        if (meta != null) {
            AsyncImage(
                model = "file:///android_asset/${meta.logoAsset}",
                contentDescription = bankName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(5.dp),
            )
        } else {
            Text(
                text = bankName.trim().take(1).ifEmpty { "؟" },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
