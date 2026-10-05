package ir.sadteam.loancalc.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.AdminStatsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** «آمارِ جیبک» - فقط برای حسابی که سرور `is_admin` می‌داند (رجوع کن به AdminRoutes.kt). */
@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    sealed interface State {
        data object Loading : State
        data object Failed : State
        data class Ready(val stats: AdminStatsResponse) : State
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _isAdmin.value = authRepository.isAdmin()
            if (_isAdmin.value) {
                authRepository.adminDigest("day")?.let { AdminSignals.unreadSupport.value = AdminSignals.parseUnread(it.notes) }
            }
        }
    }

    fun load() {
        _state.value = State.Loading
        viewModelScope.launch {
            _state.value = authRepository.adminStats()?.let { State.Ready(it) } ?: State.Failed
        }
    }
}
