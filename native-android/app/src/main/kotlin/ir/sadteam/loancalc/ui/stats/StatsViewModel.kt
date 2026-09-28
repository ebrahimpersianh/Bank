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

    /**
     * 🚨 **از روی خودِ ردیف‌های قسط، نه `installment × n`** (یافته‌ی بازبینی، ۳۱ شهریور).
     *
     * مبلغِ تکِ قسط‌ها دستی قابلِ ویرایش است (کارمزد یا جریمه‌ی بانک). ضربِ ساده یعنی
     * خلاصه‌ی بالای صفحه با تاریخچه‌ی همان صفحه - که از `loan_rows` می‌آید - نمی‌خوانْد و
     * کاربر دو عددِ متفاوت برای یک چیز می‌دید. حالا هر دو یک منبع دارند.
     *
     * وامی که هنوز ردیفی ندارد (وامِ تازه‌ی مهاجرت‌نشده) به همان محاسبه‌ی قدیمی برمی‌گردد.
     */
    suspend fun summarize(loans: List<LoanEntity>): StatsSummary {
        var totalAmount = 0.0
        var paidAmount = 0.0
        var totalInstallments = 0
        var paidInstallments = 0
        loans.forEach { loan ->
            val rows = loanRepository.getRows(loan)
            if (rows.isEmpty()) {
                totalAmount += loan.installment * loan.n
                paidAmount += loan.installment * loan.paidCount
                totalInstallments += loan.n
                paidInstallments += loan.paidCount
                return@forEach
            }
            rows.forEach { row ->
                val amount = (row["installment"] as? Number)?.toDouble() ?: loan.installment
                totalAmount += amount
                totalInstallments += 1
                if (row["paid"] == true) {
                    paidAmount += amount
                    paidInstallments += 1
                }
            }
        }
        return StatsSummary(
            loanCount = loans.size,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = totalAmount - paidAmount,
            totalInstallments = totalInstallments,
            paidInstallments = paidInstallments,
        )
    }

    /** پورت نمودار خطی «تاریخچه پرداخت» اپ رقیب (VAMMAN) - مجموع تجمعی اقساط پرداخت‌شده به‌ازای هر
     * ماه شمسی (بر اساس paidDate واقعی اگه با تاخیر پرداخت شده، وگرنه dueDate)، از رو همون rows که
     * LoanDetailScreen/getRows قبلاً برای وضعیت پرداخت هر قسط استفاده می‌کنه - نه یه منبع داده‌ی جدید. */
    suspend fun paymentHistory(loans: List<LoanEntity>): List<PaymentHistoryPoint> {
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
