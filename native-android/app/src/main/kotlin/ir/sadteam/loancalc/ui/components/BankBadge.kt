package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_OTHER
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.pillOverSurface

/**
 * بجِ لوگوی بانک/سرویس - از رو اسم (`bankName`) تو `banks`/`creditServices` لوگو رو پیدا می‌کنه و
 * رو پس‌زمینه‌ی سفید نشون می‌ده (استاندارد اپ، رجوع کن به CLAUDE.md). اگه اسم با هیچ بانکی مچ نشه
 * (وام دستی با نام دلخواه/«مشخص‌نشده»)، fallback به حرف اول اسم رو یه بج رنگ ثابت `AppPrimaryDim`
 * می‌ره - عین رفتار `renderLoansList` تو www/index.html.
 */
/**
 * نسخه‌ی «حساب‌کتاب‌آگاه»ِ [BankBadge] - همه‌جای اپ که یه حساب‌کتاب نشون داده می‌شه باید از این
 * استفاده کنه، نه مستقیم از [BankBadge].
 *
 * حساب‌کتابِ **غیربانکی** (نقدی، کیفِ پول، کارتِ اعتباری…) لوگوی بانک نداره؛ به‌جاش آیکونِ انتخابیِ
 * خودش رو رو یه پس‌زمینه‌ی تینت‌شده‌ی سبز نشون می‌ده. اگه از [BankBadge] استفاده می‌شد، چون اسمِ
 * بانکش خالیه، fallbackِ «حرفِ اول رو بجِ رنگی» می‌افتاد که برای «نقدی» گمراه‌کننده‌ست.
 */
@Composable
fun AccountBadge(account: AccountEntity, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    if (account.type == ACCOUNT_TYPE_OTHER) {
        val shape = RoundedCornerShape(12.dp)
        val tint = AppPrimary
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(tint.pillOverSurface(0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = accountIconForKey(account.iconKey),
                contentDescription = account.name,
                tint = tint,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    } else {
        BankBadge(bankName = account.bankName, modifier = modifier, size = size)
    }
}

@Composable
fun BankBadge(bankName: String, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    // ۷ مهر، خواسته‌ی کاربر: لوگوی بانک‌ها هیچ‌جا نیست - فقط حرفِ اولِ اسمِ خودِ بانک.
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(AppPrimaryDim),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = bankInitial(bankName),
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** حرفِ اولِ اسمِ اصلیِ بانک: «بانک ملت» ← «م»، «موسسه اعتباری ملل» ← «م». */
fun bankInitial(name: String): String {
    val core = name.trim()
        .removePrefix("مؤسسه اعتباری").removePrefix("موسسه اعتباری")
        .removePrefix("بانک").trim()
    return core.take(1).ifEmpty { "؟" }
}
