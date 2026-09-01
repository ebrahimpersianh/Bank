package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
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
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.assetCategoryLabel
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/**
 * بخشِ «دارایی من» - دارایی‌های غیرنقدی (طلا، سکه، ارز، رمزارز، عنوانِ دلخواه).
 *
 * قیمتِ روز از سرورِ خودمون میاد ([AssetViewModel.refreshPrices])؛ دارایی‌ای که هنوز قیمت نگرفته
 * (نمادش رو سرویسِ قیمت نیست) «—» نشون می‌ده به‌جای صفر. برای **رمزارز**، اگه نسبت به ماهِ قبل
 * گران‌تر شده باشه یه بجِ کوچیکِ زرد کنارِ اسمش می‌شینه (خواسته‌ی صریحِ کاربر).
 */
@Composable
fun AssetSection(viewModel: AssetViewModel = hiltViewModel()) {
    val assets by viewModel.assets.collectAsState()
    val trades by viewModel.trades.collectAsState()
    val monthChange by viewModel.monthChange.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    var showTradeSheet by remember { mutableStateOf(false) }
    var detailAsset by remember { mutableStateOf<AssetEntity?>(null) }

    if (showTradeSheet) {
        AssetTradeSheet(
            onDismiss = { showTradeSheet = false },
            viewModel = viewModel,
        )
        return
    }

    detailAsset?.let { asset ->
        AssetDetailScreen(
            asset = asset,
            onBack = { detailAsset = null },
            viewModel = viewModel,
        )
        return
    }

    val totalValue = remember(assets, trades) { viewModel.totalValue(assets, trades) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(label = "دارایی من") {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        // «—» یعنی قیمتِ روز نداریم، نه اینکه دارایی صفره - این تفاوت مهمه.
                        if (totalValue == null) "—" else "${maskIfPrivate(masked, fmt(totalValue))} ریال",
                        color = AppText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Text(
                    if (totalValue == null) {
                        "قیمتِ روز هنوز وصل نشده - مقدارِ دارایی‌هات ثبت می‌شه و به‌محضِ وصل‌شدنِ قیمت، ارزششون هم میاد."
                    } else {
                        "${toFa(assets.size)} دارایی"
                    },
                    color = AppMuted,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        if (assets.isEmpty()) {
            item {
                AppCard {
                    EmptyState(
                        icon = Icons.Filled.Diamond,
                        title = "هنوز دارایی ثبت نکردی",
                        description = "دارایی‌های غیرنقدیت مثلِ طلا، سکه، ارز و رمزارز رو ثبت کن تا یه‌جا حواست بهشون باشه.",
                    )
                }
            }
        } else {
            items(assets, key = { it.id }) { asset ->
                AssetRow(
                    asset = asset,
                    quantity = viewModel.quantityOf(asset.id, trades),
                    value = viewModel.currentValueOf(asset, trades),
                    // خواسته‌ی کاربر: فقط رمزارز، فقط وقتی نسبت به ماهِ قبل گران‌تر شده.
                    monthChangePercent = if (asset.category == ASSET_CATEGORY_CRYPTO) {
                        monthChange[asset.symbol]?.takeIf { it > 0.0 }
                    } else {
                        null
                    },
                    privacyMode = privacyMode,
                    onClick = { detailAsset = asset },
                )
            }
        }

        item {
            GradientButton(onClick = { showTradeSheet = true }, modifier = Modifier.fillMaxWidth()) {
                Text("افزودنِ دارایی")
            }
        }
    }
}

@Composable
private fun AssetRow(
    asset: AssetEntity,
    quantity: Double,
    value: Double?,
    monthChangePercent: Double?,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    AppCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(AppPrimaryPill, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${formatQuantity(quantity)} ${asset.name}",
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (monthChangePercent != null) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .background(AppWarningPill, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                "+${toFa(String.format("%.1f", monthChangePercent))}٪",
                                color = AppWarningInk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(assetCategoryLabel(asset.category), color = AppMuted, fontSize = 11.sp)
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    if (value == null) "—" else "${maskIfPrivate(masked, fmt(value))} ریال",
                    color = if (value == null) AppMuted else AppText,
                    fontSize = 12.5.sp,
                )
            }
        }
    }
}

/** مقدارِ دارایی معمولاً کسریه (۱.۲ بیت‌کوین) ولی سکه/ارز اغلب صحیحه - عددِ صحیح بدونِ «٫۰». */
internal fun formatQuantity(q: Double): String =
    if (q == q.toLong().toDouble()) toFa(q.toLong().toInt()) else toFa(String.format("%.4f", q).trimEnd('0').trimEnd('.'))

/** [toFa] فقط عدد می‌گیره؛ برای رشته‌ی اعشاری رقم‌به‌رقم تبدیل می‌شه. */
private fun toFa(s: String): String {
    val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString { s.forEach { c -> append(if (c in '0'..'9') fa[c - '0'] else c) } }
}
