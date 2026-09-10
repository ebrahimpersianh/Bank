package ir.sadteam.loancalc.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.data.BadgeEvaluator
import ir.sadteam.loancalc.data.BadgeProgress
import ir.sadteam.loancalc.core.StreakRepair
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.AchievementEntity
import ir.sadteam.loancalc.data.db.CoinEventEntity
import ir.sadteam.loancalc.data.prefs.UiPrefs
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** سکه و نشانِ «فعال» - رجوع کن به [GamificationRepository] برای قاعده‌های اقتصادِ سکه. */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val repository: GamificationRepository,
    private val badgeEvaluator: BadgeEvaluator,
    private val uiPrefs: UiPrefs,
) : ViewModel() {
    val coins: StateFlow<Int> = repository.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeDays: StateFlow<Int> = repository.activeDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val events: StateFlow<List<CoinEventEntity>> = repository.events
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * امروز چیزی ثبت شده یا نه - شرطِ ردیفِ «فعال» (`55a`/`56a`).
     *
     * از رویدادهای سکه خوانده می‌شه نه از تراکنش‌ها، چون `DAILY_LOG` دقیقاً به همون ثبت
     * جایزه می‌ده؛ یه تعریف، دو مصرف‌کننده (هدرِ خانه و کیفِ سکه).
     */
    val todayLogged: StateFlow<Boolean> = repository.events
        .map { list ->
            // کلید حتماً از خودِ `ActiveStreak.dateKey` بیاد - قالبش «۱۴۰۵-۰۵-۰۹»ه با صفرِ
            // پیشوند، پس ساختنِ دستیِ رشته بی‌صدا هیچ‌وقت جور درنمیاد.
            val key = ActiveStreak.dateKey(JalaliCalendar.today())
            list.any { it.dateKey == key }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** رویدادهای یک‌باره‌ی جدولِ `20e` - از جایی که واقعاً اتفاق می‌افتن صدا زده می‌شن. */
    /** خرجِ سکه - `refId` کلیدِ ضدِتکراره، پس دوبار زدنِ دکمه دوبار خرج نمی‌کنه. */
    fun spendCoins(amount: Int, refId: String) {
        viewModelScope.launch {
            repository.spend(amount, refId, ir.sadteam.loancalc.data.GamificationRepository.Type.SPEND_THEME)
        }
    }

    /** وضعیتِ نُه نشانِ `18a` - باز، در جریان (با درصد)، یا قفل. */
    val badges: StateFlow<List<BadgeProgress>> = MutableStateFlow<List<BadgeProgress>>(emptyList())
        .also { flow ->
            viewModelScope.launch { flow.value = badgeEvaluator.progressList() }
        }

    /**
     * نشان‌هایی که **همین حالا** باز شدن و باید جشن بگیرن.
     * بازشدنِ گذشته (اولین اجرا) صامته و اینجا نمیاد.
     */
    private val _justUnlocked = MutableStateFlow<List<Badge>>(emptyList())
    val justUnlocked: StateFlow<List<Badge>> = _justUnlocked

    fun refreshBadges(silent: Boolean = false) {
        viewModelScope.launch {
            val opened = badgeEvaluator.evaluate(silent = silent)
            if (opened.isNotEmpty() && !silent) _justUnlocked.value = opened
            (badges as MutableStateFlow).value = badgeEvaluator.progressList()
        }
    }

    /** نشان‌هایی که بابتِ **گذشته** باز شدن - یه‌بار، برای شیتِ جمع‌بندی. */
    private val _retroUnlocked = MutableStateFlow<List<Badge>>(emptyList())
    val retroUnlocked: StateFlow<List<Badge>> = _retroUnlocked

    /**
     * اولین اجرا: نشان‌های گذشته **صامت** باز می‌شن و جمعشون یه‌بار نشون داده می‌شه.
     * اجراهای بعدی: سنجشِ زنده، با جشن.
     */
    fun syncBadges() {
        viewModelScope.launch {
            val retroDone = uiPrefs.badgesRetroDone.first()
            val opened = badgeEvaluator.evaluate(silent = !retroDone)
            if (!retroDone) {
                uiPrefs.setBadgesRetroDone()
                if (opened.isNotEmpty()) _retroUnlocked.value = opened
            } else if (opened.isNotEmpty()) {
                _justUnlocked.value = opened
            }
            (badges as MutableStateFlow).value = badgeEvaluator.progressList()
        }
    }

    fun consumeRetro() {
        _retroUnlocked.value = emptyList()
    }

    fun consumeUnlocked() {
        _justUnlocked.value = emptyList()
    }

    /** زنجیرِ پاره‌ی قابلِ ترمیم - کارتِ خانه فقط وقتی این پر باشه دیده می‌شه. */
    private val _repairable = MutableStateFlow<StreakRepair?>(null)
    val repairable: StateFlow<StreakRepair?> = _repairable

    fun refreshRepairable() {
        viewModelScope.launch { _repairable.value = repository.repairableStreak() }
    }

    /** ترمیم با خرجِ سکه؛ بعدش کارت خودش محو می‌شه. */
    fun repairStreak() {
        viewModelScope.launch {
            repository.repairStreak()
            _repairable.value = repository.repairableStreak()
        }
    }

    fun awardOnce(type: String, amount: Int) {
        viewModelScope.launch { repository.awardOnce(type, amount) }
    }
}
