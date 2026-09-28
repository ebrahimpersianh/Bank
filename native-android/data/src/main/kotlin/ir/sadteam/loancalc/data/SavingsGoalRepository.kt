package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.SavingsGoalDao
import ir.sadteam.loancalc.data.db.SavingsGoalEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow

/**
 * هدفِ پس‌انداز - رجوع کن به [SavingsGoalEntity] برای دو تصمیمِ بنیادی (هدف پول جابه‌جا
 * نمی‌کند، و `savedRial` فقط از این‌جا نوشته می‌شود).
 */
class SavingsGoalRepository(private val dao: SavingsGoalDao) {
    fun observeGoals(): Flow<List<SavingsGoalEntity>> = dao.observeAll()

    suspend fun getGoals(): List<SavingsGoalEntity> = dao.getAll()

    suspend fun addGoal(
        title: String,
        targetRial: Double,
        iconKey: String = "star",
        deadlineYear: Int? = null,
        deadlineMonth: Int? = null,
        deadlineDay: Int? = null,
    ) {
        dao.upsert(
            SavingsGoalEntity(
                id = System.currentTimeMillis(),
                title = title,
                targetRial = targetRial,
                iconKey = iconKey,
                deadlineYear = deadlineYear,
                deadlineMonth = deadlineMonth,
                deadlineDay = deadlineDay,
                createdAt = isoNow(),
            ),
        )
    }

    /** ویرایشِ مشخصات. `savedRial` عمداً این‌جا دست‌نخورده می‌مانَد - راهش [contribute] است. */
    suspend fun updateGoal(goal: SavingsGoalEntity) {
        dao.upsert(goal.copy(achievedAt = achievedStamp(goal)))
    }

    /**
     * واریز به هدف (یا برداشت با عددِ منفی) - **تنها نقطه‌ی نوشتنِ `savedRial`**.
     *
     * زیرِ صفر نمی‌رود: «۵۰ هزار برداشتم» روی هدفی که ۲۰ هزار دارد یعنی صفر، نه منفیِ ۳۰ -
     * پس‌اندازِ منفی چیزی نیست که بشود نشانش داد.
     *
     * @return هدفِ به‌روزشده، یا `null` اگر پیدا نشد.
     */
    suspend fun contribute(goalId: Long, deltaRial: Double): SavingsGoalEntity? {
        val goal = dao.byId(goalId) ?: return null
        val updated = goal.copy(savedRial = (goal.savedRial + deltaRial).coerceAtLeast(0.0))
        // مُهرِ رسیدن **یک‌بار** زده می‌شود و با برداشتِ بعدی پس گرفته نمی‌شود: به هدف
        // رسیدن یک رویدادِ گذشته است، نه یک وضعیتِ فعلی. وگرنه نشان باز و بسته می‌شد.
        val stamped = updated.copy(achievedAt = achievedStamp(updated))
        dao.upsert(stamped)
        return stamped
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        dao.delete(goal)
    }

    private fun achievedStamp(goal: SavingsGoalEntity): String? =
        goal.achievedAt ?: if (goal.reached) isoNow() else null

    private fun isoNow(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
}
