package ir.sadteam.loancalc.ui.myloans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.PersianDate
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
        startDate: PersianDate,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.addManualLoan(
                name = name,
                bank = bank,
                installment = installment,
                n = n,
                paidCount = paidCount,
                startDate = mapOf("y" to startDate.y, "m" to startDate.m, "d" to startDate.d),
            )
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

    /** پورت rows[].paid تو www/index.html - وضعیت پرداخت هر قسط مستقله، نه یه آستانه‌ی ترتیبی. */
    fun getRows(loan: LoanEntity): List<Map<String, Any?>> = loanRepository.getRows(loan)

    fun setRowPaid(loan: LoanEntity, m: Int, paid: Boolean) {
        viewModelScope.launch {
            loanRepository.setRowPaid(loan, m, paid)
            syncIfLoggedIn()
        }
    }

    /** پورت confirmEditInstallment تو www/index.html - ویرایش دستی مبلغ یه قسط. */
    fun setRowInstallment(loan: LoanEntity, m: Int, newAmount: Double) {
        viewModelScope.launch {
            loanRepository.setRowInstallment(loan, m, newAmount)
            syncIfLoggedIn()
        }
    }

    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) loanRepository.pushToServer(token)
    }
}
