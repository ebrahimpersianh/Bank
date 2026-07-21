package ir.sadteam.roozegar.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import ir.sadteam.roozegar.MainActivity
import ir.sadteam.roozegar.R
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.core.Occasions
import ir.sadteam.roozegar.core.PersianNames
import ir.sadteam.roozegar.core.format
import ir.sadteam.roozegar.core.toPersianDigits
import ir.sadteam.roozegar.prefs.Prefs
import java.util.Calendar

/**
 * اعلان دائمی تاریخ - قلب «عدد بالای گوشی»: آیکون کوچیک اعلان (که تو نوار وضعیت دیده می‌شه) یه
 * بیت‌مپه که همون لحظه با عدد فارسیِ روزِ ماه رندر می‌شه. این جایگزینِ درستِ اندرویدیِ بجِ عددیِ
 * iOSئه - رندر runtime یعنی نه ۳۱ فایل آیکون لازمه نه محدودیتی رو فونت/شکل عدد داریم.
 */
object DayNotification {
    private const val CHANNEL_ID = "day_channel"
    private const val NOTIF_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        // IMPORTANCE_LOW: بی‌صدا و بدون مزاحمت - این اعلان یه «نمایشگر دائمی»ئه نه یه هشدار.
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notif_channel_desc)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    /** ساخت/به‌روزرسانی (یا حذف، اگه کاربر خاموشش کرده) اعلان طبق تنظیمات فعلی. از هر جایی که
     * تاریخ یا تنظیمات عوض می‌شه صدا زده می‌شه: بوت، نیمه‌شب، تغییر ساعت، تغییر تنظیمات، باز شدن اپ. */
    fun refresh(context: Context) {
        val settings = Prefs.snapshot(context)
        val nm = NotificationManagerCompat.from(context)
        if (!settings.notifEnabled) {
            nm.cancel(NOTIF_ID)
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)
        val today = JalaliCalendar.today()
        val title = today.format(withWeekday = true)

        val lines = buildList {
            if (!settings.notifMinimal) {
                if (settings.notifShowGregorian) add("${gregorianTodayLine()} میلادی")
                if (settings.notifShowHijri) HijriDates.todayLine()?.let { add("$it قمری") }
                if (settings.notifShowOccasion) Occasions.titlesOf(today)?.let { add("🎉 $it") }
            }
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithBitmap(renderDayIcon(context, today.d)))
            .setContentTitle(title)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (lines.isNotEmpty()) {
            builder.setContentText(lines.joinToString(" · "))
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
        }

        try {
            nm.notify(NOTIF_ID, builder.build())
        } catch (_: SecurityException) {
            // مجوز اعلان همین الان پس گرفته شده - چیزی برای انجام نیست
        }
    }

    /** رندر عدد فارسی روز به‌عنوان آیکون نوار وضعیت. نوار وضعیت آیکون رو فقط از رو کانال آلفا
     * (سیلوئت) نشون می‌ده، پس متن سفید رو پس‌زمینه‌ی شفاف دقیقاً همون چیزیه که لازمه. */
    private fun renderDayIcon(context: Context, day: Int): Bitmap {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val typeface = try {
            ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
        } catch (_: Throwable) {
            null
        } ?: Typeface.DEFAULT_BOLD
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
            textSize = if (day >= 10) 66f else 80f
        }
        val text = day.toPersianDigits()
        val y = size / 2f - (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
        canvas.drawText(text, size / 2f, y, paint)
        return bitmap
    }

    /** «۲۱ ژوئیه ۲۰۲۶» با ارقام فارسی. */
    private fun gregorianTodayLine(): String {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH).toPersianDigits()
        val m = PersianNames.gregorianMonths[cal.get(Calendar.MONTH)]
        val y = cal.get(Calendar.YEAR).toPersianDigits()
        return "$d $m $y"
    }
}
