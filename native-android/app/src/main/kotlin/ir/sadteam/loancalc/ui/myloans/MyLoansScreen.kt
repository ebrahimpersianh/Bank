package ir.sadteam.loancalc.ui.myloans

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.IncomeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
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
    val incomes by viewModel.incomes.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = loans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)
    val openedLoan = openedLoanId?.let { id -> loans.firstOrNull { it.id == id } }

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
    val banner = rememberInAppBanner()

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
                    banner.show("پشتیبان‌گیری انجام شد", isSuccess = true)
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
                    banner.show("فایل قابل خوندن نبود")
                } else {
                    viewModel.importBackup(json) { ok ->
                        val message = if (ok) "بازیابی شد" else "فایل معتبر نیست"
                        banner.show(message, isSuccess = ok)
                    }
                }
            }
        }
    }

    // پورت حس تعویض نرم بین حالت‌های مختلف این صفحه (لیست/ورود/اشتراک/افزودن/جزئیات) - قبلاً هرکدوم
    // با یه return زودهنگام یهو جایگزین بقیه می‌شد؛ حالا با AnimatedContent (fade ظریف) عوض می‌شه.
    val screenKey = when {
        showLoginPrompt -> "login"
        showSubscriptionScreen -> "subscription"
        showAddForm -> "add"
        openedLoan != null -> "detail"
        else -> "list"
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
        label = "myLoansScreen",
    ) { key ->
        when (key) {
            "login" -> LoginScreen(
                onDismiss = { showLoginPrompt = false },
                onLoginSuccess = { showLoginPrompt = false },
            )
            "subscription" -> SubscriptionScreen(
                onBack = { showSubscriptionScreen = false },
                onSubscribed = { showSubscriptionScreen = false },
            )
            "add" -> AddManualLoanScreen(
                onSaved = { showAddForm = false },
                onCancel = { showAddForm = false },
                viewModel = viewModel,
            )
            "detail" -> openedLoan?.let { loan ->
                LoanDetailScreen(
                    loan = loan,
                    onBack = { openedLoanId = null },
                    onDelete = { viewModel.deleteLoan(loan.id); openedLoanId = null },
                    viewModel = viewModel,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DashboardSummary(
                        loans = loans,
                        incomes = incomes,
                        onAddIncome = { label, amount, type -> viewModel.addIncome(label, amount, type) },
                        onDeleteIncome = { viewModel.deleteIncome(it) },
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
                        // انیمیشنِ فلیپِ کارت (خواسته‌ی «انیمیشن‌های سفارشی») - آیکونِ اطلاعات، کارت رو
                        // مثل یه چکِ فیزیکی می‌چرخونه و خلاصه‌ی پرداخت رو پشتش نشون می‌ده؛ ضربه‌ی اصلیِ
                        // کارت هنوز باز کردنِ جزئیاتِ وامه، این فقط یه لایه‌ی جدا و مستقله.
                        var flipped by remember { mutableStateOf(false) }
                        val density = LocalDensity.current
                        val rotation by animateFloatAsState(
                            targetValue = if (flipped) 180f else 0f,
                            animationSpec = tween(500),
                            label = "loanCardFlip",
                        )
                        // animateItem: اضافه/حذف/جابه‌جایی وام‌ها با انیمیشن نرم (نه پرش یهویی).
                        AppCard(
                            modifier = Modifier
                                .animateItem()
                                .pressScaleClickable { openedLoanId = loan.id }
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density.density
                                },
                        ) {
                            if (rotation <= 90f) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // لوگوی بانک سمت راست کارت (لبه‌ی leading در RTL) - از رو اسم بانک.
                                    BankBadge(bankName = loan.bank)
                                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                        Text(loan.name, color = AppText, fontSize = 15.sp)
                                        Text(loan.bank, color = AppMuted, fontSize = 12.sp)
                                        Text(
                                            "${loan.paidCount} از ${loan.n} قسط پرداخت‌شده",
                                            color = AppPrimary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                    IconButton(onClick = { flipped = true }) {
                                        Icon(Icons.Filled.Info, contentDescription = "خلاصه پرداخت", tint = AppMuted)
                                    }
                                    IconButton(onClick = { viewModel.deleteLoan(loan.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "حذف وام", tint = AppDanger)
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { rotationY = 180f },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(loan.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { flipped = false }) {
                                            Icon(Icons.Filled.Info, contentDescription = "بستن خلاصه", tint = AppMuted)
                                        }
                                    }
                                    Text(
                                        "باقی‌مانده: ${maskIfPrivate(LocalPrivacyMode.current, fmt(loan.installment * (loan.n - loan.paidCount)))} ریال",
                                        color = AppPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                    Text(
                                        "${toFa(loan.n - loan.paidCount)} قسط باقیمانده از ${toFa(loan.n)}",
                                        color = AppMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        // دکمه‌ی «+» دایره‌ای سبز گوشه‌ی سمت چپ (در RTL: BottomEnd) - جایگزین دکمه‌ی تمام‌عرضِ
        // «افزودن دستی وام»؛ فقط رو خودِ لیست نشون داده می‌شه، نه رو فرم افزودن/جزئیات.
        // با یه pop فنری ظاهر/محو می‌شه (نه یهو).
        androidx.compose.animation.AnimatedVisibility(
            visible = screenKey == "list",
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            FloatingActionButton(
                onClick = { onAddLoanClick() },
                containerColor = AppPrimary,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن دستی وام")
            }
        }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * پورت داشبورد اصلی اپ رقیب (VAMMAN): سه‌تا کارت بزرگ و خوانا - وضعیت کلی بدهی‌ها (مجموع مانده‌ی
 * همه‌ی وام‌ها، از رو installment×(n−paidCount) هر وام)، مجموع اقساط ماهانه (جمع installment همه‌ی
 * وام‌ها - تقریبی، فرض دوره‌ی ماهانه)، و تحلیل درآمد (نسبت اقساط به جمع چند منبع درآمد مستقل -
 * ثابت/متغیر - با آستانه‌ی «منطقه‌ی امن» ۶۵٪ که از رشته‌های واقعی رقیب استخراج شد؛ نسخه‌ی قبلی این
 * پروژه اشتباهاً دو آستانه‌ی ۳۰٪/۵۰٪ حدسی داشت که تو هیچ‌جای رقیب پیدا نشد). برخلاف رقیب که این یه
 * صفحه‌ی جدا (home) بود، چون معماری تب‌های این اپ (رجوع کن به CLAUDE.md) ثابته، بالای همین «وام‌های
 * من» اضافه شده - جایی که داده‌ی وام‌ها از قبل در دسترسه.
 */
@Composable
private fun DashboardSummary(
    loans: List<LoanEntity>,
    incomes: List<IncomeEntity>,
    onAddIncome: (label: String, amount: Double, type: IncomeType) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
) {
    val totalRemainingDebt = remember(loans) {
        loans.sumOf { it.installment * (it.n - it.paidCount) }
    }
    val totalMonthlyInstallment = remember(loans) { loans.sumOf { it.installment } }
    val totalIncome = remember(incomes) { incomes.sumOf { it.amount } }
    val ratio = if (totalIncome > 0) totalMonthlyInstallment / totalIncome else 0.0
    val statusLabel: String?
    val statusColor: Color
    when {
        totalIncome <= 0 -> {
            statusLabel = null
            statusColor = AppMuted
        }
        ratio <= 0.65 -> {
            statusLabel = "وضعیت مطلوب"
            statusColor = AppPrimary
        }
        else -> {
            statusLabel = "فشار مالی بالا"
            statusColor = AppDanger
        }
    }

    var showAddIncome by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(IncomeType.FIXED) }

    // شمارش صعودی اعداد بزرگ داشبورد (پورت animateNumber وب) - حس «پریمیوم» موقع ورود به تب.
    val animatedDebt = countUpDouble(totalRemainingDebt)
    val animatedMonthly = countUpDouble(totalMonthlyInstallment)
    val privacyMode = LocalPrivacyMode.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DashboardStatCard(
            title = "وضعیت کلی بدهی‌ها",
            value = "${maskIfPrivate(privacyMode, fmt(animatedDebt))} ریال",
            valueColor = AppText,
        )
        DashboardStatCard(
            title = "مجموع اقساط ماهانه",
            value = "${maskIfPrivate(privacyMode, fmt(animatedMonthly))} ریال",
            valueColor = AppPrimary,
        )

        AppCard(label = "تحلیل درآمد") {
            if (incomes.isNotEmpty()) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    incomes.forEach { income ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(income.label, color = AppText, fontSize = 13.sp)
                                Text(
                                    if (income.type == IncomeType.FIXED.name) "ثابت" else "متغیر",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${maskIfPrivate(privacyMode, fmt(income.amount))} ریال",
                                    color = AppMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                                IconButton(onClick = { onDeleteIncome(income) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف منبع درآمد", tint = AppDanger)
                                }
                            }
                        }
                    }
                    Text(
                        "جمع درآمد: ${maskIfPrivate(privacyMode, fmt(totalIncome))} ریال",
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            if (showAddIncome) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم منبع درآمد (مثلاً حقوق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // مبلغ با جداکننده‌ی هزارگان نشون داده می‌شه و زیرش معادل حروفی (مثل «مبلغ وام»).
                    OutlinedTextField(
                        value = if (amountText.isEmpty()) "" else fmt((amountText.toLongOrNull() ?: 0L).toDouble()),
                        onValueChange = { amountText = cleanNum(it) },
                        label = { Text("مبلغ ماهانه (ریال)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val incomeRial = amountText.toLongOrNull() ?: 0L
                    if (incomeRial > 0) {
                        AutoShrinkText(
                            text = "${numberToWordsFa((incomeRial / 10).toDouble())} تومان",
                            color = AppMuted,
                            maxFontSize = 11.sp,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppChip(label = "ثابت", selected = type == IncomeType.FIXED, onClick = { type = IncomeType.FIXED })
                        AppChip(label = "متغیر", selected = type == IncomeType.VARIABLE, onClick = { type = IncomeType.VARIABLE })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                if (label.trim().isNotEmpty() && amount > 0) {
                                    onAddIncome(label.trim(), amount, type)
                                    label = ""
                                    amountText = ""
                                    showAddIncome = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("افزودن")
                        }
                        OutlinedButton(onClick = { showAddIncome = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                OutlinedButton(onClick = { showAddIncome = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ افزودن منبع درآمد")
                }
            }

            if (totalIncome > 0) {
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
