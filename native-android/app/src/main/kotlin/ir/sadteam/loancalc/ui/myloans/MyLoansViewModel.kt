package ir.sadteam.loancalc.ui.myloans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * اولین ViewModel واقعی این پروژه — الگویی که تب‌های فاز ۱ (پرداخت/ویرایش قسط، سینک ابری و ...)
 * روش سوار می‌شن. فعلاً فقط لیست محلی Room + افزودن دستی/حذف وام رو پشتیبانی می‌کنه، بدون سینک
 * ابری واقعی و بدون محدودیت اشتراک (اون‌ها فاز بعد، وقتی ورود OTP هم پورت بشه).
 */
@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
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
            onSaved()
        }
    }

    fun deleteLoan(id: Long) {
        viewModelScope.launch { loanRepository.deleteLoan(id) }
    }
}
