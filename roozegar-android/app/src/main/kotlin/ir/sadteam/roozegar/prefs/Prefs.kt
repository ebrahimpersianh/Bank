package ir.sadteam.roozegar.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "settings")

/** تنظیمات کاربر - پیش‌فرض‌ها طوری‌ان که اپ بدون هیچ کاری «کامل» کار کنه (اعلان و افکت روشن). */
data class Settings(
    val onboardingDone: Boolean = false,
    val notifEnabled: Boolean = true,
    /** حالت مینیمال: فقط تاریخ شمسی تو اعلان، بدون خط دوم. */
    val notifMinimal: Boolean = false,
    val notifShowGregorian: Boolean = true,
    val notifShowHijri: Boolean = true,
    val notifShowOccasion: Boolean = true,
    val effectsEnabled: Boolean = true,
)

object Prefs {
    private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    private val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    private val NOTIF_MINIMAL = booleanPreferencesKey("notif_minimal")
    private val NOTIF_SHOW_GREGORIAN = booleanPreferencesKey("notif_show_gregorian")
    private val NOTIF_SHOW_HIJRI = booleanPreferencesKey("notif_show_hijri")
    private val NOTIF_SHOW_OCCASION = booleanPreferencesKey("notif_show_occasion")
    private val EFFECTS_ENABLED = booleanPreferencesKey("effects_enabled")

    fun flow(context: Context): Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            onboardingDone = p[ONBOARDING_DONE] ?: false,
            notifEnabled = p[NOTIF_ENABLED] ?: true,
            notifMinimal = p[NOTIF_MINIMAL] ?: false,
            notifShowGregorian = p[NOTIF_SHOW_GREGORIAN] ?: true,
            notifShowHijri = p[NOTIF_SHOW_HIJRI] ?: true,
            notifShowOccasion = p[NOTIF_SHOW_OCCASION] ?: true,
            effectsEnabled = p[EFFECTS_ENABLED] ?: true,
        )
    }

    /** خوندن همزمان (blocking) - فقط برای BroadcastReceiver/ویجت که کوروتین‌محور نیستن؛ DataStore
     * محلی و کوچیکه، این خوندن چند میلی‌ثانیه بیشتر نیست. */
    fun snapshot(context: Context): Settings = runBlocking { flow(context).first() }

    suspend fun setOnboardingDone(context: Context) =
        context.dataStore.edit { it[ONBOARDING_DONE] = true }

    suspend fun setNotifEnabled(context: Context, value: Boolean) =
        context.dataStore.edit { it[NOTIF_ENABLED] = value }

    suspend fun setNotifMinimal(context: Context, value: Boolean) =
        context.dataStore.edit { it[NOTIF_MINIMAL] = value }

    suspend fun setNotifShowGregorian(context: Context, value: Boolean) =
        context.dataStore.edit { it[NOTIF_SHOW_GREGORIAN] = value }

    suspend fun setNotifShowHijri(context: Context, value: Boolean) =
        context.dataStore.edit { it[NOTIF_SHOW_HIJRI] = value }

    suspend fun setNotifShowOccasion(context: Context, value: Boolean) =
        context.dataStore.edit { it[NOTIF_SHOW_OCCASION] = value }

    suspend fun setEffectsEnabled(context: Context, value: Boolean) =
        context.dataStore.edit { it[EFFECTS_ENABLED] = value }
}
