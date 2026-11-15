package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoanCalculatorRegressionTest {

    // فرمول مرجع مستقل (استاندارد قسط مساوی نزولی)، جدا از پیاده‌سازی خودِ LoanCalculator، برای
    // اطمینان از اینکه تبدیل به BigDecimal هیچ رفتار قابل‌مشاهده‌ای رو (بعد از گردکردن به ریال) عوض
    // نکرده.
    private fun referenceStandardInstallment(principal: Double, i: Double, n: Int): Double =
        if (i == 0.0) principal / n else principal * i * (1 + i).pow(n) / ((1 + i).pow(n) - 1)

    @Test
    fun `standard installment matches independent reference formula within 1 rial after rounding`() {
        val random = Random(42)
        var checked = 0
        repeat(200_000) {
            val principal = 1_000_000.0 + random.nextDouble() * 20_000_000_000.0
            val annualRatePct = 1.0 + random.nextDouble() * 35.0
            val n = 1 + random.nextInt(360)
            val intervalDays = 30
            val i = annualRatePct / 100.0 * (intervalDays / 365.0)

            val result = LoanCalculator.compute(principal, annualRatePct, n, LoanMethod.STANDARD, intervalDays = intervalDays)
            val expected = referenceStandardInstallment(principal, i, n)

            val diff = abs(Math.round(result.installment) - Math.round(expected))
            assertTrue(diff <= 1, "principal=$principal rate=$annualRatePct n=$n -> got ${result.installment} expected $expected (rounded diff=$diff)")
            checked++
        }
        assertEquals(200_000, checked)
    }

    @Test
    fun `known textbook example - 100M rial, 18 percent, 12 months`() {
        val result = LoanCalculator.compute(100_000_000.0, 18.0, 12, LoanMethod.STANDARD, intervalDays = 30)
        // فرمول مرجع دستی (بدون هیچ کد اپ)
        val i = 18.0 / 100.0 * (30.0 / 365.0)
        val expected = 100_000_000.0 * i * (1 + i).pow(12) / ((1 + i).pow(12) - 1)
        assertTrue(abs(result.installment - expected) < 1.0, "installment=${result.installment} expected=$expected")
        // جمع کل اقساط باید تقریبا اصل + سود باشه
        assertTrue(abs(result.totalPaid - result.installment * 12) < 0.001)
    }

    @Test
    fun `zero interest rate falls back to simple division`() {
        val result = LoanCalculator.compute(120_000_000.0, 0.0, 12, LoanMethod.STANDARD)
        assertEquals(10_000_000.0, result.installment, 0.0001)
    }

    @Test
    fun `flat method matches manual formula`() {
        val principal = 50_000_000.0
        val rate = 20.0
        val n = 10
        val result = LoanCalculator.compute(principal, rate, n, LoanMethod.FLAT)
        val expectedTotalInterest = principal * rate * (n + 1) / 2400.0
        val expectedInstallment = (principal + expectedTotalInterest) / n
        assertTrue(abs(result.totalInterest - expectedTotalInterest) < 1.0)
        assertTrue(abs(result.installment - expectedInstallment) < 1.0)
    }

    @Test
    fun `qarz method - principal fully repaid by non-fee installments`() {
        val principal = 60_000_000.0
        val n = 24
        val result = LoanCalculator.compute(principal, 4.0, n, LoanMethod.QARZ)
        val principalRows = result.rows.filter { it.principal > 0.0 }
        val sumPrincipal = principalRows.sumOf { it.principal }
        assertTrue(abs(sumPrincipal - principal) < 1.0, "sumPrincipal=$sumPrincipal expected=$principal")
    }

    @Test
    fun `deposit calculator matches manual simple-interest formula`() {
        val principal = 200_000_000.0
        val rate = 22.0
        val months = 6
        val result = DepositCalculator.compute(principal, rate, months)
        val expectedDaily = principal * (rate / 100.0) / 365.0
        assertTrue(abs(result.dailyInterest - expectedDaily) < 0.01)
        assertTrue(abs(result.finalAmount - (principal + result.totalInterest)) < 0.0001)
    }

    @Test
    fun `affordability calculator matches manual inverse-annuity formula`() {
        val monthlyPayment = 15_000_000.0
        val rate = 21.0
        val months = 36
        val result = AffordabilityCalculator.computeMaxPrincipal(monthlyPayment, rate, months)
        val i = rate / 1200.0
        val expected = monthlyPayment * (1 - (1 + i).pow(-months)) / i
        assertTrue(abs(result - expected) < 1.0, "got=$result expected=$expected")
    }

    @Test
    fun `affordability zero rate is simple multiplication`() {
        val result = AffordabilityCalculator.computeMaxPrincipal(5_000_000.0, 0.0, 24)
        assertEquals(120_000_000.0, result, 0.0001)
    }
}
