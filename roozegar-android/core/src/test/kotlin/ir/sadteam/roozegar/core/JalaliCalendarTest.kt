package ir.sadteam.roozegar.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * موارد تست از رو کتابخونه‌ی پایتون jdatetime (منبع مرجع تقویم جلالی رسمی) گرفته شدن - نه دستی
 * حدس‌زده شدن - و شامل مرزهای اسفند ۲۹/۳۰ (کبیسه) برای چند سال پشت‌سرهم هم هستن (از جمله سال‌های
 * کبیسه‌ی ۱۴۰۳ و ۱۴۰۸ و محدوده‌ی ۱۴۳۳). اگه الگوریتم JalaliCalendar عوض شد، این تست‌ها باید
 * همچنان سبز بمونن.
 */
class JalaliCalendarTest {
    private data class TestCase(val gy: Int, val gm: Int, val gd: Int, val jy: Int, val jm: Int, val jd: Int)

    private val cases = listOf(
        TestCase(1985, 9, 7, 1364, 6, 16),
        TestCase(1988, 10, 29, 1367, 8, 7),
        TestCase(1992, 6, 11, 1371, 3, 21),
        TestCase(1992, 10, 8, 1371, 7, 16),
        TestCase(1992, 11, 17, 1371, 8, 26),
        TestCase(1993, 1, 22, 1371, 11, 2),
        TestCase(1996, 12, 15, 1375, 9, 25),
        TestCase(1998, 8, 30, 1377, 6, 8),
        TestCase(2001, 1, 15, 1379, 10, 26),
        TestCase(2001, 1, 22, 1379, 11, 3),
        TestCase(2002, 11, 24, 1381, 9, 3),
        TestCase(2002, 12, 14, 1381, 9, 23),
        TestCase(2003, 4, 13, 1382, 1, 24),
        TestCase(2004, 1, 28, 1382, 11, 8),
        TestCase(2004, 6, 10, 1383, 3, 21),
        TestCase(2005, 8, 29, 1384, 6, 7),
        TestCase(2007, 4, 16, 1386, 1, 27),
        TestCase(2007, 7, 14, 1386, 4, 23),
        TestCase(2009, 3, 3, 1387, 12, 13),
        TestCase(2013, 5, 21, 1392, 2, 31),
        TestCase(2015, 3, 19, 1393, 12, 28),
        TestCase(2015, 3, 20, 1393, 12, 29),
        TestCase(2015, 3, 21, 1394, 1, 1),
        TestCase(2015, 5, 26, 1394, 3, 5),
        TestCase(2016, 3, 19, 1394, 12, 29),
        TestCase(2016, 3, 20, 1395, 1, 1),
        TestCase(2016, 3, 21, 1395, 1, 2),
        TestCase(2017, 3, 19, 1395, 12, 29),
        TestCase(2017, 3, 20, 1395, 12, 30),
        TestCase(2017, 3, 21, 1396, 1, 1),
        TestCase(2017, 7, 11, 1396, 4, 20),
        TestCase(2018, 3, 19, 1396, 12, 28),
        TestCase(2018, 3, 20, 1396, 12, 29),
        TestCase(2018, 3, 21, 1397, 1, 1),
        TestCase(2018, 7, 21, 1397, 4, 30),
        TestCase(2019, 2, 28, 1397, 12, 9),
        TestCase(2019, 3, 19, 1397, 12, 28),
        TestCase(2019, 3, 20, 1397, 12, 29),
        TestCase(2019, 3, 21, 1398, 1, 1),
        TestCase(2019, 5, 15, 1398, 2, 25),
        TestCase(2019, 10, 27, 1398, 8, 5),
        TestCase(2020, 3, 19, 1398, 12, 29),
        TestCase(2020, 3, 20, 1399, 1, 1),
        TestCase(2020, 3, 21, 1399, 1, 2),
        TestCase(2021, 2, 14, 1399, 11, 26),
        TestCase(2021, 3, 19, 1399, 12, 29),
        TestCase(2021, 3, 20, 1399, 12, 30),
        TestCase(2021, 3, 21, 1400, 1, 1),
        TestCase(2021, 3, 30, 1400, 1, 10),
        TestCase(2022, 3, 19, 1400, 12, 28),
        TestCase(2022, 3, 20, 1400, 12, 29),
        TestCase(2022, 3, 21, 1401, 1, 1),
        TestCase(2022, 10, 30, 1401, 8, 8),
        TestCase(2023, 2, 24, 1401, 12, 5),
        TestCase(2023, 3, 19, 1401, 12, 28),
        TestCase(2023, 3, 20, 1401, 12, 29),
        TestCase(2023, 3, 21, 1402, 1, 1),
        TestCase(2024, 3, 19, 1402, 12, 29),
        TestCase(2024, 3, 20, 1403, 1, 1),
        TestCase(2024, 3, 21, 1403, 1, 2),
        TestCase(2025, 3, 19, 1403, 12, 29),
        TestCase(2025, 3, 20, 1403, 12, 30),
        TestCase(2025, 3, 21, 1404, 1, 1),
        TestCase(2026, 3, 19, 1404, 12, 28),
        TestCase(2026, 3, 20, 1404, 12, 29),
        TestCase(2026, 3, 21, 1405, 1, 1),
        TestCase(2026, 5, 27, 1405, 3, 6),
        TestCase(2027, 1, 30, 1405, 11, 10),
        TestCase(2027, 3, 19, 1405, 12, 28),
        TestCase(2027, 3, 20, 1405, 12, 29),
        TestCase(2027, 3, 21, 1406, 1, 1),
        TestCase(2028, 3, 19, 1406, 12, 29),
        TestCase(2028, 3, 20, 1407, 1, 1),
        TestCase(2028, 3, 21, 1407, 1, 2),
        TestCase(2028, 11, 23, 1407, 9, 3),
        TestCase(2029, 3, 19, 1407, 12, 29),
        TestCase(2029, 3, 20, 1408, 1, 1),
        TestCase(2029, 3, 21, 1408, 1, 2),
        TestCase(2030, 3, 19, 1408, 12, 29),
        TestCase(2030, 3, 20, 1408, 12, 30),
        TestCase(2030, 3, 21, 1409, 1, 1),
        TestCase(2031, 3, 19, 1409, 12, 28),
        TestCase(2031, 3, 20, 1409, 12, 29),
        TestCase(2031, 3, 21, 1410, 1, 1),
        TestCase(2032, 3, 19, 1410, 12, 29),
        TestCase(2032, 3, 20, 1411, 1, 1),
        TestCase(2032, 3, 21, 1411, 1, 2),
        TestCase(2032, 6, 7, 1411, 3, 18),
        TestCase(2032, 8, 15, 1411, 5, 25),
        TestCase(2033, 3, 19, 1411, 12, 29),
        TestCase(2033, 3, 20, 1412, 1, 1),
        TestCase(2033, 3, 21, 1412, 1, 2),
        TestCase(2033, 4, 28, 1412, 2, 9),
        TestCase(2034, 3, 19, 1412, 12, 29),
        TestCase(2034, 3, 20, 1412, 12, 30),
        TestCase(2034, 3, 21, 1413, 1, 1),
        TestCase(2035, 1, 21, 1413, 11, 1),
        TestCase(2035, 3, 19, 1413, 12, 28),
        TestCase(2035, 3, 20, 1413, 12, 29),
        TestCase(2035, 3, 21, 1414, 1, 1),
        TestCase(2035, 11, 11, 1414, 8, 20),
        TestCase(2036, 3, 19, 1414, 12, 29),
        TestCase(2036, 3, 20, 1415, 1, 1),
        TestCase(2036, 3, 21, 1415, 1, 2),
        TestCase(2037, 3, 19, 1415, 12, 29),
        TestCase(2037, 3, 20, 1416, 1, 1),
        TestCase(2037, 3, 21, 1416, 1, 2),
        TestCase(2037, 10, 15, 1416, 7, 24),
        TestCase(2038, 3, 19, 1416, 12, 29),
        TestCase(2038, 3, 20, 1416, 12, 30),
        TestCase(2038, 3, 21, 1417, 1, 1),
        TestCase(2038, 10, 12, 1417, 7, 20),
        TestCase(2039, 3, 19, 1417, 12, 28),
        TestCase(2039, 3, 20, 1417, 12, 29),
        TestCase(2039, 3, 21, 1418, 1, 1),
        TestCase(2040, 1, 6, 1418, 10, 16),
        TestCase(2040, 3, 19, 1418, 12, 29),
        TestCase(2040, 3, 20, 1419, 1, 1),
        TestCase(2040, 3, 21, 1419, 1, 2),
        TestCase(2040, 12, 22, 1419, 10, 2),
        TestCase(2041, 3, 19, 1419, 12, 29),
        TestCase(2041, 3, 20, 1420, 1, 1),
        TestCase(2041, 3, 21, 1420, 1, 2),
        TestCase(2042, 3, 19, 1420, 12, 29),
        TestCase(2042, 3, 20, 1420, 12, 30),
        TestCase(2042, 3, 21, 1421, 1, 1),
        TestCase(2043, 3, 19, 1421, 12, 28),
        TestCase(2043, 3, 20, 1421, 12, 29),
        TestCase(2043, 3, 21, 1422, 1, 1),
        TestCase(2044, 3, 19, 1422, 12, 29),
        TestCase(2044, 3, 20, 1423, 1, 1),
        TestCase(2044, 3, 21, 1423, 1, 2),
        TestCase(2044, 4, 6, 1423, 1, 18),
        TestCase(2045, 10, 9, 1424, 7, 18),
        TestCase(2046, 3, 21, 1425, 1, 1),
        TestCase(2046, 5, 24, 1425, 3, 3),
        TestCase(2047, 11, 5, 1426, 8, 14),
        TestCase(2049, 4, 19, 1428, 1, 31),
        TestCase(2049, 10, 17, 1428, 7, 26),
        TestCase(2050, 5, 28, 1429, 3, 7),
        TestCase(2053, 3, 4, 1431, 12, 14),
        TestCase(2054, 6, 25, 1433, 4, 4),
        TestCase(2055, 9, 22, 1434, 6, 31),
        TestCase(2056, 2, 10, 1434, 11, 21),
        TestCase(2058, 10, 10, 1437, 7, 18),
        TestCase(2061, 4, 3, 1440, 1, 15),
        TestCase(2062, 4, 16, 1441, 1, 28),
        TestCase(2065, 12, 3, 1444, 9, 13),
        TestCase(2069, 3, 9, 1447, 12, 19),
        TestCase(2069, 12, 12, 1448, 9, 22),
    )

