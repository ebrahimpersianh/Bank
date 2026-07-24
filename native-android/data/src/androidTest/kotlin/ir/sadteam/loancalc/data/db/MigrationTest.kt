package ir.sadteam.loancalc.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * تستِ اینسترومنتدِ (نه JVMِ خام - رجوع کن به CLAUDE.md برای دلیلِ کامل) برای catch‌کردنِ دقیقاً
 * همون کلاسِ باگی که نسخه‌ی ۱.۰.۳۱۵ رو کرش می‌کرد: ناهماهنگیِ ایندکس/ستون بینِ یه migrationِ دستی
 * و اعلامِ @Entity.
 *
 * **محدودیتِ مهم**: `exportSchema` تا همین امروز خاموش بود، پس هیچ‌وقت اسکیمای نسخه‌های قدیمی‌تر
 * (مثلاً ۱۱) ثبت نشده - یعنی migrationِ ۱۱→۱۲ (که قبلاً کرش داشت و با اضافه‌کردنِ
 * `indices=[Index("loanId")]` رو LoanRowEntity رفع شد) دیگه قابلِ‌تستِ خودکار نیست، چون حالتِ
 * «قبل» ازش نداریم. از نسخه‌ی ۱۲ به بعد (که `exportSchema=true` شد) هر migrationِ جدید باید یه
 * تستِ `migrateN` اینجا اضافه بشه (الگو زیره) - وگرنه این کلاس از این محافظت بی‌بهره می‌مونه.
 *
 * تستِ [databaseOpensWithRealEncryptionSetup] یه چیزِ متفاوت رو تایید می‌کنه: اینکه خودِ SQLCipher
 * (کتابخونه‌ی نیتیوِ رمزنگاری) رو یه دستگاه/امولاتورِ واقعی درست لود و کار می‌کنه - چیزی که هیچ تستِ
 * JVMِ خام (Paparazzi/JUnitِ ساده) اصلاً نمی‌تونه چک کنه.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    /**
     * الگوی آماده برای وقتی migrationِ ۱۲→۱۳ (یا هر migrationِ بعدی) اضافه شد - این تابع رو با اسمِ
     * واقعیِ migrationِ جدید کپی/فعال کن:
     *
     * ```
     * @Test
     * fun migrate12To13() {
     *     helper.createDatabase(TEST_DB, 12).close()
     *     helper.runMigrationsAndValidate(TEST_DB, 13, true, AppDatabase.MIGRATION_12_13)
     * }
     * ```
     *
     * (نیازِ MIGRATION_12_13 به public/internal بودن به‌جای private تو AppDatabase.kt - الان
     * migrationها private ان چون تستی صداشون نمی‌زد.)
     */
    private companion object {
        const val TEST_DB = "migration-test"
    }

    /** تاییدِ اینکه SQLCipher رو یه محیطِ واقعیِ اندروید (نه JVMِ خام) درست کار می‌کنه - دقیقاً همون
     * راه‌اندازیِ تولیدی (AppDatabase.getInstance) رو یه فایلِ موقتِ جدا امتحان می‌کنه. */
    @Test
    fun databaseOpensWithRealEncryptionSetup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SQLiteDatabase.loadLibs(context)
        val factory = SupportFactory(dbPassphrase(context))
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "migration-test-encryption.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
        db.openHelper.writableDatabase // فورسِ باز شدنِ واقعیِ دیتابیس
        db.close()
        context.deleteDatabase("migration-test-encryption.db")
    }
}
