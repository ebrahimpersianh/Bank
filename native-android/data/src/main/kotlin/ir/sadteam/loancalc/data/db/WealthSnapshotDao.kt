package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WealthSnapshotDao {
    /** قدیمی → تازه، همان ترتیبی که نمودار می‌خواهد. */
    @Query("SELECT * FROM wealth_snapshots ORDER BY dateKey ASC")
    fun observeAll(): Flow<List<WealthSnapshotEntity>>

    @Upsert
    suspend fun upsert(snapshot: WealthSnapshotEntity)

    /** نگه‌داریِ محدود: بیش از یک سال عکسِ روزانه نه جایی در نمودار دارد نه فایده. */
    @Query("DELETE FROM wealth_snapshots WHERE dateKey < :beforeKey")
    suspend fun purgeBefore(beforeKey: String)

    @Query("DELETE FROM wealth_snapshots")
    suspend fun clear()
}
