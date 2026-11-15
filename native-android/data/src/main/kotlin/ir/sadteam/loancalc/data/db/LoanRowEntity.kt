package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index

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
 *
 * **باگِ کرشِ رفع‌شده**: `MIGRATION_11_12` (تو [AppDatabase]) صریحاً یه ایندکس رو `loanId` می‌سازه
 * (`CREATE INDEX index_loan_rows_loanId`)، ولی این کلاس اولش هیچ `indices`ای نداشت - موقعِ بازکردنِ
 * دیتابیس، Room ساختارِ واقعیِ جدول رو با چیزی که از رو همین annotation انتظار داره مقایسه می‌کنه؛
 * چون ایندکس تو کد اعلام نشده بود ولی تو دیتابیسِ واقعی بود، این تناقض باعثِ
 * `IllegalStateException: Migration didn't properly handle...` می‌شد - یعنی اپ رو هر گوشی‌ای که این
 * migration روش اجرا شده بود (نه رو دیتابیسِ خالی) همیشه بلافاصله بعدِ باز شدن کرش می‌کرد. رفع شد با
 * اضافه‌کردنِ `indices = [Index("loanId")]` پایین - اگه ایندکسِ دیگه‌ای هم بعداً تو migration دستی
 * ساخته شد، حتماً همین‌جا هم اعلامش کن، وگرنه همین باگ تکرار می‌شه.
 */
@Entity(tableName = "loan_rows", primaryKeys = ["loanId", "m"], indices = [Index("loanId")])
data class LoanRowEntity(
    val loanId: Long,
    val m: Int,
    val installment: Double,
    val paid: Boolean,
    val paidLate: Boolean = false,
    val paidDateY: Int? = null,
    val paidDateM: Int? = null,
    val paidDateD: Int? = null,
    /**
     * مسیرِ عکسِ رسیدِ همین قسطِ خاص (نه عکسِ کلیِ رو خودِ وام - رجوع کن به LoanEntity.photoPath).
     *
     * ⚠️ طرح (کارتِ `36d`) **چند عکس** می‌خواد. برای اینکه ستونِ موجود و داده‌ی کاربرهای فعلی
     * دست‌نخورده بمونه، چندعکسی با **جداکننده‌ی `|`** تو همین ستون نگه داشته می‌شه؛
     * `LoanRowEntity.photoPaths` و `withPhotoPaths()` تنها راهِ خوندن/نوشتنشن.
     */
    val photoPath: String? = null,
    /** یادداشتِ آزادِ همین قسط - کارتِ `36d`. */
    val note: String? = null,
    /** شماره‌ی پیگیریِ پرداخت - کارتِ `36d` (تو UI دکمه‌ی کپی داره). */
    val trackingNumber: String? = null,
) {
    /** لیستِ عکس‌های رسید (ممکنه خالی باشه). */
    val photoPaths: List<String>
        get() = photoPath?.split('|')?.filter { it.isNotBlank() } ?: emptyList()

    /** تنها نقطه‌ی نوشتنِ عکس‌ها - قاعده‌ی «یک نقطه‌ی نوشتنِ واحد» برای هر داده‌ی کدگذاری‌شده. */
    fun withPhotoPaths(paths: List<String>): LoanRowEntity =
        copy(photoPath = paths.filter { it.isNotBlank() }.joinToString("|").ifBlank { null })
}
