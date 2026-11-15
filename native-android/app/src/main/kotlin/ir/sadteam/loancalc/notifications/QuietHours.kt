package ir.sadteam.loancalc.notifications

import java.util.Calendar

/**
 * 🌙 **ساعتِ آرام** (گزارشِ کاربر، ۱۶ مهر: یادآورِ دنگ ساعتِ ۲:۲۸ بامداد آمد، گزارش‌های ادمین ۲ تا ۵ صبح).
 * اندروید زمانِ اجرای کارهای پس‌زمینه را دقیق نگه نمی‌دارد، پس هر اعلانِ زمان‌بندی‌شده خودش باید
 * بپرسد «الان وقتش هست؟». اعلانِ تراکنشِ بانکی (کارِ خودِ کاربر، همان لحظه) از این قاعده بیرون است.
 */
object QuietHours {
    /** ۲۳ تا ۸ صبح: هیچ یادآوری‌ای نه. */
    fun isQuiet(hour: Int = nowHour()): Boolean = hour >= 23 || hour < 8

    /** اعلان‌های ادمین (پیامِ کاربر، گزارشِ روز) فقط ۱۰ صبح تا ۱۰ شب - خواسته‌ی کاربر. */
    fun adminWindow(hour: Int = nowHour()): Boolean = hour in 10..21

    fun nowHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
}
