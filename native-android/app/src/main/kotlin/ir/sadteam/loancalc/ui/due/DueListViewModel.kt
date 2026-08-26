package ir.sadteam.loancalc.ui.due

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * داده‌ی تبِ **سررسید** طبقِ فریمِ `3a` - سه تبِ «اقساط / چک / طلب‌وبدهی» و داخلِ هرکدوم سه
 * گروهِ **عقب‌افتاده / این هفته / پرداخت‌شده**.
 *
 * ⚠️ `LoanRepository.getRows()` **suspend**ه - قاعده‌ی ماندگارِ پروژه می‌گه هیچ‌وقت تو
 * `remember{}` صداش نزن. برای همین کلِ محاسبه اینجا تو ViewModel می‌شینه.
 */
@HiltViewModel
class DueListViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val debtRepository: DebtRepository,
) : ViewModel() {

    /** یه ردیفِ سررسید، مستقل از اینکه قسطه یا چک یا بدهی. */
    data class DueRow(
        val id: String,
        val title: String,
        val amount: Double,
        /** منفی = هنوز نرسیده (این‌قدر روز مونده) · صفر = امروز · مثبت = این‌قدر روز عقب. */
        val daysOverdue: Int,
        val date: PersianDate,
        val paid: Boolean,
        /** فقط برای قسط - برای دکمه‌ی «پرداخت کن». */
        val loan: LoanEntity? = null,
        val installmentNumber: Int = 0,
    )

    data class DueBuckets(
        val overdue: List<DueRow> = emptyList(),
        val thisWeek: List<DueRow> = emptyList(),
        val paid: List<DueRow> = emptyList(),
    ) {
        val pendingCount: Int get() = overdue.size + thisWeek.size
        val pendingAmount: Double get() = (overdue + thisWeek).sumOf { it.amount }
    }

    private val _installments = MutableStateFlow(DueBuckets())
    val installments: StateFlow<DueBuckets> = _installments.asStateFlow()

    private val _cheques = MutableStateFlow(DueBuckets())
    val cheques: StateFlow<DueBuckets> = _cheques.asStateFlow()

    private val _debts = MutableStateFlow(DueBuckets())
    val debts: StateFlow<DueBuckets> = _debts.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val today = JalaliCalendar.today()
            _installments.value = bucket(loadInstallments(today))
            _cheques.value = bucket(loadCheques(today))
            _debts.value = bucket(loadDebts(today))
        }
    }

    /** پرداختِ یه قسط از همین صفحه - دکمه‌ی «پرداخت کن»ِ ردیفِ عقب‌افتاده. */
    fun markPaid(row: DueRow) {
        val loan = row.loan ?: return
        viewModelScope.launch {
            loanRepository.setRowPaidOnTime(loan, row.installmentNumber)
            refresh()
        }
    }

    private suspend fun loadInstallments(today: PersianDate): List<DueRow> = buildList {
        for (loan in loanRepository.getLoans()) {
            for (row in loanRepository.getRows(loan)) {
                @Suppress("UNCHECKED_CAST")
                val due = row["dueDate"] as? Map<String, Int> ?: continue
                val y = due["y"] ?: continue
                val m = due["m"] ?: continue
                val d = due["d"] ?: continue
                val number = (row["m"] as? Number)?.toInt() ?: continue
                add(
                    DueRow(
                        id = "loan-${loan.id}-$number",
                        title = loan.name,
                        amount = (row["installment"] as? Number)?.toDouble() ?: 0.0,
                        daysOverdue = JalaliCalendar.daysBetween(PersianDate(y, m, d), today),
                        date = PersianDate(y, m, d),
                        paid = row["paid"] == true,
                        loan = loan,
                        installmentNumber = number,
                    ),
                )
            }
        }
    }

    private suspend fun loadCheques(today: PersianDate): List<DueRow> =
        chequeRepository.getAllCheques().filter { !it.archived }.map { cheque ->
            val date = PersianDate(cheque.dueYear, cheque.dueMonth, cheque.dueDay)
            DueRow(
                id = "cheque-${cheque.id}",
                title = "چکِ ${cheque.bankName}",
                amount = cheque.amount,
                daysOverdue = JalaliCalendar.daysBetween(date, today),
                date = date,
                paid = cheque.status != "PENDING",
            )
        }

    private suspend fun loadDebts(today: PersianDate): List<DueRow> =
        debtRepository.observeDebts().first().map { debt ->
            val date = PersianDate(debt.year, debt.month, debt.day)
            DueRow(
                id = "debt-${debt.id}",
                title = debt.description.ifBlank { "طلب و بدهی" },
                amount = debt.amount,
                daysOverdue = JalaliCalendar.daysBetween(date, today),
                date = date,
                paid = debt.settled,
            )
        }

    /**
     * تقسیم به سه گروهِ فریم.
     *
     * «این هفته» یعنی سررسیدش **جلوتره ولی تا هفت روزِ دیگه**. سررسیدهای دورتر عمداً نمیان -
     * فریم فقط همین سه گروه رو داره و لیستِ بی‌انتها به‌درد نمی‌خوره.
     */
    private fun bucket(rows: List<DueRow>) = DueBuckets(
        overdue = rows.filter { !it.paid && it.daysOverdue >= 0 }.sortedByDescending { it.daysOverdue },
        thisWeek = rows.filter { !it.paid && it.daysOverdue in -7 until 0 }.sortedByDescending { it.daysOverdue },
        paid = rows.filter { it.paid }.sortedByDescending { it.daysOverdue }.take(10),
    )
}
