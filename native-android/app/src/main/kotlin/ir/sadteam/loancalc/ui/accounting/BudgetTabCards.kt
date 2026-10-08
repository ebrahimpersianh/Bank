package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.foundation.shape.CircleShape
import ir.sadteam.loancalc.ui.components.HeroIncome
import ir.sadteam.loancalc.ui.components.HeroExpense
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.AbsoluteAlignment
import ir.sadteam.loancalc.ui.components.ChartTooltipHost
import ir.sadteam.loancalc.ui.components.ChartTooltip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
@Composable
internal fun BudgetHeader(onAdd: () -> Unit, showAdd: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("بودجه", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                "مدیریتِ درآمد و هزینه‌های ماهانه",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!showAdd) return@Row
        // ۱۵ مهر: همان دکمه‌ی گردِ برچسب‌دارِ صفحه‌ی دارایی - یک‌دست در کلِ برنامه.
        ir.sadteam.loancalc.ui.asset.HeaderRoundAction(
            icon = Icons.Filled.Add,
            label = "افزودن",
            description = "افزودنِ بودجه",
            fill = AppPrimaryPill,
            ink = AppPrimaryInk,
            onClick = onAdd,
        )
    }
}
// ═══ ۱ب · کارتِ خط‌چینِ «بودجه‌ای تعیین نشده» (فریمِ `21d`) ═════════════════════════
@Composable
internal fun NoBudgetCard(onCreate: () -> Unit) {
    // بازطراحیِ ChatGPT (۷ مهر، دورِ دوم): تصویرِ نمودارِ دایره‌ای، زمینه‌ی کرمیِ ملایم + فهرستِ فایده‌ها.
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ir.sadteam.loancalc.ui.components.EmptyHeroCard(
            illustration = ir.sadteam.loancalc.R.drawable.empty_illu_budget,
            tint = AppPrimaryPill.copy(alpha = 0.55f),
            title = "هنوز بودجه‌ای تعیین نشده",
            description = "برای دسته‌های مهمت سقفِ ماهانه بذار و از خرج‌هات بهتر باخبر باش.",
            action = "ساختنِ بودجه",
            onAction = onCreate,
        )
        ir.sadteam.loancalc.ui.components.EmptyFeatureList(
            "با بودجه‌بندی چه می‌تونی؟",
            listOf(
                ir.sadteam.loancalc.R.drawable.empty_icon_budget_control to "هزینه‌ها رو کنترل کنی",
                ir.sadteam.loancalc.R.drawable.empty_icon_budget_prevent to "از خرجِ بیشتر جلوگیری کنی",
                ir.sadteam.loancalc.R.drawable.empty_icon_budget_goals to "هدفِ مالی تعیین کنی",
                ir.sadteam.loancalc.R.drawable.empty_icon_budget_progress to "پیشرفتت رو ببینی",
            ),
        )
    }
}
@Composable
internal fun StarterSuggestions(
    starters: List<BudgetStarter>,
    privacyMode: Boolean,
    onAcceptAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            "پیشنهادِ جیبک بر پایه‌ی خرجِ ماهِ قبلت",
            color = AppMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        starters.forEach { starter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppRadius.card))
                    .background(AppSurface)
                    .border(2.dp, RailTrack, RoundedCornerShape(AppRadius.card))
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(starter.category.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        starter.category.icon,
                        contentDescription = null,
                        tint = starter.category.color,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        starter.category.name,
                        color = AppText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "ماهِ قبل ${maskIfPrivate(masked, (starter.lastMonth).rialToFaCompact())} تومان",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, (starter.cap).rialToFaCompact()),
                        color = BudgetGreenDeep,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
        Text(
            if (starters.size == 1) "پذیرشِ پیشنهاد" else "پذیرشِ هر دو پیشنهاد",
            color = BudgetGreenDeep,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(AppSurface)
                .border(1.5.dp, BudgetGreen, RoundedCornerShape(999.dp))
                .pressScaleClickable(onClick = onAcceptAll)
                .padding(vertical = 13.dp),
        )
    }
}
// ═══ ۲ · هیرویِ سهمِ روزانه ═══════════════════════════════════════════════════════
@Composable
internal fun DailyAllowanceHero(
    allowance: Double,
    monthDaily: List<Double>,
    monthIncome: Double,
    monthExpense: Double,
    week: List<Boolean>,
    weekSpent: List<Double>,
    saved: Double,
    /** خرجِ تا امروز از سهمِ منصفانه‌ی همین روزها بیشتر شده. */
    behind: Boolean,
    dayOfMonth: Int,
    daysInMonth: Int,
    daysLeft: Int,
    privacyMode: Boolean,
) {
    // چیدمانِ طرحِ ChatGPT (۲ مهر): دو قرصِ بالا، عددِ درشت راست و دو باکسِ درآمد/خرج چپ،
    // نمودارِ روزهای ماه با «امروز»، و دو خطِ پایین. `week`/`weekSpent` دیگر نمایش داده
    // نمی‌شوند - نمودارِ ماه همان خبر را کامل‌تر می‌دهد.
    // هم‌قدِ کارتِ خانه (خواسته‌ی کاربر، ۳ مهر): یک ردیفِ بالا، عدد و «تومان» در یک خط،
    // درآمد/خرج بی‌قاب مثلِ خانه، نمودارِ کوتاه‌تر و یک خطِ پایین.
    AppHeroCard {
        // خواسته‌ی کاربر (۳ مهر): قرصِ «روزِ X از Y» گوشه‌ی بالا-راست، «امروز می‌توانی…» زیرش،
        // و درآمد/خرج کوچک‌تر در ستونِ چپ از بالای کارت - تا ارتفاعِ کارت کم شود.
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                HeroPillLabel(icon = Icons.Filled.Today, text = "روزِ ${toFa(dayOfMonth)} از ${toFa(daysInMonth)}")
                Text(
                    "امروز می‌توانی خرج کنی",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    PrivacyCrossfade(privacyMode) { masked ->
                        AutoShrinkText(
                            maskIfPrivate(masked, allowance.rialToFaCompact()),
                            color = Color.White,
                            maxFontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        "تومان",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 10.dp)) {
                HeroMiniStat(up = true, label = "درآمدِ این ماه", value = monthIncome, privacyMode = privacyMode)
                HeroMiniStat(
                    up = false,
                    label = "خرجِ این ماه",
                    value = monthExpense,
                    privacyMode = privacyMode,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
        if (monthDaily.isNotEmpty()) {
            HeroChart(
                values = monthDaily,
                labels = monthDaily.indices.map { i -> if (i + 1 == dayOfMonth) "امروز" else "${toFa(i + 1)} این ماه" },
                valueLabel = { value -> if (privacyMode) "•••" else "${value.rialToFaCompact()} تومان" },
                currentIndex = (dayOfMonth - 1).coerceIn(0, monthDaily.lastIndex),
                natural = HeroChartStyle.BARS,
                height = 34.dp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${toFa(daysLeft)} روز تا پایانِ ماه",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (saved > 0) {
                Icon(
                    Icons.Filled.Savings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    "${ir.sadteam.loancalc.ui.privacy.maskIfPrivate(ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current, saved.rialToFaCompact())} کمتر از سهمِ روزانه خرج کردی",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
@Composable
private fun HeroPillLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(
            text,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
/** خطِ درآمد/خرجِ کارتِ بالا - **بی‌قاب**، دقیقاً مثلِ جفتِ «درآمد/خرجِ امروز»ِ کارتِ خانه. */
@Composable
private fun HeroMiniStat(up: Boolean, label: String, value: Double, privacyMode: Boolean, modifier: Modifier = Modifier) {
    val ink = if (up) HeroIncome else HeroExpense
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
            PrivacyCrossfade(privacyMode) { masked ->
                AutoShrinkText(
                    ir.sadteam.loancalc.ui.jibak.isoSigned(up, maskIfPrivate(masked, value.rialToFaCompact())),
                    color = ink,
                    maxFontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Icon(
                if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = ink,
                modifier = Modifier.padding(start = 3.dp).size(11.dp),
            )
        }
    }
}
// ═══ ۳ · کارتِ «کلِ ماه» ══════════════════════════════════════════════════════════
@Composable
internal fun MonthTotalCard(
    cap: Double,
    spent: Double,
    percent: Int,
    fraction: Float,
    projectedLeft: Double,
    privacyMode: Boolean,
) {
    val over = percent > 100
    val barColor = if (over) OverInk else AppPrimary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .border(AppStroke.card, CardBorder, RoundedCornerShape(AppRadius.card))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Text(
                "کلِ ماه",
                color = AppText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            // ماهِ ردشده قرمز، نه رنگِ «خوب».
            Text("${toFa(percent)}٪", color = barColor, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        val clamped = fraction.coerceIn(0f, 1f)
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(22.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp)).background(RailTrack),
            ) {
                if (clamped > 0f) Box(
                    modifier = Modifier
                        .widthIn(min = 22.dp)
                        .fillMaxWidth(clamped)
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.45f), barColor))),
                )
            }
            // دستگیره‌ی گردِ سرِ نوار (طرحِ ChatGPT) به‌جای سکه.
            // ۱۴ مهر: با درصدِ کم، جعبه از دستگیره باریک‌تر بود و دایره له می‌شد و شبیهِ «0» دیده می‌شد.
            Box(modifier = Modifier.widthIn(min = 22.dp).fillMaxWidth(clamped), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AppSurface)
                        .border(4.dp, barColor, CircleShape),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, cap.rialToFaCompact()), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Text("بودجه‌ی ماهانه", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, spent.rialToFaCompact()), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Text("خرج شده", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppIconFrame)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = GoldInk, modifier = Modifier.size(16.dp))
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    "با این روند ${maskIfPrivate(masked, projectedLeft.rialToFaCompact())} تومان تا آخرِ ماه می‌مونه.",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 7.dp),
                )
            }
        }
    }
}
// ═══ ۴ · ردیفِ دسته ═════════════════════════════════════════════════════════════
@Composable
internal fun CategoryBudgetRow(row: BudgetRowData, privacyMode: Boolean, onClick: () -> Unit = {}) {
    val tint = if (row.over) OverInk else row.category.color
    val soft = tint.copy(alpha = 0.12f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .border(2.dp, CardBorder, RoundedCornerShape(AppRadius.card))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(soft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    row.category.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(row.category.name, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        "${maskIfPrivate(masked, row.spent.rialToFaCompact())} از " +
                            "${maskIfPrivate(masked, row.cap.rialToFaCompact())} تومان",
                        color = AppMuted,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Text(
                // «۰٪» وقتی خرجِ کمی ثبت شده دروغ است؛ «کمتر از ۱٪» می‌نویسیم.
                if (row.percent == 0 && row.spent > 0.0) "کمتر از ۱٪" else "${toFa(row.percent)}٪",
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(tint.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        // نوارِ ۹ پیکسلی. دسته‌ی ردشده به‌جای رنگِ تخت **هاشورِ موربِ ۱۳۵ درجه** می‌گیره -
        // همون چیزی که یادداشتِ فریم صریحاً می‌خواد.
        BudgetBar(
            fraction = row.fraction,
            color = tint,
            striped = row.over,
            modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
        // ۱۶ مهر: چقدر مانده (یا چقدر بیشتر شده) زیرِ نوار نوشته می‌شود.
        PrivacyCrossfade(privacyMode) { masked ->
            val left = row.cap - row.spent
            Text(
                if (left >= 0) "${maskIfPrivate(masked, left.rialToFaCompact())} تومان باقی مانده"
                else "${maskIfPrivate(masked, (-left).rialToFaCompact())} تومان بیشتر از بودجه",
                color = if (left >= 0) AppMuted else OverInk,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}
@Composable
private fun BudgetBar(fraction: Float, color: Color, striped: Boolean, modifier: Modifier = Modifier) {
    // ⚠️ توکن‌های رنگ `@Composable`ان - قبل از Canvas تو یه val محلی خونده می‌شن.
    val track = RailTrackSoft
    val dark = color.copy(alpha = 0.82f)
    Canvas(modifier = modifier.height(9.dp)) {
        val h = size.height
        val r = h / 2f
        drawRoundRect(
            color = track,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        )
        // حداقلِ ۶dp وقتی چیزی خرج شده، تا نوار «خالیِ مطلق» دیده نشود.
        val w = if (fraction > 0f) maxOf(size.width * fraction.coerceIn(0f, 1f), 6.dp.toPx()) else 0f
        if (w <= 0f) return@Canvas
        // تو RTL نوار از سمتِ راست پر می‌شه.
        val left = size.width - w
        clipRect(left = left, right = size.width) {
            drawRoundRect(
                color = color,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
            )
            if (striped) {
                // هاشورِ ۱۳۵ درجه: ۶ پیکسل روشن، ۶ پیکسل تیره.
                val step = 12.dp.toPx()
                var x = left - h
                while (x < size.width + h) {
                    drawLine(
                        color = dark,
                        start = Offset(x, h),
                        end = Offset(x + h, 0f),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Butt,
                    )
                    x += step
                }
            }
        }
    }
}
// ═══ ۵ · کارتِ نارنجیِ پیشنهاد ════════════════════════════════════════════════════
@Composable
internal fun TransferSuggestionCard(text: String, onAccept: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(GoldBg)
            .border(1.5.dp, GoldBorder, RoundedCornerShape(AppRadius.card))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldInk,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text,
            color = GoldTextInk,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            "بله",
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(GoldButton)
                .pressScaleClickable(onClick = onAccept)
                .padding(horizontal = 11.dp, vertical = 6.dp),
        )
    }
}
// ═══ ۶ · دو کاشیِ ابزار ══════════════════════════════════════════════════════════
@Composable
private fun BudgetToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .border(AppStroke.card, CardBorder, RoundedCornerShape(AppRadius.card))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AddTileBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = BudgetGreen, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 18.sp)
            Text(subtitle, color = AppMuted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}
@Composable
internal fun BudgetToolRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .pressScaleClickable(scale = 0.98f, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(AddTileBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = BudgetGreen, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = AppMuted, fontSize = 10.5.sp, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}
/**
 * نوارِ هفت‌روزه‌ی کارتِ بودجه - **لمس‌پذیر** (گزارشِ کاربر، ۱ مهر: «بودجه اصلاً نمی‌گیرد»).
 * لمس، نگه‌داشتن و کشیدن مثلِ بقیه‌ی نمودارها؛ حباب روز و خرجِ آن روز را می‌گوید.
 * فیزیکی چپ‌به‌راست است: امروز سمتِ راست، مثلِ بقیه‌ی نمودارها.
 */
@Composable
private fun WeekShareStrip(week: List<Boolean>, spent: List<Double>, privacyMode: Boolean) {
    var touched by remember(week) { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    fun slotAt(x: Float, w: Float) = if (w <= 0f) 0 else ((x / w) * week.size).toInt().coerceIn(0, week.size - 1)
    Box(modifier = Modifier.fillMaxWidth().padding(top = 13.dp)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .pointerInput(week) {
                        widthPx = size.width.toFloat()
                        detectDragGestures(
                            onDragStart = { touched = slotAt(it.x, size.width.toFloat()) },
                            onDragEnd = { touched = null },
                            onDragCancel = { touched = null },
                            onDrag = { change, _ ->
                                change.consume()
                                touched = slotAt(change.position.x, size.width.toFloat())
                            },
                        )
                    }
                    .pointerInput(week) {
                        widthPx = size.width.toFloat()
                        detectTapGestures(onPress = {
                            touched = slotAt(it.x, size.width.toFloat())
                            tryAwaitRelease()
                            touched = null
                        })
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                week.forEachIndexed { index, under ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (index == touched) 9.dp else 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (under) Color.White else Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
        }
        ChartTooltipHost(visible = touched != null, modifier = Modifier.align(AbsoluteAlignment.TopLeft)) {
            val index = touched ?: week.lastIndex
            val ago = week.lastIndex - index
            val amount = spent.getOrElse(index) { 0.0 }
            ChartTooltip(
                title = when (ago) { 0 -> "امروز"; 1 -> "دیروز"; else -> "${toFa(ago)} روز پیش" },
                value = if (privacyMode) "•••" else
                    "${amount.rialToFaCompact()} تومان · ${if (week.getOrElse(index) { true }) "زیرِ سهم" else "بیشتر از سهم"}",
                centerX = widthPx * (index + 0.5f) / week.size,
                containerWidth = widthPx,
                background = Color.Black.copy(alpha = 0.45f),
                titleColor = Color.White.copy(alpha = 0.75f),
                valueColor = Color.White,
            )
        }
    }
}
