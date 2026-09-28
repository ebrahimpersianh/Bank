package ir.sadteam.loancalc.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * پشتیبان‌گیری خودکار روزانه (پورت مفهومی اپ رقیب VAMMAN) - هر روز یه اسنپ‌شات تازه از وام/چک/حساب
 * رو تو فضای داخلی اپ (`context.filesDir/auto_backups`، نه SAF - چون بدون تعامل کاربر اجرا می‌شه و
 * نباید پرامپت انتخاب فایل نشون بده) می‌نویسه؛ هر بار جایگزین اسنپ‌شات قبلی می‌شه (یه نسخه‌ی واحد،
 * نه آرشیو تاریخچه‌ای). AutoBackupViewModel.restoreFromAutoBackup همین فایل‌ها رو برای بازیابی
 * می‌خونه.
 *
 * علاوه بر اسنپ‌شات محلی، اگه کاربر لاگین و مشترک باشه، همون سه‌تا رو به سرور هم آپلود می‌کنه
 * (LoanRepository/ChequeRepository/AccountRepository.pushToServer) - قبلاً این اسنپ‌شات فقط رو
 * خودِ گوشی می‌موند، یعنی با گم‌شدن/خرابیِ گوشی از بین می‌رفت با اینکه «پشتیبان‌گیری ابری» تو
 * صفحه‌ی امکاناتِ قدیمی (حذف‌شده) به‌عنوان مزیت اشتراک تبلیغ شده بود.
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
    private val uiPrefs: UiPrefs,
    private val authPrefs: AuthPrefs,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dir = File(applicationContext.filesDir, BACKUP_DIR_NAME).apply { mkdirs() }
        // 🚨 **اول هر سه فایلِ موقت، بعد جابه‌جاییِ یک‌جا.** اگر پروسه وسطِ نوشتن کشته
        // می‌شد، پوشه‌ی پشتیبان ترکیبی از وامِ امروز و حسابِ دیروز می‌ماند - نسخه‌ای که
        // هیچ‌وقت وجود نداشته و بازگرداندنش داده را خراب می‌کند.
        val staged = listOf(
            "loans.json" to loanRepository.exportBackupJson(),
            "cheques.json" to chequeRepository.exportBackupJson(),
            "accounts.json" to accountRepository.exportBackupJson(),
        ).map { (name, json) ->
            val tmp = File(dir, "$name.tmp")
            tmp.writeText(json)
            tmp to File(dir, name)
        }
        staged.forEach { (tmp, target) ->
            if (target.exists()) target.delete()
            tmp.renameTo(target)
        }
        uiPrefs.setLastAutoBackupAt(isoNow())

        val token = authPrefs.authToken.first()
        val subscribed = authPrefs.subscribed.first()
        if (token != null && subscribed) {
            // ⚠️ «پشتیبان گرفته شد» فقط وقتی درست است که آپلود هم انجام شده باشد. نتیجه
            // جدا ثبت می‌شود تا صفحه‌ی تنظیمات بتواند صادقانه بگوید ابری شکست خورده.
            val cloudOk = loanRepository.pushToServer(token) &&
                chequeRepository.pushToServer(token) &&
                accountRepository.pushToServer(token)
            uiPrefs.setCloudBackupResult(if (cloudOk) isoNow() else null)
            // شکستِ آپلود یعنی «دوباره تلاش کن»، نه «کار تمام شد». پشتیبانِ محلی از قبل
            // نوشته شده، پس تلاشِ دوباره چیزی را خراب نمی‌کند.
            if (!cloudOk) return Result.retry()
        }

        return Result.success()
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }

    companion object {
        const val BACKUP_DIR_NAME = "auto_backups"
    }
}
