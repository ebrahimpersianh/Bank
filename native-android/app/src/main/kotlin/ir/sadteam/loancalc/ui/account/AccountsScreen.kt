package ir.sadteam.loancalc.ui.account

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
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
fun AccountsScreen(
    onBack: () -> Unit,
    // خواسته‌ی صریحِ کاربر: افزودنِ حساب دیگه فقط از تنظیمات نباشه، از تبِ «دارایی» هم مستقیم قابلِ‌
    // دسترسی باشه - وقتی true باشه، این صفحه مستقیم با فرمِ بازِ افزودنِ حساب باز می‌شه، نه لیستِ خالی.
    startInAddMode: Boolean = false,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    var showAddForm by remember { mutableStateOf(startInAddMode) }
    var editingAccountId by remember { mutableStateOf<Long?>(null) }
    var openedAccountId by remember { mutableStateOf<Long?>(null) }

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    // موجودی هر حساب فقط وقتی حساب‌ها/تراکنش‌ها عوض می‌شن دوباره حساب می‌شه، نه هر recomposition
    // به‌ازای هر کارت (قبلاً balanceOf رو تک‌تک آیتم‌ها هر بار صدا زده می‌شد).
    val balances = remember(accounts, transactions) {
        accounts.associate { it.id to viewModel.balanceOf(it, transactions) }
    }
    // جمعِ موجودی برای هیرویِ بالای لیست - همون balances که از قبل حساب شده، جمعش بی‌هزینه‌ست.
    val totalBalance = remember(balances) { balances.values.sum() }
    val openedAccount = openedAccountId?.let { id -> accounts.firstOrNull { it.id == id } }
    val editingAccount = editingAccountId?.let { id -> accounts.firstOrNull { it.id == id } }

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
                    banner.show("پشتیبان‌گیری حساب‌ها انجام شد", isSuccess = true)
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
                        val message = if (ok) "بازیابی حساب‌ها انجام شد" else "فایل معتبر نیست"
                        banner.show(message, isSuccess = ok)
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

    Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
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
                contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 152.dp),
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

                if (accounts.isNotEmpty()) {
                    item {
                        AccountsTotalHero(total = totalBalance, balances = balances, accounts = accounts)
                    }
                }

                item {
                    BackupRestoreCard(
                        onBackup = {
                            viewModel.exportBackup { json ->
                                pendingExportJson = json
                                createDocumentLauncher.launch("accounts-backup.json")
                            }
                        },
                        onRestore = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    )
                }

                if (accounts.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.AccountBalance,
                            title = "هنوز حسابی ثبت نشده",
                            description = "حساب‌های بانکیت رو اضافه کن تا موجودی و " +
                                "گردشِ هرکدوم رو یک‌جا داشته باشی.",
                        )
                    }
                } else {
                    items(accounts, key = { it.id }) { account ->
                        val balance = balances[account.id] ?: account.initialBalance
                        AccountCard(
                            account = account,
                            balance = balance,
                            share = if (totalBalance > 0) (balance / totalBalance).toFloat().coerceIn(0f, 1f) else 0f,
                            onClick = { openedAccountId = account.id },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))

        // «+ افزودن حساب» چسبیده به پایینِ صفحه (خواسته‌ی طراحی: همیشه در دست باشه) - فقط تو حالتِ
        // لیست نشون داده می‌شه، نه موقعِ افزودن/جزئیات.
        if (screenKey == "list") {
            GradientButton(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 24.dp),
            ) {
                Text("+ افزودن حساب")
            }
        }
    }
}

/** هیرویِ جمعِ موجودیِ بالای لیست - عددِ کل + نوارِ سهمِ هر حساب از کل (بدونِ کوئریِ جدید، از همون
 * balancesِ ازقبل‌محاسبه‌شده). */
@Composable
private fun AccountsTotalHero(total: Double, balances: Map<Long, Double>, accounts: List<AccountEntity>) {
    // کارتِ `26b`ی طرح: سبزِ توپر با متنِ سفید. عددِ قهرمان ۲۶/۹۰۰ و برچسبِ بالاش ۱۰٫۵/۷۰۰.
    AppHeroCard {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("جمعِ موجودی", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${fmt(total)}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    Text("ریال · ${toFa(accounts.size)} حساب", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(HeroPillBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            // نوارِ سهمِ هر حساب - رو زمینه‌ی سبز با سفیدهای کم‌آلفا کشیده می‌شه، نه سبزهای کم‌آلفا
            // (که رو خودِ سبز اصلاً دیده نمی‌شدن).
            Row(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(AppRadius.button)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val alphas = listOf(0.92f, 0.55f, 0.30f)
                accounts.forEachIndexed { index, account ->
                    val balance = balances[account.id] ?: account.initialBalance
                    val weight = if (total > 0) (balance / total).toFloat().coerceAtLeast(0.02f) else 1f / accounts.size
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = alphas.getOrElse(index) { 0.30f })),
                    )
                }
            }
        }
    }
}

/** پشتیبان‌گیری/بازیابی - از دو OutlinedButtonِ لختِ قبلی به یه کارتِ آبیِ دوتکه (خواسته‌ی طراحی:
 * ابزار = آبی، پول = سبز). هر نیمه ناحیه‌ی لمسیِ کاملِ کارت رو داره (≥۴۴.dp با پدینگِ خودِ AppCard). */
@Composable
private fun BackupRestoreCard(onBackup: () -> Unit, onRestore: () -> Unit) {
    AppCard(borderColor = AppInfo.copy(alpha = 0.30f)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).pressScaleClickable(onClick = onBackup),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(AppInfoPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
                }
                Text("پشتیبان‌گیری", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).height(26.dp).background(AppInfoPill))
            Row(
                modifier = Modifier.weight(1f).pressScaleClickable(onClick = onRestore),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    "بازیابی",
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(AppInfoPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountEntity,
    balance: Double,
    share: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AppPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(account.bankName.take(2), color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(account.name, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${fmt(balance)} ریال",
                        color = if (balance < 0) AppDanger else AppText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(account.bankName, color = AppMuted, fontSize = 11.sp)
                    Text("٪${toFa((share * 100).toInt())} از دارایی", color = AppMuted, fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(999.dp)).background(AppPrimary.copy(alpha = 0.14f)),
                ) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(share.coerceIn(0f, 1f)).background(AppPrimary))
                }
            }
        }
    }
}
