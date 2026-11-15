package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques ORDER BY dueYear, dueMonth, dueDay")
    fun observeAll(): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques ORDER BY dueYear, dueMonth, dueDay")
    suspend fun getAll(): List<ChequeEntity>

    @Upsert
    suspend fun upsert(cheque: ChequeEntity)

    @Delete
    suspend fun delete(cheque: ChequeEntity)

    @Query("DELETE FROM cheques WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cheques")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(cheques: List<ChequeEntity>) {
        clear()
        cheques.forEach { upsert(it) }
    }
}
