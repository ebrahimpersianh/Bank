package ir.sadteam.loancalc.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** کلیدِ extra-یِ Intentِ نوتیفیکیشنِ یادآوریِ قسط - هم تو DueDateReminderWorker (ساختنِ
 * PendingIntent) هم تو MainActivity (خوندنِ intent) استفاده می‌شه. */
const val EXTRA_OPEN_LOAN_ID = "open_loan_id"

/**
 * کلیدِ extra-یِ اعلانِ سررسیدِ **چک**. تازه است: اعلانِ چک تا حالا هیچ `contentIntent`ی
 * نداشت، پس تپ روش هیچ کاری نمی‌کرد و مقصدی هم لازم نبود. حالا که دارد، مقصد می‌خواهد.
 */
const val EXTRA_OPEN_CHEQUE_ID = "open_cheque_id"

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

    /**
     * میان‌برِ فشارِ طولانی رو آیکونِ اپ (مثلِ دولینگو) - خواسته‌ی صریحِ کاربر.
     * مقدارش یکی از `SHORTCUT_*`ه و `MainActivity` بعد از خوندنش صفرش می‌کنه.
     */
    private val _pendingShortcut = MutableStateFlow<String?>(null)
    val pendingShortcut: StateFlow<String?> = _pendingShortcut

    fun setShortcut(action: String) {
        _pendingShortcut.value = action
    }

    fun consumeShortcut() {
        _pendingShortcut.value = null
    }

    companion object {
        const val SHORTCUT_ADD_TRANSACTION = "add_transaction"
        const val SHORTCUT_DUE = "due"
        const val SHORTCUT_REPORT = "report"
    }
}

/**
 * هم‌الگوی [PendingLoanDeepLink] برای چک. جدا نگه داشته شد نه ادغام، چون دو مقصدِ مستقل‌اند
 * و اعلانِ گروه‌شده می‌تواند هم‌زمان یکی از هر کدام داشته باشد - با یک فیلدِ مشترک، دومی
 * اولی را پاک می‌کرد.
 */
@Singleton
class PendingChequeDeepLink @Inject constructor() {
    private val _pendingChequeId = MutableStateFlow<Long?>(null)
    val pendingChequeId: StateFlow<Long?> = _pendingChequeId

    fun setChequeId(id: Long) {
        _pendingChequeId.value = id
    }

    fun consume() {
        _pendingChequeId.value = null
    }

}

/**
 * مقصدِ تپ روی **اعلانِ تراکنشِ خودکار** (فریمِ `50b`). جدا از وام و چک، به همان دلیلِ
 * نوشته‌شده بالای [PendingChequeDeepLink]: سه اعلانِ مستقل می‌توانند هم‌زمان روی نوار باشند.
 *
 * [pickCategory] یعنی کاربر دکمه‌ی «دسته‌اش این نیست/این است» را زده، نه خودِ بدنه‌ی اعلان را -
 * پس باید مستقیم روی انتخابِ دسته بنشیند، نه فقط کارتِ تراکنش.
 */
@Singleton
class PendingTxDeepLink @Inject constructor() {
    private val _pending = MutableStateFlow<Pair<Long, Boolean>?>(null)
    val pending: StateFlow<Pair<Long, Boolean>?> = _pending

    fun set(txId: Long, pickCategory: Boolean) {
        _pending.value = txId to pickCategory
    }

    fun consume() {
        _pending.value = null
    }
}
