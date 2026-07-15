package ir.sadteam.loancalc.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DueItem(val loanName: String, val bank: String, val installment: Double, val paid: Boolean)

/** پورت مفهومی «تقویم مالی» اپ رقیب (VAMMAN) - سررسید همه‌ی اقساط همه‌ی وام‌ها رو رو یه گرید تقویم
 * شمسی نشون می‌ده. [getRows] (تو LoanRepository) خودش سررسید هر قسط رو از رو startDate+intervalDays
 * محاسبه می‌کنه، همون منطقی که LoanDetailScreen هم استفاده می‌کنه - اینجا فقط دوباره برای همه‌ی
 * وام‌ها جمع می‌شه، منطق محاسبه‌ی سررسید تکرار/تغییر نمی‌کنه. */
@HiltViewModel
class FinancialCalendarViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
) : ViewModel() {
    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun dueItemsByDate(loans: List<LoanEntity>): Map<PersianDate, List<DueItem>> {
        val map = mutableMapOf<PersianDate, MutableList<DueItem>>()
        for (loan in loans) {
            for (row in loanRepository.getRows(loan)) {
                val due = row["dueDate"] as? Map<*, *> ?: continue
                val y = (due["y"] as? Number)?.toInt() ?: continue
                val m = (due["m"] as? Number)?.toInt() ?: continue
                val d = (due["d"] as? Number)?.toInt() ?: continue
                val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
                val paid = row["paid"] == true
                map.getOrPut(PersianDate(y, m, d)) { mutableListOf() }
                    .add(DueItem(loan.name, loan.bank, installment, paid))
            }
        }
        return map
    }
}
