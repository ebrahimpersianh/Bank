package ir.sadteam.loancalc.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * یادآورِ **رأسِ ساعت** - خواسته‌ی کاربر: «دقیقاً باید باشد».
 *
 * `PeriodicWorkRequest` ساعتِ دقیق ندارد و روی شیائومی و… تا چند ساعت عقب می‌افتد. این‌جا یک
 * آلارمِ دقیق (`setExactAndAllowWhileIdle`) سرِ ساعتِ یادآور گذاشته می‌شود؛ وقتی زنگ زد،
 * [DueDateReminderWorker] را فوراً اجرا می‌کند و آلارمِ فردا را می‌چیند.
 *
 * ⚠️ از اندروید ۱۲ به بعد آلارمِ دقیق اجازه‌ی «آلارم و یادآوری» می‌خواهد (صفحه‌ی تنظیماتِ
 * یادآوری توضیح می‌دهد و می‌گیردش). بی‌اجازه آلارمِ تقریبی گذاشته می‌شود - باز بهتر از هیچ.
 */
object ExactReminderAlarm {
    private const val REQUEST_CODE = 7310
    const val ACTION = "ir.sadteam.loancalc.EXACT_REMINDER"

    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    fun schedule(context: Context, hour: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val at = nextAt(hour)
        val pi = pendingIntent(context)
        runCatching {
            if (canScheduleExact(context)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        am.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ExactReminderReceiver::class.java).setAction(ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun nextAt(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}

/** زنگِ آلارمِ دقیق: یادآورها را همین حالا می‌فرستد و آلارمِ فردا را می‌چیند. */
@AndroidEntryPoint
class ExactReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val ok = intent.action == ExactReminderAlarm.ACTION ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
        if (!ok) return
        if (intent.action == ExactReminderAlarm.ACTION) {
            runCatching {
                WorkManager.getInstance(context)
                    .enqueue(OneTimeWorkRequestBuilder<DueDateReminderWorker>().build())
            }
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                runCatching { reminderScheduler.schedule() }
            } finally {
                pending.finish()
            }
        }
    }
}
