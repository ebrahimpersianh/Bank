package ir.sadteam.loancalc.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {
    val darkTheme: StateFlow<Boolean> = uiPrefs.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleTheme() {
        viewModelScope.launch { uiPrefs.setDarkTheme(!darkTheme.value) }
    }

    /** پورت .app.fs-small/fs-medium/fs-large تو www/index.html. */
    val fontScale: StateFlow<Float> = uiPrefs.fontScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    fun setFontScale(value: Float) {
        viewModelScope.launch { uiPrefs.setFontScale(value) }
    }
}
