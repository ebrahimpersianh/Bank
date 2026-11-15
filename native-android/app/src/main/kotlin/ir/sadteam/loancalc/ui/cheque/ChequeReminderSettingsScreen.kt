package ir.sadteam.loancalc.ui.cheque

import androidx.compose.runtime.Composable
import ir.sadteam.loancalc.ui.settings.ReminderSettingsScreen

/**
 * «تنظیمات یادآوری چک» تو منوی «امور چک» - چون منوی چک یه آیتم جدا برای این می‌خواست (نه اینکه
 * کاربر رو مجبور کنیم بره تنظیماتِ کلیِ اپ)، ولی خودِ تنظیمات (زمان‌بندی/صدا/ویبره) دیگه یه صفحه‌ی
 * واحده که هم رو اقساط وام هم رو چک‌ها اثر می‌ذاره - رجوع کن به [ReminderSettingsScreen]. دو تا
 * کپیِ جدا از منطقِ تنظیمات نداریم، این فقط یه مسیرِ ورودیِ دومه.
 */
@Composable
fun ChequeReminderSettingsScreen(onBack: () -> Unit) {
    ReminderSettingsScreen(onBack = onBack)
}
