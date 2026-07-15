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

/** پورت toggleTheme تو www/index.html (کلاس body.light). [darkTheme] از ThemeViewModel/DataStore
 * میاد؛ پیش‌فرض روشن/سفیده (به‌درخواست کاربر «تم اصلی برنامه سفید باشه»). */
@Composable
fun LoanCalcTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) {
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
    } else {
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
