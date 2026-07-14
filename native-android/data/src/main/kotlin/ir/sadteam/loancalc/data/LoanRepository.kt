package ir.sadteam.loancalc.data

import com.google.gson.Gson
import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * فعلاً فقط دور LoanDao محلیه (بدون سینک با سرور). سینک ابری (syncLoansToServer/syncAfterLogin
 * معادل www/index.html) و مدل تایپ‌شده‌ی کامل وام تو فاز ۱ اضافه می‌شن.
 */
class LoanRepository(private val loanDao: LoanDao) {
    private val gson = Gson()

    fun observeLoans(): Flow<List<LoanEntity>> = loanDao.observeAll()

    suspend fun getLoans(): List<LoanEntity> = loanDao.getAll()

    suspend fun saveLoan(loan: LoanEntity) = loanDao.upsert(loan)

    suspend fun deleteLoan(id: Long) = loanDao.deleteById(id)

    /**
     * پورت saveManualLoan تو www/index.html. [dataJson] دقیقاً همون شکل شیءای رو نگه می‌داره که
     * سرور/اپ وب برای هر وام انتظار دارن (name/bank/borrower/amount/rate/n/method/...)، تا وقتی
     * سینک ابری واقعی (فاز بعد) اضافه بشه بدون تغییر schema کار کنه. تاریخ شروع فعلاً یه مقدار
     * پیش‌فرضه چون هنوز فرم انتخاب تاریخ رو این صفحه نداره - تا صفحه‌ی جزئیات/جدول وام دستی پورت
     * نشده، جایی مصرف نمی‌شه.
     */
    suspend fun addManualLoan(name: String, bank: String, installment: Double, n: Int, paidCount: Int) {
        val id = System.currentTimeMillis()
        val amount = installment * n
        val createdAt = isoNow()
        val webShape = linkedMapOf(
            "id" to id,
            "name" to name,
            "bank" to bank,
            "borrower" to "—",
            "amount" to amount,
            "rate" to 0,
            "n" to n,
            "method" to "manual",
            "graceMonths" to 0,
            "installment" to installment,
            "totalPaid" to amount,
            "totalInterest" to 0,
            "startDate" to mapOf("y" to 1404, "m" to 1, "d" to 1),
            "paidCount" to paidCount,
            "createdAt" to createdAt,
        )
        loanDao.upsert(
            LoanEntity(
                id = id,
                name = name,
                bank = bank,
                amount = amount,
                installment = installment,
                totalPaid = amount,
                n = n,
                createdAt = createdAt,
                dataJson = gson.toJson(webShape),
            ),
        )
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
