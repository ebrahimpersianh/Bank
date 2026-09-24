package ir.sadteam.loancalc.ui.shop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.ui.background.LiveBackground
import ir.sadteam.loancalc.ui.background.LiveBackgroundState
import ir.sadteam.loancalc.ui.theme.AppFontChoice
import ir.sadteam.loancalc.ui.theme.AppFontState
import java.time.LocalDate

// ═══ سه قابلیتِ فروشگاه (۳ مهر، رفتار از `FEATURE_BEHAVIOR_SPEC.md`ِ بسته‌ی ChatGPT) ═══
// ⏳ ظاهرشان فعلاً هم‌سبکِ کارت‌های فعلی ساخته شد؛ کاربر گفت شاید بعداً بازطراحیِ ChatGPT
// بخواهد - با اسکرین‌شاتِ همین نسخه.

/** درصدِ تخفیفِ روزانه. */
const val DEAL_PERCENT = 25

/** قیمتِ تخفیفی، گرد به پایین تا مضربِ ۵ (عددِ تمیز). */
fun dealPrice(price: Int): Int = (price * (100 - DEAL_PERCENT) / 100) / 5 * 5

/**
 * قلمِ تخفیفِ امروز - برای همه‌ی کاربرها در یک روز یکی (بذر = روز)، و مستقل از مالکیت تا با
 * خریدنش قلمِ دیگری ارزان نشود (وگرنه می‌شد همه را پشتِ‌هم با تخفیف خرید).
 * فقط قلم‌های همیشگیِ پولی: نه پایه، نه نشان‌قفل، نه «به‌زودی»، نه مناسبتی.
 */
fun pickDailyDeal(items: List<ShopItem>, today: LocalDate): ShopItem? {
    val eligible = items.filter {
        !it.base && it.unlockBadge == null && !it.comingSoon && it.window == null && it.price > 0
    }.sortedBy { it.id }
    if (eligible.isEmpty()) return null
    val index = Math.floorMod(today.toEpochDay() * 2_654_435_761L, eligible.size.toLong()).toInt()
    return eligible[index]
}

data class DailyDeal(val item: ShopItem, val originalPrice: Int)

/** دسته‌هایی که «امتحان کن» دارند - فقط آن‌هایی که بی‌خطر و فوری روی کلِ برنامه دیده می‌شوند. */
fun canTry(item: ShopItem): Boolean =
    item.kind.category in setOf(ShopCategory.THEME, ShopCategory.BACKDROP, ShopCategory.FONT)

const val TRIAL_SECONDS = 10

/**
 * «امتحان کن، بعد بخر» - **فقط در حافظه**، هیچ‌وقت در تنظیماتِ ذخیره‌شده.
 *
 * 🚨 عمداً به `UiPrefs` دست نمی‌زند: اگر برنامه وسطِ امتحان بسته شود، دفعه‌ی بعد خودبه‌خود
 * همان حالتِ قبلی است و هیچ قلمِ نخریده‌ای «گیر» نمی‌کند. مالکیت و سکه هم دست نمی‌خورند.
 */
object ShopTrial {
    var item: ShopItem? by mutableStateOf(null)
        private set
    var secondsLeft by mutableIntStateOf(0)

    /** تمِ در حالِ امتحان - `MainActivity` آن را به‌جای تمِ ذخیره‌شده می‌گذارد. */
    var themeId: String? by mutableStateOf(null)
        private set

    private var prevFont: AppFontChoice? = null
    private var prevBackdrop: LiveBackground? = null
    private var touchedBackdrop = false

    fun start(target: ShopItem) {
        stop()
        item = target
        secondsLeft = TRIAL_SECONDS
        when (target.kind.category) {
            ShopCategory.THEME -> themeId = target.id.removePrefix("theme:")
            ShopCategory.FONT -> {
                prevFont = AppFontState.choice
                AppFontState.choice = AppFontChoice.fromId(target.id)
            }
            ShopCategory.BACKDROP -> {
                prevBackdrop = LiveBackgroundState.active
                touchedBackdrop = true
                LiveBackgroundState.active = LiveBackground.byId(target.id.removePrefix("bg_"))
            }
            else -> Unit
        }
    }

    fun stop() {
        themeId = null
        prevFont?.let { AppFontState.choice = it }
        prevFont = null
        if (touchedBackdrop) LiveBackgroundState.active = prevBackdrop
        touchedBackdrop = false
        prevBackdrop = null
        item = null
        secondsLeft = 0
    }
}
