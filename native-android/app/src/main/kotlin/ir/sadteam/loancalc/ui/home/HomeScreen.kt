package ir.sadteam.loancalc.ui.home

import androidx.activity.compose.BackHandler
import ir.sadteam.loancalc.ui.profile.BadgesScreen
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MonthForecast
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToFaCompactParts
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.CompactTransactionRow
import ir.sadteam.loancalc.ui.accounting.NewTransactionSheet
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.components.SkeletonRowList
import ir.sadteam.loancalc.ui.components.ActiveChip
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.FramedAvatar
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.CoinChip
import ir.sadteam.loancalc.ui.components.ConfirmPayDialog
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.JibakMascotFrame
import ir.sadteam.loancalc.ui.components.countUpAmount
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.coin.CoinHubScreen
import ir.sadteam.loancalc.ui.profile.BadgeRetroSheet
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.inbox.InboxViewModel
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/**
 * تبِ **خانه** - بازسازیِ کاملِ فریمِ `15a` (حالتِ عادی) و `15b` (روزِ اول / خالی).
 *
 * ⚠️ **این صفحه از نو نوشته شده، نه اصلاح.** دورِ قبل سبکِ جدید روی چیدمانِ اپِ قدیمی پوشیده
 * شد و نتیجه‌ش این بود که کاربر گفت «مثلِ برنامه‌ی قبلیه، فقط چهارتا آیتم اضافه شده». حالا
 * ترتیبِ کارت‌ها **عیناً** از خودِ فریم درآمده:
 *
 * ```
 * ۱ هدر: تاریخ + سلام + قرصِ فعال + شمارنده‌ی سکه
 * ۲ کارتِ سبز: خرجِ امروز + نمودارِ ۷ روزه
 * ۳ بودجهٔ ماه با سکه‌ی طلایی رو نوار
 * ۴ قسطِ سررسیدشده (فقط اگه باشه)
 * ۵ دوناتِ دسته‌بندی‌های همین ماه
 * ۶ مرورِ هفته - سه ستون
 * ```
 *
 * برای درآوردنِ همین لیست:
 * `python3 native-android/tools/static-checks/design-frames.py "design/Duolingo Redesign.dc.html" 15a`
 *
 * **چیزهایی که عمداً حذف شدن** (تو فریم نیستن): کارتِ «امروز» با میان‌برهای قسط/چک/یادداشت،
 * کارتِ «سرشماره اضافه نکردی»، و لیستِ «تراکنش‌های اخیر». هیچ‌کدوم قابلیتی رو از بین نمی‌برن -
 * تراکنش‌ها تو تبِ گزارش و سرشماره تو تنظیماتن.
 *
 * **رفتارهایی که از نسخه‌ی قبل حفظ شدن**: حالتِ خصوصی (`PrivacyCrossfade`)، شمارشِ بالارونده‌ی
 * عدد، بازکردنِ شیتِ تراکنشِ جدید با دکمه‌ی +، و تپ رو کارتِ قهرمان → تبِ دارایی.
 */
