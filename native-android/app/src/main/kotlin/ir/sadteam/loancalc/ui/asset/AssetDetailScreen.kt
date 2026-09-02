package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.assetCategoryLabel
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.data.db.AssetTradeEntity
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faMonthName
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFaDate
import ir.sadteam.loancalc.ui.jibak.toFaSignedCompact
import ir.sadteam.loancalc.ui.theme.AppDangerBorder
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * جزئیاتِ یه دارایی - فریمِ `42a`، جای `29g`.
 *
 * ⚠️ کارتِ خلاصه **پالتِ دسته‌اش** رو می‌گیره (`groupPalette`)، نه همیشه کاغذِ طلایی -
 * قبلاً هر دارایی (حتی دلار/بیت‌کوین) کاغذِ طلایی با سکه می‌گرفت.
 * «فروش» دکمه‌ی خطیه نه پرِ قرمز - فروش کارِ مخربی نیست، قرمزِ پُر برای عملِ مخربه.
 * واحد: کارتِ خلاصه «تومان»، ردیف‌های تاریخچه بی‌واحد.
 */
@Composable
fun AssetDetailScreen(
    asset: AssetEntity,
    onBack: () -> Unit,
    viewModel: AssetViewModel,
) {
    val allTrades by viewModel.trades.collectAsState()
    val monthChange by viewModel.monthChange.collectAsState()
    // historyOf هر بار یه Flowِ تازه از _history.map{} می‌سازه؛ بی remember هر ری‌کامپوز
    // اشتراکِ قبلی رو لغو و یکی تازه باز می‌کنه و نمودار یه فریم به خالی می‌پره.
    val historyFlow = remember(asset.symbol) { viewModel.historyOf(asset.symbol) }
    val history by historyFlow.collectAsState(initial = emptyList())

    val trades = remember(allTrades, asset.id) {
        allTrades.filter { it.assetId == asset.id }.sortedWith(
            compareByDescending<AssetTradeEntity> { it.year }
                .thenByDescending { it.month }.thenByDescending { it.day },
        )
    }
    val quantity = viewModel.quantityOf(asset.id, allTrades)
    val netCost = viewModel.netCostOf(asset.id, allTrades)
    val value = viewModel.currentValueOf(asset, allTrades)
    val change = monthChange[asset.symbol]

    var showDelete by remember { mutableStateOf(false) }
    var showTrade by remember { mutableStateOf(false) }
    var sellMode by remember { mutableStateOf(false) }

    if (showDelete) {
        ConfirmDeleteDialog(
            title = "حذفِ دارایی",
            text = "«${asset.name}» و همه‌ی خرید/فروش‌هایش حذف بشن؟",
            onConfirm = { viewModel.deleteAsset(asset); onBack() },
            onDismiss = { showDelete = false },
        )
    }

    // شیتِ خرید/فروش **روی** صفحه می‌شینه نه به‌جایش. قبلاً با return صدا زده می‌شد و
    // صفحه‌ی زیرین اصلاً رندر نمی‌شد: پشتِ شیت سفیدِ خالی بود و اسکرولِ LazyColumn با
    // بستنش صفر می‌شد. همون باگی که تو تبِ دارایی و خانه رفع شد.
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SquareIconButton(
                    icon = Icons.Filled.ArrowForward,
                    description = "بازگشت",
                    fill = AppIconFrame,
                    border = AppLine,
                    ink = AppMuted,
                    onClick = onBack,
                )
                Text(
                    asset.name,
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                SquareIconButton(
                    icon = Icons.Filled.Delete,
                    description = "حذف",
                    fill = AppDangerPill,
                    border = AppDangerBorder,
                    ink = AppDangerInk,
                    onClick = { showDelete = true },
                )
            }
        }

        item { AssetSummaryCard(asset, quantity, netCost, value, change) }

        item { AssetSparkline(points = history) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "خرید",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .hardShadow(AppPrimaryDim, 4.dp, AppRadius.button)
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(AppPrimary)
                        .pressScaleClickable { sellMode = false; showTrade = true }
                        .padding(vertical = 13.dp),
                )
                Text(
                    "فروش",
                    color = AppDangerInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(AppSurface)
                        .border(2.dp, AppDangerBorder, RoundedCornerShape(AppRadius.button))
                        .pressScaleClickable { sellMode = true; showTrade = true }
                        .padding(vertical = 13.dp),
                )
            }
        }

        item {
            Text(
                "تاریخچه",
                color = AppMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

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
            TradeRow(trade = trade, assetName = asset.name)
        }
    }

        if (showTrade) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AssetTradeSheet(
                    onDismiss = { showTrade = false },
                    viewModel = viewModel,
                    presetSymbol = asset.symbol,
                    presetName = asset.name,
                    presetCategory = asset.category,
                    startWithSell = sellMode,
                )
            }
        }
    }
}

@Composable
private fun SquareIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    fill: Color,
    border: Color,
    ink: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(AppRadius.icon))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(AppRadius.icon))
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = ink, modifier = Modifier.size(15.dp))
    }
}

