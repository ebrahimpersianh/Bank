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
        LoanEntity::class,
        LoanRowEntity::class,
        ChequeEntity::class,
        ChequeBookEntity::class,
        AccountEntity::class,
        AccountTransactionEntity::class,
        IncomeEntity::class,
        CalculationHistoryEntity::class,
    ],
    version = 12,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
    abstract fun loanRowDao(): LoanRowDao
    abstract fun chequeDao(): ChequeDao
    abstract fun chequeBookDao(): ChequeBookDao
    abstract fun accountDao(): AccountDao
    abstract fun accountTransactionDao(): AccountTransactionDao
    abstract fun incomeDao(): IncomeDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao

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
                        .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { instance = it }
                }
            }
    }
}
