package ir.sadteam.loancalc.core

/**
 * منطقِ خالصِ **پیشنهادِ خودکارِ نوارِ پایین** - فریمِ `41a` و قاعده‌های `41c`.
 *
 * عمداً تو `:core`ه و هیچ وابستگیِ اندرویدی نداره: شرط‌های پیشنهاد (پنجره‌ی سه هفته، نسبتِ
 * ۵ به ۱، فاصله‌ی ۶۰ روز) دقیقاً همون چیزی‌ان که باید تستِ رگرسیون داشته باشن، و رو JVM
 * سریع تست می‌شن. لایه‌ی UI فقط شناسه‌ی رشته‌ای پاس می‌ده - آیکون و برچسب کارِ `:app`ه.
 */
object NavSuggestion {

    /** حداقل روزِ داده‌ی جمع‌شده تا اولین پیشنهاد. قاعده‌ی `41c`. */
    const val MIN_DAYS = 21

    /** نسبتِ استفاده‌ی نامزدِ بالا رفتن به نامزدِ پایین رفتن. */
    const val MIN_RATIO = 5

    /** فاصله‌ی اجباری بینِ دو پیشنهاد. */
    const val COOLDOWN_DAYS = 60

    /** پنجره‌ی شمارش - «سه هفته‌ی گذشته». */
    const val WINDOW_DAYS = 21

    private const val DAY_MS = 24L * 60 * 60 * 1000

    /**
     * یه پیشنهادِ آماده‌ی نمایش.
     *
     * [pairKey] کلیدِ ضدِتکراره: «نه» فقط همین جفت رو می‌بنده، نه کلِ قابلیت رو (قاعده‌ی `41c`).
     */
    data class Result(
        val promote: String,
        val demote: String,
        val promoteCount: Int,
        val demoteCount: Int,
        val slotIndex: Int,
    ) {
        val pairKey: String get() = "$promote>$demote"
    }

    /**
     * محاسبه‌ی پیشنهاد. اگه هر کدوم از سه شرط برقرار نباشه `null` برمی‌گردونه.
     *
     * @param slots نوارِ فعلی؛ اسلاتِ ۰ (راست‌ترین تو RTL) همیشه قفله و نامزدِ پایین‌رفتن نیست.
     * @param counts شمارشِ بازشدنِ هر مقصد **در همون پنجره‌ی سه هفته** (فیلترشده، نه خام).
     * @param firstDayMillis اولین روزی که اصلاً شمارش شروع شده - برای شرطِ «≥۲۱ روز داده».
     * @param lastSuggestedAt زمانِ آخرین پیشنهادِ نشون‌داده‌شده (۰ یعنی هیچ‌وقت).
     * @param dismissedPairs جفت‌هایی که کاربر قبلاً «نه» گفته.
     */
    fun compute(
        slots: List<String>,
        counts: Map<String, Int>,
        firstDayMillis: Long,
        lastSuggestedAt: Long,
        dismissedPairs: Set<String>,
        nowMillis: Long,
    ): Result? {
        if (slots.size < 2) return null
        if (firstDayMillis <= 0L) return null
        if (daysBetween(firstDayMillis, nowMillis) < MIN_DAYS) return null
        if (lastSuggestedAt > 0L && daysBetween(lastSuggestedAt, nowMillis) < COOLDOWN_DAYS) return null

        // نامزدِ بالا رفتن: پرکاربردترین مقصدی که **تو نوار نیست**.
        val promote = counts.entries
            .filter { it.key !in slots }
            .maxByOrNull { it.value }
            ?: return null
        if (promote.value <= 0) return null

        // نامزدِ پایین رفتن: کم‌کاربردترین اسلاتِ **آزاد** (اسلاتِ ۰ قفله).
        val demote = slots.drop(1)
            .minByOrNull { counts[it] ?: 0 }
            ?: return null
        val demoteCount = counts[demote] ?: 0

        // نسبتِ ۵ به ۱. شمارشِ صفر همیشه شرط رو پاس می‌کنه (تقسیم بر صفر نداریم).
        if (promote.value < maxOf(demoteCount * MIN_RATIO, MIN_RATIO)) return null

        val result = Result(
            promote = promote.key,
            demote = demote,
            promoteCount = promote.value,
            demoteCount = demoteCount,
            slotIndex = slots.indexOf(demote),
        )
        if (result.pairKey in dismissedPairs) return null
        return result
    }

    /** نوارِ بعد از اعمالِ پیشنهاد - مقصدِ تازه دقیقاً **سرِ جای** مقصدِ کنارگذاشته می‌شینه. */
    fun apply(slots: List<String>, suggestion: Result): List<String> =
        slots.toMutableList().apply { this[suggestion.slotIndex] = suggestion.promote }

    /**
     * کلیدِ هفته برای `destinationOpenCount(destId, weekKey)`.
     *
     * شماره‌ی هفته از مبدأ اپوک - نه تقویمِ شمسی و نه میلادی. چون فقط برای «پنجره‌ی سه هفته»
     * استفاده می‌شه، مرزِ هفته اهمیتِ نمایشی نداره و همین ساده‌ترین شکلِ درسته.
     */
    fun weekKey(millis: Long): Int = (millis / (7 * DAY_MS)).toInt()

    /** کلیدهای هفته‌ی داخلِ پنجره - شاملِ همین هفته و دو هفته‌ی قبل. */
    fun windowWeeks(nowMillis: Long): List<Int> {
        val current = weekKey(nowMillis)
        return listOf(current, current - 1, current - 2)
    }

    private fun daysBetween(from: Long, to: Long): Long = (to - from) / DAY_MS
}
