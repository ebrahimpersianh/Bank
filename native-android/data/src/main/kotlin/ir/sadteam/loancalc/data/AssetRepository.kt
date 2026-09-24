package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.AssetDao
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.data.db.AssetTradeDao
import ir.sadteam.loancalc.data.db.AssetTradeEntity
import ir.sadteam.loancalc.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * دارایی‌های غیرنقدی (طلا/سکه/ارز/رمزارز/عنوانِ دلخواه).
 *
 * **قاعده‌ی محوری**: مقدارِ هر دارایی هیچ‌وقت به‌صورتِ یه عددِ ذخیره‌شده نگه داشته نمی‌شه؛ همیشه از
 * جمعِ خرید/فروش‌ها حساب می‌شه ([quantityOf]). دقیقاً همون درسی که از باگِ `paidCount` گرفتیم:
 * یه عدد نباید هم‌زمان تو دو جا زندگی کنه (رجوع کن به CLAUDE.md).
 */
class AssetRepository(
    private val assetDao: AssetDao,
    private val tradeDao: AssetTradeDao,
    private val apiService: ApiService,
) {
    fun observeAssets(): Flow<List<AssetEntity>> = assetDao.observeAll()
    fun observeTrades(): Flow<List<AssetTradeEntity>> = tradeDao.observeAll()
    fun observeTradesForAsset(assetId: Long): Flow<List<AssetTradeEntity>> = tradeDao.observeForAsset(assetId)

    /**
     * دارایی رو پیدا می‌کنه و اگه نبود می‌سازه، بعد یه معامله ثبت می‌کنه.
     *
     * همین «پیدا کن وگرنه بساز» چیزیه که باعث می‌شه خرید و فروشِ یه دارایی **تو یک ردیف** جمع بشن،
     * نه اینکه هر معامله یه ردیفِ جدا بسازه (خواسته‌ی صریحِ کاربر).
     */
    suspend fun recordTrade(
        symbol: String,
        name: String,
        category: String,
        isBuy: Boolean,
        quantity: Double,
        totalRial: Double,
        year: Int,
        month: Int,
        day: Int,
        description: String = "",
        unitPriceRial: Double? = null,
    ) {
        val existing = assetDao.findBySymbol(symbol)
        val assetId = existing?.id ?: System.currentTimeMillis()
        if (existing == null) {
            assetDao.upsert(
                AssetEntity(
                    id = assetId,
                    symbol = symbol,
                    name = name,
                    category = category,
                    unitPriceRial = unitPriceRial,
                    priceUpdatedAt = if (unitPriceRial != null) isoNow() else null,
                    createdAt = isoNow(),
                ),
            )
        } else if (unitPriceRial != null) {
            assetDao.upsert(existing.copy(unitPriceRial = unitPriceRial, priceUpdatedAt = isoNow()))
        }
        tradeDao.upsert(
            AssetTradeEntity(
                // +1 تا با idِ خودِ دارایی (که تو همین میلی‌ثانیه ساخته شده) برخورد نکنه.
                id = System.currentTimeMillis() + 1,
                assetId = assetId,
                isBuy = isBuy,
                quantity = quantity,
                totalRial = totalRial,
                year = year,
                month = month,
                day = day,
                description = description,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        tradeDao.deleteForAsset(asset.id)
        assetDao.delete(asset)
    }

    suspend fun deleteTrade(trade: AssetTradeEntity) = tradeDao.delete(trade)

    suspend fun clearLocal() {
        tradeDao.clear()
        assetDao.clear()
    }

    /** مقدارِ فعلی = جمعِ خریدها − جمعِ فروش‌ها. */
    fun quantityOf(assetId: Long, trades: List<AssetTradeEntity>): Double =
        trades.filter { it.assetId == assetId }
            .sumOf { if (it.isBuy) it.quantity else -it.quantity }

    /** خالصِ ریالی که تا حالا برای این دارایی خرج شده (خرید منهای فروش) - مبنای سود/زیان. */
    fun netCostOf(assetId: Long, trades: List<AssetTradeEntity>): Double =
        trades.filter { it.assetId == assetId }
            .sumOf { if (it.isBuy) it.totalRial else -it.totalRial }

    /** ارزشِ روزِ دارایی؛ `null` یعنی هنوز قیمتی نداریم (سرویسِ قیمت وصل نشده) - UI باید «—» نشون بده. */
    fun currentValueOf(asset: AssetEntity, trades: List<AssetTradeEntity>): Double? {
        val price = asset.unitPriceRial ?: return null
        return quantityOf(asset.id, trades) * price
    }

    /**
     * گرفتنِ قیمتِ روز از **سرورِ خودمون** (نه مستقیم از سرویسِ بیرونی - وگرنه کلید تو اپ لو می‌ره)
     * و نوشتنش رو `assets.unitPriceRial`.
     *
     * کلیدهای جوابِ سرور دقیقاً همون `symbol`ِ کاتالوگن (`BTC`, `GOLD_18`, ...) چون **نگاشتِ
     * نمادها سمتِ سروره** - اگه اسمِ نمادی از سرویس عوض شد، با یه دیپلویِ سرور درست می‌شه و
     * کاربر لازم نیست اپ رو آپدیت کنه.
     *
     * شکستِ شبکه عمداً بی‌صداست: قیمتِ قبلی سرِ جاش می‌مونه و دارایی‌هایی که هیچ‌وقت قیمت
     * نگرفتن «—» نشون می‌دن. عمومیه و توکن نمی‌خواد.
     */
    suspend fun refreshPrices() {
        val prices = runCatching { apiService.getPrices().prices }.getOrNull() ?: return
        if (prices.isEmpty()) return
        val now = isoNow()
        assetDao.getAll().forEach { asset ->
            val price = prices[asset.symbol] ?: return@forEach
            if (asset.unitPriceRial != price) {
                assetDao.upsert(asset.copy(unitPriceRial = price, priceUpdatedAt = now))
            }
        }
    }

    /**
     * قیمتِ روزِ **همه‌ی** نمادها (نه فقط دارایی‌های ثبت‌شده‌ی کاربر) - برای صفحه‌ی «قیمتِ روز»
     * و انتخابگرِ نوعِ دارایی.
     *
     * همون `GET /api/prices`ِ [refreshPrices]ه و **یک درخواسته**، پس حلقه رو نمادها لازم نیست.
     * شکستِ شبکه = نگاشتِ خالی؛ صدازننده باید مقدارِ قبلی رو نگه داره نه اینکه خالی نشون بده.
     */
    suspend fun marketPrices(): Map<String, Double> =
        runCatching { apiService.getPrices().prices }.getOrDefault(emptyMap())

    /**
     * تاریخچه‌ی روزانه‌ی یه نماد (قدیمی → جدید) برای نمودار و مقایسه‌ی «نسبت به ماهِ قبل».
     * لیستِ خالی یعنی سرور هنوز برای این نماد تاریخچه‌ای جمع نکرده - UI باید «—» بذاره.
     */
    suspend fun priceHistory(symbol: String, days: Int = 30): List<Pair<String, Double>> =
        runCatching {
            apiService.getPriceHistory(symbol, days).points.map { it.date to it.price }
        }.getOrDefault(emptyList())

    /**
     * درصدِ تغییرِ قیمت نسبت به قدیمی‌ترین نقطه‌ی بازه؛ `null` یعنی تاریخچه‌ی کافی نداریم.
     * خواسته‌ی کاربر: هشدارِ گرون‌شدنِ ارزِ دیجیتال نسبت به ماهِ قبل.
     */
    suspend fun priceChangePercent(symbol: String, days: Int = 30): Double? {
        val points = priceHistory(symbol, days)
        if (points.size < 2) return null
        val first = points.first().second
        val last = points.last().second
        if (first <= 0.0) return null
        return (last - first) / first * 100.0
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
