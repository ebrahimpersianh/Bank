package ir.sadteam.loancalc.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
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

    /** پورت syncIfLoggedIn تو MyLoansViewModel - حساب‌ها/تراکنش‌ها قبلاً فقط با AutoBackupWorkerِ
     * روزانه سینک می‌شدن، نه بعد از هر تغییر؛ کاربر خواسته با کوچیک‌ترین تغییری هم بی‌صدا آنلاین
     * بکاپ بگیره، دقیقاً مثل وام‌ها. */
    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) accountRepository.pushToServer(token)
    }

    fun balanceOf(account: AccountEntity, allTransactions: List<AccountTransactionEntity>): Double =
        accountRepository.currentBalance(account, allTransactions)

    fun addAccount(name: String, bankName: String, initialBalance: Double) {
        viewModelScope.launch {
            accountRepository.addAccount(name, bankName, initialBalance)
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
    ) {
        viewModelScope.launch {
            accountRepository.addTransaction(accountId, type, amount, description, year, month, day)
            syncIfLoggedIn()
        }
    }

    fun deleteTransaction(transaction: AccountTransactionEntity) {
        viewModelScope.launch {
            accountRepository.deleteTransaction(transaction)
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
