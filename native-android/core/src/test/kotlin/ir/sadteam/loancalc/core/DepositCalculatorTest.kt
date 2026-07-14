package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/** مقادیر مرجع مستقیم از اجرای calculateDeposit تو www/index.html (با node) گرفته شدن. */
class DepositCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.001) {
        assertTrue(abs(expected - actual) < tolerance, "expected=$expected actual=$actual")
    }

    @Test
    fun oneYear18Percent() {
        val r = DepositCalculator.compute(100_000_000.0, 18.0, 12)
        assertClose(49_315.06849315069, r.dailyInterest)
        assertClose(1_479_452.0547945206, r.monthlyInterest)
        assertClose(17_753_424.657534247, r.totalInterest)
        assertClose(117_753_424.65753424, r.finalAmount)
    }

    @Test
    fun zeroRate() {
        val r = DepositCalculator.compute(50_000_000.0, 0.0, 6)
        assertClose(0.0, r.dailyInterest)
        assertClose(0.0, r.totalInterest)
        assertClose(50_000_000.0, r.finalAmount)
    }
}
