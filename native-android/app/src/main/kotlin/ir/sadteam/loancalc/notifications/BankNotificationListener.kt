package ir.sadteam.loancalc.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.smsDedupeFingerprint
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
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

    @Inject lateinit var parsingRuleRepository: ParsingRuleRepository

    @Inject lateinit var uiPrefs: UiPrefs

    @Inject lateinit var authPrefs: ir.sadteam.loancalc.data.prefs.AuthPrefs

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
        // 🧠 (۱۴ مهر) بعضی اپ‌ها متن را در خط‌ها/زیرمتن/تیکر می‌گذارند، نه در EXTRA_TEXT.
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" ") { it.toString() }.orEmpty()
        val sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val summary = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        val ticker = notification.tickerText?.toString().orEmpty()
        val main = big.ifBlank { text }.ifBlank { lines }.ifBlank { ticker }
        val body = listOf(title, main, sub, summary).filter { it.isNotBlank() }.distinct().joinToString(" ")
        if (body.isBlank()) return

        scope.launch {
            // اپی که به یک حساب وصل شده هم «انتخاب‌شده» است (فهرستِ جدا از تنظیمات برداشته شد).
            val allowed = uiPrefs.notifAutoImportPackages.first()
            val linked = accountRepository.observeAccounts().first().any { acc -> acc.smsSender.orEmpty().split(',').any { it.trim() == packageName } }
            if (packageName !in allowed && !linked) return@launch
            fun log(r: String) = NotifDebugLog.record(applicationContext, appLabelOf(packageName), body, r)
            if (!uiPrefs.notifAutoImportEnabled.first()) { log("off"); return@launch }
            // بازبینیِ ۹ مهر: مثلِ پیامکِ بانکی، ثبتِ خودکار از اعلان هم مالِ اشتراک است.
            if (!authPrefs.subscribed.first()) { log("not_subscribed"); ir.sadteam.loancalc.data.UsageStats.error("notif_not_subscribed"); return@launch }

            val parsed = BankSmsParser.parse(body, trustedSource = true)
                ?: run { log("parse_fail"); ir.sadteam.loancalc.data.UsageStats.error("notif_parse_fail"); return@launch }
            // 🚨 اپ‌های بانکی همان اعلان را دوباره منتشر/به‌روزرسانی می‌کنند و این تابع هر بار
            // اجرا می‌شود؛ بی این کنترل، یک واریز دو تراکنشِ منتظرِ تایید می‌ساخت و با تاییدِ
            // هر دو، موجودی دو برابر جابه‌جا می‌شد.
            val importKey = "notif|$packageName|${parsed.type}|${parsed.amountRial}|${parsed.cardSuffix.orEmpty()}|${smsDedupeFingerprint(body)}"
            if (!uiPrefs.claimAutoImportKey(importKey)) { log("duplicate"); return@launch }
            // 🚨 اعلانِ بانک هیچ‌وقت روی حسابِ «نقدی/غیربانکی» نمی‌نشیند (گزارشِ کاربر، ۷ مهر: واریزهای
            // بلو در حسابِ نقدی ثبت شده بود چون تنها حسابِ کاربر بود).
            val accounts = accountRepository.observeAccounts().first()
                .filter { it.type == ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK }
            // ⚠️ بستهٔ فرستنده اول **مجموعه‌ی نامزدها** را محدود می‌کند و بعد شماره‌ی کارت بینِ
            // همان‌ها تصمیم می‌گیرد - با دو حسابِ یک بانک، «اولین تطبیق» می‌توانست حسابِ اشتباه
            // را بردارد در حالی که چهار رقمِ آخرِ کارت صریحاً در متن آمده بود.
            val sameSender = accounts.filter { acc -> acc.smsSender.orEmpty().split(',').any { it.trim() == packageName } }
            val account = sameSender.firstOrNull { acc ->
                parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
            }
                ?: sameSender.singleOrNull()
                ?: sameSender.firstOrNull()
                ?: accounts.firstOrNull { acc ->
                    parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
                }
                // نامِ اپ (مثلاً «بلو» / «بلوبانک») با نامِ بانکِ حساب - تا کاربر مجبور نباشد بسته‌نام وصل کند.
                ?: appLabelOf(packageName).let { label ->
                    accounts.filter { acc ->
                        val b = acc.bankName.replace("بانک", "").trim()
                        b.length >= 2 && (label.contains(b) || b.contains(label.replace("بانک", "").trim().ifBlank { "\u0000" }))
                    }.singleOrNull()
                }
                ?: accounts.singleOrNull() // فقط اگه کلاً یه حساب داره، حدس بی‌خطره
                ?: run {
                    ir.sadteam.loancalc.data.UsageStats.error("notif_no_account")
                    log("no_account")
                    inboxRepository.post(
                        kind = InboxMessageEntity.Kind.SYSTEM,
                        title = "اعلانِ بانکی بی‌حساب ماند",
                        body = "یک تراکنش از «${appLabelOf(packageName)}» دیدیم ولی نمی‌دانیم مالِ کدام حساب است. در ویرایشِ حساب، «اپِ اعلان‌دهنده» را همین اپ انتخاب کن.",
                        refId = null,
                        sourceLabel = "اعلانِ ${appLabelOf(packageName)}",
                        sourceText = body,
                    )
                    return@launch
                }

            val today = JalaliCalendar.today()
            val isWithdrawal = parsed.type == TransactionType.WITHDRAWAL
            // همون ترتیبِ BankSmsReceiver: قاعده‌ی دستیِ کاربر، بعد حدسِ کلیدواژه‌ای، بعد دسته‌ی
            // کور - رجوع کن به کامنتِ BankSmsReceiver برای توضیحِ کامل.
            val ruleCategory = parsingRuleRepository.firstMatch(body, isWithdrawal)?.category
            val guessedCategory = ruleCategory ?: MerchantCategoryGuesser.guess(body, isWithdrawal)
            val confident = guessedCategory != null
            val category = guessedCategory ?: if (isWithdrawal) "سایر هزینه" else "سایر درآمد"
            // ⚠️ **تاییدنشده** ثبت می‌شه (تصمیمِ صریحِ کاربر): تا وقتی خودش تاییدش نکرده رو
            // موجودی اثر نمی‌ذاره. کارتِ اقدام‌دارِ مرکزِ پیام‌ها ازش ساخته می‌شه.
            ir.sadteam.loancalc.data.UsageStats.action("notif_auto_tx")
            if (accountRepository.hasRecentAutoTwin(account.id, parsed.type, parsed.amountRial, "اعلانِ ${appLabelOf(packageName)}")) { log("twin"); return@launch }
            log("ok")
            val txId = accountRepository.addTransaction(
                accountId = account.id,
                type = parsed.type,
                amount = parsed.amountRial,
                description = "خودکار از اعلانِ بانکی",
                year = today.y,
                month = today.m,
                day = today.d,
                category = category,
                confirmed = false,
                // `71a`: منبع روی خودِ تراکنش می‌نشیند، نه فقط در مرکزِ پیام‌ها.
                originLabel = "اعلانِ ${appLabelOf(packageName)}",
            )
            // برداشت از یک حسابِ خودت + واریزِ همان مبلغ به حسابِ دیگرت = جابه‌جایی، نه خرج و درآمد.
            // اول: اگر مقصدِ برداشت شماره‌کارت/حساب/شبای یکی از حساب‌های خودت است، همین حالا جابه‌جایی.
            runCatching {
                val dest = if (parsed.type == TransactionType.WITHDRAWAL) accountRepository.ownDestinationOf(body, account.id) else null
                if (dest != null) accountRepository.markOwnTransfer(txId, dest.id) else accountRepository.pairAutoTransfer(txId)
            }
            // منبعِ واحد: پیام اول اینجا ساخته می‌شه؛ اعلانِ گوشی از رو همین ردیف ساخته
            // می‌شه، نه مستقل.
            // واحد **تومان** و رقمِ فارسی (بندِ ۲ی README + لایه‌ی ارقام) - قبلاً «ریال»ِ
            // لاتین بود، هم این‌جا هم تو BankSmsReceiver.
            val amountToman = fmt(rialToToman(parsed.amountRial.toLong()).toDouble()).faDigits()
            inboxRepository.post(
                kind = InboxMessageEntity.Kind.DETECTED_TX,
                title = if (isWithdrawal) "برداشتِ تازه" else "واریزِ تازه",
                body = if (confident) {
                    "$amountToman تومان از «${account.name}» - دسته: $category. تایید می‌کنی؟"
                } else {
                    "$amountToman تومان از «${account.name}» - دسته‌بندیش نامشخصه، لمس کن و خودت انتخاب کن."
                },
                refId = txId.toString(),
                sourceLabel = "اعلانِ ${appLabelOf(packageName)}",
                sourceText = body,
            )
            // 🚨 این تکه **نبود**: تراکنشی که خودکار ثبت می‌شد هیچ اعلانی نمی‌داد و کاربر
            // تا باز کردنِ برنامه خبردار نمی‌شد. فریمِ 50b و AutoTxNotifier همین را می‌بندند.
            // تصمیمِ ثبت‌شده: **همیشه** اعلان بدهد، چون دومین کارش گفتنِ «سیستم کار می‌کند»
            // است - کسی که خبری نمی‌گیرد فرض می‌کند خراب است.
            if (uiPrefs.autoTxNotifyEnabled.first()) AutoTxNotifier.notify(
                context = applicationContext,
                txId = txId,
                amountRial = parsed.amountRial,
                accountName = account.name,
                category = category,
                isWithdrawal = isWithdrawal,
                confident = confident,
                privacyMode = uiPrefs.privacyModeEnabled.first(),
                sourceLabel = "اعلانِ ${appLabelOf(packageName)}",
            )
            uiPrefs.setLastSmsImportAt("${today.y}/${today.m}/${today.d}")
            val updated = account.copy(lastSmsAt = System.currentTimeMillis())
            runCatching { accountRepository.updateAccount(updated) }
            // 🧠 با کارت/نامِ بانک پیدا شد → اپِ اعلان از این به بعد به همین حساب وصل باشد.
            if (account !in sameSender) runCatching { accountRepository.learnSender(updated, packageName) }
            // 🧠 مانده‌ی بانک در برابرِ موجودیِ ثبت‌شده - روزی یک بار برای هر حساب.
            parsed.balanceRial?.let { bankBal ->
                val gap = accountRepository.bankBalanceGap(updated, !isWithdrawal, parsed.amountRial, bankBal)
                if (gap != null && uiPrefs.claimAutoImportKey("gap|${updated.id}|${today.y}-${today.m}-${today.d}")) {
                    val gapToman = fmt(rialToToman(kotlin.math.abs(gap).toLong()).toDouble()).faDigits()
                    inboxRepository.post(
                        kind = InboxMessageEntity.Kind.SYSTEM,
                        title = "موجودیِ «${updated.name}» با بانک فرق دارد",
                        body = "بانک مانده را $gapToman تومان " + (if (gap > 0) "بیشتر" else "کمتر") +
                        " از جیبک نوشته. احتمالاً تراکنشی ثبت نشده یا «موجودیِ روزِ شروع» درست نیست.",
                        sourceLabel = "اعلانِ ${appLabelOf(packageName)}",
                        sourceText = body,
                    )
                }
            }
        }
    }

    /** نامِ فارسی/نمایشیِ اپِ فرستنده؛ اگر پیدا نشد خودِ نامِ بسته. */
    private fun appLabelOf(packageName: String): String = runCatching {
        val pm = applicationContext.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)
}
