package ir.sadteam.loancalc.ui.myloans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * لیست محلی Room + افزودن دستی/حذف/پرداخت قسط رو پشتیبانی می‌کنه. هر تغییر، اگه لاگین باشیم،
 * بی‌صدا به سرور هم پوش می‌شه - پورت persistLoans() تو www/index.html (که همیشه بعد از هر تغییر
 * محلی syncLoansToServer رو صدا می‌زنه).
 */
@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val authPrefs: AuthPrefs,
) : ViewModel() {
    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveManualLoan(
        name: String,
        bank: String,
        installment: Double,
        n: Int,
        paidCount: Int,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.addManualLoan(name, bank, installment, n, paidCount)
            syncIfLoggedIn()
            onSaved()
        }
    }

    fun deleteLoan(id: Long) {
        viewModelScope.launch {
            loanRepository.deleteLoan(id)
            syncIfLoggedIn()
        }
    }

    fun setPaidCount(loan: LoanEntity, paidCount: Int) {
        val clamped = paidCount.coerceIn(0, loan.n)
        viewModelScope.launch {
            loanRepository.setPaidCount(loan, clamped)
            syncIfLoggedIn()
        }
    }

    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) loanRepository.pushToServer(token)
    }
}
