package ir.sadteam.loancalc.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** کلیدِ extra-یِ Intentِ نوتیفیکیشنِ یادآوریِ قسط - هم تو DueDateReminderWorker (ساختنِ
 * PendingIntent) هم تو MainActivity (خوندنِ intent) استفاده می‌شه. */
const val EXTRA_OPEN_LOAN_ID = "open_loan_id"

/**
 * پلِ سبک بینِ Intentِ زدنِ نوتیفیکیشنِ یادآوری (که تو MainActivity.onCreate/onNewIntent می‌رسه) و
 * خودِ کامپوزیبل‌ها - رجوع کن به مورد ۵ تو CLAUDE.md. چون کامپوزیبلِ واقعیِ لیستِ وام‌ها
 * (MyLoansScreen) پشتِ چندتا گیتِ دیگه‌ست (قفلِ PIN، مجوزها، ورود، خوش‌آمد - رجوع کن به AppRoot تو
 * MainActivity.kt)، به‌جای پاس‌دادنِ callback از تهِ اون گیت‌ها، این یه StateFlowِ سراسریه که
 * LoanCalcApp خودش هر بار عوض شد collect می‌کنه و بعدِ مصرف [consume] می‌کنه (تا با چرخشِ صفحه/رفرشِ
 * بعدی دوباره باز نشه).
 */
@Singleton
class DeepLinkTarget @Inject constructor() {
    private val _pendingLoanId = MutableStateFlow<Long?>(null)
    val pendingLoanId: StateFlow<Long?> = _pendingLoanId

    fun setLoanId(id: Long) {
        _pendingLoanId.value = id
    }

    fun consume() {
        _pendingLoanId.value = null
    }
}
