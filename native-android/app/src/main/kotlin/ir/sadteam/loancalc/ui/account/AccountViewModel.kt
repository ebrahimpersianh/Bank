package ir.sadteam.loancalc.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authPrefs: AuthPrefs,
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
) : ViewModel() {
    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<AccountTransactionEntity>> = accountRepository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = accountRepository.observeBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringPayments: StateFlow<List<RecurringPaymentEntity>> = accountRepository.observeRecurringPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** پورت syncIfLoggedIn تو MyLoansViewModel - حساب‌ها/تراکنش‌ها قبلاً فقط با AutoBackupWorkerِ
     * روزانه سینک می‌شدن، نه بعد از هر تغییر؛ کاربر خواسته با کوچیک‌ترین تغییری هم بی‌صدا آنلاین
     * بکاپ بگیره، دقیقاً مثل وام‌ها. */
    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) accountRepository.pushToServer(token)
    }

    fun balanceOf(account: AccountEntity, allTransactions: List<AccountTransactionEntity>): Double =
        accountRepository.currentBalance(account, allTransactions)

    fun addAccount(name: String, bankName: String, initialBalance: Double, cardNumber: String? = null) {
        viewModelScope.launch {
            accountRepository.addAccount(name, bankName, initialBalance, cardNumber)
            syncIfLoggedIn()
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
            syncIfLoggedIn()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
            syncIfLoggedIn()
        }
    }

    fun addTransaction(
        accountId: Long,
        type: TransactionType,
        amount: Double,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        category: String? = null,
        sourceType: String? = null,
        sourceId: String? = null,
    ) {
        viewModelScope.launch {
            accountRepository.addTransaction(accountId, type, amount, description, year, month, day, category, sourceType, sourceId)
            syncIfLoggedIn()
        }
    }

    /** همگام‌سازیِ گذشته‌نگر (خواسته‌ی صریحِ کاربر: «قرار شد کل برنامه سینک باشه») - قسط‌های وامِ
     * پرداخت‌شده و چک‌های پاس‌شده که هنوز تراکنشِ حسابداریِ متناظر ندارن (چون سینکِ خودکار فقط از
     * لحظه‌ی اضافه‌شدنش به بعد کار می‌کنه - رجوع کن به LoanDetailScreen/ChequeDetailScreen) رو به
     * [accountId] نسبت می‌ده. idempotent - اگه دوباره اجرا بشه (مثلاً بعدِ ثبتِ قسطِ جدید)، چیزی رو
     * که قبلاً ساخته دوباره نمی‌سازه (رجوع کن به sourceType/sourceId تو AccountTransactionEntity).
     *
     * قبل از ساختنِ تراکنشِ تازه، اول سعی می‌کنه قسط/چک رو به یه تراکنشِ **تگ‌نشده**‌ی موجود (از سینکِ
     * قدیمی‌تر، قبل از اضافه‌شدنِ sourceType/sourceId - رجوع کن به CLAUDE.md) با تطبیقِ مبلغ+توضیح
     * وصل (retag) کنه، نه اینکه یه تراکنشِ تکراری بسازه. هر تراکنشِ تگ‌نشده فقط یه‌بار مصرف می‌شه
     * (pool) - وگرنه چند قسطِ هم‌مبلغ همه با یه تراکنشِ قدیمیِ تنها «پوشش‌داده‌شده» حساب می‌شدن.
     *
     * عددِ تراکنش‌هایی که تازه ساخته شدن رو برمی‌گردونه (retagِ تراکنشِ قدیمی جزوِ این عدد نیست، چون
     * چیزِ جدیدی به حسابداری اضافه نشده). */
    suspend fun backfillHistoricalTransactions(accountId: Long): Int {
        val existing = accountRepository.getAllTransactions()
        val coveredLoanMonths: MutableMap<Long, MutableSet<Int>> = mutableMapOf()
        existing.filter { it.sourceType == "loan" && it.sourceId != null }.forEach { tx ->
            val parts = tx.sourceId!!.split(":")
            val loanId = parts.getOrNull(0)?.toLongOrNull()
            if (parts.size == 2 && loanId != null) {
                val months = parts[1].split(",").mapNotNull { it.toIntOrNull() }
                coveredLoanMonths.getOrPut(loanId) { mutableSetOf() }.addAll(months)
            }
        }
        val coveredChequeIds: MutableSet<Long> = existing
            .filter { it.sourceType == "cheque" }
            .mapNotNull { it.sourceId?.toLongOrNull() }
            .toMutableSet()
        val untaggedPool: MutableList<AccountTransactionEntity> = existing
            .filter { it.sourceType == null && it.category == "قسط/چک" }
            .toMutableList()

        var nextId = System.currentTimeMillis()
        var count = 0

        for (loan in loanRepository.getLoans()) {
            val covered = coveredLoanMonths.getOrPut(loan.id) { mutableSetOf() }
            for (row in loanRepository.getRows(loan)) {
                if (row["paid"] != true) continue
                val m = (row["m"] as? Number)?.toInt() ?: continue
                if (m in covered) continue
                val amount = (row["installment"] as? Number)?.toDouble() ?: continue

                val legacyMatch = untaggedPool.firstOrNull { it.amount == amount && it.description.contains(loan.name) }
                if (legacyMatch != null) {
                    untaggedPool.remove(legacyMatch)
                    accountRepository.retagTransaction(legacyMatch, "loan", "${loan.id}:$m")
                    covered.add(m)
                    continue
                }

                val paidDate = row["paidDate"] as? Map<*, *>
                val dueDate = row["dueDate"] as? Map<*, *>
                val y = (paidDate?.get("y") as? Number)?.toInt() ?: (dueDate?.get("y") as? Number)?.toInt() ?: continue
                val mo = (paidDate?.get("m") as? Number)?.toInt() ?: (dueDate?.get("m") as? Number)?.toInt() ?: continue
                val d = (paidDate?.get("d") as? Number)?.toInt() ?: (dueDate?.get("d") as? Number)?.toInt() ?: 1
                accountRepository.addTransaction(
                    accountId = accountId,
                    type = TransactionType.WITHDRAWAL,
                    amount = amount,
                    description = "قسط ${toFa(m)} - ${loan.name}",
                    year = y,
                    month = mo,
                    day = d,
                    category = "قسط/چک",
                    sourceType = "loan",
                    sourceId = "${loan.id}:$m",
                    id = nextId++,
                )
                covered.add(m)
                count++
            }
        }

        for (cheque in chequeRepository.getAllCheques()) {
            if (cheque.status != ChequeStatus.PASSED.name) continue
            if (cheque.id in coveredChequeIds) continue

            val legacyMatch = untaggedPool.firstOrNull { it.amount == cheque.amount && it.description.contains(cheque.ownerName) }
            if (legacyMatch != null) {
                untaggedPool.remove(legacyMatch)
                accountRepository.retagTransaction(legacyMatch, "cheque", cheque.id.toString())
                coveredChequeIds.add(cheque.id)
                continue
            }

            val typeLabel = if (cheque.type == "RECEIVED") "دریافتی" else "پرداختی"
            accountRepository.addTransaction(
                accountId = accountId,
                type = if (cheque.type == "RECEIVED") TransactionType.DEPOSIT else TransactionType.WITHDRAWAL,
                amount = cheque.amount,
                description = "چک ${typeLabel} - ${cheque.ownerName}",
                year = cheque.dueYear,
                month = cheque.dueMonth,
                day = cheque.dueDay,
                category = "قسط/چک",
                sourceType = "cheque",
                sourceId = cheque.id.toString(),
                id = nextId++,
            )
            coveredChequeIds.add(cheque.id)
            count++
        }

        if (count > 0) syncIfLoggedIn()
        return count
    }

    fun deleteTransaction(transaction: AccountTransactionEntity) {
        viewModelScope.launch {
            accountRepository.deleteTransaction(transaction)
            syncIfLoggedIn()
        }
    }

    /** جمعِ درآمد/هزینه‌ی یه ماهِ خاص، رو همه‌ی حساب‌ها - برای کارتِ گزارشِ ماهانه. */
    fun monthlyTotals(allTransactions: List<AccountTransactionEntity>, year: Int, month: Int): Pair<Double, Double> {
        val forMonth = allTransactions.filter { it.year == year && it.month == month }
        val income = forMonth.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount }
        val expense = forMonth.filter { it.type == TransactionType.WITHDRAWAL.name }.sumOf { it.amount }
        return income to expense
    }

    fun spendByCategory(allTransactions: List<AccountTransactionEntity>, year: Int, month: Int): Map<String, Double> =
        accountRepository.spendByCategory(allTransactions, year, month)

    fun setBudget(categoryName: String, monthlyCap: Double, existingId: Long? = null) {
        viewModelScope.launch {
            accountRepository.setBudget(categoryName, monthlyCap, existingId)
            syncIfLoggedIn()
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            accountRepository.deleteBudget(budget)
            syncIfLoggedIn()
        }
    }

    fun addRecurringPayment(
        name: String,
        amount: Double,
        type: TransactionType,
        categoryName: String?,
        accountId: Long?,
        dayOfMonth: Int,
        reminderDayOffsets: String?,
    ) {
        viewModelScope.launch {
            accountRepository.addRecurringPayment(name, amount, type, categoryName, accountId, dayOfMonth, reminderDayOffsets)
            syncIfLoggedIn()
        }
    }

    fun deleteRecurringPayment(payment: RecurringPaymentEntity) {
        viewModelScope.launch {
            accountRepository.deleteRecurringPayment(payment)
            syncIfLoggedIn()
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(accountRepository.exportBackupJson()) }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = accountRepository.importBackupJson(json)
            if (ok) syncIfLoggedIn()
            onResult(ok)
        }
    }
}
