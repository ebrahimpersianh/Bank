package ir.sadteam.loancalc.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.MonthForecast
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToFaCompactParts
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ═══ ۴ · قسطِ سررسیدشده ════════════════════════════════════════════════════════
@Composable
internal fun UrgentDueCard(
    title: String,
    amount: Double,
    daysOverdue: Int,
    privacyMode: Boolean,
    onPay: () -> Unit,
    onOpen: () -> Unit,
) {
    // ⚠️ **بی‌سایه** - این کارت درست زیرِ کارتِ قهرمان می‌شینه و دو سایه‌ی سختِ پشتِ‌هم
    // شلوغ می‌شه (قاعده‌ی صریحِ طراح برای همین فریم؛ تو فریم‌های دیگه سایه داره).
    // 🚨 **باریک‌تر شد** (بازخوردِ ۳۱ شهریور): این نوار مهم است ولی هر روز دیده می‌شود،
    // و یک کارتِ بلندِ قرمز بالای صفحه با گذشتِ زمان بیشتر «سروصدا» می‌شود تا هشدار.
    // پدینگِ ۱۱، آیکونِ ۳۲ و دکمه‌ی تک‌کلمه‌ای، همان اطلاعات را در ~۲۰٪ ارتفاعِ کمتر می‌دهد.
    AppCard(
        variant = AppCardVariant.URGENT,
        shadow = false,
        contentPadding = 11.dp,
        modifier = Modifier.pressScaleClickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.jibak_home_overdue),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        (if (daysOverdue == 0) "امروز سررسید" else "${(daysOverdue).toFa()} روز عقب") +
                            " — " + maskIfPrivate(masked, amount.rialToFaCompact()),
                        color = AppDangerInk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            // «پرداخت» به‌جای «پرداخت شد»: در یک ردیفِ باریک، دو کلمه دو خط می‌شد.
            GradientButton(onClick = onPay, variant = AppButtonVariant.IN_ROW) { Text("پرداخت") }
        }
    }
}
/**
 * **«تا آخرِ ماه کم میاری»** - هشدارِ پیش‌بینیِ کسری (رجوع کن به
 * [ir.sadteam.loancalc.core.MonthForecast]).
 *
 * چرا این برگ‌برنده‌ست: بقیه‌ی اپ‌ها فقط گذشته رو گزارش می‌دن؛ این تنها چیزیه که **قبل از
 * اتفاق** هشدار می‌ده.
 *
 * 🚨 **طلایی است، نه قرمز** (فریمِ `63c`). قرمز در این برنامه معنیِ ثبت‌شده دارد: پولی که
 * **واقعاً** دیر شده - قسطِ عقب‌افتاده، چکِ برگشتی. پیش‌بینی واقعیت نیست و قرمزکردنش
 * قرمزهای واقعی را ارزان می‌کند. طلایی از قبل زبانِ «در خطر» است.
 *
 * 🚨 **عددِ دوم اجباری است**: «روزی فلان‌قدر تا آخرِ ماه می‌رسانَد». هشدارِ بی راهِ‌حل فقط
 * اضطراب است - کاربر نمی‌داند چقدر باید کم کند.
 */
