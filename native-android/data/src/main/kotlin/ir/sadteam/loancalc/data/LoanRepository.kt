package ir.sadteam.loancalc.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.LoanCalculator
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.data.db.LoanRowDao
import ir.sadteam.loancalc.data.db.LoanRowEntity
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
 * هم همینجاست.
 *
 * **معماریِ ردیف‌های قسط (بعدِ مرتب‌سازیِ دیتابیس)**: قبلاً آرایه‌ی `rows` (وضعیتِ پرداختِ هر قسط)
 * فقط تویِ [LoanEntity.dataJson] (یه blobِ JSONِ خام) بود. الان جدولِ واقعیِ Roomِ [LoanRowDao]/
 * `loan_rows` منبعِ حقیقتِ ردیف‌هاست - همه‌ی توابعِ این کلاس که قسط رو می‌خونن/می‌نویسن از رو همون
 * جدول کار می‌کنن، نه از رو JSON. [dataJson] فقط دیگه اطلاعاتِ کلیِ وام (اسم/بانک/نرخ/تاریخِ
 * شروع/...) رو نگه می‌داره - رجوع کن به [LoanEntity]/[LoanRowEntity] برای جزئیاتِ کاملِ مهاجرت.
 *
 * [getOrMigrateRows] یه فال‌بکِ خودترمیم‌شونده‌ست: اگه به هر دلیلی (لبه‌ی نادرِ مهاجرت، وامی که با
 * نسخه‌ی خیلی قدیمی‌تر ساخته شده) یه وام تو `loan_rows` هیچ ردیفی نداشت، از رو `dataJson.rows`ِ
 * قدیمی (اگه هنوز اونجا مونده - migrationِ AppDatabase عمداً استریپش نمی‌کنه) یا پیش‌فرضِ اقساطِ
 * برابر بازسازی می‌شه و همون‌جا هم persist می‌شه - یعنی تاریخچه‌ی پرداختِ هیچ کاربری با این تغییر
 * گم نمی‌شه.
 */