@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
    /**
     * کارتِ پیشنهادِ نوارِ پایین (`41a`) - به‌صورتِ یه اسلاتِ آماده‌ی رندر پاس داده می‌شه، نه
     * داده‌ی خام. دلیل: چیدمانِ نوار و `ViewModel`ش تو `MainActivity` زندگی می‌کنن (همون‌جا که
     * ویرایشگر هم باز می‌شه)؛ اگه `HomeScreen` خودش `hiltViewModel()` می‌گرفت یه **نمونه‌ی
     * دومِ** جدا می‌ساخت. `null` یعنی پیشنهادی در کار نیست.
     */
    navSuggestionSlot: (@Composable () -> Unit)? = null,
    accountViewModel: AccountViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    urgentDueViewModel: UrgentDueViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel(),
    inboxViewModel: InboxViewModel = hiltViewModel(),
) {
    val inboxCount by inboxViewModel.actionableCount.collectAsState()
    val inboxUnreadNews by inboxViewModel.unreadNews.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val urgentDue by urgentDueViewModel.urgent.collectAsState()
    val transactions by accountViewModel.transactions.collectAsState()
    val transactionsLoaded by accountViewModel.transactionsLoaded.collectAsState()
    val budgets by accountViewModel.budgets.collectAsState()
    val activeDays by gamificationViewModel.activeDays.collectAsState()
    val coins by gamificationViewModel.coins.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    // شرطِ برگشتنِ قرصِ «فعال» به هدر (`55a`). همان تعریفی که یادآورِ روزانه استفاده می‌کند:
    // امروز تراکنشی ثبت شده یا نه.
    val todayHasEntry = remember(transactions, today) {
        transactions.any { it.year == today.y && it.month == today.m && it.day == today.d }
    }

    // سنجشِ نشان‌ها فقط از اینجا (تبِ خانه = اولین صفحه‌ی بعدِ ورود) صدا زده می‌شه تا
    // بازشدنِ گذشته دقیقاً یه‌بار و صامت انجام بشه.
    LaunchedEffect(Unit) {
        gamificationViewModel.syncBadges()
    }
    val retroBadges by gamificationViewModel.retroUnlocked.collectAsState()
    var showNewTransaction by remember { mutableStateOf(false) }
    var showCoinWallet by remember { mutableStateOf(false) }
    // 🚨 **آدمک حالا مقصد دارد** (خواسته‌ی صریحِ کاربر: «رو آدمک می‌زنم، به جایی برود»).
    // فریمِ `55a` عمداً بی‌مقصدش کرده بود تا با چرخ‌دنده دو درِ یک اتاق نشوند - ولی مقصدِ
    // درست تنظیمات نبود: **نشان‌ها** جای طبیعیِ آدمک‌اند (همان‌جا که شخصی‌سازی و پیشرفتِ
    // شخصی نشان داده می‌شود). پس تنظیمات هنوز یک در دارد و آدمک درِ دیگری به اتاقِ دیگر.
    var showProfile by remember { mutableStateOf(false) }
    // «پرداخت شد» بازگشت‌ناپذیر است و روی کارتِ قهرمانِ خانه یک تپِ اشتباه راحت رخ می‌دهد.
    // ⚠️ این تایید یک‌بار اضافه شده بود و بسته‌ی بازطراحیِ خانه رویش را نوشت - اگر دوباره
    // فایل را از طراح گرفتید، همین‌جا را چک کنید.
    var confirmPayDue by remember { mutableStateOf<UrgentDueViewModel.UrgentRow?>(null) }

    // ⚠️ هر دو شیت قبلاً با `return` صدا زده می‌شدند و کلِ Box از کامپوزیشن بیرون می‌رفت:
    // پشتِ شیت سفیدِ خالی بود و اسکرولِ صفحه‌ی اول با بستنش صفر می‌شد. حالا **روی** صفحه
    // می‌نشینند، ته همان Box.
    BackHandler(enabled = showNewTransaction) { showNewTransaction = false }

    // خرجِ ۷ روزِ گذشته (قدیمی‌ترین → امروز) برای نمودارِ میله‌ایِ کارتِ قهرمان.
    val weekSpend = remember(transactions) {
        (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            transactions.filter { it.isExpenseOn(d.y, d.m, d.d) }.sumOf { it.amount }
        }
    }
    val weekTotal = remember(weekSpend) { weekSpend.sum() }
    val prevWeekTotal = remember(transactions) {
        (13 downTo 7).sumOf { back ->
            val d = PersianCalendar.addDays(today, -back)
            transactions.filter { it.isExpenseOn(d.y, d.m, d.d) }.sumOf { it.amount }
        }
    }
    val monthSpendByCategory = remember(transactions) {
        accountViewModel.spendByCategory(transactions, today.y, today.m)
    }
    val monthSpend = remember(monthSpendByCategory) { monthSpendByCategory.values.sum() }
    val monthCap = remember(budgets) { budgets.sumOf { it.monthlyCap } }

    // پیش‌بینیِ «تا آخرِ ماه کم میاری» - رجوع کن به MonthForecast تو :core.
    // موجودی = جمعِ موجودیِ همه‌ی حساب‌کتاب‌ها (همون تعریفی که تبِ دارایی نشون می‌ده).
    val accounts by accountViewModel.accounts.collectAsState()
    var showTodaySpend by rememberSaveable { mutableStateOf(false) }
    val monthForecast = remember(transactions, accounts, monthSpend) {
        MonthForecast.compute(
            spentSoFarRial = monthSpend,
            dayOfMonth = today.d,
            daysInMonth = JalaliCalendar.daysInMonth(today.y, today.m),
            balanceRial = accounts.sumOf { accountViewModel.balanceOf(it, transactions) },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // حاشیه‌ی صفحه ۱۶ طبقِ بندِ ۳ سیستمِ طراحی، فاصله‌ی بینِ کارت‌ها ۱۳ طبقِ خودِ فریم.
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                HomeHeader(
                    today = today,
                    userName = userName,
                    activeDays = activeDays,
                    onOpenCoins = { showCoinWallet = true },
                    onOpenProfile = { showProfile = true },
                    coins = coins,
                    onOpenSettings = onOpenSettings,
                    inboxCount = inboxCount,
                    inboxUnreadNews = inboxUnreadNews,
                    onOpenInbox = onOpenInbox,
                    todayHasEntry = todayHasEntry,
                )
            }

            // ── پیشنهادِ خودکارِ نوارِ پایین (فریمِ `41a`) ────────────────────────────────
            // «بالای صفحه‌ی خانه، **زیرِ هدر**. هرگز مودال نمی‌شود» - قاعده‌ی صریحِ `41c`.
            navSuggestionSlot?.let { slot -> item { slot() } }

            // ── ترمیمِ زنجیر: **از خانه برداشته شد** (تصمیمِ طراح، فریمِ `56b`) ─────────────
            // کارتش دو جا بود - این‌جا و کیفِ سکه. سندِ طراح صریح است که جایش کیفِ سکه
            // است: «آنچه نیست فقط صفحه‌ای برای دیده‌شدنشان است» و آن صفحه ساخته شد.
            //
            // 🚨 و همین کارت بود که کاربر با چهار اسکرین‌شات گزارشش کرد: بعدِ یک‌دو روز
            // غیبت، یک کارتِ سفید به بلندیِ کلِ صفحه بالای کارتِ قهرمان می‌نشست و اولین
            // چیزی که از اپ دیده می‌شد همان بود. علتش هیچ‌وقت پیدا نشد (فقط با
            // `heightIn` مهار شده بود)؛ با برداشتنش هم باگ می‌رود هم دوگانگی.
            //
            // درِ ورودی: قرصِ سکه‌ی هدرِ خانه → کیفِ سکه، که کارتِ ترمیم **بالای** ردیفِ
            // «فعال» در همان صفحه می‌نشیند.

            // ── اسکلتِ لودینگ ────────────────────────────────────────────────────────────
            // 🚨 تا اولین خواندنِ دیتابیس برنگشته، «خالی» نشان داده نمی‌شود: دیتابیس رمزنگاری‌شده
            // است و چند فریم طول می‌کشد، و در آن فاصله حالتِ خالی (کارتِ خط‌چینِ بلند) رندر
            // می‌شد و کاربر باید از رویش رد می‌شد تا محتوای واقعی را ببیند.
            if (!transactionsLoaded) {
                item { SkeletonRowList(rows = 4) }
            }

            // ── حالتِ خالی (فریمِ `15b`) - وقتی هنوز هیچ تراکنشی ثبت نشده ────────────────
            if (transactionsLoaded && transactions.isEmpty()) {
                item {
                    HomeEmptyHero(onAddFirst = { showNewTransaction = true })
                }
                item {
                    Text(
                        "یا از اینجا شروع کن",
                        color = AppMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StarterTile(
                            icon = Icons.Outlined.BarChart,
                            tint = AppInfo,
                            pill = AppInfoPill,
                            label = "ساختِ بودجه",
                            onClick = { onNavigateToRoute("budget") },
                        )
                        StarterTile(
                            icon = Icons.Outlined.CreditCard,
                            tint = AppDanger,
                            pill = AppDangerPill,
                            label = "افزودنِ چک",
                            onClick = { onNavigateToRoute("cheque") },
                        )
                        StarterTile(
                            icon = Icons.Filled.ArrowUpward,
                            tint = AppPrimary,
                            pill = AppPrimaryPill,
                            label = "ثبتِ وام",
                            onClick = { onNavigateToRoute("loan") },
                        )
                    }
                }
                item { FirstRewardNote() }
                return@LazyColumn
            }

            // ── حالتِ عادی (فریمِ `15a`) ───────────────────────────────────────────────
            item {
                TodaySpendHero(
                    todaySpend = weekSpend.last(),
                    yesterdaySpend = weekSpend[weekSpend.lastIndex - 1],
                    weekSpend = weekSpend,
                    privacyMode = privacyMode,
                    // `71d`: عددی که جلوی چشم است و لمس می‌شود ولی جواب نمی‌دهد یک بن‌بست
                    // است. مقصدش **شیت** است نه صفحه - یک نگاهِ دوثانیه‌ای، و زمینه
                    // (خودِ عددِ هیرو) بالای شیت می‌مانَد.
                    onClick = { showTodaySpend = true },
                )
            }
            if (monthCap > 0.0) {
                item {
                    MonthBudgetCard(
                        monthLabel = persianMonthName(today.m),
                        spent = monthSpend,
                        cap = monthCap,
                        dayOfMonth = today.d,
                        daysInMonth = JalaliCalendar.daysInMonth(today.y, today.m),
                        privacyMode = privacyMode,
                        onClick = { onNavigateToRoute("budget") },
                    )
                }
            }
            // «تا آخرِ ماه کم میاری» - رجوع کن به MonthForecast. عمداً **بالای** کارت‌های
            // تحلیلی و زیرِ بودجه می‌شینه: یه هشدارِ عملیه، نه یه آمار.
            monthForecast?.let { forecast ->
                item {
                    if (forecast.willRunShort) {
                        ShortfallForecastCard(
                            runsOutOnDay = forecast.runsOutOnDay,
                            daysLeft = forecast.daysLeft,
                            perDaySpend = forecast.perDayRial,
                            balance = forecast.balanceRial,
                            safePerDay = forecast.safePerDayRial,
                            privacyMode = privacyMode,
                            onClick = { onNavigateToRoute("report") },
                        )
                    } else {
                        // حالتِ سالم **کارت نمی‌گیرد** (فریمِ `63b`) - یک خطِ آرام بس است؛
                        // کارتِ «همه‌چیز خوب است» فضای کارتِ هشدار را می‌گیرد و بی‌اثر است.
                        Text(
                            "با این سرعت تا آخرِ ماه می‌رسی",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            urgentDue?.let { due ->
                item {
                    UrgentDueCard(
                        title = "قسطِ ${due.loan.name}",
                        amount = due.amount,
                        daysOverdue = due.daysOverdue,
                        privacyMode = privacyMode,
                        onPay = { confirmPayDue = due },
                        onOpen = { onNavigateToRoute("loan") },
                    )
                }
            }
            if (monthSpend > 0.0) {
                item {
                    CategoryBreakdownCard(
                        byCategory = monthSpendByCategory,
                        total = monthSpend,
                        privacyMode = privacyMode,
                        onClick = { onNavigateToRoute("report") },
                    )
                }
            }
            item {
                WeekReviewCard(
                    weekTotal = weekTotal,
                    prevWeekTotal = prevWeekTotal,
                    privacyMode = privacyMode,
                    onClick = { onNavigateToRoute("report") },
                )
            }
        }

        // فریمِ `15b` دکمه‌ی شناور نداره: تو حالتِ خالی اقدامِ اصلی همون دکمه‌ی تمام‌عرضِ
        // «ثبتِ اولین خرج»ه و دو تا دکمه‌ی هم‌کار گیج‌کننده‌ست.
        if (transactions.isNotEmpty()) AppFab(
            onClick = { showNewTransaction = true },
            contentDescription = "ثبتِ تراکنش",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
        )

        if (showNewTransaction) {
            NewTransactionSheet(onDismiss = { showNewTransaction = false })
        }
        // نشانِ تازه بازگشتِ سیستمی نمی‌گیرد: باید دیده و تأیید شود، وگرنه بی‌صدا رد
        // می‌شود و کاربر هیچ‌وقت نمی‌فهمد چه گرفته.
        if (retroBadges.isNotEmpty()) {
            BadgeRetroSheet(retroBadges) { gamificationViewModel.consumeRetro() }
        }
        // تپ روی قرصِ سکه کیفِ سکه را باز می‌کند (خواسته‌ی صریحِ کاربر). به‌صورتِ روکش
        // رندر می‌شود نه با `return` - همان باگی که شش جای دیگر صفحه را سفید می‌کرد.
        // تا وقتی سکه مقصدِ `NavHost` نشده، این کوتاه‌ترین راهِ درست است؛ روکش پس‌زمینه‌ی
        // مات دارد پس صفحه‌ی زیرش دیده نمی‌شود.
        if (showCoinWallet) {
            CoinHubScreen(onBack = { showCoinWallet = false }, todayHasEntry = todayHasEntry)
        }
        if (showProfile) {
            BackHandler { showProfile = false }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showProfile = false }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                    }
                    Text(
                        "نشان‌های من",
                        color = AppText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                    )
                }
                // ⚠️ `BadgesScreen` یک `Column` است نه فهرستِ تنبل، پس داخلِ اسکرولِ
                // عمودی امن است (قاعده‌ی کرشِ اسکرولِ تودرتو).
                BadgesScreen()
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
        if (showTodaySpend) {
            TodaySpendSheet(
                transactions = transactions,
                accountNameOf = { id -> accounts.firstOrNull { it.id == id }?.name },
                onDismiss = { showTodaySpend = false },
            )
        }
        confirmPayDue?.let { due ->
            ConfirmPayDialog(
                title = "تاییدِ پرداخت",
                text = "این قسط پرداخت‌شده علامت بخوره؟",
                onConfirm = { urgentDueViewModel.markPaid(due); confirmPayDue = null },
                onDismiss = { confirmPayDue = null },
            )
        }
    }
}

/**
 * شیتِ «خرجِ امروز» - فریمِ `71d`.
 *
 * **واریزهای امروز این‌جا نمی‌آیند**: عددِ هیرو «خرج» است نه تراز، و فهرستی که واریز هم
 * داشته باشد با آن عدد نمی‌خواند.
 *
 * ردیف همان ردیفِ `71a` است با یک چیزِ اضافه: **نامِ حساب پیشِ منبع** - خرجِ امروز چند
 * حساب را قطع می‌کند و بی نامِ حساب دو ردیفِ هم‌مبلغ از هم جدا نمی‌شوند. سرگروهِ روز هم
 * نیست، همه‌اش امروز است.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodaySpendSheet(
    transactions: List<AccountTransactionEntity>,
    accountNameOf: (Long) -> String?,
    onDismiss: () -> Unit,
) {
    val today = remember { JalaliCalendar.today() }
    val rows = remember(transactions, today) {
        transactions.filter { it.isExpenseOn(today.y, today.m, today.d) }
            .sortedByDescending { it.createdAt }
    }
    val total = remember(rows) { rows.sumOf { it.amount } }
    val privacyMode = LocalPrivacyMode.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppBg) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppHeroCard {
                Text("خرجِ امروز", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, rialToToman(total.toLong()).toFaMoney()) + " تومان",
                        color = Color.White,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (rows.isEmpty()) {
                Text(
                    "امروز هنوز خرجی ثبت نشده.",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                rows.forEach { tx ->
                    CompactTransactionRow(tx = tx, accountName = accountNameOf(tx.accountId))
                }
            }
        }
    }
}

/** خرجِ همون روز؟ (واریز خرج نیست.) */
private fun AccountTransactionEntity.isExpenseOn(y: Int, m: Int, d: Int): Boolean =
    type != "DEPOSIT" && year == y && month == m && day == d

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
/**
 * تاریخ + سلام + دو نشانه.
 *
 * ⚠️ **آدمکِ پروفایل تو این فریم نیست** و برداشته شد - کارتِ `32c` گفته بود «نوارِ بالای خانه
 * ۳۲px»، ولی خودِ فریمِ `15a` نشونش نمی‌ده و **تصویر بر متن مقدمه**. آدمک سرِ جاش تو
 * «حساب کاربری» می‌مونه.
 */
/** زیرِ این عرض هدر تنگ حساب می‌شود - گوشیِ ۳۶۰ منهای حاشیه‌ی ۱۶ی دو طرف. */
private val NARROW_HEADER_WIDTH = 330.dp

@Composable
private fun HomeHeader(
    today: ir.sadteam.loancalc.core.PersianDate,
    userName: String?,
    activeDays: Int,
    coins: Int,
    onOpenSettings: () -> Unit,
    inboxCount: Int,
    inboxUnreadNews: Int,
    onOpenInbox: () -> Unit,
    onOpenCoins: () -> Unit,
    onOpenProfile: () -> Unit,
    /** امروز تراکنشی ثبت شده یا نه - شرطِ برگشتنِ قرصِ «فعال» به هدر (فریمِ `55a`). */
    todayHasEntry: Boolean,
) {
    val avatarViewModel: AvatarViewModel = hiltViewModel()
    val avatar by avatarViewModel.avatar.collectAsState()
    val avatarFrame by avatarViewModel.frame.collectAsState()
    // آستانه‌ی حالتِ باریک که طراح نگذاشته بود چون عددش دستِ ماست: زیرِ این عرض، عددِ
    // سکه برداشته می‌شود و فقط خودِ سکه می‌مانَد (ترتیبِ `55b`: عددِ سکه ← تاریخ ← نام).
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val compactChips = maxWidth < NARROW_HEADER_WIDTH
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // ⚠️ **آدمک اینجاست، و کلیک‌پذیر نیست** - فریمِ `55a`.
        //
        // تا امروز آدمک و چرخ‌دنده **هر دو** `onOpenSettings` را صدا می‌زدند: دو در به یک
        // اتاق. علتش این بود که آدمک را *به‌جای* درِ تنظیمات گذاشته بودیم نه در کنارش، و
        // گزارشِ «دکمه‌ی تنظیمات اصلاً نیست» هم دقیقاً از همین آمد - کسی آدمکِ گوشه را
        // تنظیمات نمی‌شناسد.
        //
        // پس آدمک می‌مانَد (شخصی‌سازی است و پالتش به تمِ خریدنی وصل می‌شود) ولی **مقصد
        // ندارد**: کنارِ «سلام» می‌نشیند، جایی که همان جمله را تصویر می‌کند. چرخ‌دنده تنها
        // درِ تنظیمات است.
        //
        // ⚠️ **تنها استثنای قاعده‌ی ۴۴ در هدر، و عمدی**: جعبه‌ی ۴۴ نمی‌گیرد چون کنش نیست.
        // هدفِ لمسی برای چیزی که هیچ کاری نمی‌کند، تپ‌های اطرافش را می‌خورد. اندازه ۳۰ شد
        // نه ۳۲، تا کنارِ متن بنشیند و ارتفاعِ ردیف را بالا نبرد.
        // قابِ خریداری‌شده دورِ همین آواتار می‌نشیند - جایی که خرید نتیجه می‌دهد (`72a`).
        // ⚠️ قطرِ بیرونی همان ۳۰ می‌مانَد؛ خودِ آدمک کوچک‌تر می‌شود، پس ارتفاعِ ردیف
        // عوض نمی‌شود.
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenProfile,
                ),
            contentAlignment = Alignment.Center,
        ) {
            FramedAvatar(avatar = avatar, size = 30.dp, frame = avatarFrame)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
            // تاریخ **دومین چیزی است که در تنگنا می‌رود** (بعدِ عددِ سکه، قبلِ نام).
            // `Row`ِ بیرونی `SpaceBetween` است و ستون `weight(1f)` دارد، پس خودِ Compose
            // نام را کوتاه می‌کند؛ `maxLines`/`Ellipsis` اجباری است وگرنه نامِ بلند قرص‌ها
            // را از صفحه بیرون می‌راند.
            Text(
                "${(today.d).toFa()} ${persianMonthName(today.m)}",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (userName.isNullOrBlank()) "خوش آمدی" else "سلامْ $userName",
                color = AppText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            // ⚠️ **قرصِ «فعال» از هدر رفت** - فریمِ `55a`.
            //
            // شمارنده‌ی روزهای پیاپی است: کنش نیست، مقصد ندارد، و هر روز همان عدد
            // به‌علاوه‌ی یک را تکرار می‌کند. جایش صفحه‌ی سکه است (`45a`) کنارِ بقیه‌ی
            // بازی‌سازی، و درش همین قرصِ سکه‌ی کناری است.
            //
            // **تنها حالتی که برمی‌گردد**: رشته در خطرِ پاره‌شدن باشد - یعنی رشته‌ی هفت‌روزه
            // یا بیشتر روی میز است و امروز هنوز چیزی ثبت نشده. آن‌وقت خبر است، نه زینت.
            // و **جای** قرصِ سکه می‌نشیند نه کنارش، وگرنه همان ردیفِ شلوغ برمی‌گردد.
            //
            // ⚠️ `todayHasEntry` را باید فراخوان بدهد. در `HomeScreen` از همان داده‌ای
            // می‌آید که `notifyDailyExpenseReminder` استفاده می‌کند: «امروز تراکنشی ثبت
            // شده یا نه». امضایش را حدس نزدم.
            val streakAtRisk = activeDays >= 7 && !todayHasEntry
            if (streakAtRisk) {
                ActiveChip(days = activeDays, onClick = onOpenCoins)
            } else if (coins > 0) {
                CoinChip(coins = coins, onClick = onOpenCoins, compact = compactChips)
            }
            // زنگِ مرکزِ پیام‌ها (بخشِ ۴۰). **عدد فقط برای اقدام‌دارهای بازه**؛ خبرِ
            // خوانده‌نشده فقط یه نقطه‌ی سبز می‌گیره، نه عدد (قاعده‌ی صریحِ طرح).
            InboxBell(
                count = inboxCount,
                hasUnreadNews = inboxUnreadNews > 0,
                onClick = onOpenInbox,
            )
            // ⚠️ **تنها درِ تنظیمات.** آدمک دیگر این کار را نمی‌کند.
            PrivacyEyeButton(icon = Icons.Filled.Settings, active = false, onClick = onOpenSettings)
        }
    }
    }
}


