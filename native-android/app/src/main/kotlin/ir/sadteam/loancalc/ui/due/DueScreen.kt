package ir.sadteam.loancalc.ui.due

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.TodayCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.cheque.ChequeViewModel
import ir.sadteam.loancalc.ui.debt.DebtViewModel
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.note.NoteViewModel
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.note.NoteScreen
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion

private enum class DueSubView { NONE, DEBT, NOTES }

private data class DueShortcut(
    val title: String,
    val icon: ImageVector,
    // برای «تراکنش»/«قسط و وام»/«چک»/«حساب‌کتاب» که به تبِ دیگه‌ای می‌رن؛ null یعنی این کاشی
    // زیرصفحه‌ی داخلیِ خودِ همین تبه (رجوع کن به [subView]).
    val route: String?,
    val subView: DueSubView?,
    /** بجِ عددیِ گوشه‌ی کاشی (خواسته‌ی صریحِ کاربر: «اینطوری عدد بیاد بغلِ اینایی که وارد کردم»).
     * صفر یعنی هیچ بجی نشون داده نمی‌شه - وگرنه یه «۰» رو هر کاشیِ خالی فقط شلوغی می‌کرد. */
    val badge: Int = 0,
)

/**
 * تبِ «سررسید» - گریدِ میان‌برِ کاشی‌ای، هم‌الگو با صفحه‌ی مرجع. تراکنش/قسط‌ووام/چک/حساب‌کتاب به
 * بخش‌های موجودِ اپ (وامِ ادغام‌شده/چک/حسابداری) وصل می‌شن؛ طلب‌وبدهی و یادداشتِ مستقل زیرصفحه‌ی
 * داخلیِ خودِ همین تبن (رجوع کن به [DueSubView]) - هم‌الگو با ChequeScreen.screenKey.
 */
@Composable
fun DueScreen(
    onNavigateToRoute: (String) -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
    noteViewModel: NoteViewModel = hiltViewModel(),
    debtViewModel: DebtViewModel = hiltViewModel(),
    chequeViewModel: ChequeViewModel = hiltViewModel(),
    myLoansViewModel: MyLoansViewModel = hiltViewModel(),
) {
    var subView by rememberSaveable { mutableStateOf(DueSubView.NONE) }
    // برگشتن از طلب‌وبدهی/یادداشت به گریدِ اصلی - قبل از رسیدن به BackHandlerِ بیرونیِ LoanCalcApp
    // (که دیگه معنیش خروج/برگشتنِ بینِ تب‌هاست)، هم‌الگو با LoanTab.
    BackHandler(enabled = subView != DueSubView.NONE) { subView = DueSubView.NONE }
    // ترتیب دقیقاً هم‌چیدمانِ رفرنس (خواسته‌ی صریحِ کاربر: «دقیقا مثل هم بشند») - تو RTL آیتمِ اولِ
    // لیست سمتِ راستِ ردیفِ اول می‌شینه، پس ردیفِ اول از راست: تراکنش/یادداشت/طلب‌وبدهی و ردیفِ دوم
    // از راست: حساب‌کتاب/چک/قسط‌ووام. «پرداختِ تکراری» (فیچرِ اضافه‌ی خودِ ما، تو رفرنس نبود) از
    // اینجا به تبِ «بودجه» منتقل شد - رجوع کن به CLAUDE.md، «تصمیمِ کاشیِ پرداختِ تکراری» - چون
    // مفهوماً یه هزینه‌ی برنامه‌ریزی‌شده‌ی ماهانه‌ست (به بودجه نزدیک‌تره)، و اینجا هم گرید دوباره
    // دقیقاً ۶ کاشی/۲ ردیفِ کاملِ هم‌شکلِ رفرنس شد (بدونِ کاشیِ تنهای ردیفِ سوم).
    // عددهای واقعیِ بجِ کاشی‌ها - از همون ViewModelهای موجود خونده می‌شن، نه یه شمارنده‌ی جدا.
    val accounts by accountViewModel.accounts.collectAsState()
    val notes by noteViewModel.notes.collectAsState()
    val debts by debtViewModel.debts.collectAsState()
    val cheques by chequeViewModel.cheques.collectAsState()
    val loans by myLoansViewModel.loans.collectAsState()

    // ── کارتِ سبزِ «مجموعِ سررسیدهای معلق» (کارتِ `3a`ی طرح) ────────────────────────
    // معلق = چکِ در جریان (PENDING که بایگانی نشده) + طلب/بدهیِ تسویه‌نشده. عددها از همون
    // ViewModelهای موجود میان، نه یه منبعِ جدید.
    val pendingCheques = remember(cheques) { cheques.filter { it.status == "PENDING" && !it.archived } }
    val openDebts = remember(debts) { debts.filter { !it.settled } }
    val pendingCount = pendingCheques.size + openDebts.size
    val pendingAmount = remember(pendingCheques, openDebts) {
        pendingCheques.sumOf { it.amount } + openDebts.sumOf { it.amount }
    }

    val shortcuts = remember(accounts, notes, debts, cheques, loans) {
        listOf(
            DueShortcut("تراکنش", Icons.Filled.SwapHoriz, "assets", null),
            DueShortcut("یادداشت", Icons.Filled.EditNote, null, DueSubView.NOTES, notes.size),
            DueShortcut("طلب و بدهی", Icons.Filled.Handshake, null, DueSubView.DEBT, debts.size),
            DueShortcut("حساب‌کتاب", Icons.Filled.AccountBalanceWallet, "assets", null, accounts.size),
            DueShortcut("چک", Icons.Filled.ReceiptLong, "cheque", null, cheques.size),
            DueShortcut("قسط و وام", Icons.Filled.Payments, "loan", null, loans.size),
        )
    }

    AnimatedContent(
        targetState = subView,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "dueScreen",
    ) { current ->
        when (current) {
            DueSubView.DEBT -> DebtScreen(onBack = { subView = DueSubView.NONE })
            DueSubView.NOTES -> NoteScreen(onBack = { subView = DueSubView.NONE })
            DueSubView.NONE -> DueGrid(
                shortcuts = shortcuts,
                pendingCount = pendingCount,
                pendingAmount = pendingAmount,
                onNavigateToRoute = onNavigateToRoute,
                onOpenSubView = { subView = it },
            )
        }
    }
}

