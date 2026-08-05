package ir.sadteam.loancalc.ui.due

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.ui.accounting.RecurringPaymentsScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.TodayCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.note.NoteScreen
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion

private enum class DueSubView { NONE, DEBT, NOTES, RECURRING }

private data class DueShortcut(
    val title: String,
    val icon: ImageVector,
    // برای «تراکنش»/«قسط و وام»/«چک»/«حساب‌کتاب» که به تبِ دیگه‌ای می‌رن؛ null یعنی این کاشی
    // زیرصفحه‌ی داخلیِ خودِ همین تبه (رجوع کن به [subView]).
    val route: String?,
    val subView: DueSubView?,
)

/**
 * تبِ «سررسید» - گریدِ میان‌برِ کاشی‌ای، هم‌الگو با صفحه‌ی مرجع. تراکنش/قسط‌ووام/چک/حساب‌کتاب به
 * بخش‌های موجودِ اپ (وامِ ادغام‌شده/چک/حسابداری) وصل می‌شن؛ طلب‌وبدهی و یادداشتِ مستقل زیرصفحه‌ی
 * داخلیِ خودِ همین تبن (رجوع کن به [DueSubView]) - هم‌الگو با ChequeScreen.screenKey.
 */
@Composable
fun DueScreen(onNavigateToRoute: (String) -> Unit) {
    var subView by rememberSaveable { mutableStateOf(DueSubView.NONE) }
    // برگشتن از طلب‌وبدهی/یادداشت به گریدِ اصلی - قبل از رسیدن به BackHandlerِ بیرونیِ LoanCalcApp
    // (که دیگه معنیش خروج/برگشتنِ بینِ تب‌هاست)، هم‌الگو با LoanTab.
    BackHandler(enabled = subView != DueSubView.NONE) { subView = DueSubView.NONE }
    // ترتیب دقیقاً هم‌چیدمانِ رفرنس (خواسته‌ی صریحِ کاربر: «دقیقا مثل هم بشند») - تو RTL آیتمِ اولِ
    // لیست سمتِ راستِ ردیفِ اول می‌شینه، پس ردیفِ اول از راست: تراکنش/یادداشت/طلب‌وبدهی و ردیفِ دوم
    // از راست: حساب‌کتاب/چک/قسط‌ووام. «پرداختِ تکراری» تو رفرنس نیست (فیچرِ اضافه‌ی خودِ ما)، برای
    // همین آخر می‌شینه.
    val shortcuts = remember {
        listOf(
            DueShortcut("تراکنش", Icons.Filled.SwapHoriz, "assets", null),
            DueShortcut("یادداشت", Icons.Filled.EditNote, null, DueSubView.NOTES),
            DueShortcut("طلب و بدهی", Icons.Filled.Handshake, null, DueSubView.DEBT),
            DueShortcut("حساب‌کتاب", Icons.Filled.AccountBalanceWallet, "assets", null),
            DueShortcut("چک", Icons.Filled.ReceiptLong, "cheque", null),
            DueShortcut("قسط و وام", Icons.Filled.Payments, "loan", null),
            DueShortcut("پرداختِ تکراری", Icons.Filled.Repeat, null, DueSubView.RECURRING),
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
            DueSubView.RECURRING -> RecurringPaymentsScreen(onBack = { subView = DueSubView.NONE })
            DueSubView.NONE -> DueGrid(
                shortcuts = shortcuts,
                onNavigateToRoute = onNavigateToRoute,
                onOpenSubView = { subView = it },
            )
        }
    }
}

@Composable
private fun DueGrid(
    shortcuts: List<DueShortcut>,
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // کارتِ توضیحیِ قبلی («سررسید: میان‌برِ سریع به...») به‌خواستِ صریحِ کاربر حذف شد - رفرنس
        // (Poolaki) هیچ کارتی قبلِ گریدِ کاشی‌ها نداره، مستقیم از زیرِ نوارِ بالا شروع می‌شه.
        item {
            // عمداً LazyVerticalGrid نیست: یه گریدِ تنبل تویِ یه LazyColumnِ دیگه نمی‌تونه
            // wrap-content باشه و باید ارتفاعِ ثابت بگیره - همون ارتفاعِ ثابت بود که یه فاصله‌ی
            // خالیِ بزرگ بینِ کاشی‌ها و کارتِ «امروز» می‌انداخت (گزارشِ کاربر با اسکرین‌شات). با
            // چیدنِ دستیِ ردیف‌ها (chunked(3))، ارتفاع دقیقاً به‌اندازه‌ی خودِ کاشی‌هاست.
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
        }
        item {
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
}

/**
 * کاشیِ میان‌بر - هم‌شکلِ رفرنس: آیکونِ ساده‌ی وسط‌چین (نه تویِ دایره‌ی رنگی) با نوشته‌ی زیرش. نسبتِ
 * ابعاد عمداً از ۱.۳۵ کمتر شد (کاشی کمی بلندتر): با پدینگِ ۱۴dpی خودِ [AppCard]، ارتفاعِ قبلی
 * جا برای آیکون + نوشته نداشت و نوشته‌ی هر کاشی از پایین بریده/نامرئی می‌شد (گزارشِ کاربر با
 * اسکرین‌شات).
 */
@Composable
private fun DueShortcutTile(shortcut: DueShortcut, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AppCard(
        modifier = modifier
            .aspectRatio(1.28f)
            .pressScaleClickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                shortcut.icon,
                contentDescription = shortcut.title,
                tint = AppPrimary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                shortcut.title,
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
