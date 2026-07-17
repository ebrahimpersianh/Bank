package ir.sadteam.loancalc.calendar

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import java.util.Calendar
import java.util.TimeZone

/**
 * درج سررسید اقساط به‌عنوان رویدادِ تمام‌روز تو تقویم خودِ گوشی (Google Calendar/تقویم سیستم) -
 * خواسته‌ی کاربر، که خودش قبلاً دستی همچین رویدادهایی می‌ساخت («[وام] قسط امداد خودرو (۱۲,۷۹۴,۰۰۰
 * ریال) - ۲۵ تیر ۱۴۰۵»). مستقیم با CalendarContract می‌نویسه (نیاز به مجوز READ/WRITE_CALENDAR که
 * سمت UI درخواست می‌شه)، نه Intent تک‌رویدادی - چون یه وام می‌تونه ۱۲۰ قسط داشته باشه.
 */
object DeviceCalendarExporter {

    /**
     * همه‌ی تقویم‌های قابل‌نوشتنِ رو گوشی (نه فقط اولی/primary) - چون کاربر ممکنه هم یه تقویمِ
     * محلیِ گوشی داشته باشه هم یه حساب گوگل‌کلندر جدا، و اگه فقط تو یکی درج بشه، تو اپِ تقویمِ
     * دیگه («هم تو تقویم خود گوشی هم تو تقویم گوگل» - خواسته‌ی صریح کاربر) دیده نمی‌شه.
     */
    private fun findWritableCalendarIds(context: Context): List<Long> {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val ids = mutableListOf<Long>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}",
            null,
            null,
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            while (cursor.moveToNext()) ids.add(cursor.getLong(idIdx))
        }
        return ids
    }

    /**
     * برای هر (شماره قسط، سررسید شمسی) یه رویداد تمام‌روز، رو *همه‌ی* تقویم‌های قابل‌نوشتنِ گوشی
     * (هم تقویم محلی، هم هر حساب گوگل‌کلندرِ سینک‌شده) می‌سازه؛ تعداد کل درج‌شده رو برمی‌گردونه
     * (۰ یعنی هیچ تقویم قابل‌نوشتنی پیدا نشد). [titleFor] عنوان هر رویداد رو می‌سازه.
     *
     * قبلاً هر رویداد با یه `contentResolver.insert` جدا (یه رفت‌وبرگشتِ IPC سنکرون مجزا) درج
     * می‌شد - برای یه وام ۱۲۰ قسطی رو ۲ تا تقویم یعنی ۲۴۰ تا فراخوانیِ سنکرونِ پشتِ‌سرهم، که رو
     * خیلی گوشی‌ها چندین ثانیه طول می‌کشید بدون هیچ نشونه‌ای از پیشرفت - دقیقاً همون چیزی که کاربر
     * «هیچ اتفاقی نمی‌افته» توصیف کرد (فقط صبر نکرده بود، نه اینکه واقعاً خراب بود). حالا همه‌ی
     * رویدادها با یه `applyBatch` واحد درج می‌شن - هم چندبرابر سریع‌تره، هم اتمیک‌تره.
     */
    fun insertInstallmentEvents(
        context: Context,
        items: List<Pair<Int, PersianDate>>,
        titleFor: (Int) -> String,
    ): Int {
        val calendarIds = findWritableCalendarIds(context)
        if (calendarIds.isEmpty()) return 0
        val utc = TimeZone.getTimeZone("UTC")
        val ops = ArrayList<ContentProviderOperation>()
        items.forEach { (m, due) ->
            val g = JalaliCalendar.toGregorian(due)
            // رویداد تمام‌روز طبق مستندات CalendarContract باید بر حسب نیمه‌شب UTC باشه.
            val cal = Calendar.getInstance(utc).apply {
                clear()
                set(g.y, g.m - 1, g.d)
            }
            val startMillis = cal.timeInMillis
            calendarIds.forEach { calendarId ->
                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, titleFor(m))
                    put(CalendarContract.Events.DESCRIPTION, "یادآوری قسط - ساخته‌شده توسط اپ «وام من»")
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, startMillis + 24L * 60 * 60 * 1000)
                    put(CalendarContract.Events.ALL_DAY, 1)
                    put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                }
                ops.add(
                    ContentProviderOperation.newInsert(CalendarContract.Events.CONTENT_URI)
                        .withValues(values)
                        .build(),
                )
            }
        }
        if (ops.isEmpty()) return 0
        val results = context.contentResolver.applyBatch(CalendarContract.AUTHORITY, ops)
        return results.count { it.uri != null }
    }
}
