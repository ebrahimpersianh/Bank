package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Upsert
    suspend fun upsert(item: NoteEntity)

    @Delete
    suspend fun delete(item: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clear()
}
