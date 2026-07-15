package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.data.db.IncomeDao
import ir.sadteam.loancalc.data.db.IncomeEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * پورت مفهومی «منابع درآمد چندگانه» اپ رقیب (VAMMAN) - جایگزین `IncomePrefs` قبلی (یه عدد تکی تو
 * DataStore) که فقط یه منبع درآمد رو پشتیبانی می‌کرد. هر منبع یه لیبل (مثلاً «حقوق»، «کار دوم»)،
 * مبلغ، و نوع (ثابت/متغیر) داره؛ جمع کل برای تحلیل «چقدر از درآمد صرف اقساط می‌شه» تو DashboardSummary
 * استفاده می‌شه.
 */
class IncomeRepository(private val incomeDao: IncomeDao) {
    fun observeIncomes(): Flow<List<IncomeEntity>> = incomeDao.observeAll()

    suspend fun addIncome(label: String, amount: Double, type: IncomeType) {
        incomeDao.upsert(
            IncomeEntity(
                id = System.currentTimeMillis(),
                label = label,
                amount = amount,
                type = type.name,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.delete(income)
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
