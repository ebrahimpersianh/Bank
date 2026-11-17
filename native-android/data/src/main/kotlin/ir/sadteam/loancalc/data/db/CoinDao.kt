package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    /** موجودی = جمعِ کلِ دفتر (قاعده‌ی صریحِ طرح - هیچ ستونِ موجودیِ ذخیره‌شده‌ای نداریم). */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_events")
    fun observeBalance(): Flow<Int>

    /** همون موجودی، ولی یه‌بار - قبل از خرجِ سکه باید بدونیم کافی هست یا نه. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_events")
    suspend fun getBalance(): Int

    @Query("SELECT * FROM coin_events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CoinEventEntity>>

    /** فقط روزهایی که «ثبتِ روزانه» ثبت شده - پایه‌ی شمارشِ «فعال». */
    @Query("SELECT dateKey FROM coin_events WHERE type = :type AND dateKey != '' ORDER BY dateKey DESC")
    fun observeDateKeys(type: String): Flow<List<String>>

    /** همون لیست، ولی یه‌بار و suspend - برای بررسیِ «هفته‌ی کامل» بعد از هر ثبتِ روزانه. */
    @Query("SELECT dateKey FROM coin_events WHERE type = :type AND dateKey != '' ORDER BY dateKey DESC")
    suspend fun getDateKeys(type: String): List<String>

    /**
     * ⚠️ `IGNORE` عمدیه: ایندکسِ یکتای `(type, dateKey)` تضمین می‌کنه یه رویدادِ روزانه تو یه روز
     * فقط یک‌بار ثبت بشه؛ تلاشِ دوم بی‌صدا نادیده گرفته می‌شه نه اینکه کرش کنه.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun award(event: CoinEventEntity): Long

    /**
     * خرجِ اتمیکِ سکه: بررسیِ موجودی + ثبتِ ردیفِ خرج در **یک تراکنش** - پس دو خریدِ
     * هم‌زمان نمی‌توانند هر دو از یک موجودی خرج کنند و «بررسی بعد خرج» مسابقه ندارد.
     *
     * @return 1 = خرج ثبت شد؛ 0 = موجودی کافی نیست (هیچ ردیفی نوشته نشد)؛
     * ‎-1 = همین خرج از قبل ثبت شده بود (کلیدِ ضدِتکرارِ `(type, dateKey)`)، یعنی خرج
     * انجام شده ولی مالکیت بعدش نرسیده - صدازننده باید مالکیت را بدهد، نه اینکه دوباره خرج کند.
     */
    @Transaction
    suspend fun trySpend(amount: Int, refId: String, type: String, createdAt: Long): Int {
        if (getBalance() < amount) return 0
        val rowId = award(
            CoinEventEntity(
                type = type,
                amount = -amount,
                dateKey = refId,
                createdAt = createdAt,
                refId = refId,
            ),
        )
        return if (rowId == -1L) -1 else 1
    }

    @Query("DELETE FROM coin_events")
    suspend fun clear()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(item: AchievementEntity): Long

    @Query("DELETE FROM achievements")
    suspend fun clear()
}
