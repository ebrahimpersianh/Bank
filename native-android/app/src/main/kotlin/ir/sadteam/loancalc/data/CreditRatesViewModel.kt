package ir.sadteam.loancalc.data

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.CreditRateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نرخِ خدمات اعتباری (creditServices تو Banks.kt) قبلاً همیشه هاردکدِ تو خودِ اپ بود؛ الان از سرور
 * (GET /api/credit-rates) تازه می‌شه تا با تغییرِ نرخِ واقعیِ دیجی‌پی/اسنپ‌پی و ... نیازی به نسخه‌ی
 * جدیدِ اپ نباشه. [rates] همیشه با همون لیستِ استاتیکِ Banks.kt شروع می‌شه (بدون تاخیر/پرش تو UI و
 * کاملاً آفلاین‌سیف)، و فقط اگه فراخوانیِ شبکه موفق بود جایگزین می‌شه؛ خطا (بی‌اینترنتی، سرور پایین)
 * عمداً نادیده گرفته می‌شه و همون fallback باقی می‌مونه.
 */
@HiltViewModel
class CreditRatesViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {
    private val _rates = MutableStateFlow(creditServices)
    val rates: StateFlow<List<BankEntry>> = _rates.asStateFlow()

    // فقط برای انیمیشنِ shimmer تو BankLoanScreen - تا وقتی fetch از سرور تموم نشده true می‌مونه
    // (حتی موقعِ fallback به داده‌ی استاتیک، چون UI نمی‌دونه شکستِ شبکه پیش اومده یا هنوز در حالِ رفتنه).
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val fetched = apiService.getCreditRates().rates
                if (fetched.isNotEmpty()) {
                    _rates.value = fetched.map { it.toBankEntry() }
                }
            } catch (e: Exception) {
                // بی‌صدا نادیده گرفته می‌شه - fallback استاتیک همچنان معتبره
            } finally {
                _isLoading.value = false
            }
        }
    }
}

private fun CreditRateDto.toBankEntry(): BankEntry = BankEntry(
    name = name,
    color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFF888888)),
    logoAsset = logoAsset,
    ratePct = ratePct,
    months = months,
    minAmount = minAmount,
    maxAmount = maxAmount,
)
