package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * تستِ رگرسیونِ نشانِ «فعال».
 *
 * ⚠️ `:core` فقط `kotlin("test")` رو کلاس‌پثِ تستش داره نه JUnit4 - ایمپورت‌ها از
 * `kotlin.test.*`ان (قاعده‌ی ماندگارِ پروژه؛ یه‌بار همین CI رو شکست).
 */
class ActiveStreakTest {
    private val today = PersianDate(1405, 5, 29)

    private fun keys(vararg offsets: Int): Set<String> =
        offsets.map { ActiveStreak.dateKey(PersianCalendar.addDays(today, -it)) }.toSet()

    @Test
    fun `دفترِ خالی یعنی صفر`() {
        assertEquals(0, ActiveStreak.countActiveDays(emptySet(), today))
    }

    @Test
    fun `هفت روزِ پشتِ‌سرهم هفت شمرده می‌شه`() {
        assertEquals(7, ActiveStreak.countActiveDays(keys(0, 1, 2, 3, 4, 5, 6), today))
    }

    @Test
    fun `امروز خالی ولی دیروز پر - زنجیر پاره نیست`() {
        assertEquals(3, ActiveStreak.countActiveDays(keys(1, 2, 3), today))
    }

    @Test
    fun `دیروز و امروز هر دو خالی یعنی زنجیرِ پاره`() {
        assertEquals(0, ActiveStreak.countActiveDays(keys(2, 3, 4), today))
    }

    @Test
    fun `روزِ جاافتاده وسط، فقط تا قبلش شمرده می‌شه`() {
        // روزهای ۰ و ۱ هست، ۲ نیست، ۳ و ۴ هست → فقط ۲ روز.
        assertEquals(2, ActiveStreak.countActiveDays(keys(0, 1, 3, 4), today))
    }

    @Test
    fun `عبور از مرزِ ماه هم درست شمرده می‌شه`() {
        // اولِ ماه: زنجیر باید به ماهِ قبل سرریز کنه (addDays منفی - باگِ رفع‌شده‌ی قدیمی).
        val firstOfMonth = PersianDate(1405, 6, 1)
        val days = (0..4).map { ActiveStreak.dateKey(PersianCalendar.addDays(firstOfMonth, -it)) }.toSet()
        assertEquals(5, ActiveStreak.countActiveDays(days, firstOfMonth))
    }

    @Test
    fun `زنجیرِ سالم قابلِ ترمیم نیست`() {
        assertNull(ActiveStreak.repairable(keys(0, 1, 2), today))
    }

    @Test
    fun `یک روزِ جاافتاده - ترمیم با یک کلیدِ خالی`() {
        // آخرین روزِ فعال دو روز پیش بوده → فقط دیروز باید پر بشه.
        val repair = ActiveStreak.repairable(keys(2, 3, 4), today)!!
        assertEquals(3, repair.lostDays)
        assertEquals(listOf(ActiveStreak.dateKey(PersianCalendar.addDays(today, -1))), repair.missingKeys)
    }

    @Test
    fun `دو روزِ جاافتاده هم هنوز داخلِ ۴۸ ساعته`() {
        val repair = ActiveStreak.repairable(keys(3, 4), today)!!
        assertEquals(2, repair.lostDays)
        assertEquals(2, repair.missingKeys.size)
    }

    @Test
    fun `بیشتر از ۴۸ ساعت دیگه ترمیم نمی‌شه`() {
        assertNull(ActiveStreak.repairable(keys(4, 5, 6), today))
    }
}