@Composable
internal fun ShortfallForecastCard(
    runsOutOnDay: Int,
    daysLeft: Int,
    perDaySpend: Double,
    balance: Double,
    safePerDay: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    AppCard(
        backgroundColor = AppWarningPill,
        borderColor = AppDueNextBorder,
        shadow = false,
        modifier = Modifier.pressScaleClickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(AppDueNextBorder),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = AppWarningInk, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                // پولِ مانده منفی یعنی «تمام شده»، نه «تمام می‌شود»؛ «روزی ۰ می‌رساند» هم بی‌معناست.
                val overspent = balance <= 0.0
                Text(
                    if (overspent) "بودجه‌ی این ماه تمام شده" else "با این سرعت، ${runsOutOnDay.toFa()} روز قبلِ آخرِ ماه تمام می‌شود",
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (overspent) PrivacyCrossfade(privacyMode) { masked ->
                    Column {
                        Text(
                            "${maskIfPrivate(masked, (-balance).rialToFaCompact())} بیشتر از بودجه خرج کرده‌ای.",
                            color = AppWarningInk,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "${daysLeft.toFa()} روز تا آخرِ ماه مانده - هر خرجِ تازه روی همین اضافه می‌شود.",
                            color = AppText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                } else PrivacyCrossfade(privacyMode) { masked ->
                    Column {
                        Text(
                            "روزی ${maskIfPrivate(masked, perDaySpend.rialToFaCompact())} خرج کرده‌ای و " +
                                "${maskIfPrivate(masked, balance.rialToFaCompact())} مانده برای ${daysLeft.toFa()} روز.",
                            color = AppWarningInk,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "روزی ${maskIfPrivate(masked, safePerDay.rialToFaCompact())} تا آخرِ ماه می‌رسانَد.",
                            color = AppText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }
        }
    }
}
// ═══ ۵ · دوناتِ دسته‌بندی‌ها ════════════════════════════════════════════════════
@Composable
internal fun CategoryBreakdownCard(
    byCategory: Map<String, Double>,
    total: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    // دسته‌ی زیرِ ۱٪ («قبض ۰٪») فقط شلوغی است (۱۴ مهر).
    val top = remember(byCategory, total) {
        byCategory.entries.sortedByDescending { it.value }
            .filter { total <= 0.0 || it.value / total >= 0.01 }
            .take(3)
    }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    val (centerNumber, centerUnit) = total.rialToFaCompactParts()
    // ۱۶ مهر (طرحِ مرتب‌تر): بالا حلقه + «جمعِ خرج» با عددِ درشت؛ پایین فهرستِ هم‌ترازِ
    // دسته‌ها (نام راست، مبلغ و درصد در ستونِ چپ). وسطِ حلقه سهمِ بزرگ‌ترین دسته و نامش است.
    AppCard(contentPadding = 16.dp, modifier = Modifier.pressScaleClickable(onClick = onClick)) {
      Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("خرجِ این ماه", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text("جزئیاتِ بیشتر", color = AppPrimaryInk, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = AppPrimaryInk,
                modifier = Modifier.size(14.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDonut(
                slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
                size = 80.dp,
                strokeWidth = 9.dp,
            ) {
                // وسطِ حلقه: سهمِ **بزرگ‌ترین** خرجِ ماه و نامش - «۹۹٪ قسط/چک» یعنی تقریباً همه‌ی
                // خرجِ این ماه همین بوده (قبلاً فقط «۱ دسته» بود که چیزی نمی‌گفت).
                val biggest = top.firstOrNull()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (biggest == null || total <= 0.0) "—" else "${((biggest.value / total * 100).toInt()).toFa()}٪",
                        color = AppText, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Black,
                    )
                    Text(
                        biggest?.key ?: "",
                        color = AppLabel, fontSize = 9.5.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 56.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text("جمعِ خرجِ این ماه", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            maskIfPrivate(masked, centerNumber),
                            color = AppText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                        Text(
                            centerUnit,
                            color = AppMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 5.dp, bottom = 3.dp),
                        )
                    }
                }
            }
        }
        androidx.compose.material3.HorizontalDivider(color = AppLineRow, modifier = Modifier.padding(top = 14.dp))
        top.forEachIndexed { i, entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = if (i == 0) 10.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                    modifier = Modifier.weight(1f).padding(start = 9.dp),
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, entry.value.rialToFaCompact()),
                        color = AppText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                }
                Text(
                    if (total <= 0.0) "" else "${((entry.value / total * 100).toInt()).toFa()}٪",
                    color = AppMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(44.dp),
                )
            }
        }
      }
    }
}
/**
 * **چهار درِ همیشه‌درمعرض** زیرِ کارتِ قهرمان - طرحِ مرجعِ کاربر.
 *
 * هر کارت یک عدد دارد، و همان عدد فرقش با یک دکمه‌ی ساده است: «۴ حساب» و «۳ چکِ باز»
 * پیش از بازکردن هم خبر می‌دهند. «گزارش‌ها» عددِ معناداری ندارد، پس زیرنویسِ توصیفی
 * می‌گیرد نه یک عددِ ساختگی.
 */
