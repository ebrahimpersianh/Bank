package ir.sadteam.loancalc.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AssetRepository
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.data.db.AssetTradeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

    init {
        refreshPrices()
    }

    /**
     * قیمتِ روز از سرورِ خودمون. بی‌صدا شکست می‌خوره (آفلاین = قیمتِ قبلی می‌مونه) و چون سرور
     * خودش ساعتی یک‌بار از سرویسِ بیرونی می‌گیره، صدازدنِ مکررش هزینه‌ای نداره.
     */
    fun refreshPrices() {
        viewModelScope.launch {
            assetRepository.refreshPrices()
            val changes = mutableMapOf<String, Double>()
            assetRepository.observeAssets().first().forEach { asset ->
                assetRepository.priceChangePercent(asset.symbol)?.let { changes[asset.symbol] = it }
            }
            _monthChange.value = changes
        }
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
