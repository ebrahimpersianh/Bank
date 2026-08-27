package ir.sadteam.loancalc.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.AchievementEntity
import ir.sadteam.loancalc.data.db.CoinEventEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** سکه و نشانِ «فعال» - رجوع کن به [GamificationRepository] برای قاعده‌های اقتصادِ سکه. */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val repository: GamificationRepository,
) : ViewModel() {
    val coins: StateFlow<Int> = repository.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeDays: StateFlow<Int> = repository.activeDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val events: StateFlow<List<CoinEventEntity>> = repository.events
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** رویدادهای یک‌باره‌ی جدولِ `20e` - از جایی که واقعاً اتفاق می‌افتن صدا زده می‌شن. */
    /** خرجِ سکه - `refId` کلیدِ ضدِتکراره، پس دوبار زدنِ دکمه دوبار خرج نمی‌کنه. */
    fun spendCoins(amount: Int, refId: String) {
        viewModelScope.launch {
            repository.spend(amount, refId, ir.sadteam.loancalc.data.GamificationRepository.Type.SPEND_THEME)
        }
    }

    fun awardOnce(type: String, amount: Int) {
        viewModelScope.launch { repository.awardOnce(type, amount) }
    }
}
