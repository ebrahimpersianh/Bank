package ir.sadteam.loancalc.ui.myloans

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** پورت لیبل «فیلتر» بالا-چپِ لیست وام‌های رقیب (VAMMAN) - فقط مرتب‌سازی محلی لیست، بدون تغییر
 * داده؛ پیش‌فرض «جدیدترین» (همون ترتیب قبلی createdAt نزولی که قبلاً بدون این کنترل هم اعمال می‌شد). */
private enum class LoanSortOption(val label: String) {
    NEWEST("جدیدترین"),
    OLDEST("قدیمی‌ترین"),
    NAME("نام (الفبا)"),
    AMOUNT_DESC("بیشترین مبلغ"),
    PROGRESS_DESC("بیشترین پیشرفت پرداخت"),
}

private fun List<LoanEntity>.sortedByOption(option: LoanSortOption): List<LoanEntity> = when (option) {
    LoanSortOption.NEWEST -> sortedByDescending { it.createdAt }
    LoanSortOption.OLDEST -> sortedBy { it.createdAt }
    LoanSortOption.NAME -> sortedBy { it.name }
    LoanSortOption.AMOUNT_DESC -> sortedByDescending { it.amount }
    LoanSortOption.PROGRESS_DESC -> sortedByDescending { if (it.n > 0) it.paidCount.toDouble() / it.n else 0.0 }
}

/**
 * لیست محلی Room + افزودن دستی/حذف/بازکردن جزئیات (پرداخت قسط)، پشتیبان‌گیری/بازیابی رو نشون می‌ده.
 *
 * پورت canSaveAnotherLoan/handleLoanLimitReached تو www/index.html: بعد از اولین وام، مهمون‌ها
 * باید وارد بشن (LoginScreen غیراجباری، با دکمه‌ی بازگشت)، کاربرهای واردشده‌ی بدون اشتراک به
 * [SubscriptionScreen] (خرید واقعی با Poolakey) می‌رن.
 */
