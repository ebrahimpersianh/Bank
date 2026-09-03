package ir.sadteam.loancalc.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_OTHER
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * تنها ViewModelِ مسیرِ اولین ورود ([OnboardingFlow]).
 *
 * عمداً یه ViewModelِ اختصاصیه و نه استفاده از `ThemeViewModel`/`AccountViewModel`ِ موجود: اون‌ها
 * به گرافِ صفحه‌ی اصلی وابسته‌ان و مسیرِ آنبوردینگ **قبل از** گیتِ ورود اجرا می‌شه.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    private val accountRepository: AccountRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    fun setThemeMode(mode: String) {
        viewModelScope.launch { uiPrefs.setThemeMode(mode) }
    }

    /** یادآورِ روزانه‌ی ثبتِ دخل‌وخرج - مرحله‌ی سومِ آنبوردینگ. مجوزِ اعلان جدا و تو خودِ UI گرفته
     * می‌شه؛ اینجا فقط سوییچ و زمان‌بندی ذخیره می‌شه. */
    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            uiPrefs.setDailyExpenseReminderEnabled(enabled)
            // یادآورِ روزانه بخشی از همون DueDateReminderWorkerه، پس فقط زمان‌بندیِ مشترک روشن
            // می‌شه؛ cancel اینجا عمداً نیست (سررسیدها هم به همین worker وابسته‌ان).
            if (enabled) {
                uiPrefs.setNotificationsEnabled(true)
                reminderScheduler.schedule()
            }
        }
    }

    /** مرحله‌ی **پنجم** (نه چهارم - مرحله‌ی مجوزهای بانکیِ `35a` بینشان اضافه شد): اولین «حساب‌کتاب». [bankName] خالی یعنی منبعِ غیربانکی (نقدی، کیفِ پول…). */
    fun addFirstAccount(name: String, bankName: String, initialBalanceRial: Double, iconKey: String?) {
        viewModelScope.launch {
            val isBank = bankName.isNotBlank()
            accountRepository.addAccount(
                name = name,
                bankName = bankName,
                initialBalance = initialBalanceRial,
                type = if (isBank) ACCOUNT_TYPE_BANK else ACCOUNT_TYPE_OTHER,
                iconKey = if (isBank) null else iconKey,
            )
        }
    }
}
