package ir.sadteam.loancalc

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.ImageRequest
import dagger.hilt.android.HiltAndroidApp
import ir.sadteam.loancalc.crash.CrashReporter
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.LoanDataChange
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.creditServices
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

    override fun onCreate() {
        super.onCreate()
        crashReporter.install()
        preloadLogoAssets()
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
                if (event == Lifecycle.Event.ON_STOP) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            iconWither.applyFromDateKeys(
                                this@LoanCalcApplication,
                                gamificationRepository.activeDayKeys(),
                                uiPrefs.activeIcon.first(),
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

    // خواسته‌ی کاربر: لوگوهای بانک/خدمات اعتباری (assets/banks, assets/services - فایل‌های چندکیلوبایتی)
    // «خیلی اوقات چند ثانیه دیر لود می‌شن». چون این فایل‌ها محلی و کوچیکن، تاخیر از شبکه نیست - از اینه
    // که Coil اولین باری که هر لوگو رو تو BankLoanScreen می‌بینه، تازه شروع به decode می‌کنه. اینجا همه‌ی
    // لوگوها همون لحظه‌ی باز شدنِ اپ (هم‌زمان با اسپلش، قبل از این‌که کاربر به تبِ «وام بانکی» برسه) با
    // enqueue تو کشِ حافظه‌ی Coil گرم می‌شن، پس وقتی واقعاً رو صفحه نشون داده می‌شن دیگه چیزی برای
    // decode‌کردن نمونده - رجوع کن به CLAUDE.md.
    private fun preloadLogoAssets() {
        // عمداً Coil.imageLoader(this) (نه newImageLoader() مستقیم) - وگرنه یه ImageLoaderِ جدا و
        // بی‌ربط به singletonِ واقعی می‌سازه که کشش رو AsyncImageهای بقیه‌ی اپ به اشتراک گذاشته
        // نمی‌شه و کل هدفِ پیش‌گرم‌کردن بی‌اثر می‌مونه.
        val imageLoader = Coil.imageLoader(this)
        (banks + creditServices).forEach { entry ->
            imageLoader.enqueue(
                ImageRequest.Builder(this)
                    .data("file:///android_asset/${entry.logoAsset}")
                    .build(),
            )
        }
    }

    // کراس‌فیدِ سراسری (۲۰۰ms) رو همه‌ی AsyncImageهای اپ (لوگوی بانک/خدمات، عکسِ رسیدِ چک/وام) - قبلاً
    // تنظیمِ خاصی نبود، پس تصویر یهو «پاپ» می‌کرد؛ الان حتی اگه یه لحظه دیر برسه، محو ظاهر می‌شه، نه یهو.
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(200)
        .build()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
