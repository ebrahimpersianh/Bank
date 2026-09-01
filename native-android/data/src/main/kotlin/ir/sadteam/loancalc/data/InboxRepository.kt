package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.InboxMessageDao
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مرکزِ پیام‌ها - بخشِ ۴۰ طراحی.
 *
 * **منبعِ واحد** (قاعده‌ی صریحِ طرح): هر پیامی که قراره کاربر ببینه **اول اینجا** ساخته می‌شه؛
 * اعلانِ گوشی از رو همین ردیف ساخته می‌شه، نه برعکس.
 */
@Singleton
class InboxRepository @Inject constructor(
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
            ),
        )
        enforceLimits()
        return messageId
    }

    suspend fun markRead(id: Long) = dao.markRead(id, System.currentTimeMillis())

    /** ⚠️ فقط `readAt`ِ **خبر**ها رو پر می‌کنه - تاییدِ ضمنیِ هیچ تراکنشی نیست (قاعده‌ی طرح). */
    suspend fun markAllNewsRead() = dao.markAllNewsRead(System.currentTimeMillis())

    suspend fun resolve(id: Long, done: Boolean) = dao.setActionState(
        id = id,
        state = if (done) InboxMessageEntity.ActionState.DONE else InboxMessageEntity.ActionState.DISMISSED,
        now = System.currentTimeMillis(),
    )

    suspend fun byId(id: Long) = dao.byId(id)

    suspend fun delete(id: Long) = dao.delete(id)

    /** دو قاعده‌ی نگهداشتِ طرح: خبرِ خوانده‌شده‌ی کهنه‌تر از ۳۰ روز، و سقفِ ۲۰۰ ردیف. */
    private suspend fun enforceLimits() {
        dao.purgeOldNews(System.currentTimeMillis() - THIRTY_DAYS_MS)
        val total = dao.total()
        if (total > MAX_ROWS) dao.deleteOldest(total - MAX_ROWS)
    }

    private companion object {
        const val MAX_ROWS = 200
        const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }
}
