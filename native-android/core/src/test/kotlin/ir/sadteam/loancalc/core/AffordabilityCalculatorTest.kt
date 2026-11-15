package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/** مقادیر مرجع مستقیم از اجرای calculateAffordability تو www/index.html (با node) گرفته شدن. */
class AffordabilityCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.001) {
        assertTrue(abs(expected - actual) < tolerance, "expected=$expected actual=$actual")
    }

    @Test
    fun standardRate() {
        assertClose(129166519.39144765, AffordabilityCalculator.computeMaxPrincipal(5_000_000.0, 23.0, 36))
    }

    @Test
    fun differentRateAndMonths() {
        assertClose(393802688.8534286, AffordabilityCalculator.computeMaxPrincipal(10_000_000.0, 18.0, 60))
    }

    @Test
    fun zeroRate() {
        assertClose(48_000_000.0, AffordabilityCalculator.computeMaxPrincipal(2_000_000.0, 0.0, 24))
    }
}
