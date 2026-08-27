package ir.sadteam.loancalc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        ParsingRuleEntity::class,
        LoanEntity::class,
        LoanRowEntity::class,
        ChequeEntity::class,
        ChequeBookEntity::class,
        AccountEntity::class,
        AccountTransactionEntity::class,
        IncomeEntity::class,
        CalculationHistoryEntity::class,
        BudgetEntity::class,
        RecurringPaymentEntity::class,
        CounterpartyEntity::class,
        DebtEntity::class,
        NoteEntity::class,
        CustomCategoryEntity::class,
        CategoryOrderEntity::class,
        AssetEntity::class,
        AssetTradeEntity::class,
        CoinEventEntity::class,
        AchievementEntity::class,
    ],
    version = 27,
    // برای اینکه بشه تستِ خودکارِ migration (Room.testing.MigrationTestHelper، رجوع کن به
    // data/src/androidTest/.../MigrationTest.kt و CLAUDE.md) نوشت، Room باید اسکیمای هر نسخه رو
    // به‌عنوانِ JSON خروجی بده - این فایل‌ها تو data/schemas/ کامیت می‌شن (مسیرش تو build.gradle.kts
    // تنظیم شده). چون این گزینه همین الان روشن شده، فقط از نسخه‌ی ۱۲ به بعد اسکیمای واقعی داریم -
    // migrationِ ۱۱→۱۲ (که قبلاً کرش داشت و رفع شد) قابلِ‌تستِ خودکار نیست چون اسکیمای نسخه‌ی ۱۱
    // هیچ‌وقت ثبت نشده بود.
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun achievementDao(): AchievementDao
    abstract fun loanDao(): LoanDao
    abstract fun loanRowDao(): LoanRowDao
    abstract fun chequeDao(): ChequeDao
    abstract fun chequeBookDao(): ChequeBookDao
    abstract fun accountDao(): AccountDao
    abstract fun accountTransactionDao(): AccountTransactionDao
    abstract fun incomeDao(): IncomeDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringPaymentDao(): RecurringPaymentDao
    abstract fun counterpartyDao(): CounterpartyDao
    abstract fun debtDao(): DebtDao
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao

    abstract fun parsingRuleDao(): ParsingRuleDao
    abstract fun assetDao(): AssetDao
    abstract fun assetTradeDao(): AssetTradeDao

    companion object {
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE loans ADD COLUMN calendarExported INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** یادآوریِ حرفه‌ای/شخصی‌سازی‌شده (تایمینگِ اختصاصی به‌ازای هر وام/چک) - رجوع کن به
         * LoanEntity.reminderDayOffsets/ChequeEntity.reminderDayOffsets. */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE loans ADD COLUMN reminderDayOffsets TEXT")
                db.execSQL("ALTER TABLE cheques ADD COLUMN reminderDayOffsets TEXT")
            }
        }

        /** جداکردنِ واقعیِ ردیف‌های قسط از تویِ blobِ JSONِ `loans.dataJson` به یه جدولِ تایپ‌شده‌ی
         * جدا ([LoanRowEntity]/`loan_rows`) - رجوع کن به CLAUDE.md برای دلیلِ کامل. عمداً **فقط
         * افزایشی**ه: یه جدولِ جدید می‌سازه و بهترین‌تلاشِ ممکن رو برای پرکردنش از رو `rows`ِ داخلِ
         * dataJsonِ هر وامِ موجود انجام می‌ده، ولی خودِ `loans.dataJson` رو دست‌نمی‌زنه/استریپ
         * نمی‌کنه (نه ALTER، نه حذفِ کلیدِ "rows") - یعنی حتی اگه پارس‌کردنِ یه وامِ خاص شکست بخوره
         * (JSON نامعتبر/فرمتِ غیرمنتظره)، فقط همون یه وام تو جدولِ جدید خالی می‌مونه (و
         * `LoanRepository.getOrMigrateRows` وقتِ اولین دسترسی خودش از رو dataJsonِ قدیمی بازسازیش
         * می‌کنه)، نه اینکه کلِ مهاجرت با کرش متوقف بشه یا داده‌ای واقعاً از دست بره. */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS loan_rows (
                        loanId INTEGER NOT NULL,
                        m INTEGER NOT NULL,
                        installment REAL NOT NULL,
                        paid INTEGER NOT NULL,
                        paidLate INTEGER NOT NULL,
                        paidDateY INTEGER,
                        paidDateM INTEGER,
                        paidDateD INTEGER,
                        photoPath TEXT,
                        PRIMARY KEY(loanId, m)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_rows_loanId ON loan_rows(loanId)")

                val cursor = db.query("SELECT id, dataJson FROM loans")
                cursor.use {
                    while (it.moveToNext()) {
                        val loanId = it.getLong(0)
                        val dataJson = it.getString(1) ?: continue
                        try {
                            val obj = org.json.JSONObject(dataJson)
                            val rows = obj.optJSONArray("rows") ?: continue
                            for (i in 0 until rows.length()) {
                                val row = rows.optJSONObject(i) ?: continue
                                val m = row.optInt("m", i + 1)
                                val installment = row.optDouble("installment", 0.0)
                                val paid = if (row.optBoolean("paid", false)) 1 else 0
                                val paidLate = if (row.optBoolean("paidLate", false)) 1 else 0
                                val photoPath = if (row.isNull("photoPath")) null else row.optString("photoPath", null)
                                val paidDate = row.optJSONObject("paidDate")
                                val pdY = paidDate?.let { d -> if (d.has("y")) d.optInt("y") else null }
                                val pdM = paidDate?.let { d -> if (d.has("m")) d.optInt("m") else null }
                                val pdD = paidDate?.let { d -> if (d.has("d")) d.optInt("d") else null }
                                db.execSQL(
                                    """
                                    INSERT OR REPLACE INTO loan_rows
                                    (loanId, m, installment, paid, paidLate, paidDateY, paidDateM, paidDateD, photoPath)
                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                    """.trimIndent(),
                                    arrayOf<Any?>(loanId, m, installment, paid, paidLate, pdY, pdM, pdD, photoPath),
                                )
                            }
                        } catch (e: Exception) {
                            // این وامِ خاص با فرمتِ غیرمنتظره رد می‌شه - LoanRepository.getOrMigrateRows
                            // خودش وقتِ اولین دسترسی از رو dataJson بازسازیش می‌کنه، پس داده‌ای گم نمی‌شه.
                        }
                    }
                }
            }
        }

        /** پایه‌ی ماژولِ حسابداریِ شخصیِ اپ - دسته‌بندی روی تراکنشِ حسابِ موجود
         * ([AccountTransactionEntity]) + دو جدولِ جدید برای بودجه‌بندی و پرداخت‌های تکراری. */
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE account_transactions ADD COLUMN category TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER NOT NULL PRIMARY KEY,
                        categoryName TEXT NOT NULL,
                        monthlyCap REAL NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_payments (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        categoryName TEXT,
                        accountId INTEGER,
                        dayOfMonth INTEGER NOT NULL,
                        reminderDayOffsets TEXT,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        /** طلب‌وبدهی (طرفِ‌حساب + ردیف‌های طلب/بدهی) و یادداشتِ مستقل - رجوع کن به CLAUDE.md، بخشِ
         * تبِ «سررسید». طبقِ درسِ کرشِ migrationِ ۱۱→۱۲ (هر CREATE INDEXِ دستی باید تو indicesِ
         * @Entity هم اعلام بشه)، ایندکسِ debts.counterpartyId هم اینجا هم تو DebtEntity هست. */
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS counterparties (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS debts (
                        id INTEGER NOT NULL PRIMARY KEY,
                        counterpartyId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        day INTEGER NOT NULL,
                        settled INTEGER NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_counterpartyId ON debts(counterpartyId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id INTEGER NOT NULL PRIMARY KEY,
                        text TEXT NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        day INTEGER NOT NULL,
                        reminderDayOffsets TEXT,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        /** فیلدِ اختیاریِ شماره‌کارت رو حساب‌ها - برای تشخیصِ خودکارِ بانک از رو BIN (رجوع کن به
         * CLAUDE.md، خواسته‌ی صریحِ کاربر). */
        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN cardNumber TEXT")
            }
        }

        /** مدیریتِ کاملِ دسته‌بندی‌های حسابداری - دسته‌های دلخواهِ کاربر (کنارِ لیستِ ثابتِ
         * Category.kt تو :app) + ترتیبِ دلخواهِ جابه‌جاشده. هیچ‌کدوم `indices` ندارن (نه تو SQL نه
         * تو @Entity)، پس کلاس‌باگِ migrationِ ۱۱→۱۲ اینجا مصداق نداره. */
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_categories (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        colorArgb INTEGER NOT NULL,
                        iconKey TEXT NOT NULL,
                        type TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS category_order (
                        type TEXT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        PRIMARY KEY(type, name)
                    )
                    """.trimIndent(),
                )
            }
        }

        /** ردیابیِ منبعِ تراکنش‌های خودکارِ ساخته‌شده از پرداختِ قسط/چک (sourceType/sourceId) - در
         * اصل برای یه فیچرِ همگون‌سازیِ گذشته‌نگر اضافه شده بود که بعداً به‌خواستِ کاربر کاملاً حذف
         * شد (رجوع کن به CLAUDE.md، MIGRATION_17_18)؛ این دو ستون خودشون هنوز لازمن - سینکِ رو-به-جلو
         * (LoanDetailScreen.commitPayment/ChequeDetailScreen.commitPass) همچنان باهاشون منبعِ هر
         * تراکنش رو تگ می‌کنه. */
        // نکته برای بعد: هیچ‌کدوم از اسکیمای نسخه‌های ۱۳-۱۶ تو data/schemas/ کامیت نشدن (فقط ۱۲.json
        // هست) - چون instrumented-tests.yml فقط رو پوش به main اجرا می‌شه و این migrationها همه رو
        // برنچِ فیچر اضافه شدن، بدونِ اینکه main هیچ‌وقت باهاشون رفرش بشه. یعنی تستِ خودکارِ
        // migrate16To17 (طبقِ الگوی MigrationTest.kt) الان ممکن نیست - نیازمندِ schemas/16.jsonه که
        // دیگه قابلِ‌بازسازی نیست. از نسخه‌ی ۱۷ به بعد، اولین CIِ موفق (رو هر برنچی که اجرا بشه)
        // خودش schemas/17.json رو می‌سازه، پس migrationِ بعدی (۱۷→۱۸) قابلِ‌تستِ خودکار می‌مونه.
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE account_transactions ADD COLUMN sourceType TEXT")
                db.execSQL("ALTER TABLE account_transactions ADD COLUMN sourceId TEXT")
            }
        }

        /** لغوِ همگام‌سازیِ گذشته‌نگر (خواسته‌ی صریحِ کاربر - رجوع کن به CLAUDE.md، «حذفِ همگام‌سازیِ
         * گذشته‌نگر»): «موجودیِ اولیه»ی یه حساب یعنی موجودیِ *الان*، پس کم‌کردنِ قسط/چک‌هایی که از
         * قبل پرداخت شده بودن از همون موجودی غلطه (دوبار کم‌کردنِ پولی که از قبل تو موجودیِ الان
         * لحاظ شده) - دقیقاً همون چیزی که باعثِ موجودیِ منفیِ کاذبِ گزارش‌شده‌ی کاربر شد. این migration
         * هر تراکنشی که خودکار از پرداختِ قسط/چک ساخته شده (sourceType = "loan" یا "cheque") رو پاک
         * می‌کنه - چه از فیچرِ بک‌فیلِ حذف‌شده اومده باشه چه از سینکِ رو-به-جلو. از این به بعد فقط
         * قسط/چکی که *بعدِ* همین آپدیت پرداخت بشه، تراکنشِ حسابداری می‌گیره (کدِ
         * LoanDetailScreen.commitPayment/ChequeDetailScreen.commitPass دست‌نخورده مونده، فقط دیگه
         * چیزی گذشته‌نگر صداشون نمی‌زنه). تراکنش‌های دستیِ کاربر (sourceType نال) دست‌نخورده می‌مونن. */
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM account_transactions WHERE sourceType IN ('loan', 'cheque')")
            }
        }

        /** وصل‌کردنِ شماره/سرشماره‌ی پیامکِ بانک به یه حساب (خواسته‌ی صریحِ کاربر) - تا وقتی پیامکِ
         * برداشت/واریز میاد، BankSmsReceiver دقیقاً بدونه مالِ کدوم حسابه، نه اینکه (مثلِ قبل) اگه
         * شماره‌کارت تو متنِ پیامک نبود بی‌برو‌برگرد رو حسابِ اول ثبتش کنه. ستون nullableست، پس
         * حساب‌های موجود دست‌نخورده می‌مونن و رفتارِ قبلی براشون تغییری نمی‌کنه. */
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN smsSender TEXT")
            }
        }

        /** «حساب‌کتابِ غیربانکی» (نقدی، کیفِ پول، کارتِ اعتباری…) - خواسته‌ی صریحِ کاربر طبقِ اپِ
         * مرجع. تا قبل از این هر حساب حتماً یه بانک داشت. هر دو ستون پیش‌فرض‌دار/nullableن، پس
         * حساب‌های موجود خودبه‌خود «bank» می‌مونن و رفتارشون ذره‌ای عوض نمی‌شه. */
        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN type TEXT NOT NULL DEFAULT 'bank'")
                db.execSQL("ALTER TABLE accounts ADD COLUMN iconKey TEXT")
            }
        }

        /** محدودکردنِ بودجه به یه حساب‌کتابِ خاص - nullable، پس بودجه‌های موجود خودبه‌خود
         * «همه‌ی حساب‌کتاب‌ها» می‌مونن و رفتارشون عوض نمی‌شه. */
        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN accountId INTEGER")
            }
        }

        /** دارایی‌های غیرنقدی (طلا/سکه/ارز/رمزارز) - خواسته‌ی صریحِ کاربر. دو جدولِ کاملاً جدید،
         * پس هیچ داده‌ی موجودی دست نمی‌خوره. ⚠️ ایندکس‌ها عیناً همون‌هایی‌ان که تو `indices` هر
         * `@Entity` اعلام شدن - وگرنه Room موقعِ آپگرید کرش می‌کنه (رجوع کن به CLAUDE.md). */
        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS assets (
                        id INTEGER NOT NULL PRIMARY KEY,
                        symbol TEXT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        unitPriceRial REAL,
                        priceUpdatedAt TEXT,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_assets_symbol ON assets(symbol)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS asset_trades (
                        id INTEGER NOT NULL PRIMARY KEY,
                        assetId INTEGER NOT NULL,
                        isBuy INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        totalRial REAL NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        day INTEGER NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_trades_assetId ON asset_trades(assetId)")
            }
        }

        /** زیرمجموعه‌ی دسته‌بندی (تسکِ #32) - ستونِ nullable، پس همه‌ی دسته‌های موجود خودبه‌خود
         * «سطحِ اول» می‌مونن و هیچ رفتاری عوض نمی‌شه. */
        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE custom_categories ADD COLUMN parentName TEXT")
            }
        }

        /**
         * **گیمیفیکیشن** (سکه + نشان). دو جدولِ کاملاً تازه‌ان، پس هیچ داده‌ی موجودی لمس
         * نمی‌شه و مسیرِ آپگرید بی‌خطره.
         *
         * ⚠️ ایندکسِ یکتای `(type, dateKey)` عیناً همونیه که تو `@Entity`ِ `CoinEventEntity`
         * اعلام شده - قاعده‌ی ماندگارِ پروژه؛ نبودِ این تطابق یه‌بار کلِ آپدیت رو کرش داد.
         */
        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin_events (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        dateKey TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        refId TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_coin_events_type_dateKey " +
                        "ON coin_events(type, dateKey)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS achievements (
                        code TEXT NOT NULL PRIMARY KEY,
                        unlockedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        /**
         * جزئیاتِ هر قسط (کارتِ `36d`): یادداشت و شماره‌ی پیگیری. هر دو `nullable`ن پس ردیف‌های
         * موجود خودبه‌خود خالی می‌مونن و هیچ رفتاری عوض نمی‌شه. چندعکسی ستونِ تازه نگرفت -
         * تو همون `photoPath` با جداکننده‌ی `|` ذخیره می‌شه.
         */
        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE loan_rows ADD COLUMN note TEXT")
                db.execSQL("ALTER TABLE loan_rows ADD COLUMN trackingNumber TEXT")
            }
        }

        /**
         * دو ستونِ تازه‌ی حساب‌کتاب برای صفحه‌ی «پیامکِ بانکی»:
         * `smsEnabled` (کلیدِ هر بانک) و `lastSmsAt` (آخرین پیامکی که رو این حساب نشست).
         * هیچ ایندکسی اضافه نشد، پس چیزی تو `indices`ِ `@Entity` لازم نیست.
         */
        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN smsEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE accounts ADD COLUMN lastSmsAt INTEGER")
            }
        }

        /** جدولِ قاعده‌های تشخیصِ دسته‌بندی. ایندکسِ دستی اضافه نشد، پس `indices` هم لازم نیست. */
        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS parsing_rules (" +
                        "id INTEGER NOT NULL PRIMARY KEY, " +
                        "pattern TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "txType TEXT, " +
                        "sortOrder INTEGER NOT NULL DEFAULT 0, " +
                        "auto INTEGER NOT NULL DEFAULT 0, " +
                        "matchCount INTEGER NOT NULL DEFAULT 0)",
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: run {
                    // وام/چک/حساب اطلاعات مالی/شخصی‌ان؛ SQLCipher فایل دیتابیس رو با AES-256
                    // رمزنگاری می‌کنه (پسورد از Android Keystore - رجوع کن به DbPassphrase.kt) تا
                    // گوشیِ روت‌شده نتونه مستقیم فایلِ خام رو بخونه. چون این پروژه هنوز پیش از
                    // انتشار عمومیه (فاز ۰/۱، هیچ داده‌ی واقعی کاربری نیست)، این تغییر بدون هیچ
                    // migration ای اعمال می‌شه - نصب‌های قبلیِ رمزنگاری‌نشده با تغییرِ version دوباره
                    // ساخته می‌شن (fallbackToDestructiveMigration، هم‌مثل تغییرات schema قبلی).
                    SQLiteDatabase.loadLibs(context)
                    val factory = SupportFactory(dbPassphrase(context.applicationContext))
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "loan-calc.db",
                    )
                        .openHelperFactory(factory)
                        // برخلاف ارتقاهای قبلیِ schema (که همه destructive بودن، چون هنوز داده‌ی
                        // واقعی کاربری نبود)، این‌بار کاربر داره فعالانه رو وام‌های واقعی‌اش تست
                        // می‌کنه - یه migration واقعی نوشتیم که ستون جدید رو اضافه کنه بدون پاک‌کردنِ
                        // جدول‌ها. fallbackToDestructiveMigration فقط برای نسخه‌های خیلی قدیمی‌تر
                        // (قبل از این migration) که پوشش داده نشدن نگه داشته شده.
                        .addMigrations(
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                            MIGRATION_11_12,
                            MIGRATION_12_13,
                            MIGRATION_13_14,
                            MIGRATION_14_15,
                            MIGRATION_15_16,
                            MIGRATION_16_17,
                            MIGRATION_17_18,
                            MIGRATION_18_19,
                            MIGRATION_19_20,
                            MIGRATION_20_21,
                            MIGRATION_21_22,
                            MIGRATION_22_23,
                            MIGRATION_23_24,
                            MIGRATION_24_25,
                            MIGRATION_25_26,
                            MIGRATION_26_27,
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { instance = it }
                }
            }
    }
}
