package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.data.AssetRepository
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** ارزهای ورودی: نماد، نام. «تتر» هم هست چون خیلی‌ها با آن حساب می‌کنند. */
private val CURRENCIES = listOf("USD" to "دلار", "EUR" to "یورو", "AED" to "درهم", "USDT" to "تتر", "TRY" to "لیر", "GBP" to "پوند", "CAD" to "دلار کانادا")

@HiltViewModel
class CurrencyRateViewModel @Inject constructor(private val assets: AssetRepository) : ViewModel() {
    private val _prices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val prices: StateFlow<Map<String, Double>> = _prices
    init { viewModelScope.launch { _prices.value = assets.marketPrices() } }
}

/**
 * 💱 **مبلغ به ارز** (۸ مهر، «چند ارز»): کاربر مبلغ را به دلار/یورو/... می‌زند، با نرخِ روز (قابلِ
 * ویرایش؛ برای ارزِ بی‌قیمت دستی) به تومان تبدیل می‌شود. دیتابیس همچنان فقط ریال دارد - ارزِ اصلی
 * فقط در شرحِ تراکنش می‌ماند («۱۰۰ دلار × ۶۵٬۰۰۰»)، پس هیچ جمع و گزارشی عوض نمی‌شود.
 */
@Composable
fun CurrencyAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (toman: Long, note: String) -> Unit,
    vm: CurrencyRateViewModel = hiltViewModel(),
) {
    val prices by vm.prices.collectAsState()
    var code by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    // نرخِ روز به تومان (سرور ریال می‌دهد)؛ با عوض شدنِ ارز یا رسیدنِ قیمت دوباره پر می‌شود.
    LaunchedEffect(code, prices) { rate = prices[code]?.let { (it / 10).toLong().toString() } ?: "" }
    val a = amount.toLongOrNull() ?: 0L
    val r = rate.toLongOrNull() ?: 0L
    val toman = a * r
    val name = CURRENCIES.first { it.first == code }.second

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مبلغ به ارز") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CURRENCIES) { (c, n) -> AppChip(label = n, selected = c == code, onClick = { code = c }) }
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = cleanNum(it).take(10) },
                    label = { Text("چند $name؟") }, singleLine = true, shape = AppFieldShape, colors = appFieldColors(),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = rate, onValueChange = { rate = cleanNum(it).take(10) },
                    label = { Text("نرخِ هر $name (تومان)") }, singleLine = true, shape = AppFieldShape, colors = appFieldColors(),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(if (prices.containsKey(code)) "نرخِ روز؛ اگر به نرخِ دیگری خریدی عوضش کن" else "نرخِ روزِ این ارز را نداریم؛ دستی بزن") },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (toman > 0) "= ${toman.toFaMoney()} تومان" else "مبلغ و نرخ را بزن",
                    color = if (toman > 0) AppPrimary else AppMuted, fontSize = 16.sp, fontWeight = FontWeight.Black,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = toman > 0, onClick = { onConfirm(toman, "${a.toFaMoney()} $name × ${r.toFaMoney()}") }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = AppText) } },
    )
}
