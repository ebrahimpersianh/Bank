package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * سوییچِ «خوندنِ خودکارِ پیامکِ بانکی» تو تنظیمات - مجوزِ RECEIVE_SMS خودِ UI (کامپوزبلِ
 * SettingsScreen، چون به Activity نیاز داره) درخواست می‌کنه؛ اینجا فقط بعدِ گرفتنِ مجوز صدا زده
 * می‌شه تا تنظیم تو DataStore ذخیره بشه - رجوع کن به BankSmsReceiver که واقعاً این پرچم رو چک
 * می‌کنه.
 */
@HiltViewModel
class SmsAutoImportViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    private val gamification: GamificationRepository,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.smsAutoImportEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastImportAt: StateFlow<String?> = uiPrefs.lastSmsImportAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun enable() {
        viewModelScope.launch {
            uiPrefs.setSmsAutoImportEnabled(true)
            // «وصل‌کردنِ پیامکِ بانکی ۷۵ سکه» (جدولِ `20e`) - یک‌باره‌ست، پس خاموش/روشنِ دوباره
            // سکه‌ی تازه نمی‌ده (یگانگی رو خودِ نوعِ رویداد).
            gamification.awardOnce(
                GamificationRepository.Type.CONNECT_SMS,
                GamificationRepository.Reward.CONNECT_SMS,
            )
        }
    }

    fun disable() {
        viewModelScope.launch { uiPrefs.setSmsAutoImportEnabled(false) }
    }

    /** سوییچِ خوندنِ خودکارِ **اعلانِ** بانکی - رجوع کن به BankNotificationListener. برخلافِ
     * پیامک، مجوزش دیالوگِ Runtime نداره و کاربر باید دستی از تنظیماتِ گوشی بده؛ این پرچم فقط
     * خواستِ خودِ کاربره، نه وضعیتِ واقعیِ مجوز. */
    val notifEnabled: StateFlow<Boolean> = uiPrefs.notifAutoImportEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * بسته‌نامِ اپ‌هایی که کاربر انتخاب کرده اعلانشون خونده بشه.
     *
     * ⚠️ **بدونِ حداقل یه اپ تو این لیست، قابلیت کاملاً بی‌اثره** - `BankNotificationListener`
     * هر اعلانی که بسته‌نامش اینجا نباشه رو بی‌صدا دور می‌ندازه. تا قبل از این، هیچ نقطه‌ای تو
     * اپ این لیست رو **نمی‌نوشت**، پس همیشه خالی می‌موند و خوندنِ اعلان هیچ‌وقت کار نمی‌کرد
     * (گزارشِ کاربر: «نوتیفیکیشن رو از بلوبانک نمی‌خوند»).
     */
    val notifPackages: StateFlow<Set<String>> = uiPrefs.notifAutoImportPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setNotifPackageSelected(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            val current = uiPrefs.notifAutoImportPackages.first()
            val next = if (selected) current + packageName else current - packageName
            uiPrefs.setNotifAutoImportPackages(next)
        }
    }

    /** برچسبِ اشتراک‌هایی که کاربر تو «اشتراک‌یاب» نادیده گرفته - رجوع کن به
     * [ir.sadteam.loancalc.ui.accounting.SubscriptionFinderScreen]. */
    val ignoredSubscriptions: StateFlow<Set<String>> = uiPrefs.ignoredSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun ignoreSubscription(label: String) {
        viewModelScope.launch {
            uiPrefs.setIgnoredSubscriptions(uiPrefs.ignoredSubscriptions.first() + label)
        }
    }

    fun setNotifEnabled(value: Boolean) {
        viewModelScope.launch {
            uiPrefs.setNotifAutoImportEnabled(value)
            if (value) {
                gamification.awardOnce(
                    GamificationRepository.Type.CONNECT_NOTIFICATION,
                    GamificationRepository.Reward.CONNECT_NOTIFICATION,
                )
            }
        }
    }
}
