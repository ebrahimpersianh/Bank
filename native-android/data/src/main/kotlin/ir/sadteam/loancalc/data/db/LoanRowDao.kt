package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface LoanRowDao {
    @Query("SELECT * FROM loan_rows WHERE loanId = :loanId ORDER BY m ASC")
    suspend fun getForLoan(loanId: Long): List<LoanRowEntity>

    @Upsert
    suspend fun upsertAll(rows: List<LoanRowEntity>)

    @Query("DELETE FROM loan_rows WHERE loanId = :loanId")
    suspend fun deleteForLoan(loanId: Long)

    @Query("DELETE FROM loan_rows")
    suspend fun clearAll()

    /** پورتِ «تعدادِ کل عوض شد» (تغییرِ n تو ویرایشِ وامِ دستی) - کاملِ ردیف‌های قبلی رو با مجموعه‌ی
     * جدید عوض می‌کنه، به‌صورتِ اتمیک. */
    @Transaction
    suspend fun replaceForLoan(loanId: Long, rows: List<LoanRowEntity>) {
        deleteForLoan(loanId)
        upsertAll(rows)
    }
}
