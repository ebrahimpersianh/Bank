package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountTransactionDao {
    /**
     * ⚠️ **فقط تراکنش‌های تاییدشده.** تراکنشی که خودکار از پیامک/اعلان خونده شده تا وقتی کاربر
     * تاییدش نکرده اینجا نمیاد، پس **رو موجودی و همه‌ی گزارش‌ها بی‌اثره**.
     *
     * فیلتر عمداً همین‌جا (سطحِ DAO) زده شده نه تو ViewModelها: هر جای اپ که موجودی/گزارش/بودجه
     * حساب می‌کنه از همین متد می‌خونه، پس یه فیلترِ واحد از جاافتادنِ سهویِ شرط تو ده‌ها جا
     * جلوگیری می‌کنه.
     */
    @Query("SELECT * FROM account_transactions WHERE confirmed = 1 ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    fun observeAll(): Flow<List<AccountTransactionEntity>>

    @Query("SELECT * FROM account_transactions WHERE accountId = :accountId AND confirmed = 1 ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    fun observeForAccount(accountId: Long): Flow<List<AccountTransactionEntity>>

    /** تراکنش‌های **منتظرِ تایید** - خوراکِ کارت‌های اقدام‌دارِ مرکزِ پیام‌ها. */
    @Query("SELECT * FROM account_transactions WHERE confirmed = 0 ORDER BY createdAt DESC")
    fun observePending(): Flow<List<AccountTransactionEntity>>

    @Query("SELECT * FROM account_transactions WHERE id = :id")
    suspend fun byId(id: Long): AccountTransactionEntity?

    @Query("UPDATE account_transactions SET confirmed = 1 WHERE id = :id")
    suspend fun confirm(id: Long)

    /** ⚠️ **بدونِ فیلتر** - پشتیبان‌گیری/سینک باید تراکنشِ تاییدنشده رو هم ببره وگرنه گم می‌شه. */
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
