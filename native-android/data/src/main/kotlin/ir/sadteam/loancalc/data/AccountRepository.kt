package ir.sadteam.loancalc.data

import kotlinx.coroutines.flow.first
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.core.TransactionType
import androidx.room.withTransaction
import ir.sadteam.loancalc.data.db.AccountDao
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionDao
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.AppDatabase
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

/** نشانِ دو سمتِ یک جابه‌جاییِ داخلی. هر جا خرج/درآمدِ واقعی می‌شماریم باید کنار گذاشته شود. */
const val SOURCE_TYPE_TRANSFER = "transfer"

/** دو پیامکِ یک جابه‌جایی معمولاً چند ثانیه تا چند دقیقه فاصله دارند؛ دو ساعت حاشیه‌ی امن است. */
/** برچسبِ سمتِ واریزی که از روی شماره‌ی مقصدِ پیامکِ برداشت ساخته شده. */
const val AUTO_DEST_LEG_LABEL = "تشخیصِ مقصدِ پیامک"

const val TRANSFER_PAIR_WINDOW_MS = 2 * 60 * 60 * 1000L

/** کلیدِ این بخش در جدولِ نسخه‌های ابری. */
private const val CLOUD_MODULE = "accounts"

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
    /**
     * 🚨 **برای اتمیک‌بودنِ کارهای چندجدولی** (جابه‌جایی بینِ حساب‌ها، حذفِ حساب با
     * تراکنش‌هایش). اختیاری است تا مسیرهایی که این ریپازیتوری را دستی می‌سازند (تست،
     * بکاپ) مجبور نباشند دیتابیس پاس بدهند؛ `null` یعنی «بدونِ تراکنشِ دیتابیس اجرا کن»،
     * یعنی دقیقاً رفتارِ قبلی.
     */
    private val database: AppDatabase? = null,
    /** برای نگه‌داشتنِ نسخه‌ی ابری (کنترلِ هم‌زمانی) - اختیاری، `null` یعنی رفتارِ قدیمی. */
    private val uiPrefs: ir.sadteam.loancalc.data.prefs.UiPrefs? = null,
) {
    /** بدنه را داخلِ یک تراکنشِ دیتابیس اجرا می‌کند - یا اگر دیتابیسی نداریم، همان‌طور. */
    private suspend fun <T> inTransaction(block: suspend () -> T): T {
        val db = database ?: return block()
        return db.withTransaction { block() }
    }
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
        accountNumber: String? = null,
        sheba: String? = null,
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
                accountNumber = accountNumber,
                sheba = sheba,
            ),
        )
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.upsert(account)
    }

    /** 🚨 **یک تراکنشِ دیتابیس، نه دو نوشتنِ پشتِ‌هم**: اگر بینِ حذفِ حساب و حذفِ
     * تراکنش‌هایش کار نیمه‌کاره می‌مانْد، ردیف‌های یتیمِ بی‌حساب باقی می‌ماندند. */
    suspend fun deleteAccount(account: AccountEntity) = inTransaction {
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
        /** `false` فقط برای ثبتِ **خودکار** از پیامک/اعلانِ بانکی - تا تاییدِ کاربر رو موجودی
         * اثر نمی‌ذاره. ثبتِ دستیِ خودِ کاربر همیشه تاییدشده‌ست. */
        confirmed: Boolean = true,
        /** «اعلانِ بلوبانک» / «پیامکِ ۲۰۰۰۱۵»؛ `null` یعنی ثبتِ دستیِ خودِ کاربر (`71a`). */
        originLabel: String? = null,
        receiptPath: String? = null,
        tags: String? = null,
        reimbursable: Boolean = false,
    ): Long {
        val txId = id ?: System.currentTimeMillis()
        transactionDao.upsert(
            AccountTransactionEntity(
                id = txId,
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
                confirmed = confirmed,
                originLabel = originLabel,
                receiptPath = receiptPath,
                tags = tags,
                reimbursable = reimbursable,
            ),
        )
        // «هر روزِ ثبتِ تراکنش ۱۰ سکه» (کارتِ `20e`). عمداً اینجاست نه تو ViewModel، تا ثبتِ
        // خودکار از پیامک/اعلانِ بانک هم حساب بشه - اونم فعالیتِ همون روزه. تکرارِ همون روز
        // خودبه‌خود نادیده گرفته می‌شه (ایندکسِ یکتای `(type, dateKey)`).
        // تراکنشِ تاییدنشده هنوز «فعالیتِ کاربر» نیست - سکه‌ش موقعِ تایید داده می‌شه، نه الان.
        // پاداش نباید ثبتِ پولیِ کاربر را زمین بزند: تراکنش از قبل ذخیره شده و خطای
        // جانبیِ سکه (مثلاً دیتابیسِ قدیمی یا رویدادِ تکراری) نباید برنامه را ببندد.
        if (confirmed) runCatching { gamification?.awardDailyLog() }
        return txId
    }

    /**
     * جابه‌جاییِ پول بینِ دو حسابِ خودِ کاربر - **هر دو سمت با هم، یا هیچ‌کدام**.
     *
     * 🚨 تا امروز ViewModel دو بار [addTransaction] صدا می‌زد. اگر بینِ آن دو، برنامه
     * بسته یا پروسه کشته می‌شد، از حسابِ مبدأ کم شده بود ولی به مقصد اضافه نشده بود -
     * یعنی پولِ کاربر روی کاغذ دود می‌شد. حالا یک تراکنشِ دیتابیس است: یا هر دو ردیف
     * نوشته می‌شوند یا هیچ‌کدام.
     *
     * شناسه‌ی مشترکِ [sourceId] همان چیزی است که [transferLegsOf] با آن دو سمت را
     * کنارِ هم پیدا می‌کند (ویرایش/حذفِ هم‌زمان و کنارگذاشتن از گزارش).
     */
    suspend fun addTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        transferId: Long = System.currentTimeMillis(),
    ): Long = inTransaction {
        addTransaction(
            accountId = fromAccountId,
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            description = description,
            year = year, month = month, day = day,
            category = null,
            sourceType = SOURCE_TYPE_TRANSFER,
            sourceId = transferId.toString(),
            id = transferId,
        )
        addTransaction(
            accountId = toAccountId,
            type = TransactionType.DEPOSIT,
            amount = amount,
            description = description,
            year = year, month = month, day = day,
            category = null,
            sourceType = SOURCE_TYPE_TRANSFER,
            sourceId = transferId.toString(),
            id = transferId + 1,
        )
        transferId
    }

    /**
     * **تشخیصِ جابه‌جایی بینِ حساب‌های خودِ کاربر** (خواسته‌ی کاربر، ۶ مهر: حقوق به کارتِ
     * تجارت می‌آید و بعد به بلو منتقل می‌شود؛ دو پیامک = یک خرج + یک درآمدِ الکی).
     *
     * وقتی تراکنشِ خودکارِ تازه‌ای ثبت شد، اگر در [TRANSFER_PAIR_WINDOW_MS] گذشته یک تراکنشِ
     * خودکارِ **مخالف** (برداشت ↔ واریز) با **همان مبلغ** روی **حسابِ دیگری** از خودِ کاربر
     * باشد، هر دو یک جابه‌جایی می‌شوند: موجودیِ هر حساب درست می‌ماند ولی در گزارشِ خرج و
     * درآمد دیگر شمرده نمی‌شوند. فقط تراکنش‌های خودکار (پیامک/اعلان) جفت می‌شوند، نه دستی.
     */
    suspend fun pairAutoTransfer(txId: Long): Boolean = inTransaction {
        val tx = transactionDao.byId(txId) ?: return@inTransaction false
        if (tx.originLabel == null || tx.sourceType == SOURCE_TYPE_TRANSFER) return@inTransaction false
        // پیامکِ واریزِ حسابِ مقصد، بعد از این‌که سمتِ واریز از روی پیامکِ برداشت ساخته شده بود:
        // تکراری است، پس حذف.
        val all = observeTransactions().first()
        if (tx.type == TransactionType.DEPOSIT.name && all.any {
                it.originLabel == AUTO_DEST_LEG_LABEL && it.accountId == tx.accountId &&
                    it.amount == tx.amount && kotlin.math.abs(it.id - tx.id) <= TRANSFER_PAIR_WINDOW_MS
            }
        ) {
            transactionDao.delete(tx)
            return@inTransaction true
        }
        val match = all
            .filter {
                it.id != tx.id &&
                    it.accountId != tx.accountId &&
                    it.originLabel != null &&
                    it.sourceType != SOURCE_TYPE_TRANSFER &&
                    it.type != tx.type &&
                    it.amount == tx.amount &&
                    kotlin.math.abs(it.id - tx.id) <= TRANSFER_PAIR_WINDOW_MS
            }
            .minByOrNull { kotlin.math.abs(it.id - tx.id) }
            ?: return@inTransaction false
        val pairId = minOf(tx.id, match.id).toString()
        transactionDao.upsertAll(
            listOf(tx, match).map {
                it.copy(
                    sourceType = SOURCE_TYPE_TRANSFER,
                    sourceId = pairId,
                    category = null,
                    description = "جابه‌جایی بینِ حساب‌های خودت (تشخیصِ خودکار)",
                )
            },
        )
        true
    }

    /**
     * حسابِ **خودِ کاربر** که در متنِ پیامکِ برداشت به‌عنوانِ مقصد آمده - از روی شماره‌کارتِ
     * کامل، شماره‌حساب یا شبا (رقم‌های فارسی هم پذیرفته می‌شوند). فقط شماره‌ی **کامل** ملاک است:
     * چهار رقمِ آخر ممکن است مالِ خودِ کارتِ مبدأ باشد.
     */
    suspend fun ownDestinationOf(body: String, sourceAccountId: Long): AccountEntity? {
        val digits = body.map { c -> if (c in '۰'..'۹') '0' + (c - '۰') else c }
            .joinToString("")
            .let { Regex("\\d[\\d\\-\\s]{7,40}\\d").findAll(it) }
            .map { m -> m.value.filter(Char::isDigit) }
            .toList()
        if (digits.isEmpty()) return null
        return observeAccounts().first().firstOrNull { acc ->
            acc.id != sourceAccountId && listOfNotNull(acc.cardNumber, acc.accountNumber, acc.sheba)
                .filter { it.length >= 8 }
                .any { own -> digits.any { d -> d == own || d.endsWith(own) } }
        }
    }

    /**
     * برداشتِ خودکار که مقصدش حسابِ خودِ کاربر است → جابه‌جایی. سمتِ واریز هم همین‌جا ساخته
     * می‌شود تا موجودیِ حسابِ مقصد درست باشد حتی اگر پیامکِ آن بانک هرگز نیاید.
     */
    suspend fun markOwnTransfer(txId: Long, toAccountId: Long) = inTransaction {
        val tx = transactionDao.byId(txId) ?: return@inTransaction
        if (tx.sourceType == SOURCE_TYPE_TRANSFER) return@inTransaction
        val desc = "جابه‌جایی بینِ حساب‌های خودت (تشخیصِ خودکار)"
        transactionDao.upsertAll(
            listOf(
                tx.copy(sourceType = SOURCE_TYPE_TRANSFER, sourceId = tx.id.toString(), category = null, description = desc),
                tx.copy(
                    id = tx.id + 1,
                    accountId = toAccountId,
                    type = TransactionType.DEPOSIT.name,
                    sourceType = SOURCE_TYPE_TRANSFER,
                    sourceId = tx.id.toString(),
                    category = null,
                    description = desc,
                    originLabel = AUTO_DEST_LEG_LABEL,
                ),
            ),
        )
    }

    /** «نه، این جابه‌جایی نبود» - هر دو سمت به تراکنشِ معمولی برمی‌گردند (سمتِ ساختگی حذف). */
    suspend fun unpairTransfer(transaction: AccountTransactionEntity) = inTransaction {
        val legs = transferLegsOf(transaction)
        legs.filter { it.originLabel == AUTO_DEST_LEG_LABEL }.let { if (it.isNotEmpty()) transactionDao.deleteAll(it) }
        transactionDao.upsertAll(
            legs.filter { it.originLabel != AUTO_DEST_LEG_LABEL }.map {
                val out = it.type == TransactionType.WITHDRAWAL.name
                it.copy(
                    sourceType = null,
                    sourceId = null,
                    category = if (out) "سایر هزینه" else "سایر درآمد",
                    description = if (out) "برداشت" else "واریز",
                )
            },
        )
    }

    /**
     * 🚨 **اتصالِ پرداخت‌ها به حساب** (خواسته‌ی کاربر، ۶ مهر: «وامی که پرداخت می‌کنم باید از
     * حسابم کم بشه»). هر مسیری که چیزی را «پرداخت‌شده» می‌کند (صفحه‌ی وام، کارتِ خانه، دکمه‌ی
     * اعلان، چک، قبض، طلب‌وبدهی، خریدِ دارایی) از همین یک تابع تراکنش می‌سازد.
     *
     * [accountId] = `null` یعنی «خودت حدس بزن»: اگر کاربر **فقط یک** حساب دارد همان؛ وگرنه
     * چیزی ثبت نمی‌شود و `false` برمی‌گردد تا فراخوان به کاربر بگوید.
     */
    suspend fun recordLinkedPayment(
        accountId: Long?,
        sourceType: String,
        sourceId: String,
        amount: Double,
        description: String,
        deposit: Boolean = false,
        date: ir.sadteam.loancalc.core.PersianDate? = null,
        category: String = "قسط/چک",
    ): Boolean {
        if (amount <= 0) return false
        val target = accountId ?: observeAccounts().first().singleOrNull()?.id ?: return false
        val d = date ?: ir.sadteam.loancalc.core.JalaliCalendar.today()
        addTransaction(
            accountId = target,
            type = if (deposit) TransactionType.DEPOSIT else TransactionType.WITHDRAWAL,
            amount = amount,
            description = description,
            year = d.y, month = d.m, day = d.d,
            category = category,
            sourceType = sourceType,
            sourceId = sourceId,
        )
        return true
    }

    /**
     * برگرداندنِ پرداخت (قسطِ «پرداخت‌نشده» شد، چکِ پاس‌شده برگشت…): تراکنشِ مربوط حذف می‌شود.
     * برای وام، [sourceId] = «loanId:m1,m2» است؛ اگر قسطِ [rowM] جزئی از یک پرداختِ
     * گروهی بود، فقط سهمِ همان ([partAmount]) از آن کم می‌شود.
     */
    suspend fun removeLinkedPayment(
        sourceType: String,
        sourceIdPrefix: String,
        rowM: Int? = null,
        partAmount: Double = 0.0,
    ) {
        val all = observeTransactions().first().filter { it.sourceType == sourceType }
        if (rowM == null) {
            all.filter { it.sourceId == sourceIdPrefix }.forEach { transactionDao.delete(it) }
            return
        }
        all.filter { it.sourceId?.startsWith("$sourceIdPrefix:") == true }.forEach { tx ->
            val ms = tx.sourceId!!.substringAfter(':').split(',').mapNotNull { it.toIntOrNull() }
            if (rowM !in ms) return@forEach
            val rest = ms - rowM
            if (rest.isEmpty() || partAmount <= 0 || partAmount >= tx.amount) {
                transactionDao.delete(tx)
            } else {
                transactionDao.upsert(
                    tx.copy(amount = tx.amount - partAmount, sourceId = "$sourceIdPrefix:${rest.joinToString(",")}"),
                )
            }
        }
    }

    /** تاییدِ یه تراکنشِ خودکار - از همین لحظه رو موجودی و گزارش‌ها اثر می‌ذاره. */
    suspend fun confirmTransaction(id: Long) {
        transactionDao.confirm(id)
        gamification?.awardDailyLog()
    }

    suspend fun transactionById(id: Long) = transactionDao.byId(id)

    fun observePendingTransactions() = transactionDao.observePending()

    /**
     * به‌روزرسانیِ یه تراکنشِ موجود - برای انتقالِ دسته موقعِ حذفِ یه دسته‌بندی و ویرایشِ دستی.
     *
     * 🚨 **جابه‌جایی دو ردیفِ به‌هم‌بسته است، نه دو تراکنشِ مستقل.** تا امروز ویرایشِ یک سمتش
     * سمتِ دیگر را دست‌نخورده می‌گذاشت، یعنی بدونِ هیچ خرجِ واقعی جمعِ موجودیِ کاربر کم/زیاد
     * می‌شد. حالا مبلغ و توضیحِ **هر دو** با هم می‌روند. حساب و نوعِ هر سمت عمداً دست‌نخورده
     * می‌مانَد: عوض‌کردنشان یعنی جابه‌جاییِ دیگری.
     */
    suspend fun updateTransaction(transaction: AccountTransactionEntity) {
        val legs = transferLegsOf(transaction)
        if (legs.size < 2) {
            transactionDao.upsert(transaction)
            return
        }
        transactionDao.upsertAll(
            legs.map { it.copy(amount = transaction.amount, description = transaction.description) },
        )
    }

    /** حذفِ جابه‌جایی **هر دو سمتش** را با هم می‌برد - وگرنه یک سمتِ یتیم می‌مانْد و موجودیِ کل
     * به‌اندازه‌ی همان مبلغ غلط می‌شد. */
    suspend fun deleteTransaction(transaction: AccountTransactionEntity) {
        val legs = transferLegsOf(transaction)
        if (legs.size < 2) {
            transactionDao.delete(transaction)
            return
        }
        transactionDao.deleteAll(legs)
    }

    private suspend fun transferLegsOf(transaction: AccountTransactionEntity): List<AccountTransactionEntity> {
        if (transaction.sourceType != SOURCE_TYPE_TRANSFER) return emptyList()
        val sourceId = transaction.sourceId ?: return emptyList()
        return transactionDao.transferLegs(sourceId)
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

    /**
     * 🚨 نامِ دسته کلیدِ **چهار** جدول است، نه فقط تراکنش‌ها.
     *
     * بخشِ ۷۴ طراح یک باگ گرفت (تغییرِ نامِ دسته تراکنش‌ها را بی‌دسته می‌کرد) و رفعش را
     * فقط روی تراکنش‌ها گذاشت. ولی همان ریشه دو جای دیگر هم دارد و هر دو **بی‌صدا**
     * خراب می‌کنند:
     * - `BudgetEntity.categoryName` → بودجه به دسته‌ای اشاره می‌کند که دیگر نیست، پس
     *   سقف دیگر هیچ خرجی را نمی‌شمارد و کاربر فکر می‌کند بودجه‌اش را رعایت کرده.
     * - `RecurringPaymentEntity.categoryName` → پرداختِ تکراریِ بعدی بی‌دسته ثبت می‌شود.
     *
     * (جای چهارم `ParsingRuleEntity.category` است که مخزنِ خودش را دارد و در
     * `CategoryViewModel` هم‌قدم به‌روز می‌شود.)
     */
    suspend fun renameCategoryEverywhere(oldName: String, newName: String) {
        transactionDao.getAll()
            .filter { it.category == oldName }
            .forEach { transactionDao.upsert(it.copy(category = newName)) }
        budgetDao.getAll()
            .filter { it.categoryName == oldName }
            .forEach { budgetDao.upsert(it.copy(categoryName = newName)) }
        recurringPaymentDao.getAll()
            .filter { it.categoryName == oldName }
            .forEach { recurringPaymentDao.upsert(it.copy(categoryName = newName)) }
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
            // ۷ مهر: «همه‌چیز روی سرور» - بقیه‌ی جدول‌های کاربر هم همین‌جا می‌روند.
            "tables" to kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { dumpExtraTables() },
        )
        return GsonBuilder().setPrettyPrinting().create().toJson(data)
    }

    suspend fun importBackupJson(json: String): Boolean = try {
        importBackupJsonOrThrow(json)
    } catch (e: Exception) {
        // گزارشِ تشخیصی (فقط نوع و متنِ خطا، بدونِ داده) - ۷ مهر حساب‌ها بی‌صدا برنمی‌گشتند.
        runCatching {
            apiService.reportCrash(
                ir.sadteam.loancalc.data.network.CrashReportRequest(
                    "accounts_import_failed: ${e.javaClass.simpleName}: ${e.message?.take(300)}",
                    e.stackTraceToString().take(3000), "sync", null,
                ),
                null,
            )
        }
        false
    }

    private suspend fun importBackupJsonOrThrow(json: String): Boolean {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        val gson = GsonBuilder().create()
        val parsed: Map<String, Any?> = gson.fromJson(json, type) ?: error("empty json")
        val accountsJson = gson.toJson(parsed["accounts"] ?: error("no accounts key"))
        val transactionsJson = gson.toJson(parsed["transactions"] ?: error("no transactions key"))
        val accounts: List<AccountEntity> = gson.fromJson(accountsJson, object : TypeToken<List<AccountEntity>>() {}.type)
        val transactions: List<AccountTransactionEntity> =
            gson.fromJson(transactionsJson, object : TypeToken<List<AccountTransactionEntity>>() {}.type)
        // حساب‌ها و تراکنش‌هایشان یک کارند؛ نیمه‌کاره یعنی تراکنشِ بی‌حساب.
        @Suppress("UNCHECKED_CAST")
        val tables = parsed["tables"] as? Map<String, List<Map<String, Any?>>>
        inTransaction {
            accountDao.replaceAll(accounts)
            transactionDao.replaceAll(transactions)
            // پشتیبانِ قدیمی «tables» ندارد - آن‌وقت بقیه‌ی جدول‌ها دست نمی‌خورند.
            if (tables != null) restoreExtraTables(tables)
        }
        return true
    }

    /**
     * جدول‌هایی که جز وام/چک/حساب باید بینِ گوشی‌ها بیایند. عمداً **فهرستِ سفید** است:
     * `inbox_messages` (مالِ همین گوشی) و جدول‌های ماژول‌های دیگر این‌جا نیستند.
     * عکس‌ها فایل‌اند و این‌جا نمی‌آیند - فقط مسیرشان.
     */
    private val extraTables = listOf(
        "budgets", "recurring_payments", "counterparties", "debts", "notes",
        "custom_categories", "category_order", "assets", "asset_trades", "savings_goals",
        "parsing_rules", "incomes", "tx_templates", "bills", "wealth_snapshots",
        "dang_events", "dang_participants", "dang_items", "dang_item_shares",
        "coin_events", "achievements", "calculation_history",
    )

    private fun dumpExtraTables(): Map<String, List<Map<String, Any?>>> {
        val db = database?.openHelper?.writableDatabase ?: return emptyMap()
        val out = LinkedHashMap<String, List<Map<String, Any?>>>()
        for (table in extraTables) {
            val rows = runCatching {
                db.query("SELECT * FROM `$table`").use { c ->
                    buildList {
                        while (c.moveToNext()) {
                            val row = LinkedHashMap<String, Any?>()
                            for (i in 0 until c.columnCount) {
                                row[c.getColumnName(i)] = when (c.getType(i)) {
                                    android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                                    android.database.Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                                    android.database.Cursor.FIELD_TYPE_STRING -> c.getString(i)
                                    else -> null
                                }
                            }
                            add(row)
                        }
                    }
                }
            }.getOrNull() ?: continue
            out[table] = rows
        }
        return out
    }

    private fun restoreExtraTables(tables: Map<String, List<Map<String, Any?>>>) {
        val db = database?.openHelper?.writableDatabase ?: return
        for (table in extraTables) {
            val rows = tables[table] ?: continue
            // فقط ستون‌هایی که همین نسخه‌ی اپ دارد - پشتیبانِ نسخه‌ی دیگر خرابش نکند.
            val columns = db.query("PRAGMA table_info(`$table`)").use { c ->
                buildSet { while (c.moveToNext()) add(c.getString(c.getColumnIndexOrThrow("name"))) }
            }
            if (columns.isEmpty()) continue
            db.execSQL("DELETE FROM `$table`")
            rows.forEach { row ->
                val values = android.content.ContentValues()
                row.forEach { (k, v) ->
                    if (k !in columns) return@forEach
                    when (v) {
                        null -> values.putNull(k)
                        is Number -> if (v.toDouble() % 1.0 == 0.0 && kotlin.math.abs(v.toDouble()) < 9e15) values.put(k, v.toLong()) else values.put(k, v.toDouble())
                        is Boolean -> values.put(k, if (v) 1 else 0)
                        else -> values.put(k, v.toString())
                    }
                }
                runCatching { db.insert(table, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, values) }
            }
        }
    }

    /** پورت مفهومی pushToServer تو LoanRepository - fire-and-forget، خطاها عمداً قورت داده می‌شن. */
    suspend fun pushToServer(token: String, allowUnknownRevision: Boolean = false): Boolean {
        try {
            val expected = uiPrefs?.cloudRevision(CLOUD_MODULE)
            // 🚨 گوشی‌ای که هنوز هیچ‌وقت نسخه‌ی سرور را ندیده، کورکورانه نمی‌نویسد (همان باگِ ۷ مهر:
            // گوشیِ پاک‌شده پشتیبانِ ابری را خالی کرد). اولین نوشتن فقط از `syncAfterLogin`.
            if (expected == null && !allowUnknownRevision && uiPrefs != null) return false
            // گوشیِ خالی هیچ‌وقت پشتیبانِ ابری را بازنویسی نمی‌کند.
            if (!hasLocalData()) return false
            val response = apiService.putAccountsBackup(
                "Bearer $token",
                BackupBlobRequest(exportBackupJson(), expected),
            )
            // ۴۰۹ یعنی گوشیِ دیگری جلوتر نوشته؛ داده‌ی محلی دست‌نخورده می‌مانَد و نتیجه
            // `false` است تا وضعیتِ پشتیبان صادقانه بماند.
            if (!response.isSuccessful) return false
            uiPrefs?.let { prefs -> expected?.let { prefs.setCloudRevision(CLOUD_MODULE, it + 1) } }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** آخرین بکاپِ ابریِ حساب‌ها/تراکنش‌ها رو می‌گیره و جایگزینِ دیتای محلی می‌کنه. */
    /** فقط می‌گیرد، چیزی نمی‌نویسد - رجوع کن به [LoanRepository.fetchServerBackupJson]. */
    suspend fun fetchServerBackupJson(token: String): String? = try {
        val response = apiService.getAccountsBackup("Bearer $token")
        uiPrefs?.setCloudRevision(CLOUD_MODULE, response.revision)
        response.data
    } catch (e: Exception) {
        null
    }


    /**
     * 🚨 بعد از ورود (۷ مهر): قبلاً اینجا **بی‌قید push** می‌شد - گوشیِ تازه/پاک‌شده یعنی داده‌ی
     * خالی روی پشتیبانِ ابری می‌نشست و همه‌چیز پاک می‌شد. حالا: گوشی خالی ← از سرور بیاور؛
     * سرور خالی ← بفرست؛ هر دو پر ← دست نزن (پشتیبانِ دوره‌ای بعداً با کنترلِ نسخه می‌فرستد).
     */
    private suspend fun hasLocalData(): Boolean =
        transactionDao.getAll().isNotEmpty() || accountDao.getAll().size > 1

    suspend fun syncAfterLogin(token: String) {
        val blob = fetchServerBackupJson(token) ?: return
        val serverHasData = blob.contains("\"id\"")
        val localHasData = hasLocalData()
        when {
            !localHasData && serverHasData -> importBackupJson(blob)
            localHasData && !serverHasData -> pushToServer(token, allowUnknownRevision = true)
        }
    }

    /**
     * دو گوشی (۷ مهر): وقتی اپ باز می‌شود، اگر گوشیِ دیگری نسخه‌ی تازه‌تری روی سرور نوشته،
     * همان را بیاور. گوشیِ هرگز-همگام‌نشده (`null`) کاری نمی‌کند - آن را ورود مدیریت می‌کند.
     */
    suspend fun pullIfNewer(token: String) {
        val known = uiPrefs?.cloudRevision(CLOUD_MODULE)
        val resp = try { apiService.getAccountsBackup("Bearer $token") } catch (e: Exception) { return }
        if (!resp.data.contains("\"id\"")) return
        // گوشیِ خالی همیشه نسخه‌ی سرور را می‌گیرد، حتی اگر شماره‌ی نسخه را نداند یا از آن جلوتر باشد.
        val localEmpty = !hasLocalData()
        if (localEmpty || (known != null && resp.revision > known)) {
            if (importBackupJson(resp.data)) uiPrefs?.setCloudRevision(CLOUD_MODULE, resp.revision)
        }
    }

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
