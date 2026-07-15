package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPrefsDataStore by preferencesDataStore(name = "ui_prefs")

/** پورت toggleTheme تو www/index.html (که تو localStorage ذخیره می‌شه) - پیش‌فرض تم تیره‌ست. */
class UiPrefs(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val darkTheme: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.DARK_THEME] ?: true }

    suspend fun setDarkTheme(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.DARK_THEME] = value }
    }
}