@Composable
private fun DueGrid(
    shortcuts: List<DueShortcut>,
    pendingCount: Int,
    pendingAmount: Double,
    onNavigateToRoute: (String) -> Unit,
    onOpenSubView: (DueSubView) -> Unit,
) {
    // کارتِ «امروز» (ناوبرِ روز + میان‌برِ سریعِ قسط/چک/یادداشت) - خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ
    // رفرنس: تبِ «سررسید» هم دقیقاً همینو زیرِ کاشی‌های میان‌بر داشته باشه، عینِ تبِ «خانه» (هر دو از
    // همون TodayCardِ مشترک تو ui/components استفاده می‌کنن).
    var selectedDate by remember { mutableStateOf(JalaliCalendar.today()) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    if (showCalendarPicker) {
        CalendarPickerScreen(
            initialDate = selectedDate,
            onDateSelected = { date -> selectedDate = date; showCalendarPicker = false },
            onBack = { showCalendarPicker = false },
        )
        return
    }

    // بازطراحی: فضای خالیِ زیرِ گرید با بزرگ‌کردنِ خودِ محتوا (کاشی‌های بلندتر + Spacerِ وزن‌دار قبل از
    // کارتِ «امروز») پر شد، نه محتوای تازه - پس دیگه LazyColumn لازم نیست، محتوا هیچ‌وقت از صفحه
    // بیشتر نمی‌شه.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp, 12.dp, 14.dp, 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // کارتِ سبزِ «مجموعِ سررسیدهای معلق» - کارتِ `3a`ی فایلِ طراحی. بالای گریدِ میان‌برها
        // می‌شینه، با تعدادِ موردهای معلق تو یه قرصِ سفیدِ نیمه‌شفاف.
        if (pendingCount > 0) {
            AppHeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HeroPillBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            toFa(pendingCount),
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Column {
                        Text(
                            "مجموعِ سررسیدهای معلق",
                            color = HeroMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                            Text(
                                fmt(pendingAmount),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                " ریال",
                                color = HeroMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp, bottom = 1.dp),
                            )
                        }
                    }
                }
            }
        }

        // عمداً LazyVerticalGrid نیست: یه گریدِ تنبل تویِ یه ستونِ دیگه نمی‌تونه wrap-content باشه.
        // با چیدنِ دستیِ ردیف‌ها (chunked(3))، ارتفاع دقیقاً به‌اندازه‌ی خودِ کاشی‌هاست.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shortcuts.chunked(3).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { shortcut ->
                        DueShortcutTile(
                            shortcut = shortcut,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                shortcut.subView?.let(onOpenSubView)
                                shortcut.route?.let(onNavigateToRoute)
                            },
                        )
                    }
                    // جای خالیِ ردیفِ ناقصِ آخر، وگرنه کاشیِ تنها کلِ عرض رو می‌گیره.
                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        TodayCard(
            date = selectedDate,
            onPrevDay = { selectedDate = PersianCalendar.addDays(selectedDate, -1) },
            onNextDay = { selectedDate = PersianCalendar.addDays(selectedDate, 1) },
            onDateClick = { showCalendarPicker = true },
            onAddInstallment = { onNavigateToRoute("loan") },
            onAddCheque = { onNavigateToRoute("cheque") },
            onAddNote = { onOpenSubView(DueSubView.NOTES) },
        )
    }
}

/**
 * کاشیِ میان‌بر - آیکون حالا داخلِ یه مربعِ رنگیِ گردگوشه‌ست (نه دیگه وسط‌چینِ لختِ رو کارت)، با
 * نوشته‌ی زیرش. نسبتِ ابعاد ۱.۰۵ (تقریباً مربع) - فضای خالیِ زیرِ گریدِ سررسید با بزرگ‌کردنِ خودِ
 * کاشی‌ها پر شد، نه با کارتِ اضافه (رجوع کن به کامنتِ [DueGrid]).
 */
@Composable
private fun DueShortcutTile(shortcut: DueShortcut, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AppCard(
        modifier = modifier
            .aspectRatio(1.05f)
            .pressScaleClickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // قابِ آیکون: گوشه‌ی ۱۲ طبقِ توکنِ «۱۲ آیکون»ِ سیستمِ طراحی (قبلاً ۱۶ بود که
                // توکنِ ردیفِ فهرسته، نه قابِ آیکون).
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        shortcut.icon,
                        contentDescription = shortcut.title,
                        tint = AppPrimaryInk,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    shortcut.title,
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            // بجِ عددی - گوشه‌ی بالا-چپ (تو RTL یعنی سمتِ «شروع»ِ بصری، همون‌جایی که رفرنس داره).
            // پدینگ/فونتِ بزرگ‌تر از قبل - خواناتر رو پس‌زمینه‌ی کم‌کنتراست.
            if (shortcut.badge > 0) {
                Text(
                    toFa(shortcut.badge),
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(AppPrimary, CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
    }
}
