package ir.sadteam.loancalc.data.coin

import ir.sadteam.loancalc.ui.components.AvatarFrameStyle
import ir.sadteam.loancalc.ui.theme.AppFontChoice
import ir.sadteam.loancalc.ui.background.LiveBackground
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ═══════════ کاتالوگِ فروشگاه ═══════════
 *
 * **چرا این فایل لازم شد.** `CoinSpend` جدولِ **قیمت** است، نه کاتالوگ:
 * `THEME_PALETTE(150)` یک ردیف است ولی شش تم باید جدا فروخته شوند و مالکیتشان جدا
 * بمانَد. با کلیدِ enum، خریدِ «لاجورد» یعنی `spend:THEME_PALETTE` ثبت می‌شود و پنج تمِ
 * دیگر هم باز می‌شوند.
 *
 * پس کلیدِ مالکیت **شناسه‌ی گونه** است نه نامِ enum:
 *
 *     "theme:lapis"   "icon:piggy"   "coinskin:ancient"
 *
 * `CoinSpend` همان می‌ماند و کارش را می‌کند: قیمت، دسته، و `timeGated`. این فایل
 * می‌گوید **چه چیزهایی** با آن قیمت خریدنی‌اند.
 *
 * ⚠️ `GamificationRepository.owns(key)` باید همین رشته را بگیرد. اگر امروز امضایش
 * `owns(spend: CoinSpend)` است، اورلودِ رشته‌ای لازم دارد.
 */

/** یک ردیفِ فروشگاه. */
data class ShopItem(
    /** کلیدِ مالکیت. یک‌بار انتخاب می‌شود و **هیچ‌وقت عوض نمی‌شود** - در دفتر نوشته شده. */
    val id: String,
    val kind: CoinSpend,
    val label: String,
    /** خطِ دومِ ردیف. کارِ قلم را می‌گوید، نه تعریفش را. */
    val blurb: String,
    /**
     * نشانِ آزادکننده. `AGED_GOLD` قیمتِ صفر دارد ولی نشان می‌خواهد - سکه همه‌چیز را
     * نمی‌خرد (`BuyResult.BadgeLocked`).
     */
    val unlockBadge: String? = null,
    /**
     * تمِ چهارگانه‌ی اولِ برنامه. این‌ها پیش از اقتصادِ سکه ساخته شدند و مالکیتشان در
     * `UiPrefs.owned_themes` نشسته، پس سرگروهِ جدا می‌گیرند (`60a`).
     */
    val base: Boolean = false,
    /**
     * 🚨 **قلمی که هنوز مقصد ندارد.** «نمادهای گرد»، «سکه‌ی کهن» و اشتراکِ جایزه‌ای در
     * برنامه هیچ‌جا خوانده نمی‌شوند - خریدنشان یعنی کاربر سکه بدهد و هیچ اتفاقی نیفتد.
     * پس ردیف می‌آید (هدف دیده شود) ولی خریدنی نیست تا کدِ واقعی‌اش ساخته شود.
     */
    val comingSoon: Boolean = false,
    /**
     * بازه‌ی فروشِ قلمِ کمیاب. `null` یعنی همیشگی.
     *
     * ⚠️ بعدِ بازه ردیف از ویترین **می‌رود**، ولی کسی که خریده نگهش می‌دارد - تنها جایی
     * که «از دست دادن» در برنامه هست، و همان است که سکه را ارزشمند می‌کند.
     */
    val window: ClosedRange<LocalDate>? = null,
) {
    val price: Int get() = if (unlockBadge != null) 0 else kind.price

    /** هنوز در بازه است؟ قلمِ همیشگی همیشه `true`. */
    fun isOpen(today: LocalDate): Boolean = window == null || today in window

    /** چند روز تا بسته‌شدن - برای نوارِ «۱۲ روز» بالای ویترین (`72b`). */
    fun daysLeft(today: LocalDate): Long? =
        window?.let { ChronoUnit.DAYS.between(today, it.endInclusive).coerceAtLeast(0) }
}

