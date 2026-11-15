package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** مقادیر مرجع مستقیم از اجرای calculateRateFinder/installmentForRate تو www/index.html (با node) گرفته شدن. */
class RateFinderCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.001) {
        assertTrue(abs(expected - actual) < tolerance, "expected=$expected actual=$actual")
    }

    @Test
    fun standardCase() {
        assertClose(25.453369941816227, RateFinderCalculator.findRate(2_500_000_000.0, 100_000_000.0, 36)!!)
    }

    @Test
    fun differentAmountAndMonths() {
        assertClose(26.100507448467738, RateFinderCalculator.findRate(1_000_000_000.0, 30_000_000.0, 60)!!)
    }

    @Test
    fun shortTerm() {
        assertClose(14.452148137702729, RateFinderCalculator.findRate(500_000_000.0, 45_000_000.0, 12)!!)
    }

    @Test
    fun nearZeroRate() {
        assertClose(0.0, RateFinderCalculator.findRate(2_000_000_000.0, 2_000_000_000.0 / 24, 24)!!, tolerance = 0.001)
    }

    @Test
    fun installmentTooLowReturnsNull() {
        assertNull(RateFinderCalculator.findRate(2_500_000_000.0, 1_000_000.0, 36))
    }

    @Test
    fun invalidInputsReturnNull() {
        assertNull(RateFinderCalculator.findRate(0.0, 1_000_000.0, 36))
        assertNull(RateFinderCalculator.findRate(1_000_000.0, 0.0, 36))
        assertNull(RateFinderCalculator.findRate(1_000_000.0, 1_000_000.0, 0))
    }
}
