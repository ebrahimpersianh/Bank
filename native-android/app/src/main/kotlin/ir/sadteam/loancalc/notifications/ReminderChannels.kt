package ir.sadteam.loancalc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import ir.sadteam.loancalc.R

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
        // نسخه‌ی کانال از v2 به v3 عوض شد - چون تنظیماتِ صدا/ویبره‌ی یه کانال فقط موقعِ *ساختِ اولش*
        // گرفته می‌شه (بعداً از کد قابل‌تغییر نیست)، اضافه‌کردنِ vibrationPattern به کدِ [ensure] رو
        // کانال‌های v2ِ ازقبل‌ساخته‌شده رو گوشیِ کاربرها هیچ اثری نداشت - این بامپ مجبورشون می‌کنه
        // یه کانالِ کاملاً تازه (با تنظیماتِ جدید) بسازن.
        return "due_date_reminders_v3_${soundPart}_${if (vibrate) 1 else 0}"
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
                    // بعضی گوشی‌ها (خصوصاً MIUI/شیائومی) با enableVibration تنها بدونِ یه الگوی
                    // صریح، ویبره رو واقعاً فعال نمی‌کنن - این یه الگوی صریح و ساده می‌ده تا مطمئن‌تر
                    // باشه.
                    if (vibrate) vibrationPattern = longArrayOf(0, 300, 200, 300)
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

    // آیکونِ کوچیکِ نوارِ وضعیت (setSmallIcon) طبقِ قانونِ خودِ اندروید همیشه فقط سیلوئتِ تک‌رنگه
    // (سیستم رنگش می‌کنه، لوگوی رنگی روش اثر نداره) - این تغییرناپذیره، بگ نیست. ولی «آیکونِ بزرگ»
    // (setLargeIcon تو خودِ نوتیف، رجوع کن به DueDateReminderWorker/ReminderSettingsScreen) می‌تونه
    // رنگی باشه. به‌جای کراپِ لوگوی کاملِ اپ (که سکه هم توش بود)، از ic_notification_large_color.xml
    // استفاده می‌شه - دقیقاً هم‌شکلِ آیکونِ کوچیکِ سیاه‌سفید (همون بدنه/صفحه‌نمایش/شبکه‌ی دکمه‌ها)، فقط
    // رنگی و بدونِ سکه - خواسته‌ی صریحِ کاربر بعدِ چند دورِ پیش‌نمایش، تا بالا/پایینِ نوتیف یه‌دست بشن.
    fun largeIcon(context: Context): Bitmap? = runCatching {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_large_color)
            ?: return@runCatching null
        val size = 192
        val square = drawable.toBitmap(width = size, height = size, config = Bitmap.Config.ARGB_8888)
        circleCrop(square)
    }.getOrNull()

    private fun circleCrop(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return output
    }
}
