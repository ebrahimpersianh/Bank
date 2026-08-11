package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.AssetDao
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.data.db.AssetTradeDao
import ir.sadteam.loancalc.data.db.AssetTradeEntity
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
     * ⏳ **جای خالیِ عمدی**: وقتی کلیدِ APIِ سرویسِ قیمت رسید، اینجا قیمت‌ها گرفته و رو
     * `assets.unitPriceRial` نوشته می‌شن. طبقِ تصمیمِ ثبت‌شده، قیمت باید از **سرورِ خودمون**
     * (که کش می‌کنه، مثلِ `credit_rates`) گرفته بشه، نه مستقیم از سرویس - وگرنه کلید تو اپ لو می‌ره.
     * تا اون موقع این تابع عمداً کاری نمی‌کنه و اپ همه‌جا «—» نشون می‌ده.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun refreshPrices(token: String?) = Unit

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