/**
 * دکمه‌ی ۳۲×۳۲ی هدر - فریمِ `37b`. برای حالتِ خصوصی و چرخ‌دنده‌ی تنظیمات یه شکلِ واحد.
 *
 * خاموش: زمینه‌ی `AppIconFrame`، حاشیه‌ی ۱٫۵ `AppLine`، جوهرِ `AppMuted`.
 * روشن: زمینه‌ی `AppWarningPill`، حاشیه‌ی ۱٫۵ `AppAssetBorder`، جوهرِ `AppWarningInk`.
 */
@Composable
fun PrivacyEyeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    Box(
        modifier = Modifier.size(AppSpacing.minTouchTarget).pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (active) AppWarningPill else AppIconFrame)
                .border(
                    1.5.dp,
                    if (active) AppAssetBorder else AppLine,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (active) AppWarningInk else AppMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ═══ ۲ · کارتِ سبزِ خرجِ امروز ═══════════════════════════════════════════════════
@Composable
private fun TodaySpendHero(
    todaySpend: Double,
    yesterdaySpend: Double,
    weekSpend: List<Double>,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val shown = countUpAmount(todaySpend, enabled = !privacyMode)
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
                        // fmt() جداکننده‌ی لاتین می‌داد و عدد ریال بود.
                        maskIfPrivate(masked, shown.rialToFaCompact()),
                        color = Color.White,
                        // ⚠️ **۲۶/۹۰۰ با letterSpacing منفی**، نه ۲۸. جدولِ تایپوگرافی دو
                        // ردیفِ جدا داره: «عددِ قهرمان» ۲۸ (بقیه‌ی تب‌ها) و «عددِ کارتِ
                        // سبزِ خانه» ۲۶ - و همین یکی مالِ این کارته.
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (deltaPercent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(HeroPillBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Icon(
                        if (deltaPercent < 0) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(
                        (if (deltaPercent < 0) "${(-deltaPercent).toFa()}٪ کمتر" else "${(deltaPercent).toFa()}٪ بیشتر") +
                            " از دیروز",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
        HomeSevenDayChart(values = weekSpend, modifier = Modifier.padding(top = 14.dp))
        HomeSevenDayChartLabels()
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
private fun MonthBudgetCard(
    monthLabel: String,
    spent: Double,
    cap: Double,
    dayOfMonth: Int,
    daysInMonth: Int,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val ratio = (spent / cap).toFloat().coerceIn(0f, 1f)
    val percent = (ratio * 100).toInt()
    val projected = if (dayOfMonth > 0) spent / dayOfMonth * daysInMonth else 0.0
    val leftover = cap - projected

    AppCard(contentPadding = 15.dp, modifier = Modifier.pressScaleClickable(onClick = onClick)) {
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
        BudgetBarWithCoin(ratio = ratio, modifier = Modifier.padding(top = 9.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp),
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
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(listOf(AppPrimary, BarGradientEnd))),
        )
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

// ═══ ۴ · قسطِ سررسیدشده ════════════════════════════════════════════════════════
@Composable
private fun UrgentDueCard(
    title: String,
    amount: Double,
    daysOverdue: Int,
    privacyMode: Boolean,
    onPay: () -> Unit,
    onOpen: () -> Unit,
) {
    // ⚠️ **بی‌سایه** - این کارت درست زیرِ کارتِ قهرمان می‌شینه و دو سایه‌ی سختِ پشتِ‌هم
    // شلوغ می‌شه (قاعده‌ی صریحِ طراح برای همین فریم؛ تو فریم‌های دیگه سایه داره).
    AppCard(
        variant = AppCardVariant.URGENT,
        shadow = false,
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
                    // قابِ آیکون مقدارِ محلیِ خودشه (#FFECEC)، نه `AppDangerPill` که
                    // روشن‌تره (#FFF5F5) - همون تفکیکی که طراح تو فایلِ آدمک هم تاکید کرد.
                    .background(UrgentIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = AppDanger, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        (if (daysOverdue == 0) "امروز سررسید" else "${(daysOverdue).toFa()} روز عقب") +
                            " — " + maskIfPrivate(masked, amount.rialToFaCompact()),
                        color = AppDangerInk,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            GradientButton(onClick = onPay, variant = AppButtonVariant.IN_ROW) { Text("پرداخت شد") }
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
private fun ShortfallForecastCard(
    runsOutOnDay: Int,
    daysLeft: Int,
    perDaySpend: Double,
    balance: Double,
    safePerDay: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    AppCard(
        variant = AppCardVariant.GOLD,
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
                    .background(AppGoldPillSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = AppGoldInk, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "با این سرعت، ${runsOutOnDay.toFa()} روز قبلِ آخرِ ماه تمام می‌شود",
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Column {
                        Text(
                            "روزی ${maskIfPrivate(masked, perDaySpend.rialToFaCompact())} خرج کرده‌ای و " +
                                "${maskIfPrivate(masked, balance.rialToFaCompact())} مانده برای ${daysLeft.toFa()} روز.",
                            color = AppGoldInk,
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
private fun CategoryBreakdownCard(
    byCategory: Map<String, Double>,
    total: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val top = remember(byCategory) { byCategory.entries.sortedByDescending { it.value }.take(3) }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    val (centerNumber, centerUnit) = total.rialToFaCompactParts()
    AppCard(contentPadding = 14.dp, modifier = Modifier.pressScaleClickable(onClick = onClick)) {
      Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        // «این ماه» از داخلِ دایره بیرون آمد: در ۷sp تقریباً خوانده نمی‌شد و جای واحد را
        // می‌گرفت. اینجا برچسبِ کارت است، جایی که برچسبِ کارت باید باشد.
        Text("این ماه", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CategoryDonut(
                slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
                size = 74.dp,
                strokeWidth = 13.dp,
            ) {
                // ⚠️ سه چیز روی هم افتاده بود: عدد **ریال** بود (۱۰۲۶٫۶M جای ۱۰۲٫۶)، حرفِ
                // M لاتین وسطِ ارقامِ فارسی، و یک خطِ بلند در دایره‌ی تنگ. قطرِ داخلی
                // ۷۴ − ۲×۱۳ = ۴۸dp است و آن خط در ۱۱sp حدودِ ۵۲dp عرض می‌گرفت، پس به
                // لبه‌ی رینگ می‌چسبید. دو خطِ کوتاه حالا ~۳۰dp مصرف می‌کند.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, centerNumber),
                            color = AppText,
                            fontSize = 13.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                    }
                    Text(
                        centerUnit,
                        color = AppLabel,
                        fontSize = 7.5.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
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
                            // total صفر → NaN٪. کارت با جمعِ صفر نمی‌آید، ولی نگهبانش یک خط است.
                            if (total <= 0.0) "—"
                            else "${((entry.value / total * 100).toInt()).toFa()}٪",
                            color = AppMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }
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

/** انتهای گرادیانِ نوارِ سبزِ بودجه - مقدارِ محلیِ فریم، توکن نیست. */
private val BarGradientEnd: Color
    @Composable get() = AppPrimaryInkLight

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
        Text(label, color = AppLabel, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
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
private fun HomeEmptyHero(onAddFirst: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .dashedCardBorder()
            .padding(horizontal = 16.dp, vertical = 22.dp),
    ) {
        JibakMascotFrame()
        Text(
            "کیفت خالیه",
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "اولین خرجت رو ثبت کن. ده ثانیه وقت می‌بره و از فردا نمودارت شکل می‌گیره.",
            color = AppMuted,
            fontSize = 11.5.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            // ⚠️ `max-width:230px`ِ صریحِ فریم. بدونش رو گوشیِ واقعی (که از قابِ ۳۳۶ پیکسلیِ
            // ماک‌آپ عریض‌تره) خط‌ها دراز می‌شن و صفحه «پهن» دیده می‌شه - گزارشِ کاربر.
            modifier = Modifier.widthIn(max = 230.dp).padding(top = 5.dp),
        )
        GradientButton(
            onClick = onAddFirst,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("ثبتِ اولین خرج")
        }
    }
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
private fun FirstRewardNote() {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(RewardNoteBg)
            .border(1.5.dp, RewardNoteBorder, shape)
            .padding(horizontal = 15.dp, vertical = 13.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = RewardNoteInk,
            modifier = Modifier.size(12.dp),
        )
        Text(
            "با اولین ثبت ۱۰ سکه می‌گیری و روزهای فعالت روشن می‌شه",
            color = RewardNoteInk,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp,
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
private fun RowScope.StarterTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    pill: Color,
    label: String,
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
            .padding(horizontal = 8.dp, vertical = 13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        }
        Text(
            label,
            color = AppText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 7.dp),
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

/**
 * زنگِ مرکزِ پیام‌ها - فریمِ `40a`.
 *
 * ⚠️ **عدد و نقطه دو چیزِ متفاوتن** (قاعده‌ی صریحِ طرح): عددِ روی زنگ فقط شمارِ «اقدام‌دارهای
 * باز»ه (تراکنشِ منتظرِ تایید، سررسیدِ وام)؛ خبرِ خوانده‌نشده هرگز عدد نمی‌گیره، فقط نقطه‌ی سبز.
 * دلیلش اینه که عدد یعنی «کاری با توئه»، نه «چیزی برای خوندن هست».
 */
@Composable
private fun InboxBell(count: Int, hasUnreadNews: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(AppSpacing.minTouchTarget)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.NotificationsNone,
            contentDescription = "پیام‌ها",
            tint = AppText,
            modifier = Modifier.size(20.dp),
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppDanger)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text((count).toFa(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        } else if (hasUnreadNews) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(AppPrimary),
            )
        }
    }
}
