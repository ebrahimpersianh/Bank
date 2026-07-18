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
) : ViewModel() {
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

    /** پورت saveLoan تو www/index.html - نتیجه‌ی محاسبه‌ی تب «وام بانکی» رو تو «وام‌های من» ذخیره
     * می‌کنه (با نگه‌داشتن ردیف‌های واقعیِ محاسبه‌شده). محدودیتِ «۱ وام رایگان» باید قبلِ صدا زدن
     * این، سمتِ UI چک بشه (مثل onAddLoanClick تو MyLoansScreen). */
    fun saveComputedLoan(outcome: BankLoanOutcome, onSaved: () -> Unit) {
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

    /** پورت rows[].paid تو www/index.html - وضعیت پرداخت هر قسط مستقله، نه یه آستانه‌ی ترتیبی. */
    fun getRows(loan: LoanEntity): List<Map<String, Any?>> = loanRepository.getRows(loan)

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

    /** پیوست/حذف عکس رسیدِ مخصوصِ یه قسطِ خاص (نه یه عکسِ کلیِ رو کل وام) - خواسته‌ی کاربر که مشخص
     * باشه رسید برای کدوم وام و کدوم قسطه؛ چون [loan] و [m] همیشه صریح داده می‌شن، این خودش تضمین
     * می‌شه. عکس قبلیِ همون قسط (اگه بود) قبل از جایگزینی پاک می‌شه. */
    fun setRowPhoto(loan: LoanEntity, m: Int, uri: Uri, previousPath: String?) {
        viewModelScope.launch {
            val newPath = attachmentStorage.copyToInternalStorage(uri) ?: return@launch
            attachmentStorage.delete(previousPath)
            loanRepository.setRowPhoto(loan, m, newPath)
            syncIfLoggedIn()
        }
    }

    fun removeRowPhoto(loan: LoanEntity, m: Int, previousPath: String?) {
        viewModelScope.launch {
            attachmentStorage.delete(previousPath)
            loanRepository.removeRowPhoto(loan, m)
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

    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) loanRepository.pushToServer(token)
    }
}
