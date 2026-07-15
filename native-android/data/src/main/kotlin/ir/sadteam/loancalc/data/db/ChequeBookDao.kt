package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeBookDao {
    @Query("SELECT * FROM cheque_books ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ChequeBookEntity>>

    @Query("SELECT * FROM cheque_books ORDER BY createdAt DESC")
    suspend fun getAll(): List<ChequeBookEntity>

    @Upsert
    suspend fun upsert(book: ChequeBookEntity)

    @Delete
    suspend fun delete(book: ChequeBookEntity)
}
