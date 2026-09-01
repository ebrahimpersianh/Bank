package ir.sadteam.loancalc.ui.cheque

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.pillOverSurface
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class ChequeStats(
    val total: Int,
    val passed: Int,
    val bounced: Int,
    val pending: Int,
    val pendingReceived: Int,
    val pendingPaid: Int,
    val receivedPassedSum: Double,
    val paidPassedSum: Double,
) {
    val netBalance get() = receivedPassedSum - paidPassedSum
    val passRatePercent get() = if (total > 0) passed * 100.0 / total else 0.0
}

private fun computeChequeStats(cheques: List<ChequeEntity>): ChequeStats {
    val active = cheques.filter { !it.archived }
    return ChequeStats(
        total = active.size,
        passed = active.count { it.status == "PASSED" },
        bounced = active.count { it.status == "BOUNCED" },
        pending = active.count { it.status == "PENDING" },
        pendingReceived = active.count { it.type == "RECEIVED" && it.status == "PENDING" },
        pendingPaid = active.count { it.type == "PAID" && it.status == "PENDING" },
        receivedPassedSum = active.filter { it.type == "RECEIVED" && it.status == "PASSED" }.sumOf { it.amount },
        paidPassedSum = active.filter { it.type == "PAID" && it.status == "PASSED" }.sumOf { it.amount },
    )
}

@Composable
fun chequeStatusColor(status: String) = when (status) {
    "PASSED" -> AppPrimary
    "BOUNCED" -> AppDanger
    "REFUNDED" -> AppAccent
    else -> AppMuted
}

fun chequeStatusLabel(status: String) = when (status) {
    "PASSED" -> "پاس‌شده"
    "BOUNCED" -> "برگشت‌خورده"
    "REFUNDED" -> "مسترد شده"
    else -> "وضع‌نشده"
}

/**
 * پورت مفهومی ماژول «امور چک» اپ رقیب (VAMMAN) - لیست چک‌های دریافتی/پرداختی با فیلتر نوع/وضعیت،
 * افزودن/ویرایش/حذف، مدیریت دسته‌چک، و پشتیبان‌گیری جدا. الگوی navigation داخلی (list↔add↔detail↔
 * books) عین MyLoansScreen با یه screenKey مشتق‌شده + AnimatedContent.
 */
