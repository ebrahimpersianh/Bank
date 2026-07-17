package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.CalculationHistoryDao
import ir.sadteam.loancalc.data.db.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * تاریخچه‌ی محاسبات (وام بانکی/سقف وام/سود سپرده) - رجوع کن به CalculationHistoryEntity.
 */
class CalculationHistoryRepository(private val dao: CalculationHistoryDao) {
    fun observeAll(): Flow<List<CalculationHistoryEntity>> = dao.observeAll()

    suspend fun log(kind: String, title: String, summary: String, amount: Double) {
        dao.insert(
            CalculationHistoryEntity(
                id = System.currentTimeMillis(),
                kind = kind,
                title = title,
                summary = summary,
                amount = amount,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun delete(entry: CalculationHistoryEntity) = dao.delete(entry)

    suspend fun clearAll() = dao.clearAll()

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
