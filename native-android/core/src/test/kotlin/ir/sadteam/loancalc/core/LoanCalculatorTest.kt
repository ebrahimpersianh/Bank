package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * مقادیر مرجع مستقیم از اجرای computeLoan تو www/index.html (با node) گرفته شدن
 * تا مطمئن بشیم پورت Kotlin دقیقاً همون عدد رو می‌ده.
 */
class LoanCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, label: String, tolerance: Double = 0.001) {
        val diff = abs(expected - actual)
        assertTrue(diff < tolerance, "$label: expected=$expected actual=$actual diff=$diff")
    }

    @Test
    fun standardLoanNoGrace() {
        val r = LoanCalculator.compute(100_000_000.0, 23.0, 12, LoanMethod.STANDARD, 0, 30)
        assertClose(9392433.26920353, r.installment, "installment")
        assertClose(12709199.230442375, r.totalInterest, "totalInterest")
        assertClose(112709199.23044237, r.totalPaid, "totalPaid")
        assertClose(100000000.0, r.principal, "principal")
        assertClose(1890410.9589041097, r.rows[0].interest, "row1.interest")
        assertClose(7502022.310299421, r.rows[0].principal, "row1.principal")
        assertClose(92497977.68970057, r.rows[0].balance, "row1.balance")
        assertClose(174261.3325020151, r.rows[11].interest, "row12.interest")
        assertClose(0.0, r.rows[11].balance, "row12.balance")
    }

    @Test
    fun standardLoanWithGrace() {
        val r = LoanCalculator.compute(100_000_000.0, 23.0, 12, LoanMethod.STANDARD, 3, 30)
        assertClose(9925100.032689866, r.installment, "installment")
        assertClose(13429967.515566066, r.totalInterest, "totalInterest")
        assertClose(119101200.39227839, r.totalPaid, "totalPaid")
        assertClose(105671232.87671232, r.principal, "principal (grows with grace)")
        assertClose(1997620.566710452, r.rows[0].interest, "row1.interest")
    }

    @Test
    fun qarzAlHasanehLoan24Months() {
        val r = LoanCalculator.compute(100_000_000.0, 4.0, 24, LoanMethod.QARZ, 0, 30)
        assertClose(4545454.545454546, r.installment, "installment")
        assertClose(6000000.0, r.totalInterest, "totalInterest (fee total)")
        assertClose(106000000.0, r.totalPaid, "totalPaid")
        // قسط اول باید فقط کارمزد باشه، اصل صفر
        assertClose(4000000.0, r.rows[0].installment, "row1.installment (fee only)")
        assertClose(4000000.0, r.rows[0].interest, "row1.interest")
        assertClose(0.0, r.rows[0].principal, "row1.principal must be zero on fee months")
        assertClose(4545454.545454546, r.rows[23].installment, "row24.installment")
        assertClose(0.0, r.rows[23].balance, "row24.balance")
    }

    @Test
    fun qarzAlHasanehLoan13Months() {
        val r = LoanCalculator.compute(250_000_000.0, 2.5, 13, LoanMethod.QARZ, 0, 30)
        assertClose(22727272.727272727, r.installment, "installment")
        assertClose(6250000.0, r.totalInterest, "totalInterest")
        assertClose(256250000.0, r.totalPaid, "totalPaid")
        assertClose(6250000.0, r.rows[0].installment, "row1.installment (fee only)")
        // قسط ۱۳ام هم کارمزدیه (چون (13-1)%12==0)
        assertClose(0.0, r.rows[12].installment, "row13.installment (fee only, balance already 0)")
    }

    @Test
    fun flatLoan() {
        val r = LoanCalculator.compute(50_000_000.0, 18.0, 10, LoanMethod.FLAT, 0, 30)
        assertClose(5412500.0, r.installment, "installment")
        assertClose(4125000.0, r.totalInterest, "totalInterest")
        assertClose(54125000.0, r.totalPaid, "totalPaid")
        assertClose(412500.0, r.rows[0].interest, "row1.interest")
        assertClose(5000000.0, r.rows[0].principal, "row1.principal")
    }

    @Test
    fun standardLoanWeeklyInterval() {
        val r = LoanCalculator.compute(20_000_000.0, 30.0, 8, LoanMethod.STANDARD, 0, 7)
        assertClose(2565159.2292773146, r.installment, "installment")
        assertClose(521273.83421851695, r.totalInterest, "totalInterest")
        assertClose(115068.49315068494, r.rows[0].interest, "row1.interest")
    }

    @Test
    fun persianDateAddDaysRollsMonthsAndYears() {
        // اسفند ۲۹ روزه‌ست (بدون تصحیح کبیسه، دقیقاً مثل نسخه‌ی JS فعلی)
        val end = PersianCalendar.addDays(PersianDate(1404, 12, 28), 3)
        assertTrue(end == PersianDate(1405, 1, 2), "expected 1405/1/2 but got $end")
    }

    @Test
    fun persianDateAddDaysHandlesNegativeOffsets() {
        // باگِ واقعیِ کشف‌شده (رجوع کن به CLAUDE.md): repeat(n)ِ Kotlin با n منفی هیچ کاری نمی‌کنه -
        // addDays(date, -1) قبل از رفعِ باگ همون تاریخِ ورودی رو بدونِ تغییر برمی‌گردوند.
        assertTrue(PersianCalendar.addDays(PersianDate(1405, 5, 17), -1) == PersianDate(1405, 5, 16))
        assertTrue(PersianCalendar.addDays(PersianDate(1405, 5, 17), -6) == PersianDate(1405, 5, 11))
        // عبور از اولِ ماه به عقب
        assertTrue(PersianCalendar.addDays(PersianDate(1405, 5, 1), -1) == PersianDate(1405, 4, 31))
        // عبور از اولِ سال به عقب
        assertTrue(PersianCalendar.addDays(PersianDate(1405, 1, 1), -1) == PersianDate(1404, 12, 29))
        // days == 0 باید بی‌اثر بمونه
        assertTrue(PersianCalendar.addDays(PersianDate(1405, 5, 17), 0) == PersianDate(1405, 5, 17))
    }

    @Test
    fun persianDateAddMonthsHandlesNegativeOffsets() {
        assertTrue(PersianCalendar.addMonths(PersianDate(1405, 5, 4), -1) == PersianDate(1405, 4, 4))
        assertTrue(PersianCalendar.addMonths(PersianDate(1405, 1, 4), -1) == PersianDate(1404, 12, 4))
    }

    @Test
    fun persianDateAddMonthsClampsDay() {
        // اگه روز مبدا (۳۱) تو ماه مقصد (۳۰ یا ۲۹ روزه) وجود نداشته باشه، باید clamp بشه
        val result = PersianCalendar.addMonths(PersianDate(1404, 6, 31), 1)
        assertTrue(result == PersianDate(1404, 7, 30), "expected 1404/7/30 but got $result")
    }

    @Test
    fun persianDateAddMonthsKeepsDayOfMonth() {
        // باگ گزارش‌شده‌ی کاربر: سررسید ماهانه باید همون روزِ ماه بمونه (۴ هر ماه)، نه اینکه با
        // ۳۰+روزِ ثابت هر ماه یه روز عقب بره (۴/۴ → ۵/۳ → ۶/۲...).
        assertTrue(PersianCalendar.addMonths(PersianDate(1404, 3, 4), 1) == PersianDate(1404, 4, 4))
        assertTrue(PersianCalendar.addMonths(PersianDate(1404, 3, 4), 4) == PersianDate(1404, 7, 4))
        assertTrue(PersianCalendar.addMonths(PersianDate(1404, 3, 4), 12) == PersianDate(1405, 3, 4))
    }

    @Test
    fun persianDateAddMonthsRespectsLeapEsfand() {
        // ۱۴۰۳ کبیسه‌ست (اسفند ۳۰ روزه) - addMonths نباید ۳۰ اسفند رو اشتباهی به ۲۹ برگردونه؛
        // ۱۴۰۴ عادیه و همون clamp به ۲۹ درسته.
        assertTrue(PersianCalendar.addMonths(PersianDate(1403, 11, 30), 1) == PersianDate(1403, 12, 30))
        assertTrue(PersianCalendar.addMonths(PersianDate(1404, 11, 30), 1) == PersianDate(1404, 12, 29))
    }

    @Test
    fun fmtMatchesJsReference() {
        assertTrue(fmt(9392433.0) == "۹,۳۹۲,۴۳۳")
        assertTrue(fmt(112709199.0) == "۱۱۲,۷۰۹,۱۹۹")
    }

    @Test
    fun numberToWordsFaMatchesJsReference() {
        assertTrue(numberToWordsFa(1.0) == "یک")
        assertTrue(numberToWordsFa(0.0) == "صفر")
        assertTrue(numberToWordsFa(250000000.0) == "دویست و پنجاه میلیون")
        assertTrue(numberToWordsFa(939243.0) == "نهصد و سی و نه هزار و دویست و چهل و سه")
        assertTrue(numberToWordsFa(21.0) == "بیست و یک")
        assertTrue(numberToWordsFa(115.0) == "صد و پانزده")
    }
}
