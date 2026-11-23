package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * رگرسیونِ باگِ ۲۳ نوامبر ۲۰۲۶ (حذفِ وام با «پاک شود» یک تراکنشِ نامربوط را هم پاک
 * کرد): تجزیه‌ی شناسه‌ی پیوندِ پرداختِ وام باید دقیق باشد — تراکنشِ طلب/بدهی یا وامِ
 * دیگر هیچ‌وقت نباید با شناسه‌ی این وام جور شود، حتی اگر رقم‌هایش پیشوندِ مشترکی
 * داشته باشند.
 */
class LoanLinkedPaymentTest {

    @Test
    fun singleInstallmentParses() {
        assertEquals(1764028123456L to listOf(1), parseLoanPaymentSourceId("1764028123456:1"))
    }

    @Test
    fun groupPaymentParsesAllMonths() {
        assertEquals(
            1764028123456L to listOf(1, 2, 3),
            parseLoanPaymentSourceId("1764028123456:1,2,3"),
        )
    }

    @Test
    fun debtReceiptDoesNotParse() {
        // رسیدِ طلب/بدهی: sourceId فقط شناسه‌ی بدهی است، بدونِ «:» — نباید با وام جور شود.
        assertNull(parseLoanPaymentSourceId("1764028123456"))
    }

    @Test
    fun otherLoanIdIsDistinct() {
        // شناسه‌ی وامِ دیگر، حتی با پیشوندِ رقمیِ مشترک، وامِ دیگری است.
        val (loanId, _) = parseLoanPaymentSourceId("17640281234567:1")!!
        assertEquals(17640281234567L, loanId)
    }

    @Test
    fun malformedDoesNotParse() {
        assertNull(parseLoanPaymentSourceId(null))
        assertNull(parseLoanPaymentSourceId(""))
        assertNull(parseLoanPaymentSourceId("  "))
        assertNull(parseLoanPaymentSourceId("loan:1"))
        assertNull(parseLoanPaymentSourceId("1764028123456:"))
        assertNull(parseLoanPaymentSourceId("1764028123456:1,"))
        assertNull(parseLoanPaymentSourceId("1764028123456:1,abc"))
        assertNull(parseLoanPaymentSourceId(":1"))
        assertNull(parseLoanPaymentSourceId("1764028123456:1:2"))
        // شناسه‌ی دنگ/دارایی قالبِ وام نیست.
        assertNull(parseLoanPaymentSourceId("dang-123@456"))
        assertNull(parseLoanPaymentSourceId("GOLD_18:1"))
    }
}
