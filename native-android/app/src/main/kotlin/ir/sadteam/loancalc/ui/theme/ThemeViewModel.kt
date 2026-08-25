package ir.sadteam.loancalc.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
