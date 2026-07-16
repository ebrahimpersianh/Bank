package ir.sadteam.loancalc.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.data.db.ChequeBookDao
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.data.db.ChequeDao
import ir.sadteam.loancalc.data.db.ChequeEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * پورت مفهومی ماژول «امور چک» اپ رقیب (VAMMAN) - چک‌های دریافتی/پرداختی + دسته‌چک (برای پیشنهاد
 * خودکار شماره سریال بعدی). کاملاً محلی (Room)، هم‌الگو با LoanRepository.
 */
class ChequeRepository(
    private val chequeDao: ChequeDao,
    private val chequeBookDao: ChequeBookDao,
) {
    fun observeCheques(): Flow<List<ChequeEntity>> = chequeDao.observeAll()
    fun observeChequeBooks(): Flow<List<ChequeBookEntity>> = chequeBookDao.observeAll()

    suspend fun addCheque(
        type: ChequeType,
        amount: Double,
        chequeNumber: String,
        bankName: String,
        branchName: String,
        ownerName: String,
        dueYear: Int,
        dueMonth: Int,
        dueDay: Int,
        notes: String,
        chequeBookId: Long?,
    ) {
        chequeDao.upsert(
            ChequeEntity(
                id = System.currentTimeMillis(),
                type = type.name,
                amount = amount,
                chequeNumber = chequeNumber,
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
            ),
        )
        if (chequeBookId != null) {
            bumpNextSerial(chequeBookId, chequeNumber)
        }
    }

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

    suspend fun addChequeBook(ownerName: String, bankName: String, startSerial: Long, endSerial: Long) {
        chequeBookDao.upsert(
            ChequeBookEntity(
                id = System.currentTimeMillis(),
                ownerName = ownerName,
                bankName = bankName,
                startSerial = startSerial,
                endSerial = endSerial,
                nextSerial = startSerial,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun deleteChequeBook(book: ChequeBookEntity) {
        chequeBookDao.delete(book)
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
        chequeDao.replaceAll(cheques)
        chequeBookDao.replaceAll(books)
        return true
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
