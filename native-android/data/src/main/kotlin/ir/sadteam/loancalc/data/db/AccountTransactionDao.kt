package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountTransactionDao {
    @Query("SELECT * FROM account_transactions ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    fun observeAll(): Flow<List<AccountTransactionEntity>>

    @Query("SELECT * FROM account_transactions WHERE accountId = :accountId ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    fun observeForAccount(accountId: Long): Flow<List<AccountTransactionEntity>>

    @Query("SELECT * FROM account_transactions")
    suspend fun getAll(): List<AccountTransactionEntity>

    @Upsert
    suspend fun upsert(transaction: AccountTransactionEntity)

    @Delete
    suspend fun delete(transaction: AccountTransactionEntity)

    @Query("DELETE FROM account_transactions WHERE accountId = :accountId")
    suspend fun deleteForAccount(accountId: Long)

    @Query("DELETE FROM account_transactions")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(transactions: List<AccountTransactionEntity>) {
        clear()
        transactions.forEach { upsert(it) }
    }
}
