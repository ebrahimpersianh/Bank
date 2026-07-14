package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.Flow

/**
 * فعلاً فقط دور LoanDao محلیه (بدون سینک با سرور). سینک ابری (syncLoansToServer/syncAfterLogin
 * معادل www/index.html) و مدل تایپ‌شده‌ی کامل وام تو فاز ۱ اضافه می‌شن.
 */
class LoanRepository(private val loanDao: LoanDao) {
    fun observeLoans(): Flow<List<LoanEntity>> = loanDao.observeAll()

    suspend fun getLoans(): List<LoanEntity> = loanDao.getAll()

    suspend fun saveLoan(loan: LoanEntity) = loanDao.upsert(loan)

    suspend fun deleteLoan(id: Long) = loanDao.deleteById(id)
}