/**
 * چهار تمِ پایه - مرزِ دو سرگروهِ `60a`. شناسه‌ها از `ColorTheme.id` می‌آیند، و مبنای
 * ترجمه‌ی مالکیتِ قدیمی هم همین‌هاست (`owned_themes` از اول همین رشته‌ها را نگه می‌داشت).
 */
val BASE_THEME_IDS = setOf("green", "blue", "purple", "gold")

/**
 * پالتِ تم - **سه رنگ و نه بیشتر**.
 *
 * هر توکنِ اضافه یک راهِ تازه برای شکستنِ تمِ تیره است. طلا و قرمزِ خرج و سبزِ درآمد در
 * این جدول **نیستند**: اولی نشانِ برند است و دو تای دیگر معنایی‌اند، پس با تم نمی‌چرخند.
 *
 * سه مشتق (`primaryPill`، `primaryInk`، `primaryBorder`) و همچنین `line` و `glass*` با
 * فرمول از همین سه ساخته می‌شوند - رجوع کن به `ThemeShop-handoff.md`.
 *
 * [primary] پُرکنِ دکمه است و **متنِ سفید** رویش می‌نشیند، پس همه‌ی شش مقدارِ زیر
 * دستِ‌کم ۴٫۵:۱ با سفید کنتراست دارند. اگر رنگِ تازه‌ای اضافه کردید، همان را چک کنید؛
 * روشن‌ترش دکمه را ناخوانا می‌کند.
 */
data class ThemePalette(
    val id: String,
    val label: String,
    val dark: Long,
    val primary: Long,
    val light: Long,
    /**
     * جوهرِ متن **روی قرصِ `primaryPill`ِ حالتِ روشن** - تنها مقداری که فرمولی نیست.
     *
     * در تمِ تیره جوهر همان [light] است (هر شش ترکیب بالای ۵٫۹:۱ درآمدند)، پس فقط حالتِ
     * روشن جدولِ دستی دارد. ⚠️ این رنگ برای متنِ روی سطحِ عادی نیست - آن‌جا [primary] درست
     * است، وگرنه از `AppText` قابلِ تفکیک نیست.
     */
    val inkLight: Long,
)

/**
 * شش تمِ خریدنی.
 *
 * روی محورِ **فام** پخش شده‌اند و هیچ‌کدام نزدیکِ سبزِ برند یا طلای نشان نیست: دو تمِ
 * هم‌فام یعنی کاربر ۱۵۰ سکه داده و تفاوتی نمی‌بیند. «دودی» عمداً بی‌فام است — تنها
 * گزینه‌ای که کسی که رنگ نمی‌خواهد هم می‌خرد.
 */
