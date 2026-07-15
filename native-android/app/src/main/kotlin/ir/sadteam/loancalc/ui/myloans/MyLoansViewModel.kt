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

    /** پورت handlePayButton برای برگردوندن قسط به حالت پرداخت‌نشده. */
    fun setRowUnpaid(loan: LoanEntity, m: Int) {
        viewModelScope.launch {
            loanRepository.setRowUnpaid(loan, m)
            syncIfLoggedIn()
        }
    }

    /** پورت payOnTime تو www/index.html. */
    fun setRowPaidOnTime(loan: LoanEntity, m: Int) {
        viewModelScope.launch {
            loanRepository.setRowPaidOnTime(loan, m)
            syncIfLoggedIn()
        }
    }

    /** پورت confirmLatePayment تو www/index.html - [paidDate] تاریخ واقعیِ پرداخته، نه سررسید. */
    fun setRowPaidLate(loan: LoanEntity, m: Int, paidDate: PersianDate) {
        viewModelScope.launch {
            loanRepository.setRowPaidLate(
                loan,
                m,
                mapOf("y" to paidDate.y, "m" to paidDate.m, "d" to paidDate.d),
            )
            syncIfLoggedIn()
        }
    }

    /** پورت confirmEditInstallment تو www/index.html - ویرایش دستی مبلغ یه قسط. [onSaved] بعد از
     * ذخیره صدا زده می‌شه تا UI بتونه سوال «رو همه اعمال کنم؟» رو نشون بده. */
    fun setRowInstallment(loan: LoanEntity, m: Int, newAmount: Double, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            loanRepository.setRowInstallment(loan, m, newAmount)
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** پورت «می‌خوای این مبلغ رو برای همه‌ی اقساط اعمال کنی؟» تو confirmEditInstallment. */
    fun setAllRowsInstallment(loan: LoanEntity, newAmount: Double) {
        viewModelScope.launch {
            loanRepository.setAllRowsInstallment(loan, newAmount)
            syncIfLoggedIn()
        }
    }

    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) loanRepository.pushToServer(token)
    }
}
