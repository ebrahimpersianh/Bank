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
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

private fun openIntent(context: Context, shortcut: String, requestCode: Int): PendingIntent =
    PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("jibak_shortcut", shortcut)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

/**
 * 🔕 **فاصله‌ی اعلان‌های غیرفوری** (۸ مهر، خواسته‌ی کاربر: «با هم نیایند»): اگر در یک ساعتِ اخیر
 * اعلانِ دیگری از جیبک هنوز در نوار هست، اعلانِ غیرفوری (بودجه، خلاصه‌ی هفتگی، «برگرد») صبر می‌کند.
 * یادآوریِ قسط/چک/قبض هرگز صبر نمی‌کند.
 */
object NotifSpacing {
    private const val GAP_MS = 60 * 60 * 1000L
    fun busy(context: Context): Boolean = runCatching {
        val nm = context.getSystemService(android.app.NotificationManager::class.java) ?: return false
        val now = System.currentTimeMillis()
        nm.activeNotifications.any { now - it.postTime < GAP_MS }
    }.getOrDefault(false)
}

private fun notify(context: Context, id: Int, title: String, text: String, shortcut: String) {
    ReminderChannels.ensureAll(context)
    val n = NotificationCompat.Builder(context, ReminderChannels.CHANNEL_NUDGES)
        .setSmallIcon(R.drawable.ic_notification)
        .setLargeIcon(ReminderChannels.largeIcon(context))
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(openIntent(context, shortcut, id))
        .setAutoCancel(true)
        .build()
    runCatching { NotificationManagerCompat.from(context).notify(id, n) }
}

/**
 * 🔔 **هشدارِ بودجه** (۸ مهر، خواسته‌ی کاربر): وقتی خرجِ یک دسته از ۸۰٪ و بعد از ۱۰۰٪ِ بودجه‌ی
 * ماهش گذشت، یک اعلان. هر سطح برای هر بودجه **ماهی یک‌بار** (کلیدِ ماه در SharedPreferences) -
 * پس ویرایش/حذفِ تراکنش و برگشتنِ زیرِ ۸۰٪ دوباره اعلان نمی‌دهد.
 */
object BudgetAlerts {
    @OptIn(FlowPreview::class)
    fun start(context: Context, repo: AccountRepository) {
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            combine(repo.observeBudgets(), repo.observeTransactions()) { b, t -> b to t }
                .debounce(1500)
                .collect { (budgets, txs) -> runCatching { check(app, repo, budgets, txs) } }
        }
    }

    @Volatile private var retryScheduled = false

    /** اعلانِ دیگری تازه آمده؛ یک ساعت بعد دوباره نگاه کن (فقط یک نوبتِ منتظر). */
    private fun retryLater(context: Context, repo: AccountRepository) {
        if (retryScheduled) return
        retryScheduled = true
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(61 * 60 * 1000L)
            retryScheduled = false
            runCatching { check(context, repo, repo.observeBudgets().first(), repo.observeTransactions().first()) }
        }
    }

    private fun check(
        context: Context,
        repo: AccountRepository,
        budgets: List<ir.sadteam.loancalc.data.db.BudgetEntity>,
        txs: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>,
    ) {
        val today = JalaliCalendar.today()
        val month = "${today.y}-${today.m}"
        val prefs = context.getSharedPreferences("budget_alerts", Context.MODE_PRIVATE)
        if (prefs.getString("month", null) != month) prefs.edit().clear().putString("month", month).apply()
        budgets.filter { it.monthlyCap > 0 }.forEach { b ->
            val spent = repo.spendByCategory(txs, today.y, today.m, b.accountId)[b.categoryName] ?: 0.0
            val pct = (spent * 100 / b.monthlyCap).toInt()
            val level = when {
                pct >= 100 -> 100
                pct >= 80 -> 80
                else -> return@forEach
            }
            val key = "${b.id}:$level"
            if (prefs.getBoolean(key, false)) return@forEach
            if (NotifSpacing.busy(context)) { retryLater(context, repo); return }
            // ۱۰۰٪ که رسید، ۸۰٪ را هم «گفته‌شده» علامت بزن تا بعداً جدا نیاید.
            prefs.edit().putBoolean(key, true).putBoolean("${b.id}:80", true).apply()
            val left = (b.monthlyCap - spent).coerceAtLeast(0.0)
            val (title, text) = if (level == 100) {
                "بودجه‌ی «${b.categoryName}» تمام شد" to
                    "این ماه ${toFa(pct)}٪ِ بودجه‌ی ${b.categoryName} خرج شده. اگر لازم است بودجه را جابه‌جا کن."
            } else {
                "${toFa(pct)}٪ِ بودجه‌ی «${b.categoryName}» خرج شد" to
                    "${left.rialToFaCompact()} تومان تا آخرِ ماه مانده."
            }
            notify(context, 918_500 + (b.id % 400).toInt() + if (level == 100) 400 else 0, title, text, "budget")
        }
    }
}

