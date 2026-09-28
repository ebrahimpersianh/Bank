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
 * ### 🚨 یک سوال در هر لحظه — بازنگریِ ۲۱ شهریور
 * نسخه‌ی قبلی **دو سوالِ متفاوت را هم‌زمان** می‌پرسید: «تایید» و «دسته‌اش این نیست». کاربر
 * گزارش داد گیج‌کننده و خسته‌کننده است و اول نمی‌داند این تراکنش اصلاً از کجا آمده. حالا:
 *
 * ۱. **اعلانِ اول فقط یک سوال دارد: این درست است یا نه؟** دو دکمه: «آره، ثبت کن» / «نه».
 *    خطِ دومِ اعلان **منبع** را می‌گوید («پیامکِ بیمه آسیا»)، چون بی منبع کاربر نمی‌تواند
 *    قضاوت کند. لمسِ خودِ اعلان متنِ خامِ همان پیامک را در برنامه نشان می‌دهد.
 * ۲. **دسته فقط بعدِ تایید پرسیده می‌شود، و فقط وقتی نامشخص است** — یک اعلانِ آرامِ
 *    کم‌اولویت (بی صدا) با یک دکمه. اگر حدس مطمئن بود، اصلاً پرسیده نمی‌شود؛ کاربر
 *    می‌تواند بعداً از خودِ تراکنش عوضش کند.
 *
 * نتیجه: در حالتِ عادی **یک تپ**، و در بدترین حالت دو تپِ جدا از هم — نه دو دکمه‌ی رقیب
 * در یک اعلان.
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
        /** «پیامک از ۱۰۰۰…» یا «اعلانِ بلوبانک» — بی این، کاربر نمی‌داند این از کجا آمده. */
        sourceLabel: String? = null,
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
            // خطِ دوم **منبع** است، نه دسته: اولین چیزی که کاربر می‌پرسد «این از کجا آمد؟»
            // است و دسته تا بعد از تایید اصلاً مطرح نیست.
            .setContentText(
                listOfNotNull(sourceLabel, "روی «$accountName» ثبت می‌شود").joinToString(" · "),
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    listOfNotNull(
                        sourceLabel,
                        "روی «$accountName» ثبت می‌شود" + if (confident) " · دسته: $category" else "",
                        "درست است؟",
                    ).joinToString("\n"),
                ),
            )
            .setContentIntent(openTransactionIntent(context, notificationId, txId))
            .setGroup(GROUP_AUTO_TX)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // فقط یک سوال: درست است یا نه. دسته بعداً و جدا.
        builder.addAction(confirmAction(context, notificationId, txId, confident))
        builder.addAction(rejectAction(context, notificationId, txId))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun confirmAction(
        context: Context,
        notificationId: Int,
        txId: Long,
        confident: Boolean,
    ): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CONFIRM_TX
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_TX_ID, txId)
            // گیرنده از روی همین تصمیم می‌گیرد سوالِ دسته را بپرسد یا نه.
            putExtra(NotificationActionReceiver.EXTRA_CATEGORY_KNOWN, confident)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            "confirm_$txId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, "آره، ثبت کن", pending).build()
    }

    /**
     * **نه** — تراکنشِ تاییدنشده پاک می‌شود و پیامِ صندوق هم بسته. بی این دکمه، تشخیصِ غلط
     * برای همیشه معلق می‌ماند و کاربر باید خودش دنبالش بگردد.
     */
    private fun rejectAction(context: Context, notificationId: Int, txId: Long): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REJECT_TX
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_TX_ID, txId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            "reject_$txId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, "نه", pending).build()
    }

    /**
     * **مرحله‌ی دوم** — فقط بعدِ تاییدِ کاربر و فقط وقتی دسته نامشخص بوده. عمداً
     * `PRIORITY_LOW` و بی صدا: خبرِ اصلی داده شده، این فقط یک کارِ کوچکِ باقی‌مانده است.
     */
    fun askCategory(context: Context, txId: Long) {
        ReminderChannels.ensureAll(context)
        val notificationId = "autocat_$txId".hashCode()
        val builder = NotificationCompat.Builder(context, ReminderChannels.CHANNEL_AUTO_TX)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ثبت شد ✓")
            .setContentText("فقط دسته‌اش مانده - اگر الان وقت نداری، بعداً از خودِ تراکنش عوضش کن.")
            .setContentIntent(pickCategoryIntent(context, txId))
            .setGroup(GROUP_AUTO_TX)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                NotificationCompat.Action.Builder(0, "انتخابِ دسته", pickCategoryIntent(context, txId)).build(),
            )
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun pickCategoryIntent(context: Context, txId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_TX_ID, txId)
            putExtra(EXTRA_PICK_CATEGORY, true)
        }
        return PendingIntent.getActivity(
            context,
            "category_$txId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
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
