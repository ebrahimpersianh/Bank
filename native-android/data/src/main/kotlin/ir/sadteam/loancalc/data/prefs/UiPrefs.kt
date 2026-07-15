package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPrefsDataStore by preferencesDataStore(name = "ui_prefs")

/** پورت toggleTheme/اندازه فونت تو www/index.html (که تو localStorage ذخیره می‌شن). */
class UiPrefs(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    val darkTheme: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.DARK_THEME] ?: true }

    suspend fun setDarkTheme(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.DARK_THEME] = value }
    }

    /** پورت .app.fs-small/fs-medium/fs-large (zoom:0.9/1/1.15) - پیش‌فرض «متوسط» (۱). */
    val fontScale: Flow<Float> = context.uiPrefsDataStore.data.map { it[Keys.FONT_SCALE] ?: 1f }

    suspend fun setFontScale(value: Float) {
        context.uiPrefsDataStore.edit { it[Keys.FONT_SCALE] = value }
    }
}
