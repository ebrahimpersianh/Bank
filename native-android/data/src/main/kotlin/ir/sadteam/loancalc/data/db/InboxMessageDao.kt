package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxMessageDao {

    @Upsert
    suspend fun upsert(message: InboxMessageEntity)

    @Query("SELECT * FROM inbox_messages ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InboxMessageEntity>>

    @Query("SELECT * FROM inbox_messages WHERE id = :id")
    suspend fun byId(id: Long): InboxMessageEntity?

    /**
     * شمارنده‌ی عددیِ زنگ - **فقط اقدام‌دارهای باز**، طبقِ قاعده‌ی صریحِ طرح.
     * خبرِ خوانده‌نشده اینجا حساب نمی‌شه (اون فقط نقطه‌ی سبز می‌گیره).
     */
    @Query(
        "SELECT COUNT(*) FROM inbox_messages " +
            "WHERE kind IN ('DETECTED_TX','LOAN_DUE') AND actionState = 'OPEN'",
    )
    fun observeActionableCount(): Flow<Int>

    /** آیا خبرِ خوانده‌نشده‌ای هست؟ (نقطه‌ی سبز، نه عدد.) */
    @Query(
        "SELECT COUNT(*) FROM inbox_messages " +
            "WHERE kind NOT IN ('DETECTED_TX','LOAN_DUE') AND readAt IS NULL",
    )
    fun observeUnreadNewsCount(): Flow<Int>

    @Query("UPDATE inbox_messages SET readAt = :now WHERE readAt IS NULL AND kind NOT IN ('DETECTED_TX','LOAN_DUE')")
    suspend fun markAllNewsRead(now: Long)

    @Query("UPDATE inbox_messages SET readAt = :now WHERE id = :id AND readAt IS NULL")
    suspend fun markRead(id: Long, now: Long)

    @Query("UPDATE inbox_messages SET actionState = :state, readAt = :now WHERE id = :id")
    suspend fun setActionState(id: Long, state: String, now: Long)

    /**
     * بستنِ پیام از روی **چیزی که به آن اشاره می‌کند** نه شناسه‌ی خودش - وقتی کاربر از
     * خودِ اعلانِ گوشی تایید/رد می‌کند، فقط شناسه‌ی تراکنش را داریم.
     */
    @Query(
        "UPDATE inbox_messages SET actionState = :state, readAt = :now " +
            "WHERE refId = :refId AND actionState = 'OPEN'",
    )
    suspend fun setActionStateByRefId(refId: String, state: String, now: Long)

    @Query("DELETE FROM inbox_messages WHERE id = :id")
    suspend fun delete(id: Long)

    /** پاک‌سازیِ خبرهای خوانده‌شده‌ی قدیمی‌تر از ۳۰ روز - اقدام‌دار هرگز اینجا حذف نمی‌شه. */
    @Query(
        "DELETE FROM inbox_messages WHERE kind NOT IN ('DETECTED_TX','LOAN_DUE') " +
            "AND readAt IS NOT NULL AND createdAt < :before",
    )
    suspend fun purgeOldNews(before: Long)

    /**
     * سقفِ ۲۰۰ ردیف: از اون بالاتر **قدیمی‌ترین خبرِ خوانده‌شده** حذف می‌شه، هرگز اقدام‌دارِ باز.
     * (قاعده‌ی صریحِ طرح - نگهبانِ آخر تا جدول بی‌نهایت رشد نکنه.)
     */
    @Query(
        "DELETE FROM inbox_messages WHERE id IN (" +
            "SELECT id FROM inbox_messages " +
            "WHERE NOT (kind IN ('DETECTED_TX','LOAN_DUE') AND actionState = 'OPEN') " +
            "ORDER BY createdAt ASC LIMIT :count)",
    )
    suspend fun deleteOldest(count: Int)

    @Query("SELECT COUNT(*) FROM inbox_messages")
    suspend fun total(): Int
}
