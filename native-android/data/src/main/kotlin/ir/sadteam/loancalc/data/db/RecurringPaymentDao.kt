package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringPaymentDao {
    @Query("SELECT * FROM recurring_payments ORDER BY dayOfMonth ASC")
    fun observeAll(): Flow<List<RecurringPaymentEntity>>

    @Query("SELECT * FROM recurring_payments")
    suspend fun getAll(): List<RecurringPaymentEntity>

    @Upsert
    suspend fun upsert(payment: RecurringPaymentEntity)

    @Delete
    suspend fun delete(payment: RecurringPaymentEntity)

    @Query("DELETE FROM recurring_payments")
    suspend fun clear()

    @androidx.room.Transaction
    suspend fun replaceAll(payments: List<RecurringPaymentEntity>) {
        clear()
        payments.forEach { upsert(it) }
    }
}
