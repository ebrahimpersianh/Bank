package ir.sadteam.loancalc.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ir.sadteam.loancalc.ui.sound.rememberDeleteSound
import ir.sadteam.loancalc.ui.theme.AppDanger

/**
 * دیالوگِ تاییدِ حذفِ عمومی - برای وام/چک/دسته‌چک/حساب/درآمد که قبلاً بدونِ هیچ تاییدی حذف می‌شدن
 * (فقط «حذف حساب کاربری» تو SettingsScreen.kt از قبل تاییدِ داشت، چون یه عملیاتِ شبکه‌ایه - این
 * کامپوننت برای حذف‌های محلیِ ساده‌تره، بدونِ حالتِ لودینگ). خودِ حذفِ واقعی تو [onConfirm] صدا
 * زده می‌شه، این کامپوننت فقط UIه. چون این تنها مسیرِ مشترکِ همه‌ی حذف‌های واقعیِ اپه، صدای حذف
 * (رجوع کن به rememberDeleteSound) هم همینجا مرکزی پخش می‌شه - نیازی به دست‌زدن به تک‌تکِ صفحه‌ها نیست.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val playDeleteSound = rememberDeleteSound()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = {
                    playDeleteSound()
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppDanger),
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        },
    )
}
