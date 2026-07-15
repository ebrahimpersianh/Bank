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
import ir.sadteam.loancalc.data.prefs.UiPrefs
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
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
    private val uiPrefs: UiPrefs,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dir = File(applicationContext.filesDir, BACKUP_DIR_NAME).apply { mkdirs() }
        File(dir, "loans.json").writeText(loanRepository.exportBackupJson())
        File(dir, "cheques.json").writeText(chequeRepository.exportBackupJson())
        File(dir, "accounts.json").writeText(accountRepository.exportBackupJson())
        uiPrefs.setLastAutoBackupAt(isoNow())
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
