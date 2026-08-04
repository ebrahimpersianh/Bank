package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.data.db.CounterpartyDao
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DebtDao
import ir.sadteam.loancalc.data.db.DebtEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** ماژولِ «طلب و بدهی» - هر [CounterpartyEntity] چندتا ردیفِ [DebtEntity] داره؛ مانده‌ی طلب/بدهیِ
 * هر طرف‌حساب همیشه از رو جمعِ ردیف‌های تسویه‌نشده‌ش محاسبه می‌شه (رجوع کن به [netBalance]), نه یه
 * فیلدِ جداگونه که ممکنه از واقعیت جا بمونه - هم‌الگو با AccountRepository.currentBalance. */
class DebtRepository(
    private val counterpartyDao: CounterpartyDao,
    private val debtDao: DebtDao,
) {
    fun observeCounterparties(): Flow<List<CounterpartyEntity>> = counterpartyDao.observeAll()
    fun observeDebts(): Flow<List<DebtEntity>> = debtDao.observeAll()
    fun observeDebtsForCounterparty(counterpartyId: Long): Flow<List<DebtEntity>> =
        debtDao.observeForCounterparty(counterpartyId)

    suspend fun addCounterparty(name: String) {
        counterpartyDao.upsert(CounterpartyEntity(id = System.currentTimeMillis(), name = name, createdAt = isoNow()))
    }

    suspend fun deleteCounterparty(counterparty: CounterpartyEntity) {
        counterpartyDao.delete(counterparty)
        debtDao.deleteForCounterparty(counterparty.id)
    }

    suspend fun addDebt(
        counterpartyId: Long,
        amount: Double,
        type: DebtType,
        description: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        debtDao.upsert(
            DebtEntity(
                id = System.currentTimeMillis(),
                counterpartyId = counterpartyId,
                amount = amount,
                type = type.name,
                description = description,
                year = year,
                month = month,
                day = day,
                settled = false,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun setSettled(debt: DebtEntity, settled: Boolean) {
        debtDao.upsert(debt.copy(settled = settled))
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.delete(debt)
    }

    /** مانده‌ی خالصِ یه طرف‌حساب: مثبت یعنی در مجموع بهم بدهکاره (طلبِ من)، منفی یعنی من بهش
     * بدهکارم - فقط ردیف‌های تسویه‌نشده حساب می‌شن. */
    fun netBalance(counterpartyId: Long, debts: List<DebtEntity>): Double =
        debts.filter { it.counterpartyId == counterpartyId && !it.settled }
            .sumOf { if (it.type == DebtType.OWED_TO_ME.name) it.amount else -it.amount }

    suspend fun clearLocal() {
        counterpartyDao.clear()
        debtDao.clear()
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
