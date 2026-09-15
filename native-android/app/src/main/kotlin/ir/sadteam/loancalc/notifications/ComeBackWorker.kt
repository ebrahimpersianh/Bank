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
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.db.InboxMessageEntity
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
    /** `71e`: اعلانِ برگشت هم یک ردیفِ تاریخچه می‌نویسد - وگرنه «تمامِ اعلان‌ها» ناقص است. */
    private val inboxRepository: InboxRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!uiPrefs.comeBackReminderEnabled.first()) return Result.success()

        val transactions = accountRepository.observeTransactions().first()
        // هیچ تراکنشی نداره = کاربرِ تازه‌وارد، نه کاربرِ غایب. سرزنشش نکن.
        if (transactions.isEmpty()) return Result.success()

        // 🚨 ساعتِ اجرا. `PeriodicWorkRequest` ساعتِ ثابت ندارد - هر وقت اولین‌بار
        // زمان‌بندی شود، هر ۲۴ ساعت همان ساعت تکرار می‌شود. یک‌بار همین باعث شد اعلانِ
        // «۱ روزه رفتی» ساعتِ ۱:۳۰ بامداد برسد. زمان‌بند حالا به [NUDGE_HOUR] لنگر
        // می‌اندازد، و این نگهبان اجرای بدموقع (بوت، تعویضِ ساعت، جبرانِ عقب‌افتاده) را
        // ساکت رد می‌کند - اعلانِ انگیزشی نیمه‌شب بدتر از نفرستادنش است.
        val hourNow = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hourNow < NUDGE_HOUR || hourNow > NUDGE_HOUR_LATEST) return Result.success()

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

    private suspend fun notifyComeBack(days: Int) {
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
        inboxRepository.post(
            kind = InboxMessageEntity.Kind.STREAK_REMINDER,
            title = "${toFa(days)} روزه رفتی، وقتشه برگردی",
            body = "${toFa(days)} روزه تراکنش ثبت نکردی و نظمِ مالی‌ت در خطره.",
        )
    }

    /** فاصله‌ی روزِ تقریبی بینِ دو تاریخِ شمسی - برای «چند روز غایب بودی» دقتِ روز کافیه. */
    private fun daysBetween(from: PersianDate, to: PersianDate): Int =
        runCatching { JalaliCalendar.daysBetween(from, to) }.getOrDefault(0)

    companion object {
        private const val NOTIFICATION_ID = 918_273

        /** ساعتِ ارسالِ اعلانِ برگشت (عصر - همان منطقِ `DAILY_NUDGE_FROM_HOUR`ی یادآورِ روزانه). */
        const val NUDGE_HOUR = 20

        /** بعد از این ساعت دیگر فرستاده نمی‌شود - نیمه‌شب کسی را بیدار نمی‌کنیم. */
        const val NUDGE_HOUR_LATEST = 22
    }
}

/** زمان‌بندِ روزانه‌ی [ComeBackWorker] - هم‌الگو با [AutoBackupScheduler]. */
@Singleton
class ComeBackScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<ComeBackWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(minutesUntilNextNudgeHour(), TimeUnit.MINUTES)
            .build()
        // ⚠️ `UPDATE` نه `KEEP`: کاربرانی که کارِ بی‌لنگرِ قبلی رویشان نشسته (و اعلان را
        // مثلاً ۱:۳۰ بامداد می‌گرفتند) وگرنه تا نصبِ دوباره همان ساعت می‌ماندند.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /** دقیقه تا نزدیک‌ترین [ComeBackWorker.NUDGE_HOUR]ی بعدی. */
    private fun minutesUntilNextNudgeHour(): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, ComeBackWorker.NUDGE_HOUR)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return (target.timeInMillis - now.timeInMillis) / 60_000L
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "come_back_work"
    }
}
