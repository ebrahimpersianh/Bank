package ir.sadteam.loancalc.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.AssetRepository
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.data.db.AssetTradeEntity
import ir.sadteam.loancalc.ui.jibak.toFaTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
) : ViewModel() {
    val assets: StateFlow<List<AssetEntity>> = assetRepository.observeAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trades: StateFlow<List<AssetTradeEntity>> = assetRepository.observeTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * درصدِ تغییرِ ۳۰ روزه‌ی هر نماد - کلید نمادِ کاتالوگه. خالی‌بودنش یعنی سرور هنوز تاریخچه
     * جمع نکرده (تاریخچه از روزِ راه‌اندازیِ سرویس به بعد پر می‌شه) و UI باید «—» بذاره.
     */
    private val _monthChange = MutableStateFlow<Map<String, Double>>(emptyMap())
    val monthChange: StateFlow<Map<String, Double>> = _monthChange

    /** کشِ تاریخچه‌ی قیمت برای نمودارِ `42a` - هر نماد یک‌بار خونده می‌شه و اینجا می‌مونه. */
    private val _history = MutableStateFlow<Map<String, List<PricePoint>>>(emptyMap())

    /**
     * قیمتِ روزِ **همه‌ی** نمادهای کاتالوگ برای فریمِ `43a` - نه فقط دارایی‌های کاربر.
     * `unitPriceRial`ِ `AssetEntity` فقط روی دارایی‌های ثبت‌شده می‌نشینه، پس این جدا لازمه.
     * یک درخواسته، پس صفحه‌ی قیمت هر بار باز شدن تازه‌ش می‌کنه.
     */
    private val _marketPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val marketPrices: StateFlow<Map<String, Double>> = _marketPrices

    /** لحظه‌ی آخرین به‌روزرسانیِ موفق. null یعنی هنوز یک‌بار هم نگرفته‌ایم. */
    private val _pricesUpdatedAt = MutableStateFlow<Long?>(null)

    /**
     * **ساعتِ** آخرین به‌روزرسانی («۱۲:۳۰»)، برای زیرنویسِ سرصفحه‌ی `43a`.
     *
     * خواسته‌ی صریحِ کاربر: «به‌روزرسانی تایمِ واقعی بشه، مثلاً ۱۲:۳۰ به‌روز شد» - نه
     * «۶ دقیقه پیش». فاصله‌ی نسبی مزیتی نداشت و بدترش این‌که **خودش تیک نمی‌زد**، پس اگر
     * صفحه باز می‌ماند عدد سرِ جایش خشک می‌شد و دروغ می‌گفت. ساعتِ ثابت این مشکل را ندارد.
     */
    val pricesUpdatedClock: StateFlow<String?> = _pricesUpdatedAt
        .map { at ->
            at?.let {
                val c = java.util.Calendar.getInstance().apply { timeInMillis = it }
                toFaTime(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        refreshPrices()
        startPriceAutoRefresh()
    }

    /**
     * سرور هر نیم‌ساعت قیمت‌ها را از سرویسِ بیرونی می‌گیرد، پس برنامه هم هر نیم‌ساعت
     * یک‌بار می‌پرسد. تندتر پرسیدن جوابِ تازه‌تری نمی‌دهد.
     *
     * ⚠️ حلقه به عمرِ ViewModel بسته است، پس با بسته‌شدنِ تب هم ادامه دارد و کاربر با
     * برگشتن قیمتِ تازه می‌بیند. بارِ شبکه‌اش یک درخواستِ نیم‌ساعتی است.
     */
    private fun startPriceAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(30 * 60 * 1000L)
                refreshPrices()
            }
        }
    }

    /**
     * قیمتِ روزِ یک نماد — **اول** ستونِ خودِ دارایی، بعد جدولِ بازار.
     *
     * این جانشین همان چیزی است که باعث می‌شد عددِ داراییِ تازه‌ثبت‌شده درجا نیاید:
     * `unitPriceRial` روی سطرِ دیتابیس فقط بعد از یک دورِ `refreshPrices` پر می‌شود، پس
     * کاربر ثبت می‌کرد، «—» می‌دید، از تب بیرون می‌رفت و برمی‌گشت تا عدد بیاید.
     * جدولِ بازار همان لحظه قیمت را دارد.
     */
    fun priceOf(asset: AssetEntity): Double? =
        asset.unitPriceRial ?: _marketPrices.value[asset.symbol]

    /**
     * قیمتِ روز از سرورِ خودمون. بی‌صدا شکست می‌خوره (آفلاین = قیمتِ قبلی می‌مونه) و چون سرور
     * خودش ساعتی یک‌بار از سرویسِ بیرونی می‌گیره، صدازدنِ مکررش هزینه‌ای نداره.
     *
     * درصدِ تغییرِ هر نماد **موازی** حساب می‌شه - قبلاً حلقه‌ی ترتیبی با ده دارایی ده
     * رفت‌وبرگشتِ پشتِ‌هم بود.
     */
    fun refreshPrices() {
        viewModelScope.launch {
            assetRepository.refreshPrices()
            // قیمتِ کلِ بازار **یک درخواسته**، پس حلقه روی نمادها لازم نیست.
            // شکستِ شبکه = قیمتِ قبلی می‌مونه، نه فهرستِ خالی.
            _marketPrices.value = runCatching { assetRepository.marketPrices() }
                .getOrDefault(_marketPrices.value)
            _pricesUpdatedAt.value = System.currentTimeMillis()
            // ifEmpty چون تو فراخوانیِ init هنوز StateFlowِ assets پر نشده.
            val current = assets.value.ifEmpty { assetRepository.observeAssets().first() }
            _monthChange.value = current
                .map { asset -> async { asset.symbol to assetRepository.priceChangePercent(asset.symbol) } }
                .awaitAll()
                .mapNotNull { (symbol, pct) -> pct?.let { symbol to it } }
                .toMap()
        }
    }

    /**
     * تاریخچه‌ی قیمت برای نمودارِ `42a`. هر نماد یک‌بار خونده می‌شه و تو `StateFlow` می‌مونه.
     * فهرستِ خالی یا کوتاه حالتِ عادیه (سرور فقط از روزِ راه‌اندازیِ سرویس تاریخچه داره) نه
     * خطا - `AssetSparkline` هر سه حالت رو خودش می‌گیره.
     */
    fun historyOf(symbol: String, days: Int = 30): Flow<List<PricePoint>> {
        if (_history.value[symbol] == null) {
            viewModelScope.launch {
                val points = runCatching { assetRepository.priceHistory(symbol, days) }
                    .getOrDefault(emptyList())
                    .mapNotNull { (date, price) -> date.toPricePoint(price) }
                _history.update { it + (symbol to points) }
            }
        }
        return _history.map { it[symbol].orEmpty() }
    }

    fun recordTrade(
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
        viewModelScope.launch {
            assetRepository.recordTrade(
                symbol, name, category, isBuy, quantity, totalRial,
                year, month, day, description, unitPriceRial,
            )
            // ستونِ unitPriceRial این دارایی را همین حالا پر می‌کند تا ردیفش با عدد
            // بیاید، نه با «—» تا دورِ بعدیِ نیم‌ساعته.
            refreshPrices()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch { assetRepository.deleteAsset(asset) }
    }

    fun quantityOf(assetId: Long, allTrades: List<AssetTradeEntity>): Double =
        assetRepository.quantityOf(assetId, allTrades)

    fun netCostOf(assetId: Long, allTrades: List<AssetTradeEntity>): Double =
        assetRepository.netCostOf(assetId, allTrades)

    /** null یعنی قیمتِ روز هنوز در دسترس نیست - UI باید «—» نشون بده، نه صفر. */
    fun currentValueOf(asset: AssetEntity, allTrades: List<AssetTradeEntity>): Double? =
        assetRepository.currentValueOf(asset, allTrades)

    /** ارزشِ کلِ سبد؛ null یعنی هیچ‌کدوم از دارایی‌ها قیمت ندارن. */
    fun totalValue(allAssets: List<AssetEntity>, allTrades: List<AssetTradeEntity>): Double? {
        val values = allAssets.mapNotNull { assetRepository.currentValueOf(it, allTrades) }
        return if (values.isEmpty()) null else values.sum()
    }
}

/**
 * «۱۴۰۴-۰۶-۱۰» یا «۲۰۲۶-۰۸-۳۱» (میلادیِ سرور) → [PricePoint]ِ جلالی. تاریخِ نامعتبر
 * `null` برمی‌گردونه و همون نقطه بی‌صدا از نمودار حذف می‌شه.
 */
private fun String.toPricePoint(priceRial: Double): PricePoint? {
    val parts = take(10).split("-")
    if (parts.size != 3) return null
    val gy = parts[0].toIntOrNull() ?: return null
    val gm = parts[1].toIntOrNull() ?: return null
    val gd = parts[2].toIntOrNull() ?: return null
    val jalali = runCatching { JalaliCalendar.fromGregorian(gy, gm, gd) }.getOrNull() ?: return null
    return PricePoint(jalali.y, jalali.m, jalali.d, priceRial)
}
