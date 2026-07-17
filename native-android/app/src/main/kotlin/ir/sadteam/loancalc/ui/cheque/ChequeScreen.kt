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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ChequeStats(
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
fun ChequeScreen(onBack: () -> Unit, viewModel: ChequeViewModel = hiltViewModel()) {
    var showAddForm by remember { mutableStateOf(false) }
    var editingChequeId by remember { mutableStateOf<Long?>(null) }
    var openedChequeId by remember { mutableStateOf<Long?>(null) }
    var showChequeBooks by remember { mutableStateOf(false) }
    var typeFilter by remember { mutableStateOf<ChequeType?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }

    val allCheques by viewModel.cheques.collectAsState()
    val chequeBooks by viewModel.chequeBooks.collectAsState()
    val visibleCheques = remember(allCheques, typeFilter, showArchived) {
        val filterName = typeFilter?.name
        allCheques.filter { it.archived == showArchived && (filterName == null || it.type == filterName) }
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
                    banner.show("پشتیبان‌گیری چک انجام شد")
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
                        banner.show(message)
                    }
                }
            }
        }
    }

    val screenKey = when {
        showChequeBooks -> "books"
        showAddForm -> "add"
        openedCheque != null -> "detail"
        else -> "list"
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
        label = "chequeScreen",
    ) { key ->
        when (key) {
            "books" -> ChequeBooksScreen(
                books = chequeBooks,
                onBack = { showChequeBooks = false },
                onAdd = { owner, bank, start, end -> viewModel.addChequeBook(owner, bank, start, end) },
                onDelete = { viewModel.deleteChequeBook(it) },
            )
            "add" -> AddEditChequeScreen(
                existing = editingCheque,
                chequeBooks = chequeBooks,
                onSaved = { showAddForm = false; editingChequeId = null },
                onCancel = { showAddForm = false; editingChequeId = null },
                viewModel = viewModel,
            )
            "detail" -> openedCheque?.let { cheque ->
                ChequeDetailScreen(
                    cheque = cheque,
                    onBack = { openedChequeId = null },
                    onEdit = { editingChequeId = cheque.id; openedChequeId = null; showAddForm = true },
                    onDelete = { viewModel.deleteCheque(cheque.id); openedChequeId = null },
                    viewModel = viewModel,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                        }
                        Text(
                            if (showArchived) "بایگانی چک" else "امور چک",
                            color = AppText,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 4.dp),
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
                                    text = { Text("دسته‌چک‌ها") },
                                    onClick = { menuExpanded = false; showChequeBooks = true },
                                )
                                DropdownMenuItem(
                                    text = { Text(if (showArchived) "بازگشت به لیست اصلی" else "بایگانی") },
                                    onClick = { menuExpanded = false; showArchived = !showArchived; typeFilter = null },
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
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("هنوز چکی ثبت نشده", color = AppText, fontSize = 15.sp)
                        }
                    }
                } else {
                    items(visibleCheques, key = { it.id }) { cheque ->
                        ChequeCard(
                            cheque = cheque,
                            onClick = { openedChequeId = cheque.id },
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
private fun ChequeCard(cheque: ChequeEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("چک ${toFa(cheque.chequeNumber)}", color = AppText, fontSize = 15.sp)
                Text(cheque.bankName, color = AppMuted, fontSize = 12.sp)
                Text(
                    "${toFa(cheque.dueDay)}/${toFa(cheque.dueMonth)}/${toFa(cheque.dueYear)}",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${fmt(cheque.amount)} ریال", color = AppText, fontSize = 13.sp)
                Text(
                    chequeStatusLabel(cheque.status),
                    color = chequeStatusColor(cheque.status),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
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
        color = if (selected) color.copy(alpha = 0.16f) else AppSurface2,
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
                .background(netColor.copy(alpha = 0.10f))
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
                color = AppAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
