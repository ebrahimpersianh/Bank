package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
    }

    val authToken: Flow<String?> = context.authDataStore.data.map { it[Keys.TOKEN] }
    val phone: Flow<String?> = context.authDataStore.data.map { it[Keys.PHONE] }
    val subscribed: Flow<Boolean> = context.authDataStore.data.map { it[Keys.SUBSCRIBED] ?: false }
    val guestMode: Flow<Boolean> = context.authDataStore.data.map { it[Keys.GUEST_MODE] ?: false }

    suspend fun saveSession(token: String, phone: String, subscribed: Boolean) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.PHONE] = phone
            prefs[Keys.SUBSCRIBED] = subscribed
        }
    }

    suspend fun setSubscribed(subscribed: Boolean) {
        context.authDataStore.edit { it[Keys.SUBSCRIBED] = subscribed }
    }

    suspend fun setGuestMode(value: Boolean) {
        context.authDataStore.edit { it[Keys.GUEST_MODE] = value }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.PHONE)
            prefs[Keys.SUBSCRIBED] = false
        }
    }
}
