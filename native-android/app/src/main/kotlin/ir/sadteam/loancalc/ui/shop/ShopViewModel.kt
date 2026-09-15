package ir.sadteam.loancalc.ui.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.coin.BuyResult
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.ui.widget.IconWither
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ═══════════ فروشگاهِ سکه ═══════════
 *
 * 🚨 **موجودی و مالکیت هیچ‌کدام شمارنده‌ی ذخیره‌شده نیستند.** موجودی جمعِ دفترِ سکه است
 * و مالکیت فهرستی از کلیدهای گونه (`theme:lapis`) در `UiPrefs` - همان قاعده‌ی
 * `CoinEconomy`. پس یک خریدِ ثبت‌شده هیچ‌وقت با موجودی ناهماهنگ نمی‌شود.
 *
 * ⚠️ خرید **دو نوشتن** دارد (کسرِ سکه در دفتر، و کلیدِ مالکیت در `UiPrefs`). کلیدِ
 * ضدِتکرارِ دفتر (`refId`) همان شناسه‌ی قلم است، پس دوبار زدن دوبار خرج نمی‌کند.
 */
@HiltViewModel
class ShopViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GamificationRepository,
    private val uiPrefs: UiPrefs,
    private val iconWither: IconWither,
) : ViewModel() {

    val balance: StateFlow<Int> = repository.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val owned: StateFlow<Set<String>> = uiPrefs.ownedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** نشان‌های گرفته‌شده - کدِ `AchievementEntity` همان `Badge.code` است. */
    val earnedBadges: StateFlow<Set<String>> = repository.achievements
        .map { list -> list.map { it.code }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * قلمِ فعالِ هر دسته.
     *
     * تم و آیکون هرکدام یک کلیدِ خودشان در `UiPrefs` دارند؛ دسته‌های دیگر هنوز مقصدی
     * ندارند (`ShopItem.comingSoon`) پس چیزی هم فعال نیست.
     */
    val active: StateFlow<Map<ShopCategory, String>> =
        combine(uiPrefs.colorTheme, uiPrefs.activeIcon) { theme, icon ->
            buildMap {
                if (theme != null) put(ShopCategory.THEME, "theme:$theme")
                if (icon != null) put(ShopCategory.ICON, icon)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** نتیجه‌ی آخرین خرید - UI بعدِ نشان‌دادنش [consumeResult] را صدا می‌زند. */
    private val _lastResult = MutableStateFlow<BuyResult?>(null)
    val lastResult: StateFlow<BuyResult?> = _lastResult

    fun consumeResult() {
        _lastResult.value = null
    }

    fun buy(item: ShopItem) {
        viewModelScope.launch {
            _lastResult.value = attemptBuy(item)
        }
    }

    private suspend fun attemptBuy(item: ShopItem): BuyResult {
        if (item.id in uiPrefs.ownedItems.first()) return BuyResult.AlreadyOwned
        item.unlockBadge?.let { code ->
            val earned = repository.achievements.first().any { it.code == code }
            if (!earned) {
                val label = Badge.entries.firstOrNull { it.code == code }?.label ?: code
                return BuyResult.BadgeLocked(label)
            }
        }
        // ⚠️ موجودی **همین لحظه** از دفتر خوانده می‌شود، نه از `StateFlow`ِ UI: بینِ
        // رندرِ ردیف و زدنِ دکمه ممکن است خرجِ دیگری ثبت شده باشد.
        val now = repository.balance.first()
        if (now < item.price) return BuyResult.NotEnough(item.price - now)

        if (item.price > 0) {
            repository.spend(item.price, item.id, GamificationRepository.Type.SPEND_THEME)
        }
        uiPrefs.addOwnedItem(item.id)
        activate(item)
        return BuyResult.Ok
    }

    fun activate(item: ShopItem) {
        viewModelScope.launch {
            when (item.kind.category) {
                ShopCategory.THEME -> uiPrefs.setColorTheme(item.id.removePrefix("theme:"))
                ShopCategory.ICON -> {
                    uiPrefs.setActiveIcon(item.id)
                    // هر چهار طرح هشت پله دارند (بخشِ ۶۱)، پس پله‌ی **واقعیِ** کاربر اعمال
                    // می‌شود نه صفر - وگرنه عوض‌کردنِ آیکون پژمردگی را پاک می‌کرد.
                    runCatching {
                        iconWither.applyFromDateKeys(context, repository.activeDayKeys(), item.id)
                    }
                }
                else -> Unit
            }
        }
    }

    /** بازگشت به آیکونِ پیش‌فرضِ «کیفِ پول» - بندِ ۵ی `60d`. */
    fun resetIcon() {
        viewModelScope.launch {
            uiPrefs.setActiveIcon(null)
            runCatching {
                iconWither.applyFromDateKeys(context, repository.activeDayKeys(), activeIcon = null)
            }
        }
    }
}
