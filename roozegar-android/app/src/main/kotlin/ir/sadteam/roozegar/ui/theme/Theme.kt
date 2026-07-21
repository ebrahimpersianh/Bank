package ir.sadteam.roozegar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkScheme = darkColorScheme(
    primary = Teal,
    onPrimary = NightBg,
    secondary = Gold,
    onSecondary = NightBg,
    background = NightBg,
    onBackground = TextPrimary,
    surface = NightBgHigh,
    onSurface = TextPrimary,
    surfaceVariant = NightBgHigh,
    onSurfaceVariant = TextMuted,
    error = Danger,
)

/** تم سراسری: تیره (شیشه‌محور)، فونت وزیرمتن، و جهتِ کل UI راست‌به‌چپ. */
@Composable
fun RoozegarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, typography = RoozegarTypography) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, content = content)
    }
}
