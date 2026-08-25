package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.assetCategoryLabel
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.components.EmptyState
import androidx.compose.material.icons.outlined.SwapHoriz

/**
 * جزئیاتِ یه دارایی - مقدارِ فعلی، ارزشِ روز (اگه قیمت داشته باشیم)، هزینه‌ی خالص، و تاریخچه‌ی
 * کاملِ خرید/فروش.
 *
 * ⏳ **نمودارِ قیمت هنوز نیست**: نمودار به تاریخچه‌ی قیمتِ بازار نیاز داره که با همون سرویسِ
 * قیمتِ لحظه‌ای میاد. تا اون موقع به‌جاش تاریخچه‌ی معاملاتِ خودِ کاربر نشون داده می‌شه که داده‌ی
 * واقعی و مفیدیه، نه یه نمودارِ خالیِ الکی.
 */
@Composable
fun AssetDetailScreen(
    asset: AssetEntity,
    onBack: () -> Unit,
    viewModel: AssetViewModel,
) {
    val allTrades by viewModel.trades.collectAsState()
    val trades = remember(allTrades, asset.id) { allTrades.filter { it.assetId == asset.id } }
    val quantity = viewModel.quantityOf(asset.id, allTrades)
    val netCost = viewModel.netCostOf(asset.id, allTrades)
    val value = viewModel.currentValueOf(asset, allTrades)
    var showDelete by remember { mutableStateOf(false) }
    var showTrade by remember { mutableStateOf(false) }
    var sellMode by remember { mutableStateOf(false) }

    if (showTrade) {
        AssetTradeSheet(
            onDismiss = { showTrade = false },
            viewModel = viewModel,
            presetSymbol = asset.symbol,
            presetName = asset.name,
            presetCategory = asset.category,
            startWithSell = sellMode,
        )
        return
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            title = "حذفِ دارایی",
            text = "«${asset.name}» و همه‌ی خرید/فروش‌هاش حذف بشن؟",
            onConfirm = { viewModel.deleteAsset(asset); onBack() },
            onDismiss = { showDelete = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(
                    asset.name,
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                }
            }
        }

        item {
            AppCard {
                Text(
                    "${formatQuantity(quantity)} ${asset.name}",
                    color = AppText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(assetCategoryLabel(asset.category), color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                Text(
                    if (value == null) "ارزشِ روز: —" else "ارزشِ روز: ${fmt(value)} ریال",
                    color = if (value == null) AppMuted else AppPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    if (netCost >= 0) "خالصِ پرداختی: ${fmt(netCost)} ریال" else "خالصِ دریافتی: ${fmt(-netCost)} ریال",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (value != null) {
                    val profit = value - netCost
                    Text(
                        if (profit >= 0) "سود: ${fmt(profit)} ریال" else "زیان: ${fmt(-profit)} ریال",
                        color = if (profit >= 0) AppPrimary else AppDanger,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    onClick = { sellMode = false; showTrade = true },
                    modifier = Modifier.weight(1f),
                ) { Text("خرید") }
                GradientButton(
                    onClick = { sellMode = true; showTrade = true },
                    modifier = Modifier.weight(1f),
                ) { Text("فروش") }
            }
        }

        item {
            Text("تاریخچه", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }

        // بخشِ ۳۳ فایلِ طراحی (جدولِ `33d`، ردیفِ «جزئیاتِ دارایی»).
        if (trades.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.SwapHoriz,
                    title = "این دارایی هنوز خرید و فروشی ندارد",
                    description = "اولین خرید را ثبت کن تا سود و زیان حساب شود.",
                    actionLabel = "ثبتِ خرید",
                    onAction = { sellMode = false; showTrade = true },
                )
            }
        }

        items(trades, key = { it.id }) { trade ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${if (trade.isBuy) "خرید" else "فروش"} ${formatQuantity(trade.quantity)} ${asset.name}",
                            color = if (trade.isBuy) AppPrimary else AppDanger,
                            fontSize = 13.sp,
                        )
                        Text(
                            "${toFa(trade.day)} ${persianMonthName(trade.month)} ${toFa(trade.year)}",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text("${fmt(trade.totalRial)} ریال", color = AppText, fontSize = 12.sp)
                }
            }
        }
    }
}
