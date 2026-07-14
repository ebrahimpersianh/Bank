package ir.sadteam.loancalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppDarkColorScheme = darkColorScheme(
    background = AppBg,
    surface = AppSurface,
    surfaceVariant = AppSurface2,
    primary = AppPrimary,
    secondary = AppAccent,
    onBackground = AppText,
    onSurface = AppText,
    onPrimary = Color(0xFF04211C),
    error = AppDanger,
)

private val AppTypography = Typography()

@Composable
fun LoanCalcTheme(
    content: @Composable () -> Unit,
) {
    // فعلاً فقط تم تیره پورت شده (مطابق کارهای در حال انجام؛ روشن رو بعداً اضافه می‌کنیم)
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
