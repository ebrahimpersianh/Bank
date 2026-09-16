package ir.sadteam.loancalc.data.coin

import java.time.LocalDate

/**
 * ═══════════ اقتصادِ سکه ═══════════
 *
 * جدولِ فریمِ `45d` به کد. هیچ عددِ سکه‌ای جای دیگری در برنامه نوشته نمی‌شود؛
 * این فایل تنها مرجع است.
 *
 * سه قاعده‌ای که اقتصاد را از تورم نگه می‌دارد و در کد تحمیل می‌شوند:
 *
 * ۱. **هیچ کاری بی سقف نیست** ([CoinReason.dailyCap]). ثبتِ پنجاه تراکنشِ الکی
 *    حداکثر ۳۰ سکه می‌دهد - همان مقدار که با سه ثبتِ واقعی هم می‌گرفت. پس انگیزه‌ی
 *    ثبتِ الکی صفر است، بی این‌که لازم باشد «الکی بودن» را تشخیص بدهیم.
 * ۲. **پنج قلم هیچ مسیرِ زمانی ندارند** ([CoinSpend.timeGated] == false). با صبر
 *    نمی‌آیند، فقط با خرج. ستونِ فقراتِ انگیزه همین است: اگر همه‌چیز با گذشتِ وقت هم
 *    بیاید، سکه بی‌ارزش می‌شود.
 * ۳. **دفتر منبعِ حقیقت است.** موجودی از جمعِ ردیف‌های [CoinEvent] بازسازی می‌شود، نه
 *    فیلدی که کم و زیاد شود - وگرنه با همگام‌سازیِ چند دستگاه از هم می‌پاشد.
 */

/**
 * کارهایی که سکه می‌دهند.
 *
 * [dailyCap] سقفِ **همین کار** در بازه‌ی خودش است، نه سقفِ کلِ روز. سقفِ کلِ روز
 * [DAILY_COIN_CAP] است و جدا اعمال می‌شود، چون جمعِ سقف‌های تکی از آن بیشتر است.
 */
enum class CoinReason(
    val amount: Int,
    val label: String,
    val period: CoinPeriod,
    val dailyCap: Int,
) {
    TRANSACTION_ADDED(10, "ثبتِ تراکنش", CoinPeriod.DAY, 3),
    DAILY_OPEN(5, "ورودِ روزانه", CoinPeriod.DAY, 1),

    /**
     * تاییدِ حدسِ پیامکِ بانک - هم برای کاربر ارزش دارد و هم دقتِ تشخیص را بالا می‌برد.
     * سوءاستفاده‌پذیر نیست چون به پیامکِ **واقعیِ** بانک گره خورده: بی پیامک، تاییدی
     * برای زدن وجود ندارد.
     */
    SMS_GUESS_CONFIRMED(3, "تاییدِ حدسِ پیامک", CoinPeriod.DAY, 5),

    FULL_WEEK(50, "هفته‌ی کامل", CoinPeriod.WEEK, 1),
    INSTALLMENT_ON_TIME(20, "پرداختِ سرِ موعد", CoinPeriod.ITEM, 1),
    UNDER_BUDGET_MONTH(120, "ماندن زیرِ بودجه", CoinPeriod.MONTH, 1),

    /** مقدارِ نشان از خودِ نشان می‌آید (۲۵ تا ۱۵۰)، پس `amount` اینجا بی‌مصرف است. */
    BADGE_UNLOCKED(0, "نشانِ دستاورد", CoinPeriod.LIFETIME, 1),
    ;

    companion object {
        /**
         * سقفِ روزانه: ۳۰ ثبت + ۵ ورود + ۱۵ تایید = ۵۰.
         *
         * عمداً از جمعِ سقف‌های تکی کمتر است. بی این، کاربر با پنج تاییدِ پیامک و سه ثبت
         * ۴۵ سکه می‌گرفت و «هفته‌ی کاملِ» همان روز هم رویش می‌نشست.
         */
        const val DAILY_COIN_CAP = 50
    }
}

enum class CoinPeriod { DAY, WEEK, MONTH, ITEM, LIFETIME }

/**
 * قلم‌های فروشگاه.
 *
 * [timeGated] = آیا با گذشتِ زمان هم به دست می‌آید؟ **پنج قلمِ `false`** ستونِ فقراتِ
 * اقتصادند (قاعده‌ی ۲). اگر روزی قلمی را `false` گذاشتی و بعد راهِ رایگانی برایش باز
 * کردی، ارزشِ سکه را شکسته‌ای.
 *
 * [rarityWindow] فقط برای دو قلمِ مناسبتی. تمِ کمیاب بعد از بازه از فروشگاه می‌رود و
 * برنمی‌گردد؛ کسی که خریده نگهش می‌دارد. این تنها جایی است که «از دست دادن» در
 * برنامه هست، و همان چیزی است که سکه را ارزشمند می‌کند - پس **دو مناسبت در سال و نه
 * بیشتر**: اگر هر ردیف فوری باشد، هیچ‌کدام فوری نیست.
 */
