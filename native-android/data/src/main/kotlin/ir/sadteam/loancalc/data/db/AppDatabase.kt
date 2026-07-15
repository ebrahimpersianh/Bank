package ir.sadteam.loancalc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LoanEntity::class,
        ChequeEntity::class,
        ChequeBookEntity::class,
        AccountEntity::class,
        AccountTransactionEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
    abstract fun chequeDao(): ChequeDao
    abstract fun chequeBookDao(): ChequeBookDao
    abstract fun accountDao(): AccountDao
    abstract fun accountTransactionDao(): AccountTransactionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan-calc.db",
                )
                    // این پروژه هنوز پیش از انتشار عمومیه (فاز ۰/۱)، پس هیچ داده‌ی واقعی کاربری
                    // برای نگه‌داشتن وجود نداره - به‌جای نوشتن migration دستی برای هر تغییر schema
                    // زودهنگام، دیتابیس رو موقع تغییر version دوباره می‌سازه.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
