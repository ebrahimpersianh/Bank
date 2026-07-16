package ir.sadteam.loancalc.calendar

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

    /** اولین تقویم قابل‌نوشتن (ترجیحاً primary) رو پیدا می‌کنه؛ null اگه هیچی نبود. */
    private fun findWritableCalendarId(context: Context): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}",
            null,
            null,
        )?.use { cursor ->
            var fallback: Long? = null
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val primaryIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                if (fallback == null) fallback = id
                if (primaryIdx >= 0 && cursor.getInt(primaryIdx) == 1) return id
            }
            return fallback
        }
        return null
    }

    /**
     * برای هر (شماره قسط، سررسید شمسی) یه رویداد تمام‌روز می‌سازه؛ تعداد درج‌شده رو برمی‌گردونه
     * (۰ یعنی تقویم قابل‌نوشتنی پیدا نشد). [titleFor] عنوان هر رویداد رو می‌سازه.
     */
    fun insertInstallmentEvents(
        context: Context,
        items: List<Pair<Int, PersianDate>>,
        titleFor: (Int) -> String,
    ): Int {
        val calendarId = findWritableCalendarId(context) ?: return 0
        val utc = TimeZone.getTimeZone("UTC")
        var inserted = 0
        items.forEach { (m, due) ->
            val g = JalaliCalendar.toGregorian(due)
            // رویداد تمام‌روز طبق مستندات CalendarContract باید بر حسب نیمه‌شب UTC باشه.
            val cal = Calendar.getInstance(utc).apply {
                clear()
                set(g.y, g.m - 1, g.d)
            }
            val startMillis = cal.timeInMillis
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, titleFor(m))
                put(CalendarContract.Events.DESCRIPTION, "یادآوری قسط - ساخته‌شده توسط اپ «وام من»")
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, startMillis + 24L * 60 * 60 * 1000)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) inserted++
        }
        return inserted
    }
}
