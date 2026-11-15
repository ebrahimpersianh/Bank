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

    /** برای هر رویداد تقویمی که می‌خوایم درج کنیم لازمه. [paid] اگه true باشه، عنوان رویداد با
     * خط‌خورده (strikethrough) نشون داده می‌شه - رجوع کن به [strikethrough]. */
    data class InstallmentItem(val m: Int, val due: PersianDate, val paid: Boolean)

    /**
     * قبلاً تو *همه‌ی* تقویم‌های قابل‌نوشتنِ گوشی درج می‌کرد (نه فقط یکی) - نیتِ اولیه این بود که
     * کاربر چه تقویم محلی چه یه حساب گوگل‌کلندر جدا داشته باشه، تو هردو ببینتش. ولی رو گوشی‌هایی که
     * چندین حساب/تقویمِ فرعی سینک‌شده دارن (مثلاً ۱۴ تا) این یعنی هر قسط ۱۴ بار تکراری درج می‌شه -
     * دقیقاً همون باگیه که کاربر گزارش داد («۱۲۰ قسط داشتم، ۱۶۸۰ تا اضافه شد» = ۱۲۰ × ۱۴). حالا فقط
     * تو *یه* تقویم درج می‌شه: اول ترجیح با تقویمِ محلیِ خودِ گوشی (ACCOUNT_TYPE_LOCAL، چون قطعاً فقط
     * یه نمونه‌ازش هست و به هیچ حسابی وابسته نیست)، وگرنه اولین تقویمِ قابل‌نوشتنی که کاربر خودش تو
     * اپِ تقویمش «نمایش» (visible) رو روشن نگه داشته.
     */
    private fun findTargetCalendarId(context: Context): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.VISIBLE,
        )
        var localId: Long? = null
        var firstVisibleId: Long? = null
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}",
            null,
            "${CalendarContract.Calendars._ID} ASC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val typeIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
            val visIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.VISIBLE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                if (localId == null && cursor.getString(typeIdx) == CalendarContract.ACCOUNT_TYPE_LOCAL) {
                    localId = id
                }
                if (firstVisibleId == null && cursor.getInt(visIdx) != 0) {
                    firstVisibleId = id
                }
            }
        }
        return localId ?: firstVisibleId
    }

    /** یه رشته رو با کاراکترِ ترکیبیِ یونیکد U+0336 (COMBINING LONG STROKE OVERLAY) خط‌خورده نشون
     * می‌ده - چون فیلدِ عنوانِ رویدادِ تقویم (CalendarContract.Events.TITLE) متنِ سادست و هیچ
     * فرمت‌دهیِ غنی (HTML/rich text) پشتیبانی نمی‌کنه، این تنها راهِ واقعیِ نمایشِ بصریِ «خط‌خورده»
     * تو اپ‌های تقویمه؛ رندرش بسته به فونت/اپ فرق می‌کنه ولی رو گوگل‌کلندر و بیشتر اپ‌های تقویمِ
     * اندروید درست دیده می‌شه. */
    private fun strikethrough(text: String): String = buildString {
        for (c in text) {
            append(c)
            append('̶')
        }
    }

    /**
     * برای هر [InstallmentItem] یه رویداد تمام‌روز تو *یه* تقویمِ هدف (رجوع کن به
     * [findTargetCalendarId]) می‌سازه؛ تعداد کل درج‌شده رو برمی‌گردونه (۰ یعنی هیچ تقویم
     * قابل‌نوشتنی پیدا نشد). [titleFor] عنوانِ پایه‌ی هر رویداد رو می‌سازه؛ اگه [InstallmentItem.paid]
     * true باشه، همون عنوان خط‌خورده می‌شه.
     *
     * قبلاً هر رویداد با یه `contentResolver.insert` جدا (یه رفت‌وبرگشتِ IPC سنکرون مجزا) درج
     * می‌شد - برای یه وام ۱۲۰ قسطی این یعنی ۱۲۰ تا فراخوانیِ سنکرونِ پشتِ‌سرهم، که رو خیلی گوشی‌ها
     * چندین ثانیه طول می‌کشید بدون هیچ نشونه‌ای از پیشرفت. حالا همه‌ی رویدادها با یه `applyBatch`
     * واحد درج می‌شن - هم چندبرابر سریع‌تره، هم اتمیک‌تره.
     */
    fun insertInstallmentEvents(
        context: Context,
        items: List<InstallmentItem>,
        titleFor: (Int) -> String,
    ): Int {
        val calendarId = findTargetCalendarId(context) ?: return 0
        val utc = TimeZone.getTimeZone("UTC")
        val ops = ArrayList<ContentProviderOperation>()
        items.forEach { item ->
            val g = JalaliCalendar.toGregorian(item.due)
            // رویداد تمام‌روز طبق مستندات CalendarContract باید بر حسب نیمه‌شب UTC باشه.
            val cal = Calendar.getInstance(utc).apply {
                clear()
                set(g.y, g.m - 1, g.d)
            }
            val startMillis = cal.timeInMillis
            val baseTitle = titleFor(item.m)
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, if (item.paid) strikethrough(baseTitle) else baseTitle)
                put(CalendarContract.Events.DESCRIPTION, "یادآوری قسط - ساخته‌شده توسط اپ «جیبک»")
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
        if (ops.isEmpty()) return 0
        val results = context.contentResolver.applyBatch(CalendarContract.AUTHORITY, ops)
        return results.count { it.uri != null }
    }
}
