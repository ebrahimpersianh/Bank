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
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** کلیدِ Intent برای «تپ رو اعلانِ برگشت → مستقیم صفحه‌ی ثبتِ تراکنش». */
const val EXTRA_OPEN_NEW_TRANSACTION = "open_new_transaction"

/**
 * اعلانِ برگشتِ کاربر - خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ مرجع:
 * «۲ روزه رفتی، وقتشه برگردی 💔 / ۲ روزه تراکنش ثبت نکردی و نظمِ مالی‌ت در خطره.»
 *
 * تفاوت‌هاش با یادآورِ روزانه‌ی موجود ([DueDateReminderWorker]):
 * - اون سرِ ساعتِ ثابت به **همه** یادآوری می‌کنه؛ این فقط وقتی می‌زنه که کاربر **واقعاً غایب** بوده.
 * - عددِ روزها **واقعیه** (از آخرین تراکنشِ ثبت‌شده حساب می‌شه)، نه یه عددِ ثابت.
 *
 * ⚠️ **جلوگیری از دو اعلان تو یه روز**: اگه این اعلان زده بشه، تاریخِ امروز تو
 * [UiPrefs.lastComeBackNotifiedAt] ثبت می‌شه و [DueDateReminderWorker] هم همون روز ساکت می‌مونه -
 * وگرنه کاربرِ غایب یه روز دو تا اعلان می‌گرفت که آزاردهنده‌ست.
 */
@HiltWorker
class ComeBackWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val uiPrefs: UiPrefs,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!uiPrefs.comeBackReminderEnabled.first()) return Result.success()

        val transactions = accountRepository.observeTransactions().first()
        // هیچ تراکنشی نداره = کاربرِ تازه‌وارد، نه کاربرِ غایب. سرزنشش نکن.
        if (transactions.isEmpty()) return Result.success()

        val today = JalaliCalendar.today()
        val last = transactions.maxByOrNull { it.year * 10000 + it.month * 100 + it.day } ?: return Result.success()
        val daysAway = daysBetween(PersianDate(last.year, last.month, last.day), today)
        if (daysAway < 1) return Result.success()

        // یه‌بار در روز، نه بیشتر - حتی اگه WorkManager چند بار اجراش کنه.
        val todayKey = "${today.y}-${today.m}-${today.d}"
        if (uiPrefs.lastComeBackNotifiedAt.first() == todayKey) return Result.success()

        notifyComeBack(daysAway)
        uiPrefs.setLastComeBackNotifiedAt(todayKey)
        return Result.success()
    }

    private fun notifyComeBack(days: Int) {
        // اعلانِ انگیزشی کانالِ کم‌اهمیتِ خودش را دارد، جدا از یادآورِ سررسید - پس کاربر
        // می‌تواند این یکی را خاموش کند و یادآورِ قسط را نگه دارد.
        ReminderChannels.ensureAll(applicationContext)
        val channelId = ReminderChannels.CHANNEL_NUDGES
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_NEW_TRANSACTION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("${toFa(days)} روزه رفتی، وقتشه برگردی")
            .setContentText("${toFa(days)} روزه تراکنش ثبت نکردی و نظمِ مالی‌ت در خطره. دخل‌وخرجت رو ثبت کن و ادامه بده.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${toFa(days)} روزه تراکنش ثبت نکردی و نظمِ مالی‌ت در خطره. دخل‌وخرجت رو ثبت کن و ادامه بده.",
                ),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    /** فاصله‌ی روزِ تقریبی بینِ دو تاریخِ شمسی - برای «چند روز غایب بودی» دقتِ روز کافیه. */
    private fun daysBetween(from: PersianDate, to: PersianDate): Int =
        runCatching { JalaliCalendar.daysBetween(from, to) }.getOrDefault(0)

    private companion object {
        const val NOTIFICATION_ID = 918_273
    }
}

/** زمان‌بندِ روزانه‌ی [ComeBackWorker] - هم‌الگو با [AutoBackupScheduler]. */
@Singleton
class ComeBackScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<ComeBackWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "come_back_work"
    }
}
