package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.CategoryRepository
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
    categoryRepository: CategoryRepository,
) : ViewModel() {
    /** دسته‌ها برای انتخابگرِ آیکون‌دارِ شیتِ قاعده (فریمِ `29b`) - دلخواه‌ها هم. */
    val expenseCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.WITHDRAWAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.DEPOSIT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    /**
     * آزمایشِ تشخیص (فریمِ `29c`): متنِ نمونه همان مسیرِ گیرنده‌ی پیامک را می‌رود - پارسر، بعد
     * قاعده‌ها، بعد حدسِ کلیدواژه‌ای - ولی **شمارنده‌ی قاعده را بالا نمی‌برد** و چیزی ثبت نمی‌کند.
     * `null` یعنی پارسر این متن را تراکنش نشناخت.
     */
    suspend fun testSms(body: String): SmsTestResult? {
        val parsed = BankSmsParser.parse(body) ?: return null
        val isWithdrawal = parsed.type == TransactionType.WITHDRAWAL
        val rule = rules.value.firstOrNull { r ->
            body.contains(r.pattern, ignoreCase = true) &&
                (r.txType == null || (r.txType == TransactionType.WITHDRAWAL.name) == isWithdrawal)
        }
        val account = parsed.cardSuffix?.let { suffix ->
            accountRepository.observeAccounts().first().firstOrNull { it.cardNumber?.takeLast(4) == suffix }
        }
        return SmsTestResult(
            amountRial = parsed.amountRial.toLong(),
            isWithdrawal = isWithdrawal,
            cardSuffix = parsed.cardSuffix,
            accountName = account?.let { listOfNotNull(it.name, it.bankName.takeIf { b -> b.isNotBlank() && b != it.name }).joinToString(" · ") },
            category = rule?.category ?: MerchantCategoryGuesser.guess(body, isWithdrawal),
            byRule = rule?.pattern,
        )
    }
}

data class SmsTestResult(
    val amountRial: Long,
    val isWithdrawal: Boolean,
    val cardSuffix: String?,
    val accountName: String?,
    val category: String?,
    val byRule: String?,
)
