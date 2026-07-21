package ir.sadteam.roozegar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MonthGridTest {
    @Test
    fun gridOfFarvardin1403StartsOnWednesday() {
        // ۱ فروردین ۱۴۰۳ چهارشنبه بود (شنبه=۰ → ۴ خونه‌ی خالی اول) و فروردین ۳۱ روزه‌ست.
        val grid = MonthGrid.of(1403, 1)
        assertEquals(4, grid.leadingBlanks)
        assertEquals(31, grid.daysInMonth)
        assertEquals(5, grid.weeks)
    }

    @Test
    fun leapEsfandHas30Days() {
        assertEquals(30, MonthGrid.of(1403, 12).daysInMonth)
        assertEquals(29, MonthGrid.of(1404, 12).daysInMonth)
    }

    @Test
    fun monthIndexRoundTrips() {
        for (y in intArrayOf(1400, 1405, 1433)) {
            for (m in 1..12) {
                val (yy, mm) = MonthGrid.fromIndex(MonthGrid.indexOf(y, m))
                assertEquals(y, yy)
                assertEquals(m, mm)
            }
        }
        // یه قدم جلو/عقب از مرز سال باید ماه رو درست بچرخونه.
        assertEquals(Pair(1406, 1), MonthGrid.fromIndex(MonthGrid.indexOf(1405, 12) + 1))
        assertEquals(Pair(1404, 12), MonthGrid.fromIndex(MonthGrid.indexOf(1405, 1) - 1))
    }

    @Test
    fun persianDigitsConversion() {
        assertEquals("۱۴۰۵", 1405.toPersianDigits())
        assertEquals("۳۰ تیر ۱۴۰۵", PersianDate(1405, 4, 30).format())
        assertEquals("سه‌شنبه ۳۰ تیر ۱۴۰۵", PersianDate(1405, 4, 30).format(withWeekday = true))
    }

    @Test
    fun occasionsLookupWorks() {
        assertTrue(Occasions.isHoliday(PersianDate(1405, 1, 1)))
        assertEquals(EffectType.BLOSSOM, Occasions.effectOf(PersianDate(1405, 1, 1)))
        assertEquals(EffectType.STARS, Occasions.effectOf(PersianDate(1405, 9, 30)))
        assertNull(Occasions.titlesOf(PersianDate(1405, 2, 7)))
        assertEquals(EffectType.NONE, Occasions.effectOf(PersianDate(1405, 2, 7)))
    }
}
