package ir.sadteam.loancalc.ui.home

import ir.sadteam.loancalc.data.netDangShares
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.accounting.ReportPeriod
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.HeroExpense
import ir.sadteam.loancalc.ui.components.HeroIncome
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.countUpAmount
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk

// ═══ ۲ · کارتِ سبزِ خرجِ امروز ═══════════════════════════════════════════════════
@Composable
internal fun TodaySpendHero(
    period: ReportPeriod,
    onPeriod: (ReportPeriod) -> Unit,
    periodLabel: String,
    periodSpend: Double,
    todaySpend: Double,
    todayIncome: Double,
    yesterdaySpend: Double,
    weekSpend: List<Double>,
    privacyMode: Boolean,
    onClick: () -> Unit,
    /** تغییرِ این هفته نسبت به هفته‌ی قبل (جایگزینِ کارتِ حذف‌شده‌ی «مرورِ هفته»). */
    weekChangePercent: Int? = null,
) {
    val shown = countUpAmount(periodSpend, enabled = !privacyMode)
    val deltaPercent: Int? = if (yesterdaySpend > 0.0) {
        (((todaySpend - yesterdaySpend) / yesterdaySpend) * 100).toInt()
    } else {
        null
    }
    val simpleHero = ir.sadteam.loancalc.ui.privacy.LocalSimpleMode.current
    AppHeroCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        // چهار قرصِ بازه. روی زمینه‌ی تیره‌ی کارت، انتخاب‌شده سفیدِ مات و بقیه فقط متن -
        // همان زبانِ تاگلِ تبِ گزارش، با رنگِ مناسبِ این زمینه.
        // حالتِ ساده: بی قرص‌های بازه (همیشه «این ماه»).
        if (!simpleHero) Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            ReportPeriod.entries.forEach { p ->
                val selected = p == period
                Text(
                    p.label,
                    color = if (selected) AppPrimaryInk else Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) Color.White else HeroPillBg)
                        .pressScaleClickable { onPeriod(p) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // ستونِ چپ. `weight` اجباری است: بی آن، عددِ بزرگ با اندازه‌ی متنِ درشت کلِ
            // عرض را می‌گیرد و ستونِ درآمد/خرج از کادر بیرون می‌افتد (باگی که کاربر با
            // اسکرین‌شات گزارش کرد: «همه‌چی کشیده شده»).
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        periodLabel,
                        color = HeroMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (period == ReportPeriod.WEEK && weekChangePercent != null && !privacyMode) {
                        Text(
                            // ۱۶ مهر: هفته‌ی قبلِ تقریباً خالی «۲۲۸۵٪» می‌ساخت که بی‌معنا بود؛ بالای ۲۰۰٪ «N برابر».
                            if (weekChangePercent >= 200) "▲ ${kotlin.math.round(1 + weekChangePercent / 100.0).toInt().toFa()} برابرِ هفته‌ی قبل"
                            else "${if (weekChangePercent <= 0) "▼" else "▲"} ${kotlin.math.abs(weekChangePercent).toFa()}٪ از هفته‌ی قبل",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(HeroPillBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        // fmt() جداکننده‌ی لاتین می‌داد و عدد ریال بود.
                        maskIfPrivate(masked, shown.rialToFaCompact()),
                        color = Color.White,
                        // ⚠️ **۲۶/۹۰۰ با letterSpacing منفی**، نه ۲۸. جدولِ تایپوگرافی دو
                        // ردیفِ جدا داره: «عددِ قهرمان» ۲۸ (بقیه‌ی تب‌ها) و «عددِ کارتِ
                        // سبزِ خانه» ۲۶ - و همین یکی مالِ این کارته.
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                HomeSevenDayChart(
                    values = weekSpend,
                    modifier = Modifier.padding(top = 12.dp),
                    // برچسبِ هر میله - بی این، لمسِ نمودار چیزی برای گفتن ندارد.
                    labels = weekSpend.indices.map { index ->
                        val ago = weekSpend.lastIndex - index
                        if (ago == 0) "امروز" else "${ago.toFa()} روز پیش"
                    },
                )
                HomeSevenDayChartLabels()
            }
            // 🚨 **درآمد و خرجِ امروز، با فلشِ رنگی** (خواسته‌ی کاربر با طرحِ مرجع).
            //
            // تا امروز این گوشه یک قرصِ «٪ کمتر از دیروز» بود: یک عددِ نسبی که فقط با
            // حفظ‌کردنِ رقمِ دیروز معنی می‌داد. جفتِ درآمد/خرج جوابِ سوالی است که کاربر
            // واقعاً صبح می‌پرسد - «امروز چقدر آمد، چقدر رفت».
            //
            // رنگ‌ها معنایی‌اند نه تزئینی، پس **با تمِ خریدنی نمی‌چرخند**: سبزِ درآمد و
            // قرمزِ خرج همان دو توکنِ سراسری‌اند. روی زمینه‌ی تیره‌ی هیرو، نسخه‌ی روشنشان.
            if (!simpleHero) Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 10.dp, top = 4.dp),
            ) {
                HeroFlowLine(
                    label = "درآمدِ امروز",
                    amount = todayIncome,
                    income = true,
                    privacyMode = privacyMode,
                )
                // ⚠️ این خط یک‌بار حذف شده بود چون عددِ بزرگِ کارت هم «خرجِ امروز» بود و
                // تکراری می‌شد. حالا که عددِ بزرگ **بازه‌ی انتخاب‌شده** است (هفته/ماه/…)،
                // دیگر تکراری نیست و جفتِ فلشِ مثبت/منفیِ طرح کامل می‌شود.
                HeroFlowLine(
                    label = "خرجِ امروز",
                    amount = todaySpend,
                    income = false,
                    privacyMode = privacyMode,
                    modifier = Modifier.padding(top = 7.dp),
                )
                // درصدِ دیروز از قرص درآمد و به یک خطِ ریزِ زیرِ همین جفت تبدیل شد -
                // خبرش می‌مانَد، ولی دیگر جای عددِ اصلی را نمی‌گیرد.
                if (deltaPercent != null && deltaPercent != 0) {
                    Text(
                        when {
                            deltaPercent < 0 -> "${(-deltaPercent).toFa()}٪ کمتر از دیروز"
                            // ۱۶ مهر: «۳۵۱۴۰۰٪ بیشتر» (دیروز تقریباً صفر بود) گمراه‌کننده بود؛
                            // مثلِ کارتِ هفته از ۲۰۰٪ به بالا «N برابر» می‌نویسیم.
                            deltaPercent >= 200 -> "${kotlin.math.round(1 + deltaPercent / 100.0).toLong().toFa()} برابرِ دیروز"
                            else -> "${deltaPercent.toFa()}٪ بیشتر از دیروز"
                        },
                        color = HeroMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
/**
 * یک خطِ «برچسب + فلش + مبلغ» برای گوشه‌ی کارتِ قهرمان.
 *
 * فلش **بالا برای درآمد و پایین برای خرج** است - همان قراردادی که در طرحِ مرجعِ کاربر
 * بود و در همه‌ی برنامه‌های مالی یکی است، پس چیزی برای یادگرفتن ندارد.
 */
@Composable
private fun HeroFlowLine(
    label: String,
    amount: Double,
    income: Boolean,
    privacyMode: Boolean,
    modifier: Modifier = Modifier,
) {
    // 🚨 **هر دو خط تک‌خطی‌اند** (گزارشِ کاربر با اسکرین‌شات: «همه‌چی کشیده شده»).
    // با اندازه‌ی متنِ بزرگِ تنظیمات، «درآمدِ امروز» و مبلغ می‌شکستند و چند خط می‌شدند؛
    // چون ارتفاعِ کارتِ قهرمان از محتوایش می‌آید، کارت سه‌برابر بلند می‌شد.
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(
            label,
            color = HeroMuted,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    (if (income) "+ " else "− ") + maskIfPrivate(masked, heroFlowAmount(amount)),
                    color = if (income) HeroIncome else HeroExpense,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                if (income) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (income) HeroIncome else HeroExpense,
                modifier = Modifier.padding(start = 3.dp).size(11.dp),
            )
        }
    }
}
/**
 * مبلغِ دو خطِ «درآمد/خرجِ امروز» با **یک قالبِ هم‌شکل**: میلیون، هزار یا عددِ کامل.
 * قبلاً «۸ میلیون» کنارِ «۳۵۲,۴۰۳» می‌نشست و ناهماهنگ بود.
 */
private fun heroFlowAmount(rial: Double): String {
    val toman = kotlin.math.abs(rialToToman(rial.toLong()))
    return when {
        toman >= 1_000_000_000L -> rial.rialToFaCompact()
        toman >= 1_000_000L -> rial.rialToFaCompact()
        toman >= 1_000L -> "${(toman / 1_000).toFa()} هزار"
        else -> toman.toFa()
    }
}
// ═══ ۳ · بودجهٔ ماه ════════════════════════════════════════════════════════════
/**
 * نوارِ بودجه با **سکه‌ی طلایی روی لبه‌ی پرشده** - امضای بصریِ همین کارت تو فریمِ `15a`.
 *
 * جمله‌ی زیرش پیش‌بینیِ واقعیه، نه متنِ ثابت: با نرخِ خرجِ تا امروز، آخرِ ماه چقدر می‌مونه
 * (یا چقدر کم میاد).
 */
@Composable
internal fun MonthBudgetCard(
    monthLabel: String,
    spent: Double,
    cap: Double,
    dayOfMonth: Int,
    daysInMonth: Int,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val ratio = (spent / cap).toFloat().coerceIn(0f, 1f)
    val percent = kotlin.math.round(spent / cap * 100).toInt()
    val projected = if (dayOfMonth > 0) spent / dayOfMonth * daysInMonth else 0.0
    val leftover = cap - projected

    AppCard(contentPadding = 12.dp, modifier = Modifier.pressScaleClickable(onClick = onClick)) {
      // تصویرِ سه‌بعدیِ کوچک کنارِ کارت (۸ مهر، طرحِ ChatGPT).
      Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.jibak_home_budget),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("بودجهٔ $monthLabel", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "${(percent).toFa()}٪",
                color = if (ratio >= 1f) AppDangerInk else AppPrimaryInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
        BudgetBarWithCoin(ratio = ratio, modifier = Modifier.padding(top = 5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = AppWarningInk,
                modifier = Modifier.size(11.dp),
            )
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    if (leftover >= 0) {
                        "با این روند، ${maskIfPrivate(masked, leftover.rialToFaCompact())} تومان تا آخرِ ماه می‌مونه"
                    } else {
                        "با این روند، ${maskIfPrivate(masked, (-leftover).rialToFaCompact())} تومان کم میاری"
                    },
                    color = AppWarningInk,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
        }
      }
    }
}
/** نوارِ ۱۴ پیکسلیِ بودجه با سکه‌ی ۱۷ پیکسلی رو لبه‌ی پرشده - عیناً از فریمِ `15a`. */
@Composable
private fun BudgetBarWithCoin(ratio: Float, modifier: Modifier = Modifier) {
    // ⚠️ **اشتباهِ خودم، اصلاح‌شده**: اول `#232E38` خونده بودم و فکر کردم عمدیه. کلاد دیزاین
    // تو `design/ANSWERS-section-37.md` بندِ ۴ تایید کرد که سهو بوده - اون رنگ فقط تو
    // نقشه‌ی **تیره** به کار می‌ره و تو هیچ فریمِ روشنی نیست. ریلِ درست `#EEF3F0`ه، یعنی
    // همون توکنِ `AppLineRow` که تو تمِ تیره خودش `#232E38` می‌شه.
    val track = AppLineRow
    Box(modifier = modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(track),
        )
        // ۱۶ مهر: پرشده تا خودِ سکه می‌رسد - با درصدِ کم سکه رویش می‌نشست و نوار خالی دیده می‌شد.
        if (ratio > 0f) Box(
            modifier = Modifier
                .fillMaxWidth(if (ratio >= 1f) 1f else ratio.coerceIn(0.06f, 0.94f))
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    // بودجه تمام شد: نوارِ پرِ قرمز و **بی سکه** - سکه‌ی تهِ نوارِ پر شبیهِ «نشانگری
                    // وسطِ راه» خوانده می‌شد (گزارشِ کاربر، ۳ مهر).
                    if (ratio >= 1f) Brush.horizontalGradient(listOf(AppDanger, AppDanger))
                    else Brush.horizontalGradient(listOf(AppPrimary, BarGradientEnd)),
                ),
        )
        if (ratio >= 1f) return@Box
        // سکه دقیقاً رو لبه‌ی پرشده می‌شینه. تو RTL «شروع» سمتِ راسته، پس با کسرِ عرض
        // جابه‌جا می‌شه نه با offsetِ ثابت.
        //
        // ⚠️ **فقط موقعیتِ سکه** بینِ ۶٪ و ۹۴٪ محدود می‌شه، نه خودِ عرضِ پر - وگرنه تو
        // درصدهای خیلی کم/زیاد نصفِ سکه بیرونِ کارت می‌زد (تذکرِ صریحِ طراح).
        Box(
            modifier = Modifier.fillMaxWidth(ratio.coerceIn(0.06f, 0.94f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            ir.sadteam.loancalc.ui.components.CoinIcon(size = 17.dp)
        }
    }
}
// ═══ ۶ · مرورِ هفته ════════════════════════════════════════════════════════════
/**
 * نوارِ رنگیِ ۴ پیکسلیِ بالا + **سه** ستون: جمعِ هفته / هفته‌ی قبل / تغییر.
 *
 * ⚠️ ستونِ سوم دورِ قبل جا افتاده بود چون فقط وقتی هفته‌ی قبل عدد داشت رندر می‌شد؛ فریم
 * همیشه هر سه ستون رو نشون می‌ده.
 */
/** خروجیِ [buildHeroSeries]: عنوان، جمعِ بازه و میله‌های همان بازه. */
internal data class HeroSeries(val label: String, val total: Double, val bars: List<Double>)
/**
 * داده‌ی کارتِ قهرمان برای بازه‌ی انتخاب‌شده.
 *
 * ⚠️ همان تعریف‌های تبِ گزارش: جابه‌جاییِ بینِ حساب‌ها خرج نیست، و «هفته» یعنی ۷ روزِ
 * گذشته تا امروز. اگر این دو تعریف این‌جا فرق کنند، کاربر برای یک چیز دو عدد می‌بیند.
 *
 * میله‌ها: هفته ۷ روز · ماه روزهای همان ماه · فصل و سال ماه‌به‌ماه (۹۰ میله در عرضِ یک
 * کارت خط می‌شود نه نمودار - همان تصمیمِ تبِ گزارش).
 */
internal fun buildHeroSeries(
    transactions: List<AccountTransactionEntity>,
    today: PersianDate,
    period: ReportPeriod,
): HeroSeries {
    val expenses = transactions.netDangShares().filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type != "DEPOSIT" }
    fun sumOfDay(d: PersianDate) =
        expenses.filter { it.year == d.y && it.month == d.m && it.day == d.d }.sumOf { it.amount }
    fun sumOfMonth(y: Int, m: Int) =
        expenses.filter { it.year == y && it.month == m }.sumOf { it.amount }

    val bars = when (period) {
        ReportPeriod.WEEK -> (6 downTo 0).map { back -> sumOfDay(PersianCalendar.addDays(today, -back)) }
        ReportPeriod.MONTH -> (1..JalaliCalendar.daysInMonth(today.y, today.m))
            .map { day -> sumOfDay(PersianDate(today.y, today.m, day)) }
        else -> (period.months - 1 downTo 0).map { back ->
            val m = ((today.m - 1 - back) % 12 + 12) % 12 + 1
            val y = if (today.m - back <= 0) today.y - 1 else today.y
            sumOfMonth(y, m)
        }
    }
    val label = when (period) {
        ReportPeriod.WEEK -> "خرجِ هفته"
        ReportPeriod.MONTH -> "خرجِ ${persianMonthName(today.m)}"
        ReportPeriod.SEASON -> "خرجِ سه ماه"
        ReportPeriod.YEAR -> "خرجِ امسال"
    }
    return HeroSeries(label = label, total = bars.sum(), bars = bars)
}
/** انتهای گرادیانِ نوارِ سبزِ بودجه - مقدارِ محلیِ فریم، توکن نیست. */
private val BarGradientEnd: Color
    @Composable get() = AppPrimaryInkLight
