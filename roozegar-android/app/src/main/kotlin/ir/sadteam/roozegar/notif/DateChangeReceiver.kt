package ir.sadteam.roozegar.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.updateAll
import ir.sadteam.roozegar.widget.MonthWidget
import ir.sadteam.roozegar.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * تنها مسیر به‌روزرسانی «روز» تو کل اپ: بوت گوشی، تغییر دستی ساعت، تعویض منطقه‌ی زمانی، آپدیت اپ،
 * و آلارم رأس نیمه‌شب - همه از همین‌جا اعلان تاریخ و هر دو ویجت رو تازه می‌کنن و آلارم نیمه‌شب بعدی
 * رو دوباره برپا می‌کنن.
 */
class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                DayNotification.refresh(context)
                Scheduler.scheduleMidnight(context)
                TodayWidget().updateAll(context)
                MonthWidget().updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MIDNIGHT = "ir.sadteam.roozegar.ACTION_MIDNIGHT"
    }
}

object Scheduler {
    /** آلارم بعدی رأس نیمه‌شب (۵ ثانیه بعدش، برای اطمینان از ردشدن مرز روز). Exact فقط وقتی مجازه؛
     * وگرنه یه پنجره‌ی ۱۰ دقیقه‌ای - برای تعویض عدد تاریخ کاملاً کافیه و باتری هم اذیت نمی‌شه. */
    fun scheduleMidnight(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val next = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DateChangeReceiver::class.java).setAction(DateChangeReceiver.ACTION_MIDNIGHT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, next, 10 * 60 * 1000L, pi)
        }
    }
}
