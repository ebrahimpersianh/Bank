package ir.sadteam.loancalc.data.coin

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
) {
    val price: Int get() = if (unlockBadge != null) 0 else kind.price
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
    ThemePalette("lapis", "لاجورد", dark = 0xFF16275E, primary = 0xFF2F4FC4, light = 0xFF93B0F5, inkLight = 0xFF1E357F),
    ThemePalette("garnet", "انار", dark = 0xFF5E0F20, primary = 0xFFB32943, light = 0xFFF0A3B2, inkLight = 0xFF7C1A2E),
    ThemePalette("turquoise", "فیروزه", dark = 0xFF08484B, primary = 0xFF0B7A7E, light = 0xFF86E0E3, inkLight = 0xFF075254),
    ThemePalette("copper", "مسی", dark = 0xFF5C2A10, primary = 0xFFB05520, light = 0xFFF0B489, inkLight = 0xFF7A3A16),
    ThemePalette("aubergine", "بادمجانی", dark = 0xFF3A1648, primary = 0xFF7E3799, light = 0xFFD9A8E8, inkLight = 0xFF56236A),
    ThemePalette("graphite", "دودی", dark = 0xFF1A1F24, primary = 0xFF44525F, light = 0xFFA8B8C6, inkLight = 0xFF2C3640),
)

/** نگاشتِ شناسه به پالت. `null` یعنی تمِ پیش‌فرضِ برند. */
fun themeById(id: String?): ThemePalette? = THEME_CATALOG.firstOrNull { it.id == id }

/**
 * کلِ کاتالوگ.
 *
 * ترتیب **همان ترتیبِ نمایش** است. دسته از `kind.category` می‌آید، پس ردیف‌های یک دسته
 * باید پشتِ‌هم باشند.
 */
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

    // ⚠️ تمِ مناسبتی ردیفِ ثابت **ندارد**: `rarityWindow` بازه‌ی تاریخ می‌خواهد و
    // «دو مناسبت در سال و نه بیشتر». هر بار که مناسبتی می‌آید، یک `ShopItem` با
    // شناسه‌ی خودش (مثلاً `theme:nowruz1405`) اضافه می‌شود و بعدِ بازه از فهرست
    // می‌رود. **شناسه باید سال داشته باشد** - وگرنه نوروزِ سالِ بعد را کسی که پارسال
    // خریده مالکِ آن حساب می‌شود و «از دست دادن» که کلِ ارزشِ قلمِ کمیاب است می‌شکند.

    // ═══ آیکونِ برنامه ═══
    // چهار طرحِ `58a`. «کیف» پیش‌فرض است و فروشی نیست، پس در کاتالوگ نمی‌آید.
    add(ShopItem("icon:coin", CoinSpend.APP_ICON, "آیکونِ سکه", "سکه‌ی طلایی روی زمینه‌ی روشن"))
    add(ShopItem("icon:letter", CoinSpend.APP_ICON, "آیکونِ حرفِ ج", "نشانِ حرفی، ساده‌ترین طرح"))
    add(ShopItem("icon:piggy", CoinSpend.APP_ICON, "آیکونِ قلک", "هم‌خانواده‌ی نمادِ بودجه"))

    // ═══ نمادها ═══
    add(ShopItem("symbolset:rounded", CoinSpend.CATEGORY_ICON_SET, "نمادهای گرد", "نمادِ همه‌ی دسته‌ها یک‌دست می‌شود", comingSoon = true))
    add(ShopItem("coinskin:ancient", CoinSpend.COIN_SKIN, "سکه‌ی کهن", "شکلِ سکه در همه‌ی برنامه", comingSoon = true))
    add(
        ShopItem(
            id = "coinskin:aged_gold",
            kind = CoinSpend.COIN_SKIN,
            label = "سکه‌ی طلای کهنه",
            blurb = "با نشانِ «سالِ کامل» باز می‌شود",
            unlockBadge = "YEAR_COMPLETE",
            comingSoon = true,
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
