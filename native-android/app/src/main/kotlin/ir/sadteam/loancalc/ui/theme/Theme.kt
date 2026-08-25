package ir.sadteam.loancalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

// فونتِ سراسریِ اپ Vazirmatn شد (به‌جای پیش‌فرضِ سیستم/اندروید که تا الان هیچ‌جا override نمی‌شد) -
// چون MaterialTheme متنِ پیش‌فرضِ کلِ اپ رو از رو AppTypography.bodyLarge می‌گیره (با
// ProvideTextStyle)، همین یه‌جا کافیه؛ هیچ Text ای تو اپ فونتِ صریح ست نکرده بود.
private val defaultTypography = Typography()
private val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = VazirmatnFontFamily),
)

// ⚠️ **بازطراحیِ جیبک**: این شکل‌ها فقط سلیقه‌ای نیستن - `AlertDialog`، `OutlinedTextField`،
// `DropdownMenu` و بقیه‌ی کامپوننت‌های آماده‌ی Material شکلشون رو از همین‌جا می‌گیرن. با
// رسوندنشون به توکن‌های `AppRadius`، دیالوگ‌ها و فیلدهایی که کامپوننتِ خودمون رو ندارن هم
// خودبه‌خود با بقیه‌ی اپ یک‌دست می‌شن (شش دیالوگ/پیکرِ اپ دقیقاً از همین راه به‌روز شدن).
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadius.icon),
    small = RoundedCornerShape(AppRadius.row),
    medium = RoundedCornerShape(AppRadius.card),
    large = RoundedCornerShape(AppRadius.sheet),
    extraLarge = RoundedCornerShape(AppRadius.sheet),
)

/**
 * حالت‌های تمِ اپ. تمِ تاریک الان برای **همه رایگانه** (یه دوره گیتِ اشتراکی داشت، برداشته شد).
 *
 * یه تمِ چهارمِ جدا («طلایی») یه دور امتحان شد ولی کاربر توضیح داد منظورش این نبود - می‌خواست
 * رنگِ طلایی فقط به‌عنوانِ **لهجه‌ی پرمیوم** تو هر دو تم دیده بشه، نه یه تمِ کاملاً جدا.
 */
enum class ThemeMode {
    LIGHT,
    DARK,

    /**
     * **تازه (بازطراحیِ جیبک)** - از تنظیماتِ خودِ گوشی پیروی می‌کنه. سیستمِ طراحی صریحاً این
     * حالت رو می‌خواد («یک `ColorScheme` جفتی + یک `ThemeMode` سه‌حالته»).
     *
     * ⚠️ برای کاربرِ قدیمی‌ای که تا الان روشن/تیره ذخیره کرده هیچ‌چی عوض نمی‌شه - این فقط یه
     * گزینه‌ی **اضافه**‌ست، پیش‌فرض همچنان [LIGHT]ه.
     */
    SYSTEM,
}

/** پورت toggleTheme تو www/index.html (کلاس body.light). پیش‌فرض روشن/سفیده (به‌درخواست کاربر
 * «تم اصلی برنامه سفید باشه»). */
@Composable
fun LoanCalcTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    // حالتِ «سیستم» به تنظیماتِ خودِ گوشی نگاه می‌کنه؛ بقیه صریح‌ان.
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (dark) DarkAppColors else LightAppColors
    val colorScheme = if (!dark) {
        lightColorScheme(
            background = palette.bg,
            surface = palette.surface,
            surfaceVariant = palette.surface2,
            primary = palette.primary,
            secondary = palette.goldInk,
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
            secondary = palette.goldInk,
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
