package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<IncomeEntity>>

    @Upsert
    suspend fun upsert(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)

    @Query("DELETE FROM incomes")
    suspend fun clear()
}
