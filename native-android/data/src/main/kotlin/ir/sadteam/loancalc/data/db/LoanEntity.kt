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
    /** یه‌بار سررسیدهای این وام به تقویم گوشی اضافه شدن یا نه - بعد از true شدن، دکمه‌ی «افزودن
     * سررسیدها» تو LoanDetailScreen به‌جای درجِ دوباره فقط یه پیام نشون می‌ده (خواسته‌ی کاربر: جلوگیری
     * از رویدادهای تکراری تو تقویم گوشی با هر بار کلیک). */
    val calendarExported: Boolean = false,
    /** یادآوریِ اختصاصیِ این وام: CSV از تعداد روزهای قبل از سررسید (مثلاً "1,3,7"). null یعنی از
     * تنظیماتِ سراسریِ [ir.sadteam.loancalc.data.prefs.UiPrefs.reminderDayOffsets] استفاده کن؛ رشته‌ی
     * خالی یعنی یادآوری برای این وام کاملاً خاموشه. حتماً nullable و بدون defaultِ معنادار (نه یه
     * لیستِ ثابت) بمونه - رجوع کن به توضیحِ مشابه رو [ir.sadteam.loancalc.data.db.ChequeEntity.sayadId]
     * درباره‌ی گسون/Unsafe موقعِ importِ بک‌آپ‌های قدیمی. */
    val reminderDayOffsets: String? = null,
)
