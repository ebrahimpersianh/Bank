package ir.sadteam.loancalc.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    @Inject lateinit var uiPrefs: UiPrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return
        val body = messages.joinToString("") { it.messageBody ?: "" }
        if (body.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!uiPrefs.smsAutoImportEnabled.first()) return@launch
                val parsed = BankSmsParser.parse(body) ?: return@launch
                val accounts = accountRepository.observeAccounts().first()
                val account = accounts.firstOrNull { acc ->
                    parsed.cardSuffix != null && acc.cardNumber?.takeLast(4) == parsed.cardSuffix
                } ?: accounts.firstOrNull() ?: return@launch

                val today = JalaliCalendar.today()
                accountRepository.addTransaction(
                    accountId = account.id,
                    type = parsed.type,
                    amount = parsed.amountRial,
                    description = "خودکار از پیامکِ بانکی",
                    year = today.y,
                    month = today.m,
                    day = today.d,
                    category = if (parsed.type == TransactionType.WITHDRAWAL) "سایر هزینه" else "سایر درآمد",
                )
                uiPrefs.setLastSmsImportAt("${today.y}/${today.m}/${today.d}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
