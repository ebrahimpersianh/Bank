package ir.sadteam.loancalc.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.findCategory
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.accounting.NewTransactionSheet
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import androidx.compose.material.icons.filled.PriorityHigh
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.TodayCard
import ir.sadteam.loancalc.ui.components.countUpAmount
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.note.NoteViewModel
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.components.AvatarView

/**
 * تبِ «خانه» - داشبوردِ ورودیِ اصلیِ اپ، هم‌راستا با نمونه‌ی رفرنس (Poolaki - رجوع کن به CLAUDE.md،
 * «بازطراحیِ تبِ خانه»): خلاصه‌ی مانده‌ی کلِ حساب‌ها بالای صفحه، کارتِ «امروز» با میان‌برِ سریعِ
 * قسط/چک/یادداشت، لیستِ تراکنش‌های اخیر، دکمه‌ی شناورِ افزودن. چون پرداختِ قسط/چک خودکار تو
 * تراکنشِ حسابداری هم ثبت می‌شه (رجوع کن به «سینکِ خودکارِ پرداختِ وام/چک ↔ تراکنشِ حسابداری» تو
 * CLAUDE.md)، همون [AccountViewModel.transactions] برای «تراکنش‌های اخیر» کافیه - نیازی به ادغامِ
 * جداگانه‌ی جدولِ اقساط نیست.
 */