@Composable
fun MyLoansScreen(viewModel: MyLoansViewModel = hiltViewModel(), authViewModel: AuthViewModel = hiltViewModel()) {
    var showAddForm by remember { mutableStateOf(false) }
    var openedLoanId by remember { mutableStateOf<Long?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showSubscriptionScreen by remember { mutableStateOf(false) }

    val rawLoans by viewModel.loans.collectAsState()
    var sortOption by remember { mutableStateOf(LoanSortOption.NEWEST) }
    val loans = remember(rawLoans, sortOption) { rawLoans.sortedByOption(sortOption) }
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = loans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)

    if (showLoginPrompt) {
        LoginScreen(
            onDismiss = { showLoginPrompt = false },
            onLoginSuccess = { showLoginPrompt = false },
        )
        return
    }

    if (showSubscriptionScreen) {
        SubscriptionScreen(
            onBack = { showSubscriptionScreen = false },
            onSubscribed = { showSubscriptionScreen = false },
        )
        return
    }

    if (showAddForm) {
        AddManualLoanScreen(
            onSaved = { showAddForm = false },
            onCancel = { showAddForm = false },
            viewModel = viewModel,
        )
        return
    }

    val openedLoan = openedLoanId?.let { id -> loans.firstOrNull { it.id == id } }
    if (openedLoan != null) {
        LoanDetailScreen(
            loan = openedLoan,
            onBack = { openedLoanId = null },
            onDelete = { viewModel.deleteLoan(openedLoan.id); openedLoanId = null },
            viewModel = viewModel,
        )
        return
    }

    fun onAddLoanClick() {
        when {
            canSaveAnotherLoan -> showAddForm = true
            gateState == null -> Unit // هنوز از DataStore خونده نشده، صبر کن
            gateState != GateState.LOGGED_IN -> showLoginPrompt = true
            else -> showSubscriptionScreen = true
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "پشتیبان‌گیری انجام شد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (json == null) {
                    Toast.makeText(context, "فایل قابل خوندن نبود", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.importBackup(json) { ok ->
                        val message = if (ok) "بازیابی شد" else "فایل معتبر نیست"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            DashboardSummary(
                loans = loans,
                monthlyIncome = monthlyIncome,
                onIncomeChange = { viewModel.setMonthlyIncome(it) },
            )
        }

        if (loans.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    LoanSortMenu(selected = sortOption, onSelect = { sortOption = it })
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("بازیابی", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        viewModel.exportBackup { json ->
                            pendingExportJson = json
                            createDocumentLauncher.launch("loans-backup.json")
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("پشتیبان‌گیری", fontSize = 12.sp)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { onAddLoanClick() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ افزودن دستی وام")
            }
        }

        if (loans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("هنوز وامی ذخیره نشده", color = AppText, fontSize = 15.sp)
                        Text(
                            "با دکمه‌ی + یه وام دستی اضافه کن",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        } else {
            items(loans, key = { it.id }) { loan ->
                AppCard(modifier = Modifier.clickable { openedLoanId = loan.id }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(loan.name, color = AppText, fontSize = 15.sp)
                            Text(loan.bank, color = AppMuted, fontSize = 12.sp)
                            Text(
                                "${loan.paidCount} از ${loan.n} قسط پرداخت‌شده",
                                color = AppPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        IconButton(onClick = { viewModel.deleteLoan(loan.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف وام", tint = AppDanger)
                        }
                    }
                }
            }
        }
    }
}

/**
 * پورت داشبورد اصلی اپ رقیب (VAMMAN): سه‌تا کارت بزرگ و خوانا - وضعیت کلی بدهی‌ها (مجموع مانده‌ی
 * همه‌ی وام‌ها، از رو installment×(n−paidCount) هر وام)، مجموع اقساط ماهانه (جمع installment همه‌ی
 * وام‌ها - تقریبی، فرض دوره‌ی ماهانه)، و تحلیل درآمد (نسبت اقساط به درآمد دستی کاربر با آستانه‌ی
 * رایج ۳۰٪/۵۰٪ که تو هیچ‌جای دیگه‌ی این پروژه از قبل تعریف نشده بود). برخلاف رقیب که این یه صفحه‌ی
 * جدا (home) بود، چون معماری تب‌های این اپ (رجوع کن به CLAUDE.md) ثابته، بالای همین «وام‌های من»
 * اضافه شده - جایی که داده‌ی وام‌ها از قبل در دسترسه.
 */
@Composable
private fun DashboardSummary(
    loans: List<LoanEntity>,
    monthlyIncome: Double,
    onIncomeChange: (Double) -> Unit,
) {
    val totalRemainingDebt = remember(loans) {
        loans.sumOf { it.installment * (it.n - it.paidCount) }
    }
    val totalMonthlyInstallment = remember(loans) { loans.sumOf { it.installment } }
    val ratio = if (monthlyIncome > 0) totalMonthlyInstallment / monthlyIncome else 0.0
    val statusLabel: String?
    val statusColor: Color
    when {
        monthlyIncome <= 0 -> {
            statusLabel = null
            statusColor = AppMuted
        }
        ratio <= 0.3 -> {
            statusLabel = "وضعیت مطلوب"
            statusColor = AppPrimary
        }
        ratio <= 0.5 -> {
            statusLabel = "محتاط باش"
            statusColor = AppAccent
        }
        else -> {
            statusLabel = "فشار مالی بالا"
            statusColor = AppDanger
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DashboardStatCard(
            title = "وضعیت کلی بدهی‌ها",
            value = "${fmt(totalRemainingDebt)} ریال",
            valueColor = AppText,
        )
        DashboardStatCard(
            title = "مجموع اقساط ماهانه",
            value = "${fmt(totalMonthlyInstallment)} ریال",
            valueColor = AppPrimary,
        )

        AppCard(label = "تحلیل درآمد") {
            var incomeText by remember(monthlyIncome) {
                mutableStateOf(if (monthlyIncome > 0) monthlyIncome.roundToInt().toString() else "")
            }
            OutlinedTextField(
                value = if (incomeText.isEmpty()) "" else toFa(incomeText),
                onValueChange = { raw ->
                    val cleaned = cleanNum(raw)
                    incomeText = cleaned
                    onIncomeChange(cleaned.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("درآمد ماهانه (ریال)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            if (monthlyIncome > 0) {
                Text(
                    "${toFa((ratio * 100).roundToInt())}٪ از درآمدت صرف اقساط می‌شه",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (statusLabel != null) {
                Text(
                    statusLabel,
                    color = statusColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun DashboardStatCard(title: String, value: String, valueColor: Color) {
    AppCard(label = title) {
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LoanSortMenu(selected: LoanSortOption, onSelect: (LoanSortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.FilterList, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
            Text("فیلتر", color = AppPrimary, fontSize = 13.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LoanSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            color = if (option == selected) AppPrimary else AppText,
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