@Composable
private fun HomeQuickCardsRow(
    accountCount: Int,
    monthTransactionCount: Int,
    openChequeCount: Int,
    onNavigateToRoute: (String) -> Unit,
    onOpenTransactions: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeQuickCard(
            icon = Icons.Filled.AccountBalanceWallet,
            title = "حساب‌ها",
            subtitle = "${accountCount.toFa()} حساب",
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToRoute("assets") },
        )
        HomeQuickCard(
            icon = Icons.Filled.SwapHoriz,
            title = "تراکنش‌ها",
            subtitle = "${monthTransactionCount.toFa()} این ماه",
            modifier = Modifier.weight(1f),
            onClick = onOpenTransactions,
        )
        // «گزارش‌ها» و «چک‌ها» به خواسته‌ی کاربر (۱۳ مهر: «برنامه شلوغه») برداشته شدند.
    }
}
@Composable
private fun HomeQuickCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // کوتاه‌تر از دورِ قبل (بازخوردِ ۳۱ شهریور: «کارت‌ها کمی کوتاه‌تر تا محتوای بیشتری
    // دیده شود»): پدینگ ۱۰→۸ و فاصله‌ی عنوان ۷→۵. آیکون‌ها همه از یک خانواده‌ی
    // `Filled` و یک اندازه‌اند تا ردیف یکدست دیده شود.
    AppCard(contentPadding = 8.dp, modifier = modifier.pressScaleClickable(onClick = onClick)) {
        Icon(icon, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(17.dp))
        Text(
            title,
            color = AppText,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 5.dp),
        )
        Text(
            subtitle,
            color = AppLabel,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}
/**
 * ساعتِ محلیِ ثبت از رشته‌ی `createdAt` (ISOی UTC).
 *
 * `null` یعنی رشته خوانا نبود - تراکنشِ خیلی قدیمی یا واردشده از پشتیبانِ دستی؛
 * آن‌وقت ردیف فقط تاریخ نشان می‌دهد، نه ساعتِ ساختگی.
 */
private fun localTimeOf(createdAt: String): String? = runCatching {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    parser.timeZone = TimeZone.getTimeZone("UTC")
    val date = parser.parse(createdAt) ?: return null
    val out = SimpleDateFormat("HH:mm", Locale.US)
    out.timeZone = TimeZone.getDefault()
    // ⚠️ `toFa()` **اکستنشنِ Int** است نه String (باگِ بیلدِ ۶۱۶). برای رشته‌ی ساعت
    // `faDigits()` درست است - همان چیزی که `toFaTime` هم استفاده می‌کند.
    out.format(date).faDigits()
}.getOrNull()
/**
 * **سه تراکنشِ آخر** - میان‌برِ تاریخچه روی صفحه‌ی اول.
 *
 * سه ردیف نه بیشتر: این‌جا جای مرورِ تاریخچه نیست، جای «آخرین چیزی که ثبت شد درست
 * ثبت شد؟» است. «مشاهده‌ی همه» به تبِ دارایی می‌رود که دفترِ کاملِ تراکنش‌ها آن‌جاست.
 *
 * ⚠️ مرتب‌سازی با **شناسه** است نه تاریخِ شمسی: شناسه زمانِ ثبت است (میلی‌ثانیه) و دو
 * تراکنشِ یک روز را هم درست پشتِ هم می‌چیند.
 */
