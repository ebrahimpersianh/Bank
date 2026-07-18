package ir.sadteam.loancalc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build

/**
 * اندروید صدا/ویبره‌ی یه کانالِ نوتیف رو فقط موقعِ *ساختِ اولش* می‌گیره - بعداً از کد قابل‌تغییر
 * نیست (فقط خودِ کاربر از تنظیماتِ سیستم می‌تونه دستی عوضش کنه). برای این‌که «صدای دلخواه/ویبره‌ی
 * دلخواه» تو تنظیماتِ یادآوری واقعاً اثر کنه، به‌جای یه کانالِ ثابت، به‌ازای هر ترکیبِ (صدا، ویبره)
 * یه کانالِ جدا می‌سازیم؛ هر بار کاربر تنظیمات رو عوض کنه، دفعه‌ی بعد یه شناسه‌ی تازه محاسبه و
 * (در صورتِ نبود) ساخته می‌شه - کانال‌های قدیمی بی‌ضرر تو سیستم می‌مونن، فقط دیگه استفاده نمی‌شن.
 */
object ReminderChannels {
    private fun channelId(soundUri: String?, vibrate: Boolean): String {
        val soundPart = soundUri?.hashCode() ?: 0
        return "due_date_reminders_v2_${soundPart}_${if (vibrate) 1 else 0}"
    }

    /** کانالِ متناظرِ این ترکیبِ (صدا، ویبره) رو اگه لازم بود می‌سازه و شناسه‌ش رو برمی‌گردونه. */
    fun ensure(context: Context, soundUri: String?, vibrate: Boolean): String {
        val id = channelId(soundUri, vibrate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(id, "یادآوری سررسید", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "یادآوری برای اقساط/چک‌هایی که سررسیدشون نزدیکه"
                    enableVibration(vibrate)
                    if (soundUri != null) {
                        val attrs = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        setSound(Uri.parse(soundUri), attrs)
                    }
                }
                manager?.createNotificationChannel(channel)
            }
        }
        return id
    }
}
