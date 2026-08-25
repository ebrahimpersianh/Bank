package ir.sadteam.loancalc.core

/**
 * منطقِ خالصِ نشانِ **«فعال»** - روزهای پشتِ‌سرهمی که کاربر تراکنش ثبت کرده.
 *
 * ⚠️ واژه‌ی «استریک» عمداً هیچ‌جا به کار نرفته - خواسته‌ی صریحِ کاربر اینه که همه‌جا **«فعال»**
 * نوشته بشه.
 *
 * اینجا (تو `:core`) نشسته نه کنارِ ریپازیتوری، چون `:core` وابستگیِ اندرویدی نداره و `:core:test`
 * رو JVM سریع اجرا می‌شه - همون‌جایی که تستِ رگرسیونِ این منطق زندگی می‌کنه.
 */
object ActiveStreak {
    /** کلیدِ روز به شکلِ `YYYY-MM-DD` شمسی؛ مرتب‌سازیِ متنی هم درست کار می‌کنه. */
    fun dateKey(date: PersianDate): String = "%04d-%02d-%02d".format(date.y, date.m, date.d)

    /**
     * شمارِ روزهای پشتِ‌سرهمِ فعال، از امروز به عقب.
     *
     * ⚠️ اگه کاربر **امروز** هنوز چیزی ثبت نکرده ولی **دیروز** کرده، زنجیر پاره نیست و از دیروز
     * شمرده می‌شه؛ وگرنه کسی که هنوز صبح چیزی ثبت نکرده زنجیرش رو صفر می‌دید.
     */
    fun countActiveDays(days: Set<String>, today: PersianDate): Int {
        if (days.isEmpty()) return 0
        var cursor = if (days.contains(dateKey(today))) today else PersianCalendar.addDays(today, -1)
        if (!days.contains(dateKey(cursor))) return 0
        var count = 0
        while (days.contains(dateKey(cursor))) {
            count++
            cursor = PersianCalendar.addDays(cursor, -1)
        }
        return count
    }
}