val THEME_CATALOG = listOf(
    // 🚨 **ترتیب روی محورِ فام است، نه تاریخِ اضافه‌شدن** (خواسته‌ی کاربر، دورِ ۹:
    // «بغلِ هم یک طیفِ رنگی از آن‌ها بگذار»). ده ردیفِ پشتِ‌هم که رنگشان بی‌نظم بالا و
    // پایین می‌پرد شبیهِ فهرست است نه ویترین؛ با طیف، چشم خودش گروه‌ها را می‌سازد.
    // فامِ تقریبیِ هر `primary` جلوی خودش نوشته شده تا افزودنِ رنگِ تازه جایش را بداند.
    ThemePalette("copper", "نارنجیِ مسی", dark = 0xFF5C2A10, primary = 0xFFB05520, light = 0xFFF0B489, inkLight = 0xFF7A3A16), // ۲۵°
    ThemePalette("olive", "سبزِ زیتونی", dark = 0xFF2A2E12, primary = 0xFF5F6B22, light = 0xFFC7D68A, inkLight = 0xFF444C18), // ۷۳°
    ThemePalette("teal", "سبزآبیِ تیره", dark = 0xFF0B2320, primary = 0xFF0E5F53, light = 0xFF7ACBBC, inkLight = 0xFF0A4A41), // ۱۶۸°
    ThemePalette("turquoise", "فیروزه", dark = 0xFF08484B, primary = 0xFF0B7A7E, light = 0xFF86E0E3, inkLight = 0xFF075254), // ۱۸۲°
    ThemePalette("lapis", "لاجورد", dark = 0xFF16275E, primary = 0xFF2F4FC4, light = 0xFF93B0F5, inkLight = 0xFF1E357F), // ۲۲۶°
    ThemePalette("indigo", "بنفشِ نیلی", dark = 0xFF1E1B4B, primary = 0xFF4338CA, light = 0xFFA5B4FC, inkLight = 0xFF312BA0), // ۲۴۵°
    ThemePalette("aubergine", "بادمجانی", dark = 0xFF3A1648, primary = 0xFF7E3799, light = 0xFFD9A8E8, inkLight = 0xFF56236A), // ۲۸۳°
    ThemePalette("crimson", "قرمزِ شرابی", dark = 0xFF4A0F2B, primary = 0xFF9E2A52, light = 0xFFF2A4BF, inkLight = 0xFF721E3C), // ۳۳۷°
    ThemePalette("garnet", "انار", dark = 0xFF5E0F20, primary = 0xFFB32943, light = 0xFFF0A3B2, inkLight = 0xFF7C1A2E), // ۳۴۹°
    // «دودی» عمداً **آخر** است و نه جایی وسطِ طیف: بی‌فام است، پس هر جای دیگری بگذاریش
    // طیف را وسط می‌شکند. تنها گزینه‌ی کسی است که اصلاً رنگ نمی‌خواهد.
    ThemePalette("graphite", "دودی", dark = 0xFF1A1F24, primary = 0xFF44525F, light = 0xFFA8B8C6, inkLight = 0xFF2C3640),
    // ── چهار فامِ تازه (دورِ ۱۳) ──────────────────────────────────────────────
    // هر چهار تا **حفره‌های واقعیِ طیف** را پر می‌کنند، نه ردیفِ بیشتر: بینِ مسی و
    // زیتونی ۴۸ درجه فاصله بود، بینِ لاجورد و نیلی هیچ آبیِ روشنی نبود، و کلِ طیف
    // هیچ قهوه‌ای/خاکی نداشت. تمِ هم‌فام با تمِ موجود یعنی کاربر ۱۵۰ سکه داده و
    // تفاوتی نمی‌بیند - همان قاعده‌ی خودِ این فایل.
    // هگزها **عیناً از جدولِ بخشِ ۷۸** برداشته شدند، نه از حدسِ ما (قاعده‌ی «حدس نزن،
    // گرد نکن»). تنها چیزی که خودمان ساختیم `inkLight` است: جدولِ طراح سه هگز می‌دهد و
    // خودش نوشت که ششمی فرمولی نیست و باید دستی بیاید - این‌ها تیره‌ترشده‌ی `primary`اند
    // تا روی سطحِ روشن ۴٫۵:۱ بدهند.
    ThemePalette("saffron", "زعفرانی", dark = 0xFF4A2C04, primary = 0xFFD98016, light = 0xFFF5C782, inkLight = 0xFF8A5209), // ۴۴°
    ThemePalette("cobalt", "آبیِ کبالت", dark = 0xFF0B2A5E, primary = 0xFF1B6FD6, light = 0xFF8FC4F5, inkLight = 0xFF13508F), // ۲۰۵°
    // الهام از نورِ آبیِ الکتریکی روی سرمه‌ایِ عمیق: تیره، تمیز و چشمگیر؛ نه کپیِ یک رابط دیگر.
    // primary با سفید کنتراستِ خوانا دارد و light فقط نقشِ درخشش/لهجه را می‌گیرد.
    ThemePalette("neon_midnight", "نئونِ نیمه‌شب", dark = 0xFF070C24, primary = 0xFF1D54E8, light = 0xFF62D5FF, inkLight = 0xFF143C9A), // ۲۲۱°
    ThemePalette("plum", "آلوییِ روشن", dark = 0xFF3A1240, primary = 0xFF9B3FA8, light = 0xFFE0A8E8, inkLight = 0xFF6B2B75), // ۲۷۵°
    ThemePalette("clay", "خاکِ رس", dark = 0xFF3A2318, primary = 0xFFA05C3C, light = 0xFFE0B39A, inkLight = 0xFF70402A), // ۱۸°
)

