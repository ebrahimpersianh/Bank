package ir.sadteam.loancalc.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * تست‌های رگرسیونِ دو حفره‌ی امنیتیِ رفع‌شده‌ی مرداد ۱۴۰۵ + قواعدِ ماسک‌کردنِ لاگ.
 * رجوع کن به [RateLimit] و [Log] برای شرحِ کاملِ هر کدوم.
 */
class SecurityTest {

    @Test
    fun `rate limit blocks after the configured limit`() {
        RateLimit.reset()
        val window = 60_000L
        repeat(3) { i ->
            assertTrue(RateLimit.allow("test:ip1", limit = 3, windowMs = window), "درخواستِ ${i + 1} باید مجاز باشه")
        }
        assertFalse(RateLimit.allow("test:ip1", limit = 3, windowMs = window), "درخواستِ چهارم باید بلاک بشه")
    }

    @Test
    fun `rate limit is per key so one abuser cannot block others`() {
        RateLimit.reset()
        repeat(4) { RateLimit.allow("test:abuser", limit = 3, windowMs = 60_000L) }
        assertTrue(RateLimit.allow("test:innocent", limit = 3, windowMs = 60_000L))
    }

    @Test
    fun `rate limit window resets`() {
        RateLimit.reset()
        // پنجره‌ی صفر یعنی هر فراخوان یه پنجره‌ی تازه‌ست - معادلِ گذشتنِ کاملِ زمان.
        repeat(5) { assertTrue(RateLimit.allow("test:rolling", limit = 1, windowMs = 0L)) }
    }

    @Test
    fun `masking never leaks full phone token or ip`() {
        val phone = "09123456789"
        val masked = maskPhone(phone)
        assertFalse(masked.contains("2345"), "بخشِ میانیِ شماره نباید تو لاگ بیاد: $masked")
        assertEquals("0912***6789", masked)
        assertEquals("***", maskPhone("123"))

        val token = "abcdefghijklmnop"
        val maskedToken = maskToken(token)
        assertFalse(maskedToken.contains("ghij"), "توکن نباید کامل لاگ بشه: $maskedToken")
        assertTrue(maskedToken.startsWith("abcdef"))
        assertEquals("-", maskToken(null))

        assertEquals("37.152.187.x", maskIp("37.152.187.44"))
        assertEquals("***", maskIp("::1"))
    }
}
