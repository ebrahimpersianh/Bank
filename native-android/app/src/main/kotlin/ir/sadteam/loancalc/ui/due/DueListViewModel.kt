package ir.sadteam.loancalc.ui.due

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.AccountRepository
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
    private val accountRepository: AccountRepository,
) : ViewModel() {

    /**
     * نوعِ تعهد. **رنگِ ردیف از اینجا نمی‌آید** - قاعده‌ی `51c`: رنگ از فوریت می‌آید و
     * تفاوتِ نوع از آیکون و بجِ سطرِ دوم.
     */
    enum class DueSource(val label: String) {
        LOAN("قسط"),
        CHEQUE("چک"),
        RECURRING("تکراری"),
        DEBT("قرض"),
    }

    /** یه ردیفِ سررسید، مستقل از اینکه قسطه یا چک یا بدهی. */
    data class DueRow(
        val id: String,
        val title: String,
        val amount: Double,
        /** منفی = هنوز نرسیده (این‌قدر روز مونده) · صفر = امروز · مثبت = این‌قدر روز عقب. */
        val daysOverdue: Int,
        val date: PersianDate,
        val paid: Boolean,
        /** فقط برای قسط. */
        val loan: LoanEntity? = null,
        val installmentNumber: Int = 0,
        /** فقط برای چک - تپِ ردیف باید جزئیاتِ همین چک رو باز کنه (تصمیمِ ۱ی README). */
        val chequeId: Long? = null,
        /** فقط برای طلب‌وبدهی. */
        val debtId: Long? = null,
        val kind: DueSource = DueSource.LOAN,
        /** سطرِ دومِ ردیف - «قسطِ ۷ از ۶۰» / «بانکِ ملت» / «پرداختِ تکراری». */
        val subtitle: String = "",
    )

    data class DueBuckets(
        val overdue: List<DueRow> = emptyList(),
        val thisWeek: List<DueRow> = emptyList(),
        val paid: List<DueRow> = emptyList(),
        /**
         * شمارشِ **کلِ** ردیف‌های پرداخت‌شده، بی سقفِ نمایش.
         *
         * `paid` عمداً `take(10)` داره تا لیست بی‌انتها نشه، ولی حلقه‌ی پیشرفتِ هیرو
         * باید از عددِ واقعی حساب بشه - وگرنه کاربرِ ۴۰ قسطِ تمام‌شده ۸۳٪ می‌بینه نه ۹۵٪.
         */
        val paidTotalCount: Int = 0,
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

    /**
     * **فهرستِ یکپارچه‌ی بخشِ ۵۱** - سه منبعِ بدهی در یک لیست، گروه‌بندی روی **فوریت** نه
     * روی نوعِ تعهد: کاربر اولِ ماه یک سوال دارد نه سه.
     *
     * ⚠️ فقط **بدهی**: چکِ دریافتی و طلبِ کاربر از دیگران نمی‌آیند، وگرنه هیرویِ
     * «باید بدهی» غلط می‌شود.
     */
    private val _all = MutableStateFlow(DueList())
    val all: StateFlow<DueList> = _all.asStateFlow()

    data class DueList(
        val overdue: List<DueRow> = emptyList(),
        val thisWeek: List<DueRow> = emptyList(),
        val later: List<DueRow> = emptyList(),
    ) {
        val isEmpty: Boolean get() = overdue.isEmpty() && thisWeek.isEmpty() && later.isEmpty()
        val upcomingCount: Int get() = thisWeek.size + later.size

        /**
         * عددِ هیرو: جمعِ **تا آخرِ ماهِ جاری**. عقب‌افتاده در این جمع می‌آید چون هنوز
         * پرداخت‌نشده و همین ماه باید داده شود.
         */
        fun monthTotal(today: PersianDate): Double =
            (overdue + thisWeek + later)
                .filter { it.date.y < today.y || (it.date.y == today.y && it.date.m <= today.m) }
                .sumOf { it.amount }
    }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val today = JalaliCalendar.today()
            _installments.value = bucket(loadInstallments(today))
            _cheques.value = bucket(loadCheques(today))
            val debtRows = loadDebts(today)
            _debts.value = bucket(debtRows)
            // فقط چیزی که کاربر **باید بدهد**: قسط، چکِ پرداختنی، پرداختِ تکراری، و قرضی
            // که خودش گرفته.
            val owed = debtRows.filter { it.title.isNotBlank() }
            val unified = (loadInstallments(today) + loadCheques(today) + loadRecurring(today) + owed)
                .filter { !it.paid }
            _all.value = DueList(
                // مرزها روزِ شمسی‌اند نه ۲۴ ساعت: «امروز» یعنی `daysOverdue == 0` و در
                // گروهِ «همین هفته» می‌نشیند، نه در عقب‌افتاده.
                overdue = unified.filter { it.daysOverdue > 0 }.sortedByDescending { it.daysOverdue },
                thisWeek = unified.filter { it.daysOverdue in -7..0 }.sortedByDescending { it.daysOverdue },
                later = unified.filter { it.daysOverdue < -7 }.sortedByDescending { it.daysOverdue },
            )
        }
    }

    // 🚨 `markPaid` حذف شد. تنها مصرف‌کننده‌اش دکمه‌ی «پرداخت کن»ِ ردیفِ عقب‌افتاده بود که
    // تصمیمِ ۲ی README و فریمِ `36i` را نقض می‌کرد. پرداخت از جزئیاتِ قسط و از اعلان
    // انجام می‌شه، هر دو با تاریخِ قابلِ اصلاح.

    private suspend fun loadInstallments(today: PersianDate): List<DueRow> = buildList {
        for (loan in loanRepository.getLoans()) {
            // ⚠️ یک‌بار خوانده می‌شود، نه داخلِ حلقه - `getRows` می‌رود سراغِ دیتابیس.
            val loanRows = loanRepository.getRows(loan)
            for (row in loanRows) {
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
                        kind = DueSource.LOAN,
                        subtitle = "قسطِ ${number} از ${loanRows.size}",
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
                // 🚨 همان باگی که طراح در تقویم گرفت، این‌جا هم بود (بخشِ ۷۳):
                // `status != "PENDING"` چکِ **برگشتی** را پرداخت‌شده حساب می‌کرد، پس چکِ
                // برگشتی از گروهِ «عقب‌افتاده» بیرون می‌افتاد و اصلاً دیده نمی‌شد - بدترین
                // خبرِ ممکن، غایب. فقط `PASSED` پرداخت‌شده است.
                // `when` نوشته شده نه `==` تا اگر وضعیتِ تازه‌ای اضافه شد جایش پیدا باشد.
                paid = when (cheque.status) {
                    "PASSED" -> true
                    "BOUNCED" -> false
                    else -> false
                },
                chequeId = cheque.id,
                kind = DueSource.CHEQUE,
                subtitle = cheque.bankName,
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
                debtId = debt.id,
                kind = DueSource.DEBT,
                subtitle = "قرض",
            )
        }

    /**
     * پرداختِ تکراری سررسیدِ ذخیره‌شده ندارد، فقط **روزِ ماه**. سررسیدِ پیشِ‌رو همان روز در
     * ماهِ جاری است، و اگر گذشته باشد ماهِ بعد - پس این ردیف هیچ‌وقت «عقب‌افتاده» نمی‌شود
     * (نمی‌دانیم پرداخت شده یا نه، و قرمزکردنش قرمزهای واقعی را ارزان می‌کند).
     */
    private suspend fun loadRecurring(today: PersianDate): List<DueRow> =
        accountRepository.observeRecurringPayments().first()
            .filter { it.type == "WITHDRAWAL" }
            .map { payment ->
                val day = payment.dayOfMonth.coerceIn(1, JalaliCalendar.daysInMonth(today.y, today.m))
                val thisMonth = PersianDate(today.y, today.m, day)
                val date = if (day >= today.d) {
                    thisMonth
                } else {
                    val nextMonth = if (today.m == 12) PersianDate(today.y + 1, 1, 1) else PersianDate(today.y, today.m + 1, 1)
                    PersianDate(
                        nextMonth.y,
                        nextMonth.m,
                        payment.dayOfMonth.coerceIn(1, JalaliCalendar.daysInMonth(nextMonth.y, nextMonth.m)),
                    )
                }
                DueRow(
                    id = "recurring-${payment.id}",
                    title = payment.name,
                    amount = payment.amount,
                    daysOverdue = JalaliCalendar.daysBetween(date, today),
                    date = date,
                    paid = false,
                    kind = DueSource.RECURRING,
                    subtitle = "پرداختِ تکراری",
                )
            }

    /**
     * تقسیم به سه گروهِ فریم.
     *
     * «این هفته» یعنی سررسیدش **جلوتره ولی تا هفت روزِ دیگه**. سررسیدهای دورتر عمداً نمیان -
     * فریم فقط همین سه گروه رو داره و لیستِ بی‌انتها به‌درد نمی‌خوره.
     */
    private fun bucket(rows: List<DueRow>): DueBuckets {
        val paidRows = rows.filter { it.paid }.sortedByDescending { it.daysOverdue }
        return DueBuckets(
            overdue = rows.filter { !it.paid && it.daysOverdue >= 0 }.sortedByDescending { it.daysOverdue },
            thisWeek = rows.filter { !it.paid && it.daysOverdue in -7 until 0 }.sortedByDescending { it.daysOverdue },
            paid = paidRows.take(10),
            paidTotalCount = paidRows.size,
        )
    }
}