enum class CoinSpend(
    val price: Int,
    val label: String,
    val category: ShopCategory,
    val timeGated: Boolean,
    val rarityWindow: ClosedRange<LocalDate>? = null,
) {
    THEME_PALETTE(150, "تمِ رنگی", ShopCategory.THEME, timeGated = true),
    THEME_SEASONAL(300, "تمِ مناسبتی", ShopCategory.THEME, timeGated = false),

    /**
     * تمِ **هنری** - گران‌تر از تمِ رنگی چون کارِ ساختش بیشتر است، نه چون بیشتر دیده
     * می‌شود (همان قاعده‌ی `72c`). پالتِ رنگیِ معمولی سه رنگِ هم‌فام است؛ پالتِ هنری از
     * روی یک اثر برداشته می‌شود و هر سه رنگش باید هم با هم بخوانند هم کنتراستِ متنِ
     * سفید را نگه دارند - همان چیزی که چند دورِ تنظیم می‌برد.
     */
    // ⚠️ **۸۰۰ → ۴۰۰ (جوابِ دورِ ۱۱).** ایرادِ ما وارد بود: تمِ هنری با سه توکنِ رنگی از ده
    // تمِ رنگی **متمایز دیده نمی‌شود**، و تمی که متمایز نیست با ۸۰۰ سکه حسِ بدِ خرید
    // می‌دهد - بدتر از نداشتنِ تم. طراح توکنِ چهارم را رد کرد (هر توکنِ اضافه یک راهِ تازه
    // برای شکستنِ تمِ تیره) و راهِ درست را **بافتِ زمینه** دانست؛ ⏳ تا وقتی آن بافت ساخته
    // نشده، قیمت همان کاری را می‌کند که باید: ۴۰۰.
    THEME_ART(400, "تمِ هنری", ShopCategory.THEME, timeGated = false),

    APP_ICON(400, "آیکونِ برنامه", ShopCategory.ICON, timeGated = false),

    /**
     * 🚨 **قیمت کارِ ساخت را می‌گوید، نه میزانِ دیده‌شدن** (بندِ `72c`).
     *
     * منطقِ «هرچه بیشتر دیده شود گران‌تر» منطقِ فروشنده است نه خریدار: با سقفِ ماهانه‌ی
     * ~۹۰۰ سکه، کاربرِ تازه تنها چیزی را که واقعاً می‌خواهد آخر می‌خرد و بین راه دلسرد
     * می‌شود. ستِ ۲۴نمادی از آیکونِ برنامه (نُه پله + یک فایلِ هنری) کارِ بیشتری دارد،
     * پس ۵۰۰ است - نه چون روزانه دیده می‌شود.
     */
    CATEGORY_ICON_SET(500, "مجموعه‌ی نمادِ دسته‌ها", ShopCategory.SYMBOL, timeGated = false),
    COIN_SKIN(200, "نمادِ سکه", ShopCategory.SYMBOL, timeGated = false),

    /** پنج‌شش شکلِ ساده‌ی وکتوری - ارزان‌ترین قلمِ فروشگاه، و همین درست است. */
    AVATAR_FRAME(100, "قابِ آواتار", ShopCategory.FRAME, timeGated = false),

    /**
     * قلمِ فونت. سه ردیف دارد و **ارزان‌ترینش زیرِ ۱۵۰** است (شرطِ صریحِ طراح در دورِ ۹:
     * هر دسته باید دستِ‌کم یک قلمِ زیرِ ۱۵۰ داشته باشد، وگرنه کاربرِ تازه در آن دسته
     * فقط قفل می‌بیند و ویترین به نمایشگاه تبدیل می‌شود). پس قیمت **روی خودِ ردیف**
     * است نه این‌جا - رجوع کن به `FONT_CATALOG`.
     */
    FONT_FACE(120, "قلمِ متن", ShopCategory.FONT, timeGated = false),

    STREAK_REPAIR(100, "ترمیمِ زنجیره", ShopCategory.REWARD, timeGated = true),
    SUBSCRIPTION_3D(1200, "اشتراکِ جایزه ۳ روزه", ShopCategory.REWARD, timeGated = true),
    SUBSCRIPTION_7D(2500, "اشتراکِ جایزه ۷ روزه", ShopCategory.REWARD, timeGated = true),
    ;

    /**
     * آیکونِ برنامه گران‌ترین قلمِ غیرِاشتراک است چون نُه پله کار می‌برد و بیرونی‌ترین
     * نمایشِ هویت است - کاربر آن را روی صفحه‌ی گوشی به دیگران نشان می‌دهد.
     *
     * حسابِ سرانگشتی با ~۹۰۰ سکه در ماه: یا یک تم و پس‌انداز، یا **سه ماه صرفه‌جویی**
     * برای اشتراکِ هفت‌روزه. سختِ رسیدن است، همان‌طور که خواسته شد.
     */
    val isFlagship: Boolean get() = this == SUBSCRIPTION_7D
}

