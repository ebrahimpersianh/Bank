package ir.sadteam.loancalc.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.notifications.AutoBackupScheduler
import ir.sadteam.loancalc.notifications.AutoBackupWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** سوییچ «پشتیبان‌گیری خودکار روزانه» تو تنظیمات - هم‌الگو با NotificationsViewModel (فقط بعد از
 * فعال/غیرفعال شدن، WorkManager رو زمان‌بندی/لغو می‌کنه). [restoreFromAutoBackup] آخرین اسنپ‌شات
 * نوشته‌شده‌ی [AutoBackupWorker] رو (اگه وجود داشته باشه) به‌جای وام/چک/حساب فعلی می‌نشونه. */
@HiltViewModel
class AutoBackupViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    private val autoBackupScheduler: AutoBackupScheduler,
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.autoBackupEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastBackupAt: StateFlow<String?> = uiPrefs.lastAutoBackupAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            if (uiPrefs.autoBackupEnabled.first()) autoBackupScheduler.schedule()
        }
    }

    fun enable() {
        viewModelScope.launch {
            uiPrefs.setAutoBackupEnabled(true)
            autoBackupScheduler.schedule()
        }
    }

    fun disable() {
        viewModelScope.launch {
            uiPrefs.setAutoBackupEnabled(false)
            autoBackupScheduler.cancel()
        }
    }

    fun restoreFromAutoBackup(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val dir = File(context.filesDir, AutoBackupWorker.BACKUP_DIR_NAME)
            val loansFile = File(dir, "loans.json")
            if (!loansFile.exists()) {
                onResult(false)
                return@launch
            }
            var ok = runCatching { loanRepository.importBackupJson(loansFile.readText()) }.getOrDefault(false)
            File(dir, "cheques.json").takeIf { it.exists() }?.let { file ->
                if (!runCatching { chequeRepository.importBackupJson(file.readText()) }.getOrDefault(false)) ok = false
            }
            File(dir, "accounts.json").takeIf { it.exists() }?.let { file ->
                if (!runCatching { accountRepository.importBackupJson(file.readText()) }.getOrDefault(false)) ok = false
            }
            onResult(ok)
        }
    }
}
