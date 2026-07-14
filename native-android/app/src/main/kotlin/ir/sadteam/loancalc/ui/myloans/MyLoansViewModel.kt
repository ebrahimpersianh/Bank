package ir.sadteam.loancalc.ui.myloans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * اولین ViewModel واقعی این پروژه — الگویی که تب‌های فاز ۱ (پرداخت/ویرایش قسط، سینک ابری و ...)
 * روش سوار می‌شن. فعلاً فقط لیست محلی Room رو نشون می‌ده، بدون منطق ذخیره/سینک (فاز ۱).
 */
@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
) : ViewModel() {
    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
