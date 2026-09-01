package ir.sadteam.loancalc.ui.myloans

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.ui.BankLoanOutcome
import ir.sadteam.loancalc.data.AttachmentStorage
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.IncomeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.IncomeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * لیست محلی Room + افزودن دستی/حذف/پرداخت قسط رو پشتیبانی می‌کنه. هر تغییر، اگه لاگین باشیم،
 * بی‌صدا به سرور هم پوش می‌شه - پورت persistLoans() تو www/index.html (که همیشه بعد از هر تغییر
 * محلی syncLoansToServer رو صدا می‌زنه).
 */
@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val authPrefs: AuthPrefs,
    private val incomeRepository: IncomeRepository,
    private val attachmentStorage: AttachmentStorage,
    private val debtRepository: DebtRepository,
) : ViewModel() {
    init {
        // ترمیمِ یک‌بارِ وام‌هایی که پیشرفتشون قبلاً صفر شده بود (باگِ گزارش‌شده‌ی کاربر: بعد از
        // خروج و ورودِ دوباره، همه‌ی وام‌ها ۰٪ و «عقب‌افتاده» شدن). ردیف‌های قسط سالم موندن، پس
        // عددِ خلاصه از رو خودشون بازسازی می‌شه - رجوع کن به LoanRepository.repairPaidCounts.
        // اجرای دوباره‌ش بی‌ضرره، پس نیازی به پرچمِ «یه‌بار انجام شد» نیست.
        viewModelScope.launch { loanRepository.repairPaidCounts() }

        // حدسِ خودکارِ طرفِ‌حساب برای وام‌های قدیمی‌ای که قبل از فیچرِ طلب‌وبدهی ساخته شدن (سوالِ ۶).
        // «—» یعنی وامِ دستی بدونِ اسمِ وام‌گیرنده‌ی واقعی، برای همون طرفِ‌حسابِ مشترکِ «نامشخص» می‌ره.
        // اجرای دوباره‌ش بی‌ضرره (فقط وام‌هایی که هنوز counterpartyId ندارن رو برمی‌گردونه).
        viewModelScope.launch {
            loanRepository.loansWithoutCounterparty().forEach { loan ->
                val borrower = loanRepository.getBorrower(loan).takeIf { it != "—" }
                val counterpartyId = debtRepository.guessOrCreateCounterparty(borrower)
                loanRepository.setLoanCounterparty(loan, counterpartyId)
            }
        }
    }

    val loans: StateFlow<List<LoanEntity>> = loanRepository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** برای باکس «تحلیل درآمد» تو داشبورد بالای «وام‌های من» - رجوع کن به DashboardSummary. چند منبع
     * درآمد مستقل (ثابت/متغیر)، نه یه عدد تکی. */
    val incomes: StateFlow<List<IncomeEntity>> = incomeRepository.observeIncomes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addIncome(label: String, amount: Double, type: IncomeType) {
        viewModelScope.launch { incomeRepository.addIncome(label, amount, type) }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch { incomeRepository.deleteIncome(income) }
    }

    fun saveManualLoan(
        name: String,
        bank: String,
        installment: Double,
        n: Int,
        paidCount: Int,
        startDate: PersianDate,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.addManualLoan(
                name = name,
                bank = bank,
                installment = installment,
                n = n,
                paidCount = paidCount,
                startDate = mapOf("y" to startDate.y, "m" to startDate.m, "d" to startDate.d),
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** پورت گیت ویرایشِ مشخصاتِ کلیِ یه وامِ دستیِ ازقبل‌ذخیره‌شده - رجوع کن به
     * [LoanRepository.updateManualLoan]. */
    fun updateManualLoan(
        loan: LoanEntity,
        name: String,
        bank: String,
        installment: Double,
        n: Int,
        startDate: PersianDate,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.updateManualLoan(
                loan = loan,
                name = name,
                bank = bank,
                installment = installment,
                n = n,
                startDate = mapOf("y" to startDate.y, "m" to startDate.m, "d" to startDate.d),
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** آیا این وام دستی‌ه (فرمِ ویرایشِ کاملِ اسم/بانک/مبلغ/تعداد فقط رو این نوع معنی داره) - رجوع
     * کن به [LoanRepository.isManualLoan]. */
    fun isManualLoan(loan: LoanEntity): Boolean = loanRepository.isManualLoan(loan)

    /** تاریخِ شروعِ وام برای پرکردنِ فرمِ ویرایش - رجوع کن به [LoanRepository.getStartDate]. */
    fun getLoanStartDate(loan: LoanEntity): PersianDate = loanRepository.getStartDate(loan)

    /** اسمِ وام‌گیرنده برای پرکردنِ دیالوگِ ویرایشِ وام‌های محاسبه‌شده - رجوع کن به
     * [LoanRepository.getBorrower]. */
    fun getLoanBorrower(loan: LoanEntity): String = loanRepository.getBorrower(loan)

    /** دوره‌ی تنفسِ وام (ماه) - رجوع کن به [LoanRepository.getGraceMonths]. */
    fun getLoanGraceMonths(loan: LoanEntity): Int = loanRepository.getGraceMonths(loan)

    /** نرخِ سالانه (درصد) - برای ردیفِ «سود» تو خلاصه‌ی فریمِ `27b`. */
    fun getLoanRatePct(loan: LoanEntity): Double = loanRepository.getRatePct(loan)

    /** سودِ حذف‌شونده با تسویه‌ی یک‌جا - کارتِ «تسویه‌ی زودتر»ِ فریمِ `27b`. `null` = کارت نیاد. */
    fun earlySettlementSaving(loan: LoanEntity, unpaidTotal: Double): Double? =
        loanRepository.earlySettlementSaving(loan, unpaidTotal)

    /** سررسیدِ اولین قسطِ پرداخت‌نشده - برای مرتب‌سازیِ «نزدیک‌ترین سررسید»، رجوع کن به
     * [LoanRepository.getNextDueDate]. */
    fun getLoanNextDueDate(loan: LoanEntity): PersianDate? = loanRepository.getNextDueDate(loan)

    /** آیا بازپرداختِ این وام عقب‌افتاده (سررسیدِ اولین قسطِ پرداخت‌نشده گذشته)؟ - برای بجِ هشدارِ
     * قرمز رو کارتِ وام، رجوع کن به [LoanRepository.isOverdue]. */
    fun isLoanOverdue(loan: LoanEntity): Boolean = loanRepository.isOverdue(loan)

    /** جمعِ کلِ اقساطِ معوقِ همه‌ی وام‌ها - برای مورد ۱۹ (خلاصه‌ی داشبورد)، رجوع کن به
     * [LoanRepository.overdueInstallmentsTotal]. */
    suspend fun totalOverdueAmount(loans: List<LoanEntity>): Double =
        loans.sumOf { loanRepository.overdueInstallmentsTotal(it) }

    /** جمعِ مبلغِ قسطِ همینِ الانِ همه‌ی وام‌ها («مجموع اقساط ماهانه» تو داشبورد) - مورد ۱۴/۳۵،
     * رجوع کن به [LoanRepository.currentInstallmentAmount]. */
    suspend fun totalCurrentInstallment(loans: List<LoanEntity>): Double =
        loans.sumOf { loanRepository.currentInstallmentAmount(it) }

    /** ترتیبِ دلخواهِ کاربر (کشیدن‌ورهاکردن) - رجوع کن به [LoanRepository.getSortOrder]. */
    fun getLoanSortOrder(loan: LoanEntity): Long? = loanRepository.getSortOrder(loan)

    /** بعدِ رهاکردنِ کارتِ یه وامِ کشیده‌شده - رجوع کن به [LoanRepository.reorderLoans]. */
    fun reorderLoans(orderedLoans: List<LoanEntity>) {
        viewModelScope.launch {
            loanRepository.reorderLoans(orderedLoans)
            syncIfLoggedIn()
        }
    }

    /** یادداشتِ آزادِ وام - رجوع کن به [LoanRepository.getNotes]/[LoanRepository.updateNotes]. */
    fun getLoanNotes(loan: LoanEntity): String = loanRepository.getNotes(loan)

    fun updateLoanNotes(loan: LoanEntity, notes: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            loanRepository.updateNotes(loan, notes)
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** ویرایشِ مشخصاتِ *غیرمالیِ* هر نوع وامی (اسم/بانک/وام‌گیرنده/تاریخ) - رجوع کن به
     * [LoanRepository.updateLoanMeta]. */
    fun updateLoanMeta(
        loan: LoanEntity,
        name: String,
        bank: String,
        borrower: String,
        startDate: PersianDate,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.updateLoanMeta(
                loan = loan,
                name = name,
                bank = bank,
                borrower = borrower,
                startDate = mapOf("y" to startDate.y, "m" to startDate.m, "d" to startDate.d),
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** ویرایشِ مبلغ/تعدادِ اقساطِ یه وامِ محاسبه‌شده - فقط وقتی [loan.paidCount] صفره؛ رجوع کن به
     * [LoanRepository.updateComputedLoanAmount]. */
    fun updateComputedLoanAmount(
        loan: LoanEntity,
        name: String,
        bank: String,
        borrower: String,
        principalAmount: Double,
        n: Int,
        startDate: PersianDate,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            loanRepository.updateComputedLoanAmount(
                loan = loan,
                name = name,
                bank = bank,
                borrower = borrower,
                principalAmount = principalAmount,
                n = n,
                startDate = mapOf("y" to startDate.y, "m" to startDate.m, "d" to startDate.d),
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** پورت saveLoan تو www/index.html - نتیجه‌ی محاسبه‌ی تب «وام بانکی» رو تو «وام‌های من» ذخیره
     * می‌کنه (با نگه‌داشتن ردیف‌های واقعیِ محاسبه‌شده). محدودیتِ «۱ وام رایگان» باید قبلِ صدا زدن
     * این، سمتِ UI چک بشه (مثل onAddLoanClick تو MyLoansScreen). */
    fun saveComputedLoan(outcome: BankLoanOutcome, paidCount: Int = 0, onSaved: () -> Unit) {
        viewModelScope.launch {
            val r = outcome.result
            val loanName = outcome.borrower.takeIf { it != "—" && it.isNotBlank() } ?: outcome.bankName
            loanRepository.saveComputedLoan(
                name = loanName,
                bank = outcome.bankName,
                borrower = outcome.borrower,
                principal = r.principal,
                ratePct = outcome.ratePct,
                n = outcome.n,
                method = if (outcome.method == LoanMethod.QARZ) "qarz" else "standard",
                graceMonths = r.graceMonths,
                installment = r.installment,
                totalPaid = r.totalPaid,
                totalInterest = r.totalInterest,
                startDate = mapOf("y" to outcome.startDate.y, "m" to outcome.startDate.m, "d" to outcome.startDate.d),
                intervalDays = r.intervalDays,
                rows = r.rows.map { it.month to it.installment },
                paidCount = paidCount,
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    fun deleteLoan(id: Long) {
        viewModelScope.launch {
            loanRepository.deleteLoan(id)
            syncIfLoggedIn()
        }
    }

    /** پورت «پیوست عکس رسید» اپ رقیب - عکس انتخابی رو به فضای داخلی اپ کپی می‌کنه، عکس قبلی (اگه بود)
     * رو پاک می‌کنه، و مسیر جدید رو رو خودِ وام ذخیره می‌کنه. */
    fun setLoanPhoto(loan: LoanEntity, uri: Uri) {
        viewModelScope.launch {
            val newPath = attachmentStorage.copyToInternalStorage(uri) ?: return@launch
            attachmentStorage.delete(loan.photoPath)
            loanRepository.saveLoan(loan.copy(photoPath = newPath))
            syncIfLoggedIn()
        }
    }

    fun removeLoanPhoto(loan: LoanEntity) {
        viewModelScope.launch {
            attachmentStorage.delete(loan.photoPath)
            loanRepository.saveLoan(loan.copy(photoPath = null))
            syncIfLoggedIn()
        }
    }

    /** بعد از یه درجِ موفقِ سررسیدها تو تقویم گوشی صدا زده می‌شه - persist می‌کنه تا دکمه‌ی «افزودن
     * سررسیدها» تو LoanDetailScreen دیگه هیچ‌وقت (نه فقط تو همین session) دوباره درج نکنه. */
    fun markCalendarExported(loan: LoanEntity) {
        viewModelScope.launch {
            loanRepository.saveLoan(loan.copy(calendarExported = true))
            syncIfLoggedIn()
        }
    }

    /** یادآوریِ اختصاصیِ این وام رو ست می‌کنه - null یعنی از پیش‌فرضِ سراسری استفاده کن، رشته‌ی خالی
     * یعنی برای این وام کاملاً خاموش باشه، وگرنه CSVِ روزهای انتخاب‌شده. رجوع کن به
     * ReminderSettingsScreen برای پیش‌فرضِ سراسری. */
    fun setLoanReminderOffsets(loan: LoanEntity, offsets: String?) {
        viewModelScope.launch {
            loanRepository.saveLoan(loan.copy(reminderDayOffsets = offsets))
            syncIfLoggedIn()
        }
    }

    /** پورت rows[].paid تو www/index.html - وضعیت پرداخت هر قسط مستقله، نه یه آستانه‌ی ترتیبی. */
    suspend fun getRows(loan: LoanEntity): List<Map<String, Any?>> = loanRepository.getRows(loan)

    /** پورت handlePayButton برای برگردوندن قسط به حالت پرداخت‌نشده. */
    fun setRowUnpaid(loan: LoanEntity, m: Int) {
        viewModelScope.launch {
            loanRepository.setRowUnpaid(loan, m)
            syncIfLoggedIn()
        }
    }

    /** پورت payOnTime تو www/index.html. */
    fun setRowPaidOnTime(loan: LoanEntity, m: Int) {
        viewModelScope.launch {
            loanRepository.setRowPaidOnTime(loan, m)
            syncIfLoggedIn()
        }
    }

    /** پورت confirmLatePayment تو www/index.html - [paidDate] تاریخ واقعیِ پرداخته، نه سررسید. */
    fun setRowPaidLate(loan: LoanEntity, m: Int, paidDate: PersianDate) {
        viewModelScope.launch {
            loanRepository.setRowPaidLate(
                loan,
                m,
                mapOf("y" to paidDate.y, "m" to paidDate.m, "d" to paidDate.d),
            )
            syncIfLoggedIn()
        }
    }

    /** پرداختِ گروهیِ چندتا قسطِ پرداخت‌نشده به‌موقع - رجوع کن به [LoanRepository.setRowsPaidOnTime]. */
    fun setRowsPaidOnTime(loan: LoanEntity, ms: List<Int>) {
        viewModelScope.launch {
            loanRepository.setRowsPaidOnTime(loan, ms)
            syncIfLoggedIn()
        }
    }

    /** پرداختِ گروهیِ چندتا قسط با تاخیر - رجوع کن به [LoanRepository.setRowsPaidLate]. */
    fun setRowsPaidLate(loan: LoanEntity, ms: List<Int>, paidDate: PersianDate) {
        viewModelScope.launch {
            loanRepository.setRowsPaidLate(
                loan,
                ms,
                mapOf("y" to paidDate.y, "m" to paidDate.m, "d" to paidDate.d),
            )
            syncIfLoggedIn()
        }
    }

    /** پیوست/حذف عکس رسیدِ مخصوصِ یه قسطِ خاص (نه یه عکسِ کلیِ رو کل وام) - خواسته‌ی کاربر که مشخص
     * باشه رسید برای کدوم وام و کدوم قسطه؛ چون [loan] و [m] همیشه صریح داده می‌شن، این خودش تضمین
     * می‌شه. عکس قبلیِ همون قسط (اگه بود) قبل از جایگزینی پاک می‌شه. */
    /**
     * ⚠️ [previousPath] دیگه **پاک نمی‌شه**: طرح (کارتِ `36d`) چند عکسِ رسید برای هر قسط
     * می‌خواد، پس عکسِ تازه به لیست **اضافه** می‌شه. برای حذفِ یه عکسِ خاص از
     * [removeRowPhoto] استفاده کن.
     */
    fun setRowPhoto(loan: LoanEntity, m: Int, uri: Uri, previousPath: String? = null) {
        viewModelScope.launch {
            val newPath = attachmentStorage.copyToInternalStorage(uri) ?: return@launch
            loanRepository.setRowPhoto(loan, m, newPath)
            syncIfLoggedIn()
        }
    }

    /** [previousPath] برابرِ null یعنی «همه‌ی عکس‌های این قسط». */
    fun removeRowPhoto(loan: LoanEntity, m: Int, previousPath: String?) {
        viewModelScope.launch {
            attachmentStorage.delete(previousPath)
            if (previousPath == null) {
                loanRepository.removeRowPhoto(loan, m)
            } else {
                loanRepository.removeRowPhoto(loan, m, previousPath)
            }
            syncIfLoggedIn()
        }
    }

    /** یادداشت و شماره‌ی پیگیریِ یه قسط - کارتِ `36d`. */
    fun setRowDetails(loan: LoanEntity, m: Int, note: String?, trackingNumber: String?) {
        viewModelScope.launch {
            loanRepository.setRowDetails(loan, m, note, trackingNumber)
            syncIfLoggedIn()
        }
    }

    /** پورت confirmEditInstallment تو www/index.html - ویرایش دستی مبلغ یه قسط. [onSaved] بعد از
     * ذخیره صدا زده می‌شه تا UI بتونه سوال «رو همه اعمال کنم؟» رو نشون بده. */
    fun setRowInstallment(loan: LoanEntity, m: Int, newAmount: Double, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            loanRepository.setRowInstallment(loan, m, newAmount)
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** پورت «می‌خوای این مبلغ رو برای همه‌ی اقساط اعمال کنی؟» تو confirmEditInstallment. */
    fun setAllRowsInstallment(loan: LoanEntity, newAmount: Double) {
        viewModelScope.launch {
            loanRepository.setAllRowsInstallment(loan, newAmount)
            syncIfLoggedIn()
        }
    }

    /** پورت exportBackup - [onResult] با متن JSON صدا زده می‌شه تا UI با SAF ذخیره‌ش کنه. */
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(loanRepository.exportBackupJson()) }
    }

    /** پورت importBackup - کل لیست وام‌ها رو با محتوای [json] جایگزین می‌کنه. [onResult] با
     * true/false (فایل معتبر بود یا نه) صدا زده می‌شه. */
    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = loanRepository.importBackupJson(json)
            if (ok) syncIfLoggedIn()
            onResult(ok)
        }
    }

    /**
     * همگام‌سازیِ دستی (کشیدنِ لیست به پایین تو [MyLoansScreen]).
     *
     * عمداً فقط **پوش** می‌کنه، نه بازیابی از سرور: یه ژستِ ساده‌ی کشیدن نباید بتونه داده‌ی محلی رو
     * با نسخه‌ی سرور جایگزین کنه (اون کارِ «بازیابی از سرورِ ابری» تو تنظیماته، با تاییدِ صریح).
     * برای کاربرِ مهمان/خارج‌شده هیچ‌کاری نمی‌کنه و فوراً [onDone] رو صدا می‌زنه.
     */
    fun syncNow(onDone: () -> Unit) {
        viewModelScope.launch {
            syncIfLoggedIn()
            onDone()
        }
    }

    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) loanRepository.pushToServer(token)
    }
}