@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
    noteViewModel: NoteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    urgentDueViewModel: UrgentDueViewModel = hiltViewModel(),
) {
    val userName by authViewModel.userName.collectAsState()
    val urgentDue by urgentDueViewModel.urgent.collectAsState()
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by accountViewModel.transactions.collectAsState()
    val privacyMode = LocalPrivacyMode.current

    val recentTransactions = remember(transactions) {
        transactions
            .sortedWith(
                compareByDescending<AccountTransactionEntity> { it.year }
                    .thenByDescending { it.month }
                    .thenByDescending { it.day }
                    .thenByDescending { it.id },
            )
            .take(6)
    }

    // خرجِ ۷ روزِ گذشته (قدیمی‌ترین → امروز) برای نمودارِ میله‌ایِ کارتِ قهرمان (`15a`).
    // داده‌ی جدیدی لازم نداره - از همون تراکنش‌های موجود حساب می‌شه.
    val weekSpend = remember(transactions) {
        val today = JalaliCalendar.today()
        (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            transactions
                .filter { it.type != "DEPOSIT" && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount }
        }
    }
    val todaySpend = weekSpend.last()
    val yesterdaySpend = weekSpend[weekSpend.lastIndex - 1]

    // کارتِ «مرورِ هفته»ی طرح - جمعِ این هفته در برابرِ هفته‌ی قبل و درصدِ تغییر.
    val weekTotal = remember(weekSpend) { weekSpend.sum() }
    val prevWeekTotal = remember(transactions) {
        val today = JalaliCalendar.today()
        (13 downTo 7).sumOf { back ->
            val d = PersianCalendar.addDays(today, -back)
            transactions
                .filter { it.type != "DEPOSIT" && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount }
        }
    }

    var selectedDate by remember { mutableStateOf(JalaliCalendar.today()) }
    var showAddNote by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    // شیتِ «تراکنش جدید» (خرج/دخل/جابجایی) - خواسته‌ی صریحِ کاربر: دکمه‌ی + تو تبِ خانه همین رو
    // باز کنه، نه اینکه ببره تبِ دارایی.
    var showNewTransaction by remember { mutableStateOf(false) }

    if (showNewTransaction) {
        NewTransactionSheet(onDismiss = { showNewTransaction = false })
        return
    }

    if (showAddNote) {
        QuickAddNoteDialog(
            date = selectedDate,
            onDismiss = { showAddNote = false },
            onSubmit = { text ->
                noteViewModel.addNote(text, selectedDate.y, selectedDate.m, selectedDate.d, null)
                showAddNote = false
            },
        )
    }

    // خواسته‌ی صریحِ کاربر: تپ رو خودِ متنِ تاریخ تو کارتِ «امروز» یه صفحه‌ی تقویمِ کامل باز کنه، نه
    // فقط قدم‌به‌قدم با فلش. همون CalendarPickerScreenِ مشترکِ اپ (الگوی AddManualLoanScreen و...).
    if (showCalendarPicker) {
        CalendarPickerScreen(
            initialDate = selectedDate,
            onDateSelected = { date -> selectedDate = date; showCalendarPicker = false },
            onBack = { showCalendarPicker = false },
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // سربرگِ صفحه‌ی خانه (کارتِ `15a`): تاریخِ امروز ۱۱/۷۰۰ و زیرش سلام ۱۹/۹۰۰.
            // اسمِ کاربر اختیاریه (تسکِ #37) - اگه نذاشته باشه «خوش آمدی» میاد، دقیقاً مثلِ
            // حالتِ خالیِ طرح (کارتِ `15b`).
            item {
                HomeGreetingHeader(userName = userName)
            }
            item {
                // ⚠️ **بازطراحیِ سبکِ «جیبک»** - کارتِ قهرمانِ خانه (کارتِ `15a`ی فایلِ طراحی).
                // قبلاً یه AppCardِ شیشه‌ای با گرادیانِ سبزِ نیمه‌شفاف و متنِ تیره بود. الان طبقِ طرح
                // یه **کارتِ سبزِ توپر** با متنِ سفیده: `linear-gradient(160deg,#0EA968,#0B8C57)` +
                // سایه‌ی سختِ `0 5px 0 #096F45` + یه هاله‌ی نرمِ سفید تو گوشه‌ی بالا-چپ.
                // تنها کارتِ «رنگیِ» صفحه‌ست - قاعده‌ی «حداکثر یک رنگِ لهجه در هر صفحه».
                //
                // عددِ قهرمان **«خرجِ امروز»**ه، دقیقاً مثلِ طرح - نه «مانده‌ی کل»ِ نسخه‌ی قبل.
                // (خواسته‌ی صریحِ کاربر: «کپیِ برابرِ اصل، مو نزنه».) مانده‌ی کل حذف نشد؛
                // تبِ «دارایی» عددِ اصلیِ خودشه و تپ رو همین کارت هم می‌بره همون‌جا.
                HomeBalanceHero(
                    todaySpend = todaySpend,
                    yesterdaySpend = yesterdaySpend,
                    weekSpend = weekSpend,
                    privacyMode = privacyMode,
                    onClick = { onNavigateToRoute("assets") },
                )
            }
            // کارتِ **فوریِ** `15a` - نزدیک‌ترین قسطِ سررسیدشده‌ی پرداخت‌نشده، با دکمه‌ی
            // «پرداخت شد» درجا. تنها کارتِ فوریِ صفحه‌ست (قاعده‌ی «حداکثر یکی در هر صفحه»).
            urgentDue?.let { due ->
                item {
                    UrgentDueCard(
                        title = "قسطِ ${due.loan.name}",
                        amount = due.amount,
                        daysOverdue = due.daysOverdue,
                        privacyMode = privacyMode,
                        onPay = { urgentDueViewModel.markPaid(due) },
                        onOpen = { onNavigateToRoute("loan") },
                    )
                }
            }
            // کارتِ «مرورِ هفته» (`15a`) - نوارِ رنگیِ ۴ پیکسلیِ بالا + سه ستونِ جمعِ هفته /
            // هفته‌ی قبل / تغییر. رنگِ نوار و درصد از **جهتِ** تغییر میاد: بیشتر شدن قرمز،
            // کمتر شدن سبز.
            item {
                WeekReviewCard(
                    weekTotal = weekTotal,
                    prevWeekTotal = prevWeekTotal,
                    privacyMode = privacyMode,
                )
            }
            item {
                TodayCard(
                    date = selectedDate,
                    onPrevDay = { selectedDate = PersianCalendar.addDays(selectedDate, -1) },
                    onNextDay = { selectedDate = PersianCalendar.addDays(selectedDate, 1) },
                    onDateClick = { showCalendarPicker = true },
                    onAddInstallment = { onNavigateToRoute("loan") },
                    onAddCheque = { onNavigateToRoute("cheque") },
                    onAddNote = { showAddNote = true },
                )
            }
            // کارتِ «هنوز سرشماره اضافه نکردی» - خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع. فقط وقتی
            // دیده می‌شه که **هیچ** حساب‌کتابی سرشماره‌ی پیامک نداره؛ به‌محضِ اینکه یکی اضافه شد
            // خودش محو می‌شه (نه یه بنرِ همیشگیِ آزاردهنده).
            val noSmsSender = accounts.isNotEmpty() && accounts.none { !it.smsSender.isNullOrBlank() }
            if (noSmsSender) {
                item {
                    AppCard(modifier = Modifier.pressScaleClickable(onClick = { onNavigateToRoute("assets") })) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Sms,
                                contentDescription = null,
                                tint = AppPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                "هنوز سرشماره اضافه نکردی",
                                color = AppText,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f).padding(start = 10.dp),
                            )
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "تنظیمات پیامک",
                                tint = AppMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
            item {
                AppCard(label = "تراکنش‌های اخیر") {
                    if (recentTransactions.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.SwapHoriz,
                            title = "هنوز تراکنشی نیست",
                            description = "پرداختِ اقساط/چک‌ها یا ثبتِ دخل‌وخرج، همینجا دیده می‌شه.",
                        )
                    } else {
                        Column {
                            recentTransactions.forEachIndexed { index, tx ->
                                val accountName = accounts.firstOrNull { it.id == tx.accountId }?.name ?: "—"
                                RecentTransactionRow(tx, accountName, privacyMode)
                                if (index != recentTransactions.lastIndex) {
                                    Box(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        AppFab(
            onClick = { showNewTransaction = true },
            contentDescription = "افزودنِ تراکنش",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }
}

/**
 * کارتِ قهرمانِ تبِ خانه - کارتِ `15a`ی فایلِ طراحی.
 *
 * تنها سطحِ «رنگیِ» صفحه‌ست: سبزِ توپر با متنِ سفید، سایه‌ی سختِ `0 5px 0 #096F45` و یه هاله‌ی
 * نرمِ سفید تو گوشه‌ی بالا-چپ (تنها گرادیانِ رادیالِ باقی‌مونده‌ی اپ - تو خودِ طرح هست).
 *
 * ⚠️ عمداً `AppCard` نیست: `AppCard` سطحِ **مات و بی‌رنگ** با حاشیه‌ی خاکستریه؛ این کارت سبزِ
 * بی‌حاشیه با سایه‌ی سبزِ تیره‌ست. این تنها استثنای قاعده‌ی «هیچ کارتی رو دستی نساز»ه و برای
 * همین اینجا یه کامپوننتِ نام‌دارِ جداست، نه یه بلوکِ inline که یه‌بارِ دیگه تکرار بشه.
 */
/**
 * سربرگِ تبِ خانه - کارتِ `15a` / `15b`ی فایلِ طراحی.
 *
 * تاریخِ امروز (۱۱/۷۰۰ خاکستری) و زیرش سلام (۱۹/۹۰۰). اسمِ کاربر **اختیاریه** (تسکِ #37):
 * اگه ثبت نکرده باشه «خوش آمدی» میاد - همون متنِ حالتِ خالیِ طرح.
 *
 * 📌 قرصِ «فعال» و شمارنده‌ی سکه که طرح اینجا داره **هنوز ساخته نشدن** - گیمیفیکیشن به جدولِ
 * دیتابیسِ جدید نیاز داره و قدمِ جداییه.
 */
@Composable
private fun HomeGreetingHeader(userName: String?) {
    val today = remember { JalaliCalendar.today() }
    // آدمکِ پروفایل - **۳۲px** طبقِ کارتِ `32c` («نوارِ بالای خانه ۳۲px»).
    val avatarViewModel: AvatarViewModel = hiltViewModel()
    val avatar by avatarViewModel.avatar.collectAsState()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AvatarView(avatar, size = 32.dp)
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                "${toFa(today.d)} ${persianMonthName(today.m)}",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (userName.isNullOrBlank()) "خوش آمدی" else "سلامْ $userName",
                color = AppText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun HomeBalanceHero(
    todaySpend: Double,
    yesterdaySpend: Double,
    weekSpend: List<Double>,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    // شمارشِ بالارونده - تو حالتِ خصوصی خاموشه (عدد پشتِ ••• مخفیه، انیمیشن بی‌معنیه).
    val shown = countUpAmount(todaySpend, enabled = !privacyMode)
    // «۳۱٪ کمتر از دیروز» - نسبت به خرجِ دیروز. اگه دیروز صفر بوده مقایسه بی‌معنیه و قرص نمیاد.
    val deltaPercent: Int? = if (yesterdaySpend > 0.0) {
        (((todaySpend - yesterdaySpend) / yesterdaySpend) * 100).toInt()
    } else {
        null
    }

    AppHeroCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text("خرجِ امروز", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, fmt(shown)),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (deltaPercent != null) {
                HeroPill(
                    icon = if (deltaPercent < 0) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    text = if (deltaPercent < 0) {
                        "${toFa(-deltaPercent)}٪ کمتر از دیروز"
                    } else {
                        "${toFa(deltaPercent)}٪ بیشتر از دیروز"
                    },
                )
            }
        }
        HomeSevenDayChart(values = weekSpend, modifier = Modifier.padding(top = 12.dp))
        HomeSevenDayChartLabels()
    }
}

/**
 * قرصِ نیمه‌شفافِ سفید رو کارتِ قهرمان - `rgba(255,255,255,.2)`، متنِ ۹٫۵/۹۰۰ طبقِ طرح.
 * آیکونش اختیاریه (تو طرح یه فلشِ ۱۰ پیکسلی کنارِ درصدِ تغییر داره).
 */
@Composable
private fun HeroPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(HeroPillBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
        }
        Text(text, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
    }
}


/**
 * کارتِ «مرورِ هفته» - کارتِ `15a`ی فایلِ طراحی.
 *
 * مقادیرِ دقیقِ طرح: کارتِ سفید با **نوارِ رنگیِ ۴ پیکسلی چسبیده به لبه‌ی بالا** (پس پدینگِ کارت
 * صفره و خودِ محتوا پدینگِ ۱۳×۱۵ می‌گیره)، عنوانِ ۱۲٫۵/۸۰۰، و سه ستون با برچسبِ ۸٫۵/۷۰۰ و
 * عددِ ۱۲/۹۰۰.
 *
 * رنگِ نوار و ستونِ «تغییر» از **جهتِ** تغییر میاد: خرجِ بیشتر قرمز، خرجِ کمتر سبز.
 */
/**
 * کارتِ **فوریِ** تبِ خانه - کارتِ `15a`ی فایلِ طراحی.
 *
 * مقادیرِ دقیقِ طرح: زمینه `#FFF5F5` · حاشیه‌ی ۲ پیکسلیِ `#FFC9C9` · سایه‌ی سختِ `0 4px 0 #FFECEC`
 * · قابِ آیکونِ ۳۸ با گوشه‌ی ۱۲ و ته‌رنگِ `#FFECEC` · عنوانِ ۱۲٫۵/۸۰۰ · زیرعنوانِ ۱۰٫۵/۷۰۰ قرمز
 * · دکمه‌ی «پرداخت شد» کپسولیِ سبز با سایه‌ی `0 3px 0`.
 *
 * همه‌ی این‌ها از [AppCardVariant.URGENT] و [AppButtonVariant.IN_ROW] میان - چیزی دستی ساخته نشده.
 */
@Composable
private fun UrgentDueCard(
    title: String,
    amount: Double,
    daysOverdue: Int,
    privacyMode: Boolean,
    onPay: () -> Unit,
    onOpen: () -> Unit,
) {
    AppCard(
        variant = AppCardVariant.URGENT,
        contentPadding = 14.dp,
        modifier = Modifier.pressScaleClickable(onClick = onOpen),
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
                    .background(AppDangerPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PriorityHigh,
                    contentDescription = null,
                    tint = AppDanger,
                    modifier = Modifier.size(17.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        (if (daysOverdue == 0) "امروز سررسید" else "${toFa(daysOverdue)} روز عقب") +
                            " — " + maskIfPrivate(masked, fmt(amount)),
                        color = AppDangerInk,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            GradientButton(onClick = onPay, variant = AppButtonVariant.IN_ROW) {
                Text("پرداخت شد")
            }
        }
    }
}

@Composable
private fun WeekReviewCard(weekTotal: Double, prevWeekTotal: Double, privacyMode: Boolean) {
    val deltaPercent: Int? = if (prevWeekTotal > 0.0) {
        (((weekTotal - prevWeekTotal) / prevWeekTotal) * 100).toInt()
    } else {
        null
    }
    val worse = (deltaPercent ?: 0) > 0
    val stripe = if (worse) AppDanger else AppPrimary
    val deltaInk = if (worse) AppDangerInk else AppPrimaryDim

    AppCard(contentPadding = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(stripe),
        )
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
            Text("مرورِ هفته", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WeekReviewCell("جمعِ هفته", fmt(weekTotal), AppText, privacyMode)
                WeekReviewCell("هفته‌ی قبل", fmt(prevWeekTotal), AppText, privacyMode)
                if (deltaPercent != null) {
                    WeekReviewCell(
                        "تغییر",
                        (if (worse) "▲ " else "▼ ") + toFa(kotlin.math.abs(deltaPercent)) + "٪",
                        deltaInk,
                        privacyMode = false, // درصد مبلغ نیست، تو حالتِ خصوصی هم مخفی نمی‌شه
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekReviewCell(label: String, value: String, ink: Color, privacyMode: Boolean) {
    Column {
        Text(label, color = AppLabel, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, value),
                color = ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: AccountTransactionEntity, accountName: String, privacyMode: Boolean) {
    val category = findCategory(tx.category)
    val isIncome = tx.type == "DEPOSIT"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // مقادیر از بخشِ «۸ · ردیفِ فهرست»ِ سیستمِ طراحی: آیکونِ ۱۶ در قابِ ۳۲ با گوشه‌ی ۱۲ و
        // ته‌رنگِ دسته · عنوانِ ۱۲/۸۰۰ · فرادادهٔ ۹٫۵/۷۰۰ · مبلغِ ۹۰۰.
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (category != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(category.color.copy(alpha = 0.16f), RoundedCornerShape(AppRadius.icon)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(16.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    category?.name ?: (if (isIncome) "واریز" else "برداشت"),
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "$accountName · ${toFa(tx.day)}/${toFa(tx.month)}/${toFa(tx.year)}",
                    color = AppLabel,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                // عددِ منفی با «−» میاد نه پرانتز و نه خطِ تیره‌ی ساده - قاعده‌ی صریحِ سیستمِ طراحی.
                "${if (isIncome) "+" else "−"}${maskIfPrivate(masked, fmt(tx.amount))}",
                color = if (isIncome) AppPrimaryInk else AppDangerInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun QuickAddNoteDialog(date: PersianDate, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("یادداشتِ ${toFa(date.d)} ${persianMonthName(date.m)}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onSubmit(text.trim()) }) { Text("ثبت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

