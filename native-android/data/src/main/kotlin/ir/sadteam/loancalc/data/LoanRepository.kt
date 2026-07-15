package ir.sadteam.loancalc.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.PutLoansRequest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** پورت syncAfterLogin تو www/index.html - نتیجه‌ی چک اولیه‌ی سینک بعد از ورود موفق. */
sealed class SyncOutcome {
    data object NoAction : SyncOutcome()
    data object RestoredFromServer : SyncOutcome()
    data class ConflictNeedsChoice(val serverLoans: List<Map<String, Any?>>) : SyncOutcome()
    data object Failed : SyncOutcome()
}

/**
 * فقط دور LoanDao محلی نیست - سینک ابری (syncLoansToServer/syncAfterLogin معادل www/index.html)
 * هم همینجاست. مدل تایپ‌شده‌ی کامل وام (به‌جای Map<String, Any?> مات) فاز بعده.
 */
class LoanRepository(private val loanDao: LoanDao, private val apiService: ApiService) {
    private val gson = Gson()

    fun observeLoans(): Flow<List<LoanEntity>> = loanDao.observeAll()

    suspend fun getLoans(): List<LoanEntity> = loanDao.getAll()

    suspend fun saveLoan(loan: LoanEntity) = loanDao.upsert(loan)

    suspend fun deleteLoan(id: Long) = loanDao.deleteById(id)

    /**
     * پورت saveManualLoan تو www/index.html. [dataJson] دقیقاً همون شکل شیءای رو نگه می‌داره که
     * سرور/اپ وب برای هر وام انتظار دارن (name/bank/borrower/amount/rate/n/method/...).
     * تاریخ شروع فعلاً یه مقدار پیش‌فرضه چون هنوز فرم انتخاب تاریخ رو این صفحه نداره.
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
                paidCount = paidCount,
                createdAt = createdAt,
                dataJson = gson.toJson(webShape),
            ),
        )
    }

    /** پرداخت/لغو پرداخت یه قسط برای وام‌های دستی: هم ستون سریع [LoanEntity.paidCount] رو آپدیت
     * می‌کنه هم کلید متناظرش رو تو [LoanEntity.dataJson]، تا این دو هیچ‌وقت از هم عقب نیفتن. */
    suspend fun setPaidCount(loan: LoanEntity, paidCount: Int) {
        val type = object : TypeToken<MutableMap<String, Any?>>() {}.type
        val data: MutableMap<String, Any?> = gson.fromJson(loan.dataJson, type) ?: mutableMapOf()
        data["paidCount"] = paidCount
        loanDao.upsert(loan.copy(paidCount = paidCount, dataJson = gson.toJson(data)))
    }

    /** پورت syncLoansToServer: fire-and-forget، خطاها رو قورت می‌ده (دقیقاً مثل `.catch(()=>{})`
     * تو وب) چون این یه سینک پس‌زمینه‌ست، نه یه عملیات که کاربر منتظرش بمونه. */
    suspend fun pushToServer(token: String) {
        try {
            val loans = loanDao.getAll().map { toWebMap(it) }
            apiService.putLoans("Bearer $token", PutLoansRequest(loans))
        } catch (e: Exception) {
            // عمداً نادیده گرفته می‌شه
        }
    }

    /** پورت syncAfterLogin: فقط دقیقاً یه‌بار بلافاصله بعد از ورود موفق صدا زده می‌شه. */
    suspend fun syncAfterLogin(token: String): SyncOutcome {
        val serverLoans = try {
            apiService.getLoans("Bearer $token").loans
        } catch (e: Exception) {
            return SyncOutcome.Failed
        }
        val local = loanDao.getAll()
        val localHasData = local.isNotEmpty()
        val serverHasData = serverLoans.isNotEmpty()

        return when {
            serverHasData && !localHasData -> {
                replaceAllWithServerData(serverLoans)
                SyncOutcome.RestoredFromServer
            }
            serverHasData && localHasData && !sameLoans(local, serverLoans) -> {
                SyncOutcome.ConflictNeedsChoice(serverLoans)
            }
            else -> {
                pushToServer(token)
                SyncOutcome.NoAction
            }
        }
    }

    suspend fun replaceAllWithServerData(serverLoans: List<Map<String, Any?>>) {
        loanDao.replaceAll(serverLoans.mapNotNull { fromWebMap(it) })
    }

    /** مقایسه‌ی «فرق دارن یا نه» - نه یه‌به‌یه مثل JSON.stringify تو وب (چون Gson اعداد رو موقع
     * رفت‌وبرگشت به Double تبدیل می‌کنه و ترتیب کلیدها تضمین‌شده نیست)، بلکه بر اساس شناسه‌ها و
     * تعداد قسط پرداخت‌شده‌ی هر وام - برای تشخیص «واقعاً فرق دارن» به همون اندازه قابل‌اعتماده. */
    private fun sameLoans(local: List<LoanEntity>, server: List<Map<String, Any?>>): Boolean {
        if (local.size != server.size) return false
        val localSig = local.map { it.id to it.paidCount }.toSet()
        val serverSig = server.mapNotNull { m ->
            val id = (m["id"] as? Number)?.toLong() ?: return@mapNotNull null
            val paidCount = (m["paidCount"] as? Number)?.toInt() ?: 0
            id to paidCount
        }.toSet()
        return localSig == serverSig
    }

    private fun toWebMap(entity: LoanEntity): Map<String, Any?> {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        return gson.fromJson(entity.dataJson, type) ?: emptyMap()
    }

    private fun fromWebMap(map: Map<String, Any?>): LoanEntity? {
        val id = (map["id"] as? Number)?.toLong() ?: return null
        val name = map["name"] as? String ?: return null
        val bank = map["bank"] as? String ?: ""
        val amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
        val installment = (map["installment"] as? Number)?.toDouble() ?: 0.0
        val totalPaid = (map["totalPaid"] as? Number)?.toDouble() ?: amount
        val n = (map["n"] as? Number)?.toInt() ?: 0
        val paidCount = (map["paidCount"] as? Number)?.toInt() ?: 0
        val createdAt = map["createdAt"] as? String ?: isoNow()
        return LoanEntity(
            id = id,
            name = name,
            bank = bank,
            amount = amount,
            installment = installment,
            totalPaid = totalPaid,
            n = n,
            paidCount = paidCount,
            createdAt = createdAt,
            dataJson = gson.toJson(map),
        )
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
