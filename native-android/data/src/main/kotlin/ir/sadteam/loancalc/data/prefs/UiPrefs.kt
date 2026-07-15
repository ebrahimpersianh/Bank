package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPrefsDataStore by preferencesDataStore(name = "ui_prefs")

/** پورت toggleTheme/اندازه فونت تو www/index.html (که تو localStorage ذخیره می‌شن). */
class UiPrefs(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val LAST_AUTO_BACKUP_AT = stringPreferencesKey("last_auto_backup_at")
    }

    // پیش‌فرض روشن/سفید (به‌درخواست کاربر «تم اصلی برنامه سفید باشه») - کاربری که قبلاً دستی
    // تم رو عوض کرده باشه، انتخابش تو DataStore می‌مونه؛ فقط نصب‌های تازه پیش‌فرض روشن می‌گیرن.
    val darkTheme: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.DARK_THEME] ?: false }

    suspend fun setDarkTheme(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.DARK_THEME] = value }
    }

    /** پورت .app.fs-small/fs-medium/fs-large (zoom:0.9/1/1.15) - پیش‌فرض «متوسط» (۱). */
    val fontScale: Flow<Float> = context.uiPrefsDataStore.data.map { it[Keys.FONT_SCALE] ?: 1f }

    suspend fun setFontScale(value: Float) {
        context.uiPrefsDataStore.edit { it[Keys.FONT_SCALE] = value }
    }

    /** پورت یادآوری سررسید (وب هنوز نداره) - پیش‌فرض خاموش چون روشن‌کردنش رو Android 13+ نیاز به
     * مجوز POST_NOTIFICATIONS داره که فقط با تعامل کاربر (سوییچ تو تنظیمات) درخواست می‌شه. */
    val notificationsEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: false }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    /** پورت «پشتیبان‌گیری خودکار روزانه» اپ رقیب - رجوع کن به AutoBackupWorker/AutoBackupScheduler. */
    val autoBackupEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.AUTO_BACKUP_ENABLED] ?: false }

    suspend fun setAutoBackupEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = value }
    }

    val lastAutoBackupAt: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.LAST_AUTO_BACKUP_AT] }

    suspend fun setLastAutoBackupAt(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.LAST_AUTO_BACKUP_AT] = value }
    }
}
