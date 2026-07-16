package ir.sadteam.loancalc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppTypography = Typography()

// گوشه‌گردیِ مینیمالِ سراسری - چون OutlinedTextField شکلش رو از shapes.extraSmall می‌گیره، این‌جا
// یه‌بار گردتر کردنش همه‌ی فیلدهای ورودی اپ رو گوشه‌گرد می‌کنه (به‌درخواست کاربر «همه باکس‌ها ar دار»).
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
)

/** حالت‌های تمِ اپ - روشن (پیش‌فرض، رایگان) و تاریک (ویژگی اشتراکی، رجوع کن به
 * AuthViewModel.subscribed تو MainActivity/SettingsScreen که تعویض بهش رو گیت می‌کنه). یه تمِ سومِ
 * جدا («طلایی») یه دور امتحان شد ولی کاربر توضیح داد منظورش این نبود - می‌خواست رنگِ طلایی
 * (که همون [AppAccent] موجوده) فقط به‌عنوانِ لهجه‌ی ظریف تو کل اپ (هر دو تمِ روشن/تاریک) بیشتر
 * دیده بشه، نه یه تمِ کاملاً جدا. برای همین تمِ طلاییِ جدا حذف شد؛ [GoldAppColors] (تو Color.kt)
 * دیگه به‌عنوانِ تمِ فعال استفاده نمی‌شه. */
enum class ThemeMode { LIGHT, DARK }

/** پورت toggleTheme تو www/index.html (کلاس body.light). پیش‌فرض روشن/سفیده (به‌درخواست کاربر
 * «تم اصلی برنامه سفید باشه»). */
@Composable
fun LoanCalcTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val palette = if (themeMode == ThemeMode.DARK) DarkAppColors else LightAppColors
    val colorScheme = if (themeMode == ThemeMode.LIGHT) {
        lightColorScheme(
            background = palette.bg,
            surface = palette.surface,
            surfaceVariant = palette.surface2,
            primary = palette.primary,
            secondary = palette.accent,
            onBackground = palette.text,
            onSurface = palette.text,
            onPrimary = Color.White,
            error = palette.danger,
        )
    } else {
        darkColorScheme(
            background = palette.bg,
            surface = palette.surface,
            surfaceVariant = palette.surface2,
            primary = palette.primary,
            secondary = palette.accent,
            onBackground = palette.text,
            onSurface = palette.text,
            onPrimary = Color(0xFF04211C),
            error = palette.danger,
        )
    }

    CompositionLocalProvider(LocalAppColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
