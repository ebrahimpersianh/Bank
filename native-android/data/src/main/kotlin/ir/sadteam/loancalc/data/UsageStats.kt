package ir.sadteam.loancalc.data

import android.content.Context
import ir.sadteam.loancalc.data.network.ApiClient
import ir.sadteam.loancalc.data.network.UsageEventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * آمارِ استفاده‌ی **بی‌نام** روی سرورِ خودمان (تصمیمِ کاربر، ۶ مهر: به‌جای Firebase).
 *
 * فقط نامِ یکی از پنج رویدادِ ثابت فرستاده می‌شود - **هیچ مبلغ، عنوان، نامِ حساب، شماره‌کارت،
 * موجودی، شماره‌ی موبایل یا شناسه‌ای**؛ حتی توکنِ ورود هم فرستاده نمی‌شود. سرور فقط شمارشِ
 * روزانه نگه می‌دارد. شکستِ شبکه بی‌صدا نادیده گرفته می‌شود. کلیدِ خاموش در تنظیمات.
 */
object UsageStats {
    const val FIRST_OPEN = "first_open"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val TRANSACTION_CREATED = "transaction_created"
    const val ACCOUNT_CREATED = "account_created"
    const val REPORT_VIEWED = "report_viewed"

    private const val PREFS = "usage_stats"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_FIRST_OPEN_SENT = "first_open_sent"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val api by lazy { ApiClient.create() }
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val p = prefs() ?: return
        if (!p.getBoolean(KEY_FIRST_OPEN_SENT, false)) {
            p.edit().putBoolean(KEY_FIRST_OPEN_SENT, true).apply()
            track(FIRST_OPEN)
        }
    }

    fun isEnabled(): Boolean = prefs()?.getBoolean(KEY_ENABLED, true) ?: true

    fun setEnabled(enabled: Boolean) {
        prefs()?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
    }

    fun track(name: String) {
        if (!isEnabled()) return
        scope.launch { runCatching { api.trackEvent(UsageEventRequest(name)) } }
    }

    private fun prefs() = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
