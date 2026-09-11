package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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

    /**
     * متنِ اعلانِ بانک‌های دیجیتال فعل‌های دیگری دارد؛ با فهرستِ کلیدواژه‌ی قبلی هیچ‌کدام پارس
     * نمی‌شدند و اعلان بی‌صدا رد می‌شد - گزارشِ واقعیِ کاربر درباره‌ی بلوبانک.
     */
    @Test
    fun parsesDigitalBankWordingForBothDirections() {
        val received = BankSmsParser.parse("۲۵۰,۰۰۰ تومان دریافت کردید. موجودی: ۱,۲۰۰,۰۰۰ تومان")
        assertEquals(TransactionType.DEPOSIT, received?.type)
        assertEquals(2_500_000.0, received?.amountRial)

        val sent = BankSmsParser.parse("انتقال به سعید · ۳۲۰,۰۰۰ تومان")
        assertEquals(TransactionType.WITHDRAWAL, sent?.type)
        assertEquals(3_200_000.0, sent?.amountRial)

        assertEquals(TransactionType.WITHDRAWAL, BankSmsParser.parse("مبلغ ۹۹,۰۰۰ تومان کسر شد")?.type)
    }

    /**
     * 🚨 رگرسیونِ گزارشِ واقعی: «برداشتِ ۵۱۱٬۵۰۰ تومان - دسته: قبض» ساخته شده بود در حالی که
     * هیچ برداشتی رخ نداده بود. پیامکِ قبض هم مبلغ دارد هم فعلِ «پرداخت».
     */
    @Test
    fun ignoresBillNoticesAndAds() {
        assertNull(BankSmsParser.parse("قبض برق شما ۵۱۱,۵۰۰ تومان - مهلت پرداخت ۱۵ مهر"))
        assertNull(BankSmsParser.parse("مبلغ قابل پرداخت ۲۳۰,۰۰۰ تومان، شناسه قبض ۱۲۳۴"))
        assertNull(BankSmsParser.parse("جشنواره! با خرید ۵۰۰,۰۰۰ تومان برنده شوید"))

        // ولی گزارشِ واقعیِ بانک که مانده را هم می‌گوید باید بماند، حتی با کلمه‌ی «پرداخت».
        val real = BankSmsParser.parse("پرداخت قبض ۵۱۱,۵۰۰ ریال - مانده: ۲,۰۰۰,۰۰۰ ریال")
        assertEquals(TransactionType.WITHDRAWAL, real?.type)
    }
}