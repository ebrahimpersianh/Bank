package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.haptics.HapticsViewModel
import ir.sadteam.loancalc.ui.theme.AppIsDark
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

private val fontSizeOptions = listOf(0.9f to "کوچک", 1f to "متوسط", 1.15f to "بزرگ")
private val themeModeOptions =
    listOf(ThemeMode.LIGHT to "روشن", ThemeMode.DARK to "تاریک", ThemeMode.SYSTEM to "خودکار")
@Composable
internal fun AppearanceSettings(themeViewModel: ThemeViewModel, hapticsViewModel: HapticsViewModel = hiltViewModel()) {
    val vibrationEnabled by hapticsViewModel.enabled.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val reducedMotion by themeViewModel.reducedMotion.collectAsState()
    val isDark = AppIsDark
    // همون افکتِ دایره‌ایِ تعویضِ تم که از مرکزِ خودِ گزینه‌ی زده‌شده باز می‌شه (ThemeReveal.kt).
    val themeReveal = LocalThemeReveal.current
    val chipCenters = remember { mutableStateMapOf<ThemeMode, Offset>() }
    val themeToggleScope = rememberCoroutineScope()

    SettingsHero(
        Icons.Filled.Palette,
        "ظاهرِ برنامه",
        "پوسته، اندازه‌ی متن و حرکت",
        badge = themeModeOptions.firstOrNull { it.first == themeMode }?.second,
    )
    // ── سه‌حالتیِ روشن · تیره · سیستم ─────────────────────────────────────────
    SettingsGroupLabel("پوسته")
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            themeModeOptions.forEach { (mode, label) ->
                val selected = themeMode == mode
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) AppPrimary else AppSurface2)
                        .pressScaleClickable {
                            if (mode != themeMode && !themeReveal.inProgress) {
                                val origin = chipCenters[mode] ?: Offset.Zero
                                themeToggleScope.launch {
                                    themeReveal.startReveal(origin = origin, currentKey = themeMode)
                                    themeViewModel.setThemeMode(mode)
                                }
                            } else {
                                themeViewModel.setThemeMode(mode)
                            }
                        }
                        .onGloballyPositioned { chipCenters[mode] = it.boundsInRoot().center },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        when (mode) {
                            ThemeMode.LIGHT -> Icons.Filled.LightMode
                            ThemeMode.DARK -> Icons.Filled.DarkMode
                            ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                        },
                        contentDescription = null,
                        tint = if (selected) Color.White else AppMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        label,
                        color = if (selected) Color.White else AppMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
    // خطِ خبری - **فقط** تو حالتِ سیستم دیده می‌شه.
    if (themeMode == ThemeMode.SYSTEM) {
        Text(
            if (isDark) "الان تیره است، چون گوشی‌ات تیره است." else "الان روشن است، چون گوشی‌ات روشن است.",
            color = AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp, end = 4.dp),
        )
    }

    // ── گروهِ خواندن ──────────────────────────────────────────────────────────
    SettingsGroupLabel("خوانایی")
    SettingsGroup {
        SettingsRowItem(
            title = "اندازه‌ی متن",
            icon = Icons.Filled.FormatSize,
            tone = SettingsTone.PURPLE,
            status = fontSizeOptions.firstOrNull { it.first == fontScale }?.second ?: "معمولی",
            onClick = null,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            fontSizeOptions.forEach { (scale, label) ->
                AppChip(
                    label = label,
                    selected = fontScale == scale,
                    onClick = { themeViewModel.setFontScale(scale) },
                )
            }
        }
        // پیش‌نمایشِ زنده - اندازه‌ی انتخابی همین حالا روی کلِ برنامه نشسته، پس همین متن نمونه‌اش است.
        Text(
            "نمونه: امروز ۲۵۰٬۰۰۰ ${ir.sadteam.loancalc.ui.jibak.unitFa()} خرجِ خوراک ثبت شد.",
            color = AppText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurface2)
                .padding(12.dp),
        )
    }

    // ── واحدِ مبلغ (۱۶ مهر): تومان یا ریال ───────────────────────────────────────
    SettingsGroupLabel("واحدِ مبلغ")
    SettingsGroup {
        val unitContext = androidx.compose.ui.platform.LocalContext.current
        SettingsRowItem(
            title = "نمایشِ مبلغ‌ها",
            icon = androidx.compose.material.icons.Icons.Filled.Paid,
            tone = SettingsTone.GREEN,
            status = if (ir.sadteam.loancalc.ui.jibak.MoneyUnit.rial) "ریال (دقیقِ بانک)" else "تومان",
            onClick = null,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            AppChip(
                label = "تومان",
                selected = !ir.sadteam.loancalc.ui.jibak.MoneyUnit.rial,
                onClick = { ir.sadteam.loancalc.ui.jibak.MoneyUnit.set(unitContext, false) },
            )
            AppChip(
                label = "ریال",
                selected = ir.sadteam.loancalc.ui.jibak.MoneyUnit.rial,
                onClick = { ir.sadteam.loancalc.ui.jibak.MoneyUnit.set(unitContext, true) },
            )
        }
        Text(
            "با «ریال» همه‌ی مبلغ‌ها دقیقاً مثلِ بانک نوشته می‌شوند؛ حروفِ زیرِ کادرِ ورودی همچنان تومان می‌ماند.",
            color = AppMuted,
            fontSize = 10.5.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        )
    }

    // ── گروهِ حرکت ────────────────────────────────────────────────────────────
    SettingsGroupLabel("حرکت")
    SettingsGroup {
        SettingsRowItem(
            title = "انیمیشنِ کم",
            icon = Icons.Filled.Animation,
            tone = SettingsTone.NEUTRAL,
            status = "برای گوشی‌های کم‌قدرت",
            checked = reducedMotion,
            onCheckedChange = { themeViewModel.setReducedMotion(it) },
        )
        SettingsDivider()
        SettingsRowItem(
            title = "لرزشِ لمسی",
            icon = Icons.Filled.Vibration,
            tone = SettingsTone.PURPLE,
            status = "موقعِ لمسِ دکمه‌ها یه لرزشِ کوتاه",
            checked = vibrationEnabled,
            onCheckedChange = { hapticsViewModel.setEnabled(it) },
        )
    }
}
