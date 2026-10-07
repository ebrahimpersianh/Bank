package ir.sadteam.loancalc.ui.debt

import kotlinx.coroutines.flow.first
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DebtEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val accountRepository: ir.sadteam.loancalc.data.AccountRepository,
) : ViewModel() {
    val accounts = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** پولی که واقعاً جابه‌جا شد (تسویه یا پرداختِ بخشی) - از/به حساب (بخشِ «اتصالِ پرداخت‌ها»). */
    fun recordMoney(
        accountId: Long, sourceId: String, amount: Double, deposit: Boolean, description: String,
        dangTotal: Double = 0.0, dangY: Int = 0, dangM: Int = 0, dangD: Int = 0,
        onNoExpense: () -> Unit = {},
    ) {
        viewModelScope.launch {
            // سهمِ دنگ: خرجِ شام را پیدا کن تا در گزارش از آن کم شود. شناسه = «dang-<سهم>@<خرج>».
            val sid = if (sourceId.startsWith("dang-")) {
                val expenseId = if (dangTotal > 0) accountRepository.findDangExpenseId(dangTotal, dangY, dangM, dangD) else 0L
                if (expenseId == 0L) onNoExpense()
                "$sourceId@$expenseId"
            } else sourceId
            accountRepository.recordLinkedPayment(accountId, sourceTypeOf(sourceId), sid, amount, description, deposit, category = "طلب و بدهی")
        }
    }

    /** واریزِ سهمِ دنگ نوعِ جدا دارد تا در گزارش نه خرج حساب شود نه درآمد. */
    private fun sourceTypeOf(sourceId: String) = if (sourceId.startsWith("dang-")) "dang" else "debt"

    fun unrecordMoney(sourceId: String) {
        viewModelScope.launch {
            if (sourceId.startsWith("dang-")) accountRepository.removeLinkedPaymentsByPrefix("dang", "$sourceId@")
            else accountRepository.removeLinkedPayment(sourceTypeOf(sourceId), sourceId)
        }
    }

    /** خرج‌های اخیر - برای «این دنگ مالِ کدام خرج است؟» (۴۰ مورد، تازه‌ترین اول). */
    val recentExpenses: StateFlow<List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>> =
        accountRepository.observeTransactions()
            .map { all ->
                all.filter {
                    it.type == "WITHDRAWAL" && it.confirmed && ir.sadteam.loancalc.data.countsInReports(it)
                }.sortedWith(compareByDescending<ir.sadteam.loancalc.data.db.AccountTransactionEntity> { it.year }
                    .thenByDescending { it.month }.thenByDescending { it.day }.thenByDescending { it.id })
                    .take(40)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counterparties: StateFlow<List<CounterpartyEntity>> = debtRepository.observeCounterparties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = debtRepository.observeDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun netBalance(counterpartyId: Long, allDebts: List<DebtEntity>): Double =
        debtRepository.netBalance(counterpartyId, allDebts)

    fun addCounterparty(name: String, phone: String? = null, onResult: (Long) -> Unit = {}) {
        ir.sadteam.loancalc.data.UsageStats.action("counterparty_added")
        viewModelScope.launch { onResult(debtRepository.addCounterparty(name, phone)) }
    }

    fun updateCounterparty(counterparty: CounterpartyEntity) {
        viewModelScope.launch { debtRepository.updateCounterparty(counterparty) }
    }

    fun deleteCounterparty(counterparty: CounterpartyEntity) {
        viewModelScope.launch {
            // بدهی‌های این طرف‌حساب هم می‌روند؛ پولی که با آن‌ها جابه‌جا شده هم برگردد.
            val debts = debtRepository.observeDebtsForCounterparty(counterparty.id).first()
            debtRepository.deleteCounterparty(counterparty)
            debts.forEach { accountRepository.removeLinkedPayment("debt", it.id.toString()) }
        }
    }

    fun addDebt(
        counterpartyId: Long,
        amount: Double,
        type: DebtType,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        onAdded: (Long) -> Unit = {},
    ) {
        ir.sadteam.loancalc.data.UsageStats.action("debt_added")
        viewModelScope.launch {
            onAdded(debtRepository.addDebt(counterpartyId, amount, type, description, year, month, day))
        }
    }

    fun setSettled(debt: DebtEntity, settled: Boolean) {
        ir.sadteam.loancalc.data.UsageStats.action("debt_settled")
        viewModelScope.launch { debtRepository.setSettled(debt, settled) }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
            // ردیفِ پاک‌شده یعنی «اتفاق نیفتاده» - پولی که با آن جابه‌جا شده هم برگردد (۹ مهر).
            accountRepository.removeLinkedPayment("debt", debt.id.toString())
        }
    }
}
