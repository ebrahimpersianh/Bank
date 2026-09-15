package ir.sadteam.loancalc.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.smsSenderMatches
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * خوندنِ خودکارِ پیامکِ بانکی و ثبتِ خودکارِ تراکنش - رجوع کن به CLAUDE.md، «خوندنِ خودکارِ پیامکِ
 * بانکی». عمداً هیچ داده‌ای هیچ‌جا فرستاده نمی‌شه (نه سرور، نه سرویسِ ثالث) - فقط یه تراکنشِ محلی
 * ساخته می‌شه که مثلِ هر تراکنشِ دیگه‌ای بعداً قابلِ‌حذف/ویرایشه (چون تشخیصِ خودکار همیشه ۱۰۰٪
 * درست نیست - رجوع کن به کامنتِ BankSmsParser). فقط وقتی [UiPrefs.smsAutoImportEnabled] روشن
 * باشه کاری می‌کنه (پیش‌فرض خاموش - کاربر باید صریح از تنظیمات فعالش کنه، چون RECEIVE_SMS/READ_SMS
 * مجوزِ حساسیه).
 */
@AndroidEntryPoint
class BankSmsReceiver : BroadcastReceiver() {
    @Inject lateinit var accountRepository: AccountRepository

    @Inject lateinit var inboxRepository: InboxRepository

    @Inject
    lateinit var parsingRuleRepository: ParsingRuleRepository

    @Inject lateinit var uiPrefs: UiPrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return
        val body = messages.joinToString("") { it.messageBody ?: "" }
        if (body.isBlank()) return
        val sender = messages.firstNotNullOfOrNull { it.originatingAddress }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!uiPrefs.smsAutoImportEnabled.first()) return@launch
                val parsed = BankSmsParser.parse(body) ?: return@launch
                val accounts = accountRepository.observeAccounts().first()
                // ترتیبِ تشخیصِ حساب، از مطمئن‌ترین به ضعیف‌ترین:
                // ۱) شماره/سرشماره‌ی پیامکی که کاربر خودش موقعِ ساختِ حساب وصل کرده (خواسته‌ی صریحِ
                //    کاربر - قابلِ‌اعتمادترین، چون هر بانک سرشماره‌ی خودش رو داره)
                // ۲) ۴ رقمِ آخرِ کارت اگه تو متنِ پیامک اومده باشه
                // ۳) اگه هیچ حسابی سرشماره ثبت نکرده، همون رفتارِ قبلی (حسابِ اول) - ولی اگه حداقل
                //    یه حساب سرشماره داره، دیگه حدس نمی‌زنیم و پیامکِ ناشناس نادیده گرفته می‌شه،
                //    وگرنه پیامکِ بانکِ B بی‌سروصدا رو حسابِ بانکِ A ثبت می‌شد.
                // کلیدِ هر بانک: حسابی که کاربر خاموشش کرده اصلاً نامزدِ تطبیق نیست.
                val candidates = accounts.filter { it.smsEnabled }
                // ⚠️ سرشماره اول **مجموعه‌ی نامزدها** را محدود می‌کند، بعد شماره‌ی کارت بینِ
                // همان‌ها تصمیم می‌گیرد. قبلاً اولین تطبیقِ سرشماره فوراً برنده می‌شد، پس با دو
                // حساب در یک بانک، پیامکی که صریحاً چهار رقمِ آخرِ کارتِ دوم را داشت روی حسابِ
                // اول می‌نشست.
                val sameSender = candidates.filter { acc -> smsSenderMatches(acc.smsSender, sender) }
                val account = sameSender.firstOrNull { acc ->
                    parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
                }
                    ?: sameSender.firstOrNull()
                    ?: candidates.firstOrNull { acc ->
                        parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
                    }
                    ?: candidates.takeIf { list -> list.none { !it.smsSender.isNullOrBlank() } }?.firstOrNull()
                    ?: return@launch

                // هم‌الگو با BankNotificationListener: یک پیامک که دو بار تحویل شود (تکرارِ
                // شبکه یا اجرای دوباره‌ی receiver) نباید دو تراکنش بسازد.
                val importKey = "sms|$sender|${parsed.type}|${parsed.amountRial}|${parsed.cardSuffix.orEmpty()}"
                if (!uiPrefs.claimAutoImportKey(importKey)) return@launch

                val today = JalaliCalendar.today()
                val isWithdrawal = parsed.type == TransactionType.WITHDRAWAL
                // ترتیبِ دسته‌بندی: ۱) قاعده‌ی دستیِ خودِ کاربر (مرتب، اولین تطبیق برنده) ۲)
                // حدسِ کلیدواژه‌ای از رو متنِ پیامک (بندِ ۲.۲) ۳) دسته‌ی کورِ قبلی، وقتی هیچ‌کدوم
                // مطمئن نبودن - و تو این حالتِ آخر پیامِ اعلان صریح می‌گه کاربر باید خودش انتخاب کنه.
                val ruleCategory = parsingRuleRepository.firstMatch(body, isWithdrawal)?.category
                val guessedCategory = ruleCategory ?: MerchantCategoryGuesser.guess(body, isWithdrawal)
                val confident = guessedCategory != null
                val category = guessedCategory ?: if (isWithdrawal) "سایر هزینه" else "سایر درآمد"
                // ⚠️ **تاییدنشده** (تصمیمِ صریحِ کاربر) - تا تاییدِ خودش رو موجودی اثر نمی‌ذاره.
                val txId = accountRepository.addTransaction(
                    accountId = account.id,
                    type = parsed.type,
                    amount = parsed.amountRial,
                    description = "خودکار از پیامکِ بانکی",
                    year = today.y,
                    month = today.m,
                    day = today.d,
                    category = category,
                    confirmed = false,
                    // `71a`: منبع روی خودِ تراکنش می‌نشیند، نه فقط در مرکزِ پیام‌ها.
                    originLabel = "پیامکِ $sender",
                )
                // واحد **تومان** و رقمِ فارسی - قبلاً «ریال»ِ لاتین بود (بندِ ۹ی تحویلِ اعلان‌ها).
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
                    // منبع: کاربر باید بتواند بگوید این کارت از کدام پیامک ساخته شده. بی این،
                    // تشخیصِ غلط قابلِ ردیابی نیست.
                    sourceLabel = "پیامک از $sender",
                    sourceText = body,
                )
                // 🚨 بندِ ۷ی تحویل: مسیرِ پیامک هم مثلِ مسیرِ اعلانِ بانکی باید اعلان بدهد،
                // وگرنه تراکنشِ پیامکی بی‌خبر و تاییدنشده می‌ماند.
                if (uiPrefs.autoTxNotifyEnabled.first()) AutoTxNotifier.notify(
                    context = context.applicationContext,
                    txId = txId,
                    amountRial = parsed.amountRial,
                    accountName = account.name,
                    category = category,
                    isWithdrawal = isWithdrawal,
                    confident = confident,
                    privacyMode = uiPrefs.privacyModeEnabled.first(),
                    sourceLabel = "پیامک از $sender",
                )
                uiPrefs.setLastSmsImportAt("${today.y}/${today.m}/${today.d}")
                // زمانِ آخرین پیامکِ **همین حساب** - زیرنویسِ صفحه‌ی تنظیماتِ پیامک از این ساخته می‌شه.
                accountRepository.updateAccount(account.copy(lastSmsAt = System.currentTimeMillis()))
            } finally {
                pendingResult.finish()
            }
        }
    }
}
