package ir.sadteam.loancalc.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.BadgeEvaluator
import ir.sadteam.loancalc.data.SavingsGoalRepository
import ir.sadteam.loancalc.data.db.SavingsGoalEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val repository: SavingsGoalRepository,
    private val badgeEvaluator: BadgeEvaluator,
    private val accountRepository: ir.sadteam.loancalc.data.AccountRepository,
) : ViewModel() {
    val accounts = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** پولِ واریز به هدف (یا برداشت از آن) را در حساب هم ثبت می‌کند؛ [goalId]+[stamp] یکتاست. */
    fun recordGoalMoney(accountId: Long, goalId: Long, goalTitle: String, deltaRial: Double) {
        viewModelScope.launch {
            accountRepository.recordLinkedPayment(
                accountId = accountId,
                sourceType = "goal",
                sourceId = "$goalId:${System.currentTimeMillis()}",
                amount = kotlin.math.abs(deltaRial),
                description = if (deltaRial >= 0) "واریز به هدف «$goalTitle»" else "برداشت از هدف «$goalTitle»",
                deposit = deltaRial < 0,
                category = "پس‌انداز",
            )
        }
    }

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * هدفی که **همین الان** کامل شد - برای جشن. یک‌بار مصرف است و بعدِ نمایش
     * [consumeJustReached] صفرش می‌کند، وگرنه با هر چرخشِ صفحه دوباره جشن می‌گیرد.
     */
    private val _justReached = MutableStateFlow<SavingsGoalEntity?>(null)
    val justReached: StateFlow<SavingsGoalEntity?> = _justReached

    fun consumeJustReached() {
        _justReached.value = null
    }

    fun addGoal(
        title: String,
        targetRial: Double,
        iconKey: String,
        deadlineYear: Int? = null,
        deadlineMonth: Int? = null,
        deadlineDay: Int? = null,
    ) {
        ir.sadteam.loancalc.data.UsageStats.action("goal_added")
        viewModelScope.launch {
            repository.addGoal(title, targetRial, iconKey, deadlineYear, deadlineMonth, deadlineDay)
        }
    }

    fun updateGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.updateGoal(goal) }
    }

    /**
     * واریز/برداشتِ هدف. اگر با همین حرکت هدف کامل شد، **همان‌جا** نشانِ `goal_reached`
     * سنجیده می‌شود - نشانی که تا رفتنِ کاربر به صفحه‌ی نشان‌ها صبر کند، جشنش را از
     * لحظه‌ی درستش جدا کرده.
     */
    fun contribute(goalId: Long, deltaRial: Double) {
        viewModelScope.launch {
            val before = repository.getGoals().firstOrNull { it.id == goalId }
            val after = repository.contribute(goalId, deltaRial) ?: return@launch
            if (after.reached && before?.reached != true) {
                _justReached.value = after
                // ⚠️ `silent = true` عمدی است (ایرادِ ۲ی طراح): خودِ همین صفحه جشن را
                // نشان می‌دهد، پس جشنِ دومِ `BadgeEvaluator` یعنی دو تا با هم.
                // سکه‌ی نشان همچنان ریخته می‌شود؛ فقط گونه‌ی ردیف `badge_retro` است.
                badgeEvaluator.evaluate(silent = true)
            }
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }
}
