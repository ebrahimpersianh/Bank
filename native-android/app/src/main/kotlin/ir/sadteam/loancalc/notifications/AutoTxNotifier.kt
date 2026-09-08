package ir.sadteam.loancalc.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.rialToToman

/**
 * اعلانِ گوشیِ تراکنشِ خودکار — فریمِ `50b`.
 *
 * ### چرا فایلِ تازه
 * این اعلان **در برنامه وجود نداشت**. `BankNotificationListener` و `BankSmsReceiver` فقط
 * ردیفِ صندوقِ پیام می‌ساختند و کامنتِ خودشان می‌گفت «اعلانِ گوشیِ خودمون (اگه بعداً اضافه
 * بشه) باید از رو همین ردیف ساخته بشه». یعنی تراکنشی که خودکار ثبت می‌شد **هیچ خبری
 * نمی‌داد** تا کاربر خودش برنامه را باز کند. این فایل همان اضافه‌شدنِ وعده‌داده‌شده است، و
 * عمداً یک نقطه‌ی مشترک برای هر دو مسیر است (پیامک و اعلانِ بانکی) تا دو صورتِ متفاوت پیدا
 * نکنند.
 *
 * ### دو کنش
 * - **تایید** — تراکنش را `confirmed = true` می‌کند. تا قبلِ تایید روی موجودی اثر ندارد
 *   (تصمیمِ ثبت‌شده)، پس این دکمه کارِ واقعی می‌کند نه فقط بستنِ اعلان.
 * - **دسته‌اش این نیست** / **دسته‌اش این است** — برنامه را روی شیتِ دسته‌ی همان تراکنش باز
 *   می‌کند. اندروید انتخابگر داخلِ اعلان ندارد، پس دیپ‌لینک تنها راه است.
 *
 * برچسبِ دکمه‌ی دوم به `confident` بستگی دارد و این عمدی است — قاعده‌ی `50d`: وقتی حدسی
 * هست، متنِ اعلان از قبل گفته‌اش، پس دکمه باید کارِ **رد کردن** را بگوید («این نیست»)؛
 * وگرنه دو چیزِ یکسان دو بار گفته می‌شود و تفاوتش با «تایید» گم می‌شود. وقتی حدسی نیست،
 * تاییدی هم معنا ندارد: یک دکمه‌ی «دسته‌اش این است» می‌ماند.
 */
object AutoTxNotifier {

    /**
     * @param txId شناسه‌ی تراکنشِ تاییدنشده. هم شناسه‌ی اعلان از آن ساخته می‌شود و هم
     *   دیپ‌لینکِ شیتِ دسته — پس دو اعلان برای یک تراکنش ممکن نیست.
     * @param confident آیا دسته حدس زده شد یا کور افتاد. برچسبِ دکمه‌ی دوم و متن از این
     *   می‌آید.
     * @param privacyMode مبلغ را برمی‌دارد. همان کلیدی که یادآورِ سررسید می‌خواند.
     */
    fun notify(
        context: Context,
        txId: Long,
        amountRial: Double,
        accountName: String,
        category: String,
        isWithdrawal: Boolean,
        confident: Boolean,
        privacyMode: Boolean,
    ) {
        ReminderChannels.ensureAll(context)
        val notificationId = "autotx_$txId".hashCode()
        // واحد **تومان** و رقمِ فارسی. ستونِ amount ریال است پس تبدیل همین لبه.
        val amount = fmt(rialToToman(amountRial.toLong()).toDouble()).faDigits()
        val verb = if (isWithdrawal) "خرج شد" else "به حسابت اومد"

        val builder = NotificationCompat.Builder(context, ReminderChannels.CHANNEL_AUTO_TX)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(context))
            .setContentTitle(
                if (privacyMode) {
                    if (isWithdrawal) "برداشتِ تازه" else "واریزِ تازه"
                } else {
                    "$amount تومان $verb"
                },
            )
            .setContentText(
                if (confident) {
                    "از $accountName · حدس می‌زنم «$category» باشد"
                } else {
                    "از $accountName · دسته‌اش را نمی‌دانم"
                },
            )
            .setContentIntent(openTransactionIntent(context, notificationId, txId))
            .setGroup(GROUP_AUTO_TX)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (confident) {
            builder.addAction(confirmAction(context, notificationId, txId))
            builder.addAction(pickCategoryAction(context, notificationId, txId, "دسته‌اش این نیست"))
        } else {
            // بی حدس، «تایید» چیزی برای تایید کردن ندارد.
            builder.addAction(pickCategoryAction(context, notificationId, txId, "دسته‌اش این است"))
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun confirmAction(context: Context, notificationId: Int, txId: Long): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CONFIRM_TX
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_TX_ID, txId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            "confirm_$txId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, "تایید", pending).build()
    }

    /**
     * برنامه را روی شیتِ دسته باز می‌کند. `getActivity` است نه `getBroadcast` — کاربر باید
     * صفحه را ببیند، و بازکردنِ Activity از BroadcastReceiver روی اندروید ۱۰+ محدود است.
     */
    private fun pickCategoryAction(
        context: Context,
        notificationId: Int,
        txId: Long,
        label: String,
    ): NotificationCompat.Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_TX_ID, txId)
            putExtra(EXTRA_PICK_CATEGORY, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            "category_$txId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, label, pending).build()
    }

    private fun openTransactionIntent(context: Context, requestCode: Int, txId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_TX_ID, txId)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    const val GROUP_AUTO_TX = "ir.sadteam.loancalc.group.AUTO_TX"
}

/** تراکنشی که اعلان به آن اشاره می‌کند. `MainActivity` باید بخواندش. */
const val EXTRA_OPEN_TX_ID = "open_tx_id"

/** اگر `true`، برنامه باید شیتِ دسته را هم باز کند نه فقط تراکنش را نشان دهد. */
const val EXTRA_PICK_CATEGORY = "pick_category"
