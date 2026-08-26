package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.asset.compact
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentBorder
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * تبِ **گزارش** - بازسازیِ کاملِ فریمِ `26a`.
 *
 * ```
 * ۱ عنوان + تاگلِ ماه/فصل/سال + کلیدِ خصوصی
 * ۲ هیرویِ بنفش: خرجِ دوره + نمودارِ ۷ ماهه
 * ۳ ثابت و متغیر - نوارِ راه‌راهِ قرمز/سبز
 * ۴ دوناتِ دسته‌ها
 * ۵ کارتِ کشف: اشتراک‌های تکراری
 * ۶ کارتِ کشف: دسته‌ی پرخرج نسبت به میانگینِ سه ماه
 * ۷ خروجیِ اکسل و PDF
 * ```
 *
 * ⚠️ **کارتِ کشفِ دوم** طبقِ `design/ANSWERS-section-37.md` بندِ ۵ بازنویسی شد. متنِ اولیه
 * («نان ۲۲٪ گران‌تر شد») با داده‌ی ما درنمی‌اومد چون اپ اسمِ قلم/فروشنده رو ذخیره نمی‌کنه.
 * قاعده‌ی جایگزینِ خودِ طراح: **دسته‌ای که جمعِ ماهِ جاری‌اش دستِ‌کم ۱۵٪ از میانگینِ سه ماهِ
 * کاملِ گذشته بیشتر باشه، با شرطِ حداقل پنج تراکنش در هر پنجره. بزرگ‌ترین انحراف، یکی در ماه.**
 */
@Composable
fun ReportTabScreen(
    onOpenExport: () -> Unit = {},
    accountViewModel: AccountViewModel = hiltViewModel(),
    privacyViewModel: PrivacyModeViewModel = hiltViewModel(),
) {
    val transactions by accountViewModel.transactions.collectAsState()
    val recurring by accountViewModel.recurringPayments.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }

    val stats = remember(transactions, recurring, period) {
        buildReportStats(transactions, recurring, today, period)
    }
    var showNewTransaction by remember { mutableStateOf(false) }
    if (showNewTransaction) {
        NewTransactionSheet(onDismiss = { showNewTransaction = false })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ReportHeader(
                period = period,
                onPeriod = { period = it },
                privacyMode = privacyMode,
                onTogglePrivacy = { privacyViewModel.toggle() },
                showControls = transactions.isNotEmpty(),
            )
        }
        // هیچ تراکنشی ثبت نشده → **فریمِ `21c`**: کارتِ خط‌چینِ نمودارِ خالی، یادآوریِ پیامکِ
        // بانکی، و لیستِ «وقتی داده داشته باشی اینها را می‌بینی». هیرویِ بنفشِ صفر نشون داده نمی‌شه.
        if (transactions.isEmpty()) {
            item { NoChartCard(onAddTransaction = { showNewTransaction = true }) }
            item { BankSmsHintCard() }
            item { ComingSoonCard() }
            return@LazyColumn
        }
        item {
            PeriodSpendHero(
                label = stats.periodLabel,
                spend = stats.periodSpend,
                deltaPercent = stats.deltaPercent,
                bars = stats.monthlyBars,
                firstLabel = stats.firstBarLabel,
                lastLabel = stats.lastBarLabel,
                privacyMode = privacyMode,
            )
        }
        if (stats.fixedShare != null) {
            item {
                FixedVsFreeCard(
                    fixedShare = stats.fixedShare,
                    fixedAmount = stats.fixedAmount,
                    freeAmount = stats.freeAmount,
                    privacyMode = privacyMode,
                )
            }
        }
        if (stats.byCategory.isNotEmpty()) {
            item { CategoryDonutCard(stats.byCategory, stats.periodSpend, privacyMode) }
        }
        if (stats.recurringCount > 0) {
            item {
                DiscoveryCard(
                    icon = Icons.Filled.Autorenew,
                    title = "${toFa(stats.recurringCount)} اشتراکِ تکراری",
                    subtitle = "ماهی ${compact(stats.recurringMonthly)}",
                    bg = DiscoverWarnBg,
                    border = DiscoverWarnBorder,
                    pill = DiscoverWarnPill,
                    ink = DiscoverWarnInk,
                    subInk = DiscoverWarnSubInk,
                    iconInk = DiscoverWarnIconInk,
                )
            }
        }
        stats.overspentCategory?.let { over ->
            item {
                DiscoveryCard(
                    icon = Icons.Filled.BarChart,
                    title = "${over.name} ${toFa(over.percent)}٪ بیشتر از معمول",
                    subtitle = "نسبت به میانگینِ سه ماه",
                    bg = DiscoverDangerBg,
                    border = DiscoverDangerBorder,
                    pill = DiscoverDangerPill,
                    ink = AppText,
                    subInk = AppMuted,
                    iconInk = AppDanger,
                )
            }
        }
        item { ExportRow(onClick = onOpenExport) }
    }
}

