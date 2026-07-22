package ir.sadteam.loancalc.data.db

import androidx.room.Entity

/**
 * ردیفِ تک‌قسطِ یه وام - جدولِ جدا (نه دیگه تویِ [LoanEntity.dataJson] به‌صورتِ آرایه‌ی JSONِ خام).
 * قبلاً هر قسط (وضعیتِ پرداخت/تاخیر/تاریخِ پرداخت/عکسِ رسید) داخلِ یه آرایه‌ی `rows` تویِ همون
 * blobِ JSON بود که هرجا لازم بود با `as? Number`/`as? String` دستی از رو `Map<String, Any?>`
 * خونده می‌شد - نه type-safe، نه قابلِ query. الان هر ردیف یه سطرِ واقعیِ Room ئه.
 *
 * [paidDateY]/[paidDateM]/[paidDateD] به‌جای یه Mapِ تودرتو (`{y,m,d}`) سه ستونِ جدا هستن - چون
 * Room نمی‌تونه یه data class تودرتو رو مستقیم به‌عنوانِ ستون‌های null‌پذیر نگه داره بدونِ
 * TypeConverterِ اضافه؛ سه ستونِ ساده هم query راحت‌تره هم بدونِ converter کار می‌کنه.
 *
 * **مهم**: [LoanRepository] عمداً یه فال‌بکِ خودترمیم‌شونده داره (`getOrMigrateRows`) - اگه به هر
 * دلیلی (لبه‌ی نادرِ مهاجرت، وامِ ساخته‌شده با نسخه‌ی خیلی قدیمی‌تر) یه وام تو این جدول هیچ ردیفی
 * نداشت، از رو `dataJson.rows`ِ قدیمی (اگه هنوز اونجا مونده باشه) یا پیش‌فرضِ اقساطِ برابر بازسازی و
 * همون‌جا persist می‌شه - تضمین می‌کنه تاریخچه‌ی پرداختِ هیچ کاربری با این تغییر گم نشه.
 */
@Entity(tableName = "loan_rows", primaryKeys = ["loanId", "m"])
data class LoanRowEntity(
    val loanId: Long,
    val m: Int,
    val installment: Double,
    val paid: Boolean,
    val paidLate: Boolean = false,
    val paidDateY: Int? = null,
    val paidDateM: Int? = null,
    val paidDateD: Int? = null,
    /** مسیرِ عکسِ رسیدِ همین قسطِ خاص (نه عکسِ کلیِ رو خودِ وام - رجوع کن به LoanEntity.photoPath). */
    val photoPath: String? = null,
)
