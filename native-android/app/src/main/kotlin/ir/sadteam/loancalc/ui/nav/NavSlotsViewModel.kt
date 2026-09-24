package ir.sadteam.loancalc.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.NavSuggestion
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * وضعیتِ **نوارِ پایینِ شخصی‌سازی‌شده** - بخشِ ۴۱.
 *
 * سه چیز رو کنارِ هم می‌ذاره: چیدمانِ فعلی، شمارشِ استفاده، و پیشنهادِ خودکار. خودِ شرط‌های
 * پیشنهاد تو [NavSuggestion]ِ ماژولِ `:core`ه (تستِ JVM داره) - اینجا فقط داده جمع می‌شه.
 */
@HiltViewModel
class NavSlotsViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {

    val slots: StateFlow<List<NavDestination>> = uiPrefs.navSlots
        .map { raw -> NavDestination.sanitize(raw?.split(',')?.filter { it.isNotBlank() } ?: NavDestination.DEFAULT_SLOTS) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavDestination.sanitize(NavDestination.DEFAULT_SLOTS))

    /** آیا کاربر نوار رو دستی عوض کرده - ردیفِ «نوارِ پیش‌فرض» فقط وقتی معنی داره. */
    /**
     * آیا راهنمای «نگه‌دار تا جابه‌جا کنی» زیرِ نوار نشان داده شود.
     *
     * سه بار، و بعد از اولین جابه‌جایی دیگر هیچ‌وقت — بندِ ۲ی بخشِ ۸۱.
     */
    val showReorderHint: StateFlow<Boolean> =
        combine(uiPrefs.reorderHintShownCount, uiPrefs.hasReorderedOnce) { seen, reordered ->
            !reordered && seen < 3
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** یک بار در هر اجرا شمرده می‌شود، نه در هر بازسازیِ نوار. */
    fun noteReorderHintShown() {
        viewModelScope.launch { uiPrefs.noteReorderHintShown() }
    }

    val customized: StateFlow<Boolean> = uiPrefs.navSlots
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * پیشنهادِ آماده‌ی نمایش (فریمِ `41a`) یا `null`.
     *
     * ⚠️ اینجا **فقط محاسبه** می‌شه؛ نوشتنِ «آخرین پیشنهاد» عمداً نمی‌شه تا صرفِ رندرِ کارت
     * ساعتِ ۶۰روزه رو راه نندازه. اون ساعت وقتی شروع می‌شه که کاربر جواب بده ([apply]/[dismiss]).
     */
    val suggestion: StateFlow<NavSuggestion.Result?> = combine(
        slots,
        uiPrefs.navUsage,
        uiPrefs.navUsageStartedAt,
        uiPrefs.navSuggestLastAt,
        uiPrefs.navSuggestDismissed,
    ) { slots, usage, startedAt, lastAt, dismissedRaw ->
        val now = System.currentTimeMillis()
        NavSuggestion.compute(
            slots = slots.map { it.id },
            counts = windowCounts(usage, now),
            firstDayMillis = startedAt,
            lastSuggestedAt = lastAt,
            dismissedPairs = dismissedRaw?.split(',')?.filter { it.isNotBlank() }?.toSet().orEmpty(),
            nowMillis = now,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * ثبتِ بازشدنِ یه صفحه.
     *
     * قاعده‌ی `41c`: **رویدادِ ورودِ صفحه شمرده می‌شود، نه بازگشتِ دکمه‌ی back** - جای صداکردنش
     * تو `MainActivity` همون‌جاییه که کاربر واقعاً مقصد رو انتخاب می‌کنه.
     */
    fun recordOpen(dest: NavDestination) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            uiPrefs.recordNavOpen(
                destId = dest.id,
                weekKey = NavSuggestion.weekKey(now),
                keepWeeks = NavSuggestion.windowWeeks(now).toSet(),
                nowMillis = now,
            )
        }
    }

    fun setSlots(ids: List<String>) {
        viewModelScope.launch {
            uiPrefs.setNavSlots(NavDestination.sanitize(ids).map { it.id })
            // اولین جابه‌جایی راهنما را برای همیشه برمی‌دارد (بندِ ۲ی بخشِ ۸۱).
            uiPrefs.markReordered()
        }
    }

    /** ردیفِ «نوارِ پیش‌فرض» - بی‌مودالِ تأیید (قاعده‌ی `41c`). */
    fun resetToDefault() {
        viewModelScope.launch { uiPrefs.clearNavSlots() }
    }

    fun applySuggestion(suggestion: NavSuggestion.Result) {
        viewModelScope.launch {
            uiPrefs.setNavSlots(NavSuggestion.apply(slots.value.map { it.id }, suggestion))
            uiPrefs.setNavSuggestLastAt(System.currentTimeMillis())
        }
    }

    /** «نه» - فقط همین جفت بسته می‌شه، نه کلِ قابلیت. */
    fun dismissSuggestion(suggestion: NavSuggestion.Result) {
        viewModelScope.launch {
            uiPrefs.addNavSuggestDismissed(suggestion.pairKey)
            uiPrefs.setNavSuggestLastAt(System.currentTimeMillis())
        }
    }

    /** «خودم می‌چینم» - کارت بسته می‌شه ولی جفت بسته نمی‌شه؛ ویرایشگر باز می‌شه. */
    fun snoozeSuggestion() {
        viewModelScope.launch { uiPrefs.setNavSuggestLastAt(System.currentTimeMillis()) }
    }

    private fun windowCounts(usage: String?, now: Long): Map<String, Int> {
        val window = NavSuggestion.windowWeeks(now).toSet()
        return UiPrefs.parseNavUsage(usage)
            .filterKeys { it.second in window }
            .entries
            .groupingBy { it.key.first }
            .fold(0) { acc, entry -> acc + entry.value }
    }
}
