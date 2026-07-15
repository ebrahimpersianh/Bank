package ir.sadteam.loancalc.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
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

data class PaymentHistoryPoint(val label: String, val cumulativeAmount: Double)

private val statsMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

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

    /** پورت نمودار خطی «تاریخچه پرداخت» اپ رقیب (VAMMAN) - مجموع تجمعی اقساط پرداخت‌شده به‌ازای هر
     * ماه شمسی (بر اساس paidDate واقعی اگه با تاخیر پرداخت شده، وگرنه dueDate)، از رو همون rows که
     * LoanDetailScreen/getRows قبلاً برای وضعیت پرداخت هر قسط استفاده می‌کنه - نه یه منبع داده‌ی جدید. */
    fun paymentHistory(loans: List<LoanEntity>): List<PaymentHistoryPoint> {
        val monthlyTotals = sortedMapOf<Int, Double>()
        loans.forEach { loan ->
            loanRepository.getRows(loan).forEach { row ->
                if (row["paid"] != true) return@forEach
                val date = (row["paidDate"] as? Map<*, *>) ?: (row["dueDate"] as? Map<*, *>) ?: return@forEach
                val y = (date["y"] as? Number)?.toInt() ?: return@forEach
                val m = (date["m"] as? Number)?.toInt() ?: return@forEach
                val amount = (row["installment"] as? Number)?.toDouble() ?: 0.0
                val key = y * 100 + m
                monthlyTotals[key] = (monthlyTotals[key] ?: 0.0) + amount
            }
        }
        var running = 0.0
        return monthlyTotals.map { (key, amount) ->
            running += amount
            val y = key / 100
            val m = key % 100
            PaymentHistoryPoint(label = "${statsMonthNames[m - 1]} ${toFa(y)}", cumulativeAmount = running)
        }
    }
}
