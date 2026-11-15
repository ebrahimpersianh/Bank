package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** پرداخت/دریافتِ تکراریِ ماهانه (مثلِ اجاره) - هر ماه تو همون `dayOfMonth` یادآوری می‌شه؛
 * ثبت‌شدنش تو `account_transactions` خودکار نیست (کاربر خودش با یادآوری ثبت می‌کنه)، این جدول
 * فقط برنامه/الگو رو نگه می‌داره. [accountId] اختیاریه (اگه معلوم نبود، حسابِ پیش‌فرض نداره). */
@Entity(tableName = "recurring_payments")
data class RecurringPaymentEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val amount: Double,
    val type: String,
    val categoryName: String?,
    val accountId: Long?,
    val dayOfMonth: Int,
    val reminderDayOffsets: String?,
    val createdAt: String,
)