@Composable
fun ChequeScreen(
    onBack: () -> Unit,
    // وقتی چک به‌عنوانِ تبِ مستقلِ نوارِ پایین استفاده می‌شه (رجوع کن به CLAUDE.md - بازسازیِ
    // تب‌بندی)، دیگه «برگشت» معنی نداره (همتای بقیه‌ی تب‌هاست، نه زیرصفحه‌ی یه تبِ دیگه) - دکمه‌ی
    // برگشتِ ردیفِ بالای لیست مخفی می‌شه. زیرصفحه‌های داخلی (افزودن/جزئیات/گزارش/...) دست‌نخورده
    // می‌مونن، چون اون‌ها همیشه با همون منطقِ داخلیِ خودشون به لیست برمی‌گردن، نه به بیرونِ ChequeScreen.
    standalone: Boolean = false,
    viewModel: ChequeViewModel = hiltViewModel(),
) {
    var showAddForm by remember { mutableStateOf(false) }
    var editingChequeId by remember { mutableStateOf<Long?>(null) }
    var openedChequeId by remember { mutableStateOf<Long?>(null) }
    var showChequeBooks by remember { mutableStateOf(false) }
    var typeFilter by remember { mutableStateOf<ChequeType?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    // سه‌حالته‌ی بالای فهرست (فریمِ بخشِ ۳۶): در جریان · پاس‌شده · بایگانی.
    // قبلاً «بایگانی» فقط یه آیتمِ منوی سه‌خط بود و دیده نمی‌شد.
    var chequeTab by remember { mutableIntStateOf(0) }
    val showArchived = chequeTab == 2
    var showSayadInquiry by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val allCheques by viewModel.cheques.collectAsState()
    val chequeBooks by viewModel.chequeBooks.collectAsState()
    val visibleCheques = remember(allCheques, typeFilter, chequeTab, searchQuery) {
        val filterName = typeFilter?.name
        val q = searchQuery.trim()
        allCheques.filter {
            it.archived == showArchived &&
                (chequeTab != 0 || it.status == "PENDING") &&
                (chequeTab != 1 || it.status != "PENDING") &&
                (filterName == null || it.type == filterName) &&
                (
                    q.isBlank() ||
                        it.ownerName.contains(q, ignoreCase = true) ||
                        it.chequeNumber.contains(q, ignoreCase = true) ||
                        it.bankName.contains(q, ignoreCase = true)
                    )
        }
    }
    val stats = remember(allCheques) { computeChequeStats(allCheques) }
    val openedCheque = openedChequeId?.let { id -> allCheques.firstOrNull { it.id == id } }
    val editingCheque = editingChequeId?.let { id -> allCheques.firstOrNull { it.id == id } }

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
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }
                withContext(Dispatchers.Main) {
                    banner.show("پشتیبان‌گیری چک انجام شد", isSuccess = true)
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
                        val message = if (ok) "بازیابی چک انجام شد" else "فایل معتبر نیست"
                        banner.show(message, isSuccess = ok)
                    }
                }
            }
        }
    }

    // خروجی PDF/اکسل از لیست چک‌ها - هم‌الگو با StatsScreen (وام‌ها).
    val createPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { ChequePdfExporter.export(allCheques, it) }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    banner.show(if (ok) "PDF ذخیره شد" else "ذخیره‌ی PDF ناموفق بود", isSuccess = ok)
                }
            }
        }
    }
    val createXlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { ChequeXlsxExporter.export(allCheques, it) }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    banner.show(if (ok) "اکسل ذخیره شد" else "ذخیره‌ی اکسل ناموفق بود", isSuccess = ok)
                }
            }
        }
    }

    val screenKey = when {
        showChequeBooks -> "books"
        showAddForm -> "add"
        showSayadInquiry -> "sayad"
        showReport -> "report"
        showReminderSettings -> "reminders"
        openedCheque != null -> "detail"
        else -> "list"
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "chequeScreen",
    ) { key ->
        when (key) {
            "books" -> ChequeBooksScreen(
                books = chequeBooks,
                onBack = { showChequeBooks = false },
                onAdd = { owner, bank, start, end, sayadId, last4 ->
                    viewModel.addChequeBook(owner, bank, start, end, sayadId, last4)
                },
                onDelete = { viewModel.deleteChequeBook(it) },
                onClose = { viewModel.closeChequeBook(it) },
            )
            "add" -> AddEditChequeScreen(
                existing = editingCheque,
                chequeBooks = chequeBooks,
                onSaved = { showAddForm = false; editingChequeId = null },
                onCancel = { showAddForm = false; editingChequeId = null },
                viewModel = viewModel,
            )
            "sayad" -> SayadInquiryScreen(
                sayadId = openedCheque?.sayadId,
                onBack = { showSayadInquiry = false },
            )
            "report" -> ChequeReportScreen(
                stats = stats,
                onBack = { showReport = false },
                onDownloadPdf = { createPdfLauncher.launch("cheques.pdf") },
                onDownloadXlsx = { createXlsxLauncher.launch("cheques.xlsx") },
                onAddCheque = { showReport = false; showAddForm = true },
            )
            "reminders" -> ChequeReminderSettingsScreen(onBack = { showReminderSettings = false })
            "detail" -> openedCheque?.let { cheque ->
                ChequeDetailScreen(
                    cheque = cheque,
                    onBack = { openedChequeId = null },
                    onEdit = { editingChequeId = cheque.id; openedChequeId = null; showAddForm = true },
                    onDelete = { viewModel.deleteCheque(cheque.id); openedChequeId = null },
                    onSayadInquiry = { showSayadInquiry = true },
                    viewModel = viewModel,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!showArchived) {
                    item { LiveDateTimeHeader() }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!standalone) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                            }
                        }
                        Text(
                            if (showArchived) "بایگانی چک" else "امور چک",
                            color = AppText,
                            // ۱۵ نه ۱۸ - این صفحه یه لایه تودرتوست (از سررسید/خانه باز می‌شه).
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = if (standalone) 0.dp else 4.dp),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // تنظیمات این بخش (دسته‌چک‌ها/بایگانی/پشتیبان‌گیری/بازیابی) زیر آیکون
                        // سه‌خط، نه دیگه دکمه‌های همیشه‌نمایانِ بالای صفحه (خواسته‌ی کاربر).
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.Menu, contentDescription = "تنظیمات امور چک")
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("گزارش‌دهی") },
                                    onClick = { menuExpanded = false; showReport = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("دسته‌چک‌ها") },
                                    onClick = { menuExpanded = false; showChequeBooks = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("استعلام چک صیادی") },
                                    onClick = { menuExpanded = false; showSayadInquiry = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("تنظیمات یادآوری چک") },
                                    onClick = { menuExpanded = false; showReminderSettings = true },
                                )
                                DropdownMenuItem(
                                    text = { Text(if (showArchived) "بازگشت به لیست اصلی" else "بایگانی") },
                                    onClick = { menuExpanded = false; chequeTab = if (showArchived) 0 else 2; typeFilter = null },
                                )
                                DropdownMenuItem(
                                    text = { Text("پشتیبان‌گیری") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.exportBackup { json ->
                                            pendingExportJson = json
                                            createDocumentLauncher.launch("cheques-backup.json")
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("بازیابی") },
                                    onClick = {
                                        menuExpanded = false
                                        openDocumentLauncher.launch(arrayOf("application/json"))
                                    },
                                )
                            }
                        }
                    }
                }

                item {
                    SegmentedToggle(
                        options = listOf("در جریان", "پاس‌شده", "بایگانی"),
                        selectedIndex = chequeTab,
                        onSelect = { chequeTab = it; typeFilter = null },
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجو تو چک‌ها (اسم/شماره/بانک)...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }

                item {
                    // وضعیتِ چک‌های وضع‌نشده - چندتا دریافتی و چندتا پرداختی هنوز منتظرن.
                    AppCard(label = "وضعیت چک‌های وضع‌نشده") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PendingStatusItem(label = "دریافتی", count = stats.pendingReceived, color = AppPrimary)
                            PendingStatusItem(label = "پرداختی", count = stats.pendingPaid, color = AppDanger)
                        }
                    }
                }

                item {
                    // دکمه‌های نوع، هم‌زمان فیلترِ لیستِ پایین هم هستن (دوباره زدن روی یکی که
                    // انتخابه، فیلتر رو برمی‌داره) - رنگ‌ها هم‌الگو با chequeStatusColor.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChequeTypeButton(
                            label = "چک‌های دریافتی",
                            color = AppPrimary,
                            selected = typeFilter == ChequeType.RECEIVED,
                            onClick = { typeFilter = if (typeFilter == ChequeType.RECEIVED) null else ChequeType.RECEIVED },
                            modifier = Modifier.weight(1f),
                        )
                        ChequeTypeButton(
                            label = "چک‌های پرداختی",
                            color = AppDanger,
                            selected = typeFilter == ChequeType.PAID,
                            onClick = { typeFilter = if (typeFilter == ChequeType.PAID) null else ChequeType.PAID },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    GradientButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("+ افزودن چک جدید")
                    }
                }

                item {
                    ChequeAnalyticsDashboard(stats)
                }

                if (visibleCheques.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.ReceiptLong,
                            title = "هنوز چکی ثبت نشده",
                            description = "چک‌های دریافتی و پرداختیت رو اینجا ثبت کن تا " +
                                "قبل از سررسیدِ هرکدوم بهت یادآوری بشه.",
                        )
                    }
                } else {
                    items(visibleCheques, key = { it.id }) { cheque ->
                        ChequeCard(
                            cheque = cheque,
                            onClick = { openedChequeId = cheque.id },
                            onDelete = { viewModel.deleteCheque(cheque.id) },
                            onRestore = if (cheque.archived) {
                                { viewModel.setArchived(cheque, false) }
                            } else {
                                null
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ChequeCard(
    cheque: ChequeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onRestore: (() -> Unit)? = null,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    SwipeToDeleteRow(onDelete = { showDeleteConfirm = true }, confirmDismiss = false, modifier = modifier) {
    // ⚠️ **بازطراحیِ سبکِ «جیبک»** - ردیفِ چک طبقِ بخشِ «۸ · ردیفِ فهرست»ِ سیستمِ طراحی و
    // کارتِ `3a`: قابِ آیکونِ ۳۲ با گوشه‌ی ۱۲ و ته‌رنگِ وضعیت · عنوانِ ۱۲٫۵/۸۰۰ ·
    // فرادادهٔ ۱۰٫۵/۷۰۰ · مبلغِ ۱۲٫۵/۸۰۰.
    //
    // گونه‌ی کارت از **وضعیتِ** چک میاد، نه یه کارتِ سفیدِ همیشگی:
    //   وضع‌نشده‌ی سررسیدگذشته → فوری (زمینه‌ی صورتی، حاشیه و سایه‌ی قرمز)
    //   پاس‌شده/برگشت‌خورده/مسترد → تمام‌شده (شفافیتِ ۰٫۷۲، بی‌سایه)
    //   بقیه                    → پیش‌فرض
    val statusInk = chequeStatusColor(cheque.status)
    val settled = cheque.status != "PENDING"
    val overdue = !settled && JalaliCalendar.daysBetween(
        PersianDate(cheque.dueYear, cheque.dueMonth, cheque.dueDay),
        JalaliCalendar.today(),
    ) > 0
    AppCard(
        variant = when {
            overdue -> AppCardVariant.URGENT
            settled -> AppCardVariant.DONE
            else -> AppCardVariant.DEFAULT
        },
        contentPadding = 12.dp,
        modifier = Modifier.pressScaleClickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // قابِ آیکون از **نوعِ** چک رنگ می‌گیره نه وضعیتش (فریمِ بخشِ ۳۶): دریافتی بنفش،
            // پرداختی آبی. چکِ بایگانی خاکستری می‌شه.
            val received = cheque.type == "RECEIVED"
            val frameFill = when {
                cheque.archived -> AppChipBg
                received -> AppPurplePill
                else -> AppInfoPill
            }
            val frameInk = when {
                cheque.archived -> AppLabel
                received -> AppPurple
                else -> AppInfo
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(frameFill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = frameInk,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                // شماره‌ی چک ذاتاً چپ‌به‌راسته - فقط همون تکه تو `Ltr` می‌ره، نه کلِ سطر.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${cheque.bankName} · ",
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Ltr {
                        Text(
                            toFa(cheque.chequeNumber.takeLast(4)),
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                Text(
                    "${chequeStatusLabel(cheque.status)} · ${toFa(cheque.dueDay)}/${toFa(cheque.dueMonth)}/${toFa(cheque.dueYear)}",
                    color = if (overdue) AppDangerInk else AppMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // ردیفِ بایگانی مبلغ نداره؛ جاش چیپِ «برگردان» می‌شینه (فریمِ بخشِ ۳۶).
            if (cheque.archived && onRestore != null) {
                Box(modifier = Modifier.height(44.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppPrimaryPill)
                            .border(1.dp, AppPrimaryBorder, RoundedCornerShape(999.dp))
                            .pressScaleClickable(onClick = onRestore)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            "برگردان",
                            color = AppPrimaryInk,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            } else {
                Text(
                    fmt(cheque.amount),
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
    }
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "حذف چک",
            text = "چکِ شماره‌ی «${toFa(cheque.chequeNumber)}» حذف بشه؟ این کار قابلِ‌برگشت نیست.",
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

private val faWeekDayNames = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

/**
 * ساعت زنده + تاریخ جلالی امروز، بالای صفحه‌ی اصلیِ «امور چک» (خواسته‌ی کاربر، عکسِ مرجع داشت). هر
 * ثانیه با یه `LaunchedEffect` حلقه‌ای تیک می‌خوره؛ تبدیل میلادی→جلالی از رو همون
 * `core/JalaliCalendar.kt` نجومیِ دقیق (تنها منبع درستِ «امروزِ شمسی» تو این پروژه، رجوع کن به
 * کامنت بالای خودش).
 */
@Composable
private fun LiveDateTimeHeader() {
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }
    val cal = remember(nowMillis) { Calendar.getInstance().apply { timeInMillis = nowMillis } }
    val jalali = remember(nowMillis) {
        JalaliCalendar.fromGregorian(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
    val weekDay = faWeekDayNames[JalaliCalendar.dayOfWeekSaturdayFirst(jalali)]
    val dateText = "${toFa(jalali.y)}/${toFa("%02d".format(jalali.m))}/${toFa("%02d".format(jalali.d))}"
    val timeText = toFa(
        "%02d:%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND),
        ),
    )

    // ⚠️ کارتِ نمایشِ تاریخ/ساعته، نه کارتِ **قهرمانِ** صفحه - طبقِ قاعده‌ی «حداکثر یک رنگِ لهجه
    // در هر صفحه» گرادیانِ سبزش برداشته شد و کارتِ سفیدِ معمولی شد.
    AppCard {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(weekDay, color = AppText.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(
                    dateText,
                    color = AppText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppChipBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(timeText, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PendingStatusItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(label, color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
        }
        Text(
            toFa(count),
            color = AppText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ChequeTypeButton(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color.pillOverSurface(0.16f) else AppSurface2,
        border = BorderStroke(1.dp, if (selected) color else Color.Transparent),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) color else AppMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatNumber(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(toFa(value), color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppMuted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/**
 * پورت «داشبورد تحلیلی چک» اپ رقیب - کاملاً از رو دیتای واقعیِ محلی (Room) محاسبه می‌شه، نه عدد
 * ساختگی: کل/پاس‌شده/برگشت‌خورده/وضع‌نشده، مجموع دریافتی/پرداختیِ پاس‌شده، مانده‌ی خالص (تفاضل اون
 * دوتا)، و نرخ پاس‌شدن (پاس‌شده از کل).
 */
@Composable
private fun ChequeAnalyticsDashboard(stats: ChequeStats) {
    AppCard(label = "داشبورد تحلیلی چک") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatNumber("کل چک‌ها", stats.total, AppText)
            StatNumber("پاس‌شده", stats.passed, AppPrimary)
            StatNumber("برگشت‌خورده", stats.bounced, AppDanger)
            StatNumber("وضع‌نشده", stats.pending, AppMuted)
        }

        HorizontalDivider(color = AppLine, modifier = Modifier.padding(vertical = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("دریافتی پاس‌شده", color = AppMuted, fontSize = 11.sp)
                Text(
                    "${fmt(stats.receivedPassedSum)} ریال",
                    color = AppPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("پرداختی پاس‌شده", color = AppMuted, fontSize = 11.sp)
                Text(
                    "${fmt(stats.paidPassedSum)} ریال",
                    color = AppDanger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        val netColor = if (stats.netBalance >= 0) AppPrimary else AppDanger
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(netColor.pillOverSurface(0.10f))
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("مانده خالص", color = AppText, fontSize = 13.sp)
                Text(
                    "${if (stats.netBalance >= 0) "+" else ""}${fmt(stats.netBalance)} ریال",
                    color = netColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("نرخ پاس‌شدن", color = AppMuted, fontSize = 12.sp)
            LinearProgressIndicator(
                progress = (stats.passRatePercent / 100.0).toFloat(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AppAccent,
                trackColor = AppSurface2,
            )
            Text(
                "${toFa(stats.passRatePercent.toInt())}٪",
                color = AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
