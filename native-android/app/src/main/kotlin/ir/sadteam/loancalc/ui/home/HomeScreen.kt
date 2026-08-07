package ir.sadteam.loancalc.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.TodayCard
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.note.NoteViewModel
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppGlassBase
import ir.sadteam.loancalc.ui.theme.AppGlassBorder
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText

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
) {
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by accountViewModel.transactions.collectAsState()
    val privacyMode = LocalPrivacyMode.current

    val totalBalance = remember(accounts, transactions) {
        accounts.sumOf { accountViewModel.balanceOf(it, transactions) }
    }
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

    var selectedDate by remember { mutableStateOf(JalaliCalendar.today()) }
    var showAddNote by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }

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
            item {
                // کارتِ بزرگِ گرادیانیِ موجودی - بروزرسانیِ ظاهرِ کلیِ اپ (خواسته‌ی صریحِ کاربر:
                // «کل برنامه مدرن‌تر بشه»). قبلاً فقط یه متنِ ساده‌ی معلق رو پس‌زمینه‌ی Aurora بود؛
                // الان یه کارتِ شیشه‌ای با گرادیانِ رنگِ اصلیِ اپ، شبیهِ کارتِ بانکی.
                val balanceGradient = Brush.linearGradient(
                    listOf(AppPrimary.copy(alpha = 0.40f), AppPrimaryDim.copy(alpha = 0.18f)),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppGlassBase)
                        .background(balanceGradient)
                        .border(1.dp, AppGlassBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("مانده‌ی کل", color = AppMuted, fontSize = 13.sp)
                        Box(
                            modifier = Modifier.size(34.dp).background(AppPrimary.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "${maskIfPrivate(masked, fmt(totalBalance))} ریال",
                            color = if (totalBalance < 0) AppDanger else AppText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                    Text(
                        "${toFa(accounts.size)} حساب‌کتاب",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            item {
                // نمودارِ روندِ ۶ماهه‌ی درآمد/هزینه + خلاصه‌ی ماهِ جاری - هم‌الگو با عکسِ مرجعِ کاربر
                // («نمودارِ جریانِ مالی» بالای تبِ خانه). از رو همون AccountViewModel.monthlyTotals
                // (که قبلاً برای بودجه استفاده می‌شد) برای هر ماه حساب می‌شه.
                val trend = remember(transactions) {
                    (5 downTo 0).map { offset ->
                        val d = PersianCalendar.addMonths(JalaliCalendar.today(), -offset)
                        val (inc, exp) = accountViewModel.monthlyTotals(transactions, d.y, d.m)
                        Triple(d.m, inc, exp)
                    }
                }
                val curIncome = trend.last().second
                val curExpense = trend.last().third
                val maxTrendValue = (trend.maxOfOrNull { maxOf(it.second, it.third) } ?: 0.0).coerceAtLeast(1.0)
                // رنگ‌ها باید بیرونِ Canvas خونده بشن - AppPrimary/AppDanger پراپرتیِ @Composable ان
                // (get() تم‌آگاه)، و بلوکِ رسمِ Canvas یه DrawScope معمولی‌ه نه @Composable، پس
                // مستقیم صداکردنشون اونجا خطای کامپایل می‌ده.
                val incomeLineColor = AppPrimary
                val expenseLineColor = AppDanger
                AppCard(label = "نمودارِ جریانِ مالی") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("درآمدِ این ماه", color = AppMuted, fontSize = 12.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(curIncome))} ریال",
                                    color = AppPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("هزینه‌ی این ماه", color = AppMuted, fontSize = 12.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(curExpense))} ریال",
                                    color = AppDanger,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(top = 14.dp),
                    ) {
                        if (trend.size < 2) return@Canvas
                        val stepX = size.width / (trend.size - 1)
                        fun pointsFor(selector: (Triple<Int, Double, Double>) -> Double): List<Offset> =
                            trend.mapIndexed { index, t ->
                                val value = selector(t)
                                val y = size.height - (value / maxTrendValue).toFloat() * size.height
                                Offset(index * stepX, y.coerceIn(0f, size.height))
                            }
                        fun drawTrendLine(points: List<Offset>, color: Color) {
                            val path = Path()
                            points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
                            drawPath(path, color = color, style = Stroke(width = 5f, cap = StrokeCap.Round))
                        }
                        drawTrendLine(pointsFor { it.second }, incomeLineColor)
                        drawTrendLine(pointsFor { it.third }, expenseLineColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        trend.forEach { (m, _, _) ->
                            Text(persianMonthName(m).take(4), color = AppMuted, fontSize = 9.sp)
                        }
                    }
                }
            }
            item {
                // شبکه‌ی «دسترسیِ سریع» - هم‌الگو با عکسِ مرجعِ کاربر (۴ دکمه: تراکنشِ جدید، گزارش‌ها،
                // دسته‌بندی‌ها، حساب‌ها). هرکدوم به نزدیک‌ترین تبی که همون قابلیت رو داره می‌بره.
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessButton(
                        icon = Icons.Filled.Add,
                        label = "تراکنشِ جدید",
                        tint = AppPrimary,
                        onClick = { onNavigateToRoute("assets") },
                        modifier = Modifier.weight(1f),
                    )
                    QuickAccessButton(
                        icon = Icons.Filled.BarChart,
                        label = "گزارش‌ها",
                        tint = AppText,
                        onClick = { onNavigateToRoute("report") },
                        modifier = Modifier.weight(1f),
                    )
                    QuickAccessButton(
                        icon = Icons.Filled.Category,
                        label = "دسته‌بندی‌ها",
                        tint = AppText,
                        onClick = { onNavigateToRoute("budget") },
                        modifier = Modifier.weight(1f),
                    )
                    QuickAccessButton(
                        icon = Icons.Filled.AccountBalanceWallet,
                        label = "حساب‌ها",
                        tint = AppText,
                        onClick = { onNavigateToRoute("assets") },
                        modifier = Modifier.weight(1f),
                    )
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
                    onAddNote = { showAddNote = true },
                )
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
        FloatingActionButton(
            onClick = { onNavigateToRoute("assets") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AppPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "افزودنِ تراکنش")
        }
    }
}

/** یه دکمه‌ی مربعیِ «دسترسیِ سریع» - آیکونِ توی دایره‌ی رنگیِ ملایم + لیبل زیرش، هم‌الگو با
 * BudgetRow/RecentTransactionRow (رجوع کن به CLAUDE.md، «مدرن‌ترکردنِ ظاهرِ کلیِ اپ»). */
@Composable
private fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppGlassBase)
            .border(1.dp, AppGlassBorder, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(tint.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(
            label,
            color = AppText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (category != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(category.color.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(16.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(category?.name ?: (if (isIncome) "واریز" else "برداشت"), color = AppText, fontSize = 13.sp)
                Text(
                    "$accountName — ${toFa(tx.day)}/${toFa(tx.month)}/${toFa(tx.year)}",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "${if (isIncome) "+" else "-"}${maskIfPrivate(masked, fmt(tx.amount))}",
                color = if (isIncome) AppPrimary else AppDanger,
                fontSize = 13.sp,
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

