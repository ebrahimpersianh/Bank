package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/** مقادیر مرجع مستقیم از اجرای calculateDeposit تو www/index.html (با node) گرفته شدن. */
class DepositCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.001) {
        assertTrue(abs(expected - actual) < tolerance, "expected=$expected actual=$actual")
    }

    /** باگِ رفع‌شده: مقادیرِ قبلیِ این تست («مرجعِ www/index.html») از رو یه سالِ ۳۶۰روزه (۳۰×۱۲)
     * محاسبه شده بودن - یعنی خودِ سایتِ قدیمی هم همین باگ رو داشت (نتیجه حدودِ ۱.۴٪ کمتر از سودِ
     * واقعیِ سالانه). با گزارشِ کاربر (مقایسه با یه سایتِ معتبر) تایید شد و به ۳۶۵ روزِ واقعی برای
     * سالِ کامل اصلاح شد - سودِ سالانه‌ی ۱۸٪ روی ۱۰۰ میلیون باید دقیقاً ۱۸ میلیون بشه، نه کمتر. */
    @Test
    fun oneYear18Percent() {
        val r = DepositCalculator.compute(100_000_000.0, 18.0, 12)
        assertClose(49_315.06849315069, r.dailyInterest)
        assertClose(1_479_452.0547945206, r.monthlyInterest)
        assertClose(18_000_000.0, r.totalInterest)
        assertClose(118_000_000.0, r.finalAmount)
    }

    @Test
    fun zeroRate() {
        val r = DepositCalculator.compute(50_000_000.0, 0.0, 6)
        assertClose(0.0, r.dailyInterest)
        assertClose(0.0, r.totalInterest)
        assertClose(50_000_000.0, r.finalAmount)
    }
}
