package ir.sadteam.loancalc.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * پالتِ آیکونِ «حساب‌کتابِ غیربانکی» (نقدی، کیفِ پول، کارتِ اعتباری، کیفِ پولِ موبایلی…).
 *
 * چرا جدا از [categoryIconChoices]؟ اون‌ها آیکونِ *دسته‌بندیِ خرج* هستن (رستوران، ماشین، هدیه…)
 * که برای یه حساب‌کتاب بی‌معنی‌ان. اینجا فقط چیزهایی هست که واقعاً می‌تونن «جایی که پول توشه» باشن.
 *
 * اولین گزینه (`cash`) عمداً پیش‌فرضه - همون «نقدی»ه که کاربر تو مرحله‌ی افزودنِ حساب‌کتابِ اولین
 * ورود می‌بینه (رجوع کن به CLAUDE.md، بسته‌ی دومِ پولکی‌سازی).
 */
val accountIconChoices: List<Pair<String, ImageVector>> = listOf(
    "cash" to Icons.Filled.Payments,
    "wallet" to Icons.Filled.AccountBalanceWallet,
    "card" to Icons.Filled.CreditCard,
    "savings" to Icons.Filled.Savings,
    "bank" to Icons.Filled.AccountBalance,
    "mobile" to Icons.Filled.PhoneAndroid,
    "exchange" to Icons.Filled.CurrencyExchange,
    "store" to Icons.Filled.Store,
    "gold" to Icons.Filled.Diamond,
)

/** پیش‌فرضِ حساب‌کتابِ غیربانکی، وقتی کاربر آیکونی انتخاب نکرده. */
const val DEFAULT_ACCOUNT_ICON_KEY = "cash"

fun accountIconForKey(key: String?): ImageVector =
    accountIconChoices.firstOrNull { it.first == key }?.second ?: Icons.Filled.Payments
