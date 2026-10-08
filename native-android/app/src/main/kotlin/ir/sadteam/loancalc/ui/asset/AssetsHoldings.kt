package ir.sadteam.loancalc.ui.asset

import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.foundation.layout.height
import ir.sadteam.loancalc.ui.components.AppCard
import androidx.compose.foundation.layout.heightIn
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToFaSignedCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

// ═══ ۳ · حساب‌های بانکی ═════════════════════════════════════════════════════════
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 4.dp),
    )
}
/**
 * ردیفِ حساب - کلیک‌پذیر.
 *
 * ⚠️ `bankName` **رشته‌ی خالی** است نه `null` (فرم برای منبعِ غیربانکی `""`
 * می‌نویسد)، پس `?:` هیچ‌وقت آتش نمی‌کرد و حسابِ نقدی بجِ خالی می‌گرفت. حالا رو
 * `type` تصمیم می‌گیریم و `iconKey`ی که کاربر انتخاب کرده استفاده می‌شه.
 */
@Composable
internal fun AccountRow(
    account: AccountEntity,
    balance: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape, clip = false)
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        if (account.type == ACCOUNT_TYPE_BANK) {
            BankBadge(bankName = account.bankName, size = 40.dp)
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(AppIconFrame),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    accountIconForKey(account.iconKey),
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                account.name,
                color = AppText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = account.smsSender?.takeIf { it.isNotBlank() }?.let { "پیامک $it" }
                ?: account.bankName.ifBlank { null }
                ?: "منبعِ نقدی"
            Text(
                meta,
                color = AppMuted,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("موجودی", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            PrivacyCrossfade(privacyMode) { masked ->
                AutoShrinkText(
                    maskIfPrivate(masked, balance.rialToFaCompact()) + " ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                    // موجودیِ منفیِ کارتِ اعتباری وضعِ عادیه نه خطا: فقط عدد قرمز می‌شه.
                    color = if (balance < 0) AppDangerInk else AppText,
                    maxFontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // چهار رقمِ آخرِ کارت، اگر ثبت شده - بقیه ستاره (شماره‌ی کامل هیچ‌جا نشان داده نمی‌شود).
            val digits = account.cardNumber?.filter { it.isDigit() }.orEmpty()
            if (digits.length >= 8) {
                Text(
                    "${digits.takeLast(4).faNum()} **** **** ${digits.take(4).faNum()}",
                    color = AppMuted,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(AppIconFrame),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
/** سرِ بخش (طرحِ ChatGPT): کاشیِ آیکون + عنوان + شمارنده، و قرصِ کنش در سمتِ چپ. */
@Composable
internal fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    count: Int? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            title,
            color = AppText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp),
        )
        if (count != null) {
            Text(
                count.toFa(),
                color = AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryPill)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppIconFrame)
                    .pressScaleClickable(onClick = onAction)
                    .padding(start = 11.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            ) {
                Text(actionLabel, color = AppText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.padding(start = 4.dp).size(16.dp),
                )
            }
        }
    }
}
// ═══ ۴ · دسته‌های دارایی ═══════════════════════════════════════════════════════
/** سرگروه: عنوان + جمعِ همون دسته + سودِ همون دسته (طلا و ارز دیگه با هم جمع نمی‌شن). */
@Composable
private fun GroupHeader(title: String, total: Double?, profit: Double?, privacyMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, total?.rialToFaCompact() ?: "—"),
                color = if (total == null) AppMuted else AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
        }
        if (profit != null) {
            Text(
                profit.rialToFaSignedCompact(),
                color = if (profit < 0) AppDangerInk else AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
/** طلا داخلِ کاغذِ طلایی؛ بقیه ردیفِ ساده روی صفحه. `groupPalette` تصمیم می‌گیره. */
@Composable
private fun AssetGroup(
    category: String,
    rows: List<AssetHolding>,
    privacyMode: Boolean,
    onOpen: (AssetEntity) -> Unit,
) {
    val p = groupPalette(category)
    val body: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(if (p.paper != null) 9.dp else 8.dp)) {
            rows.forEach { HoldingRow(it, p, privacyMode, onOpen) }
        }
    }
    if (p.paper == null) {
        body()
    } else {
        val shape = RoundedCornerShape(AppRadius.card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(p.paper)
                .border(1.5.dp, p.rowBorder, shape)
                .padding(12.dp),
        ) { body() }
    }
}
/**
 * ردیفِ موجودی. سه ستون: نشان · اسم و **قیمتِ روزِ واحد** · ارزشِ کل و سود.
 *
 * ⚠️ قیمتِ واحد ماسکِ خصوصی نمی‌گیره - قیمتِ اونسِ طلا داراییِ کاربر نیست، نرخِ
 * بازاره. ارزشِ کل و سود ماسک می‌گیرن، مثلِ قبل.
 */
@Composable
private fun HoldingRow(
    holding: AssetHolding,
    p: GroupPalette,
    privacyMode: Boolean,
    onOpen: (AssetEntity) -> Unit,
) {
    // ارزشِ ردیف از قبل با همان جانشین حساب شده، پس قیمتِ واحد را از آن برمی‌گردانیم
    // و به یک منبعِ دوم نیاز نیست.
    val unitPrice = holding.value?.takeIf { holding.quantity > 0.0 }?.div(holding.quantity)
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(p.rowSurface)
            .border(if (p.paper != null) 1.dp else 2.dp, p.rowBorder, shape)
            .pressScaleClickable { onOpen(holding.asset) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        AssetBadge(holding.asset.symbol, holding.asset.category, 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${formatQuantity(holding.quantity)} ${holding.asset.name}",
                color = p.ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                // قیمتِ روز از همان جانشینِ ViewModel می‌آید، پس ردیفِ تازه هم عدد دارد.
                unitPrice?.let { "قیمتِ روز ${it.rialToFaCompact()} ${ir.sadteam.loancalc.ui.jibak.unitFa()}" } ?: "قیمتِ روز —",
                color = p.subInk,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.Start) {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, holding.value?.rialToFaCompact() ?: "—"),
                    color = p.ink,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            holding.profit?.let { profit ->
                Text(
                    profit.rialToFaSignedCompact(),
                    color = if (profit >= 0) AppPrimaryInk else AppDangerInk,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
/** رقم‌های لاتین → فارسی، بی جداکننده‌ی هزارگان (برای شماره‌ی کارت). */
private fun String.faNum(): String = map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

// ═══ بازطراحیِ صفحه‌ی دارایی (بسته‌ی ChatGPT، ۳ مهر) ═════════════════════════════
// ترتیب: هیرو → جست‌وجو → قرصِ دسته → نمای کلیِ بازار → دارایی‌های من → حساب‌ها → بازار.
// همه‌ی عددها از داده‌ی واقعی (قیمتِ روز، تغییرِ ۳۰روزه، تاریخچه‌ی سرور) - هیچ عددِ طرح.
// شکلِ نمودارهای کوچک از همان سبکِ نمودارِ خریده‌شده در فروشگاه می‌آید (`HeroChart`).
/**
 * درصدِ تغییرِ **امروز** نسبت به آخرین قیمتِ ثبت‌شده‌ی روزهای قبل (خواسته‌ی کاربر، ۳ مهر).
 * از تاریخچه‌ی روزانه‌ی سرور + قیمتِ همین لحظه ساخته می‌شود؛ بی تاریخچه = `null` («—»).
 */
@Composable
internal fun rememberDailyChange(symbol: String, viewModel: AssetViewModel): Double? {
    val history by remember(symbol) { viewModel.historyOf(symbol) }.collectAsState(initial = emptyList())
    val prices by viewModel.marketPrices.collectAsState()
    val today = remember { JalaliCalendar.today() }
    val prev = history.lastOrNull { !(it.year == today.y && it.month == today.m && it.day == today.d) }?.priceRial
    val cur = prices[symbol] ?: history.lastOrNull()?.priceRial
    return if (prev != null && cur != null && prev > 0.0) (cur - prev) / prev * 100.0 else null
}
/** نمودارِ کوچکِ روندِ یک نماد - هرچقدر تاریخچه هست؛ کمتر از ۲ نقطه = خطِ صاف. */
@Composable
internal fun MiniTrend(symbol: String, change: Double?, viewModel: AssetViewModel, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 26.dp) {
    val history by remember(symbol) { viewModel.historyOf(symbol) }.collectAsState(initial = emptyList())
    val ink = if ((change ?: 0.0) < 0.0) AppDangerInk else AppPrimaryInk
    HeroChart(
        values = history.map { it.priceRial },
        labels = emptyList(),
        valueLabel = { "" },
        currentIndex = (history.size - 1).coerceAtLeast(0),
        natural = HeroChartStyle.LINE,
        modifier = modifier,
        height = height,
        ink = ink,
    )
}
/** «دارایی‌های من» - ردیفِ فشرده: نشان | نام و نماد | نمودار | ارزش و تغییر | فلش. */
@Composable
internal fun MyAssetsSection(
    rows: List<AssetHolding>,
    changes: Map<String, Double>,
    privacyMode: Boolean,
    viewModel: AssetViewModel,
    onSeeAll: () -> Unit,
    onOpen: (AssetEntity) -> Unit,
) {
    AppCard(contentPadding = 12.dp) {
        MarketSectionTitle("دارایی‌های من", onSeeAll)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            rows.forEach { h ->
                val change = rememberDailyChange(h.asset.symbol, viewModel)
                val shape = RoundedCornerShape(AppRadius.card)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .clip(shape)
                        .border(1.dp, AppLine, shape)
                        .pressScaleClickable { onOpen(h.asset) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    AssetBadge(h.asset.symbol, h.asset.category, 40.dp)
                    Column(modifier = Modifier.weight(1.2f).padding(start = 10.dp)) {
                        Text(
                            "${formatQuantity(h.quantity)} ${h.asset.name}",
                            color = AppText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Ltr { Text(h.asset.symbol.removePrefix("CUSTOM_"), color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                    MiniTrend(h.asset.symbol, change, viewModel, modifier = Modifier.weight(1f).padding(horizontal = 6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                maskIfPrivate(masked, h.value?.rialToFaCompact() ?: "—"),
                                color = AppText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Text("${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        change?.let { PriceChangeBadge(it, modifier = Modifier.padding(top = 2.dp)) }
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.padding(start = 4.dp).size(18.dp))
                }
            }
        }
    }
}
