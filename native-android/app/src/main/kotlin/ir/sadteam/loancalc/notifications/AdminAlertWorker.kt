package ir.sadteam.loancalc.notifications

import android.content.Context
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
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import java.util.concurrent.TimeUnit

/**
 * 🚨 **هشدارِ خودکار به ادمین** (۸ مهر): هر ۶ ساعت گزارشِ امروز را می‌گیرد و اگر هشدارِ تازه‌ای
 * هست (پیامِ بی‌جواب، زیاد شدنِ کرش، افتِ کاربرِ فعال، یا اولین فروشِ روز) اعلان می‌دهد.
 * فقط روی گوشیِ حسابِ ادمین کاری می‌کند؛ بقیه بی‌صدا رد می‌شوند.
 */
@HiltWorker
class AdminAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: AuthRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!runCatching { auth.isAdmin() }.getOrDefault(false)) return Result.success()
        val d = auth.adminDigest("day") ?: return Result.success()
        val lines = buildList {
            d.notes.forEach { n ->
                when {
                    n.startsWith("open_support:") -> add("${toFa(n.removePrefix("open_support:"))} پیامِ بی‌جواب")
                    n == "crashes_up" -> add("کرش‌ها زیاد شده")
                    n == "active_down" -> add("کاربرِ فعال کم شده")
                }
            }
            d.metrics.firstOrNull { it.key == "purchases" }?.takeIf { it.now > 0 }?.let { add("${toFa(it.now)} خریدِ امروز") }
        }
        if (lines.isEmpty()) return Result.success()
        val key = lines.joinToString("|")
        val prefs = applicationContext.getSharedPreferences("admin_alert", Context.MODE_PRIVATE)
        if (prefs.getString("last", null) == key) return Result.success()
        prefs.edit().putString("last", key).apply()
        ReminderChannels.ensureAll(applicationContext)
        val n = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_NUDGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ادمین · گزارشِ امروز")
            .setContentText(lines.joinToString(" · "))
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, n) }
        return Result.success()
    }

    companion object {
        private const val NOTIFICATION_ID = 918_400
        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<AdminAlertWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("admin_alert", ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