@Composable
internal fun RecentTransactionsCard(
    transactions: List<AccountTransactionEntity>,
    privacyMode: Boolean,
    onSeeAll: () -> Unit,
) {
    val recent = remember(transactions) { transactions.sortedByDescending { it.id }.take(3) }
    // ۱۶ مهر: همیشه ۲ تراکنشِ آخر پیداست (سؤالِ «آخرین اتفاقِ مالی چی بود؟»)؛ فلش سومی را باز می‌کند.
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "بستن" else "باز کردن",
                tint = AppMuted,
                modifier = Modifier.padding(end = 6.dp).size(22.dp),
            )
            Text("آخرین تراکنش‌ها", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text(
                "مشاهده‌ی همه",
                color = AppPrimaryInk,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
        (if (expanded) recent else recent.take(2)).forEachIndexed { index, tx ->
            val income = tx.type == "DEPOSIT"
            val transfer = tx.sourceType == SOURCE_TYPE_TRANSFER
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = if (index == 0) 10.dp else 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            when {
                                transfer -> AppIconFrame
                                income -> AppPrimaryPill
                                else -> AppDangerPill
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when {
                            transfer -> Icons.Filled.SwapHoriz
                            income -> Icons.Filled.ArrowUpward
                            else -> Icons.Filled.ArrowDownward
                        },
                        contentDescription = null,
                        tint = when {
                            transfer -> AppMuted
                            income -> AppPrimaryInk
                            else -> AppDangerInk
                        },
                        modifier = Modifier.size(14.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
                    Text(
                        tx.description.ifBlank { tx.category ?: (if (income) "واریز" else "برداشت") },
                        color = AppText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        // ساعت از `createdAt` می‌آید (ISOی UTC که همان لحظه‌ی ثبت است)،
                        // پس **هیچ ستونِ تازه و هیچ مهاجرتی لازم نبود**؛ تراکنشِ قدیمی که
                        // رشته‌اش خراب باشد فقط تاریخ نشان می‌دهد.
                        "${tx.day.toFa()} ${persianMonthName(tx.month)}" + (localTimeOf(tx.createdAt)?.let { " · $it" } ?: ""),
                        color = AppLabel,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        ir.sadteam.loancalc.ui.jibak.isoSigned(income, maskIfPrivate(masked, tx.amount.rialToFaCompact())),
                        color = if (income) AppPrimaryInk else AppDangerInk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}
@Composable
private fun WeekReviewCard(
    weekTotal: Double,
    prevWeekTotal: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val delta: Int? = if (prevWeekTotal > 0.0) {
        (((weekTotal - prevWeekTotal) / prevWeekTotal) * 100).toInt()
    } else {
        null
    }
    // ⚠️ **اصلاحِ برداشتِ قبلیِ من**: فکر کرده بودم نوار همیشه قرمزه چون تو فریم با
    // تغییرِ کاهشی هم قرمز بود. طراح تصریح کرد که اون فقط داده‌ی نمونه‌ی بدتر بوده و
    // رنگ **وضعیت** رو می‌گه: خرجِ بیشتر از هفته‌ی قبل قرمز، کمتر سبز.
    val statusColor = if (delta != null && delta > 0) AppDanger else AppPrimary
    AppCard(contentPadding = 0.dp, horizontalPadding = 0.dp, modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor))
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("مرورِ هفته", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = WeekChevron,
                    modifier = Modifier.size(13.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WeekCell("جمعِ هفته", weekTotal.rialToFaCompact(), AppText, privacyMode)
                WeekCell("هفته‌ی قبل", prevWeekTotal.rialToFaCompact(), AppText, privacyMode)
                // ⚠️ مثلثِ ▲/▼ رو **با کاراکتر ننویس** - Vazirmatn رندرش نمی‌کنه و مربعِ
                // خالی می‌شه (تذکرِ صریحِ طراح). آیکونِ ۹ پیکسلی جاشه.
                WeekCell(
                    label = "تغییر",
                    value = if (delta == null) "—" else "${(kotlin.math.abs(delta)).toFa()}٪",
                    ink = when {
                        delta == null -> AppMuted
                        delta > 0 -> AppDangerInk
                        else -> AppPrimaryInk
                    },
                    privacyMode = false,
                    trend = delta,
                )
            }
        }
    }
}
/** قابِ آیکونِ کارتِ فوری - مقدارِ محلیِ فریم. */
private val UrgentIconBg: Color
    @Composable get() = AppUrgentShadow
/** رنگِ شِورانِ کارتِ مرورِ هفته - مقدارِ صریحِ فریم. */
private val WeekChevron: Color
    @Composable get() = AppMarkOff
@Composable
private fun WeekCell(
    label: String,
    value: String,
    ink: Color,
    privacyMode: Boolean,
    /** مثبت = افزایش (مثلثِ بالا)، منفی = کاهش، `null` = بدونِ مثلث. */
    trend: Int? = null,
) {
    Column {
        Text(label, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                if (trend != null) {
                    Icon(
                        if (trend > 0) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = ink,
                        modifier = Modifier.size(9.dp),
                    )
                }
                Text(
                    maskIfPrivate(masked, value),
                    color = ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = if (trend != null) Modifier.padding(start = 2.dp) else Modifier,
                )
            }
        }
    }
}
// ═══ حالتِ خالی (`15b`) ════════════════════════════════════════════════════════
/** کارتِ خط‌چینِ «کیفت خالیه» با مسکاتِ جیبک و دکمه‌ی تمام‌عرض. */
@Composable
internal fun HomeEmptyHero(onAddFirst: () -> Unit) {
    // بازطراحیِ ChatGPT (۷ مهر، دورِ دوم) - تصویرِ تختِ کیفِ پول، زمینه‌ی سبزِ ملایم.
    ir.sadteam.loancalc.ui.components.EmptyHeroCard(
        illustration = ir.sadteam.loancalc.R.drawable.empty_illu_wallet,
        tint = ir.sadteam.loancalc.ui.theme.AppPrimaryPill.copy(alpha = 0.55f),
        title = "شروعِ مدیریتِ مالی",
        description = "اولین ثبتت رو انجام بده تا جیبک کم‌کم الگوی خرج‌هات رو بشناسه.",
        action = "ثبتِ اولین خرج",
        onAction = onAddFirst,
        guideKey = "add_tx",
    )
}
/**
 * یادداشتِ پاداشِ اولین ثبت - کارتِ نارنجیِ فریمِ `15b`.
 *
 * ⚠️ این کارت **تم‌آگاه نیست**: تو فریمِ روشن و تیره **عیناً همین رنگ‌ها**ست
 * (`#FFF1DC` / `#FFD79A` / `#8B5A00`). هر دو فریم چک شدن، پس مقادیر ثابت درست‌ان -
 * برخلافِ اشتباهِ ریلِ نوارِ بودجه که فقط از رو یه فریم حدس زده بودم.
 *
 * مقادیرِ صریحِ فریم: گوشه ۱۸ · پدینگِ ۱۳ در ۱۵ · حاشیه‌ی ۱٫۵ · فاصله‌ی آیکون تا متن ۱۰ ·
 * متنِ ۱۰٫۵ با وزنِ ۷۰۰ و ارتفاعِ خطِ ۱٫۸.
 */
@Composable
internal fun FirstRewardNote() {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(RewardNoteBg)
            .border(1.5.dp, RewardNoteBorder, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        androidx.compose.foundation.Image(
            androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.empty_illu_coins),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Text(
            "با اولین ثبت ۱۰ سکه می‌گیری و روزهای فعالت روشن می‌شه",
            color = RewardNoteInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 20.sp,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
private val RewardNoteBg: Color
    @Composable get() = AppWarningPill
private val RewardNoteBorder: Color
    @Composable get() = AppGoldPillSoft
private val RewardNoteInk: Color
    @Composable get() = AppGoldInkSoft
/** یکی از سه کاشیِ «یا از اینجا شروع کن». */
@Composable
internal fun RowScope.StarterTile(
    iconRes: Int,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(AppRadius.row))
            .background(AppSurface)
            .rowBorder()
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        androidx.compose.foundation.Image(
            androidx.compose.ui.res.painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
        )
        Text(
            label,
            color = AppText,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 7.dp),
        )
        Text(
            subtitle,
            color = AppMuted,
            fontSize = 9.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
/** حاشیه‌ی ۲ پیکسلیِ ردیف (`#EEF3F0`) - جدا شد چون سه‌بار تکرار می‌شد. */
@Composable
private fun Modifier.rowBorder(): Modifier =
    this.border(2.dp, AppLineRow, RoundedCornerShape(AppRadius.row))
/** حاشیه‌ی خط‌چینِ ۲ پیکسلیِ کارتِ حالتِ خالی. */
@Composable
private fun Modifier.dashedCardBorder(): Modifier {
    val color = ir.sadteam.loancalc.ui.theme.AppDashedBorder
    val radius = AppRadius.card
    return this.drawBehind {
        val stroke = 2.dp.toPx()
        val r = radius.toPx()
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = stroke,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f),
            ),
        )
    }
}