    @Test
    fun fromGregorianMatchesReferenceVectors() {
        cases.forEach { c ->
            val result = JalaliCalendar.fromGregorian(c.gy, c.gm, c.gd)
            assertEquals(
                PersianDate(c.jy, c.jm, c.jd),
                result,
                "gregorian ${c.gy}-${c.gm}-${c.gd} should be jalali ${c.jy}-${c.jm}-${c.jd}, got $result",
            )
        }
    }

    @Test
    fun toGregorianMatchesReferenceVectors() {
        cases.forEach { c ->
            val result = JalaliCalendar.toGregorian(PersianDate(c.jy, c.jm, c.jd))
            assertEquals(GregorianDate(c.gy, c.gm, c.gd), result, "case $c")
        }
    }

    @Test
    fun roundTripIsStable() {
        cases.forEach { c ->
            val jalali = JalaliCalendar.fromGregorian(c.gy, c.gm, c.gd)
            val back = JalaliCalendar.toGregorian(jalali)
            assertEquals(GregorianDate(c.gy, c.gm, c.gd), back)
        }
    }

    @Test
    fun daysBetweenCountsRealCalendarDays() {
        // ۱۴۰۳/۰۱/۰۱ (نوروز، ۲۰۲۴-۰۳-۲۰) تا ۱۴۰۴/۰۱/۰۱ (۲۰۲۵-۰۳-۲۱) دقیقاً ۳۶۶ روزه (۱۴۰۳ کبیسه‌ست)
        assertEquals(366, JalaliCalendar.daysBetween(PersianDate(1403, 1, 1), PersianDate(1404, 1, 1)))
    }

