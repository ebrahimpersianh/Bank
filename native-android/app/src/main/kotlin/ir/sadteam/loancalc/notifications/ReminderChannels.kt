package ir.sadteam.loancalc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import ir.sadteam.loancalc.R

/**
 * سه کانالِ **ثابت** برای سه نوعِ اعلان. جایگزینِ کانالِ پویا (یک کانال به‌ازای هر ترکیبِ
 * صدا/ویبره) - تصمیمِ ثبت‌شده‌ی این دور.
 *
 * ### چرا کانالِ پویا رفت
 * کانالِ پویا کار می‌کرد، ولی هزینه‌اش این بود که هر بار کاربر صدا یا ویبره را عوض می‌کرد یک
 * کانالِ تازه در سیستم ساخته می‌شد و قدیمی‌ها **پاک نمی‌شدند**. نتیجه: فهرستِ رشدکننده‌ای از
 * ردیف‌های هم‌نامِ «یادآوری سررسید» در تنظیماتِ اعلانِ اندروید که کاربر نمی‌داند کدام فعال است -
 * و اگر ردیفِ اشتباه را خاموش کند، هیچ اتفاقی نمی‌افتد. سه کانالِ ثابت این را می‌بندد و در عوض
 * چیزی می‌دهد که پویا هیچ‌وقت نداشت: کاربر می‌تواند یادآورِ سررسید را نگه دارد و اعلانِ
 * انگیزشی را جدا خاموش کند.
 *
 * ### ⚠️ نتیجه‌ای که باید بدانید
 * اندروید صدا/ویبره‌ی کانال را فقط لحظه‌ی **ساختِ اول** می‌گیرد و بعد از کد تغییرناپذیر است.
 * با کانالِ ثابت، انتخابگرِ صدا و کلیدِ ویبره‌ی **داخلِ برنامه دیگر کاری نمی‌کنند** - پس باید از
 * صفحه‌ی تنظیماتِ یادآور برداشته شوند و جایشان یک ردیفِ «صدا و ویبره» بنشیند که
 * [openChannelSettings] را صدا می‌زند. نگه‌داشتنِ آن دو کنترل بدترین حالت است: کنترلی که
 * می‌چرخد و اثر نمی‌کند.
 *
 * سه کلیدِ *روشن/خاموش*ِ خودِ برنامه سرِ جایشان می‌مانند - آن‌ها فرستادن یا نفرستادن را کنترل
 * می‌کنند، نه صدای کانال را.
 */
object ReminderChannels {

    /** یادآورِ سررسید: قسط، چک، پرداختِ تکراری. */
    const val CHANNEL_DUE_DATES = "jibak_due_dates"

    /** تراکنشی که خودکار از پیامک یا اعلانِ بانکی ثبت شده. */
    const val CHANNEL_AUTO_TX = "jibak_auto_transactions"

    /** انگیزشی: «دخل‌وخرج امروز یادت نره»، «برگشت». */
    const val CHANNEL_NUDGES = "jibak_nudges"

    /**
     * هر سه کانال را یک‌بار می‌سازد. جای صدا زدنش [BootReceiver] و اولین اجرای برنامه است، نه
     * لحظه‌ی فرستادنِ اعلان - ساختنِ کانال ارزان است ولی تکرارش در هر اعلان بی‌دلیل.
     *
     * اهمیت‌ها عمداً فرق دارند: سررسید پول است پس `HIGH`؛ تراکنشِ خودکار خبرِ کارِ
     * انجام‌شده است پس `DEFAULT` بی مزاحمت؛ انگیزشی `LOW` تا صدا نکند.
     */
    fun ensureAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DUE_DATES,
                "یادآورِ سررسید",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "قسط، چک و پرداختِ تکراری که سررسیدشان نزدیک است"
                enableVibration(true)
                // بعضی گوشی‌ها (خصوصاً MIUI) با enableVibration تنها و بی الگوی صریح، ویبره را
                // واقعاً فعال نمی‌کنند.
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            },
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_AUTO_TX,
                "تراکنشِ خودکار",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "تراکنشی که خودکار از پیامک یا اعلانِ بانک ثبت شد"
                enableVibration(false)
            },
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NUDGES,
                "یادآورِ روزانه",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "یادآوریِ ثبتِ دخل‌وخرج و پیامِ بازگشت"
                enableVibration(false)
            },
        )

        deleteLegacyChannels(manager)
    }

    /**
     * کانال‌های پویای نسخه‌های قبل را پاک می‌کند، وگرنه همان فهرستِ متورمی که این تغییر برای
     * رفعش آمد روی گوشیِ کاربرانِ فعلی باقی می‌ماند.
     *
     * شناسه‌ها الگوی `due_date_reminders_v3_<hash>_<0|1>` داشتند و hash از رشته‌ی صدا
     * می‌آمد، پس نمی‌شود بازسازی‌شان کرد - به‌جایش هر کانالی که با پیشوندِ قدیمی شروع شود
     * حذف می‌شود.
     */
    private fun deleteLegacyChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.notificationChannels
            .filter { it.id.startsWith("due_date_reminders_v") }
            .forEach { manager.deleteNotificationChannel(it.id) }
    }

    /**
     * صفحه‌ی تنظیماتِ سیستمیِ همین کانال را باز می‌کند - تنها جایی که صدا و ویبره از این به
     * بعد عوض می‌شوند. جایگزینِ انتخابگرِ صدای داخلِ برنامه.
     */
    fun openChannelSettings(context: Context, channelId: String = CHANNEL_DUE_DATES) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startActivity(
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        } else {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        }
    }

    /**
     * آیکونِ بزرگِ رنگیِ داخلِ اعلان. آیکونِ کوچکِ نوارِ وضعیت طبقِ قانونِ اندروید همیشه
     * تک‌رنگ است (سیستم رنگش می‌کند) - این تغییرناپذیر است، باگ نیست.
     *
     * `ic_notification_large_color` هم‌هندسه‌ی آیکونِ کوچک است و **سکه ندارد**: در ۲۴dp
     * سکه روی بدنه فقط با رنگ جدا می‌شد و در تک‌رنگ سیلوئتِ آدم می‌داد. جزئیاتش در کامنتِ
     * خودِ دو XML.
     */
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
