package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.db.WealthSnapshotDao
import ir.sadteam.loancalc.data.db.WealthSnapshotEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * دفترِ **عکسِ روزانه‌ی دارایی** - رجوع کن به [WealthSnapshotEntity].
 *
 * تنها نقطه‌ی نوشتن [record] است و کلیدش خودِ روز، پس چند بار صدازدنش در یک روز فقط
 * همان ردیف را تازه می‌کند (نه ردیفِ تکراری، نه شمارنده‌ی زمانی).
 */
class WealthSnapshotRepository(private val dao: WealthSnapshotDao) {
    fun observe(): Flow<List<WealthSnapshotEntity>> = dao.observeAll()

    /**
     * عکسِ امروز را می‌نویسد (یا تازه می‌کند).
     *
     * ⚠️ با داراییِ صفر هم نوشته می‌شود: «آن روز چیزی نداشتم» خودش یک نقطه‌ی معتبرِ
     * نمودار است، و نبودِ ردیف با صفربودن فرق دارد.
     */
    suspend fun record(today: PersianDate, cashRial: Double, assetsRial: Double) {
        dao.upsert(
            WealthSnapshotEntity(
                dateKey = keyOf(today),
                cashRial = cashRial,
                assetsRial = assetsRial,
                createdAt = isoNow(),
            ),
        )
        // بیش از یک سال نگه نمی‌داریم - نموداری که نشانش بدهد وجود ندارد.
        dao.purgeBefore(keyOf(PersianDate(today.y - 1, today.m, today.d)))
    }

    suspend fun clearLocal() = dao.clear()

    companion object {
        /** رقمِ لاتین و دو رقمی، تا مرتب‌سازیِ رشته‌ای با ترتیبِ تاریخ یکی باشد. */
        fun keyOf(date: PersianDate): String =
            String.format(Locale.US, "%04d-%02d-%02d", date.y, date.m, date.d)
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
