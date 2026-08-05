package ir.sadteam.loancalc.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authPrefs: AuthPrefs,
) : ViewModel() {
    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<AccountTransactionEntity>> = accountRepository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = accountRepository.observeBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringPayments: StateFlow<List<RecurringPaymentEntity>> = accountRepository.observeRecurringPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** پورت syncIfLoggedIn تو MyLoansViewModel - حساب‌ها/تراکنش‌ها قبلاً فقط با AutoBackupWorkerِ
     * روزانه سینک می‌شدن، نه بعد از هر تغییر؛ کاربر خواسته با کوچیک‌ترین تغییری هم بی‌صدا آنلاین
     * بکاپ بگیره، دقیقاً مثل وام‌ها. */
    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) accountRepository.pushToServer(token)
    }

    fun balanceOf(account: AccountEntity, allTransactions: List<AccountTransactionEntity>): Double =
        accountRepository.currentBalance(account, allTransactions)

    fun addAccount(name: String, bankName: String, initialBalance: Double, cardNumber: String? = null) {
        viewModelScope.launch {
            accountRepository.addAccount(name, bankName, initialBalance, cardNumber)
            syncIfLoggedIn()
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
            syncIfLoggedIn()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
            syncIfLoggedIn()
        }
    }

    fun addTransaction(
        accountId: Long,
        type: TransactionType,
        amount: Double,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        category: String? = null,
    ) {
        viewModelScope.launch {
            accountRepository.addTransaction(accountId, type, amount, description, year, month, day, category)
            syncIfLoggedIn()
        }
    }

    fun deleteTransaction(transaction: AccountTransactionEntity) {
        viewModelScope.launch {
            accountRepository.deleteTransaction(transaction)
            syncIfLoggedIn()
        }
    }

    /** جمعِ درآمد/هزینه‌ی یه ماهِ خاص، رو همه‌ی حساب‌ها - برای کارتِ گزارشِ ماهانه. */
    fun monthlyTotals(allTransactions: List<AccountTransactionEntity>, year: Int, month: Int): Pair<Double, Double> {
        val forMonth = allTransactions.filter { it.year == year && it.month == month }
        val income = forMonth.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount }
        val expense = forMonth.filter { it.type == TransactionType.WITHDRAWAL.name }.sumOf { it.amount }
        return income to expense
    }

    fun spendByCategory(allTransactions: List<AccountTransactionEntity>, year: Int, month: Int): Map<String, Double> =
        accountRepository.spendByCategory(allTransactions, year, month)

    fun setBudget(categoryName: String, monthlyCap: Double, existingId: Long? = null) {
        viewModelScope.launch {
            accountRepository.setBudget(categoryName, monthlyCap, existingId)
            syncIfLoggedIn()
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            accountRepository.deleteBudget(budget)
            syncIfLoggedIn()
        }
    }

    fun addRecurringPayment(
        name: String,
        amount: Double,
        type: TransactionType,
        categoryName: String?,
        accountId: Long?,
        dayOfMonth: Int,
        reminderDayOffsets: String?,
    ) {
        viewModelScope.launch {
            accountRepository.addRecurringPayment(name, amount, type, categoryName, accountId, dayOfMonth, reminderDayOffsets)
            syncIfLoggedIn()
        }
    }

    fun deleteRecurringPayment(payment: RecurringPaymentEntity) {
        viewModelScope.launch {
            accountRepository.deleteRecurringPayment(payment)
            syncIfLoggedIn()
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(accountRepository.exportBackupJson()) }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = accountRepository.importBackupJson(json)
            if (ok) syncIfLoggedIn()
            onResult(ok)
        }
    }
}
