package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories")
    fun observeCustom(): Flow<List<CustomCategoryEntity>>

    @Insert
    suspend fun insertCustom(entity: CustomCategoryEntity): Long

    @Delete
    suspend fun deleteCustom(entity: CustomCategoryEntity)

    @Query("SELECT * FROM category_order")
    fun observeOrder(): Flow<List<CategoryOrderEntity>>

    @Upsert
    suspend fun upsertOrder(entities: List<CategoryOrderEntity>)

    @Query("DELETE FROM category_order WHERE type = :type AND name = :name")
    suspend fun deleteOrder(type: String, name: String)
}
