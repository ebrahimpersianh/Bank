package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    /**
     * `parentName` **نام** است نه کلیدِ خارجی، پس دیتابیس خودش هیچ‌چیزی را آبشاری نمی‌کند.
     * این دو کوئری همان کارِ `ON UPDATE CASCADE`/`ON DELETE SET NULL` را دستی می‌کنند.
     *
     * بی این‌ها تغییرِ نامِ یک دسته‌ی مادر، زیرمجموعه‌هایش را به یک `parentName`ی می‌سپارد که
     * دیگر به هیچ دسته‌ای نمی‌خورد - و آن‌ها **در هیچ گروهی رندر نمی‌شوند**: یتیمِ نامرئی،
     * نه یک ردیفِ خراب که کاربر ببیند و درستش کند.
     */
    @Query("UPDATE custom_categories SET parentName = :newName WHERE type = :type AND parentName = :oldName")
    suspend fun renameParentOfChildren(type: String, oldName: String, newName: String)

    /** حذفِ مادر: زیرمجموعه‌ها **ترفیع** می‌گیرند، پاک نمی‌شوند (تصمیمِ تاییدشده‌ی کاربر). */
    @Query("UPDATE custom_categories SET parentName = NULL WHERE type = :type AND parentName = :name")
    suspend fun promoteChildrenOf(type: String, name: String)

    @Query("SELECT COUNT(*) FROM custom_categories WHERE type = :type AND parentName = :name")
    suspend fun childCountOf(type: String, name: String): Int

    @Update
    suspend fun updateCustom(entity: CustomCategoryEntity)
}
