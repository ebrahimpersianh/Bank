package ir.sadteam.loancalc.ui.components

import ir.sadteam.loancalc.ui.jibak.faCardTail
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Text
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
    title: String = "از کدوم حساب پرداخت کردی؟",
) {
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                accounts.forEach { account ->
                    // لوگوی بانک + نام؛ حسابِ نقدی «()»ِ خالی نشان نمی‌داد (گزارشِ کاربر، ۶ مهر).
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(account) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        BankBadge(bankName = account.bankName, size = 36.dp)
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
