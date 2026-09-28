package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * **قاعده‌ی تشخیصِ دسته‌بندیِ خودکار** - تنها جای برنامه که کاربرِ غیرِفنی یه منطق می‌نویسه.
 *
 * قاعده‌ی صریحِ طراحی که موتور هم بر همون بنا شده: **قاعده‌ها مرتب‌ان و اولین تطبیق برنده‌ست.**
 * بدونِ ترتیبِ دیدنی، «چرا این تراکنش دسته‌ی اشتباه خورد» هیچ راهِ توضیحی نداره.
 *
 * الگو عمداً فقط «شامل است»ه - نه regex، نه wildcard. کاربرِ ما regex نمی‌نویسه و اگه بنویسه
 * هم اشکالش رو نمی‌فهمه.
 */
@Entity(tableName = "parsing_rules")
data class ParsingRuleEntity(
    @PrimaryKey val id: Long,
    /** تکه‌متنی که باید تو متنِ پیامک باشه. «رفاه» هم «فروشگاه رفاه» رو می‌گیره. */
    val pattern: String,
    /** دسته‌ی مقصد - نامِ دسته، هم‌الگو با `CustomCategoryEntity.parentName`. */
    val category: String,
    /** `null` یعنی «مهم نیست»؛ وگرنه `WITHDRAWAL` یا `DEPOSIT`. */
    val txType: String? = null,
    /** جای قاعده تو ترتیبِ بررسی. کوچیک‌تر زودتر بررسی می‌شه. */
    val sortOrder: Int = 0,
    /**
     * خودِ موتور ساختتش (بعد از سه بار دسته‌بندیِ دستیِ یه فروشنده) یا کاربر؟
     * با ویرایشِ کاربر `false` می‌شه - «چیپِ خودکار می‌افته».
     */
    val auto: Boolean = false,
    /** چند بار تا حالا واقعاً تطبیق داده. صفرش به کاربر با رنگِ هشدار نشون داده می‌شه. */
    val matchCount: Int = 0,
)