enum class ReportPeriod(val label: String, val months: Int) {
    MONTH("ماه", 1),
    SEASON("فصل", 3),
    YEAR("سال", 12),
}

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
@Composable
private fun ReportHeader(
    period: ReportPeriod,
    onPeriod: (ReportPeriod) -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    showControls: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("گزارش", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        // تو حالتِ خالی نه بازه‌ای برای انتخاب هست نه مبلغی برای پنهان‌کردن (فریمِ `21c` هدرِ لخت).
        if (!showControls) return@Row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            // تاگلِ ماه/فصل/سال - انتخاب‌شده قرصِ سبزِ پرشده، بقیه فقط متن.
            ReportPeriod.entries.forEach { p ->
                val selected = p == period
                Text(
                    p.label,
                    color = if (selected) Color.White else AppMuted,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) AppPrimary else Color.Transparent)
                        .pressScaleClickable { onPeriod(p) }
                        .padding(horizontal = if (selected) 11.dp else 9.dp, vertical = 6.dp),
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (privacyMode) PrivacyOnBg else PrivacyOffBg)
                    .border(
                        1.5.dp,
                        if (privacyMode) PrivacyOnBorder else AppLine,
                        RoundedCornerShape(10.dp),
                    )
                    .pressScaleClickable(onClick = onTogglePrivacy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "پنهان‌کردنِ مبلغ‌ها",
                    tint = if (privacyMode) PrivacyOnInk else AppMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private val PrivacyOffBg = Color(0xFFF5F8F6)
private val PrivacyOnBg: Color
    @Composable get() = AppWarningPill
private val PrivacyOnBorder = Color(0xFFF0CE9B)
private val PrivacyOnInk: Color
    @Composable get() = AppWarningInk
// ═══ ۱ب · کارتِ خط‌چینِ «نموداری برای کشیدن نیست» (فریمِ `21c`) ═════════════════════
@Composable
private fun NoChartCard(onAddTransaction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurface)
            .dashedBorder(20.dp)
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // چهار میله‌ی خط‌چینِ خالی که یکی‌شون سبزِ توپره - «شکلِ نمودارِ پیشاپیش».
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.height(92.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                listOf(30.dp to false, 51.dp to false, 39.dp to true, 69.dp to false).forEach { (h, filled) ->
                    val shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    Box(
                        modifier = Modifier
                            .width(23.dp)
                            .height(h)
                            .clip(shape)
                            .background(if (filled) SkeletonBarFill else SkeletonBarBg)
                            .then(
                                if (filled) {
                                    Modifier.border(1.5.dp, AppPrimary, shape)
                                } else {
                                    Modifier.dashedBorder(6.dp, width = 1.5.dp)
                                }
                            ),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(2.dp)
                    .background(SkeletonBaseline),
            )
        }
        // ⚠️ عنوان و توضیح **یه بلوکِ واحد**ن با فاصله‌ی ۶ (مثلِ `margin-top`ی فریم)، نه دو
        // آیتمِ جدا با فاصله‌ی منفی - `Modifier.padding` عددِ منفی رو قبول نمی‌کنه و همون
        // لحظه‌ی رسم کرش می‌ده (کرشِ نسخه‌ی ۱.۰.۴۷۷: «Padding must be non-negative»).
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "نموداری برای کشیدن نیست",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "با سه تراکنش، اولین نمودارت شکل می‌گیرد. با یک ماه، مقایسه‌ی ماه‌به‌ماه هم اضافه می‌شود.",
                color = AppMuted,
                fontSize = 12.5.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "ثبتِ اولین خرج",
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(PrimaryShadow, 4.dp, 999.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onAddTransaction)
                .padding(vertical = 15.dp),
        )
    }
}

/** نوارِ طلاییِ «اگر پیامکِ بانکی را وصل کنی، گزارش خودش پر می‌شود». */
@Composable
private fun BankSmsHintCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoldHintBg)
            .border(1.5.dp, GoldHintBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldHintIcon,
            modifier = Modifier.size(17.dp),
        )
        Text(
            "اگر پیامکِ بانکی را وصل کنی، گزارش خودش پر می‌شود",
            color = GoldHintInk,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
        )
    }
}

/** «وقتی داده داشته باشی اینها را می‌بینی» - سه نقطه‌ی رنگیِ فریم. */
@Composable
private fun ComingSoonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(18.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "وقتی داده داشته باشی اینها را می‌بینی",
            color = AppMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        listOf(
            AppDanger to "سهمِ هر دسته از خرجِ ماه",
            AppInfo to "مقایسه‌ی این ماه با ماهِ قبل و پارسال",
            AppPurple to "تفکیکِ خرجِ ثابت از متغیر",
        ).forEach { (dot, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(dot),
                )
                Text(label, color = AppMuted, fontSize = 11.5.sp)
            }
        }
    }
}

