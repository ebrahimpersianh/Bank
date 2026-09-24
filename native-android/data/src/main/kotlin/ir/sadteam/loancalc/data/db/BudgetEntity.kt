package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** سقفِ ماهانه‌ی هزینه به‌ازای هر دسته - هر ماه دوباره ساخته نمی‌شه، همیشه رو ماهِ جاری اعمال
 * می‌شه (مصرفِ واقعیِ ماه از رو account_transactions حساب می‌شه، نه اینکه اینجا ذخیره بشه). */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: Long,
    val categoryName: String,
    val monthlyCap: Double,
    /** به کدوم حساب‌کتاب محدوده؟ `null` یعنی **همه‌ی حساب‌کتاب‌ها** (رفتارِ قبلی و پیش‌فرض) -
     * خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع: موقعِ تعیینِ بودجه بشه یه حساب‌کتابِ خاص رو انتخاب کرد. */
    val accountId: Long? = null,
)
