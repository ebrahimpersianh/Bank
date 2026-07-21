package ir.sadteam.roozegar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.sadteam.roozegar.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_light, FontWeight.Light),
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val default = Typography()

/** همه‌ی استایل‌های متریال با فونت وزیرمتن - عنوان‌ها Medium/Bold، متن‌ها Normal. */
val RoozegarTypography = Typography(
    displayLarge = default.displayLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    displayMedium = default.displayMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    displaySmall = default.displaySmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    headlineLarge = default.headlineLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    headlineMedium = default.headlineMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    headlineSmall = default.headlineSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    titleLarge = default.titleLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    titleMedium = default.titleMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    titleSmall = default.titleSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    bodyLarge = default.bodyLarge.copy(fontFamily = Vazirmatn),
    bodyMedium = default.bodyMedium.copy(fontFamily = Vazirmatn),
    bodySmall = default.bodySmall.copy(fontFamily = Vazirmatn),
    labelLarge = default.labelLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    labelMedium = default.labelMedium.copy(fontFamily = Vazirmatn),
    labelSmall = default.labelSmall.copy(fontFamily = Vazirmatn),
)
