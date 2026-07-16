package ir.sadteam.loancalc.ui.account

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
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * پورت مفهومی ماژول «حساب» اپ رقیب (VAMMAN) - لیست حساب‌های بانکی با موجودی فعلی (محاسبه‌شده از رو
 * تراکنش‌ها، نه یه فیلد ثابت)، افزودن/ویرایش/حذف حساب، و جزئیات هر حساب (دفترچه‌ی تراکنش). الگوی
 * navigation داخلی عین ChequeScreen (screenKey مشتق‌شده + AnimatedContent).
 */
@Composable
fun AccountsScreen(onBack: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    var showAddForm by remember { mutableStateOf(false) }
    var editingAccountId by remember { mutableStateOf<Long?>(null) }
    var openedAccountId by remember { mutableStateOf<Long?>(null) }

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    // موجودی هر حساب فقط وقتی حساب‌ها/تراکنش‌ها عوض می‌شن دوباره حساب می‌شه، نه هر recomposition
    // به‌ازای هر کارت (قبلاً balanceOf رو تک‌تک آیتم‌ها هر بار صدا زده می‌شد).
    val balances = remember(accounts, transactions) {
        accounts.associate { it.id to viewModel.balanceOf(it, transactions) }
    }
    val openedAccount = openedAccountId?.let { id -> accounts.firstOrNull { it.id == id } }
    val editingAccount = editingAccountId?.let { id -> accounts.firstOrNull { it.id == id } }

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
                    Toast.makeText(context, "پشتیبان‌گیری حساب‌ها انجام شد", Toast.LENGTH_SHORT).show()
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
                        val message = if (ok) "بازیابی حساب‌ها انجام شد" else "فایل معتبر نیست"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val screenKey = when {
        showAddForm -> "add"
        openedAccount != null -> "detail"
        else -> "list"
    }

    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
        label = "accountsScreen",
    ) { key ->
        when (key) {
            "add" -> AddEditAccountScreen(
                existing = editingAccount,
                onSaved = { showAddForm = false; editingAccountId = null },
                onCancel = { showAddForm = false; editingAccountId = null },
                viewModel = viewModel,
            )
            "detail" -> openedAccount?.let { account ->
                AccountDetailScreen(
                    account = account,
                    onBack = { openedAccountId = null },
                    onEdit = { editingAccountId = account.id; openedAccountId = null; showAddForm = true },
                    onDelete = { viewModel.deleteAccount(account); openedAccountId = null },
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                        }
                        Text("حساب‌های بانکی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
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
                                    createDocumentLauncher.launch("accounts-backup.json")
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
                    GradientButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("+ افزودن حساب")
                    }
                }

                if (accounts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("هنوز حسابی ثبت نشده", color = AppText, fontSize = 15.sp)
                        }
                    }
                } else {
                    items(accounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            balance = balances[account.id] ?: account.initialBalance,
                            onClick = { openedAccountId = account.id },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(account: AccountEntity, balance: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(account.name, color = AppText, fontSize = 15.sp)
                Text(account.bankName, color = AppMuted, fontSize = 12.sp)
            }
            Text(
                "${fmt(balance)} ریال",
                color = if (balance < 0) AppDanger else AppText,
                fontSize = 14.sp,
            )
        }
    }
}