private val SkeletonBarBg: Color
    @Composable get() = AppLineRow
private val SkeletonBarFill: Color
    @Composable get() = AppPrimaryPill
private val SkeletonBaseline = Color(0xFFDCE7E1)
private val PrimaryShadow = Color(0xFF0B8C57)
private val GoldHintBg: Color
    @Composable get() = AppWarningPill
private val GoldHintBorder = Color(0xFFFFD79A)
private val GoldHintIcon: Color
    @Composable get() = AppWarningInk
private val GoldHintInk = Color(0xFF8B5A00)

// ═══ ۲ · هیرویِ بنفش ════════════════════════════════════════════════════════════
@Composable
private fun PeriodSpendHero(
    label: String,
    spend: Double,
    deltaPercent: Int?,
    bars: List<Double>,
    firstLabel: String,
    lastLabel: String,
    privacyMode: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(PurpleShadow, 5.dp, AppRadius.card)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(Brush.linearGradient(listOf(AppPurple, PurpleDeep)))
            .padding(16.dp),
    ) {
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
                        maskIfPrivate(masked, fmt(spend)),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (deltaPercent != null) {
                Text(
                    (if (deltaPercent > 0) "▲ " else "▼ ") + "${toFa(kotlin.math.abs(deltaPercent))}٪ قدرتِ خرید",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        if (bars.any { it > 0.0 }) {
            val max = bars.max()
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                bars.forEachIndexed { index, value ->
                    val current = index == bars.lastIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight((value / max).toFloat().coerceIn(0.06f, 1f))
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(if (current) Color.White else Color.White.copy(alpha = 0.30f)),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(firstLabel, color = Color.White.copy(alpha = 0.62f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Text(lastLabel, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Text(
                "هنوز خرجی ثبت نکردی - با اولین تراکنش، روندِ ماه‌ها همین‌جا ساخته می‌شه",
                color = Color.White.copy(alpha = 0.72f),
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
@Composable
private fun FixedVsFreeCard(
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
            Text("${toFa(fixedShare)}٪", color = AppDangerInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
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
                "اجاره، اقساط و قبض ${maskIfPrivate(masked, compact(fixedAmount))} از درآمدت را برده — " +
                    "${maskIfPrivate(masked, compact(freeAmount))} برای خرجِ آزاد مانده.",
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
private fun CategoryDonutCard(byCategory: Map<String, Double>, total: Double, privacyMode: Boolean) {
    val top = remember(byCategory) { byCategory.entries.sortedByDescending { it.value }.take(3) }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(14.dp),
    ) {
        CategoryDonut(
            slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
            size = 74.dp,
            strokeWidth = 13.dp,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, compact(total)),
                        color = AppText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text("این ماه", color = AppLabel, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            top.forEachIndexed { i, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors[i % colors.size]),
                    )
                    Text(
                        entry.key,
                        color = AppText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 7.dp),
                    )
                    Text(
                        "${toFa((entry.value / total * 100).toInt())}٪",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

// ═══ ۵ و ۶ · کارت‌های کشف ═══════════════════════════════════════════════════════
/** کارتِ کشف - گوشه ۲۰ · پدینگ ۱۴×۱۶ · حاشیه ۲ · قابِ آیکونِ ۳۴ با گوشه‌ی ۱۱ · فاصله ۱۱. */
@Composable
private fun DiscoveryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bg: Color,
    border: Color,
    pill: Color,
    ink: Color,
    subInk: Color,
    iconInk: Color,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(2.dp, border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconInk, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ink, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = subInk, fontSize = 9.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = ChevronInk,
            modifier = Modifier.size(13.dp),
        )
    }
}

private val DiscoverWarnBg: Color
    @Composable get() = AppWarningPill
private val DiscoverWarnBorder = Color(0xFFFFD79A)
private val DiscoverWarnPill = Color(0xFFFFE3B8)
private val DiscoverWarnInk = Color(0xFF8B5A00)
private val DiscoverWarnSubInk = Color(0xFF8B6F3D)
private val DiscoverWarnIconInk: Color
    @Composable get() = AppWarningInk
private val DiscoverDangerBg: Color
    @Composable get() = AppDangerPill
private val DiscoverDangerBorder: Color
    @Composable get() = AppUrgentBorder
private val DiscoverDangerPill: Color
    @Composable get() = AppUrgentShadow
private val ChevronInk = Color(0xFFC7D2CC)

// ═══ ۷ · خروجی ═════════════════════════════════════════════════════════════════
@Composable
private fun ExportRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLineRow, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
        }
        Text(
            "خروجیِ اکسل و PDF",
            color = AppText,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = ChevronInk, modifier = Modifier.size(13.dp))
    }
}
