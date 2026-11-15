package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals

/** cleanNum/cleanNumDecimal باید ارقام فارسی (کیبورد فارسی) و عربی رو هم مثل انگلیسی parse کنن -
 * قبلاً یه باگ مشابه تو نسخه‌ی وب بوده (رجوع کن به CLAUDE.md)، این تست از تکرارش جلوگیری می‌کنه. */
class PersianFormatTest {

    @Test
    fun cleanNumHandlesPersianDigits() {
        assertEquals("12345", cleanNum("۱۲۳۴۵"))
    }

    @Test
    fun cleanNumHandlesArabicDigits() {
        assertEquals("12345", cleanNum("١٢٣٤٥"))
    }

    @Test
    fun cleanNumStripsNonDigits() {
        assertEquals("300000000", cleanNum("۳۰۰,۰۰۰,۰۰۰ تومان"))
    }

    @Test
    fun cleanNumDecimalKeepsDecimalPoint() {
        assertEquals("4.5", cleanNumDecimal("۴.۵"))
    }

    @Test
    fun toFaRoundTripsWithCleanNum() {
        assertEquals("42", cleanNum(toFa(42)))
    }
}
