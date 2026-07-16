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

val DarkAppColors = AppColorPalette(
    bg = Color(0xFF0D1321),
    surface = Color(0xFF161F35),
    surface2 = Color(0xFF1D2A46),
    primary = Color(0xFF2DD8B8),
    primaryDim = Color(0xFF1B8E78),
    accent = Color(0xFFF0A857),
    text = Color(0xFFEEF1F8),
    muted = Color(0xFF7C879E),
    danger = Color(0xFFE56B6F),
    line = Color(0x14EEF1F8), // rgba(238,241,248,0.08)
)

val LightAppColors = AppColorPalette(
    bg = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEEF1F8),
    primary = Color(0xFF0F9E88),
    primaryDim = Color(0xFF0C7C6A),
    accent = Color(0xFFC97A1F),
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
