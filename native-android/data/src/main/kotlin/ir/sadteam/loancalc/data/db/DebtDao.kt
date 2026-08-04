package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE counterpartyId = :counterpartyId ORDER BY createdAt DESC")
    fun observeForCounterparty(counterpartyId: Long): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts")
    suspend fun getAll(): List<DebtEntity>

    @Upsert
    suspend fun upsert(item: DebtEntity)

    @Delete
    suspend fun delete(item: DebtEntity)

    @Query("DELETE FROM debts WHERE counterpartyId = :counterpartyId")
    suspend fun deleteForCounterparty(counterpartyId: Long)

    @Query("DELETE FROM debts")
    suspend fun clear()
}
