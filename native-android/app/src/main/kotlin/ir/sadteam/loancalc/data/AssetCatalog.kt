package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD

/**
 * کاتالوگِ ثابتِ دارایی‌های آماده - هم‌الگو با [banks]/[expenseCategories]: یه لیستِ کد، نه یه
 * جدولِ دیتابیس. کاربر از این لیست انتخاب می‌کنه و اون‌وقت یه ردیف تو `assets` ساخته می‌شه.
 *
 * ⚠️ **قیمت اینجا نیست.** قیمتِ لحظه‌ای باید از یه سرویسِ بیرونی بیاد که کلیدش هنوز نرسیده
 * (تصمیمِ صریحِ کاربر: «همه‌چیز رو بساز و قیمت‌ها فعلاً جای خالی بمونن»). وقتی کلید رسید، فقط
 * کافیه `AssetRepository.refreshPrices()` پر بشه - هیچ‌جای دیگه‌ای لازم نیست عوض بشه.
 */
data class AssetCatalogEntry(
    val symbol: String,
    val name: String,
    val category: String,
    /**
     * `false` یعنی سرویسِ قیمت (servix.cc) این نماد را **اصلاً ندارد** - نه این‌که موقتاً
     * نیامده. کاربر می‌تواند ثبتش کند و قیمتِ واحد را دستی بزند، ولی در صفحه‌ی «قیمتِ روز»
     * (`43a`) نمی‌آید؛ یک ستونِ پُر از «—» آن صفحه را بی‌فایده می‌کند.
     *
     * فهرستِ کاملِ نمادهای سرویس در `design/SERVIX-symbols.md`.
     */
    val hasLivePrice: Boolean = true,
)

/**
 * ⚠️ نامِ فارسیِ هر نماد **عیناً از صفحه‌ی رسمیِ نمادهای servix** برداشته شده، نه حدس -
 * همان درسی که با لوگوی بانک‌ها گرفتیم. سه غلطِ تایپیِ خودِ آن صفحه اصلاح شد
 * (الیت‌کوین→لایت‌کوین، سوالنا→سولانا، استالر→استلار).
 *
 * ترتیب: پرکاربردها اول، بعد بقیه به ترتیبِ همان صفحه.
 */
val cryptoAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("BTC", "بیت کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ETH", "اتریوم", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USDT", "تتر", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("XAUT", "تتر گلد", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("PAXG", "پکس گلد", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("BNB", "بایننس کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("TRX", "ترون", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("LTC", "لایت کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("SOL", "سولانا", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("XRP", "ریپل", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("DOGE", "دوج کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ADA", "کاردانو", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("DOT", "پولکادات", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("AVAX", "آوالانچ", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("LINK", "چین لینک", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("SHIB", "شیبا اینو", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("PEPE", "پپه", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ATOM", "کازماس", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("XLM", "استلار", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("BCH", "بیت کوین کش", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("UNI", "یونی سواپ", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("AAVE", "آوه", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("NEAR", "نیر پروتکل", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ICP", "اینترنت کامپیوتر", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("HBAR", "هدرا", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("SUI", "سویی", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("TAO", "بیت تنسور", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("WLD", "ورلدکوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ZEC", "زی کش", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("CRO", "کرونوس", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("OKB", "اوکی بی", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("MNT", "منتل", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("HYPE", "هایپرلیکویید", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ONDO", "اوندو", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ASTER", "استر", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("WLFI", "ورلد لیبرتی فایننشال", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("PUMP", "پامپ", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("SKY", "اسکای", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("CC", "کانتون کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("M", "ام", ASSET_CATEGORY_CRYPTO),
    // استیبل‌کوین‌ها
    AssetCatalogEntry("USDC", "یو اس دی کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("DAI", "دای", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("PYUSD", "پی پل یو اس دی", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("RLUSD", "ریپل یو اس دی", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USDE", "یو اس دی ای", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USDD", "یو اس دی دی", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USDG", "گلوبال دلار", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USD1", "یو اس دی وان", ASSET_CATEGORY_CRYPTO),
    // سرویس این را ندارد - ثبتِ دستی آری، قیمتِ روز نه.
    AssetCatalogEntry("TON", "تون", ASSET_CATEGORY_CRYPTO, hasLivePrice = false),
)

val fiatAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("USD", "دلار", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("EUR", "یورو", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("AED", "درهم امارات", ASSET_CATEGORY_FIAT),
    // سرویس این سه را ندارد - ثبتِ دستی آری، قیمتِ روز نه.
    AssetCatalogEntry("CAD", "دلار کانادا", ASSET_CATEGORY_FIAT, hasLivePrice = false),
    AssetCatalogEntry("GBP", "پوند", ASSET_CATEGORY_FIAT, hasLivePrice = false),
    AssetCatalogEntry("TRY", "لیر ترکیه", ASSET_CATEGORY_FIAT, hasLivePrice = false),
)

val goldAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("GOLD_24", "یک گرم طلای ۲۴ عیار", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("GOLD_18", "یک گرم طلای ۱۸ عیار", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("GOLD_MESGHAL", "مثقال طلا", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SEKKE_EMAMI", "سکه امامی", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SEKKE_AZADI", "سکه تمام آزادی", ASSET_CATEGORY_GOLD),
    // سرویس این چهار را ندارد - ثبتِ دستی آری، قیمتِ روز نه.
    AssetCatalogEntry("NIM_SEKKE", "نیم سکه", ASSET_CATEGORY_GOLD, hasLivePrice = false),
    AssetCatalogEntry("ROB_SEKKE", "ربع سکه", ASSET_CATEGORY_GOLD, hasLivePrice = false),
    AssetCatalogEntry("SEKKE_GERAMI", "سکه گرمی", ASSET_CATEGORY_GOLD, hasLivePrice = false),
    AssetCatalogEntry("SILVER_999", "یک گرم نقره ۹۹۹", ASSET_CATEGORY_GOLD, hasLivePrice = false),
)

/** هر سه دسته، به همون ترتیبی که تو انتخابگرِ «نوع دارایی» نشون داده می‌شن. */
val assetCatalogGroups: List<Pair<String, List<AssetCatalogEntry>>> = listOf(
    "ارزهای دیجیتال" to cryptoAssets,
    "ارزهای فیزیکی" to fiatAssets,
    "طلا، سکه و نقره" to goldAssets,
)

fun assetCategoryLabel(category: String): String = when (category) {
    ASSET_CATEGORY_CRYPTO -> "ارز دیجیتال"
    ASSET_CATEGORY_FIAT -> "ارز فیزیکی"
    ASSET_CATEGORY_GOLD -> "طلا و سکه"
    else -> "دلخواه"
}
