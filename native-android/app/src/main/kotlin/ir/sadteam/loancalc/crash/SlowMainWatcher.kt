package ir.sadteam.loancalc.crash

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

/**
 * 🔬 **ابزارِ تشخیصِ کندی** (۱۶ مهر): وقتی رشته‌ی اصلی بیش از ~۱۲۰ میلی‌ثانیه گیر کند، از جایی که
 * گیر کرده «عکسِ پشته» می‌گیرد و در حافظه نگه می‌دارد (آخرین ۱۲ مورد). هیچ داده‌ی کاربر
 * (مبلغ، نام، شماره) ثبت نمی‌شود - فقط نامِ کلاس/تابعِ برنامه و نامِ صفحه. با ضربه‌ی طولانی
 * روی «نسخه» در تنظیمات ← درباره، همراهِ اطلاعاتِ فنی کپی می‌شود.
 */
object SlowMainWatcher {
    private const val STALL_MS = 120L
    private const val MAX_EVENTS = 12

    @Volatile var route: String = "?"
    private val events = ArrayDeque<String>()
    @Volatile private var started = false
    private var prefs: android.content.SharedPreferences? = null
    private const val PREFS = "slow_main"
    private const val KEY_EVENTS = "events"
    private const val KEY_LAST_SENT_DAY = "last_sent_day"
    private const val SEP = "\u0001"

    fun start(context: Context) {
        if (started) return
        started = true
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // موردهای ثبت‌شده‌ی اجراهای قبلی (هنوز نفرستاده‌ایم) برمی‌گردند.
        prefs?.getString(KEY_EVENTS, null)?.split(SEP)?.filter { it.isNotBlank() }?.takeLast(MAX_EVENTS)?.let { old ->
            synchronized(events) { events.addAll(old) }
        }
        val main = Handler(Looper.getMainLooper())
        val mainThread = Looper.getMainLooper().thread
        Thread({
            while (true) {
                val ack = AtomicLong(0)
                val sent = SystemClock.uptimeMillis()
                main.post { ack.set(SystemClock.uptimeMillis()) }
                var sample: String? = null
                var sampled = 0
                while (ack.get() == 0L) {
                    Thread.sleep(25)
                    val waited = SystemClock.uptimeMillis() - sent
                    if (waited >= STALL_MS && sampled < 2) {
                        sampled++
                        val s = summarize(mainThread.stackTrace)
                        sample = if (sample == null) s else "$sample || $s"
                    }
                }
                val total = ack.get() - sent
                if (total >= STALL_MS && sample != null) {
                    val line = "[${route}] ${total}ms: $sample"
                    synchronized(events) {
                        events.addLast(line)
                        while (events.size > MAX_EVENTS) events.removeFirst()
                        prefs?.edit()?.putString(KEY_EVENTS, events.joinToString(SEP))?.apply()
                    }
                }
                Thread.sleep(60)
            }
        }, "slow-main-watch").apply { isDaemon = true }.start()
    }

    private fun summarize(trace: Array<StackTraceElement>): String {
        val ours = trace.filter { it.className.startsWith("ir.sadteam") }
            .take(6).joinToString(" < ") { "${it.className.substringAfterLast('.')}.${it.methodName}:${it.lineNumber}" }
        val top = trace.firstOrNull()?.let { "${it.className.substringAfterLast('.')}.${it.methodName}" } ?: "?"
        return "top=$top ours=${ours.ifEmpty { "-" }}"
    }

    fun report(): String = synchronized(events) {
        if (events.isEmpty()) "کندیِ ثبت‌شده: ندارد" else "کندیِ رشته‌ی اصلی (آخرین ${events.size}):\n" + events.joinToString("\n")
    }

    /**
     * روزی یک‌بار (اگر موردی ثبت شده باشد): متنِ گزارش را برمی‌گرداند و حافظه را خالی می‌کند. فرستادنش با
     * فراخوان است؛ بعد از موفقیتِ ارسال [markSent] را صدا بزن. فقط نامِ صفحه/تابع، بی هیچ داده‌ی کاربر.
     */
    fun pendingToSend(): String? {
        val today = System.currentTimeMillis() / 86_400_000L
        if (prefs?.getLong(KEY_LAST_SENT_DAY, -1L) == today) return null
        return synchronized(events) {
            if (events.isEmpty()) null else events.joinToString("\n")
        }
    }

    fun markSent() {
        synchronized(events) {
            events.clear()
            prefs?.edit()?.remove(KEY_EVENTS)?.putLong(KEY_LAST_SENT_DAY, System.currentTimeMillis() / 86_400_000L)?.apply()
        }
    }
}
