package ir.sadteam.loancalc.ui.home

import ir.sadteam.loancalc.core.toFa
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
    private val accountRepository: ir.sadteam.loancalc.data.AccountRepository,
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

    /** جمعِ قسط‌های پرداخت‌نشده‌ی ۷ روزِ آینده (ریال) - برای پیشنهادِ «هفته‌ی بعد پولت کم میاد». */
    private val _upcoming7d = MutableStateFlow(0.0)
    val upcoming7d: StateFlow<Double> = _upcoming7d.asStateFlow()

    init {
        // زنده: هر پرداخت/ویرایشِ وام (paidCount روی خودِ ردیفِ وام هم نوشته می‌شود) کارت را تازه می‌کند.
        viewModelScope.launch {
            loanRepository.observeLoans().collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val today = JalaliCalendar.today()
            var best: UrgentRow? = null
            var upcoming = 0.0

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
                    val amount = (row["installment"] as? Number)?.toDouble() ?: continue
                    if (overdue in -7..-1) upcoming += amount
                    if (overdue < 0) continue // هنوز نرسیده - فوری نیست
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
            _upcoming7d.value = upcoming
        }
    }

    /** «پرداخت شد» - قسط رو سرِ وقت تسویه می‌کنه و کارت رو تازه می‌کنه. */
    fun markPaid(row: UrgentRow, accountId: Long?) {
        ir.sadteam.loancalc.data.UsageStats.action("home_mark_paid")
        viewModelScope.launch {
            loanRepository.setRowPaidOnTime(row.loan, row.installmentNumber)
            // همان تراکنشی که صفحه‌ی وام می‌سازد - تا پرداخت از کارتِ خانه هم از حساب کم شود.
            accountRepository.recordLinkedPayment(
                accountId = accountId,
                sourceType = "loan",
                sourceId = "${row.loan.id}:${row.installmentNumber}",
                amount = row.amount,
                description = "قسط ${toFa(row.installmentNumber)} - ${row.loan.name}",
            )
            refresh()
        }
    }
}
