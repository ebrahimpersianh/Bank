package ir.sadteam.loancalc.core

import java.util.Locale

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
    fun dateKey(date: PersianDate): String = String.format(Locale.US, "%04d-%02d-%02d", date.y, date.m, date.d)

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

    /**
     * زنجیرِ **پاره‌شده‌ی قابلِ ترمیم**.
     *
     * فقط تا **۴۸ ساعت** بعد از پاره‌شدن پیشنهاد می‌شه (یعنی آخرین روزِ فعال دو یا سه روز پیش
     * بوده)؛ بعدش دیگه نه کارت نشون داده می‌شه نه ترمیم معنی داره.
     *
     * @return طولِ زنجیرِ ازدست‌رفته و روزهایی که باید پر بشن، یا `null` اگه چیزی برای ترمیم نیست.
     */
    fun repairable(days: Set<String>, today: PersianDate): StreakRepair? {
        if (days.isEmpty()) return null
        // زنجیر فقط وقتی «پاره» است که نه امروز نه دیروز فعال نبوده.
        if (countActiveDays(days, today) > 0) return null
        var cursor = PersianCalendar.addDays(today, -2)
        var gap = 2
        while (gap <= 3) {
            if (days.contains(dateKey(cursor))) {
                val missing = (1 until gap).map { back -> dateKey(PersianCalendar.addDays(today, -back)) }
                return StreakRepair(
                    lostDays = countActiveDays(days, cursor),
                    missingKeys = missing,
                )
            }
            cursor = PersianCalendar.addDays(cursor, -1)
            gap++
        }
        return null
    }
}

/** خروجیِ [ActiveStreak.repairable] - طولِ زنجیرِ ازدست‌رفته و روزهای خالیِ وسط. */
data class StreakRepair(val lostDays: Int, val missingKeys: List<String>)
