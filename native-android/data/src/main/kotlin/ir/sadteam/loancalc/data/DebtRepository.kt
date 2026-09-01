package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.core.guessCounterparty
import ir.sadteam.loancalc.data.db.CounterpartyDao
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DebtDao
import ir.sadteam.loancalc.data.db.DebtEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/** ماژولِ «طلب و بدهی» - هر [CounterpartyEntity] چندتا ردیفِ [DebtEntity] داره؛ مانده‌ی طلب/بدهیِ
 * هر طرف‌حساب همیشه از رو جمعِ ردیف‌های تسویه‌نشده‌ش محاسبه می‌شه (رجوع کن به [netBalance]), نه یه
 * فیلدِ جداگونه که ممکنه از واقعیت جا بمونه - هم‌الگو با AccountRepository.currentBalance. */
class DebtRepository(
    private val counterpartyDao: CounterpartyDao,
    private val debtDao: DebtDao,
) {
    /** پنج رنگِ چرخشیِ `AvatarColor` (تو `ui/components/Avatar.kt`) - `NEUTRAL` عمداً بیرونه چون
     * حالتِ ویژه‌ی خودشه، نه یه رنگِ عادیِ قابلِ‌چرخش. */
    private val avatarColorNames = listOf("GREEN", "PURPLE", "BLUE", "ORANGE", "RED")

    fun observeCounterparties(): Flow<List<CounterpartyEntity>> = counterpartyDao.observeAll()
    fun observeDebts(): Flow<List<DebtEntity>> = debtDao.observeAll()
    fun observeDebtsForCounterparty(counterpartyId: Long): Flow<List<DebtEntity>> =
        debtDao.observeForCounterparty(counterpartyId)

    /**
     * رنگِ آواتار **قطعی از رو نامه** تعیین می‌شه، نه تصادفی/دستی (جوابِ سوالِ ۱۱ی
     * design/MESSAGE-round4-to-design.md) - همون نام همیشه همون رنگ رو می‌گیره.
     */
    suspend fun addCounterparty(name: String, phone: String? = null, id: Long = System.currentTimeMillis()): Long {
        val color = avatarColorNames[abs(name.hashCode()) % avatarColorNames.size]
        counterpartyDao.upsert(
            CounterpartyEntity(id = id, name = name, createdAt = isoNow(), phone = phone, avatarColor = color),
        )
        return id
    }

    suspend fun updateCounterparty(counterparty: CounterpartyEntity) {
        counterpartyDao.upsert(counterparty)
    }

    /**
     * حدسِ خودکارِ طرفِ‌حساب از رو یه نامِ خام (چک/وامِ قدیمی که `counterpartyId` ندارن) - تطبیقِ
     * قطعی به طرفِ‌حسابِ موجود وصل می‌شه؛ مبهم/بی‌نام یه طرفِ‌حسابِ موقتِ «نامشخص» می‌گیره (جوابِ
     * سوالِ ۶). صدا زدنِ دوباره‌ش برای «نامشخص»های تکراری همون یکی رو برمی‌گردونه، ردیفِ جدید نمی‌سازه.
     */
    suspend fun guessOrCreateCounterparty(rawName: String?): Long {
        val existing = counterpartyDao.getAll()
        guessCounterparty(rawName, existing) { it.name }?.let { return it.id }
        val unknown = existing.firstOrNull { it.name == UNKNOWN_COUNTERPARTY_NAME }
        if (unknown != null) return unknown.id
        return addCounterparty(UNKNOWN_COUNTERPARTY_NAME)
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

    companion object {
        /** طرفِ‌حسابِ موقتِ چک/وامِ قدیمیِ مبهم/بی‌نام - کاربر بعداً دستی اصلاحش می‌کنه (سوالِ ۶). */
        const val UNKNOWN_COUNTERPARTY_NAME = "نامشخص"
    }
}
