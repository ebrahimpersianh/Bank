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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleTheme() {
        viewModelScope.launch { uiPrefs.setDarkTheme(!darkTheme.value) }
    }
}