/**
 * 🗓 **خلاصه‌ی هفتگی** (۸ مهر، خواسته‌ی کاربر): جمعه عصر یک اعلان با خرج و درآمدِ هفته، مقایسه
 * با هفته‌ی قبل و پرخرج‌ترین دسته؛ تپ → تبِ گزارش. هفته‌ای یک‌بار (شنبه تا جمعه).
 */
@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: AccountRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = Calendar.getInstance()
        if (now.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY || now.get(Calendar.HOUR_OF_DAY) < 18) return Result.success()
        val today = JalaliCalendar.today()
        val weekKey = "${today.y}-${today.m}-${today.d}"
        val prefs = applicationContext.getSharedPreferences("weekly_summary", Context.MODE_PRIVATE)
        if (prefs.getString("last", null) == weekKey) return Result.success()

        // جابه‌جایی بینِ حساب‌های خودِ کاربر نه خرج است نه درآمد (بازبینیِ ۹ مهر).
        val txs = repo.observeTransactions().first().filter { it.confirmed && it.sourceType != ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER }
        fun key(y: Int, m: Int, d: Int) = y * 10_000 + m * 100 + d
        val days = (0..6).map { PersianCalendar.addDays(today, -it) }.map { key(it.y, it.m, it.d) }.toSet()
        val prevDays = (7..13).map { PersianCalendar.addDays(today, -it) }.map { key(it.y, it.m, it.d) }.toSet()
        val out = TransactionType.WITHDRAWAL.name
        val inn = TransactionType.DEPOSIT.name
        val week = txs.filter { key(it.year, it.month, it.day) in days }
        val prev = txs.filter { key(it.year, it.month, it.day) in prevDays }
        if (week.isEmpty()) return Result.success()
        // اعلانِ دیگری تازه آمده: اجرای بعدی (۳ ساعت بعد) دوباره امتحان می‌کند.
        if (NotifSpacing.busy(applicationContext)) return Result.success()
        prefs.edit().putString("last", weekKey).apply()

        val spent = week.filter { it.type == out }.sumOf { it.amount }
        val earned = week.filter { it.type == inn }.sumOf { it.amount }
        val prevSpent = prev.filter { it.type == out }.sumOf { it.amount }
        val top = week.filter { it.type == out }.groupBy { it.category ?: "سایر" }
            .mapValues { (_, l) -> l.sumOf { it.amount } }.maxByOrNull { it.value }
        val text = buildString {
            append("خرج ${spent.rialToFaCompact()} · درآمد ${earned.rialToFaCompact()} تومان")
            if (prevSpent > 0) {
                val d = ((spent - prevSpent) * 100 / prevSpent).toInt()
                append(if (d > 0) " · ${toFa(d)}٪ بیشتر از هفته‌ی قبل" else if (d < 0) " · ${toFa(-d)}٪ کمتر از هفته‌ی قبل" else "")
            }
            top?.let { append("\nبیشترین خرج: ${it.key} (${it.value.rialToFaCompact()} تومان)") }
        }
        notify(applicationContext, 918_990, "خلاصه‌ی این هفته", text, "report")
        ir.sadteam.loancalc.data.UsageStats.action("weekly_summary_sent")
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            // هر ۳ ساعت بیدار می‌شود و فقط جمعه بعد از ۱۸ کار می‌کند - WorkManager زمانِ دقیق نمی‌دهد.
            val req = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(3, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("weekly_summary", ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
