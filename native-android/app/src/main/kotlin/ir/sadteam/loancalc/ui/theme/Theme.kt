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
import ir.sadteam.loancalc.data.coin.ThemePalette
import androidx.compose.ui.unit.dp

// فونتِ سراسریِ اپ Vazirmatn شد (به‌جای پیش‌فرضِ سیستم/اندروید که تا الان هیچ‌جا override نمی‌شد) -
// چون MaterialTheme متنِ پیش‌فرضِ کلِ اپ رو از رو AppTypography.bodyLarge می‌گیره (با
// ProvideTextStyle)، همین یه‌جا کافیه؛ هیچ Text ای تو اپ فونتِ صریح ست نکرده بود.
private val defaultTypography = Typography()
/**
 * 🚨 از `val`ِ ثابت به **تابع** تبدیل شد: قلم حالا خریدنی است (دسته‌ی «قلم» در
 * فروشگاه) و باید با تغییرِ [AppFontState] دوباره ساخته شود. ورودی صریح است تا از
 * بیرونِ composition هم قابلِ ساخت بماند.
 */
private fun appTypography(choice: AppFontChoice): Typography {
    // تیتر و بدنه دو خانواده‌ی جدا می‌گیرند - جوابِ طراح به سوالِ وزن (دورِ ۱۰).
    val titleFamily = if (choice.target == FontTarget.BODY) VazirmatnFontFamily else choice.family
    val bodyFamily = if (choice.target == FontTarget.TITLE) VazirmatnFontFamily else choice.family
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = titleFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = titleFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = titleFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = titleFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = titleFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = titleFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = titleFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = titleFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = titleFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = bodyFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = bodyFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = bodyFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = bodyFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = bodyFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = bodyFamily),
    )
}

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
    colorTheme: ColorTheme = ColorTheme.GREEN,
    /**
     * تمِ خریدنیِ کاتالوگِ فروشگاه (بخشِ ۵۹). اگر پر باشد **جای** [colorTheme] می‌نشیند -
     * دو تم هم‌زمان بی‌معنی است و شناسه‌ی ذخیره‌شده همیشه یکی از این دو خانواده است.
     */
    catalogTheme: ThemePalette? = null,
    content: @Composable () -> Unit,
) {
    // حالتِ «سیستم» به تنظیماتِ خودِ گوشی نگاه می‌کنه؛ بقیه صریح‌ان.
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    // تمِ رنگیِ خریدنی فقط خانواده‌ی primary رو رو همین پالت می‌نشونه (رجوع کن به [ColorTheme]).
    val base = if (dark) DarkAppColors else LightAppColors
    val palette = catalogTheme?.applyTo(base) ?: colorTheme.applyTo(base)
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
            typography = appTypography(AppFontState.choice),
            shapes = AppShapes,
            content = content,
        )
    }
}
