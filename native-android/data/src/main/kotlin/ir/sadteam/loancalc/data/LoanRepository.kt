package ir.sadteam.loancalc.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
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

    /** پورت پاک‌سازیِ لوکالِ بعد از خروج - وگرنه وام‌های همون گوشی زیرِ حسابِ قبلی، موقع ورود با یه
     * شماره‌ی دیگه (که سرورش هنوز خالیه)، تو [syncAfterLogin] به‌جای «سرور خالیه» به «پوشِ محلی به
     * سرور» می‌رفت و اشتباهی وام‌های کاربرِ قبلی رو زیرِ حسابِ جدید آپلود می‌کرد. */
    suspend fun clearLocal() = loanDao.clear()

    /**
     * پورت saveManualLoan تو www/index.html. [dataJson] دقیقاً همون شکل شیءای رو نگه می‌داره که
     * سرور/اپ وب برای هر وام انتظار دارن (name/bank/borrower/amount/rate/n/method/...)، به‌علاوه
     * یه آرایه‌ی `rows` (پورت مدل مستقل هر قسط `rows[].paid`/`paidLate`/`paidDate` تو وب).
     */
    suspend fun addManualLoan(
        name: String,
        bank: String,
        installment: Double,
        n: Int,
        paidCount: Int,
        startDate: Map<String, Int>,
    ) {
        val id = System.currentTimeMillis()
        val amount = installment * n
        val createdAt = isoNow()
        val rows = buildInitialRows(installment, n, paidCount)
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
            "startDate" to startDate,
            "intervalDays" to 30,
            "paidCount" to paidCount,
            "createdAt" to createdAt,
            "rows" to rows,
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

    /**
     * پورت saveLoan تو www/index.html (ذخیره‌ی نتیجه‌ی یه محاسبه‌ی وام بانکی، نه ورود دستی). برخلاف
     * [addManualLoan] که اقساط رو یکسان فرض می‌کنه، اینجا خودِ ردیف‌های محاسبه‌شده ([rows] = جفت
     * (شماره‌ی قسط، مبلغ)) ذخیره می‌شن تا وام‌های قرض‌الحسنه که اقساط نامساوی دارن درست بازسازی بشن.
     * شناسه‌ی وامِ ساخته‌شده رو برمی‌گردونه.
     */
    suspend fun saveComputedLoan(
        name: String,
        bank: String,
        borrower: String,
        principal: Double,
        ratePct: Double,
        n: Int,
        method: String,
        graceMonths: Int,
        installment: Double,
        totalPaid: Double,
        totalInterest: Double,
        startDate: Map<String, Int>,
        intervalDays: Int,
        rows: List<Pair<Int, Double>>,
    ): Long {
        val id = System.currentTimeMillis()
        val createdAt = isoNow()
        val rowMaps = rows.map { (m, inst) -> mapOf("m" to m, "installment" to inst, "paid" to false) }
        val webShape = linkedMapOf(
            "id" to id,
            "name" to name,
            "bank" to bank,
            "borrower" to borrower,
            "amount" to principal,
            "rate" to ratePct,
            "n" to n,
            "method" to method,
            "graceMonths" to graceMonths,
            "installment" to installment,
            "totalPaid" to totalPaid,
            "totalInterest" to totalInterest,
            "startDate" to startDate,
            "intervalDays" to intervalDays,
            "paidCount" to 0,
            "createdAt" to createdAt,
            "rows" to rowMaps,
        )
        loanDao.upsert(
            LoanEntity(
                id = id,
                name = name,
                bank = bank,
                amount = principal,
                installment = installment,
                totalPaid = totalPaid,
                n = n,
                paidCount = 0,
                createdAt = createdAt,
                dataJson = gson.toJson(webShape),
            ),
        )
        return id
    }

    /** پورت rows[].paid تو www/index.html - وضعیت پرداخت هر قسط مستقله (نه یه آستانه‌ی ترتیبی)،
     * به‌علاوه `dueDate` که از startDate/intervalDaysِ خودِ وام محاسبه می‌شه (:app مستقیم به Gson
     * دسترسی نداره، برای همین این محاسبه اینجا تو :data انجام می‌شه، نه تو UI). */
    fun getRows(loan: LoanEntity): List<Map<String, Any?>> {
        val data = parseData(loan)
        val rows = rowsFromData(data, loan)
        val startDate = parseStartDate(data)
        val intervalDays = (data["intervalDays"] as? Number)?.toInt() ?: 30
        return rows.map { row ->
            val m = (row["m"] as? Number)?.toInt() ?: 1
            // قسط ۱ سررسیدش یه دوره بعد از startDate ئه (نه خودِ startDate) - پورت renderTable وب.
            // باگِ گزارش‌شده‌ی کاربر: نسخه‌ی قبلی برای فاصله‌ی «ماهانه» هم ثابت m×۳۰ روز جمع می‌زد،
            // که چون ۶ ماهِ اولِ سالِ شمسی ۳۱ روزه‌ان هر ماه یه روز عقب می‌رفت (۴/۴ → ۵/۳ → ۶/۲...).
            // الان فاصله‌های مضربِ ۳۰ (ماهانه/دوماهه/سه‌ماهه) ماهِ تقویمیِ واقعی جلو می‌رن و روزِ
            // ماه ثابت می‌مونه (با clamp آخرِ ماه)؛ فقط هفتگی/دوهفته‌ای (۷/۱۴) روزشمار می‌مونن.
            // توجه: این فقط «تاریخِ نمایشیِ» سررسیده - فرمولِ مالی (i = rate×interval/365 تو
            // LoanCalculator) عمداً همون interval قبلی رو نگه می‌داره، رجوع کن به CLAUDE.md.
            val due = if (intervalDays % 30 == 0) {
                PersianCalendar.addMonths(startDate, m * (intervalDays / 30))
            } else {
                PersianCalendar.addDays(startDate, m * intervalDays)
            }
            row + ("dueDate" to mapOf("y" to due.y, "m" to due.m, "d" to due.d))
        }
    }

    private fun parseStartDate(data: Map<String, Any?>): PersianDate {
        val sd = data["startDate"] as? Map<*, *>
        val y = (sd?.get("y") as? Number)?.toInt() ?: 1404
        val m = (sd?.get("m") as? Number)?.toInt() ?: 1
        val d = (sd?.get("d") as? Number)?.toInt() ?: 1
        return PersianDate(y, m, d)
    }

    /** پورت handlePayButton برای برگردوندن قسط به حالت پرداخت‌نشده - paidLate/paidDate هم پاک می‌شن. */
    suspend fun setRowUnpaid(loan: LoanEntity, m: Int) = updateRowPayment(loan, m) { row ->
        row + mapOf("paid" to false, "paidLate" to false, "paidDate" to null)
    }

    /** پورت payOnTime تو www/index.html. */
    suspend fun setRowPaidOnTime(loan: LoanEntity, m: Int) = updateRowPayment(loan, m) { row ->
        row + mapOf("paid" to true, "paidLate" to false, "paidDate" to null)
    }

    /** پورت confirmLatePayment تو www/index.html - [paidDate] تاریخ واقعی پرداخته (نه سررسید). */
    suspend fun setRowPaidLate(loan: LoanEntity, m: Int, paidDate: Map<String, Int>) = updateRowPayment(loan, m) { row ->
        row + mapOf("paid" to true, "paidLate" to true, "paidDate" to paidDate)
    }

    /** پیوست عکس رسید مخصوص یه قسطِ خاص (نه یه عکس کلیِ روی خودِ وام) - خواسته‌ی کاربر «مشخص باشه
     * برای کدوم وام و کدوم قسطه» که چون این تابع همیشه با یه [loan] و یه [m] مشخص صدا زده می‌شه،
     * به‌طور طبیعی تضمین می‌شه. */
    suspend fun setRowPhoto(loan: LoanEntity, m: Int, photoPath: String) = updateRowPayment(loan, m) { row ->
        row + ("photoPath" to photoPath)
    }

    suspend fun removeRowPhoto(loan: LoanEntity, m: Int) = updateRowPayment(loan, m) { row ->
        row + ("photoPath" to null)
    }

    private suspend fun updateRowPayment(loan: LoanEntity, m: Int, transform: (Map<String, Any?>) -> Map<String, Any?>) {
        val data = parseDataMutable(loan)
        val rows = rowsFromData(data, loan).map { row ->
            if ((row["m"] as? Number)?.toInt() == m) transform(row) else row
        }
        val newPaidCount = rows.count { it["paid"] == true }
        data["rows"] = rows
        data["paidCount"] = newPaidCount
        loanDao.upsert(loan.copy(paidCount = newPaidCount, dataJson = gson.toJson(data)))
    }

    /** ویرایش دستی مبلغ یه قسط (کارمزد/جریمه‌ی بانکی که نمی‌تونیم حدس بزنیم) - پورت
     * confirmEditInstallment. */
    suspend fun setRowInstallment(loan: LoanEntity, m: Int, newAmount: Double) {
        val data = parseDataMutable(loan)
        val rows = rowsFromData(data, loan).map { row ->
            if ((row["m"] as? Number)?.toInt() == m) row + ("installment" to newAmount) else row
        }
        saveRows(loan, data, rows)
    }

    /** پورت گزینه‌ی «می‌خوای این مبلغ رو برای همه‌ی اقساط اعمال کنی؟» تو confirmEditInstallment -
     * برخلاف وب که فقط اقساطِ همون نتیجه‌ی محاسبه رو عوض می‌کنه، اینجا مستقیم همه‌ی ردیف‌های
     * ذخیره‌شده رو آپدیت می‌کنه (چون :app به rows مستقیم دسترسی نداره، جایی برای «نگه داشتن اون
     * نسخه‌ی موقت تو حافظه» مثل وب نیست). */
    suspend fun setAllRowsInstallment(loan: LoanEntity, newAmount: Double) {
        val data = parseDataMutable(loan)
        val rows = rowsFromData(data, loan).map { row -> row + ("installment" to newAmount) }
        // چون این‌جا واقعاً همه‌ی اقساط برابرِ newAmount شدن، loan.installment (که دایره‌ی بالای
        // صفحه و کارتِ «مبلغ هر قسط» ازش می‌خونن، نه از rows) هم باید هم‌قدمش بشه - وگرنه بعد از
        // «بله، رو همه اعمال کن» دایره‌ی بالا هنوز مبلغِ قدیمی رو نشون می‌ده (باگی که کاربر گزارش داد).
        saveRows(loan, data, rows, installment = newAmount)
    }

    private suspend fun saveRows(
        loan: LoanEntity,
        data: MutableMap<String, Any?>,
        rows: List<Map<String, Any?>>,
        installment: Double? = null,
    ) {
        val newTotal = rows.sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
        data["rows"] = rows
        data["amount"] = newTotal
        data["totalPaid"] = newTotal
        val updated = loan.copy(amount = newTotal, totalPaid = newTotal, dataJson = gson.toJson(data))
        loanDao.upsert(if (installment != null) updated.copy(installment = installment) else updated)
    }

    private fun buildInitialRows(installment: Double, n: Int, paidCount: Int): List<Map<String, Any?>> =
        (1..n).map { m -> mapOf("m" to m, "installment" to installment, "paid" to (m <= paidCount)) }

    private fun rowsFromData(data: Map<String, Any?>, loan: LoanEntity): List<Map<String, Any?>> {
        val raw = (data["rows"] as? List<*>)?.mapNotNull { it as? Map<*, *> }
        return raw?.map { row -> row.entries.associate { it.key.toString() to it.value } }
            ?: buildInitialRows(loan.installment, loan.n, loan.paidCount)
    }

    private fun parseData(loan: LoanEntity): Map<String, Any?> {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        return gson.fromJson(loan.dataJson, type) ?: emptyMap()
    }

    private fun parseDataMutable(loan: LoanEntity): MutableMap<String, Any?> {
        val type = object : TypeToken<MutableMap<String, Any?>>() {}.type
        return gson.fromJson(loan.dataJson, type) ?: mutableMapOf()
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

    /** پورت مفهومی restoreFromServer تو ChequeRepository/AccountRepository - برای «بازیابی از سرور
     * ابری» دستی تو تنظیمات، برخلاف syncAfterLogin که یه‌بار خودکار بعد از ورود صدا زده می‌شه و
     * برخورد داده‌ی محلی/سرور رو مدیریت می‌کنه، این همیشه بی‌قیدوشرط با نسخه‌ی سرور جایگزین می‌کنه. */
    suspend fun restoreFromServer(token: String): Boolean {
        val serverLoans = try {
            apiService.getLoans("Bearer $token").loans
        } catch (e: Exception) {
            return false
        }
        replaceAllWithServerData(serverLoans)
        return true
    }

    /** پورت exportBackup تو www/index.html - همون آرایه‌ی خام (currentLoans) رو به JSON خوانا
     * (pretty-printed) تبدیل می‌کنه تا کاربر با SAF ذخیره‌ش کنه. */
    suspend fun exportBackupJson(): String {
        val loans = loanDao.getAll().map { toWebMap(it) }
        return GsonBuilder().setPrettyPrinting().create().toJson(loans)
    }

    /** پورت importBackup - کل لیست وام‌ها رو با محتوای فایل جایگزین می‌کنه (نه merge)، دقیقاً مثل
     * وب. اگه JSON یه آرایه نباشه (فایل نامعتبر)، false برمی‌گردونه بدون تغییر دادن چیزی. */
    suspend fun importBackupJson(json: String): Boolean {
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        val imported: List<Map<String, Any?>> = try {
            gson.fromJson(json, type) ?: return false
        } catch (e: Exception) {
            return false
        }
        replaceAllWithServerData(imported)
        return true
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
