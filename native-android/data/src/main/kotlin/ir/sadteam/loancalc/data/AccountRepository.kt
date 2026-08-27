package ir.sadteam.loancalc.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.db.AccountDao
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionDao
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetDao
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentDao
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.BackupBlobRequest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * پورت مفهومی ماژول «حساب بانکی» اپ رقیب (VAMMAN) - چند حساب با موجودی اولیه، هر کدوم یه دفترچه‌ی
 * تراکنش (واریز/برداشت) مستقل؛ موجودی فعلی همیشه از رو تراکنش‌ها محاسبه می‌شه (currentBalance)، نه
 * یه فیلد جداگونه که ممکنه از واقعیت جا بمونه.
 */
class AccountRepository(
    private val accountDao: AccountDao,
    private val transactionDao: AccountTransactionDao,
    private val apiService: ApiService,
    private val budgetDao: BudgetDao,
    private val recurringPaymentDao: RecurringPaymentDao,
    /**
     * گیمیفیکیشن - اختیاریه تا مسیرهایی که این ریپازیتوری رو دستی می‌سازن (تست، بکاپ) مجبور
     * نباشن دفترِ سکه هم بسازن. `null` یعنی «سکه‌ای در کار نیست»، نه خطا.
     */
    private val gamification: GamificationRepository? = null,
) {
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()
    fun observeTransactions(): Flow<List<AccountTransactionEntity>> = transactionDao.observeAll()
    fun observeTransactionsForAccount(accountId: Long): Flow<List<AccountTransactionEntity>> =
        transactionDao.observeForAccount(accountId)

    /** [type] یکی از [ACCOUNT_TYPE_BANK] / [ACCOUNT_TYPE_OTHER]. برای نوعِ «منبعِ دیگر» (نقدی،
     * کیفِ پول، کارتِ اعتباری…) `bankName` خالی می‌مونه و به‌جاش [iconKey] نشون داده می‌شه. */
    suspend fun addAccount(
        name: String,
        bankName: String,
        initialBalance: Double,
        cardNumber: String? = null,
        smsSender: String? = null,
        type: String = ACCOUNT_TYPE_BANK,
        iconKey: String? = null,
    ) {
        accountDao.upsert(
            AccountEntity(
                id = System.currentTimeMillis(),
                name = name,
                bankName = bankName,
                initialBalance = initialBalance,
                createdAt = isoNow(),
                cardNumber = cardNumber,
                smsSender = smsSender,
                type = type,
                iconKey = iconKey,
            ),
        )
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.upsert(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.delete(account)
        transactionDao.deleteForAccount(account.id)
    }

    /** پورت پاک‌سازیِ لوکالِ بعد از خروج - رجوع کن به توضیح [ir.sadteam.loancalc.data.LoanRepository.clearLocal]. */
    suspend fun clearLocal() {
        accountDao.clear()
        transactionDao.clear()
        budgetDao.clear()
        recurringPaymentDao.clear()
    }

    suspend fun addTransaction(
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
        /** ⚠️ هر جا تو یه حلقه/پشتِ‌هم چند تراکنش می‌سازی، **شمارنده‌ی صریح پاس بده**: پیش‌فرضِ
         * `System.currentTimeMillis()` تو فراخوانی‌های سریعِ پشتِ‌هم می‌تونه یکی دربیاد و چون
         * DAO از `@Upsert` استفاده می‌کنه، تراکنش‌ها بی‌صدا رو هم نوشته می‌شن (باگِ ثبت‌شده تو
         * CLAUDE.md). برای ثبتِ تکیِ عادی خالی گذاشتنش امنه. */
        id: Long? = null,
    ) {
        transactionDao.upsert(
            AccountTransactionEntity(
                id = id ?: System.currentTimeMillis(),
                accountId = accountId,
                type = type.name,
                amount = amount,
                description = description,
                year = year,
                month = month,
                day = day,
                createdAt = isoNow(),
                category = category,
                sourceType = sourceType,
                sourceId = sourceId,
            ),
        )
        // «هر روزِ ثبتِ تراکنش ۱۰ سکه» (کارتِ `20e`). عمداً اینجاست نه تو ViewModel، تا ثبتِ
        // خودکار از پیامک/اعلانِ بانک هم حساب بشه - اونم فعالیتِ همون روزه. تکرارِ همون روز
        // خودبه‌خود نادیده گرفته می‌شه (ایندکسِ یکتای `(type, dateKey)`).
        gamification?.awardDailyLog()
    }

    /** به‌روزرسانیِ یه تراکنشِ موجود - برای انتقالِ دسته موقعِ حذفِ یه دسته‌بندی. */
    suspend fun updateTransaction(transaction: AccountTransactionEntity) {
        transactionDao.upsert(transaction)
    }

    suspend fun deleteTransaction(transaction: AccountTransactionEntity) {
        transactionDao.delete(transaction)
    }

    /** موجودی فعلی = موجودی اولیه + جمع واریزها - جمع برداشت‌ها. */
    fun currentBalance(account: AccountEntity, transactions: List<AccountTransactionEntity>): Double {
        val forAccount = transactions.filter { it.accountId == account.id }
        val deposits = forAccount.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount }
        val withdrawals = forAccount.filter { it.type == TransactionType.WITHDRAWAL.name }.sumOf { it.amount }
        return account.initialBalance + deposits - withdrawals
    }

    // ---- بودجه‌بندی ----
    fun observeBudgets(): Flow<List<BudgetEntity>> = budgetDao.observeAll()

    /** [accountId] برابرِ null یعنی «همه‌ی حساب‌کتاب‌ها» (پیش‌فرض و رفتارِ قبلی). */
    suspend fun setBudget(
        categoryName: String,
        monthlyCap: Double,
        existingId: Long? = null,
        accountId: Long? = null,
    ) {
        budgetDao.upsert(
            BudgetEntity(
                id = existingId ?: System.currentTimeMillis(),
                categoryName = categoryName,
                monthlyCap = monthlyCap,
                accountId = accountId,
            ),
        )
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.delete(budget)
    }

    /** جمعِ خرجِ هر دسته تو یه ماهِ خاص (فقط برداشت‌ها) - برای مقایسه با سقفِ بودجه. */
    fun spendByCategory(
        transactions: List<AccountTransactionEntity>,
        year: Int,
        month: Int,
        /** فقط خرجِ همین حساب‌کتاب حساب بشه؛ null یعنی همه‌ی حساب‌کتاب‌ها (رفتارِ قبلی). */
        accountId: Long? = null,
    ): Map<String, Double> =
        transactions
            .filter { accountId == null || it.accountId == accountId }
            .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == year && it.month == month && !it.category.isNullOrBlank() }
            .groupBy { it.category!! }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

    // ---- پرداخت‌های تکراری ----
    fun observeRecurringPayments(): Flow<List<RecurringPaymentEntity>> = recurringPaymentDao.observeAll()

    /** پورت suspend (نه Flow) - برای DueDateReminderWorker، هم‌الگو با LoanRepository.getLoans(). */
    suspend fun getRecurringPayments(): List<RecurringPaymentEntity> = recurringPaymentDao.getAll()

    suspend fun addRecurringPayment(
        name: String,
        amount: Double,
        type: TransactionType,
        categoryName: String?,
        accountId: Long?,
        dayOfMonth: Int,
        reminderDayOffsets: String?,
    ) {
        recurringPaymentDao.upsert(
            RecurringPaymentEntity(
                id = System.currentTimeMillis(),
                name = name,
                amount = amount,
                type = type.name,
                categoryName = categoryName,
                accountId = accountId,
                dayOfMonth = dayOfMonth,
                reminderDayOffsets = reminderDayOffsets,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun deleteRecurringPayment(payment: RecurringPaymentEntity) {
        recurringPaymentDao.delete(payment)
    }

    /** پورت جدا از بکاپ وام/چک - یه فایل JSON مستقل برای حساب‌ها و تراکنش‌هاشون. */
    suspend fun exportBackupJson(): String {
        val data = mapOf(
            "accounts" to accountDao.getAll(),
            "transactions" to transactionDao.getAll(),
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
        val accountsJson = gson.toJson(parsed["accounts"] ?: return false)
        val transactionsJson = gson.toJson(parsed["transactions"] ?: return false)
        val accounts: List<AccountEntity> = gson.fromJson(accountsJson, object : TypeToken<List<AccountEntity>>() {}.type)
        val transactions: List<AccountTransactionEntity> =
            gson.fromJson(transactionsJson, object : TypeToken<List<AccountTransactionEntity>>() {}.type)
        accountDao.replaceAll(accounts)
        transactionDao.replaceAll(transactions)
        return true
    }

    /** پورت مفهومی pushToServer تو LoanRepository - fire-and-forget، خطاها عمداً قورت داده می‌شن. */
    suspend fun pushToServer(token: String) {
        try {
            apiService.putAccountsBackup("Bearer $token", BackupBlobRequest(exportBackupJson()))
        } catch (e: Exception) {
            // عمداً نادیده گرفته می‌شه
        }
    }

    /** آخرین بکاپِ ابریِ حساب‌ها/تراکنش‌ها رو می‌گیره و جایگزینِ دیتای محلی می‌کنه. */
    suspend fun restoreFromServer(token: String): Boolean {
        val blob = try {
            apiService.getAccountsBackup("Bearer $token").data
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
