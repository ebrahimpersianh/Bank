package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import ir.sadteam.loancalc.ui.jibak.faCardTail
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.data.db.AccountEntity

/**
 * دیالوگِ انتخابِ حساب - برای سینکِ خودکارِ پرداختِ وام/چک با حسابداری (رجوع کن به CLAUDE.md،
 * تصمیمِ صریحِ کاربر: «همه‌چیز به‌هم وصل باشه»، فعلاً اجباری). عمداً هیچ دکمه‌ی «رد کن»/«بدونِ
 * حساب» نداره - اگه کاربر بخواد بی‌خیالِ ثبتِ تراکنش بشه، باید کلِ دیالوگ رو ببنده (onDismiss)، که
 * یعنی خودِ عملِ «پرداخت‌شده کردن» هم لغو می‌شه؛ منطقِ فراخوان (نه این کامپوننت) تصمیم می‌گیره وقتی
 * هیچ حسابی از قبل ساخته نشده چیکار کنه (رجوع کن به کامنتِ محلِ استفاده).
 */
@Composable
fun AccountPickerDialog(
    accounts: List<AccountEntity>,
    onSelect: (AccountEntity) -> Unit,
    onDismiss: () -> Unit,
    title: String = "از کدام حساب پرداخت کردی؟",
    dismissHint: String? = null,
) {
    // بستنِ پنجره = بی‌حساب. اگر فراخوان بخواهد، به کاربر می‌گوید چه اتفاقی افتاد (نه بی‌صدا).
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val dismiss = {
        dismissHint?.let { android.widget.Toast.makeText(ctx, it, android.widget.Toast.LENGTH_LONG).show() }
        onDismiss()
    }
    ir.sadteam.loancalc.ui.components.JibakListSheet(
        onDismissRequest = dismiss,
        title = title,
    ) {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
            ) {
                accounts.forEach { account ->
                    // لوگوی بانک + نام؛ حسابِ نقدی «()»ِ خالی نشان نمی‌داد (گزارشِ کاربر، ۶ مهر).
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .background(ir.sadteam.loancalc.ui.theme.AppSurface2)
                            .clickable { onSelect(account) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        // تستِ ۱۳ آبان: حسابِ نقدی (بی بانک) «؟» می‌گرفت - حالا آیکونِ خودِ حساب.
                        if (account.bankName.isNotBlank()) BankBadge(bankName = account.bankName, size = 36.dp)
                        else androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ir.sadteam.loancalc.ui.theme.AppIconFrame),
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                ir.sadteam.loancalc.data.accountIconForKey(account.iconKey),
                                contentDescription = null,
                                tint = ir.sadteam.loancalc.ui.theme.AppMuted,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(account.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            // فریمِ `18`: بانک + ۴ رقمِ آخرِ کارت، تا دو حسابِ یک بانک از هم شناخته شوند.
                            val last4 = account.cardNumber?.takeLast(4)?.takeIf { it.length == 4 }
                            val sub = listOfNotNull(
                                account.bankName.takeIf { it.isNotBlank() && it != account.name },
                                last4?.let { faCardTail(it) },
                            ).joinToString(" · ")
                            if (sub.isNotEmpty()) {
                                Text(sub, fontSize = 11.sp, color = ir.sadteam.loancalc.ui.theme.AppMuted)
                            }
                        }
                    }
                }
            }
    }
}
