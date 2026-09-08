package ir.sadteam.loancalc.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * کنش‌های داخلِ اعلان. بی این گیرنده، تنها کارِ ممکن روی یک اعلان باز کردنِ برنامه است -
 * یعنی کاربری که فقط می‌خواهد بگوید «پرداختش کردم» باید صفحه پیدا کند، قسط را باز کند و
 * تیک بزند؛ چهار تپ برای کاری که در اعلان یکی است.
 *
 * دو کنش روی یادآورِ سررسید می‌نشیند:
 * - **پرداخت شد** - قسط/چک را پرداخت‌شده علامت می‌زند و اعلان را می‌بندد.
 * - **فردا یادم بیاور** - اعلان را می‌بندد و فردا دوباره می‌آید.
 *
 * ⚠️ `goAsync()` لازم است: کارِ دیتابیس async است و بی آن پروسه ممکن است قبلِ تمام شدنش
 * کشته شود. مهلتش حدودِ ۱۰ ثانیه است که برای یک نوشتن کافی است.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var loanRepository: LoanRepository

    @Inject lateinit var chequeRepository: ChequeRepository

    @Inject lateinit var accountRepository: AccountRepository

    @Inject lateinit var uiPrefs: UiPrefs

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        // اعلان **فوری** بسته می‌شود، قبلِ کارِ دیتابیس - وگرنه کاربر نیم‌ثانیه دکمه‌ی بی‌اثر
        // می‌بیند و دوباره می‌زند.
        NotificationManagerCompat.from(context).cancel(notificationId)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_PAID -> markPaid(intent)
                    ACTION_SNOOZE -> snooze(intent)
                    ACTION_CONFIRM_TX -> confirmTransaction(intent)
                }
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * ✅ **دو امضایی که طراح علامت زده بود، با امضای واقعیِ ریپازیتوری‌ها جایگزین شد** (حدسش
     * کامپایل نمی‌شد):
     * - وام: `setRowPaidOnTime(loan, m)` که خودِ `LoanEntity` می‌گیرد نه شناسه، پس اول وام
     *   از فهرست پیدا می‌شود.
     * - چک: `setStatus(cheque, ChequeStatus.PASSED)` - نوعِ enum است، نه رشته.
     */
    private suspend fun markPaid(intent: Intent) {
        val loanId = intent.getLongExtra(EXTRA_LOAN_ID, -1L)
        val installment = intent.getIntExtra(EXTRA_INSTALLMENT, -1)
        val chequeId = intent.getLongExtra(EXTRA_CHEQUE_ID, -1L)

        when {
            loanId > 0 && installment > 0 -> {
                loanRepository.getLoans().firstOrNull { it.id == loanId }?.let { loan ->
                    loanRepository.setRowPaidOnTime(loan, installment)
                }
            }
            chequeId > 0 -> {
                chequeRepository.getAllCheques().firstOrNull { it.id == chequeId }?.let { cheque ->
                    chequeRepository.setStatus(cheque, ChequeStatus.PASSED)
                }
            }
        }
    }

    /**
     * تعویقِ یک‌روزه. تاریخِ امروز در [UiPrefs] ثبت می‌شود تا اجرای فردای
     * [DueDateReminderWorker] بداند این مورد را دوباره بفرستد و یادآورِ عادی هم تکرارش
     * نکند.
     *
     * پیاده‌سازیِ ساده و عمدی: صفِ جدا و زمان‌بندِ اختصاصی نمی‌سازد. یادآور از قبل روزی
     * یک‌بار همه‌ی سررسیدها را می‌بیند، پس «فردا» یعنی «اجرای بعدی» و کافی است.
     */
    private suspend fun snooze(intent: Intent) {
        val key = intent.getStringExtra(EXTRA_SNOOZE_KEY) ?: return
        val today = JalaliCalendar.today()
        uiPrefs.setSnoozedUntilTomorrow(key, "${today.y}-${today.m}-${today.d}")
        // زمان‌بندی دوباره چیده نمی‌شود: یادآور از قبل روزی یک‌بار اجرا می‌شود، پس «فردا»
        // یعنی همان اجرای بعدی. فراخوانیِ دوباره‌ی زمان‌بند فقط لنگرِ ساعتش را جابه‌جا می‌کرد.
    }

    /**
     * دکمه‌ی «تایید» روی اعلانِ تراکنشِ خودکار - تراکنش را `confirmed = true` می‌کند. تا قبلِ
     * تایید روی موجودی اثر ندارد، پس این دکمه کارِ واقعی می‌کند نه فقط بستنِ اعلان.
     *
     * ✅ امضایی که طراح علامت زده بود اصلاح شد: `confirmTransaction(id)` - یک آرگومان،
     * چون تابعِ ریپازیتوری فقط تایید می‌کند و برگرداندن ندارد.
     */
    private suspend fun confirmTransaction(intent: Intent) {
        val txId = intent.getLongExtra(EXTRA_TX_ID, -1L)
        if (txId > 0) accountRepository.confirmTransaction(txId)
    }

    companion object {
        const val ACTION_MARK_PAID = "ir.sadteam.loancalc.action.MARK_PAID"
        const val ACTION_SNOOZE = "ir.sadteam.loancalc.action.SNOOZE"
        const val ACTION_CONFIRM_TX = "ir.sadteam.loancalc.action.CONFIRM_TX"

        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TX_ID = "tx_id"
        const val EXTRA_LOAN_ID = "loan_id"
        const val EXTRA_INSTALLMENT = "installment_m"
        const val EXTRA_CHEQUE_ID = "cheque_id"
        const val EXTRA_SNOOZE_KEY = "snooze_key"
    }
}
