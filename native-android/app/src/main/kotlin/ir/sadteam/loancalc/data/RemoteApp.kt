package ir.sadteam.loancalc.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson

data class RemotePromo(val title: String = "", val text: String = "", val until: String? = null)
data class RemoteFaq(val q: String = "", val a: String = "")
data class RemoteSurvey(val id: String = "", val question: String = "", val options: List<String> = emptyList())

/**
 * ⚙️ **تنظیماتِ برنامه از روی سرور** (۸ مهر، خواسته‌ی کاربر: «که هر بار بیخودی آپدیت نکنم»).
 * همه‌ی فیلدها اختیاری‌اند؛ خالی/`null` = رفتارِ داخلیِ برنامه. از «ادمین ← تنظیماتِ از راهِ دور» عوض می‌شود.
 */
data class RemoteAppConfig(
    // نسخه‌ی رایگان
    val freeTx: Int? = null,
    val freeAccounts: Int? = null,
    val freeLoans: Int? = null,
    val freeCheques: Int? = null,
    /** شناسه‌ی میان‌برهایی که موقتاً پنهان می‌شوند (خاموش کردنِ اضطراری). */
    val disabled: List<String> = emptyList(),
    /** نسخه‌ی کمتر از این = پنجره‌ی «باید آپدیت کنی» که بسته نمی‌شود. */
    val minVersion: Int? = null,
    val minVersionText: String? = null,
    /** نوارِ تخفیفِ بالای صفحه‌ی اشتراک؛ `until` = کلیدِ جلالیِ ۱۴۰۵-۰۷-۱۵ (بعدش خودش پنهان می‌شود). */
    val promo: RemotePromo? = null,
    /** دو جمله‌ی آزمایشِ A/B زیرِ عنوانِ اشتراک. */
    val paywallA: String? = null,
    val paywallB: String? = null,
    // پیامکِ بانکی
    val smsDeposit: List<String> = emptyList(),
    val smsWithdrawal: List<String> = emptyList(),
    val smsIgnore: List<String> = emptyList(),
    /** کلیدواژه → دسته. */
    val catWithdrawal: Map<String, String> = emptyMap(),
    val catDeposit: Map<String, String> = emptyMap(),
    /** شش رقمِ اولِ کارت → نامِ بانک. */
    val bins: Map<String, String> = emptyMap(),
    // سکه و امتیاز
    val dailyCoins: Int? = null,
    val weekCoins: Int? = null,
    val rateFirst: Int? = null,
    val rateEvery: Int? = null,
    // متن‌ها
    val comeBackTitle: String? = null,
    val comeBackText: String? = null,
    val faq: List<RemoteFaq> = emptyList(),
    val survey: RemoteSurvey? = null,
)

object RemoteApp {
    private val gson = Gson()
    private const val PREFS = "remote_config"

    var config by mutableStateOf(RemoteAppConfig())
        private set

    fun loadCached(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("app", null)?.let { apply(it) }
    }

    fun update(context: Context, json: String) {
        if (apply(json)) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("app", json).apply()
    }

    fun toJson(c: RemoteAppConfig): String = gson.toJson(c)

    @Suppress("USELESS_ELVIS", "SENSELESS_COMPARISON")
    private fun apply(json: String): Boolean = runCatching {
        val r = gson.fromJson(json, RemoteAppConfig::class.java) ?: return false
        // Gson فیلدِ نیامده را null می‌گذارد حتی برای نوعِ غیرِ-null؛ یکدست می‌کنیم.
        val c = r.copy(
            disabled = r.disabled ?: emptyList(),
            smsDeposit = r.smsDeposit ?: emptyList(),
            smsWithdrawal = r.smsWithdrawal ?: emptyList(),
            smsIgnore = r.smsIgnore ?: emptyList(),
            catWithdrawal = r.catWithdrawal ?: emptyMap(),
            catDeposit = r.catDeposit ?: emptyMap(),
            bins = r.bins ?: emptyMap(),
            faq = r.faq ?: emptyList(),
        )
        config = c
        // موتورهای خالص (`:core`/`:data`) فقط مقدار می‌گیرند.
        ir.sadteam.loancalc.core.BankSmsParser.remoteDeposit = c.smsDeposit
        ir.sadteam.loancalc.core.BankSmsParser.remoteWithdrawal = c.smsWithdrawal
        ir.sadteam.loancalc.core.BankSmsParser.remoteIgnore = c.smsIgnore
        ir.sadteam.loancalc.core.MerchantCategoryGuesser.remoteWithdrawal = c.catWithdrawal
        ir.sadteam.loancalc.core.MerchantCategoryGuesser.remoteDeposit = c.catDeposit
        GamificationRepository.Reward.DAILY_OPEN = c.dailyCoins?.coerceIn(0, 500) ?: 5
        GamificationRepository.Reward.DAILY_OPEN_WEEK = c.weekCoins?.coerceIn(0, 2000) ?: 30
        true
    }.getOrDefault(false)

    /** میان‌بر خاموش است؟ */
    fun isDisabled(id: String): Boolean = id in config.disabled
}
