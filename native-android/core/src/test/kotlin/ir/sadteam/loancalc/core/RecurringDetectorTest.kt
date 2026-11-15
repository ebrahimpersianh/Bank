package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecurringDetectorTest {
    private fun tx(desc: String, amount: Double, y: Int, m: Int, d: Int = 5) =
        RecurringInput(desc, amount, y, m, d)

    @Test
    fun detects_monthly_subscription_across_three_months() {
        val found = RecurringDetector.detect(
            listOf(
                tx("خرید اینترنتی فیلیمو ۱۲۳۴", 1_200_000.0, 1405, 4),
                tx("خرید اینترنتی فیلیمو ۹۹۸۸", 1_200_000.0, 1405, 5),
                tx("خرید اینترنتی فیلیمو ۵۵۵", 1_250_000.0, 1405, 6),
            ),
        )
        assertEquals(1, found.size)
        assertEquals("فیلیمو", found[0].label)
        assertEquals(3, found[0].monthsSeen)
    }

    @Test
    fun ignores_expense_seen_in_only_two_months() {
        val found = RecurringDetector.detect(
            listOf(
                tx("بیمه ایران", 3_000_000.0, 1405, 5),
                tx("بیمه ایران", 3_000_000.0, 1405, 6),
            ),
        )
        assertTrue(found.isEmpty())
    }

    @Test
    fun ignores_wildly_varying_amounts() {
        // خریدِ روزمره‌ی سوپرمارکت: هر ماه هست ولی مبلغش ثابت نیست.
        val found = RecurringDetector.detect(
            listOf(
                tx("سوپرمارکت", 500_000.0, 1405, 4),
                tx("سوپرمارکت", 4_000_000.0, 1405, 5),
                tx("سوپرمارکت", 12_000_000.0, 1405, 6),
            ),
        )
        assertTrue(found.isEmpty())
    }

    @Test
    fun two_purchases_in_one_month_is_not_recurring() {
        val found = RecurringDetector.detect(
            listOf(
                tx("قهوه", 900_000.0, 1405, 6, d = 2),
                tx("قهوه", 900_000.0, 1405, 6, d = 9),
                tx("قهوه", 900_000.0, 1405, 6, d = 20),
            ),
        )
        assertTrue(found.isEmpty())
    }

    @Test
    fun monthlyTotal_sums_typical_amounts() {
        val found = RecurringDetector.detect(
            listOf(
                tx("فیلیمو", 1_200_000.0, 1405, 4), tx("فیلیمو", 1_200_000.0, 1405, 5), tx("فیلیمو", 1_200_000.0, 1405, 6),
                tx("ایرانسل", 2_000_000.0, 1405, 4), tx("ایرانسل", 2_000_000.0, 1405, 5), tx("ایرانسل", 2_000_000.0, 1405, 6),
            ),
        )
        assertEquals(2, found.size)
        assertEquals(3_200_000.0, RecurringDetector.monthlyTotal(found), 0.01)
        // پرخرج‌ترین اول
        assertEquals("ایرانسل", found[0].label)
    }

    @Test
    fun upcomingThisMonth_only_returns_not_yet_charged() {
        val found = RecurringDetector.detect(
            listOf(
                tx("فیلیمو", 1_200_000.0, 1405, 3), tx("فیلیمو", 1_200_000.0, 1405, 4), tx("فیلیمو", 1_200_000.0, 1405, 5),
                tx("ایرانسل", 2_000_000.0, 1405, 4), tx("ایرانسل", 2_000_000.0, 1405, 5), tx("ایرانسل", 2_000_000.0, 1405, 6),
            ),
        )
        val upcoming = RecurringDetector.upcomingThisMonth(found, currentYear = 1405, currentMonth = 6)
        assertEquals(listOf("فیلیمو"), upcoming.map { it.label })
    }

    @Test
    fun normalizeLabel_strips_digits_and_noise() {
        assertEquals("ایرانسل", RecurringDetector.normalizeLabel("خرید اینترنتی ایرانسل ۱۲۳۴۵"))
        assertEquals("اسنپ", RecurringDetector.normalizeLabel("پرداخت اسنپ - شماره پیگیری 998877"))
    }
}

class MonthForecastTest {
    @Test
    fun no_forecast_before_seventh_day() {
        assertNull(MonthForecast.compute(spentSoFarRial = 5_000_000.0, dayOfMonth = 3, daysInMonth = 31, balanceRial = 1e7))
    }

    @Test
    fun projects_remaining_from_daily_rate() {
        // ۱۰ روز گذشته، ۱۰ میلیون خرج → روزی ۱ میلیون؛ ۲۱ روزِ مونده → ۲۱ میلیون
        val f = MonthForecast.compute(
            spentSoFarRial = 10_000_000.0,
            dayOfMonth = 10,
            daysInMonth = 31,
            balanceRial = 30_000_000.0,
        )
        assertNotNull(f)
        assertEquals(21, f.daysLeft)
        assertEquals(21_000_000.0, f.projectedRemainingRial, 0.01)
        assertEquals(31_000_000.0, f.projectedTotalRial, 0.01)
        assertTrue(!f.willRunShort) // ۳۰ میلیون موجودی در برابرِ ۲۱ میلیونِ باقیمانده
    }

    @Test
    fun flags_shortfall_when_balance_is_not_enough() {
        val f = MonthForecast.compute(
            spentSoFarRial = 10_000_000.0,
            dayOfMonth = 10,
            daysInMonth = 31,
            balanceRial = 5_000_000.0,
            committedRial = 4_000_000.0, // قسطِ ماه
        )
        assertNotNull(f)
        assertEquals(25_000_000.0, f.projectedRemainingRial, 0.01)
        assertTrue(f.willRunShort)
        assertEquals(20_000_000.0, f.shortfallRial, 0.01)
    }
}
