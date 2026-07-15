package ir.sadteam.loancalc.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<AccountTransactionEntity>> = accountRepository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun balanceOf(account: AccountEntity, allTransactions: List<AccountTransactionEntity>): Double =
        accountRepository.currentBalance(account, allTransactions)

    fun addAccount(name: String, bankName: String, initialBalance: Double) {
        viewModelScope.launch { accountRepository.addAccount(name, bankName, initialBalance) }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch { accountRepository.updateAccount(account) }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { accountRepository.deleteAccount(account) }
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
        }
    }

    fun deleteTransaction(transaction: AccountTransactionEntity) {
        viewModelScope.launch { accountRepository.deleteTransaction(transaction) }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(accountRepository.exportBackupJson()) }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(accountRepository.importBackupJson(json)) }
    }
}
