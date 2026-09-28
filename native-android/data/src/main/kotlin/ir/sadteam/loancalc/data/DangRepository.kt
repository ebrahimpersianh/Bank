package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.DangMethod
import ir.sadteam.loancalc.data.db.DangEventDao
import ir.sadteam.loancalc.data.db.DangEventEntity
import ir.sadteam.loancalc.data.db.DangItemDao
import ir.sadteam.loancalc.data.db.DangItemEntity
import ir.sadteam.loancalc.data.db.DangItemShareDao
import ir.sadteam.loancalc.data.db.DangItemShareEntity
import ir.sadteam.loancalc.data.db.DangParticipantDao
import ir.sadteam.loancalc.data.db.DangParticipantEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** ورودیِ سهمِ یه شرکت‌کننده برای [DangRepository.createEvent] - `counterpartyId == null` یعنی
 * «خودم» (صاحبِ اپ)، رجوع کن به کامنتِ [DangParticipantEntity]. */
data class DangParticipantInput(
    val counterpartyId: Long?,
    val shareAmount: Double,
    val percentage: Double? = null,
)

/** سهمِ یه شرکت‌کننده از یه قلمِ فاکتور - [participantIndex] جایگاهِ همون نفر تو لیستِ
 * `participants`ِ همون [createEvent] (چون هنوز idِ واقعی ساخته نشده). */
data class DangItemShareInput(val participantIndex: Int, val shareAmount: Double)

data class DangItemInput(val description: String, val amount: Double, val shares: List<DangItemShareInput>)

/**
 * ماژولِ «دنگ» (تقسیمِ یه هزینه بینِ چند نفر) - فریمِ `22c`، جوابِ سوالِ ۷ و ۸ی
 * design/ANSWERS-chequecounterpartydang.md. هر ۴ روش (مساوی/درصدی/دلخواه/قلم‌به‌قلم) از همینجا رد
 * می‌شن - محاسبه‌ی سهم‌ها (تقسیمِ مساوی/درصدی) تو `:core` (`Dang.kt`) انجام می‌شه، این ریپازیتوری
 * فقط با سهم‌های آماده کار می‌کنه.
 */
class DangRepository(
    private val eventDao: DangEventDao,
    private val participantDao: DangParticipantDao,
    private val itemDao: DangItemDao,
    private val itemShareDao: DangItemShareDao,
) {
    fun observeEvents(): Flow<List<DangEventEntity>> = eventDao.observeAll()
    fun observeParticipants(eventId: Long): Flow<List<DangParticipantEntity>> = participantDao.observeForEvent(eventId)
    fun observeItems(eventId: Long): Flow<List<DangItemEntity>> = itemDao.observeForEvent(eventId)
    fun observeItemShares(itemId: Long): Flow<List<DangItemShareEntity>> = itemShareDao.observeForItem(itemId)

    /**
     * یه رویدادِ دنگِ تازه می‌سازه: خودِ رویداد + همه‌ی شرکت‌کننده‌ها + (فقط برای روشِ ITEMIZED)
     * قلم‌های فاکتور و سهمِ هرکس از هر قلم. ⚠️ id همه‌ی ردیف‌های زیرمجموعه از یه شمارنده‌ی صریحِ
     * واحد میاد، نه `System.currentTimeMillis()`ِ جدا برای هرکدوم تو حلقه - رجوع کن به درسِ
     * `AccountRepository.addTransaction` تو CLAUDE.md (نوشتنِ سریعِ پشتِ‌سرهم با timestamp خطرِ
     * برخوردِ id داره).
     */
    suspend fun createEvent(
        title: String,
        method: DangMethod,
        totalAmount: Double,
        year: Int,
        month: Int,
        day: Int,
        isEventMode: Boolean,
        participants: List<DangParticipantInput>,
        items: List<DangItemInput> = emptyList(),
    ): Long {
        val base = System.currentTimeMillis()
        val eventId = base
        eventDao.upsert(
            DangEventEntity(
                id = eventId,
                title = title,
                method = method.name,
                totalAmount = totalAmount,
                year = year,
                month = month,
                day = day,
                isEventMode = isEventMode,
                settled = false,
                createdAt = isoNow(),
            ),
        )
        var idCounter = base + 1
        val participantIds = participants.map { p ->
            val pid = idCounter++
            participantDao.upsert(
                DangParticipantEntity(
                    id = pid,
                    eventId = eventId,
                    counterpartyId = p.counterpartyId,
                    shareAmount = p.shareAmount,
                    percentage = p.percentage,
                ),
            )
            pid
        }
        items.forEach { item ->
            val itemId = idCounter++
            itemDao.upsert(DangItemEntity(id = itemId, eventId = eventId, description = item.description, amount = item.amount))
            item.shares.forEach { share ->
                val shareId = idCounter++
                itemShareDao.upsert(
                    DangItemShareEntity(
                        id = shareId,
                        itemId = itemId,
                        participantId = participantIds[share.participantIndex],
                        shareAmount = share.shareAmount,
                    ),
                )
            }
        }
        return eventId
    }

    suspend fun setEventSettled(event: DangEventEntity, settled: Boolean) {
        eventDao.upsert(event.copy(settled = settled))
    }

    suspend fun setParticipantSettled(participant: DangParticipantEntity, settled: Boolean) {
        participantDao.upsert(participant.copy(settled = settled))
    }

    suspend fun deleteEvent(event: DangEventEntity) {
        itemShareDao.deleteForEvent(event.id)
        itemDao.deleteForEvent(event.id)
        participantDao.deleteForEvent(event.id)
        eventDao.delete(event)
    }

    suspend fun getAllEvents(): List<DangEventEntity> = eventDao.getAll()
    suspend fun getAllParticipants(): List<DangParticipantEntity> = participantDao.getAll()

    suspend fun clearLocal() {
        itemShareDao.clear()
        itemDao.clear()
        participantDao.clear()
        eventDao.clear()
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
