package ir.sadteam.loancalc

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.crash.CrashReporter
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.LoanDataChange
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.notifications.ComeBackScheduler
import ir.sadteam.loancalc.ui.auth.SmsRetrieverHash
import ir.sadteam.loancalc.ui.widget.IconWither
import ir.sadteam.loancalc.ui.widget.LoanWidget
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class LoanCalcApplication : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    lateinit var comeBackScheduler: ComeBackScheduler

    @Inject
    lateinit var iconWither: IconWither

    @Inject
    lateinit var gamificationRepository: GamificationRepository

    @Inject
    lateinit var uiPrefs: UiPrefs

    @Inject
    lateinit var authPrefs: AuthPrefs

    @Inject
    lateinit var accountRepository: ir.sadteam.loancalc.data.AccountRepository

    @Inject
    lateinit var database: ir.sadteam.loancalc.data.db.AppDatabase

    override fun onCreate() {
        super.onCreate()
        crashReporter.install()
        ir.sadteam.loancalc.data.UsageStats.init(this, BuildConfig.FLAVOR)
        ir.sadteam.loancalc.data.UsageStats.profileProvider = { buildUsageProfile() }
        // آمار فقط «واردشده یا نه» را می‌خواهد، نه اینکه چه کسی.
        CoroutineScope(Dispatchers.IO).launch {
            authPrefs.authToken.collect { ir.sadteam.loancalc.data.UsageStats.loggedIn = it != null }
        }
        // برای هماهنگ‌کردنِ پترنِ پیامکِ OTP با SMS Retriever API - رجوع کن به کامنتِ
        // SmsRetrieverHash.kt. فقط لاگ می‌کنه (Log.i)، هیچ اثرِ دیگه‌ای رو رفتارِ اپ نداره.
        SmsRetrieverHash.logForDebugging(this)
        // اعلانِ «X روزه تراکنش ثبت نکردی» - اینجا زمان‌بندی می‌شه (نه تو یه ViewModelِ صفحه‌ی
        // تنظیمات) چون نباید به بازکردنِ اون صفحه وابسته باشه. خودِ Worker قبل از هر اعلان
        // پرچمِ comeBackReminderEnabled رو چک می‌کنه، پس زمان‌بندیِ بی‌قیدش بی‌ضرره.
        comeBackScheduler.schedule()
        // قلابِ «داده‌ی وام عوض شد» → تازه‌کردنِ ویجت. `:data` خودِ ویجت را نمی‌بیند، پس
        // این‌جا پُر می‌شود - رجوع کن به [LoanDataChange]. بی این، ویجت تا شش ساعت عددِ
        // کهنه نشان می‌دهد.
        // **پژمردگیِ آیکون** (`49b`) - لحظه‌ی اعمالش **رفتنِ اپ به پس‌زمینه** است نه باز
        // شدنش: کاربر موقعِ باز کردن دقیقاً به آیکون نگاه می‌کند و پرشِ آیکون زیرِ انگشتش
        // دیده می‌شود (قاعده‌ی صریحِ `49`).
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    ir.sadteam.loancalc.data.UsageStats.onForeground()
                    // دو گوشی: تازه‌ترین نسخه‌ی ابری (اگر گوشیِ دیگری نوشته) بیاید.
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching { authPrefs.authToken.first()?.let { accountRepository.pullIfNewer(it); ir.sadteam.loancalc.data.PhotoSync.sync(this@LoanCalcApplication, it) } }
                    }
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    // همه‌ی تغییرها (یادداشت، بودجه، دارایی…) با رفتن به پس‌زمینه روی سرور می‌روند.
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching { authPrefs.authToken.first()?.let { accountRepository.pushToServer(it); ir.sadteam.loancalc.data.PhotoSync.sync(this@LoanCalcApplication, it) } }
                    }
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    ir.sadteam.loancalc.data.UsageStats.onBackground()
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            // 🚨 **آیکونِ ناشناخته پاک می‌شود، نه اینکه اعمال شود.**
                            // «نشانِ رشد» (`icon:emblem`) به‌خاطرِ کیفیتِ پایینِ فایلِ هنری
                            // حذف شد؛ کسی که در بیلدهای ۶۱۵..۶۲۲ فعالش کرده بود، الیاسش
                            // دیگر وجود ندارد و بی این خط، آیکونِ اپ از صفحه‌ی گوشی
                            // ناپدید می‌مانْد. `aliasFor` خودش به پیش‌فرض برمی‌گردد، ولی
                            // ترجیحِ ذخیره‌شده هم باید پاک شود وگرنه هر بار تکرار می‌شود.
                            val active = uiPrefs.activeIcon.first()
                            val known = active == null || IconWither.ICON_ALIAS.containsKey(active)
                            if (!known) uiPrefs.setActiveIcon(null)
                            // 🚨 **امروز را همین‌جا «دیده‌شده» ثبت می‌کنیم.**
                            // پژمردگی تا امروز از دفترِ سکه می‌خواند و آن دفتر فقط با
                            // **ثبتِ تراکنش** پر می‌شود؛ کاربری که ده بار در روز برنامه را
                            // باز می‌کرد ولی خرجی ثبت نمی‌کرد آیکونِ پژمرده می‌دید. سکه
                            // دست‌نخورده مانْد و فقط آیکون مبنای درستش را گرفت.
                            val today = ActiveStreak.dateKey(JalaliCalendar.today())
                            uiPrefs.setLastSeenDay(today)
                            iconWither.applyFromDateKeys(
                                this@LoanCalcApplication,
                                gamificationRepository.activeDayKeys() + today,
                                if (known) active else null,
                            )
                        }
                    }
                }
            },
        )
        LoanDataChange.onChanged = {
            CoroutineScope(Dispatchers.Main).launch { LoanWidget.updateAll(this@LoanCalcApplication) }
        }
    }

    // کراس‌فیدِ سراسری (۲۰۰ms) رو همه‌ی AsyncImageهای اپ (لوگوی بانک/خدمات، عکسِ رسیدِ چک/وام) - قبلاً
    // تنظیمِ خاصی نبود، پس تصویر یهو «پاپ» می‌کرد؛ الان حتی اگه یه لحظه دیر برسه، محو ظاهر می‌شه، نه یهو.
    /**
     * مشخصاتِ بی‌نامِ این نصب برای «گزارشِ برنامه» - روزی یک بار. **فقط** مدل/نسخه/حالت و
     * **تعدادِ** موارد؛ هیچ مبلغ، اسم، متن، شماره یا شناسه‌ی گوشی (قاعده‌ی کاربر، ۷ مهر).
     */
    private suspend fun buildUsageProfile(): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        val ctx = this
        out["device_brand"] = android.os.Build.MANUFACTURER.orEmpty().lowercase().take(30)
        out["device_model"] = android.os.Build.MODEL.orEmpty().take(40)
        out["android"] = android.os.Build.VERSION.RELEASE.orEmpty().take(10)
        val cfg = resources.configuration
        out["screen_dp"] = "${cfg.screenWidthDp}x${cfg.screenHeightDp}"
        out["lang"] = cfg.locales[0]?.language.orEmpty().take(8)
        out["font_scale_sys"] = String.format(java.util.Locale.US, "%.2f", cfg.fontScale)
        out["system_dark"] = ((cfg.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES).toString()
        fun granted(perm: String) = androidx.core.content.ContextCompat.checkSelfPermission(ctx, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        out["perm_notifications"] = androidx.core.app.NotificationManagerCompat.from(ctx).areNotificationsEnabled().toString()
        out["perm_sms"] = granted(android.Manifest.permission.RECEIVE_SMS).toString()
        out["perm_calendar"] = granted(android.Manifest.permission.WRITE_CALENDAR).toString()
        out["notif_listener"] = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(ctx)
            .contains(packageName).toString()
        out["battery_unrestricted"] = (getSystemService(POWER_SERVICE) as? android.os.PowerManager)
            ?.isIgnoringBatteryOptimizations(packageName)?.toString() ?: "?"
        runCatching {
            out["theme_mode"] = uiPrefs.themeMode.first()
            out["color_theme"] = uiPrefs.colorTheme.first() ?: "default"
            out["font_scale_app"] = uiPrefs.fontScale.first().toString()
            out["privacy_mode"] = uiPrefs.privacyModeEnabled.first().toString()
            out["sms_import"] = uiPrefs.smsAutoImportEnabled.first().toString()
            out["notif_import"] = uiPrefs.notifAutoImportEnabled.first().toString()
            out["reminders"] = uiPrefs.notificationsEnabled.first().toString()
            out["reminder_hour"] = uiPrefs.reminderHour.first().toString()
            out["daily_reminder"] = uiPrefs.dailyExpenseReminderEnabled.first().toString()
            out["auto_backup"] = uiPrefs.autoBackupEnabled.first().toString()
            out["vibration"] = uiPrefs.vibrationEnabled.first().toString()
            out["reduced_motion"] = uiPrefs.reducedMotion.first().toString()
            out["owned_themes"] = uiPrefs.ownedThemes.first().size.toString()
            out["owned_items"] = uiPrefs.ownedItems.first().size.toString()
        }
        runCatching {
            val sec = ir.sadteam.loancalc.data.prefs.SecurityPrefs(ctx)
            out["lock"] = (sec.pinHash.first() != null).toString()
            out["biometric"] = sec.biometricEnabled.first().toString()
        }
        runCatching { out["subscription"] = authPrefs.subscriptionTier.first() ?: "none" }
        // فقط **تعداد** ردیف‌ها - برای «کدام بخش واقعاً استفاده می‌شود».
        val tables = listOf(
            "loans", "cheques", "cheque_books", "accounts", "account_transactions", "budgets", "assets",
            "asset_trades", "debts", "counterparties", "notes", "incomes", "recurring_payments",
            "savings_goals", "tx_templates", "bills", "parsing_rules", "custom_categories", "dang_events",
            "inbox_messages", "calculation_history", "achievements",
        )
        runCatching {
            val db = database.openHelper.readableDatabase
            tables.forEach { t ->
                runCatching {
                    db.query("SELECT COUNT(*) FROM `$t`").use { c -> if (c.moveToFirst()) out["n_$t"] = c.getLong(0).toString() }
                }
            }
            runCatching {
                // ثبت‌شده‌ی خودکار از پیامک/اعلانِ بانک (برچسبِ منبع دارد).
                db.query("SELECT COUNT(*) FROM account_transactions WHERE originLabel IS NOT NULL")
                    .use { c -> if (c.moveToFirst()) out["n_tx_auto"] = c.getLong(0).toString() }
            }
        }
        return out
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(200)
        .build()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