/**
 * پنج نوعِ قلم - همان پنج تبِ ویترینِ `72b`.
 *
 * [tab] نامِ کوتاهِ تب است؛ [label] عنوانِ سرگروهِ داخلِ همان تب. با پنج نوع، سرگروه‌بندیِ
 * تنها یعنی کاربر برای رسیدن به «قاب» باید از چهارده تم عبور کند - تب فهرست را **کوتاه**
 * می‌کند، سرگروه فقط نشانه‌گذاری‌اش.
 */
enum class ShopCategory(val label: String, val tab: String) {
    THEME("تم", "تم"),
    ICON("آیکونِ برنامه", "آیکون"),
    SYMBOL("نمادها", "نماد"),
    FRAME("قابِ آواتار", "قاب"),
    FONT("قلمِ متن", "قلم"),
    REWARD("جایزه", "جایزه"),
}

/** یک ردیفِ دفتر. `amount` مثبت = کسب، منفی = خرج. */
data class CoinEvent(
    val id: Long = 0,
    val amount: Int,
    val label: String,
    /** `reason:NAME` یا `spend:NAME` یا `badge:NAME` - برای شمارشِ سقف. */
    val key: String,
    val atEpochMs: Long,
)

/**
 * سقفِ باقی‌ماندهِ‌ی امروز - عددی که نوارِ `45a` نشان می‌دهد.
 *
 * ⚠️ این نوار مهم‌ترین چیزِ صفحه‌ی سکه است و ظاهراً کوچک به‌نظر می‌رسد: **بی آن، کاربر
 * نمی‌فهمد چرا ثبتِ چهارمش سکه نداد و باگ گزارش می‌کند.** سقفِ پیدا نشده، سقفِ باگ است.
 */
fun remainingDailyCap(todayEvents: List<CoinEvent>): Int =
    (CoinReason.DAILY_COIN_CAP - todayEvents.filter { it.amount > 0 }.sumOf { it.amount })
        .coerceAtLeast(0)

sealed interface CoinAward {
    data class Granted(val amount: Int) : CoinAward
    /** سقفِ خودِ کار پر شده. */
    data object ReasonCapped : CoinAward
    /** سقفِ کلِ روز پر شده. */
    data object DailyCapped : CoinAward
}

/**
 * محاسبه‌ی جایزه. **هیچ ردیفی نمی‌نویسد** - تصمیم را برمی‌گرداند تا لایه‌ی دیتا در
 * تراکنشِ خودش ثبتش کند.
 *
 * [periodEvents] ردیف‌های همین کار در بازه‌ی [CoinReason.period]، و [todayEvents]
 * ردیف‌های کلِ امروز.
 */
fun awardFor(
    reason: CoinReason,
    periodEvents: List<CoinEvent>,
    todayEvents: List<CoinEvent>,
    badgeAmount: Int? = null,
): CoinAward {
    val alreadyThisPeriod = periodEvents.count { it.key == "reason:${reason.name}" }
    if (alreadyThisPeriod >= reason.dailyCap) return CoinAward.ReasonCapped

    val amount = badgeAmount ?: reason.amount
    val remaining = remainingDailyCap(todayEvents)
    // سقفِ روز **نمی‌بُرد**، رد می‌کند: نیم‌جایزه به کاربر می‌گوید محاسبه خراب است.
    // نشانِ دستاورد استثناست - یک‌بار در عمر است و از دست دادنش بابتِ سقفِ روز ظالمانه.
    if (reason != CoinReason.BADGE_UNLOCKED && amount > remaining) return CoinAward.DailyCapped

    return CoinAward.Granted(amount)
}

sealed interface BuyResult {
    data object Ok : BuyResult
    data object AlreadyOwned : BuyResult
    /** حالتِ «بی‌سکه»ی فریمِ `18c` - پیام «۷۰ سکه کم داری». */
    data class NotEnough(val missing: Int) : BuyResult
    /** `AGED_GOLD`: قیمتش ۰ است ولی نشان می‌خواهد. سکه همه‌چیز را نمی‌خرد. */
    data class BadgeLocked(val badgeLabel: String) : BuyResult
    /** بازه‌ی قلمِ کمیاب تمام شده. */
    data object WindowClosed : BuyResult
}
