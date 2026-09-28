package ir.sadteam.loancalc.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.room.withTransaction
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.AppDatabase
import ir.sadteam.loancalc.data.prefs.AuthPrefs
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
    private val authPrefs: AuthPrefs,
    private val database: AppDatabase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.autoBackupEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastBackupAt: StateFlow<String?> = uiPrefs.lastAutoBackupAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** `true` یعنی نسخه‌ی محلی ساخته شد ولی **آپلودِ ابری شکست خورد** - باید صریح گفته شود. */
    val cloudBackupFailed: StateFlow<Boolean> = uiPrefs.lastCloudBackupFailed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    /**
     * 🚨 **اول همه‌چیز خوانده و اعتبارسنجی می‌شود، بعد یک‌جا نوشته** (یافته‌ی بازبینی،
     * ۳۱ شهریور). تا امروز وام، چک و حساب پشتِ‌هم و مستقل برگردانده می‌شدند: اگر وسطِ
     * کار چیزی می‌شکست، وامِ نسخه‌ی تازه کنارِ حساب‌های قدیمی می‌نشست - ترکیبی که هیچ‌وقت
     * وجود نداشته. حالا هر سه داخلِ **یک تراکنشِ دیتابیس** می‌روند و شکستِ هرکدام همه را
     * برمی‌گردانَد سرِ جای اولش.
     */
    fun restoreFromAutoBackup(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val dir = File(context.filesDir, AutoBackupWorker.BACKUP_DIR_NAME)
            val loansJson = File(dir, "loans.json").takeIf { it.exists() }?.readText()
            if (loansJson == null) {
                onResult(false)
                return@launch
            }
            val chequesJson = File(dir, "cheques.json").takeIf { it.exists() }?.readText()
            val accountsJson = File(dir, "accounts.json").takeIf { it.exists() }?.readText()
            onResult(applyAll(loansJson, chequesJson, accountsJson))
        }
    }

    /**
     * بازیابی از سرورِ ابری. **اول هر سه بسته دانلود می‌شوند**؛ اگر حتی یکی نیامد، هیچ
     * چیزی روی دیتابیس نوشته نمی‌شود - وگرنه قطعِ اینترنت وسطِ کار همان حالتِ ترکیبیِ
     * خطرناک را می‌ساخت.
     *
     * ⚠️ نتیجه‌ی قبلی `loansOk || chequesOk || accountsOk` بود: بازیابیِ نصفه‌نیمه هم
     * «موفق» اعلام می‌شد. حالا یا همه یا هیچ.
     */
    fun restoreFromCloud(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = authPrefs.authToken.first()
            if (token.isNullOrEmpty()) {
                onResult(false)
                return@launch
            }
            val loansJson = loanRepository.fetchServerBackupJson(token)
            val chequesJson = chequeRepository.fetchServerBackupJson(token)
            val accountsJson = accountRepository.fetchServerBackupJson(token)
            if (loansJson == null || chequesJson == null || accountsJson == null) {
                onResult(false)
                return@launch
            }
            onResult(applyAll(loansJson, chequesJson, accountsJson))
        }
    }

    /** نوشتنِ هر سه بسته در یک تراکنش؛ `false` یعنی هیچ‌چیز عوض نشد. */
    private suspend fun applyAll(loansJson: String, chequesJson: String?, accountsJson: String?): Boolean =
        runCatching {
            database.withTransaction {
                if (!loanRepository.importBackupJson(loansJson)) error("loans")
                if (chequesJson != null && !chequeRepository.importBackupJson(chequesJson)) error("cheques")
                if (accountsJson != null && !accountRepository.importBackupJson(accountsJson)) error("accounts")
                true
            }
        }.getOrDefault(false)
}