/**
 * تمِ **هنری** - گران‌تر و کمیاب‌تر از تمِ رنگی (خواسته‌ی کاربر: «یک تمِ ون‌گوگ، خاص‌تر
 * و گران‌تر»).
 *
 * رنگ‌ها از «شبِ پرستاره» برداشته شده‌اند: `dark` آبیِ عمیقِ آسمان، `primary` آبیِ
 * چرخش‌های میانی، `light` زردِ ستاره‌ها.
 *
 * ⚠️ **زرد `primary` نشد** با این‌که رنگِ امضای آن تابلوست: `primary` پُرکنِ دکمه است و
 * متنِ **سفید** رویش می‌نشیند؛ زرد با سفید به ~۱٫۶:۱ می‌رسد یعنی کاملاً ناخوانا. پس زرد
 * جای `light` نشست (همان‌جا که جوهر روی سطحِ تیره است) و آبی پُرکنِ دکمه ماند.
 * قاعده‌ی «دستِ‌کم ۴٫۵:۱ با سفید» بندِ صریحِ همین فایل است و برای هیچ تمی استثنا ندارد.
 */
val ART_PALETTES = listOf(
    ThemePalette("vangogh", "شبِ پرستاره", dark = 0xFF0B1A3A, primary = 0xFF1F4E8C, light = 0xFFF2C14E, inkLight = 0xFF17396B),
)

/** نگاشتِ شناسه به پالت. `null` یعنی تمِ پیش‌فرضِ برند. */
fun themeById(id: String?): ThemePalette? =
    (THEME_CATALOG + SEASONAL_PALETTES + ART_PALETTES).firstOrNull { it.id == id }

/**
 * کلِ کاتالوگ.
 *
 * ترتیب **همان ترتیبِ نمایش** است. دسته از `kind.category` می‌آید، پس ردیف‌های یک دسته
 * باید پشتِ‌هم باشند.
 */
/**
 * پالتِ تمِ مناسبتی. بازه‌ی فروشش در [SEASONAL_THEMES] می‌نشیند، نه این‌جا - خودِ پالت
 * بعدِ بازه هم لازم است تا کسی که خریده نگهش دارد.
 */
val SEASONAL_PALETTES = listOf(
    ThemePalette("nowruz1405", "سبزِ نوروزی", dark = 0xFF14401F, primary = 0xFF1E8A3C, light = 0xFF96E0AC, inkLight = 0xFF156630),
)

/** ردیف‌های مناسبتی با بازه‌ی خودشان. */
val SEASONAL_THEMES: List<ShopItem> = listOf(
    ShopItem(
        id = "theme:nowruz1405",
        kind = CoinSpend.THEME_SEASONAL,
        label = "تمِ سبزِ نوروزی",
        blurb = "تمِ رنگی · فقط تا پایانِ فروردین",
        window = LocalDate.of(2026, 3, 1)..LocalDate.of(2026, 4, 20),
    ),
)

