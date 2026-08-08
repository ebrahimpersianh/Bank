package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BankSmsParserTest {

    @Test
    fun parsesWithdrawalSmsInRial() {
        val parsed = BankSmsParser.parse("برداشت 1,500,000 ریال از کارت منتهی به 4821")
        assertEquals(1_500_000.0, parsed?.amountRial)
        assertEquals(TransactionType.WITHDRAWAL, parsed?.type)
        assertEquals("4821", parsed?.cardSuffix)
    }

    /** مبلغِ تومانی باید به ریال نرمال بشه، وگرنه یه‌دهمِ مبلغِ واقعی از حساب کم می‌شه. */
    @Test
    fun normalizesTomanToRial() {
        assertEquals(200_000.0, BankSmsParser.parse("واریز 20000 تومان به حساب شما")?.amountRial)
    }

    @Test
    fun ignoresNonTransactionalSms() {
        assertEquals(null, BankSmsParser.parse("رمز پویا: 84512"))
    }

    /** یه سرشماره‌ی یکسان رو گوشی‌های مختلف با/بدونِ پیش‌شماره و با ارقامِ فارسی دیده می‌شه - همه
     * باید با همون چیزی که کاربر تو فرمِ حساب وارد کرده جور در بیان. */
    @Test
    fun senderMatchesAcrossFormats() {
        assertTrue(smsSenderMatches("100011111", "+98100011111"))
        assertTrue(smsSenderMatches("۱۰۰۰۱۱۱۱۱", "100011111"))
        assertTrue(smsSenderMatches("bankmellat", "BANKMELLAT"))
        assertTrue(smsSenderMatches("0210 0011", "02100011"))
    }

    @Test
    fun senderDoesNotMatchDifferentBankOrEmptyValue() {
        assertFalse(smsSenderMatches("100011111", "200022222"))
        assertFalse(smsSenderMatches(null, "100011111"))
        assertFalse(smsSenderMatches("100011111", ""))
    }
}
