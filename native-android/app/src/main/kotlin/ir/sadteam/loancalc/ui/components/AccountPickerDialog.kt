package ir.sadteam.loancalc.ui.components

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
                    TextButton(
                        onClick = { onSelect(account) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) {
                        Text("${account.name} (${account.bankName})", modifier = Modifier.fillMaxWidth())
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
