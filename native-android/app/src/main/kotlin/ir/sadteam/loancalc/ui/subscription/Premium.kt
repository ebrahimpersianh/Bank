package ir.sadteam.loancalc.ui.subscription

import ir.sadteam.loancalc.ui.update.ExpirySheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import ir.sadteam.loancalc.data.UsageStats
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakAlertDialog

/**
 * مرزِ رایگان/اشتراک (تصمیمِ کاربر، ۷ مهر). **هیچ داده‌ای پاک یا پنهان نمی‌شود** - فقط افزودنِ
 * چیزِ تازه در بخش‌های اشتراکی قفل است. پیش‌فرض `true` تا پیش‌نمایش‌ها/تست‌ها قفل نشوند؛
 * مقدارِ واقعی را `MainAppContent` از `AuthViewModel.subscribed` می‌دهد.
 */
val LocalIsPremium = staticCompositionLocalOf { true }

object FreeLimits {
    const val ACCOUNTS = 1
    const val TX_PER_MONTH = 30
    const val LOANS = 1
    const val CHEQUES = 1
}

/** درخواستِ بازکردنِ پنجره‌ی «این بخش مالِ اشتراک است» از هر جای برنامه. */
object PremiumPaywall {
    var feature by mutableStateOf<String?>(null)
    var showPlans by mutableStateOf(false)

    fun ask(key: String, label: String) {
        UsageStats.action("paywall_gate_$key")
        feature = label
    }
}

/** اگر اشتراک دارد [action] اجرا می‌شود، وگرنه پنجره‌ی اشتراک با نامِ [label]. */
@Composable
fun premiumGuard(): (key: String, label: String, action: () -> Unit) -> Unit {
    val premium = LocalIsPremium.current
    return { key, label, action -> if (premium) action() else PremiumPaywall.ask(key, label) }
}

/**
 * برای بالای فرم‌های «افزودن»: اگر اشتراک ندارد و [blocked] است، پنجره‌ی اشتراک باز و فرم
 * بسته می‌شود. یک نقطه به‌جای قفل‌کردنِ تک‌تکِ دکمه‌هایی که به این فرم می‌رسند.
 */
@Composable
fun PremiumBlock(blocked: Boolean, key: String, label: String, onBlocked: () -> Unit) {
    val premium = LocalIsPremium.current
    if (!premium && blocked) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            PremiumPaywall.ask(key, label)
            onBlocked()
        }
    }
}

/**
 * هشدارِ نزدیکیِ پایانِ اشتراک (خواسته‌ی کاربر، ۷ مهر): از ۳ روز مانده، روزی یک بار یک پنجره.
 * [daysLeft] = روزهای باقی‌مانده از اشتراک یا دوره‌ی هدیه؛ `null` = نامعلوم/بی‌انقضا.
 */
@Composable
fun androidx.compose.foundation.layout.BoxScope.SubscriptionExpiryReminder(daysLeft: Int?) {
    if (daysLeft == null || daysLeft !in 0..3) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("sub_expiry_reminder", android.content.Context.MODE_PRIVATE) }
    val today = remember { (System.currentTimeMillis() / 86_400_000L).toInt() }
    var show by remember { mutableStateOf(prefs.getInt("last_day", -1) != today) }
    val close = {
        prefs.edit().putInt("last_day", today).apply()
        show = false
    }
    ExpirySheet(
        visible = show,
        daysLeft = daysLeft,
        onRenew = {
            close()
            UsageStats.action("expiry_reminder_renew")
            PremiumPaywall.showPlans = true
        },
        onDismiss = close,
    )
}

@Composable
fun PremiumPaywallHost() {
    PremiumPaywall.feature?.let { feature ->
        JibakAlertDialog(
            onDismissRequest = { PremiumPaywall.feature = null },
            title = { Text("این بخش مالِ اشتراکه") },
            text = {
                Text(
                    "«$feature» با اشتراکِ جیبک باز می‌شه. اطلاعاتی که قبلاً ثبت کردی سرِ جاشه و " +
                        "همیشه می‌بینیش - فقط برای افزودنِ مورد تازه اشتراک لازمه.",
                )
            },
            confirmButton = {
                GradientButton(onClick = {
                    PremiumPaywall.feature = null
                    PremiumPaywall.showPlans = true
                }) { Text("دیدنِ اشتراک‌ها") }
            },
            dismissButton = {
                GradientButton(onClick = { PremiumPaywall.feature = null }, variant = AppButtonVariant.SECONDARY) { Text("بعداً") }
            },
        )
    }
    if (PremiumPaywall.showPlans) {
        FullScreenDialog(onDismissRequest = { PremiumPaywall.showPlans = false }) {
            SubscriptionScreen(
                onBack = { PremiumPaywall.showPlans = false },
                onSubscribed = { PremiumPaywall.showPlans = false },
            )
        }
    }
}
