package ir.sadteam.loancalc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * قسطِ **فوریِ** بالای تبِ خانه - کارتِ فوریِ `15a`ی فایلِ طراحی («قسطِ وامِ خودرو / امروز
 * سررسید — ۳٬۵۰۰٬۰۰۰ / [پرداخت شد]»).
 *
 * فوری = نزدیک‌ترین قسطِ **پرداخت‌نشده‌ای** که سررسیدش امروز یا گذشته‌ست. اگه چند تا باشن،
 * عقب‌افتاده‌ترین اول میاد.
 *
 * ⚠️ `LoanRepository.getRows()` یه تابعِ **suspend**ه - هیچ‌وقت مستقیم تو `remember{}` صداش نزن
 * (کرشِ تردِ اصلی، قاعده‌ی ماندگارِ پروژه). برای همین اینجا تو یه ViewModel نشسته و نتیجه‌ش رو
 * از طریقِ [urgent] به‌صورتِ StateFlow می‌ده.
 */
@HiltViewModel
class UrgentDueViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
) : ViewModel() {

    /** یه قسطِ سررسیدشده‌ی پرداخت‌نشده - `null` یعنی کارتِ فوری اصلاً نشون داده نمی‌شه. */
    data class UrgentRow(
        val loan: LoanEntity,
        /** شماره‌ی قسط تو همون وام. */
        val installmentNumber: Int,
        val amount: Double,
        /** صفر یعنی سررسیدش دقیقاً امروزه؛ مثبت یعنی این‌قدر روز عقب افتاده. */
        val daysOverdue: Int,
    )

    private val _urgent = MutableStateFlow<UrgentRow?>(null)
    val urgent: StateFlow<UrgentRow?> = _urgent.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val today = JalaliCalendar.today()
            var best: UrgentRow? = null

            for (loan in loanRepository.getLoans()) {
                for (row in loanRepository.getRows(loan)) {
                    if (row["paid"] == true) continue
                    @Suppress("UNCHECKED_CAST")
                    val due = row["dueDate"] as? Map<String, Int> ?: continue
                    val y = due["y"] ?: continue
                    val m = due["m"] ?: continue
                    val d = due["d"] ?: continue
                    // `daysBetween(from, to)` مثبته وقتی `to` بعدِ `from`ه؛ پس فاصله‌ی
                    // سررسید→امروز مثبت یعنی سررسید گذشته.
                    val overdue = JalaliCalendar.daysBetween(PersianDate(y, m, d), today)
                    if (overdue < 0) continue // هنوز نرسیده - فوری نیست
                    val amount = (row["installment"] as? Number)?.toDouble() ?: continue
                    val candidate = UrgentRow(
                        loan = loan,
                        installmentNumber = (row["m"] as? Number)?.toInt() ?: 0,
                        amount = amount,
                        daysOverdue = overdue,
                    )
                    // عقب‌افتاده‌ترین اول - اونی که بیشتر از همه گذشته فوری‌تره.
                    if (best == null || overdue > best!!.daysOverdue) best = candidate
                }
            }
            _urgent.value = best
        }
    }

    /** «پرداخت شد» - قسط رو سرِ وقت تسویه می‌کنه و کارت رو تازه می‌کنه. */
    fun markPaid(row: UrgentRow) {
        viewModelScope.launch {
            loanRepository.setRowPaidOnTime(row.loan, row.installmentNumber)
            refresh()
        }
    }
}