class LoanRepository(
    private val loanDao: LoanDao,
    private val loanRowDao: LoanRowDao,
    private val apiService: ApiService,
) {
    private val gson = Gson()

    fun observeLoans(): Flow<List<LoanEntity>> = loanDao.observeAll()

    suspend fun getLoans(): List<LoanEntity> = loanDao.getAll()

    suspend fun saveLoan(loan: LoanEntity) = loanDao.upsert(loan)

    suspend fun deleteLoan(id: Long) {
        loanDao.deleteById(id)
        loanRowDao.deleteForLoan(id)
    }

    /** پورت پاک‌سازیِ لوکالِ بعد از خروج - وگرنه وام‌های همون گوشی زیرِ حسابِ قبلی، موقع ورود با یه
     * شماره‌ی دیگه (که سرورش هنوز خالیه)، تو [syncAfterLogin] به‌جای «سرور خالیه» به «پوشِ محلی به
     * سرور» می‌رفت و اشتباهی وام‌های کاربرِ قبلی رو زیرِ حسابِ جدید آپلود می‌کرد. */
    suspend fun clearLocal() {
        loanDao.clear()
        loanRowDao.clearAll()
    }

    /**
     * پورت saveManualLoan تو www/index.html. [dataJson] دیگه ردیف‌ها رو نگه نمی‌داره (رجوع کن به
     * کامنتِ بالای کلاس) - فقط شکلِ کلیِ وام (name/bank/borrower/amount/rate/n/method/...) که سرور/
     * اپ وب انتظار دارن، برای سینک/بک‌آپ رجوع کن به [toWebMap] که ردیف‌ها رو از رو `loan_rows`
     * دوباره بهش اضافه می‌کنه.
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
        loanRowDao.upsertAll(buildInitialRowEntities(id, installment, n, paidCount))
    }

    /** آیا این وام با فرمِ افزودنِ دستی ساخته شده (نه از رو یه محاسبه‌ی وامِ بانکی/قرض‌الحسنه)؟ فقط
     * وام‌های دستی ساختارِ ساده‌ی «همه‌ی اقساط برابر» دارن که ویرایشِ کلی (اسم/بانک/مبلغ/تعداد) روش
     * معنی داره - رجوع کن به [updateManualLoan]. */
    fun isManualLoan(loan: LoanEntity): Boolean = (parseData(loan)["method"] as? String) == "manual"

    /** تاریخِ شروعِ وام - چون :app مستقیم به Gson دسترسی نداره (فقط :data)، این تابع رو برای پرکردنِ
     * فرمِ ویرایش با تاریخِ فعلیِ وام لازم داریم. */
    fun getStartDate(loan: LoanEntity): PersianDate = parseStartDate(parseData(loan))

    /** اسمِ وام‌گیرنده - برای پرکردنِ فرمِ ویرایشِ وام‌های محاسبه‌شده (رجوع کن به [updateLoanMeta]).
     * وام‌های دستی این فیلد رو ندارن، همیشه «—» برمی‌گردونه. */
    fun getBorrower(loan: LoanEntity): String = (parseData(loan)["borrower"] as? String) ?: "—"

    /** دوره‌ی تنفس (ماه) - برای نشون‌دادنِ توضیحِ دینامیکِ درست کنارِ «تاریخ دریافت وام» تو دیالوگِ
     * ویرایشِ مشخصات (رجوع کن به LoanDetailScreen.showEditMetaDialog): وامِ دستی همیشه ۰ برمی‌گردونه
     * (addManualLoan همیشه graceMonths=0 ذخیره می‌کنه)، وامِ محاسبه‌شده هرچی موقعِ محاسبه بوده. */
    fun getGraceMonths(loan: LoanEntity): Int = (parseData(loan)["graceMonths"] as? Number)?.toInt() ?: 0

    /** یادداشتِ آزادِ کاربر رو این وام (مثلاً شماره حساب/کارت) - تو dataJson ذخیره می‌شه، نیازی به
     * تغییرِ schema نداره. پیش‌فرض رشته‌ی خالی، نه هیچ‌کدومِ وام‌های قدیمی‌تر این کلید رو ندارن. */
    fun getNotes(loan: LoanEntity): String = (parseData(loan)["notes"] as? String) ?: ""

    /** ترتیبِ دلخواهِ کاربر (کشیدن‌ورهاکردنِ کارتِ وام تو MyLoansScreen) - تو dataJson ذخیره می‌شه
     * (`sortOrder`)، نیازی به تغییرِ schema نداره. null یعنی این وام هنوز هیچ‌وقت دستی جابه‌جا نشده -
     * تو مرتب‌سازیِ CUSTOM همیشه آخر می‌افته. */
    fun getSortOrder(loan: LoanEntity): Long? = (parseData(loan)["sortOrder"] as? Number)?.toLong()

    /** بعدِ رهاکردنِ کارتِ یه وامِ کشیده‌شده - همه‌ی وام‌های [orderedLoans] رو به همون ترتیب
     * عددگذاری (۰..n-۱) و ذخیره می‌کنه؛ از این به بعد مرتب‌سازیِ CUSTOM از رو همین عدد کار می‌کنه. */
    suspend fun reorderLoans(orderedLoans: List<LoanEntity>) {
        orderedLoans.forEachIndexed { index, loan ->
            val data = parseDataMutable(loan)
            data["sortOrder"] = index
            loanDao.upsert(loan.copy(dataJson = gson.toJson(data)))
        }
    }

    /** ذخیره‌ی یادداشتِ وام - رو هر نوع وامی (دستی یا محاسبه‌شده) امنه، چون فقط dataJson رو دست
     * می‌زنه، نه مبلغ/نرخ/ردیف‌ها. */
    suspend fun updateNotes(loan: LoanEntity, notes: String) {
        val data = parseDataMutable(loan)
        data["notes"] = notes
        loanDao.upsert(loan.copy(dataJson = gson.toJson(data)))
    }

    /**
     * ویرایشِ مشخصاتِ *غیرمالیِ* هر وامی (دستی یا محاسبه‌شده) - فقط اسم/بانک/وام‌گیرنده/تاریخِ شروع،
     * بدون دست‌زدن به مبلغ/نرخ/تعدادِ اقساط/ردیف‌ها. برخلافِ [updateManualLoan] که مخصوصِ وام‌های
     * دستیه (چون فرضِ «همه‌ی اقساط برابر» می‌کنه)، این یکی رو هر نوع وامی امن‌ه - وام‌های محاسبه‌شده/
     * قرض‌الحسنه ساختارِ اقساطِ نامساوی دارن که بازمحاسبه‌شون نیاز به اجرای دوباره‌ی فرمولِ کاملِ
     * LoanCalculator داره (که یعنی پاک‌شدنِ تاریخچه‌ی پرداخت) - این تابع عمداً وارد اون بحث نمی‌شه.
     */
    suspend fun updateLoanMeta(
        loan: LoanEntity,
        name: String,
        bank: String,
        borrower: String,
        startDate: Map<String, Int>,
    ) {
        val data = parseDataMutable(loan)
        data["name"] = name
        data["bank"] = bank
        data["borrower"] = borrower
        data["startDate"] = startDate
        loanDao.upsert(loan.copy(name = name, bank = bank, dataJson = gson.toJson(data)))
    }

    /**
     * ویرایشِ مشخصاتِ کلیِ یه وامِ دستیِ ازقبل‌ذخیره‌شده (اسم/بانک/مبلغِ هر قسط/تعدادِ کل/تاریخِ
     * شروع) - برخلافِ [setRowInstallment]/[setAllRowsInstallment] که فقط مبلغِ اقساط رو دست می‌زنن.
     * اگه [n] عوض بشه، ردیف‌ها هوشمند بازسازی می‌شن: قسط‌هایی که هنوز تو بازه‌ی جدیدن (وضعیتِ
     * پرداخت/تاخیر/عکسِ رسیدشون) دست‌نخورده می‌مونن، قسط‌های جدید (اگه n بیشتر شده) پرداخت‌نشده اضافه
     * می‌شن، قسط‌های اضافی (اگه n کمتر شده) حذف می‌شن - نه اینکه کل تاریخچه‌ی پرداخت پاک بشه.
     */
    suspend fun updateManualLoan(
        loan: LoanEntity,
        name: String,
        bank: String,
        installment: Double,
        n: Int,
        startDate: Map<String, Int>,
    ) {
        val oldRowsByM = getOrMigrateRows(loan).associateBy { it.m }
        val newRows = (1..n).map { m ->
            oldRowsByM[m]?.copy(installment = installment)
                ?: LoanRowEntity(loanId = loan.id, m = m, installment = installment, paid = false)
        }
        val amount = installment * n
        val newPaidCount = newRows.count { it.paid }
        val data = parseDataMutable(loan)
        data["name"] = name
        data["bank"] = bank
        data["amount"] = amount
        data["installment"] = installment
        data["totalPaid"] = amount
        data["n"] = n
        data["startDate"] = startDate
        data["paidCount"] = newPaidCount
        // اگه این وام از قبلِ مرتب‌سازیِ دیتابیس مونده باشه، دیگه لازم نیست dataJson کپیِ روبه‌زوالِ
        // "rows" رو نگه داره - از این به بعد منبعِ حقیقتِ همین وام قطعاً loan_rows ئه.
        data.remove("rows")
        loanDao.upsert(
            loan.copy(
                name = name,
                bank = bank,
                amount = amount,
                installment = installment,
                totalPaid = amount,
                n = n,
                paidCount = newPaidCount,
                dataJson = gson.toJson(data),
            ),
        )
        loanRowDao.replaceForLoan(loan.id, newRows)
    }

    /**
     * ویرایشِ مبلغ/تعدادِ اقساطِ یه وامِ *محاسبه‌شده* (غیردستی، method != "manual") - برخلافِ
     * [updateManualLoan] که فقط یه ضرب ساده‌ست، اینجا باید کلِ فرمولِ [LoanCalculator] با نرخ/روش/
     * دوره‌ی تنفسِ همین وام دوباره اجرا بشه (چون قسط‌ها لزوماً مساوی نیستن - قرض‌الحسنه). برای همین
     * **فقط وقتی هنوز هیچ قسطی پرداخت نشده** ([loan.paidCount] == 0) صدا زده بشه - وگرنه چون همه‌ی
     * ردیف‌های قبلی با ردیف‌های تازه‌محاسبه‌شده جایگزین می‌شن، تاریخچه‌ی پرداخت/تاخیر/عکسِ رسیدِ
     * قسط‌های قبلاً پرداخت‌شده گم می‌شه. این محدودیت با `require` هم اینجا اجباری شده (نه فقط سمتِ UI)
     * تا اشتباهاً صدا زدنش رو یه وامِ نیمه‌پرداخت‌شده به‌جای پاک‌کردنِ بی‌صدا، کرش کنه.
     */
    suspend fun updateComputedLoanAmount(
        loan: LoanEntity,
        name: String,
        bank: String,
        borrower: String,
        principalAmount: Double,
        n: Int,
        startDate: Map<String, Int>,
    ) {
        require(loan.paidCount == 0) {
            "updateComputedLoanAmount فقط رو وامی که هنوز هیچ قسطی پرداخت نشده مجازه"
        }
        val data = parseData(loan)
        val ratePct = (data["rate"] as? Number)?.toDouble() ?: 0.0
        val method = if ((data["method"] as? String) == "qarz") LoanMethod.QARZ else LoanMethod.STANDARD
        val graceMonths = (data["graceMonths"] as? Number)?.toInt() ?: 0
        val intervalDays = (data["intervalDays"] as? Number)?.toInt() ?: 30
        val result = LoanCalculator.compute(principalAmount, ratePct, n, method, graceMonths, intervalDays)

        val newData = parseDataMutable(loan)
        newData["name"] = name
        newData["bank"] = bank
        newData["borrower"] = borrower
        newData["amount"] = result.principal
        newData["n"] = n
        newData["installment"] = result.installment
        newData["totalPaid"] = result.totalPaid
        newData["totalInterest"] = result.totalInterest
        newData["startDate"] = startDate
        newData["paidCount"] = 0
        newData.remove("rows")
        loanDao.upsert(
            loan.copy(
                name = name,
                bank = bank,
                amount = result.principal,
                installment = result.installment,
                totalPaid = result.totalPaid,
                n = n,
                paidCount = 0,
                dataJson = gson.toJson(newData),
            ),
        )
        loanRowDao.replaceForLoan(
            loan.id,
            result.rows.map { LoanRowEntity(loanId = loan.id, m = it.month, installment = it.installment, paid = false) },
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
        // برای وامی که تازه (نه از امروز) واقعاً وجود داره و کاربر داره از رو محاسبه‌گر واردش
        // می‌کنه - همون قابلیتِ فرمِ افزودنِ دستی (addManualLoan)، این‌جا هم برای وامِ محاسبه‌شده.
        paidCount: Int = 0,
    ): Long {
        val id = System.currentTimeMillis()
        val createdAt = isoNow()
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
            "paidCount" to paidCount,
            "createdAt" to createdAt,
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
                paidCount = paidCount,
                createdAt = createdAt,
                dataJson = gson.toJson(webShape),
            ),
        )
        loanRowDao.upsertAll(
            rows.map { (m, inst) -> LoanRowEntity(loanId = id, m = m, installment = inst, paid = m <= paidCount) },
        )
        return id
    }

    /** پورت rows[].paid تو www/index.html - وضعیت پرداخت هر قسط مستقله (نه یه آستانه‌ی ترتیبی)،
     * به‌علاوه `dueDate` که از startDate/intervalDaysِ خودِ وام محاسبه می‌شه (:app مستقیم به Gson
     * دسترسی نداره، برای همین این محاسبه اینجا تو :data انجام می‌شه، نه تو UI). */
    suspend fun getRows(loan: LoanEntity): List<Map<String, Any?>> {
        val rows = getOrMigrateRows(loan).sortedBy { it.m }
        val data = parseData(loan)
        val startDate = parseStartDate(data)
        val intervalDays = (data["intervalDays"] as? Number)?.toInt() ?: 30
        // باگِ جداگانه‌ی کشف‌شده و رفع‌شده: وام‌های دارای «دوره‌ی تنفس» (graceMonths، فقط وام‌های
        // محاسبه‌شده - وامِ دستی همیشه ۰ داره) قبلاً اینجا اصلاً اعمال نمی‌شد - یعنی بعدِ ذخیره‌شدن،
        // تاریخِ سررسیدِ همه‌ی اقساط بدونِ دوره‌ی تنفس نشون داده می‌شد، درحالی‌که تو پیش‌نمایشِ محاسبه
        // (ResultScreen، قبل از ذخیره) درست اعمال می‌شد - ناهماهنگیِ بینِ پیش‌نمایش و ذخیره. رفع شد
        // با اضافه‌کردنِ همون graceMonths به base، هم‌راستا با ResultScreen.
        val graceMonths = (data["graceMonths"] as? Number)?.toInt() ?: 0
        val base = if (graceMonths > 0) PersianCalendar.addMonths(startDate, graceMonths) else startDate
        return rows.map { row ->
            // قسطِ ۱ سررسیدش خودِ base ئه (خواسته‌ی صریحِ کاربر - تاریخی که تو «تاریخ دریافت وام»
            // می‌زنه مستقیم سررسیدِ قسطِ اول باشه، نه یه دوره جلوتر که رفتارِ قبلی/بانکیِ استاندارد
            // بود؛ برای وام‌هایی که دوره‌ی تنفس دارن، قسطِ اول base ئه که خودش startDate+graceMonths
            // ئه، نه startDate) - برای همین (row.m - 1) به‌جای row.m.
            // فاصله‌های مضربِ ۳۰ (ماهانه/دوماهه/سه‌ماهه) ماهِ تقویمیِ واقعی جلو می‌رن و روزِ ماه
            // ثابت می‌مونه (با clamp آخرِ ماه، رفعِ باگِ قدیمیِ لغزشِ روز)؛ فقط هفتگی/دوهفته‌ای
            // (۷/۱۴) روزشمار می‌مونن. توجه: این فقط «تاریخِ نمایشیِ» سررسیده - فرمولِ مالی
            // (i = rate×interval/365 تو LoanCalculator) عمداً همون interval قبلی رو نگه می‌داره،
            // رجوع کن به CLAUDE.md.
            val due = if (intervalDays % 30 == 0) {
                PersianCalendar.addMonths(base, (row.m - 1) * (intervalDays / 30))
            } else {
                PersianCalendar.addDays(base, (row.m - 1) * intervalDays)
            }
            row.toRowMap() + ("dueDate" to mapOf("y" to due.y, "m" to due.m, "d" to due.d))
        }
    }

    /** سررسیدِ اولین قسطِ پرداخت‌نشده - برای مرتب‌سازیِ «نزدیک‌ترین سررسید» تو MyLoansScreen. عمداً
     * کاملاً سینکرونه (فقط از رو dataJson/paidCountِ خودِ [loan]، بدونِ کوئریِ loan_rows) چون این
     * تابع باید رو کلِ لیستِ وام‌ها (مرتب‌سازیِ محلی) بدونِ suspend/کوروتین اجرا بشه - همون فرمولِ
     * تاریخِ [getRows] رو تکرار می‌کنه، فقط برای شماره‌قسطِ `paidCount + 1`. اگه وام تسویه شده
     * (paidCount >= n) یا اصلاً قسطی نداره، null برمی‌گردونه (تو مرتب‌سازی همیشه آخر می‌افته). */
    fun getNextDueDate(loan: LoanEntity): PersianDate? {
        if (loan.n <= 0 || loan.paidCount >= loan.n) return null
        val data = parseData(loan)
        val startDate = parseStartDate(data)
        val intervalDays = (data["intervalDays"] as? Number)?.toInt() ?: 30
        val graceMonths = (data["graceMonths"] as? Number)?.toInt() ?: 0
        val base = if (graceMonths > 0) PersianCalendar.addMonths(startDate, graceMonths) else startDate
        val m = loan.paidCount + 1
        return if (intervalDays % 30 == 0) {
            PersianCalendar.addMonths(base, (m - 1) * (intervalDays / 30))
        } else {
            PersianCalendar.addDays(base, (m - 1) * intervalDays)
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
        row.copy(paid = false, paidLate = false, paidDateY = null, paidDateM = null, paidDateD = null)
    }

    /** پورت payOnTime تو www/index.html. */
    suspend fun setRowPaidOnTime(loan: LoanEntity, m: Int) = updateRowPayment(loan, m) { row ->
        row.copy(paid = true, paidLate = false, paidDateY = null, paidDateM = null, paidDateD = null)
    }

    /** پورت confirmLatePayment تو www/index.html - [paidDate] تاریخ واقعی پرداخته (نه سررسید). */
    suspend fun setRowPaidLate(loan: LoanEntity, m: Int, paidDate: Map<String, Int>) = updateRowPayment(loan, m) { row ->
        row.copy(paid = true, paidLate = true, paidDateY = paidDate["y"], paidDateM = paidDate["m"], paidDateD = paidDate["d"])
    }

    /** پیوست عکس رسید مخصوص یه قسطِ خاص (نه یه عکس کلیِ روی خودِ وام) - خواسته‌ی کاربر «مشخص باشه
     * برای کدوم وام و کدوم قسطه» که چون این تابع همیشه با یه [loan] و یه [m] مشخص صدا زده می‌شه،
     * به‌طور طبیعی تضمین می‌شه. */
    suspend fun setRowPhoto(loan: LoanEntity, m: Int, photoPath: String) = updateRowPayment(loan, m) { row ->
        row.copy(photoPath = photoPath)
    }

    suspend fun removeRowPhoto(loan: LoanEntity, m: Int) = updateRowPayment(loan, m) { row ->
        row.copy(photoPath = null)
    }

    /** پرداختِ گروهیِ چندتا قسط باهم (خواسته‌ی کاربر: به‌جای تک‌تک زدنِ هرکدوم، چندتا رو انتخاب کنه
     * و یه‌جا «به‌موقع» علامت بزنه) - رجوع کن به [ir.sadteam.loancalc.ui.myloans.LoanDetailScreen]. */
    suspend fun setRowsPaidOnTime(loan: LoanEntity, ms: List<Int>) = updateRowsPayment(loan, ms) { row ->
        row.copy(paid = true, paidLate = false, paidDateY = null, paidDateM = null, paidDateD = null)
    }

    /** معادلِ گروهیِ [setRowPaidLate] - همون [paidDate] برای همه‌ی [ms] اعمال می‌شه. */
    suspend fun setRowsPaidLate(loan: LoanEntity, ms: List<Int>, paidDate: Map<String, Int>) = updateRowsPayment(loan, ms) { row ->
        row.copy(paid = true, paidLate = true, paidDateY = paidDate["y"], paidDateM = paidDate["m"], paidDateD = paidDate["d"])
    }

    private suspend fun updateRowPayment(loan: LoanEntity, m: Int, transform: (LoanRowEntity) -> LoanRowEntity) {
        val rows = getOrMigrateRows(loan)
        val current = rows.firstOrNull { it.m == m }
            ?: LoanRowEntity(loanId = loan.id, m = m, installment = loan.installment, paid = false)
        loanRowDao.upsertAll(listOf(transform(current)))
        val newPaidCount = loanRowDao.getForLoan(loan.id).count { it.paid }
        loanDao.upsert(loan.copy(paidCount = newPaidCount))
    }

    /** هم‌الگو با [updateRowPayment] ولی رو چندتا قسط باهم - یه upsertAll/یه محاسبه‌ی paidCount
     * برای کلِ دسته، نه یکی جدا به‌ازای هر قسط (کارآمدتر + یه سینکِ سرور به‌جای N تا). */
    private suspend fun updateRowsPayment(loan: LoanEntity, ms: List<Int>, transform: (LoanRowEntity) -> LoanRowEntity) {
        if (ms.isEmpty()) return
        val rows = getOrMigrateRows(loan).associateBy { it.m }
        val updated = ms.map { m ->
            val current = rows[m] ?: LoanRowEntity(loanId = loan.id, m = m, installment = loan.installment, paid = false)
            transform(current)
        }
        loanRowDao.upsertAll(updated)
        val newPaidCount = loanRowDao.getForLoan(loan.id).count { it.paid }
        loanDao.upsert(loan.copy(paidCount = newPaidCount))
    }

    /** ویرایش دستی مبلغ یه قسط (کارمزد/جریمه‌ی بانکی که نمی‌تونیم حدس بزنیم) - پورت
     * confirmEditInstallment. */
    suspend fun setRowInstallment(loan: LoanEntity, m: Int, newAmount: Double) {
        val rows = getOrMigrateRows(loan).map { if (it.m == m) it.copy(installment = newAmount) else it }
        saveRows(loan, rows)
    }

    /** پورت گزینه‌ی «می‌خوای این مبلغ رو برای همه‌ی اقساط اعمال کنی؟» تو confirmEditInstallment -
     * برخلاف وب که فقط اقساطِ همون نتیجه‌ی محاسبه رو عوض می‌کنه، اینجا مستقیم همه‌ی ردیف‌های
     * ذخیره‌شده رو آپدیت می‌کنه (چون :app به rows مستقیم دسترسی نداره، جایی برای «نگه داشتن اون
     * نسخه‌ی موقت تو حافظه» مثل وب نیست). */
    suspend fun setAllRowsInstallment(loan: LoanEntity, newAmount: Double) {
        val rows = getOrMigrateRows(loan).map { it.copy(installment = newAmount) }
        // چون این‌جا واقعاً همه‌ی اقساط برابرِ newAmount شدن، loan.installment (که دایره‌ی بالای
        // صفحه و کارتِ «مبلغ هر قسط» ازش می‌خونن، نه از rows) هم باید هم‌قدمش بشه - وگرنه بعد از
        // «بله، رو همه اعمال کن» دایره‌ی بالا هنوز مبلغِ قدیمی رو نشون می‌ده (باگی که کاربر گزارش داد).
        saveRows(loan, rows, installment = newAmount)
    }

    private suspend fun saveRows(loan: LoanEntity, rows: List<LoanRowEntity>, installment: Double? = null) {
        loanRowDao.upsertAll(rows)
        val newTotal = rows.sumOf { it.installment }
        val updated = loan.copy(amount = newTotal, totalPaid = newTotal)
        loanDao.upsert(if (installment != null) updated.copy(installment = installment) else updated)
    }

    /** ردیف‌های این وام رو از `loan_rows` می‌خونه؛ اگه خالی بود (لبه‌ی نادرِ مهاجرت یا وامِ خیلی
     * قدیمی)، از رو dataJsonِ قدیمی/پیش‌فرضِ اقساطِ برابر بازسازی و همون‌جا persist می‌کنه - رجوع کن
     * به کامنتِ بالای کلاس. */
    private suspend fun getOrMigrateRows(loan: LoanEntity): List<LoanRowEntity> {
        val stored = loanRowDao.getForLoan(loan.id)
        if (stored.isNotEmpty()) return stored
        val legacy = legacyRowsFromDataJson(loan).map { it.toLoanRowEntity(loan.id) }
        if (legacy.isNotEmpty()) loanRowDao.upsertAll(legacy)
        return legacy
    }

    private fun buildInitialRowEntities(loanId: Long, installment: Double, n: Int, paidCount: Int): List<LoanRowEntity> =
        (1..n).map { m -> LoanRowEntity(loanId = loanId, m = m, installment = installment, paid = m <= paidCount) }

    private fun buildInitialRows(installment: Double, n: Int, paidCount: Int): List<Map<String, Any?>> =
        (1..n).map { m -> mapOf("m" to m, "installment" to installment, "paid" to (m <= paidCount)) }

    /** فقط برای فال‌بکِ [getOrMigrateRows] - پارسِ دستیِ همون شکلِ آرایه‌ی "rows" که قبلاً تویِ
     * dataJson بود (برای وام‌های خیلی قدیمی که migrationِ AppDatabase بنا به دلیلی موقعِ
     * جمع‌آوری‌شون رد کرده). */
    private fun legacyRowsFromDataJson(loan: LoanEntity): List<Map<String, Any?>> {
        val data = parseData(loan)
        val raw = (data["rows"] as? List<*>)?.mapNotNull { it as? Map<*, *> }
        return raw?.map { row -> row.entries.associate { it.key.toString() to it.value } }
            ?: buildInitialRows(loan.installment, loan.n, loan.paidCount)
    }

    private fun LoanRowEntity.toRowMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "m" to m,
            "installment" to installment,
            "paid" to paid,
        )
        if (paidLate) map["paidLate"] = true
        if (paidDateY != null && paidDateM != null && paidDateD != null) {
            map["paidDate"] = mapOf("y" to paidDateY, "m" to paidDateM, "d" to paidDateD)
        }
        if (photoPath != null) map["photoPath"] = photoPath
        return map
    }

    private fun Map<String, Any?>.toLoanRowEntity(loanId: Long): LoanRowEntity {
        val m = (this["m"] as? Number)?.toInt() ?: 1
        val installment = (this["installment"] as? Number)?.toDouble() ?: 0.0
        val paid = this["paid"] == true
        val paidLate = this["paidLate"] == true
        val paidDate = this["paidDate"] as? Map<*, *>
        val photoPath = this["photoPath"] as? String
        return LoanRowEntity(
            loanId = loanId,
            m = m,
            installment = installment,
            paid = paid,
            paidLate = paidLate,
            paidDateY = (paidDate?.get("y") as? Number)?.toInt(),
            paidDateM = (paidDate?.get("m") as? Number)?.toInt(),
            paidDateD = (paidDate?.get("d") as? Number)?.toInt(),
            photoPath = photoPath,
        )
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
        val parsed = serverLoans.mapNotNull { fromWebMap(it) }
        loanDao.replaceAll(parsed.map { it.first })
        loanRowDao.clearAll()
        parsed.forEach { (entity, rows) -> if (rows.isNotEmpty()) loanRowDao.upsertAll(rows) }
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

    /** شکلِ کاملِ وب (meta + rows) - ردیف‌ها همیشه تازه از رو `loan_rows` بازسازی می‌شن (نه از رو
     * کپیِ احتمالاً روبه‌زوالِ dataJson) تا سرور/بک‌آپ همیشه آخرین وضعیتِ واقعیِ پرداخت رو بگیره. */
    private suspend fun toWebMap(entity: LoanEntity): Map<String, Any?> {
        val meta = parseDataMutable(entity)
        val rows = getOrMigrateRows(entity).sortedBy { it.m }.map { it.toRowMap() }
        meta["rows"] = rows
        return meta
    }

    /** پارسِ یه وامِ کاملِ اومده از سرور/بک‌آپ - metaی وام رو تویِ [LoanEntity.dataJson] (بدونِ
     * "rows") و ردیف‌ها رو جدا برمی‌گردونه تا صدازننده هر دو رو تو جدولِ درستشون ذخیره کنه. */
    private fun fromWebMap(map: Map<String, Any?>): Pair<LoanEntity, List<LoanRowEntity>>? {
        val id = (map["id"] as? Number)?.toLong() ?: return null
        val name = map["name"] as? String ?: return null
        val bank = map["bank"] as? String ?: ""
        val amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
        val installment = (map["installment"] as? Number)?.toDouble() ?: 0.0
        val totalPaid = (map["totalPaid"] as? Number)?.toDouble() ?: amount
        val n = (map["n"] as? Number)?.toInt() ?: 0
        val paidCount = (map["paidCount"] as? Number)?.toInt() ?: 0
        val createdAt = map["createdAt"] as? String ?: isoNow()
        val rows = (map["rows"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.map { row -> row.entries.associate { it.key.toString() to it.value }.toLoanRowEntity(id) }
            ?: emptyList()
        val metaOnly = map.toMutableMap().apply { remove("rows") }
        val entity = LoanEntity(
            id = id,
            name = name,
            bank = bank,
            amount = amount,
            installment = installment,
            totalPaid = totalPaid,
            n = n,
            paidCount = paidCount,
            createdAt = createdAt,
            dataJson = gson.toJson(metaOnly),
        )
        return entity to rows
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
