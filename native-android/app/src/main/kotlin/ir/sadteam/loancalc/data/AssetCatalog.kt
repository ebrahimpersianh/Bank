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
)

val cryptoAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("BTC", "بیت کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("ETH", "اتریوم", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("USDT", "تتر", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("XAUT", "تتر گلد", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("BNB", "بایننس کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("TRX", "ترون", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("LTC", "لایت کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("SOL", "سولانا", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("XRP", "ریپل", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("DOGE", "دوج کوین", ASSET_CATEGORY_CRYPTO),
    AssetCatalogEntry("TON", "تون", ASSET_CATEGORY_CRYPTO),
)

val fiatAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("USD", "دلار", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("EUR", "یورو", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("CAD", "دلار کانادا", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("GBP", "پوند", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("TRY", "لیر ترکیه", ASSET_CATEGORY_FIAT),
    AssetCatalogEntry("AED", "درهم امارات", ASSET_CATEGORY_FIAT),
)

val goldAssets: List<AssetCatalogEntry> = listOf(
    AssetCatalogEntry("GOLD_24", "یک گرم طلای ۲۴ عیار", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("GOLD_18", "یک گرم طلای ۱۸ عیار", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SILVER_999", "یک گرم نقره ۹۹۹", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SEKKE_EMAMI", "سکه امامی", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SEKKE_AZADI", "سکه تمام آزادی", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("NIM_SEKKE", "نیم سکه", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("ROB_SEKKE", "ربع سکه", ASSET_CATEGORY_GOLD),
    AssetCatalogEntry("SEKKE_GERAMI", "سکه گرمی", ASSET_CATEGORY_GOLD),
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
