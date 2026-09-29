package ir.sadteam.loancalc.ui.support

import androidx.compose.runtime.Composable

/**
 * «تماس با ما» - از ۷ مهر همان صفحه‌ی پیامِ مستقیم به سرور است ([BugReportScreen]).
 * ایمیل به خواسته‌ی کاربر کاملاً از برنامه برداشته شد؛ یک راه، نه دو تا.
 */
@Composable
fun ContactSupportContent(onClose: () -> Unit) {
    BugReportScreen(onBack = onClose)
}
