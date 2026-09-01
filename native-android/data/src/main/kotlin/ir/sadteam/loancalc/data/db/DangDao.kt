package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DangEventDao {
    @Query("SELECT * FROM dang_events ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    fun observeAll(): Flow<List<DangEventEntity>>

    @Query("SELECT * FROM dang_events ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    suspend fun getAll(): List<DangEventEntity>

    @Upsert
    suspend fun upsert(event: DangEventEntity)

    @Delete
    suspend fun delete(event: DangEventEntity)

    @Query("DELETE FROM dang_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM dang_events")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(events: List<DangEventEntity>) {
        clear()
        events.forEach { upsert(it) }
    }
}

@Dao
interface DangParticipantDao {
    @Query("SELECT * FROM dang_participants WHERE eventId = :eventId")
    fun observeForEvent(eventId: Long): Flow<List<DangParticipantEntity>>

    @Query("SELECT * FROM dang_participants")
    suspend fun getAll(): List<DangParticipantEntity>

    @Upsert
    suspend fun upsert(participant: DangParticipantEntity)

    @Delete
    suspend fun delete(participant: DangParticipantEntity)

    @Query("DELETE FROM dang_participants WHERE eventId = :eventId")
    suspend fun deleteForEvent(eventId: Long)

    @Query("DELETE FROM dang_participants")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(participants: List<DangParticipantEntity>) {
        clear()
        participants.forEach { upsert(it) }
    }
}

@Dao
interface DangItemDao {
    @Query("SELECT * FROM dang_items WHERE eventId = :eventId")
    fun observeForEvent(eventId: Long): Flow<List<DangItemEntity>>

    @Query("SELECT * FROM dang_items")
    suspend fun getAll(): List<DangItemEntity>

    @Upsert
    suspend fun upsert(item: DangItemEntity)

    @Delete
    suspend fun delete(item: DangItemEntity)

    @Query("DELETE FROM dang_items WHERE eventId = :eventId")
    suspend fun deleteForEvent(eventId: Long)

    @Query("DELETE FROM dang_items")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<DangItemEntity>) {
        clear()
        items.forEach { upsert(it) }
    }
}

@Dao
interface DangItemShareDao {
    @Query("SELECT * FROM dang_item_shares WHERE itemId = :itemId")
    fun observeForItem(itemId: Long): Flow<List<DangItemShareEntity>>

    @Query("SELECT * FROM dang_item_shares")
    suspend fun getAll(): List<DangItemShareEntity>

    @Upsert
    suspend fun upsert(share: DangItemShareEntity)

    @Delete
    suspend fun delete(share: DangItemShareEntity)

    @Query(
        "DELETE FROM dang_item_shares WHERE itemId IN (SELECT id FROM dang_items WHERE eventId = :eventId)",
    )
    suspend fun deleteForEvent(eventId: Long)

    @Query("DELETE FROM dang_item_shares")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(shares: List<DangItemShareEntity>) {
        clear()
        shares.forEach { upsert(it) }
    }
}
