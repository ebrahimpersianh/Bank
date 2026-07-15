package ir.sadteam.loancalc.ui.cheque

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val typeFilterOptions = listOf(null to "همه", ChequeType.RECEIVED to "دریافتی", ChequeType.PAID to "پرداختی")

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

    val allCheques by viewModel.cheques.collectAsState()
    val chequeBooks by viewModel.chequeBooks.collectAsState()
    val visibleCheques = remember(allCheques, typeFilter) {
        val filterName = typeFilter?.name
        allCheques.filter { !it.archived && (filterName == null || it.type == filterName) }
    }
    val openedCheque = openedChequeId?.let { id -> allCheques.firstOrNull { it.id == id } }
    val editingCheque = editingChequeId?.let { id -> allCheques.firstOrNull { it.id == id } }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "پشتیبان‌گیری چک انجام شد", Toast.LENGTH_SHORT).show()
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
                        val message = if (ok) "بازیابی چک انجام شد" else "فایل معتبر نیست"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                        Text("امور چک", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        typeFilterOptions.forEach { (value, label) ->
                            AppChip(label = label, selected = typeFilter == value, onClick = { typeFilter = value })
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
                                    createDocumentLauncher.launch("cheques-backup.json")
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(onClick = { showAddForm = true }, modifier = Modifier.weight(1f)) {
                            Text("+ افزودن چک")
                        }
                        OutlinedButton(onClick = { showChequeBooks = true }, modifier = Modifier.weight(1f)) {
                            Text("دسته‌چک‌ها")
                        }
                    }
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
                        ChequeCard(cheque = cheque, onClick = { openedChequeId = cheque.id })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChequeCard(cheque: ChequeEntity, onClick: () -> Unit) {
    AppCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
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
