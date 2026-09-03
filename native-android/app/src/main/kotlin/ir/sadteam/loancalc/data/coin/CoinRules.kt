package ir.sadteam.loancalc.data.coin

import java.time.LocalDate

/**
 * ═══════════════ اقتصادِ سکه — مدل و قاعده‌ها ═══════════════
 *
 * فریمِ `45d`. سه قاعده‌ای که کلِ فایل را شکل می‌دهند:
 *
 * ۱ · **هیچ کاری بی سقف نیست.** ثبتِ پنجاه تراکنشِ الکی حداکثر ۳۰ سکه می‌دهد — همان مقدار
 *     که با سه ثبتِ واقعی هم می‌گرفت. پس انگیزه‌ی ثبتِ الکی صفر است، بی این‌که لازم باشد
 *     «الکی بودن» را تشخیص بدهیم (که نمی‌شود).
 *
 * ۲ · **پنج قلم هیچ مسیرِ زمانی ندارند** — آیکونِ برنامه، مجموعه‌ی نمادها، نمادِ سکه، و دو
 *     تمِ مناسبتی. با صبر نمی‌آیند، فقط با خرج. این ستونِ فقراتِ انگیزه است: اگر همه‌چیز با
 *     گذشتِ وقت هم بیاید، سکه بی‌ارزش می‌شود.
 *
 * ۳ · **دفتر منبعِ حقیقت است.** موجودی از جمعِ ردیف‌های [CoinEvent] بازسازی می‌شود، نه یک
 *     فیلدِ `balance` که کم و زیاد شود. با همگام‌سازیِ چند دستگاه از هم نمی‌پاشد: دو دستگاه
 *     که هر کدام یک ردیف نوشته‌اند، بعد از سینک هر دو ردیف را دارند و جمع درست است. با
 *     فیلدِ شمارشی، آخرین نوشتن برنده می‌شد و یکی از دو کسب گم می‌شد.
 */

/** یک ردیفِ دفتر. تنها چیزی که ذخیره می‌شود؛ موجودی محاسبه است. */
data class CoinEvent(
    val id: Long = 0,
    val kind: String,
    /** مثبت برای کسب، منفی برای خرج. */
    val amount: Int,
    val at: Long,
    /**
     * شناسه‌ی چیزی که سکه بابتش داده شد — `"txn:۱۴۰۳"`, `"badge:CLEAN_DESK"`, `"theme:blue"`.
     *
     * ⚠️ برای کسب‌های **یک‌بار در عمر** باید یکتا باشد و در دیتابیس `UNIQUE` بگیرد: تنها
     * دفاعِ واقعی در برابرِ سکه‌ی دوباره بابتِ یک نشان، بعد از پاک‌کردن و برگرداندنِ پشتیبان.
     */
    val ref: String? = null,
)

/**
 * هفت کارِ سکه‌دار.
 *
 * [perDay] سقفِ **تعدادِ دفعه** در روز است، نه سقفِ سکه. `null` یعنی روزانه سقف ندارد و
 * [period] محدودش می‌کند.
 */
enum class CoinEarn(
    val kind: String,
    val label: String,
    val amount: Int,
    val perDay: Int?,
    val period: Period,
) {
    TRANSACTION("txn", "ثبتِ تراکنش", 10, perDay = 3, period = Period.DAILY),
    DAILY_OPEN("open", "ورودِ روزانه", 5, perDay = 1, period = Period.DAILY),
    SMS_CONFIRM("sms", "تاییدِ حدسِ پیامکِ بانک", 3, perDay = 5, period = Period.DAILY),
    FULL_WEEK("week", "هفته‌ی کامل", 50, perDay = null, period = Period.WEEKLY),
    ON_TIME_PAYMENT("ontime", "پرداختِ قسط یا چک سرِ موعد", 20, perDay = null, period = Period.PER_ITEM),
    UNDER_BUDGET("budget", "ماندن زیرِ بودجه‌ی ماه", 120, perDay = null, period = Period.MONTHLY),
    BADGE("badge", "نشانِ دستاورد", 0, perDay = null, period = Period.ONCE_EVER);

    enum class Period { DAILY, WEEKLY, MONTHLY, PER_ITEM, ONCE_EVER }
}

/**
 * سقفِ سکه‌ی روزانه: ۳۰ (ثبت) + ۵ (ورود) + ۱۵ (تایید) = **۵۰**.
 *
 * عددِ ثابت است و از جمعِ [CoinEarn] حساب **نمی‌شود** — عمداً. اگر روزی کارِ روزانه‌ی
 * تازه‌ای اضافه شد، سقف نباید خودکار بالا برود؛ باید تصمیمِ آگاهانه باشد.
 */
const val DAILY_COIN_CAP = 50

/** نرخِ سکه‌ی هر نشان. جمعشان ۷۵۰ است و یک‌بار در عمر. */
val BADGE_COINS: Map<String, Int> = mapOf(
    "FIRST_STEP" to 25,
    "BUDGETER" to 75,
    "FULL_WEEK" to 50,
    "CAUTIOUS" to 75,
    "UNDER_BUDGET" to 100,
    "CLEAN_DESK" to 75,
    "GOAL_REACHED" to 150,
    "LOAN_CLOSED" to 150,
    "STEADY_MONTH" to 50,
)

