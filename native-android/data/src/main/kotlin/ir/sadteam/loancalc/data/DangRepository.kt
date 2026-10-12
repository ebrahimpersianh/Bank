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
    fun observeAllParticipants(): Flow<List<DangParticipantEntity>> = participantDao.observeAll()

    /** همه‌چیزِ یک دنگ برای فرمِ ویرایش. */
    suspend fun loadForEdit(eventId: Long): Triple<List<DangParticipantEntity>, List<DangItemEntity>, List<DangItemShareEntity>> =
        Triple(participantDao.getForEvent(eventId), itemDao.getForEvent(eventId), itemShareDao.getForEvent(eventId))

    /**
     * ویرایشِ دنگ (۱۶ مهر). شرکت‌کننده‌ای که مانده (همان طرفِ‌حساب) **همان شناسه و تیکِ تسویه** را
     * نگه می‌دارد تا پولی که با آن جابه‌جا شده گم نشود؛ قلم‌ها از نو ساخته می‌شوند.
     * خروجی: (شناسه‌ی سهم‌های حذف‌شده، سهم‌های تسویه‌شده‌ای که مبلغشان عوض شد) - برای اصلاحِ تراکنشِ حساب.
     */
    suspend fun updateEvent(
        event: DangEventEntity,
        title: String,
        method: DangMethod,
        totalAmount: Double,
        year: Int,
        month: Int,
        day: Int,
        isEventMode: Boolean,
        participants: List<DangParticipantInput>,
        items: List<DangItemInput> = emptyList(),
    ): Pair<List<Long>, List<DangParticipantEntity>> {
        val old = participantDao.getForEvent(event.id)
        val pool = old.toMutableList()
        var idCounter = System.currentTimeMillis()
        val changed = mutableListOf<DangParticipantEntity>()
        val kept = participants.map { p ->
            val match = pool.firstOrNull { it.counterpartyId == p.counterpartyId }?.also { pool.remove(it) }
            val row = DangParticipantEntity(
                id = match?.id ?: idCounter++,
                eventId = event.id,
                counterpartyId = p.counterpartyId,
                shareAmount = p.shareAmount,
                percentage = p.percentage,
                settled = match?.settled ?: false,
            )
            if (match != null && match.settled && kotlin.math.abs(match.shareAmount - p.shareAmount) >= 1.0) changed += row
            participantDao.upsert(row)
            row
        }
        pool.forEach { participantDao.delete(it) }
        itemShareDao.deleteForEvent(event.id)
        itemDao.deleteForEvent(event.id)
        items.forEach { item ->
            val itemId = idCounter++
            itemDao.upsert(DangItemEntity(id = itemId, eventId = event.id, description = item.description, amount = item.amount))
            item.shares.forEach { share ->
                itemShareDao.upsert(
                    DangItemShareEntity(id = idCounter++, itemId = itemId, participantId = kept[share.participantIndex].id, shareAmount = share.shareAmount),
                )
            }
        }
        eventDao.upsert(
            event.copy(
                title = title, method = method.name, totalAmount = totalAmount,
                year = year, month = month, day = day, isEventMode = isEventMode,
                settled = othersAllSettled(kept),
            ),
        )
        return pool.map { it.id } to changed
    }

    /** دنگ وقتی تسویه است که **همه‌ی دیگران** سهمشان را داده باشند؛ «خودم» حساب نمی‌شود. */
    private fun othersAllSettled(list: List<DangParticipantEntity>): Boolean {
        val others = list.filter { it.counterpartyId != null }
        return others.isNotEmpty() && others.all { it.settled }
    }

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
        // تیکِ همه = دنگ خودبه‌خود تسویه؛ برداشتنِ یک تیک = دوباره باز (۱۶ مهر، خواسته‌ی کاربر).
        val all = participantDao.getForEvent(participant.eventId)
        eventDao.getAll().firstOrNull { it.id == participant.eventId }?.let { ev ->
            val s = othersAllSettled(all)
            if (ev.settled != s) eventDao.upsert(ev.copy(settled = s))
        }
    }

    /** «همه پرداخت کردند»: تیکِ همه‌ی دیگران. خروجی = سهم‌هایی که تازه تیک خوردند. */
    suspend fun settleAllOthers(event: DangEventEntity): List<DangParticipantEntity> {
        val open = participantDao.getForEvent(event.id).filter { it.counterpartyId != null && !it.settled }
        open.forEach { participantDao.upsert(it.copy(settled = true)) }
        eventDao.upsert(event.copy(settled = true))
        return open
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
