package ir.sadteam.loancalc.ui.asset

import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.foundation.layout.height
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroExpense
import ir.sadteam.loancalc.ui.components.HeroIncome
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText

// ═══ ۲ · هیرویِ داراییِ کل ══════════════════════════════════════════════════════
/** گرادیان/سایه/گوشه/هاله همه داخلِ `AppHeroCard`ن - همون‌جا با توکنِ تیره درست می‌چرخن. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TotalWealthHero(
    total: Double,
    cash: Double,
    gold: Double,
    fiat: Double,
    crypto: Double,
    other: Double,
    trend: List<Double>,
    trendIsReal: Boolean,
    trendPercent: Int?,
    privacyMode: Boolean,
    onOpen: (() -> Unit)? = null,
    onPill: (String) -> Unit,
) {
    AppHeroCard(modifier = if (onOpen != null) Modifier.pressScaleClickable(onClick = onOpen) else Modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Text(
                    "ارزشِ کلِ دارایی‌ها",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 9.dp),
                )
                if (trendPercent != null && trendPercent != 0) {
                    val up = trendPercent > 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (up) HeroIncome else HeroExpense,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            "${kotlin.math.abs(trendPercent).toFa()}٪ نسبت به ماهِ قبل",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
            // مبلغ و «تومان» در یک خط (خواسته‌ی کاربر: کارت هم‌قدِ کارتِ خانه، نه کشیده).
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                val shownTotal = ir.sadteam.loancalc.ui.components.countUpDouble(total)
                PrivacyCrossfade(privacyMode) { masked ->
                    AutoShrinkText(
                        maskIfPrivate(masked, shownTotal.rialToFaCompact()),
                        color = Color.White,
                        maxFontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                // واحد تو کارتِ خلاصه میاد - قاعده‌ی عددِ TOKENS.md، مثلِ AccountsTotalHero.
                Text(
                    "تومان",
                    color = HeroMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                )
            }
            // نمودارِ روندِ نقدی - فقط وقتی داده‌ی واقعی هست. در حالتِ خصوصی هم می‌مانَد:
            // شکلِ روند مبلغ لو نمی‌دهد، و همان چیزی است که کارت برایش ساخته شده.
            // ۱۶ مهر: روزهای بی‌داده‌ی اولِ بازه‌ی ۳۰روزه با همان مقدارِ اولین روز پر می‌شوند تا
            // نمودار از اولِ کارت شروع شود (خطِ صاف = آن روزها تغییری نبوده)، نه یکهو از وسط.
            val trend = if (trend.isNotEmpty() && trend.size < 30) List(30 - trend.size) { trend.first() } + trend else trend
            if (trend.size >= 2 && trend.any { it != trend.first() }) {
                HeroChart(
                    values = trend,
                    // دو روز داده = دو نقطه در جای واقعی‌شان روی محورِ ۳۰روزه (خواسته‌ی کاربر).
                    slots = 30,
                    natural = HeroChartStyle.LINE,
                    currentIndex = trend.lastIndex,
                    height = 38.dp,
                    modifier = Modifier.padding(top = 6.dp),
                    labels = trend.indices.map { index ->
                        val ago = trend.lastIndex - index
                        if (ago == 0) "امروز" else "${ago.toFa()} روز پیش"
                    },
                    valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
                )
                // برچسبِ دو سرِ محور، مثلِ کارتِ خانه - جای قرصِ بزرگِ قبلی که کارت را بلند می‌کرد.
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("امروز", color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (onOpen != null) Text("نمودارِ کامل ‹", color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        // صادقانه: تا وقتی عکسِ روزانه جمع نشده، نمودار فقط نقد را می‌گوید.
                        if (trendIsReal) "۳۰ روزِ گذشته" else "نقدِ ۳۰ روزِ گذشته",
                        color = HeroMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            // پنج قرص تو یه ردیفِ عادی جا نمی‌شن؛ FlowRow خطِ دوم می‌سازه.
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (cash > 0) HeroPill("نقد", cash, privacyMode) { onPill(PILL_CASH) }
                if (gold > 0) HeroPill("طلا", gold, privacyMode) { onPill(ASSET_CATEGORY_GOLD) }
                if (fiat > 0) HeroPill("ارز", fiat, privacyMode) { onPill(ASSET_CATEGORY_FIAT) }
                if (crypto > 0) HeroPill("رمز ارز", crypto, privacyMode) { onPill(ASSET_CATEGORY_CRYPTO) }
                if (other > 0) HeroPill("سایر", other, privacyMode) { onPill(ASSET_CATEGORY_CUSTOM) }
            }
        }
    }
}
/**
 * قرصِ تفکیک. «نقد» می‌بره به فرمی که رقمش اونجا عوض می‌شه؛ بقیه به سرگروهِ خودشون اسکرول می‌کنن.
 * `onClick`ِ null یعنی بازخوردِ لمس هم نداره - قرصِ بی‌مقصد نباید کلیک‌پذیر به‌نظر بیاد.
 */
@Composable
private fun HeroPill(
    label: String,
    value: Double,
    privacyMode: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(Color.White.copy(alpha = 0.2f))
            .then(if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "$label ${maskIfPrivate(masked, value.rialToFaCompact())}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
    }
}
/** «خالص پس از بدهی‌ها» - دارایی منهای اقساطِ باقی‌مانده و بدهی به دیگران. */
@Composable
private fun NetWorthCard(total: Double, liabilities: Double, loans: Double, owe: Double, privacyMode: Boolean) {
    val net = total - liabilities
    AppCard(contentPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("خالص پس از بدهی‌ها", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        buildString {
                            if (loans > 0) append("اقساطِ مانده ").append(maskIfPrivate(masked, loans.rialToFaCompact()))
                            if (loans > 0 && owe > 0) append(" · ")
                            if (owe > 0) append("بدهی ").append(maskIfPrivate(masked, owe.rialToFaCompact()))
                        },
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, (if (net < 0) "−" else "") + kotlin.math.abs(net).rialToFaCompact()) + " تومان",
                    color = if (net < 0) AppDanger else AppPrimaryInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
