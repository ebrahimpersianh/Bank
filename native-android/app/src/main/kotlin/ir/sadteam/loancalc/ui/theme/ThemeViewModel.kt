package ir.sadteam.loancalc.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.coin.ThemePalette
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = uiPrefs.themeMode
        .map { raw -> ThemeMode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ThemeMode.LIGHT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.LIGHT)

    /**
     * بین روشن ← تاریک ← خودکار می‌چرخه - چک اشتراکی‌بودنِ کاربر وظیفه‌ی UI (MainActivity) هست،
     * نه اینجا. («خودکار» یعنی از تنظیماتِ خودِ گوشی پیروی کن.)
     */
    /** تمِ رنگیِ فعال - فقط خانواده‌ی primary رو عوض می‌کنه (رجوع کن به [ColorTheme]). */
    val colorTheme: StateFlow<ColorTheme> = uiPrefs.colorTheme
        .map { ColorTheme.fromId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ColorTheme.GREEN)

    /** تم‌هایی که خریده شدن. سبز همیشه هست چون رایگانه. */
    val ownedThemes: StateFlow<Set<String>> = uiPrefs.ownedThemes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * تمِ کاتالوگِ فروشگاه، اگر شناسه‌ی ذخیره‌شده مالِ آن‌ها باشد؛ وگرنه `null` و
     * [colorTheme]ِ بالا تصمیم می‌گیرد.
     */
    val catalogTheme: StateFlow<ThemePalette?> = uiPrefs.colorTheme
        .map { themeById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectColorTheme(theme: ColorTheme) {
        viewModelScope.launch { uiPrefs.setColorTheme(theme.id) }
    }

    /** بعد از خریدِ موفق: هم مالکیت ثبت می‌شه هم **فوراً روشن می‌شه** (رسیدِ خرید همون رنگه). */
    fun ownAndSelect(theme: ColorTheme) {
        viewModelScope.launch {
            uiPrefs.addOwnedTheme(theme.id)
            uiPrefs.setColorTheme(theme.id)
        }
    }

    val reducedMotion: StateFlow<Boolean> = uiPrefs.reducedMotion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setReducedMotion(value: Boolean) {
        viewModelScope.launch { uiPrefs.setReducedMotion(value) }
    }

    fun cycleThemeMode() {
        val next = when (themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(next)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { uiPrefs.setThemeMode(mode.name.lowercase()) }
    }

    /** پورت .app.fs-small/fs-medium/fs-large تو www/index.html. */
    val fontScale: StateFlow<Float> = uiPrefs.fontScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    fun setFontScale(value: Float) {
        viewModelScope.launch { uiPrefs.setFontScale(value) }
    }
}