/** فهرستِ خرج. `timeless = true` یعنی هیچ مسیرِ زمانی ندارد (قاعده‌ی ۲). */
enum class CoinSpend(
    val kind: String,
    val label: String,
    val price: Int,
    val timeless: Boolean,
) {
    THEME("theme", "تمِ رنگی", 150, timeless = false),
    THEME_RARE("theme_rare", "تمِ مناسبتیِ کمیاب", 300, timeless = true),
    APP_ICON("icon", "آیکونِ برنامه", 400, timeless = true),
    CATEGORY_ICONS("icons", "مجموعه‌ی نمادِ دسته‌ها", 250, timeless = true),
    COIN_SKIN("coin", "نمادِ سکه", 200, timeless = true),
    STREAK_REPAIR("repair", "ترمیمِ زنجیره", 100, timeless = false),
    TRIAL_3("trial3", "اشتراکِ جایزه ۳ روزه", 1200, timeless = false),
    TRIAL_7("trial7", "اشتراکِ جایزه ۷ روزه", 2500, timeless = false),
}

/** خروجیِ تلاش برای خرید. */
sealed interface BuyResult {
    data object Ok : BuyResult
    data object AlreadyOwned : BuyResult
    /** قیمت ندارد؛ فقط با نشان باز می‌شود (مثلِ `AGED_GOLD`). */
    data class BadgeLocked(val badge: String) : BuyResult
    /** [short] چند سکه کم است — در پیام `toFa` می‌شود. */
    data class NotEnough(val short: Int) : BuyResult
    /** کمیابِ منقضی‌شده. از فروشگاه رفته و برنمی‌گردد. */
    data object Expired : BuyResult
}

/**
 * محاسبه‌های خالصِ دفتر. هیچ I/O ندارد، پس تست‌پذیر است و از ViewModel و از Worker هر دو
 * صدا زده می‌شود.
 */
object CoinRules {

    fun balanceOf(ledger: List<CoinEvent>): Int = ledger.sumOf { it.amount }

    /** جمعِ کسبِ امروز — فقط ردیف‌های مثبت. خرج در سقفِ روزانه حساب نمی‌شود. */
    fun earnedToday(ledger: List<CoinEvent>, startOfDay: Long): Int =
        ledger.filter { it.at >= startOfDay && it.amount > 0 }.sumOf { it.amount }

    fun remainingToday(ledger: List<CoinEvent>, startOfDay: Long): Int =
        (DAILY_COIN_CAP - earnedToday(ledger, startOfDay)).coerceAtLeast(0)

    /**
     * سکه‌ی واقعیِ یک کسب، بعدِ اعمالِ سه سقف.
     *
     * ⚠️ خروجیِ صفر **خطا نیست** و نباید پیامِ خطا بدهد: یعنی «امروز سهمت را گرفتی».
     * نوارِ سقفِ `45a` جای توضیحش است.
     *
     * سقفِ دفعه‌ای (`perDay`) قبل از سقفِ سکه‌ای اعمال می‌شود، و در آخر عدد به
     * [remainingToday] بریده می‌شود — پس ثبتِ سومِ کسی که ۴۵ سکه دارد، ۵ سکه می‌گیرد نه ۱۰
     * و نه صفر.
     */
    fun award(
        earn: CoinEarn,
        ledger: List<CoinEvent>,
        startOfDay: Long,
        ref: String? = null,
        badgeKey: String? = null,
    ): Int {
        if (earn == CoinEarn.BADGE) {
            val key = badgeKey ?: return 0
            // یک‌بار در عمر — با `ref` یکتا، نه با شمارشِ امروز.
            if (ledger.any { it.ref == "badge:$key" }) return 0
            // نشان از سقفِ روزانه **مستثنی است**: دستاوردِ یک‌بار در عمر نباید به‌خاطرِ
            // ثبتِ همان روز نصفه داده شود، و کاربر هم نمی‌تواند تعدادش را زیاد کند.
            return BADGE_COINS[key] ?: 0
        }
        earn.perDay?.let { cap ->
            val usedToday = ledger.count { it.at >= startOfDay && it.kind == earn.kind }
            if (usedToday >= cap) return 0
        }
        if (earn.period == CoinEarn.Period.PER_ITEM && ref != null) {
            if (ledger.any { it.ref == ref }) return 0
        }
        return earn.amount.coerceAtMost(remainingToday(ledger, startOfDay))
    }

    fun canBuy(
        spend: CoinSpend,
        balance: Int,
        owned: Set<String>,
        itemId: String,
        unlockBadge: String? = null,
        unlockedBadges: Set<String> = emptySet(),
        rareWindowOpen: Boolean = true,
    ): BuyResult = when {
        itemId in owned -> BuyResult.AlreadyOwned
        unlockBadge != null && unlockBadge !in unlockedBadges -> BuyResult.BadgeLocked(unlockBadge)
        !rareWindowOpen -> BuyResult.Expired
        balance < spend.price -> BuyResult.NotEnough(spend.price - balance)
        else -> BuyResult.Ok
    }

    /**
     * ماهِ کاربرِ منظم ~۹۰۰ سکه است (۳۰ روز × ~۳۰). با این نرخ:
     * یک تم در ماه، یا **سه ماه صرفه‌جویی** برای اشتراکِ هفت‌روزه.
     */
    fun monthlyEstimate(activeDaysPerWeek: Int): Int =
        (activeDaysPerWeek * 4) * 30 + 50 * 4
}

/** شروعِ روزِ محلی به میلی‌ثانیه — مرزِ همه‌ی سقف‌های روزانه. */
fun startOfLocalDay(date: LocalDate = LocalDate.now()): Long =
    date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
