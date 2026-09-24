package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    /** نرسیده‌ها اول، بعد رسیده‌ها؛ داخلِ هر گروه تازه‌ترین بالا. */
    @Query("SELECT * FROM savings_goals ORDER BY achievedAt IS NOT NULL ASC, id DESC")
    fun observeAll(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    suspend fun getAll(): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun byId(id: Long): SavingsGoalEntity?

    @Upsert
    suspend fun upsert(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)
}
