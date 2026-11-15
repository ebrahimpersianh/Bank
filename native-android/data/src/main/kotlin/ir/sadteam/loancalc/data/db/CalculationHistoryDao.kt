package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CalculationHistoryEntity>>

    @Insert
    suspend fun insert(entry: CalculationHistoryEntity)

    @Delete
    suspend fun delete(entry: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