    @Test
    fun todayReturnsAPlausibleDate() {
        val cal = java.util.Calendar.getInstance()
        val expected = JalaliCalendar.fromGregorian(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
        )
        assertEquals(expected, JalaliCalendar.today())
    }

    @Test
    fun daysInMonthMatchesRealCalendarIncludingLeapEsfand() {
        // ۱۴۰۲ کبیسه نیست (اسفندش ۲۹ روزه)، ۱۴۰۳ و ۱۴۰۸ کبیسه‌ان (اسفندشون ۳۰ روزه).
        assertEquals(29, JalaliCalendar.daysInMonth(1402, 12))
        assertEquals(30, JalaliCalendar.daysInMonth(1403, 12))
        assertEquals(29, JalaliCalendar.daysInMonth(1404, 12))
        assertEquals(30, JalaliCalendar.daysInMonth(1408, 12))
        assertEquals(31, JalaliCalendar.daysInMonth(1404, 1))
        assertEquals(30, JalaliCalendar.daysInMonth(1404, 7))
    }

    @Test
    fun dayOfWeekMatchesRealCalendar() {
        // ۱۴۰۳/۰۱/۰۱ (نوروز ۱۴۰۳ = ۲۰۲۴-۰۳-۲۰) یه چهارشنبه بود - چهارشنبه تو هفته‌ی شنبه=۰ یعنی ۴.
        assertEquals(4, JalaliCalendar.dayOfWeekSaturdayFirst(PersianDate(1403, 1, 1)))
        // ۱۴۰۵/۰۴/۳۰ (۲۰۲۶-۰۷-۲۱) یه سه‌شنبه‌ست - یعنی ۳.
        assertEquals(3, JalaliCalendar.dayOfWeekSaturdayFirst(PersianDate(1405, 4, 30)))
    }
}
