package ir.sadteam.loancalc.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.ChequeRiskScore
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.computeChequeRiskScore
import ir.sadteam.loancalc.data.db.ChequeBookDao
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import androidx.room.withTransaction
import ir.sadteam.loancalc.data.db.AppDatabase
import ir.sadteam.loancalc.data.db.ChequeDao
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.BackupBlobRequest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * پورت مفهومی ماژول «امور چک» اپ رقیب (VAMMAN) - چک‌های دریافتی/پرداختی + دسته‌چک (برای پیشنهاد
 * خودکار شماره سریال بعدی). محلی (Room) + پشتیبان‌گیری ابری اختیاری (pushToServer/restoreFromServer)،
 * هم‌الگو با LoanRepository.
 */
/** کلیدِ این بخش در جدولِ نسخه‌های ابری. */
private const val CHEQUE_CLOUD_MODULE = "cheques"

class ChequeRepository(
    private val chequeDao: ChequeDao,
    private val chequeBookDao: ChequeBookDao,
    private val apiService: ApiService,
    /** اختیاری - برای اتمیک‌بودنِ بازیابیِ چک‌ها و دسته‌چک‌ها با هم. */
    private val database: AppDatabase? = null,
    private val uiPrefs: ir.sadteam.loancalc.data.prefs.UiPrefs? = null,
) {
    /** بدنه را داخلِ یک تراکنشِ دیتابیس اجرا می‌کند - یا اگر دیتابیسی نداریم، همان‌طور.
     * تودرتو امن است: Room تراکنشِ داخلی را به همان تراکنشِ بیرونی می‌چسبانَد. */
    private suspend fun <T> inTransaction(block: suspend () -> T): T {
        val db = database ?: return block()
        return db.withTransaction { block() }
    }

    fun observeCheques(): Flow<List<ChequeEntity>> = chequeDao.observeAll()
    fun observeChequeBooks(): Flow<List<ChequeBookEntity>> = chequeBookDao.observeAll()

    suspend fun addCheque(
        type: ChequeType,
        amount: Double,
        chequeNumber: String,
        sayadId: String,
        bankName: String,
        branchName: String,
        ownerName: String,
        dueYear: Int,
        dueMonth: Int,
        dueDay: Int,
        notes: String,
        chequeBookId: Long?,
        photoPath: String? = null,
        nationalId: String? = null,
        previousBalance: Double? = null,
        depositAmount: Double? = null,
        counterpartyId: Long? = null,
    ) {
        chequeDao.upsert(
            ChequeEntity(
                id = System.currentTimeMillis(),
                type = type.name,
                amount = amount,
                chequeNumber = chequeNumber,
                sayadId = sayadId.takeIf { it.isNotBlank() },
                bankName = bankName,
                branchName = branchName,
                ownerName = ownerName,
                dueYear = dueYear,
                dueMonth = dueMonth,
                dueDay = dueDay,
                status = ChequeStatus.PENDING.name,
                notes = notes,
                chequeBookId = chequeBookId,
                archived = false,
                createdAt = isoNow(),
                photoPath = photoPath,
                nationalId = nationalId?.takeIf { it.isNotBlank() },
                previousBalance = previousBalance,
                depositAmount = depositAmount,
                counterpartyId = counterpartyId,
            ),
        )
        if (chequeBookId != null) {
            bumpNextSerial(chequeBookId, chequeNumber)
        }
    }

    /** چک‌هایی که هنوز به طرفِ‌حساب وصل نشدن (`counterpartyId == null`) - برای اجرای یه‌بارِ
     * حدسِ خودکار رو دیتای قدیمی (سوالِ ۶). */
    suspend fun chequesWithoutCounterparty(): List<ChequeEntity> = chequeDao.getAll().filter { it.counterpartyId == null }

    suspend fun updateCheque(cheque: ChequeEntity) {
        chequeDao.upsert(cheque)
    }

    suspend fun setStatus(cheque: ChequeEntity, status: ChequeStatus) {
        chequeDao.upsert(cheque.copy(status = status.name))
    }

    suspend fun setArchived(cheque: ChequeEntity, archived: Boolean) {
        chequeDao.upsert(cheque.copy(archived = archived))
    }

    suspend fun deleteCheque(id: Long) {
        chequeDao.deleteById(id)
    }

    suspend fun getAllCheques(): List<ChequeEntity> = chequeDao.getAll()

    /** امتیازِ ریسکِ برگشتِ یه صادرکننده - جوابِ سوالِ ۲: تاریخچه‌ی پاس/برگشتِ چک‌هایی که به همون
     * طرفِ‌حساب لینک شدن. رجوع کن به [ir.sadteam.loancalc.core.computeChequeRiskScore]. */
    suspend fun riskScoreFor(counterpartyId: Long): ChequeRiskScore {
        val all = chequeDao.getAll().filter { it.counterpartyId == counterpartyId }
        val passed = all.count { it.status == ChequeStatus.PASSED.name }
        val bounced = all.count { it.status == ChequeStatus.BOUNCED.name }
        return computeChequeRiskScore(passed, bounced)
    }

    /** پورت پاک‌سازیِ لوکالِ بعد از خروج - رجوع کن به توضیح [ir.sadteam.loancalc.data.LoanRepository.clearLocal]. */
    suspend fun clearLocal() {
        chequeDao.clear()
        chequeBookDao.clear()
    }

    suspend fun addChequeBook(
        ownerName: String,
        bankName: String,
        startSerial: Long,
        endSerial: Long,
        sayadId: String? = null,
        last4: String? = null,
    ) {
        chequeBookDao.upsert(
            ChequeBookEntity(
                id = System.currentTimeMillis(),
                ownerName = ownerName,
                bankName = bankName,
                startSerial = startSerial,
                endSerial = endSerial,
                nextSerial = startSerial,
                createdAt = isoNow(),
                sayadId = sayadId?.takeIf { it.isNotBlank() },
                last4 = last4?.takeIf { it.isNotBlank() },
            ),
        )
    }

    suspend fun deleteChequeBook(book: ChequeBookEntity) {
        chequeBookDao.delete(book)
    }

    /** بستنِ یه دسته‌چکِ تمام‌شده - `29k` («بسته‌شده در فلان‌ماه»). دوباره صداکردنش رو یه دسته‌چکِ
     * بسته بی‌اثره (closedAt عوض نمی‌شه، همون تاریخِ اولِ بسته‌شدن می‌مونه). */
    suspend fun closeChequeBook(book: ChequeBookEntity) {
        if (book.closedAt != null) return
        chequeBookDao.upsert(book.copy(closedAt = isoNow()))
    }

    /** بعد از ثبت یه چک با شماره‌ی عددی از یه دسته‌چک، اگه شماره برابر nextSerial فعلی بود، یکی
     * جلو می‌ره - پیشنهاد ساده‌ی «شماره‌ی بعدی» برای دفعه‌ی بعد، نه شماره‌گذاری اجباری. */
    private suspend fun bumpNextSerial(chequeBookId: Long, chequeNumber: String) {
        val serial = chequeNumber.toLongOrNull() ?: return
        val book = chequeBookDao.getAll().firstOrNull { it.id == chequeBookId } ?: return
        if (serial == book.nextSerial && book.nextSerial < book.endSerial) {
            chequeBookDao.upsert(book.copy(nextSerial = book.nextSerial + 1))
        }
    }

    /** پورت جدا از بکاپ وام‌ها - یه فایل JSON مستقل برای چک‌ها (هم‌الگو با
     * LoanRepository.exportBackupJson/importBackupJson). */
    suspend fun exportBackupJson(): String {
        val data = mapOf(
            "cheques" to chequeDao.getAll(),
            "chequeBooks" to chequeBookDao.getAll(),
        )
        return GsonBuilder().setPrettyPrinting().create().toJson(data)
    }

    suspend fun importBackupJson(json: String): Boolean {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        val gson = GsonBuilder().create()
        val parsed: Map<String, Any?> = try {
            gson.fromJson(json, type) ?: return false
        } catch (e: Exception) {
            return false
        }
        val chequesJson = gson.toJson(parsed["cheques"] ?: return false)
        val booksJson = gson.toJson(parsed["chequeBooks"] ?: return false)
        val cheques: List<ChequeEntity> = gson.fromJson(chequesJson, object : TypeToken<List<ChequeEntity>>() {}.type)
        val books: List<ChequeBookEntity> = gson.fromJson(booksJson, object : TypeToken<List<ChequeBookEntity>>() {}.type)
        // 🚨 **چک و دسته‌چک با هم یا هیچ‌کدام**: اگر بینِ این دو کار نیمه می‌مانْد،
        // چک‌های تازه کنارِ دسته‌چک‌های قدیمی می‌نشستند - یعنی داده‌ی دو نسل مخلوط.
        inTransaction {
            chequeDao.replaceAll(cheques)
            chequeBookDao.replaceAll(books)
        }
        return true
    }

    /** پورت مفهومی pushToServer تو LoanRepository - fire-and-forget، خطاها (اینترنت قطع، اشتراک
     * منقضی و ...) عمداً قورت داده می‌شن چون این یه سینک پس‌زمینه‌ست. */
    suspend fun pushToServer(token: String): Boolean {
        try {
            val expected = uiPrefs?.cloudRevision(CHEQUE_CLOUD_MODULE)
            val response = apiService.putChequesBackup(
                "Bearer $token",
                BackupBlobRequest(exportBackupJson(), expected),
            )
            if (!response.isSuccessful) return false
            uiPrefs?.let { prefs -> expected?.let { prefs.setCloudRevision(CHEQUE_CLOUD_MODULE, it + 1) } }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** آخرین بکاپِ ابریِ چک‌ها/دسته‌چک‌ها رو می‌گیره و جایگزینِ دیتای محلی می‌کنه. */
    /** فقط می‌گیرد، چیزی نمی‌نویسد - رجوع کن به [LoanRepository.fetchServerBackupJson]. */
    suspend fun fetchServerBackupJson(token: String): String? = try {
        val response = apiService.getChequesBackup("Bearer $token")
        uiPrefs?.setCloudRevision(CHEQUE_CLOUD_MODULE, response.revision)
        response.data
    } catch (e: Exception) {
        null
    }

    suspend fun restoreFromServer(token: String): Boolean {
        val blob = try {
            apiService.getChequesBackup("Bearer $token").data
        } catch (e: Exception) {
            return false
        }
        return importBackupJson(blob)
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
