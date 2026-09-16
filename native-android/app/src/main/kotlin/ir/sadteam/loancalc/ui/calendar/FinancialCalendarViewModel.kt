package ir.sadteam.loancalc.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** نوعِ تعهدِ یه روز. طبقِ فریمِ `51a` نوع **با آیکون** تفکیک می‌شه نه با رنگ - رنگِ نقطه‌ی
 * تقویم فقط فوریت/پرداخت‌شدن رو می‌گه، چون سه رنگ تو یه دایره‌ی ۵dp قابلِ تشخیص نیست. */
enum class DueKind { INSTALLMENT, CHEQUE, RECURRING }

data class DueItem(
    val title: String,
    val subtitle: String,
    val amount: Double,
    /**
     * `null` = **وضعیت ندارد**، نه «پرداخت‌نشده».
     *
     * 🚨 پرداختِ تکراری `paid = false` می‌گرفت، و چون تکرارهایش برای ۱۸ ماه ساخته می‌شوند،
     * هر ماهِ تقویم روی `dayOfMonth` یک نقطه‌ی **قرمز** می‌گرفت - برای همیشه، شاملِ
     * ماه‌های گذشته‌ای که کاربر اجاره‌اش را سالِ پیش داده. قرمز معنیِ ثبت‌شده دارد
     * (پرداخت‌نشده)، و `RecurringPaymentEntity` فقط الگو را نگه می‌دارد نه ثبتِ واقعی -
     * پس این جدول نمی‌تواند بداند پرداخت شده یا نه.
     */
    val paid: Boolean?,
    val kind: DueKind,
    /**
     * مقصدِ تپ (خواسته‌ی کاربر، دورِ ۹): «هرچه آن پایین هست اگر بزنم برود رویش».
     *
     * پرداختِ تکراری مقصدی ندارد - الگوست، ردیفِ مستقلی در برنامه نیست - پس هر دو `null`
     * می‌مانند و ردیفش عمداً کلیک‌پذیر نمی‌شود.
     */
    val loanId: Long? = null,
    val chequeId: Long? = null,
)

/** پورت مفهومی «تقویم مالی» اپ رقیب (VAMMAN) - سررسیدِ **هر سه منبع** (قسطِ وام، چک، پرداختِ
 * تکراری) رو رو یه گرید تقویم شمسی نشون می‌ده. [LoanRepository.getRows] خودش سررسید هر قسط رو از
 * رو startDate+intervalDays محاسبه می‌کنه، همون منطقی که LoanDetailScreen هم استفاده می‌کنه -
 * اینجا فقط دوباره برای همه‌ی وام‌ها جمع می‌شه، منطقِ محاسبه‌ی سررسید تکرار/تغییر نمی‌کنه. */
@HiltViewModel
class FinancialCalendarViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun dueItemsByDate(loans: List<LoanEntity>): Map<PersianDate, List<DueItem>> {
        val map = mutableMapOf<PersianDate, MutableList<DueItem>>()
        fun put(date: PersianDate, item: DueItem) {
            map.getOrPut(date) { mutableListOf() }.add(item)
        }

        for (loan in loans) {
            for (row in loanRepository.getRows(loan)) {
                val due = row["dueDate"] as? Map<*, *> ?: continue
                val y = (due["y"] as? Number)?.toInt() ?: continue
                val m = (due["m"] as? Number)?.toInt() ?: continue
                val d = (due["d"] as? Number)?.toInt() ?: continue
                put(
                    PersianDate(y, m, d),
                    DueItem(
                        title = loan.name,
                        subtitle = loan.bank,
                        amount = (row["installment"] as? Number)?.toDouble() ?: loan.installment,
                        paid = row["paid"] == true,
                        kind = DueKind.INSTALLMENT,
                        loanId = loan.id,
                    ),
                )
            }
        }

        // چکِ بایگانی‌شده عمداً نمیاد - همون قاعده‌ی تبِ سررسید (DueListViewModel.loadCheques).
        for (cheque in chequeRepository.getAllCheques()) {
            if (cheque.archived) continue
            put(
                PersianDate(cheque.dueYear, cheque.dueMonth, cheque.dueDay),
                DueItem(
                    title = "چکِ ${cheque.bankName}",
                    subtitle = cheque.ownerName,
                    amount = cheque.amount,
                    // 🚨 `status != "PENDING"` چکِ **برگشتی** را هم پرداخت‌شده حساب می‌کرد،
                    // پس روزِ چکِ برگشتی نقطه‌ی سبز می‌گرفت - یعنی بدترین خبرِ ممکن به
                    // شکلِ خبرِ خوب. فقط `PASSED` پرداخت‌شده است.
                    paid = when (cheque.status) {
                        "PASSED" -> true
                        "BOUNCED" -> false
                        else -> false
                    },
                    kind = DueKind.CHEQUE,
                    chequeId = cheque.id,
                ),
            )
        }

        // پرداختِ تکراری تاریخِ ذخیره‌شده نداره، فقط `dayOfMonth` - پس تکرارهاش برای بازه‌ی
        // قابلِ‌مرورِ تقویم ساخته می‌شن (۶ ماه عقب تا ۱۲ ماه جلو). «پرداخت‌شده» نداره چون این
        // جدول فقط الگو رو نگه می‌داره، نه ثبتِ واقعی - رجوع کن به RecurringPaymentEntity.
        val recurring = accountRepository.getRecurringPayments()
        if (recurring.isNotEmpty()) {
            val today = JalaliCalendar.today()
            for (offset in -6..12) {
                var m = today.m + offset
                var y = today.y
                while (m > 12) { m -= 12; y++ }
                while (m < 1) { m += 12; y-- }
                val daysInMonth = JalaliCalendar.daysInMonth(y, m)
                for (payment in recurring) {
                    val day = payment.dayOfMonth.coerceIn(1, daysInMonth)
                    put(
                        PersianDate(y, m, day),
                        DueItem(
                            title = payment.name,
                            subtitle = payment.categoryName.orEmpty(),
                            amount = payment.amount,
                            // الگو است نه ثبت - رجوع کن به `DueItem.paid`.
                            paid = null,
                            kind = DueKind.RECURRING,
                        ),
                    )
                }
            }
        }
        return map
    }
}
