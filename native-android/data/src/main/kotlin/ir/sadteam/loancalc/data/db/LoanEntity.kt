package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * جدول محلی «وام‌های من». [dataJson] شکلِ کلیِ وام (method/rate/graceMonths/borrower/startDate/...)
 * رو نگه می‌داره - همون شیءای که سرور (server/.../routes/LoansRoutes.kt) هم به‌صورتِ JSONِ مات
 * ذخیره/سینک می‌کنه، برای سازگاریِ کاملِ فرمتِ سیم با سرور/بک‌آپ عمداً دست‌نخورده مونده. ستون‌های
 * دیگه فقط برای لیست/مرتب‌سازیِ سریع بدون deserialize کردنِ کل JSON هستن.
 *
 * **مرتب‌سازیِ دیتابیس**: تا قبل از این، آرایه‌ی `rows` (وضعیتِ پرداختِ تک‌تکِ اقساط) هم همینجا تویِ
 * [dataJson] بود؛ الان یه جدولِ جدای واقعیِ Room ([ir.sadteam.loancalc.data.db.LoanRowEntity]/
 * `loan_rows`) منبعِ حقیقتشه - رجوع کن به [LoanRepository][ir.sadteam.loancalc.data.LoanRepository]
 * برای جزئیاتِ کاملِ معماری/مهاجرت. `dataJson`ِ وام‌های قدیمی‌ای که هنوز از قبلِ این تغییر ذخیره
 * شدن ممکنه یه کلیدِ "rows" روبه‌زوال هم داخلش داشته باشن (migrationِ AppDatabase عمداً حذفش
 * نمی‌کنه، فقط دیگه هیچ‌جای کد ازش نمی‌خونه) - بی‌ضرره، فقط دیتای مرده.
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
