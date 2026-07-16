package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
        val TRIAL_ENDS_AT = longPreferencesKey("trial_ends_at")
    }

    val authToken: Flow<String?> = context.authDataStore.data.map { it[Keys.TOKEN] }
    val phone: Flow<String?> = context.authDataStore.data.map { it[Keys.PHONE] }
    val subscribed: Flow<Boolean> = context.authDataStore.data.map { it[Keys.SUBSCRIBED] ?: false }
    val guestMode: Flow<Boolean> = context.authDataStore.data.map { it[Keys.GUEST_MODE] ?: false }

    /** پایانِ دوره‌ی آزمایشیِ ۷روزه (میلی‌ثانیه‌ی epoch، از سرور - رجوع کن به MeResponse/
     * VerifyOtpResponse.trialEndsAt) - فقط برای نمایشِ «چند روز مونده»، منبع حقیقتِ واقعی
     * (اینکه دسترسی مجازه یا نه) همیشه فیلد subscribed خودِ سرورـه، نه این تاریخ. */
    val trialEndsAt: Flow<Long?> = context.authDataStore.data.map { it[Keys.TRIAL_ENDS_AT] }

    suspend fun setTrialEndsAt(value: Long?) {
        context.authDataStore.edit { prefs ->
            if (value != null) prefs[Keys.TRIAL_ENDS_AT] = value else prefs.remove(Keys.TRIAL_ENDS_AT)
        }
    }

    /** پورت صفحه‌ی خوش‌آمد امکانات (رایگان/اشتراکی) اپ رقیب (VAMMAN) - فقط یه‌بار تو کل عمر نصب
     * نشون داده می‌شه، درست بعد از گیت مجوز و قبل از گیت ورود/مهمان. */
    val benefitsSeen: Flow<Boolean> = context.authDataStore.data.map { it[Keys.BENEFITS_SEEN] ?: false }

    suspend fun saveSession(token: String, phone: String, subscribed: Boolean, trialEndsAt: Long? = null) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.PHONE] = phone
            prefs[Keys.SUBSCRIBED] = subscribed
            if (trialEndsAt != null) prefs[Keys.TRIAL_ENDS_AT] = trialEndsAt else prefs.remove(Keys.TRIAL_ENDS_AT)
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
