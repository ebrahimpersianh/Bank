package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.data.netDangShares
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import androidx.compose.foundation.layout.defaultMinSize
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppPurpleInk
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.components.AppHeroCard

internal val PrimaryShadow: Color
    @Composable get() = AppPrimaryDim
internal val GoldHintBg: Color
    @Composable get() = AppWarningPill
internal val GoldHintBorder: Color
    @Composable get() = AppDueNextBorder
internal val GoldHintIcon: Color
    @Composable get() = AppWarningInk
internal val GoldHintInk: Color
    @Composable get() = AppGoldInkSoft
// ═══ ۲ · هیرویِ بنفش ════════════════════════════════════════════════════════════
@Composable
internal fun PeriodSpendHero(
    label: String,
    spend: Double,
    deltaPercent: Int?,
    bars: List<Double>,
    firstLabel: String,
    lastLabel: String,
    privacyMode: Boolean,
    /** کدام میله «حالا»ست. در نمای ماه روزِ جاری، در فصل/سال آخرین ماه. */
    currentBarIndex: Int = bars.lastIndex,
    /** برچسبِ هر میله برای حبابِ لمس. خالی یعنی نمودار لمس‌پذیر نیست. */
    barLabels: List<String> = emptyList(),
) {
    // خواسته‌ی کاربر (۳ مهر): مثلِ بقیه‌ی صفحه‌ها با تم عوض شود و همان موجِ نرم را داشته
    // باشد - پس همان `AppHeroCard` است، نه گرادیانِ بنفشِ ثابتِ قبلی.
    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "خرجِ $label",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        // fmt() جداکننده‌ی لاتین می‌داد و عدد ریال بود: خرجِ ۱۰۲ میلیون
                        // تومان «1,026,600,000» چاپ می‌شد.
                        maskIfPrivate(masked, spend.rialToFaCompact()),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                // واحد یک‌بار زیرِ عدد می‌آید، طبقِ قاعده‌ی عددِ سیستمِ طراحی.
                Text(
                    "${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (deltaPercent != null) {
                Text(
                    // ⚠️ برچسبِ قبلی «قدرتِ خرید» بود و غلط: این عدد نسبتِ خرجِ این دوره به
                    // دوره‌ی قبل است، نه تورم و نه قدرتِ خرید. کاربری که خرجش ۲۰٪ بیشتر
                    // شده «۲۰٪ قدرتِ خرید» می‌دید و معنایش را برعکس می‌فهمید.
                    (if (deltaPercent > 0) "▲ " else "▼ ") +
                        "${(kotlin.math.abs(deltaPercent)).toFa()}٪ از دوره‌ی قبل",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        // خرجِ بیشتر پُررنگ‌تر. قرمز روی هیرویِ بنفش نمی‌نشیند، پس شدتِ
                        // همان سفید جهت را می‌رساند - کنارِ مثلثِ ▲/▼ که مستقل از رنگ است.
                        .background(Color.White.copy(alpha = if (deltaPercent > 0) 0.28f else 0.16f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        if (bars.any { it > 0.0 }) {
            HeroChart(
                values = bars,
                labels = barLabels,
                valueLabel = { value -> if (privacyMode) "•••" else "${value.rialToFaCompact()} ${ir.sadteam.loancalc.ui.jibak.unitFa()}" },
                currentIndex = currentBarIndex,
                natural = HeroChartStyle.BARS,
                modifier = Modifier.padding(top = 12.dp),
            )
            // هم‌جهتِ میله‌ها (فیزیکی چپ‌به‌راست): اولِ دوره چپ، امروز راست.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(firstLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Text(lastLabel, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            Text(
                "هنوز خرجی ثبت نکردی - با اولین تراکنش، روندِ ماه‌ها همین‌جا ساخته می‌شه",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
private val PurpleDeep = Color(0xFF7440C9)
private val PurpleShadow = Color(0xFF5C2FA8)
// ═══ ۳ · ثابت و متغیر ══════════════════════════════════════════════════════════
/** نوارِ ۲۴ پیکسلی با گوشه‌ی ۹: بخشِ «ثابت» راه‌راهِ قرمز، بخشِ «آزاد» سبزِ کم‌رنگ. */
/**
 * سه کارتِ کوچکِ «درآمد / هزینه / تعدادِ تراکنش»، هرکدام با تغییرِ نسبت به دوره‌ی قبل.
 *
 * ⚠️ رنگِ فلش **معناییِ پول** است نه «خوب/بد»: بالا رفتنِ درآمد سبز و بالا رفتنِ هزینه
 * قرمز است. همان دو توکنِ سراسری، پس با تمِ خریدنی نمی‌چرخند.
 */
@Composable
internal fun PeriodStatRow(
    income: Double,
    incomeChangePercent: Int?,
    spend: Double,
    spendChangePercent: Int?,
    count: Int,
    countDelta: Int,
    privacyMode: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodStatCard(
            icon = Icons.Filled.ArrowDownward,
            iconTint = AppPrimaryInk,
            iconBg = AppPrimaryPill,
            title = "درآمد",
            value = maskIfPrivate(privacyMode, income.rialToFaCompact()),
            unit = "${ir.sadteam.loancalc.ui.jibak.unitFa()}",
            footer = incomeChangePercent?.let { "${kotlin.math.abs(it).toFa()}٪ از دوره‌ی قبل" } ?: "دوره‌ی قبل خالی",
            footerTint = if ((incomeChangePercent ?: 0) >= 0) AppPrimaryInk else AppDangerInk,
            modifier = Modifier.weight(1f),
        )
        PeriodStatCard(
            icon = Icons.Filled.ArrowUpward,
            iconTint = AppDangerInk,
            iconBg = AppDangerPill,
            title = "هزینه",
            value = maskIfPrivate(privacyMode, spend.rialToFaCompact()),
            unit = "${ir.sadteam.loancalc.ui.jibak.unitFa()}",
            footer = spendChangePercent?.let { "${kotlin.math.abs(it).toFa()}٪ از دوره‌ی قبل" } ?: "دوره‌ی قبل خالی",
            footerTint = if ((spendChangePercent ?: 0) > 0) AppDangerInk else AppPrimaryInk,
            modifier = Modifier.weight(1f),
        )
        PeriodStatCard(
            icon = Icons.Filled.SwapHoriz,
            iconTint = AppPurple,
            iconBg = AppIconFrame,
            title = "تعدادِ تراکنش",
            value = count.toFa(),
            unit = "عدد",
            footer = when {
                countDelta > 0 -> "+${countDelta.toFa()} از دوره‌ی قبل"
                countDelta < 0 -> "−${(-countDelta).toFa()} از دوره‌ی قبل"
                else -> "بی‌تغییر"
            },
            footerTint = AppMuted,
            modifier = Modifier.weight(1f),
        )
    }
}
@Composable
private fun PeriodStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    unit: String,
    footer: String,
    footerTint: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(contentPadding = 10.dp, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = AppText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(999.dp)).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(12.dp))
            }
        }
        Text(
            value,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(unit, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        Text(
            footer,
            color = footerTint,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}
@Composable
internal fun FixedVsFreeCard(
    fixedShare: Int,
    fixedAmount: Double,
    freeAmount: Double,
    privacyMode: Boolean,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("ثابت و متغیر", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("${(fixedShare).toFa()}٪", color = AppDangerInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(9.dp)),
        ) {
            Box(
                modifier = Modifier
                    .weight(fixedShare.coerceIn(1, 99).toFloat())
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppDanger, StripeDark, AppDanger),
                            start = Offset(0f, 0f),
                            end = Offset(24f, 24f),
                            tileMode = androidx.compose.ui.graphics.TileMode.Repeated,
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("ثابت", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .weight((100 - fixedShare).coerceIn(1, 99).toFloat())
                    .fillMaxHeight()
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Text("آزاد", color = AppPrimaryDim, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "اجاره، اقساط و قبض ${maskIfPrivate(masked, (fixedAmount).rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()} از " +
                    "درآمدت را برده — ${maskIfPrivate(masked, (freeAmount).rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()} " +
                    "برای خرجِ آزاد مانده.",
                color = AppMuted,
                fontSize = 10.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
private val StripeDark = Color(0xFFE23F3F)
// ═══ ۴ · دونات ════════════════════════════════════════════════════════════════
@Composable
internal fun CategoryDonutCard(
    byCategory: Map<String, Double>,
    total: Double,
    periodLabel: String,
    privacyMode: Boolean,
    monthlyInstallmentRial: Double,
    chequesThisMonth: Int,
    onOpenLoanStats: () -> Unit,
    onOpenChequeReport: () -> Unit,
) {
    // زیرِ ۱٪ پنهان (۱۴ مهر، مثلِ خانه) - «قبض ۰٪» فقط شلوغی بود.
    val top = remember(byCategory, total) {
        byCategory.entries.sortedByDescending { it.value }
            .filter { total <= 0.0 || it.value / total >= 0.01 }.take(3)
    }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    // مبلغِ کل همین بالا در کارتِ سبز هست؛ وسطِ دایره تعدادِ دسته‌ها می‌آید.
    // مثلِ خانه: سهمِ بزرگ‌ترین دسته و نامش («۹۹٪ قسط/چک»)، نه تعدادِ دسته‌ها.
    val biggest = top.firstOrNull()
    val centerNumber = if (biggest == null || total <= 0.0) "—" else "${((biggest.value / total * 100).toInt()).toFa()}٪"
    val centerUnit = biggest?.key ?: ""
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(14.dp),
    ) {
    // برچسبِ دوره از داخلِ دایره بیرون آمد. «این ماه»ِ ثابت هم غلط بود: با تاگلِ فصل/سال
    // عوض نمی‌شد، پس روی دوره‌ی سالانه هم «این ماه» می‌نوشت.
    Text("سهمِ دسته‌ها از خرجِ $periodLabel", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    // ۱۶ مهر (طرحِ مرتب‌تر، مثلِ خانه): حلقه + فهرستِ هم‌ترازِ دسته‌ها با مبلغ و درصد.
    // جمعِ کل همین بالا در کارتِ سبز هست، پس این‌جا تکرارش نمی‌کنیم.
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        CategoryDonut(
            slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
            size = 80.dp,
            strokeWidth = 9.dp,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    centerNumber,
                    color = AppText,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    centerUnit,
                    color = AppLabel,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 56.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            top.forEachIndexed { i, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors[i % colors.size]),
                    )
                    Text(
                        entry.key,
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, entry.value.rialToFaCompact()),
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                        )
                    }
                    Text(
                        // total صفر → NaN٪. کارت با جمعِ صفر نمی‌آید، ولی نگهبانش یک خط است.
                        if (total <= 0.0) "—" else "${((entry.value / total * 100).toInt()).toFa()}٪",
                        color = AppMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(40.dp),
                    )
                }
            }
        }
    }
    }
}

// ═══ ۵ و ۶ · کارت‌های کشف ═══════════════════════════════════════════════════════
/**
 * دو ردیفِ ورودی به گزارشِ تعهدی — بخشِ ۸۱، فریمِ `81a`.
 *
 * 🚨 **چرا ردیف و نه کارت** (تشخیصِ مرکزیِ طراح): کاری که این تکه می‌کند **دو تپ** است، و
 * هر چیزی که فقط در را باز می‌کند در این برنامه ردیف است. نسخه‌ی قبلی یک کارتِ مادر با
 * سرصفحه داشت و دو کارتِ فرزند داخلش — سه لایه قاب برای دو تپ، و «کارتِ داخلِ کارت» با
 * تبِ تختِ گزارش هم‌خانواده نمی‌شد. **دو کارتِ هم‌عرض هم امتحان و رد شد**: عرضِ ~۱۵۰dp جای
 * `۴۶٬۸۳۸٬۶۳۶` را ندارد و عدد خلاصه می‌شد.
 *
 * 🚨 **سرصفحه حذف شد و عددِ سرصفحه هم ساخته نشد**: جمعِ قسط و چک همان ۵۷٪ِ دوناتِ بالای
 * همین صفحه است، و یک عدد دو بار در یک صفحه کاربر را دنبالِ تفاوتشان می‌فرستد. جایش خطِ
 * گروه با نقطه‌ی هم‌رنگِ تکه‌ی دونات است — کارِ سرصفحه‌ی ۳۴پیکسلی را ۹ پیکسل انجام می‌دهد.
 *
 * ⚠️ **هیچ عددِ تازه‌ای ساخته نشد**: مبلغ همان صورتِ کسرِ قرصِ «۱۰۹٪ از درآمدت» در تبِ وام
 * است، و شمارش همان چکِ در انتظارِ تبِ چک. دو ردیف دو **جنسِ** عدد دارند (مبلغ و شمارش) و
 * همین تفاوت دو مقصد را بی یک کلمه توضیح از هم جدا می‌کند.
 */
@Composable
internal fun CommitmentRows(
    monthlyInstallmentRial: Double,
    chequesThisMonth: Int,
    privacyMode: Boolean,
    onOpenLoanStats: () -> Unit,
    onOpenChequeReport: () -> Unit,
) {
    // ۱۴ مهر: از داخلِ کارتِ دونات بیرون آمد - دیگر کارت توی کارت نیست.
    run {
        AppCard(label = "تعهدهای این ماه") {
            CommitmentRow(
                icon = Icons.Filled.EventRepeat,
                title = "اقساط وام",
                value = maskIfPrivate(privacyMode, rialToToman(monthlyInstallmentRial.toLong()).toFaMoney()) + " ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                // ۱۶ مهر: «این ماه» معلوم نبود جمعِ سررسید است، نه پرداخت‌شده؛ و با «خرجِ» بالا اشتباه می‌شد.
                caption = "سررسیدِ این ماه",
                onClick = onOpenLoanStats,
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppLineRow))
            CommitmentRow(
                icon = Icons.Filled.CheckCircle,
                title = "چک‌ها",
                // شمارش است نه مبلغ، پس حالتِ خصوصی پوشانده‌اش نمی‌کند.
                value = "${chequesThisMonth.toFa()} چک",
                caption = "تا آخرِ ماه",
                onClick = onOpenChequeReport,
            )
        }
    }
}
/** یک ردیفِ تعهد. بنفش فقط سه نقطه می‌آید: زمینه‌ی آیکون، خودِ عدد، و نقطه‌ی خطِ گروه. */
@Composable
private fun CommitmentRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    caption: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pressScaleClickable(onClick = onClick)
            .defaultMinSize(minHeight = AppSpacing.minTouchTarget)
            .padding(vertical = 9.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPurplePill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppPurple, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
            Text(title, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(caption, color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 1.dp))
        }
        Text(value, color = AppPurpleInk, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.padding(start = 4.dp).size(15.dp),
        )
    }
}
/** خرجِ هر دسته در ماهِ جاری در برابرِ ماهِ قبل - پنج دسته با بیشترین تغییر. */
@Composable
internal fun MonthCompareCard(all: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>, privacyMode: Boolean) {
    val today = remember { ir.sadteam.loancalc.core.JalaliCalendar.today() }
    val (py, pm) = if (today.m == 1) (today.y - 1) to 12 else today.y to (today.m - 1)
    fun spend(y: Int, m: Int) = all.netDangShares().filter {
        it.confirmed && it.year == y && it.month == m && it.type == "WITHDRAWAL" &&
            ir.sadteam.loancalc.data.countsInReports(it)
    }.groupBy { it.category ?: "بی‌دسته" }.mapValues { e -> e.value.sumOf { it.amount } }
    val now = spend(today.y, today.m)
    val prev = spend(py, pm)
    if (now.isEmpty() && prev.isEmpty()) return
    val rows = (now.keys + prev.keys).map { k -> Triple(k, now[k] ?: 0.0, prev[k] ?: 0.0) }
        .sortedByDescending { kotlin.math.abs(it.second - it.third) }.take(5)
    val totalNow = now.values.sum()
    val totalPrev = prev.values.sum()
    AppCard(contentPadding = 14.dp) {
        Text("مقایسه با ماهِ قبل", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
        PrivacyCrossfade(privacyMode) { masked ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppBg)
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("این ماه", color = AppMuted, fontSize = 10.sp)
                    Text("${maskIfPrivate(masked, totalNow.rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ماهِ قبل", color = AppMuted, fontSize = 10.sp)
                    Text("${maskIfPrivate(masked, totalPrev.rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        val scaleMax = rows.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(1.0) ?: 1.0
        rows.forEach { (cat, a, b) ->
            val diff = a - b
            val up = diff > 0
            val ink = if (diff == 0.0) AppMuted else if (up) AppDangerInk else AppPrimaryInk
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(cat, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, kotlin.math.abs(diff).rialToFaCompact()) +
                                (if (up) "  ↑ بیشتر" else if (diff < 0) "  ↓ کمتر" else ""),
                            color = ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                // ۱۶ مهر: دو نوارِ جدا (رنگی = این ماه، خاکستری = ماهِ قبل) با **یک مقیاسِ مشترک** برای
                // همه‌ی دسته‌ها - قبلاً هر دسته نسبت به خودش پر می‌شد و همه‌ی نوارهای قرمز پر بودند.
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(7.dp).clip(RoundedCornerShape(99.dp)).background(AppLine),
                ) {
                    if (a > 0.0) Box(
                        Modifier.fillMaxWidth((a / scaleMax).toFloat().coerceIn(0.03f, 1f)).height(7.dp)
                            .clip(RoundedCornerShape(99.dp)).background(ink.copy(alpha = 0.85f)),
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(5.dp).clip(RoundedCornerShape(99.dp)).background(AppLine),
                ) {
                    if (b > 0.0) Box(
                        Modifier.fillMaxWidth((b / scaleMax).toFloat().coerceIn(0.03f, 1f)).height(5.dp)
                            .clip(RoundedCornerShape(99.dp)).background(AppMuted.copy(alpha = 0.45f)),
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp),
        ) {
            listOf(
                AppPrimaryInk to "این ماه (کمتر شده)",
                AppDangerInk to "این ماه (بیشتر شده)",
                AppMuted.copy(alpha = 0.45f) to "ماهِ قبل",
            ).forEach { (c, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(width = 12.dp, height = 5.dp).clip(RoundedCornerShape(99.dp)).background(c))
                    Text(label, color = AppLabel, fontSize = 9.5.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
/** جمعِ خرجِ هر برچسب (کلِ زمان) + خرج‌های بازپرداختی. */
@Composable
internal fun TagsAndReimbursableCard(all: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>, privacyMode: Boolean) {
    val spends = all.filter { it.confirmed && it.type == "WITHDRAWAL" }
    val byTag = spends.flatMap { tx -> tx.tags?.split(',')?.filter { it.isNotBlank() }?.map { it to tx.amount } ?: emptyList() }
        .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
        .entries.sortedByDescending { it.value }.take(6)
    val reimb = spends.filter { it.reimbursable }.sumOf { it.amount }
    if (byTag.isEmpty() && reimb == 0.0) return
    AppCard(contentPadding = 14.dp) {
        if (byTag.isNotEmpty()) {
            Text("برچسب‌ها", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
            byTag.forEach { (tag, sum) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("#$tag", color = AppText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(maskIfPrivate(masked, sum.rialToFaCompact()) + " ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppMuted, fontSize = 12.sp)
                    }
                }
            }
        }
        if (reimb > 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("↩ خرج‌های بازپرداختی (قرار است پس بگیری)", color = AppText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, reimb.rialToFaCompact()) + " ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
