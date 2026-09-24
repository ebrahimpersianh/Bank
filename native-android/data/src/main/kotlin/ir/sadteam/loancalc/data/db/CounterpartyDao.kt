package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterpartyDao {
    @Query("SELECT * FROM counterparties ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CounterpartyEntity>>

    @Query("SELECT * FROM counterparties ORDER BY createdAt DESC")
    suspend fun getAll(): List<CounterpartyEntity>

    @Upsert
    suspend fun upsert(item: CounterpartyEntity)

    @Delete
    suspend fun delete(item: CounterpartyEntity)

    @Query("DELETE FROM counterparties")
    suspend fun clear()
}
