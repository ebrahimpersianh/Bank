package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<AssetEntity>>

    /** همون لیست ولی یه‌بار و suspend - برای نوشتنِ قیمتِ تازه رو همه‌ی دارایی‌ها. */
    @Query("SELECT * FROM assets")
    suspend fun getAll(): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE symbol = :symbol LIMIT 1")
    suspend fun findBySymbol(symbol: String): AssetEntity?

    @Upsert
    suspend fun upsert(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)

    @Query("DELETE FROM assets")
    suspend fun clear()
}

@Dao
interface AssetTradeDao {
    @Query("SELECT * FROM asset_trades ORDER BY year DESC, month DESC, day DESC, id DESC")
    fun observeAll(): Flow<List<AssetTradeEntity>>

    @Query("SELECT * FROM asset_trades WHERE assetId = :assetId ORDER BY year DESC, month DESC, day DESC, id DESC")
    fun observeForAsset(assetId: Long): Flow<List<AssetTradeEntity>>

    @Upsert
    suspend fun upsert(trade: AssetTradeEntity)

    @Delete
    suspend fun delete(trade: AssetTradeEntity)

    @Query("DELETE FROM asset_trades WHERE assetId = :assetId")
    suspend fun deleteForAsset(assetId: Long)

    @Query("DELETE FROM asset_trades")
    suspend fun clear()
}
