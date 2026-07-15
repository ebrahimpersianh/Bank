package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.securityDataStore by preferencesDataStore(name = "security_prefs")

/** قفل امنیتی PIN+اثر انگشت (برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست صریح جدید کاربر - رجوع
 * کن به CLAUDE.md). فقط هش PIN ذخیره می‌شه (SHA-256، رجوع کن به core/Security.kt)، هیچ‌وقت خودِ PIN.
 * `AUTO_LOCK_TIMEOUT_MINUTES` (پیش‌فرض ۰ = بی‌درنگ) مدت زمانیه که اپ بعد از رفتن به پس‌زمینه، بدون
 * قفل شدن دوباره، تحمل می‌کنه - رجوع کن به AppLockViewModel. */
class SecurityPrefs(private val context: Context) {
    private object Keys {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("auto_lock_timeout_minutes")
    }

    val pinHash: Flow<String?> = context.securityDataStore.data.map { it[Keys.PIN_HASH] }
    val biometricEnabled: Flow<Boolean> = context.securityDataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }
    val autoLockTimeoutMinutes: Flow<Int> =
        context.securityDataStore.data.map { it[Keys.AUTO_LOCK_TIMEOUT_MINUTES] ?: 0 }

    suspend fun setPinHash(hash: String?) {
        context.securityDataStore.edit { prefs ->
            if (hash == null) prefs.remove(Keys.PIN_HASH) else prefs[Keys.PIN_HASH] = hash
        }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        context.securityDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = value }
    }

    suspend fun setAutoLockTimeoutMinutes(value: Int) {
        context.securityDataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT_MINUTES] = value }
    }
}
