package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

/**
 * معادل بومی کلیدهای localStorage اپ وب فعلی: auth_token / user_account (phone) /
 * subscribed / guest_mode (رجوع کن به www/index.html برای رفتار دقیق هرکدوم).
 */
class AuthPrefs(private val context: Context) {
    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val PHONE = stringPreferencesKey("phone")
        val SUBSCRIBED = booleanPreferencesKey("subscribed")
        val GUEST_MODE = booleanPreferencesKey("guest_mode")
        val BENEFITS_SEEN = booleanPreferencesKey("benefits_seen")
        val TRIAL_DAYS_LEFT = intPreferencesKey("trial_days_left")
    }

    val authToken: Flow<String?> = context.authDataStore.data.map { it[Keys.TOKEN] }
    val phone: Flow<String?> = context.authDataStore.data.map { it[Keys.PHONE] }
    val subscribed: Flow<Boolean> = context.authDataStore.data.map { it[Keys.SUBSCRIBED] ?: false }
    val guestMode: Flow<Boolean> = context.authDataStore.data.map { it[Keys.GUEST_MODE] ?: false }

    /** چند روز از دوره‌ی آزمایشیِ ۷روزه مونده (از سرور - رجوع کن به MeResponse/
     * VerifyOtpResponse.trialDaysLeft) - قبلاً یه timestamp خام بود که خودِ کلاینت با ساعتِ گوشی
     * «چند روز مونده» رو حساب می‌کرد (وابسته به دستکاری‌پذیرِ ساعتِ گوشی برای نمایش، هرچند خودِ
     * subscribed همیشه واقعی و سمت سرور بود)؛ حالا خودِ عددِ نهایی از سرور میاد، هیچ محاسبه‌ای
     * سمت کلاینت لازم نیست. فقط برای نمایشه، منبع حقیقتِ واقعیِ دسترسی همیشه فیلد subscribed ـه. */
    val trialDaysLeft: Flow<Int?> = context.authDataStore.data.map { it[Keys.TRIAL_DAYS_LEFT] }

    suspend fun setTrialDaysLeft(value: Int?) {
        context.authDataStore.edit { prefs ->
            if (value != null) prefs[Keys.TRIAL_DAYS_LEFT] = value else prefs.remove(Keys.TRIAL_DAYS_LEFT)
        }
    }

    /** پورت صفحه‌ی خوش‌آمد امکانات (رایگان/اشتراکی) اپ رقیب (VAMMAN) - فقط یه‌بار تو کل عمر نصب
     * نشون داده می‌شه، درست بعد از گیت مجوز و قبل از گیت ورود/مهمان. */
    val benefitsSeen: Flow<Boolean> = context.authDataStore.data.map { it[Keys.BENEFITS_SEEN] ?: false }

    suspend fun saveSession(token: String, phone: String, subscribed: Boolean, trialDaysLeft: Int? = null) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.PHONE] = phone
            prefs[Keys.SUBSCRIBED] = subscribed
            if (trialDaysLeft != null) prefs[Keys.TRIAL_DAYS_LEFT] = trialDaysLeft else prefs.remove(Keys.TRIAL_DAYS_LEFT)
        }
    }

    suspend fun setSubscribed(subscribed: Boolean) {
        context.authDataStore.edit { it[Keys.SUBSCRIBED] = subscribed }
    }

    suspend fun setGuestMode(value: Boolean) {
        context.authDataStore.edit { it[Keys.GUEST_MODE] = value }
    }

    suspend fun setBenefitsSeen(value: Boolean) {
        context.authDataStore.edit { it[Keys.BENEFITS_SEEN] = value }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.PHONE)
            prefs[Keys.SUBSCRIBED] = false
        }
    }
}
