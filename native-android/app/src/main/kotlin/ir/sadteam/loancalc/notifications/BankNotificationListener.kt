package ir.sadteam.loancalc.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * خوندنِ خودکارِ **اعلانِ** اپ‌های بانکی و ثبتِ خودکارِ تراکنش.
 *
 * **چرا لازم شد**: بانک‌های دیجیتال (بلوبانک و…) اصلاً پیامک نمی‌فرستن و فقط اعلانِ درون‌اپی
 * می‌دن، پس [BankSmsReceiver] براشون بی‌فایده‌ست - خواسته‌ی صریحِ کاربر.
 *
 * **مغزِ تشخیص همون [BankSmsParser]ِ پیامکه** - از صفر نوشته نشد. متنِ اعلان (عنوان + متن) به هم
 * چسبونده و به همون پارسر داده می‌شه؛ الگوهای «برداشت/واریز + مبلغ» تو هر دو کانال یکسانن.
 *
 * **حریمِ خصوصی (مهم برای بازبینِ استور)**:
 * - پیش‌فرض کاملاً خاموشه ([UiPrefs.notifAutoImportEnabled]).
 * - حتی وقتی روشنه، **فقط اعلانِ اپ‌هایی خونده می‌شه که خودِ کاربر تو تنظیمات انتخاب کرده**
 *   ([UiPrefs.notifAutoImportPackages]) - نه همه‌ی اپ‌ها. اگه لیست خالی باشه هیچ کاری نمی‌کنه.
 * - هیچ داده‌ای هیچ‌جا فرستاده نمی‌شه؛ فقط یه تراکنشِ محلی ساخته می‌شه که مثلِ هر تراکنشِ دیگه
 *   قابلِ‌حذف/ویرایشه.
 * - مجوزش (`BIND_NOTIFICATION_LISTENER_SERVICE`) دیالوگِ Runtime نداره؛ کاربر باید دستی از
 *   تنظیماتِ گوشی بده - دکمه‌ش تو صفحه‌ی تنظیماتِ اپه.
 *
 * ⚠️ تشخیصِ حساب همون ترتیبِ [BankSmsReceiver]ه با یه تفاوت: به‌جای سرشماره‌ی پیامک، **بسته‌نامِ
 * اپ** به حساب وصل می‌شه (فیلدِ `smsSender` همون حساب می‌تونه بسته‌نام هم باشه) - و اگه پیدا نشد،
 * چهار رقمِ آخرِ کارت. عمداً به «حسابِ اول» فال‌بک نمی‌کنه چون بر خلافِ پیامک، اینجا کاربر ممکنه
 * چند اپِ بانکی انتخاب کرده باشه و حدسِ اشتباه پول رو رو حسابِ اشتباه بشونه.
 */
@AndroidEntryPoint
class BankNotificationListener : NotificationListenerService() {

    @Inject lateinit var accountRepository: AccountRepository

    @Inject lateinit var inboxRepository: InboxRepository

    @Inject lateinit var uiPrefs: UiPrefs

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val packageName = sbn.packageName ?: return
        // اعلانِ خودِ این اپ رو هرگز نخون (وگرنه یادآوری‌های خودمون دوباره پارس می‌شن).
        if (packageName == applicationContext.packageName) return

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val body = listOf(title, big.ifBlank { text }).filter { it.isNotBlank() }.joinToString(" ")
        if (body.isBlank()) return

        scope.launch {
            if (!uiPrefs.notifAutoImportEnabled.first()) return@launch
            val allowed = uiPrefs.notifAutoImportPackages.first()
            if (packageName !in allowed) return@launch

            val parsed = BankSmsParser.parse(body) ?: return@launch
            val accounts = accountRepository.observeAccounts().first()
            val account = accounts.firstOrNull { it.smsSender?.trim() == packageName }
                ?: accounts.firstOrNull { acc ->
                    parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
                }
                ?: accounts.singleOrNull() // فقط اگه کلاً یه حساب داره، حدس بی‌خطره
                ?: return@launch

            val today = JalaliCalendar.today()
            // ⚠️ **تاییدنشده** ثبت می‌شه (تصمیمِ صریحِ کاربر): تا وقتی خودش تاییدش نکرده رو
            // موجودی اثر نمی‌ذاره. کارتِ اقدام‌دارِ مرکزِ پیام‌ها ازش ساخته می‌شه.
            val txId = accountRepository.addTransaction(
                accountId = account.id,
                type = parsed.type,
                amount = parsed.amountRial,
                description = "خودکار از اعلانِ بانکی",
                year = today.y,
                month = today.m,
                day = today.d,
                category = if (parsed.type == TransactionType.WITHDRAWAL) "سایر هزینه" else "سایر درآمد",
                confirmed = false,
            )
            // منبعِ واحد: پیام اول اینجا ساخته می‌شه؛ اعلانِ گوشیِ خودمون (اگه بعداً اضافه بشه)
            // باید از رو همین ردیف ساخته بشه، نه مستقل.
            inboxRepository.post(
                kind = InboxMessageEntity.Kind.DETECTED_TX,
                title = if (parsed.type == TransactionType.WITHDRAWAL) "برداشتِ تازه" else "واریزِ تازه",
                body = "${fmt(parsed.amountRial)} ریال از «${account.name}» - تایید می‌کنی؟",
                refId = txId.toString(),
            )
            uiPrefs.setLastSmsImportAt("${today.y}/${today.m}/${today.d}")
        }
    }
}
