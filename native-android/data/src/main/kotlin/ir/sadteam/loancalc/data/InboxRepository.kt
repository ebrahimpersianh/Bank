package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.InboxMessageDao
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * مرکزِ پیام‌ها - بخشِ ۴۰ طراحی.
 *
 * **منبعِ واحد** (قاعده‌ی صریحِ طرح): هر پیامی که قراره کاربر ببینه **اول اینجا** ساخته می‌شه؛
 * اعلانِ گوشی از رو همین ردیف ساخته می‌شه، نه برعکس.
 *
 * ⚠️ **کلاسِ سادست، نه `@Inject constructor`** - `provideInboxRepository`ِ `AppModule` این رو
 * می‌سازه (مثلِ بقیه‌ی ریپازیتوری‌های `:data`). `:data` هیچ وابستگیِ Dagger/Hiltی نداره؛
 * constructor injectionِ قبلی باعثِ شکستِ `kaptDebugKotlin` با `error.NonExistentClass` می‌شد.
 */
class InboxRepository(
    private val dao: InboxMessageDao,
) {
    fun observeAll(): Flow<List<InboxMessageEntity>> = dao.observeAll()

    /** شمارنده‌ی عددیِ زنگ - فقط اقدام‌دارهای باز. */
    fun observeActionableCount(): Flow<Int> = dao.observeActionableCount()

    /** نقطه‌ی سبز - خبرِ خوانده‌نشده. */
    fun observeUnreadNewsCount(): Flow<Int> = dao.observeUnreadNewsCount()

    suspend fun post(
        kind: String,
        title: String,
        body: String,
        refId: String? = null,
        id: Long? = null,
        sourceLabel: String? = null,
        sourceText: String? = null,
    ): Long {
        val messageId = id ?: System.currentTimeMillis()
        dao.upsert(
            InboxMessageEntity(
                id = messageId,
                kind = kind,
                title = title,
                body = body,
                createdAt = System.currentTimeMillis(),
                // اقدام‌دار با حالتِ «باز» شروع می‌شه تا تو شمارنده‌ی زنگ بیاد؛ خبر حالتِ اقدام نداره.
                actionState = if (InboxMessageEntity.Kind.isActionable(kind)) {
                    InboxMessageEntity.ActionState.OPEN
                } else {
                    InboxMessageEntity.ActionState.NONE
                },
                refId = refId,
                sourceLabel = sourceLabel,
                sourceText = sourceText,
            ),
        )
        enforceLimits()
        return messageId
    }

    /**
     * اطلاعیه‌های سرور را به صندوق اضافه می‌کند. شناسه‌ی پیام **منفیِ** شناسه‌ی سرور است تا با
     * شناسه‌های زمانیِ بقیه‌ی پیام‌ها هرگز برخورد نکند، و اطلاعیه‌ای که از قبل هست دوباره نوشته
     * نمی‌شود - وگرنه «خوانده‌شده» بودنش پاک می‌شد.
     */
    suspend fun mergeAnnouncements(items: List<ir.sadteam.loancalc.data.network.AnnouncementDto>) {
        items.forEach { a ->
            val localId = -a.id
            if (dao.byId(localId) != null) return@forEach
            val created = parseServerUtc(a.createdAt) ?: System.currentTimeMillis()
            // اطلاعیه‌ی کهنه‌تر از ۶۰ روز اضافه نمی‌شود: پاک‌سازیِ ۹۰روزه‌ی خبرها آن را حذف
            // می‌کند و بی این شرط، دفعه‌ی بعد دوباره «خوانده‌نشده» برمی‌گشت.
            if (System.currentTimeMillis() - created > 60L * 24 * 60 * 60 * 1000) return@forEach
            dao.upsert(
                InboxMessageEntity(
                    id = localId,
                    kind = InboxMessageEntity.Kind.ANNOUNCEMENT,
                    title = a.title,
                    body = a.body,
                    createdAt = created,
                    actionState = InboxMessageEntity.ActionState.NONE,
                    refId = a.kind,
                ),
            )
        }
    }

    /** `2026-09-24 10:15:00` (UTCِ SQLite) → میلی‌ثانیه. */
    // ⚠️ SimpleDateFormat نه java.time: minSdk 24 است و desugaring روشن نیست.
    private fun parseServerUtc(raw: String): Long? = runCatching {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
            .parse(raw)?.time
    }.getOrNull()

    suspend fun markRead(id: Long) = dao.markRead(id, System.currentTimeMillis())

    /** ⚠️ فقط `readAt`ِ **خبر**ها رو پر می‌کنه - تاییدِ ضمنیِ هیچ تراکنشی نیست (قاعده‌ی طرح). */
    suspend fun markAllNewsRead() = dao.markAllNewsRead(System.currentTimeMillis())

    suspend fun resolve(id: Long, done: Boolean) = dao.setActionState(
        id = id,
        state = if (done) InboxMessageEntity.ActionState.DONE else InboxMessageEntity.ActionState.DISMISSED,
        now = System.currentTimeMillis(),
    )

    /** همان [resolve] ولی از روی شناسه‌ی تراکنش - مسیرِ دکمه‌های خودِ اعلانِ گوشی. */
    suspend fun resolveByRefId(refId: String, done: Boolean) = dao.setActionStateByRefId(
        refId = refId,
        state = if (done) InboxMessageEntity.ActionState.DONE else InboxMessageEntity.ActionState.DISMISSED,
        now = System.currentTimeMillis(),
    )

    suspend fun byId(id: Long) = dao.byId(id)

    suspend fun delete(id: Long) = dao.delete(id)

    /** دو قاعده‌ی نگهداشتِ طرح: خبرِ خوانده‌شده‌ی کهنه‌تر از ۳۰ روز، و سقفِ ۲۰۰ ردیف. */
    private suspend fun enforceLimits() {
        dao.purgeOldNews(System.currentTimeMillis() - NINETY_DAYS_MS)
        val total = dao.total()
        if (total > MAX_ROWS) dao.deleteOldest(total - MAX_ROWS)
    }

    private companion object {
        // `71e`: این جدول حالا **تاریخچه‌ی اعلان‌ها** هم هست، نه فقط صندوقِ کارهای باز - پس
        // سقف بالاتر رفت. ولی بی‌سقف نشد: دفترِ بی‌سقف روی کاربرِ دوساله هزاران ردیف می‌شود و
        // اسکرول و پشتیبان را سنگین می‌کند، و کسی اعلانِ سالِ پیش را نمی‌خواند.
        const val MAX_ROWS = 600
        const val NINETY_DAYS_MS = 90L * 24 * 60 * 60 * 1000
    }
}
