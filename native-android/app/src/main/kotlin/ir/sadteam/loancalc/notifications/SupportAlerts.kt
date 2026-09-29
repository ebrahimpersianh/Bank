package ir.sadteam.loancalc.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.data.AuthRepository
import java.util.concurrent.TimeUnit

/**
 * 🔔 خبرِ «پیامِ تازه از کاربر» برای **صاحبِ برنامه** (خواسته‌ی کاربر، ۷ مهر: «همون لحظه»).
 *
 * بی سرویسِ پوشِ گوگل (در ایران قابلِ‌اتکا نیست) دو لایه داریم:
 *  - برنامه باز است → هر ۲۰ ثانیه می‌پرسد ([SupportInboxViewModel.watch]) = تقریباً همان لحظه.
 *  - برنامه بسته است → هر ۱۵ دقیقه ([SupportCheckWorker]) - کمترین فاصله‌ای که اندروید اجازه می‌دهد.
 */
object SupportAlerts {
    private const val PREFS = "support_alerts"
    private const val KEY_LAST = "last_seen_id"
    private const val NOTIFICATION_ID = 771_300

    /** پیام‌های بازِ تازه‌تر از آخرین دیده‌شده را اعلان می‌کند؛ تعدادِ تازه‌ها را برمی‌گرداند. */
    suspend fun check(context: Context, repo: AuthRepository, notify: Boolean = true): Int {
        val list = repo.adminSupport() ?: return 0
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val last = prefs.getLong(KEY_LAST, -1L)
        val maxId = list.items.maxOfOrNull { it.id } ?: 0L
        if (last < 0) { // اولین بار: فقط نقطه‌ی شروع را ثبت کن، سیلِ اعلانِ پیام‌های قدیمی نه.
            prefs.edit().putLong(KEY_LAST, maxId).apply()
            return 0
        }
        val fresh = list.items.filter { it.id > last }
        if (fresh.isEmpty()) return 0
        prefs.edit().putLong(KEY_LAST, maxId).apply()
        if (notify) show(context, fresh.size, fresh.first().message)
        return fresh.size
    }

    private fun show(context: Context, count: Int, preview: String) {
        ReminderChannels.ensureAll(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = if (count == 1) "پیامِ تازه از یک کاربر" else "${ir.sadteam.loancalc.core.toFa(count)} پیامِ تازه از کاربران"
        val n = NotificationCompat.Builder(context, ReminderChannels.CHANNEL_DUE_DATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(preview.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(preview.take(400)))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n) }
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<SupportCheckWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("support_check", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}

@HiltWorker
class SupportCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authRepository: AuthRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!authRepository.isAdmin()) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork("support_check")
            return Result.success()
        }
        SupportAlerts.check(applicationContext, authRepository)
        return Result.success()
    }
}
