package ir.sadteam.loancalc.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsSummary(
    val loanCount: Int,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val totalInstallments: Int,
    val paidInstallments: Int,
) {
    val progressRatio: Double get() = if (totalAmount > 0) paidAmount / totalAmount else 0.0
}

/** پورت مفهومی «آمار و گزارشات» اپ رقیب (VAMMAN) - از رو همون فیلدهای LoanEntity که DashboardSummary
 * هم استفاده می‌کنه حساب می‌شه (installment×paidCount/installment×(n−paidCount))، نه یه منبع داده‌ی
 * جدید. */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
) : ViewModel() {
    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun summarize(loans: List<LoanEntity>): StatsSummary {
        val totalAmount = loans.sumOf { it.installment * it.n }
        val paidAmount = loans.sumOf { it.installment * it.paidCount }
        return StatsSummary(
            loanCount = loans.size,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = totalAmount - paidAmount,
            totalInstallments = loans.sumOf { it.n },
            paidInstallments = loans.sumOf { it.paidCount },
        )
    }
}
