package ir.sadteam.loancalc.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * تستِ فرستنده‌ی ایمیل.
 *
 * خودِ گفتگوی SMTP سرورِ واقعی می‌خواهد و در سندباکس اجراشدنی نیست؛ چیزی که این‌جا
 * تضمین می‌شود همان دو رفتاری است که اگر بشکنند بی‌صدا آسیب می‌زنند: **خاموش‌بودنِ
 * بی‌تنظیمات** (گزارشِ مشکل نباید به‌خاطرِ SMTPِ تنظیم‌نشده خطا بدهد) و **ماسک‌شدنِ
 * نشانی در لاگ**.
 */
class MailTest {

    @Test
    fun `بی تنظیماتِ SMTP ارسال بی‌صدا false می‌دهد`() {
        // هیچ `SMTP_HOST`ی در محیطِ تست نیست، پس نه استثنا و نه تلاشی برای اتصال.
        assertFalse(Mail.configured)
        assertFalse(Mail.send("a@b.com", "موضوع", "متن"))
    }

    @Test
    fun `نشانیِ ایمیل در لاگ ماسک می‌شود`() {
        assertEquals("al***@example.com", maskEmail("ali@example.com"))
        assertEquals("***", maskEmail("بی-اتساین"))
    }
}
