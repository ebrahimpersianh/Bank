package ir.sadteam.loancalc.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.CalculationHistoryRepository
import ir.sadteam.loancalc.data.db.CalculationHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * تاریخچه‌ی سبک از هر محاسبه‌ای که تو محاسبه‌گرهای اپ (وام بانکی، سقف وام، سود سپرده) انجام می‌شه -
 * [log] از هر سه صفحه‌ی محاسبه صدا زده می‌شه، بدون اینکه کاربر صریحاً «ذخیره» بزنه.
 */
@HiltViewModel
class CalculationHistoryViewModel @Inject constructor(
    private val repository: CalculationHistoryRepository,
) : ViewModel() {
    val history: StateFlow<List<CalculationHistoryEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun log(kind: String, title: String, summary: String, amount: Double) {
        viewModelScope.launch { repository.log(kind, title, summary, amount) }
    }

    fun delete(entry: CalculationHistoryEntity) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
