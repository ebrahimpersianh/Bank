package ir.sadteam.loancalc.data

import android.content.Context
import android.os.Build
import ir.sadteam.loancalc.data.network.ApiClient
import ir.sadteam.loancalc.data.network.UsageBatchEvent
import ir.sadteam.loancalc.data.network.UsageBatchRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * آمارِ استفاده‌ی **بی‌نام** روی سرورِ خودمان (تصمیمِ کاربر، ۶ مهر: به‌جای Firebase؛ ۷ مهر: کامل‌تر).
 *
 * هر نصب یک **شناسه‌ی تصادفیِ خودش** را دارد (نه شماره، نه حساب، نه شناسه‌ی گوشی) تا بشود «چند
 * نفر» را از «چند بار» جدا کرد، و ماندگاری (برگشتنِ کاربر) را سنجید. فرستاده می‌شود: نامِ صفحه
 * (`screen:…`)، نامِ کار (`action:…`)، تعداد در هر روز، شماره‌ی نسخه، استور، نسخه‌ی اندروید، و
 * اینکه وارد حساب شده یا نه. **هیچ مبلغ، عنوان، نامِ حساب، شماره‌کارت، موجودی، شماره‌ی موبایل یا
 * متنِ پیامکی** فرستاده نمی‌شود. رویدادها روی گوشی جمع و هر چند دقیقه یک‌جا فرستاده می‌شوند؛
 * شکستِ شبکه یعنی دفعه‌ی بعد. کلیدِ خاموش در تنظیمات.
 */
object UsageStats {
    const val FIRST_OPEN = "first_open"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val TRANSACTION_CREATED = "transaction_created"
    const val ACCOUNT_CREATED = "account_created"
    const val REPORT_VIEWED = "report_viewed"
    const val SESSION_START = "session_start"

    private const val PREFS = "usage_stats"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_FIRST_OPEN_SENT = "first_open_sent"
    private const val KEY_INSTALL_ID = "install_id"
    private const val KEY_PENDING = "pending"
    private const val FLUSH_EVERY_MS = 3 * 60 * 1000L
    private const val SESSION_GAP_MS = 30 * 60 * 1000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val api by lazy { ApiClient.create() }
    private var appContext: Context? = null
    private var store: String? = null

    /** از بیرون (اپ) پُر می‌شود - فقط «واردشده یا نه»، نه اینکه چه کسی. */
    @Volatile
    var loggedIn: Boolean = false

    /** کلید `روز|نام` → تعداد. */
    private val pending = LinkedHashMap<String, Int>()
    private var lastScreen: String? = null
    private var backgroundAt = 0L
    private var flushing = false

    fun init(context: Context, store: String? = null) {
        appContext = context.applicationContext
        this.store = store?.lowercase()?.filter { it in 'a'..'z' }?.take(20)
        val p = prefs() ?: return
        synchronized(pending) { decode(p.getString(KEY_PENDING, null)) }
        if (!p.getBoolean(KEY_FIRST_OPEN_SENT, false)) {
            p.edit().putBoolean(KEY_FIRST_OPEN_SENT, true).apply()
            track(FIRST_OPEN)
        }
        scope.launch {
            while (true) {
                delay(FLUSH_EVERY_MS)
                flush()
            }
        }
    }

    fun isEnabled(): Boolean = prefs()?.getBoolean(KEY_ENABLED, true) ?: true

    fun setEnabled(enabled: Boolean) {
        prefs()?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
        if (!enabled) {
            synchronized(pending) { pending.clear() }
            persist()
        }
    }

    fun track(name: String, count: Int = 1) {
        if (!isEnabled() || count <= 0) return
        val key = "${today()}|$name"
        synchronized(pending) { pending[key] = (pending[key] ?: 0) + count }
        persist()
    }

    /** ورود به یک صفحه - [route] الگوی مسیر است (`loan_detail/{id}`)؛ فقط تکه‌ی اول نگه داشته می‌شود. */
    fun screen(route: String?) {
        val clean = clean(route ?: return) ?: return
        // بازسازیِ همان صفحه (چرخشِ گوشی، برگشت از پنجره) دوباره شمرده نمی‌شود.
        if (clean == lastScreen) return
        lastScreen = clean
        track("screen:$clean")
    }

    /** یک کارِ مشخص (مثلاً `loan_added`، `backup_restored`). */
    fun action(name: String) {
        val clean = clean(name) ?: return
        track("action:$clean")
    }

    /** اپ به پیش‌زمینه آمد - اگر بیش از نیم ساعت بیرون بوده، یک «بارِ استفاده»ی تازه است. */
    fun onForeground() {
        val now = System.currentTimeMillis()
        if (backgroundAt == 0L || now - backgroundAt > SESSION_GAP_MS) {
            track(SESSION_START)
            lastScreen = null
        }
    }

    fun onBackground() {
        backgroundAt = System.currentTimeMillis()
        scope.launch { flush() }
    }

    private suspend fun flush() {
        val context = appContext ?: return
        if (!isEnabled()) return
        val snapshot = synchronized(pending) {
            if (flushing || pending.isEmpty()) return
            flushing = true
            val copy = pending.toMap()
            pending.clear()
            copy
        }
        persist()
        val ok = runCatching {
            api.trackBatch(
                UsageBatchRequest(
                    installId = installId(),
                    appVersion = appVersion(context),
                    store = store,
                    sdk = Build.VERSION.SDK_INT,
                    loggedIn = loggedIn,
                    events = snapshot.entries.take(300).map { (key, count) ->
                        val (day, name) = key.split('|', limit = 2)
                        UsageBatchEvent(name = name, count = count, day = day)
                    },
                ),
            ).isSuccessful
        }.getOrDefault(false)
        synchronized(pending) {
            if (!ok) snapshot.forEach { (k, v) -> pending[k] = (pending[k] ?: 0) + v }
            flushing = false
        }
        if (!ok) persist()
    }

    private fun clean(raw: String): String? {
        val head = raw.substringBefore('/').substringBefore('?').substringBefore('{').lowercase(Locale.US)
        val safe = head.map { if (it in 'a'..'z' || it in '0'..'9' || it == '_' || it == '.') it else '_' }
            .joinToString("").trim('_').take(50)
        return safe.ifEmpty { null }
    }

    private fun installId(): String {
        val p = prefs() ?: return "unknown-install-id"
        p.getString(KEY_INSTALL_ID, null)?.let { return it }
        val id = UUID.randomUUID().toString()
        p.edit().putString(KEY_INSTALL_ID, id).apply()
        return id
    }

    @Suppress("DEPRECATION")
    private fun appVersion(context: Context): Int? = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt() else info.versionCode
    }.getOrNull()

    /** روزِ ایران، میلادی (`yyyy-MM-dd`) - همان روزی که سرور می‌شمارد. */
    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("GMT+03:30") }
        .format(Date())

    private fun persist() {
        val encoded = synchronized(pending) { pending.entries.joinToString(";") { "${it.key}=${it.value}" } }
        prefs()?.edit()?.putString(KEY_PENDING, encoded)?.apply()
    }

    private fun decode(raw: String?) {
        raw?.split(';')?.forEach { part ->
            val key = part.substringBefore('=', "")
            val value = part.substringAfter('=', "").toIntOrNull()
            if (key.contains('|') && value != null) pending[key] = (pending[key] ?: 0) + value
        }
    }

    private fun prefs() = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