/** کارتِ خلاصه - مقدار، بجِ درصد، ارزشِ روز، قیمتِ هر واحد و سود. پالتِ دسته‌اش رو می‌گیره. */
@Composable
private fun AssetSummaryCard(
    asset: AssetEntity,
    quantity: Double,
    netCost: Double,
    value: Double?,
    changePercent: Double?,
) {
    val p = groupPalette(asset.category)
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(13.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (p.paper != null) Modifier.background(p.paper) else Modifier.background(AppSurface))
            .border(if (p.paper != null) 1.5.dp else 2.dp, p.rowBorder, shape)
            .padding(15.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AssetBadge(asset.symbol, asset.category, 38.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatQuantity(quantity),
                    color = p.ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    if (quantity > 0 && netCost > 0) {
                        "میانگینِ خرید ${(netCost / quantity).rialToFaCompact()} هر واحد"
                    } else {
                        assetCategoryLabel(asset.category)
                    },
                    color = p.subInk,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // بجِ درصد. خالی‌بودنِ کلید یعنی سرور هنوز تاریخچه نداره - بج **نمی‌آد**، نه صفر.
            if (changePercent != null) PriceChangeBadge(changePercent)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(p.rowBorder))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    "ارزشِ روز",
                    color = p.subInk,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    value?.rialToFaCompact() ?: "—",
                    color = p.ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 1.dp),
                )
                Text(
                    "تومان",
                    color = p.subInk,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
                asset.unitPriceRial?.let { unit ->
                    Text(
                        "قیمتِ هر واحد ${unit.rialToFaCompact()}",
                        color = p.subInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (value != null) {
                val profit = value - netCost
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        if (profit >= 0) "سود" else "زیان",
                        color = p.subInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        profit.toLong().toFaSignedCompact(),
                        color = if (profit >= 0) AppPrimaryInk else AppDangerInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}


/** یک نقطه‌ی تاریخچه‌ی قیمت. `day/month` فقط برای برچسبِ دو سرِ محورِ افقی. */
data class PricePoint(val year: Int, val month: Int, val day: Int, val priceRial: Double)

/**
 * نمودارِ خطیِ ۳۰روزه - سه حالتِ دیتا:
 *
 * - **≥۲ نقطه** نمودار کشیده می‌شه. محورِ عمودی از کم‌ترین تا بیش‌ترینِ همون بازه، نه از صفر.
 * - **۱ نقطه** کارت می‌مونه، محتواش یه خطِ متن می‌شه.
 * - **صفر نقطه** کارت اصلاً نمیاد.
 */
@Composable
private fun AssetSparkline(points: List<PricePoint>) {
    if (points.isEmpty()) return

    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLineRow, shape)
            .padding(14.dp),
    ) {
        Text("۳۰ روزِ گذشته", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black)

        if (points.size == 1) {
            Text(
                "قیمت از امروز ثبت می‌شود. نمودار از فردا شکل می‌گیرد.",
                color = AppLabel,
                fontSize = 11.sp,
                lineHeight = 20.sp,
            )
            return@Column
        }

        val min = points.minOf { it.priceRial }
        val max = points.maxOf { it.priceRial }
        val span = (max - min).takeIf { it > 0.0 }
        val line = AppPrimary
        val fill = AppPrimaryPill

        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxWidth().height(78.dp)) {
                val w = size.width
                val h = size.height
                val stepX = if (points.size > 1) w / (points.size - 1) else w
                // بازه‌ی [6, h-6]. «+ 6f»ِ آخر «- 6f»ِ اول رو خنثی می‌کرد، پس کم‌ترین
                // نقطه روی y = h می‌افتاد و نصفِ خطِ ۲٫۶dp بیرونِ کانوس بریده می‌شد.
                fun yOf(pt: PricePoint): Float =
                    if (span == null) h / 2f
                    else (h - 6f) - ((pt.priceRial - min) / span).toFloat() * (h - 12f)

                val path = Path()
                val area = Path()
                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val y = yOf(pt)
                    if (i == 0) { path.moveTo(x, y); area.moveTo(x, y) } else { path.lineTo(x, y); area.lineTo(x, y) }
                }
                area.lineTo(w, h)
                area.lineTo(0f, h)
                area.close()
                drawPath(area, color = fill)
                drawPath(
                    path,
                    color = line,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                val last = points.last()
                drawCircle(Color.White, radius = 4.5.dp.toPx(), center = Offset(w, yOf(last)))
                drawCircle(line, radius = 3.dp.toPx(), center = Offset(w, yOf(last)))
            }
            Text(
                max.rialToFaCompact(),
                color = AppLabel,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart),
            )
            Text(
                min.rialToFaCompact(),
                color = AppLabel,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val first = points.first()
            val last = points.last()
            Text(
                "${first.day.toFa()} ${faMonthName(first.month)}",
                color = AppLabel, fontSize = 8.5.sp, fontWeight = FontWeight.Bold,
            )
            Text(
                "${last.day.toFa()} ${faMonthName(last.month)}",
                color = AppLabel, fontSize = 8.5.sp, fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TradeRow(trade: AssetTradeEntity, assetName: String) {
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(1.5.dp, AppLine, shape)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (trade.isBuy) AppPrimaryPill else AppDangerPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (trade.isBuy) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = if (trade.isBuy) AppPrimaryInk else AppDangerInk,
                modifier = Modifier.size(13.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${if (trade.isBuy) "خرید" else "فروش"} ${formatQuantity(trade.quantity)} $assetName",
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                toFaDate(trade.year, trade.month, trade.day),
                color = AppLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        // ردیفِ فهرست بی‌واحده - قاعده‌ی عددِ TOKENS.md.
        Text(
            trade.totalRial.rialToFaCompact(),
            color = AppText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
    }
}
