package ir.sadteam.loancalc.notifications

import ir.sadteam.loancalc.core.toFa
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.AccountRepository
import kotlinx.coroutines.flow.first
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.InboxRepository
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

    @Inject lateinit var authPrefs: ir.sadteam.loancalc.data.prefs.AuthPrefs

    @Inject lateinit var inboxRepository: InboxRepository

    @Inject lateinit var parsingRuleRepository: ir.sadteam.loancalc.data.ParsingRuleRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        // اعلان **فوری** بسته می‌شود، قبلِ کارِ دیتابیس - وگرنه کاربر نیم‌ثانیه دکمه‌ی بی‌اثر
        // می‌بیند و دوباره می‌زند.
        NotificationManagerCompat.from(context).cancel(notificationId)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ir.sadteam.loancalc.data.UsageStats.action("notif_button_" + (intent.action ?: "?").substringAfterLast('.').lowercase())
                when (intent.action) {
                    ACTION_MARK_PAID -> markPaid(intent)
                    ACTION_SNOOZE -> snooze(intent)
                    ACTION_CONFIRM_TX -> confirmTransaction(context, intent)
                    ACTION_REJECT_TX -> rejectTransaction(intent)
                    ACTION_SET_CATEGORY -> {
                        val txId = intent.getLongExtra(EXTRA_TX_ID, -1L)
                        val cat = intent.getStringExtra(EXTRA_CATEGORY)
                        if (txId > 0 && !cat.isNullOrBlank()) {
                            CategoryLearning.apply(accountRepository, parsingRuleRepository, txId, cat)
                        }
                    }
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
        markPaidLocal(intent)
        // پرداخت از اعلان هم **همان لحظه** به سرور برود (قبلاً تا اولین تغییرِ بعدیِ داخلِ برنامه
        // می‌ماند و اگر گوشی خراب می‌شد این پرداخت از پشتیبانِ ابری جا می‌افتاد).
        runCatching {
            val token = authPrefs.authToken.first()
            if (!token.isNullOrEmpty()) {
                loanRepository.pushToServer(token)
                chequeRepository.pushToServer(token)
                accountRepository.pushToServer(token)
            }
        }
    }

    private suspend fun markPaidLocal(intent: Intent) {
        val loanId = intent.getLongExtra(EXTRA_LOAN_ID, -1L)
        val installment = intent.getIntExtra(EXTRA_INSTALLMENT, -1)
        val chequeId = intent.getLongExtra(EXTRA_CHEQUE_ID, -1L)

        when {
            loanId > 0 && installment > 0 -> {
                loanRepository.getLoans().firstOrNull { it.id == loanId }?.let { loan ->
                    // بازبینیِ ۹ مهر: اعلانِ قدیمی برای قسطی که در برنامه پرداخت شده دوباره از حساب کم نکند.
                    val alreadyPaid = loanRepository.getRows(loan)
                        .firstOrNull { (it["m"] as? Number)?.toInt() == installment }?.get("paid") == true
                    if (alreadyPaid) return@let
                    loanRepository.setRowPaidOnTime(loan, installment)
                    val amount = loanRepository.getRows(loan).firstOrNull { (it["m"] as? Number)?.toInt() == installment }
                        ?.let { (it["installment"] as? Number)?.toDouble() } ?: loan.installment
                    // از اعلان نمی‌شود حساب پرسید: فقط اگر یک حساب داری از همان کم می‌شود؛ وگرنه
                    // در پیام‌ها می‌گوییم تا کاربر خودش ثبت کند (بی‌صدا ثبت‌نشدن ممنوع).
                    val recorded = accountRepository.recordLinkedPayment(
                        accountId = null,
                        sourceType = "loan",
                        sourceId = "${loan.id}:$installment",
                        amount = amount,
                        description = "قسط ${toFa(installment)} - ${loan.name}",
                    )
                    if (!recorded) {
                        inboxRepository.post(
                            kind = ir.sadteam.loancalc.data.db.InboxMessageEntity.Kind.SYSTEM,
                            title = "قسط پرداخت‌شده علامت خورد",
                            body = "قسط ${toFa(installment)} «${loan.name}» از حسابی کم نشد، چون حسابِ مشخصی نبود (هیچ حسابی نداری یا چند حساب داری). از صفحه‌ی تراکنش‌ها یک برداشت با همین مبلغ ثبت کن.",
                        )
                    }
                }
            }
            chequeId > 0 -> {
                chequeRepository.getAllCheques().firstOrNull { it.id == chequeId }?.let { cheque ->
                    if (cheque.status == ChequeStatus.PASSED.name) return@let
                    chequeRepository.setStatus(cheque, ChequeStatus.PASSED)
                    val recorded = accountRepository.recordLinkedPayment(
                        accountId = null,
                        sourceType = "cheque",
                        sourceId = cheque.id.toString(),
                        amount = cheque.amount,
                        description = "چک - ${cheque.ownerName}",
                        deposit = cheque.type == "RECEIVED",
                    )
                    if (!recorded) {
                        inboxRepository.post(
                            kind = ir.sadteam.loancalc.data.db.InboxMessageEntity.Kind.SYSTEM,
                            title = "چک پاس‌شده علامت خورد",
                            body = "مبلغِ چکِ «${cheque.ownerName}» در حسابی ثبت نشد، چون حسابِ مشخصی نبود (هیچ حسابی نداری یا چند حساب داری). از صفحه‌ی تراکنش‌ها ثبتش کن.",
                        )
                    }
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
    private suspend fun confirmTransaction(context: Context, intent: Intent) {
        val txId = intent.getLongExtra(EXTRA_TX_ID, -1L)
        if (txId <= 0) return
        accountRepository.confirmTransaction(txId)
        inboxRepository.resolveByRefId(txId.toString(), done = true)
        // سوالِ دوم فقط حالا و فقط اگر دسته نامشخص بوده - یک سوال در هر لحظه.
        if (!intent.getBooleanExtra(EXTRA_CATEGORY_KNOWN, true)) {
            val type = accountRepository.transactionById(txId)?.type ?: "WITHDRAWAL"
            AutoTxNotifier.askCategory(context, txId, CategoryLearning.topCategories(accountRepository, type))
        }
    }

    /**
     * دکمه‌ی «نه» - تراکنشِ تاییدنشده **پاک** می‌شود و پیامِ صندوق هم بسته. تشخیصِ غلط نباید
     * برای همیشه معلق بماند (گزارشِ واقعیِ کاربر: یک واریزِ بیمه که اصلاً رخ نداده بود).
     */
    private suspend fun rejectTransaction(intent: Intent) {
        val txId = intent.getLongExtra(EXTRA_TX_ID, -1L)
        if (txId <= 0) return
        // فقط تراکنشِ هنوز-تاییدنشده؛ تراکنشِ تأییدشده (مثلاً با پرداختِ دستی یکی شده) پاک نمی‌شود.
        accountRepository.transactionById(txId)?.takeIf { !it.confirmed }?.let { accountRepository.deleteTransaction(it) }
        inboxRepository.resolveByRefId(txId.toString(), done = false)
    }

    companion object {
        const val ACTION_MARK_PAID = "ir.sadteam.loancalc.action.MARK_PAID"
        const val ACTION_SNOOZE = "ir.sadteam.loancalc.action.SNOOZE"
        const val ACTION_CONFIRM_TX = "ir.sadteam.loancalc.action.CONFIRM_TX"
        const val ACTION_REJECT_TX = "ir.sadteam.loancalc.action.REJECT_TX"
        const val ACTION_SET_CATEGORY = "ir.sadteam.loancalc.action.SET_CATEGORY"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_CATEGORY_KNOWN = "category_known"

        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TX_ID = "tx_id"
        const val EXTRA_LOAN_ID = "loan_id"
        const val EXTRA_INSTALLMENT = "installment_m"
        const val EXTRA_CHEQUE_ID = "cheque_id"
        const val EXTRA_SNOOZE_KEY = "snooze_key"
    }
}
