package ir.sadteam.loancalc.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * **بعد از روشن‌شدنِ دوباره‌ی گوشی، کارهای زمان‌بندی‌شده را دوباره می‌چیند.**
 *
 * خواسته‌ی صریحِ کاربر: «اگه گوشی رو خاموش روشن کردم برنامه اجرا بشه». خودِ WorkManager
 * زمان‌بندی‌اش را روی reboot نگه می‌دارد (رجوع کن به کامنتِ [ReminderScheduler])، ولی این
 * گیرنده دو کارِ دیگر می‌کند که آن مکانیزم پوششان نمی‌دهد:
 *
 * ۱. زمان‌بندی را **همان لحظه‌ی بالا آمدنِ گوشی** دوباره می‌چیند، نه با تأخیرِ نامعلومِ
 *    بازیابیِ داخلیِ WorkManager.
 * ۲. اگر کاربر یک‌بار اپ را force-stop کرده باشد، همه‌ی کارهای زمان‌بندی‌شده متوقف می‌مانند
 *    تا وقتی چیزی اپ را بیدار کند؛ این گیرنده همان بیدارکننده است.
 *
 * ⚠️ **روی شیائومی/هواوی/اوپو/ویوو تا وقتی «اجرای خودکار» (autostart) اپ دستی روشن نشده
 * باشد، این گیرنده اصلاً صدا زده نمی‌شود** - این محدودیتِ خودِ سازنده‌ی گوشی است و از داخلِ
 * برنامه قابلِ دور زدن نیست. مسیرِ روشن‌کردنش در صفحه‌ی «اجرای در پس‌زمینه»ی تنظیمات است.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderScheduler: ReminderScheduler

    @Inject lateinit var autoBackupScheduler: AutoBackupScheduler

    @Inject lateinit var comeBackScheduler: ComeBackScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in BOOT_ACTIONS) return
        // هر سه `KEEP`ـن، پس اگه از قبل زمان‌بندی سالم مانده باشد دست‌نخورده می‌ماند و این
        // فراخوانی بی‌اثر است - دوباره‌چیدن هیچ‌وقت زمان‌بندیِ موجود را ریست نمی‌کند.
        // ⚠️ نسخه‌ی suspend نیست: گیرنده‌ی Broadcast کوروتین‌اسکوپ ندارد و ساعتِ دلخواهِ
        // کاربر هم اینجا لازم نیست - اولین باری که برنامه باز شود با ساعتِ واقعی دوباره
        // چیده می‌شود (UPDATE است، نه KEEP).
        runCatching { reminderScheduler.scheduleWithDefaultHour() }
        // کانال‌ها هم همین‌جا ساخته می‌شوند تا اولین اعلانِ بعد از ری‌استارت جا نیفتد.
        runCatching { ReminderChannels.ensureAll(context) }
        runCatching { autoBackupScheduler.schedule() }
        runCatching { comeBackScheduler.schedule() }
    }

    private companion object {
        val BOOT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            // شیائومی/اچ‌تی‌سی این را به‌جای BOOT_COMPLETED می‌فرستند.
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            // بعد از آپدیتِ خودِ اپ، همه‌ی کارهای زمان‌بندی‌شده لغو می‌شوند.
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
