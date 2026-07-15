package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.incomeDataStore by preferencesDataStore(name = "income_prefs")

/** درآمد ماهانه‌ی دلخواه کاربر - فقط برای تحلیل «چقدر از درآمدت صرف اقساط می‌شه» تو داشبورد
 * «وام‌های من» استفاده می‌شه (رجوع کن به MyLoansScreen.DashboardSummary)؛ جایی دیگه به کار نمی‌ره. */
class IncomePrefs(private val context: Context) {
    private object Keys {
        val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
    }

    val monthlyIncome: Flow<Double> = context.incomeDataStore.data.map { it[Keys.MONTHLY_INCOME] ?: 0.0 }

    suspend fun setMonthlyIncome(value: Double) {
        context.incomeDataStore.edit { it[Keys.MONTHLY_INCOME] = value }
    }
}
