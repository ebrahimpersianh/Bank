package ir.sadteam.loancalc.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** دقیقاً هم‌راستا با :root {}/body.light {} تو www/index.html. */
data class AppColorPalette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val primaryDim: Color,
    val accent: Color,
    val text: Color,
    val muted: Color,
    val danger: Color,
    val line: Color,
)

// خواسته‌ی صریح کاربر: primary/primaryDim از یه سبزِ فیروزه‌ایِ نزدیک به سبز (hue ~169°) به یه
// سبزآبیِ واضح‌تر و پررنگ‌تر (hue ~185-187°، اشباعِ ~100%، حداقل ۳۰٪ پررنگ‌تر از قبل) تغییر کرد.
// چون همه‌ی پس‌زمینه‌های کم‌رنگِ باکس‌ها تو کل اپ با AppPrimary.copy(alpha=...) ساخته می‌شن، همین
// یه تغییر رنگِ پایه کافیه که همه‌جا (دکمه، آیکون، پس‌زمینه‌ها) یکجا آپدیت بشه.
val DarkAppColors = AppColorPalette(
    bg = Color(0xFF0D1321),
    surface = Color(0xFF161F35),
    surface2 = Color(0xFF1D2A46),
    primary = Color(0xFF00C2D1),
    primaryDim = Color(0xFF00838D),
    accent = Color(0xFFFFB020),
    text = Color(0xFFEEF1F8),
    muted = Color(0xFF7C879E),
    danger = Color(0xFFE56B6F),
    line = Color(0x14EEF1F8), // rgba(238,241,248,0.08)
)

val LightAppColors = AppColorPalette(
    bg = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEEF1F8),
    primary = Color(0xFF008C99),
    primaryDim = Color(0xFF00707A),
    accent = Color(0xFFB8860B),
    text = Color(0xFF141A2A),
    muted = Color(0xFF6B7488),
    danger = Color(0xFFC6474B),
    line = Color(0x14141A2A), // rgba(20,26,42,0.08)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/**
 * این‌ها عمداً property هستن نه val ثابت (با `@Composable get()`) - همه‌ی صفحه‌ها همین اسم‌ها رو
 * مستقیم import می‌کنن (`AppText`، `AppPrimary`، ...)، این‌جوری فقط با اضافه‌کردن تم روشن به
 * [LocalAppColors]، بدون تغییر تک‌تک صفحه‌ها همه‌جا رنگ درست به‌روز می‌شه.
 */
val AppBg: Color @Composable get() = LocalAppColors.current.bg
val AppSurface: Color @Composable get() = LocalAppColors.current.surface
val AppSurface2: Color @Composable get() = LocalAppColors.current.surface2
val AppPrimary: Color @Composable get() = LocalAppColors.current.primary
val AppPrimaryDim: Color @Composable get() = LocalAppColors.current.primaryDim
val AppAccent: Color @Composable get() = LocalAppColors.current.accent
val AppText: Color @Composable get() = LocalAppColors.current.text
val AppMuted: Color @Composable get() = LocalAppColors.current.muted
val AppDanger: Color @Composable get() = LocalAppColors.current.danger
val AppLine: Color @Composable get() = LocalAppColors.current.line
