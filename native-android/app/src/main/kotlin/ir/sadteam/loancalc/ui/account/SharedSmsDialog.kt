package ir.sadteam.loancalc.ui.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman

/**
 * وقتی کاربر یک پیام را از **برنامه‌ی پیامکِ خودِ گوشی** با «اشتراک‌گذاری» به جیبک می‌فرستد،
 * این همان‌جا که هست باز می‌شود - بی رفتن به تنظیمات و بی طبقه‌ی فرستنده.
 *
 * دو مسیر:
 * - متن مبلغ داشت → تاییدِ ثبت، بعد انتخابِ حساب‌کتاب.
 * - نداشت → یک پیامِ صریح که چرا نشد. **بی‌صدا رد نمی‌شویم**؛ کاربر تازه از برنامه‌ی دیگری
 *   آمده و سکوت یعنی «اپ خراب است».
 */
@Composable
fun SharedSmsDialog(
    text: String,
    onDone: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val accounts by accountViewModel.accounts.collectAsState()
    val parsed = BankSmsParser.parse(text)

    if (parsed == null) {
        ConfirmDialog(
            tone = ConfirmTone.HEAVY_CHANGE,
            title = "مبلغی در این پیام پیدا نشد",
            consequence = "از این متن نتوانستم مبلغ و نوعِ تراکنش را دربیاورم. " +
                "می‌توانی دستی ثبتش کنی، یا پیامِ دیگری را به اشتراک بگذاری.",
            actionLabel = "باشد",
            onConfirm = onDone,
            onDismiss = onDone,
        )
        return
    }

    var confirmed by remember { mutableStateOf(false) }
    val isWithdrawal = parsed.type == TransactionType.WITHDRAWAL
    val tomanText = fmt(rialToToman(parsed.amountRial.toLong()).toDouble()).faDigits()

    if (!confirmed) {
        ConfirmDialog(
            tone = ConfirmTone.PAYMENT,
            title = "${if (isWithdrawal) "برداشتِ" else "واریزِ"} $tomanText تومان ثبت شود؟",
            // متنِ خودِ پیام داخلِ پیام می‌آید - کاربر باید ببیند از کدام پیام خوانده شده،
            // وگرنه عددِ بی‌زمینه است. `ConfirmDialog` جای محتوای دلخواه ندارد.
            consequence = "«${text.replace('\n', ' ').take(140)}»\n\n" +
                "این مبلغ از همین متن خوانده شد. بعد از ثبت هم می‌توانی ویرایش یا حذفش کنی.",
            actionLabel = "ثبت کن",
            onConfirm = { confirmed = true },
            onDismiss = onDone,
        )
        return
    }

    AccountPickerDialog(
        accounts = accounts,
        title = "روی کدام حساب‌کتاب ثبت شود؟",
        onDismiss = onDone,
        onSelect = { account ->
            val today = JalaliCalendar.today()
            accountViewModel.addTransaction(
                accountId = account.id,
                type = parsed.type,
                amount = parsed.amountRial,
                description = "از پیامکِ اشتراک‌گذاری‌شده",
                year = today.y,
                month = today.m,
                day = today.d,
                category = MerchantCategoryGuesser.guess(text, isWithdrawal),
                originLabel = "پیامکِ اشتراک‌گذاری‌شده",
            )
            onDone()
        },
    )
}
