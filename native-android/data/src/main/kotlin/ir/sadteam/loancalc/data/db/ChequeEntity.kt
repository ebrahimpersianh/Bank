package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یه چک دریافتی یا پرداختی. [type]/[status] رشته‌ی `name` اینام‌های `ChequeType`/`ChequeStatus`
 * (تو :core) رو نگه می‌دارن - Room مستقیم enum نمی‌گیره، برای همین تبدیل تو ChequeRepository انجام
 * می‌شه. [chequeBookId] اختیاریه (چک می‌تونه بدون دسته‌چک هم ثبت بشه).
 */
@Entity(tableName = "cheques")
data class ChequeEntity(
    @PrimaryKey val id: Long,
    val type: String,
    val amount: Double,
    val chequeNumber: String,
    val bankName: String,
    val branchName: String,
    val ownerName: String,
    val dueYear: Int,
    val dueMonth: Int,
    val dueDay: Int,
    val status: String,
    val notes: String,
    val chequeBookId: Long?,
    val archived: Boolean,
    val createdAt: String,
    /** مسیر مطلق عکس رسید تو فضای داخلی اپ (پورت «پیوست عکس» اپ رقیب) - رجوع کن به AttachmentStorage. */
    val photoPath: String? = null,
    /** شناسه‌ی ۱۶ رقمی صیادی (سامانه‌ی صیاد چک) - اختیاریه، خیلی از چک‌های قدیمی/دست‌نویس این رو ندارن.
     * Nullable (نه رشته‌ی خالی پیش‌فرض) هم‌الگو با [photoPath]: وقتی گسون یه بک‌آپ JSON قدیمی‌تر از
     * قبل از این فیلد رو import می‌کنه، مقدارِ پیش‌فرضِ Kotlin اعمال نمی‌شه (گسون از Unsafe استفاده
     * می‌کنه)، پس فقط nullable امنه، نه یه non-null با مقدار پیش‌فرض. */
    val sayadId: String? = null,
)
