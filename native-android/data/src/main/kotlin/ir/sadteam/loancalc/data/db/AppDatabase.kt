package ir.sadteam.loancalc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
    abstract fun chequeDao(): ChequeDao
    abstract fun chequeBookDao(): ChequeBookDao
    abstract fun accountDao(): AccountDao
    abstract fun accountTransactionDao(): AccountTransactionDao
    abstract fun incomeDao(): IncomeDao

    companion object {
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
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { instance = it }
                }
            }
    }
}
