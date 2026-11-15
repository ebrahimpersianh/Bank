package ir.sadteam.loancalc.core

import kotlin.math.abs

/**
 * **اشتراک‌یاب** - پیداکردنِ خرج‌های تکرارشونده‌ی ماهانه (اینترنت، فیلیمو، بیمه، باشگاه…) از رو
 * خودِ تراکنش‌های ثبت‌شده، بدونِ اینکه کاربر چیزی اعلام کنه.
 *
 * چرا مهمه: بزرگ‌ترین «نشتیِ» بودجه‌ی آدم‌ها همین خرج‌های کوچیکِ ماهانه‌ای‌ان که فراموش می‌شن.
 * کاربر تا وقتی جمعشون رو یکجا نبینه («ماهی ۳ میلیون بابتِ ۷ اشتراک») متوجهشون نمی‌شه.
 *
 * ⚠️ این فایل عمداً هیچ وابستگیِ اندرویدی/دیتابیسی نداره: ورودیش یه لیستِ ساده‌ی
 * [RecurringInput]ه که لایه‌ی بالاتر از رو `account_transactions` می‌سازه. پس کاملاً رو JVM
 * تست می‌شه (`:core:test`).
 */

/** یه تراکنشِ خرج، در حدی که اشتراک‌یاب لازم داره. */
data class RecurringInput(
    val description: String,
    val amountRial: Double,
    val year: Int,
    val month: Int,
    val day: Int,
    val category: String? = null,
)

/** یه خرجِ تکرارشونده‌ی کشف‌شده. [monthsSeen] تعدادِ ماه‌های متمایزیه که این خرج توش دیده شده. */
data class RecurringExpense(
    val label: String,
    val typicalAmountRial: Double,
    val monthsSeen: Int,
    val lastYear: Int,
    val lastMonth: Int,
    val lastDay: Int,
    val category: String?,
) {
    /** روزِ تقریبیِ ماه که معمولاً کسر می‌شه - برای پیام «حدودِ روزِ X هر ماه». */
    val dayOfMonth: Int get() = lastDay
}

object RecurringDetector {
    /** کمترین تعدادِ ماهی که یه خرج باید توش دیده بشه تا «تکرارشونده» حساب بشه. */
    const val MIN_MONTHS = 3

    /** اختلافِ مجازِ مبلغ بینِ ماه‌ها (۱۵٪) - قبضِ اینترنت هر ماه دقیقاً یه عدد نیست. */
    private const val AMOUNT_TOLERANCE = 0.15

    /**
     * توضیحِ تراکنش رو به یه «کلید» تبدیل می‌کنه تا ماه‌های مختلفِ یه خرج به هم برسن:
     * عدد، تاریخ، شماره‌ی پیگیری و کلمه‌های عمومی حذف می‌شن.
     *
     * مثال: «خرید اینترنتی ایرانسل ۱۲۳۴۵» و «خرید اینترنتی ایرانسل ۹۹۸۸» هر دو → «خرید اینترنتی ایرانسل».
     */
    fun normalizeLabel(raw: String): String {
        var s = normalizePersianName(raw)
        s = s.replace(Regex("[0-9۰-۹]+"), " ")
        NOISE_WORDS.forEach { s = s.replace(it, " ") }
        // ⚠️ **علائم هم باید برن، نه فقط عدد و کلمه.** وگرنه «اسنپ -» و «اسنپ» دو گروهِ
        // جدا می‌شن و هیچ‌کدوم به حدنصابِ سه ماه نمی‌رسن - یعنی اشتراکِ واقعی اصلاً کشف
        // نمی‌شه. تستِ normalizeLabel_strips_digits_and_noise همین رو گرفت.
        s = s.replace(Regex("[-–—_,،:;.·/\\\\()\\[\\]{}*#+]"), " ")
        return s.replace(Regex("\\s+"), " ").trim()
    }

    private val NOISE_WORDS = listOf(
        "خرید اینترنتی", "خرید", "پرداخت", "کارت به کارت", "انتقال", "تراکنش",
        "شماره پیگیری", "پیگیری", "بابت", "از حساب", "به حساب", "ریال", "تومان",
    )

    /**
     * خرج‌های تکرارشونده رو برمی‌گردونه، از پرخرج‌ترین به کم‌خرج‌ترین.
     *
     * قاعده: خرج‌هایی که **با همون توضیح** تو حداقل [MIN_MONTHS] ماهِ متمایز دیده شدن و مبلغشون
     * بینِ ماه‌ها بیشتر از [AMOUNT_TOLERANCE] فرق نکرده. مبلغِ اعلامی **میانه**ست نه میانگین
     * (یه ماهِ پرت کلِ عدد رو خراب نکنه).
     */
    fun detect(transactions: List<RecurringInput>): List<RecurringExpense> {
        val groups = transactions
            .filter { it.amountRial > 0 }
            .groupBy { normalizeLabel(it.description) }
            .filterKeys { it.length >= 3 }

        val found = mutableListOf<RecurringExpense>()
        for ((label, rows) in groups) {
            // ماهِ متمایز، نه تعدادِ تراکنش: دو خریدِ یه ماه، «تکرارِ ماهانه» نیست.
            val byMonth = rows.groupBy { it.year * 12 + it.month }
            if (byMonth.size < MIN_MONTHS) continue

            // مبلغِ نماینده‌ی هر ماه = بزرگ‌ترین تراکنشِ همون ماه با این برچسب.
            val monthly = byMonth.values.map { m -> m.maxOf { it.amountRial } }.sorted()
            val median = monthly[monthly.size / 2]
            if (median <= 0.0) continue
            // اگه مبلغ‌ها بینِ ماه‌ها خیلی فرق دارن، این یه اشتراکِ ثابت نیست (مثلاً خریدِ روزمره).
            val stable = monthly.count { abs(it - median) / median <= AMOUNT_TOLERANCE }
            if (stable < byMonth.size - 1) continue

            val last = rows.maxWithOrNull(compareBy({ it.year }, { it.month }, { it.day })) ?: continue
            found += RecurringExpense(
                label = label,
                typicalAmountRial = median,
                monthsSeen = byMonth.size,
                lastYear = last.year,
                lastMonth = last.month,
                lastDay = last.day,
                category = rows.mapNotNull { it.category }.groupingBy { it }.eachCount()
                    .maxByOrNull { it.value }?.key,
            )
        }
        return found.sortedByDescending { it.typicalAmountRial }
    }

    /** جمعِ ماهانه‌ی همه‌ی اشتراک‌های کشف‌شده - عددی که تو کارتِ خانه نشون داده می‌شه. */
    fun monthlyTotal(found: List<RecurringExpense>): Double = found.sumOf { it.typicalAmountRial }

    /**
     * اشتراک‌هایی که **این ماه هنوز کسر نشدن** - یعنی آخرین باری که دیده شدن قبل از ماهِ جاریه.
     * برای هشدارِ «این‌ها تا آخرِ ماه از حسابت می‌ره».
     */
    fun upcomingThisMonth(
        found: List<RecurringExpense>,
        currentYear: Int,
        currentMonth: Int,
    ): List<RecurringExpense> = found.filter { it.lastYear * 12 + it.lastMonth < currentYear * 12 + currentMonth }
}
