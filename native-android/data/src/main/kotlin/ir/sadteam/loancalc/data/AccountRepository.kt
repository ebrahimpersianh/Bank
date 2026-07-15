package ir.sadteam.loancalc.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.db.AccountDao
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionDao
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
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
) {
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()
    fun observeTransactions(): Flow<List<AccountTransactionEntity>> = transactionDao.observeAll()
    fun observeTransactionsForAccount(accountId: Long): Flow<List<AccountTransactionEntity>> =
        transactionDao.observeForAccount(accountId)

    suspend fun addAccount(name: String, bankName: String, initialBalance: Double) {
        accountDao.upsert(
            AccountEntity(
                id = System.currentTimeMillis(),
                name = name,
                bankName = bankName,
                initialBalance = initialBalance,
                createdAt = isoNow(),
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

    suspend fun addTransaction(
        accountId: Long,
        type: TransactionType,
        amount: Double,
        description: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        transactionDao.upsert(
            AccountTransactionEntity(
                id = System.currentTimeMillis(),
                accountId = accountId,
                type = type.name,
                amount = amount,
                description = description,
                year = year,
                month = month,
                day = day,
                createdAt = isoNow(),
            ),
        )
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

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
