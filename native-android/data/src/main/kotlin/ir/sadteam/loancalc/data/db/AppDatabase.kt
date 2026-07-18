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
        ChequeEntity::class,
        ChequeBookEntity::class,
        AccountEntity::class,
        AccountTransactionEntity::class,
        IncomeEntity::class,
        CalculationHistoryEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
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
                        .addMigrations(MIGRATION_9_10)
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { instance = it }
                }
            }
    }
}
