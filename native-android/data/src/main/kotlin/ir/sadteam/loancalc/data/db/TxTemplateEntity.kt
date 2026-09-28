package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** الگوی تراکنش («نون»، «بنزین») - ثبتِ یک‌تپی. مبلغ ریال؛ `0` یعنی هر بار پرسیده شود. */
@Entity(tableName = "tx_templates")
data class TxTemplateEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val amount: Double,
    val category: String?,
    val accountId: Long?,
    val sortOrder: Int = 0,
)

@Dao
interface TxTemplateDao {
    @Query("SELECT * FROM tx_templates ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<TxTemplateEntity>>

    @Upsert
    suspend fun upsert(item: TxTemplateEntity)

    @Delete
    suspend fun delete(item: TxTemplateEntity)
}

/**
 * قبضِ دوره‌ای (آب، برق، گاز، موبایل، اینترنت…) - یادآوری در [dueDay]ِ هر ماه (یا هر دو ماه).
 * [lastPaidKey] = «سال-ماه»ِ آخرین پرداخت، تا یادآورِ همان دوره دوباره نیاید.
 */
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val kind: String,
    val billId: String?,
    val dueDay: Int,
    val periodMonths: Int = 1,
    val lastAmount: Double = 0.0,
    val lastPaidKey: String? = null,
)

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDay")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills")
    suspend fun getAll(): List<BillEntity>

    @Upsert
    suspend fun upsert(item: BillEntity)

    @Delete
    suspend fun delete(item: BillEntity)
}
