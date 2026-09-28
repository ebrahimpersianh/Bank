package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یه چک دریافتی یا پرداختی. [type]/[status] رشته‌ی `name` اینام‌های `ChequeType`/`ChequeStatus`
 * (تو :core) رو نگه می‌دارن - Room مستقیم enum نمی‌گیره، برای همین تبدیل تو ChequeRepository انجام
 * می‌شه. [chequeBookId] اختیاریه (چک می‌تونه بدون دسته‌چک هم ثبت بشه).
 */
@Entity(tableName = "cheques", indices = [Index("counterpartyId")])
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
    /** بخشِ اختیاریِ «اطلاعات بیشتر» تو فرمِ چک - شناسه/کد ملیِ طرفِ چک، برای مواردی که کاربر می‌خواد
     * ثبتش کنه (مثلاً برای پیگیریِ حقوقی/چک برگشتی). همه‌ی سه فیلدِ زیر nullable هستن، هم‌الگو با
     * [sayadId]/[photoPath] (سازگاری با بک‌آپ‌های JSON قدیمی‌تر که این فیلدها رو ندارن). */
    val nationalId: String? = null,
    /** مانده‌ی حساب قبل از واریزِ امروز - صرفاً برای محاسبه‌ی «جمع»/«مانده» تو خودِ فرم، رو چیز دیگه‌ای
     * تو اپ اثر نداره (حساب بانکیِ واقعی جای دیگه‌ست، `AccountEntity`). */
    val previousBalance: Double? = null,
    /** مبلغی که هم‌زمان با ثبتِ این چک به حساب واریز شده - رجوع کن به [previousBalance]. */
    val depositAmount: Double? = null,
    /** یادآوریِ اختصاصیِ این چک: CSV از تعداد روزهای قبل از سررسید (مثلاً "1,3,7"). null یعنی از
     * تنظیماتِ سراسری استفاده کن؛ رشته‌ی خالی یعنی یادآوری برای این چک کاملاً خاموشه - رجوع کن به
     * توضیحِ مشابه رو [ir.sadteam.loancalc.data.db.LoanEntity.reminderDayOffsets]. */
    val reminderDayOffsets: String? = null,
    /** لینک به [CounterpartyEntity] - طبقِ جوابِ سوالِ ۶ی MESSAGE-round4: چکِ **تازه** الزاماً یکی
     * می‌گیره (اجباری تو UI، نه تو دیتابیس - چون چکِ قدیمی باید بتونه null بمونه/حدسی وصل بشه). */
    val counterpartyId: Long? = null,
)
