package ir.sadteam.loancalc.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import ir.sadteam.loancalc.data.coin.ThemePalette

/**
 * نشاندنِ یک تمِ **کاتالوگِ فروشگاه** ([ThemePalette]) روی پالتِ پایه.
 *
 * چهار تمِ قدیمی (`ColorTheme`) هر مقدارشان دستی است؛ تم‌های کاتالوگ عمداً فقط **سه رنگ +
 * یک جوهر** دارند و بقیه با فرمول ساخته می‌شوند - هر توکنِ دستیِ اضافه یک راهِ تازه برای
 * شکستنِ تمِ تیره است (قاعده‌ی صریحِ بخشِ ۵۹).
 *
 * فرمول‌ها:
 * - `primaryDim` و `heroShadow` = `dark`ِ پالت.
 * - حالتِ روشن: قرص و حاشیه تینتِ `light` روی سفیدند؛ جوهر مقدارِ دستیِ `inkLight`.
 * - حالتِ تیره: قرص و حاشیه همان `light` با آلفای ۱۴٪ و ۳۵٪ (هم‌قاعده با تم‌های قدیمی)، و
 *   **جوهر همان `light`** - هر شش ترکیب روی `dark`ِ خودشان بالای ۵٫۹:۱ درآمدند، پس جدولِ
 *   دستی لازم نیست و تمِ هفتم هم خودبه‌خود درست می‌آید.
 */
fun ThemePalette.applyTo(base: AppColorPalette): AppColorPalette {
    val primaryColor = Color(primary)
    val lightColor = Color(light)
    return if (base.isDark) {
        base.copy(
            primary = primaryColor,
            primaryDim = Color(dark),
            primaryInk = lightColor,
            primaryLight = lightColor,
            primaryPill = lightColor.copy(alpha = 0.14f),
            primaryBorder = lightColor.copy(alpha = 0.35f),
            heroShadow = Color(dark),
        )
    } else {
        base.copy(
            primary = primaryColor,
            primaryDim = Color(dark),
            primaryInk = Color(inkLight),
            primaryLight = lightColor,
            primaryPill = lerp(Color.White, lightColor, 0.30f),
            primaryBorder = lerp(Color.White, lightColor, 0.62f),
            heroShadow = Color(dark),
        )
    }
}