val SHOP_CATALOG: List<ShopItem> = buildList {

    // ═══ تمِ پایه (`60a`) ═══
    // «سبز» پیش‌فرض است و همیشه مالِ کاربر؛ «طلایی» با نشان باز می‌شود نه با سکه.
    add(ShopItem("theme:green", CoinSpend.THEME_PALETTE, "تمِ سبزِ جیبک", "تمِ پیش‌فرضِ برنامه", base = true))
    add(ShopItem("theme:blue", CoinSpend.THEME_PALETTE, "تمِ آبی", "رنگِ اصلیِ برنامه را عوض می‌کند", base = true))
    add(ShopItem("theme:purple", CoinSpend.THEME_PALETTE, "تمِ بنفش", "رنگِ اصلیِ برنامه را عوض می‌کند", base = true))
    add(
        ShopItem(
            id = "theme:gold",
            kind = CoinSpend.THEME_PALETTE,
            label = "تمِ طلایی",
            blurb = "با نشانِ «ماهِ منظم» باز می‌شود",
            unlockBadge = "steady_month",
            base = true,
        ),
    )

    // ═══ تمِ رنگی ═══
    // نامِ متغیر عمداً `palette` نیست: بررسیِ ایستای `palette.py` هر `palette.X` را دسترسی
    // به فیلدِ `AppColorPalette` می‌خواند و این‌جا مثبتِ کاذب می‌داد.
    THEME_CATALOG.forEach { theme ->
        add(
            ShopItem(
                id = "theme:${theme.id}",
                kind = CoinSpend.THEME_PALETTE,
                label = "تمِ ${theme.label}",
                blurb = "رنگِ اصلیِ برنامه را عوض می‌کند",
            ),
        )
    }

    // ═══ تمِ هنری ═══
    // بعد از تم‌های رنگی می‌نشیند نه بینشان: گران‌تر است و اگر وسطِ ده ردیفِ ۱۵۰سکه‌ای
    // بیفتد، فقط یک ردیفِ گران به‌نظر می‌رسد نه یک قلمِ متفاوت.
    ART_PALETTES.forEach { art ->
        add(
            ShopItem(
                id = "theme:${art.id}",
                kind = CoinSpend.THEME_ART,
                label = "تمِ ${art.label}",
                blurb = "پالتِ «شبِ پرستاره»ی ون‌گوگ · آبیِ عمیق و زردِ ستاره",
            ),
        )
    }

    // ⚠️ تمِ مناسبتی ردیفِ ثابت **ندارد**: `rarityWindow` بازه‌ی تاریخ می‌خواهد و
    // «دو مناسبت در سال و نه بیشتر». هر بار که مناسبتی می‌آید، یک `ShopItem` با
    // شناسه‌ی خودش (مثلاً `theme:nowruz1405`) اضافه می‌شود و بعدِ بازه از فهرست
    // می‌رود. **شناسه باید سال داشته باشد** - وگرنه نوروزِ سالِ بعد را کسی که پارسال
    // خریده مالکِ آن حساب می‌شود و «از دست دادن» که کلِ ارزشِ قلمِ کمیاب است می‌شکند.

    // ═══ تمِ مناسبتی - تنها قلمِ مهلت‌دارِ فروشگاه (`72a` بندِ ۳) ═══
    // بقیه‌ی فروشگاه همیشه سرِ جایش است، پس هیچ فوریتی نمی‌سازد؛ و سکه‌ای که فوریت
    // نداشته باشد جمع می‌شود و خرج نمی‌شود.
    // 🚨 **شناسه سال دارد.** بی سال، نوروزِ سالِ بعد برای کسی که پارسال خریده مجانی است و
    // «از دست دادن» - که کلِ ارزشِ قلمِ کمیاب است - می‌شکند.
    SEASONAL_THEMES.forEach { add(it) }

    // ═══ آیکونِ برنامه ═══
    // چهار طرحِ `58a`. «کیف» پیش‌فرض است و فروشی نیست، پس در کاتالوگ نمی‌آید.
    add(ShopItem("icon:coin", CoinSpend.APP_ICON, "آیکونِ سکه", "سکه‌ی طلایی روی زمینه‌ی روشن"))
    add(ShopItem("icon:letter", CoinSpend.APP_ICON, "آیکونِ حرفِ ج", "نشانِ حرفی، ساده‌ترین طرح"))
    // 🚨 **قلک با هیچ قیمتی خریدنی نیست - با نشانِ «هدف‌رس» باز می‌شود** (جوابِ دورِ ۱۱).
    // قاعده‌ی `72c` می‌گفت هر نوعِ قلم باید یک نمونه‌ی نشان‌قفل داشته باشد و «آیکون» نداشت.
    // طراح فایلِ هنریِ تازه نداد و لازم هم نبود: نشان‌قفل باید **معنی** داشته باشد نه فقط
    // کمیاب باشد. سیلوئتِ قلک هم‌خانواده‌ی پس‌انداز است، پس «قلک با رسیدن به اولین هدفِ
    // پس‌انداز» یک جمله‌ی کامل است؛ «آیکونِ پنجم با نشانِ X» فقط یک قفلِ دیگر بود.
    // چیزی از ویترین کم نمی‌شود چون هر سه آیکون یک قیمت داشتند.
    add(
        ShopItem(
            id = "icon:piggy",
            kind = CoinSpend.APP_ICON,
            label = "آیکونِ قلک",
            blurb = "با نشانِ «هدف‌رس» باز می‌شود",
            unlockBadge = "goal_reached",
        ),
    )

    // ═══ پس‌زمینه‌ی زنده (قلمِ تازه‌ی دورِ ۱۳) ═══
    // رنگش از توکنِ تمِ فعال می‌آید، پس با هر تمی که کاربر دارد هم‌قدم است و ردیفِ
    // ویترین هم همین را می‌گوید.
    // 🚨 **یک قیمت برای هر چهار طرح** (بندِ ۱ی وصله‌ی `ShopScreen` در بخشِ ۷۸): با چهار
    // قیمتِ جدا، کاربر یکی را می‌خرید و سه طرحِ دیگر را **هیچ‌وقت نمی‌دید**. ردیفِ اول
    // قیمت دارد و بقیه بجِ «با بسته» می‌گیرند؛ خریدِ هر کدام هر چهار را باز می‌کند.
    LiveBackground.entries.forEach { b ->
        add(
            ShopItem(
                id = "bg_${b.id}",
                kind = CoinSpend.LIVE_BACKDROP,
                label = b.nameFa,
                // ⚠️ «چرخشِ شب» در تمِ روشن کشیده نمی‌شود (`darkOnly`)، پس ردیفش باید
                // خودش این را بگوید - وگرنه کاربر می‌خرد و فکر می‌کند کار نمی‌کند.
                blurb = if (b.darkOnly) "فقط در حالتِ تاریکِ برنامه فعال می‌شود" else "روی هر تمی می‌نشیند",
            ),
        )
    }

    // ═══ نمادها ═══
    // تنها قلمی که **هر روز** دیده می‌شود (بندِ ۱ی `72a`): در فرمِ ثبت، در دونات، و روی
    // هر ردیفِ تراکنش. هر دو ست واقعی‌اند - `SymbolStyle` سه شکلِ هر کلید را دارد و
    // خریدشان روی دسته‌های **ثابت** هم می‌نشیند، نه فقط دلخواه‌ها.
    add(ShopItem("symbolset:rounded", CoinSpend.CATEGORY_ICON_SET, "نمادهای گرد", "۱۸ نمادِ دسته، گوشه‌گرد و نرم"))
    add(ShopItem("symbolset:outlined", CoinSpend.CATEGORY_ICON_SET, "نمادهای خطی", "۱۸ نمادِ دسته، فقط خط بی پُرکن"))
    add(ShopItem("symbolset:sharp", CoinSpend.CATEGORY_ICON_SET, "نمادهای زاویه‌دار", "۱۸ نمادِ دسته، تیز و مدرن"))
    add(ShopItem("symbolset:two_tone", CoinSpend.CATEGORY_ICON_SET, "نمادهای دو‌لایه", "۱۸ نمادِ دسته، سایه‌دار و چشمگیر"))
    add(ShopItem("symbolset:pictorial", CoinSpend.CATEGORY_ICON_SET, "نمادهای روزمره", "۱۸ نمادِ دسته، ساده و آشنا"))
    add(ShopItem("coinskin:ancient", CoinSpend.COIN_SKIN, "سکه‌ی کهن", "شکلِ سکه در همه‌ی برنامه", comingSoon = true))
    add(
        ShopItem(
            id = "coinskin:aged_gold",
            kind = CoinSpend.COIN_SKIN,
            label = "سکه‌ی طلای کهنه",
            blurb = "با نشانِ «وامِ بسته» باز می‌شود",
            // ⚠️ قبلاً `YEAR_COMPLETE` بود که **هیچ نشانی با آن کد وجود ندارد** - یعنی
            // این ردیف برای همیشه قفل می‌مانْد و برچسبِ دیالوگ هم کدِ خام را نشان می‌داد.
            unlockBadge = "loan_closed",
            comingSoon = true,
        ),
    )

    // ═══ قابِ آواتار (`72a` بندِ ۲) ═══
    // ارزان‌ترین قلم و همین درست است: پنج‌شش شکلِ سادهٔ وکتوری. قیمت کارِ ساخت را می‌گوید،
    // نه میزانِ دیده‌شدن (`72c`).
    AvatarFrameStyle.entries.forEach { frame ->
        add(
            ShopItem(
                id = "frame:${frame.id}",
                kind = CoinSpend.AVATAR_FRAME,
                label = frame.label,
                blurb = frame.blurb,
                // قلمِ نشان‌قفلِ این نوع: با هیچ مقدار سکه‌ای خریدنی نیست، پس نشان
                // می‌گوید کاربر **چه کرده** - همان چیزی که سکه هیچ‌وقت نمی‌تواند بگوید.
                unlockBadge = if (frame == AvatarFrameStyle.LAUREL) "under_budget" else null,
            ),
        )
    }

    // ═══ قلمِ متن ═══
    // نوعِ تازه‌ی ششم. قلم **هر کلمه‌ی برنامه** را عوض می‌کند، پس بیشترین اثرِ بصری را
    // با کمترین فایلِ تازه دارد - ولی همین باعث می‌شود خطرناک هم باشد: قلمِ ناخوانا
    // یعنی برنامه‌ی ناخوانا. پس هر سه قلم **متنی** و فارسی‌خوان‌اند و «لاله‌زار»
    // (تزئینی‌ترینشان) گران‌ترین است، نه ارزان‌ترین - تا اولین خریدِ کاربرِ تازه نباشد.
    add(
        ShopItem(
            id = AppFontChoice.NASKH.id,
            kind = CoinSpend.FONT_FACE,
            label = AppFontChoice.NASKH.label,
            blurb = "متنِ برنامه · کتابی و آرام",
        ),
    )
    add(
        ShopItem(
            id = AppFontChoice.MARKAZI.id,
            kind = CoinSpend.FONT_FACE,
            label = AppFontChoice.MARKAZI.label,
            blurb = "متنِ برنامه · کشیده و باریک",
        ),
    )
    add(
        ShopItem(
            id = AppFontChoice.LALEZAR.id,
            kind = CoinSpend.FONT_FACE,
            label = AppFontChoice.LALEZAR.label,
            // ⚠️ **باید بگوید «برای تیترها»** (بندِ صریحِ طراح، دورِ ۱۰): لاله‌زار فقط روی
            // تیتر می‌نشیند و بدنه وزیرمتن می‌ماند. بی این جمله، کاربر ۱۲۰ سکه می‌دهد،
            // متنِ بدنه عوض نمی‌شود و فکر می‌کند خرید اعمال نشده.
            blurb = "فقط برای تیترها · درشت و نمایشی",
        ),
    )

    // ═══ جایزه ═══
    // ترمیمِ زنجیره در فروشگاه **نمی‌آید**: دسته ندارد، مالکیت نمی‌آورد، و جایش کارتِ
    // `56b` است که فقط وقتی رشته پاره شده دیده می‌شود. قلمی که همیشه در ویترین باشد
    // ولی فقط دو روز در ماه قابلِ خرید، ردیفِ خاموشِ دائمی است.
    add(ShopItem("sub:3d", CoinSpend.SUBSCRIPTION_3D, "اشتراکِ ۳ روزه", "همه‌ی امکاناتِ اشتراکی، سه روز", comingSoon = true))
    add(ShopItem("sub:7d", CoinSpend.SUBSCRIPTION_7D, "اشتراکِ ۷ روزه", "همه‌ی امکاناتِ اشتراکی، یک هفته", comingSoon = true))
}

/** ردیف‌های یک دسته، به ترتیبِ کاتالوگ. */
fun catalogOf(category: ShopCategory): List<ShopItem> =
    SHOP_CATALOG.filter { it.kind.category == category }
