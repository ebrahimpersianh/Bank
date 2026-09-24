package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.db.ParsingRuleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParsingRulesViewModel @Inject constructor(
    private val repository: ParsingRuleRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val rules: StateFlow<List<ParsingRuleEntity>> = repository.observeRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(existing: ParsingRuleEntity?, pattern: String, category: String, txType: String?, count: Int) {
        viewModelScope.launch {
            repository.save(
                existing?.copy(
                    pattern = pattern,
                    category = category,
                    txType = txType,
                    // ویرایشِ کاربر قاعده‌ی خودکار رو «دستی» می‌کنه - چیپش می‌افته.
                    auto = false,
                ) ?: ParsingRuleEntity(
                    id = System.currentTimeMillis(),
                    pattern = pattern,
                    category = category,
                    txType = txType,
                    sortOrder = count,
                ),
            )
        }
    }

    fun delete(rule: ParsingRuleEntity) {
        viewModelScope.launch { repository.delete(rule) }
    }

    /**
     * پیش‌نمایشِ زنده - رو **تراکنش‌های محلی**، نه سرور. جوابِ خطرِ «قاعده‌ی غلط ساختم و
     * یه ماه بعد فهمیدم».
     */
    suspend fun previewMatchCount(pattern: String): Int {
        val q = pattern.trim()
        if (q.isBlank()) return 0
        return accountRepository.observeTransactions().first()
            .count { it.description.contains(q, ignoreCase = true) }
    }
}
