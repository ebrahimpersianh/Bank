package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * جدول محلی «وام‌های من». مطابق الگوی سرور فعلی (server/src/routes/loans.js) که کل شیء وام رو
 * به‌صورت یک JSON مات (بدون schema) ذخیره می‌کنه، اینجا هم [dataJson] همون شیء کامل (rows،
 * method، تاریخ‌ها، ویرایش‌های دستی قسط و ...) رو نگه می‌داره؛ ستون‌های دیگه فقط برای لیست/مرتب‌سازی
 * سریع بدون deserialize کردن کل JSON هستن. مدل تایپ‌شده‌ی کامل (Loan/InstallmentRow در :core) و
 * منطق سینک با سرور در فاز ۱ اضافه می‌شه.
 */
@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val bank: String,
    val amount: Double,
    val installment: Double,
    val totalPaid: Double,
    val n: Int,
    val paidCount: Int,
    val createdAt: String,
    val dataJson: String,
    /** مسیر مطلق عکس رسید تو فضای داخلی اپ (پورت «پیوست عکس» اپ رقیب) - رجوع کن به AttachmentStorage. */
    val photoPath: String? = null,
)
