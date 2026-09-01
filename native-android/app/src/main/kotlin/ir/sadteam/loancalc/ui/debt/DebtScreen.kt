package ir.sadteam.loancalc.ui.debt

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DebtEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinCelebration
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion

/**
 * تبِ «طلب و بدهی» - لیستِ طرف‌حساب‌ها با مانده‌ی خالص (طلب/بدهی)، و جزئیاتِ هر طرف‌حساب (ردیف‌های
 * طلب/بدهیِ جداگانه، هرکدوم قابلِ‌تسویه/حذف). رجوع کن به CLAUDE.md برای تصمیمِ ادغامِ این ماژول با
 * بقیه‌ی اکوسیستم.
 */
@Composable
fun DebtScreen(
    onBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel(),
    dangViewModel: DangViewModel = hiltViewModel(),
) {
    val counterparties by viewModel.counterparties.collectAsState()
    val debts by viewModel.debts.collectAsState()
    var openedCounterpartyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showAddCounterparty by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CounterpartyEntity?>(null) }

    // «دنگ» - فریمِ `22c`، ناوبریِ داخلیِ خودش (لیست/فرمِ ساخت/جزئیات) کنارِ همون
    // سه‌حالتِ قبلیِ این صفحه.
    val dangEvents by dangViewModel.events.collectAsState()
    var dangScreen by rememberSaveable { mutableStateOf("none") } // none | list | create | detail
    var openedDangEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    val openedDangEvent = openedDangEventId?.let { id -> dangEvents.firstOrNull { it.id == id } }
    // جشنِ کوچیکِ «تسویه شد» (بستهٔ ارتقاهای گرافیکی، خواسته‌ی صریحِ کاربر). عمداً فقط وقتی
    // **آخرین** ردیفِ بازِ یه طرف‌حساب تسویه می‌شه اجرا می‌شه، نه هر تسویه‌ی تکی - یه اتفاقِ نادر و
    // واقعاً «رسیدن به هدف»ه، پس تکراری/آزاردهنده نمی‌شه. (هم‌الگو با جشنِ تسویه‌ی کاملِ وام تو
    // LoanDetailScreen.)
    var celebrate by remember { mutableStateOf(false) }

    val opened = openedCounterpartyId?.let { id -> counterparties.firstOrNull { it.id == id } }
    val screenKey = when {
        dangScreen != "none" -> "dang-$dangScreen"
        opened != null -> "detail"
        else -> "list"
    }
    fun counterpartyNameFor(id: Long?): String = counterparties.firstOrNull { it.id == id }?.name ?: "خودم"

    pendingDelete?.let { counterparty ->
        ConfirmDeleteDialog(
            title = "حذفِ طرف‌حساب",
            text = "«${counterparty.name}» و همه‌ی ردیف‌های طلب/بدهیش حذف بشه؟",
            onConfirm = { viewModel.deleteCounterparty(counterparty); openedCounterpartyId = null },
            onDismiss = { pendingDelete = null },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "debtScreen",
    ) { key ->
        when (key) {
            "dang-list" -> DangListScreen(
                events = dangEvents,
                onBack = { dangScreen = "none" },
                onOpen = { openedDangEventId = it.id; dangScreen = "detail" },
                onAddNew = { dangScreen = "create" },
            )
            "dang-create" -> DangCreateScreen(
                counterparties = counterparties,
                onCancel = { dangScreen = "list" },
                onCreateCounterparty = { name, onCreated -> viewModel.addCounterparty(name, onCreated) },
                onSave = { title, method, total, y, m, d, eventMode, participants, items ->
                    dangViewModel.createEvent(title, method, total, y, m, d, eventMode, participants, items) {
                        dangScreen = "list"
                    }
                },
            )
            "dang-detail" -> openedDangEvent?.let { event ->
                val participants by dangViewModel.participants(event.id).collectAsState(initial = emptyList())
                DangDetailScreen(
                    event = event,
                    participants = participants,
                    counterpartyNameFor = ::counterpartyNameFor,
                    onBack = { dangScreen = "list"; openedDangEventId = null },
                    onDelete = { dangViewModel.deleteEvent(event); dangScreen = "list"; openedDangEventId = null },
                    onToggleEventSettled = { dangViewModel.setEventSettled(event, it) },
                    onToggleParticipantSettled = { participant, settled -> dangViewModel.setParticipantSettled(participant, settled) },
                )
            }
            "detail" -> opened?.let { counterparty ->
                CounterpartyDetail(
                    counterparty = counterparty,
                    debts = debts.filter { it.counterpartyId == counterparty.id },
                    onBack = { openedCounterpartyId = null },
                    onDelete = { pendingDelete = counterparty },
                    onAddDebt = { amount, type, description, y, m, d ->
                        viewModel.addDebt(counterparty.id, amount, type, description, y, m, d)
                    },
                    onToggleSettled = { debt, settled ->
                        viewModel.setSettled(debt, settled)
                        // اگه این تسویه، آخرین ردیفِ بازِ این طرف‌حساب رو ببنده → جشن.
                        if (settled) {
                            val stillOpen = debts.any {
                                it.counterpartyId == counterparty.id && it.id != debt.id && !it.settled
                            }
                            if (!stillOpen) celebrate = true
                        }
                    },
                    onDeleteDebt = { viewModel.deleteDebt(it) },
                )
            }
            else -> DebtList(
                counterparties = counterparties,
                debts = debts,
                onBack = onBack,
                onOpen = { openedCounterpartyId = it.id },
                showAddCounterparty = showAddCounterparty,
                onShowAddCounterpartyChange = { showAddCounterparty = it },
                onAddCounterparty = { viewModel.addCounterparty(it) },
                netBalance = { id -> viewModel.netBalance(id, debts) },
                onOpenDang = { dangScreen = "list" },
            )
        }
    }

    // رو همه‌چیزِ صفحه (آخرین بچه‌ی Box) - لمس رو مصرف نمی‌کنه، فقط چند ثانیه سکه می‌باره.
    if (celebrate) {
        CoinCelebration(
            modifier = Modifier.fillMaxSize(),
            onFinished = { celebrate = false },
        )
    }
    }
}

@Composable
private fun DebtList(
    counterparties: List<CounterpartyEntity>,
    debts: List<DebtEntity>,
    onBack: () -> Unit,
    onOpen: (CounterpartyEntity) -> Unit,
    showAddCounterparty: Boolean,
    onShowAddCounterpartyChange: (Boolean) -> Unit,
    onAddCounterparty: (String) -> Unit,
    netBalance: (Long) -> Double,
    onOpenDang: () -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var newName by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visibleCounterparties = remember(counterparties, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) counterparties else counterparties.filter { it.name.contains(q, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("طلب و بدهی", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            // «دنگ» - فریمِ `22c`، درِ ورودی از همینجا (تقسیمِ یه هزینه بینِ چند طرف‌حساب).
            OutlinedButton(onClick = onOpenDang, modifier = Modifier.fillMaxWidth()) {
                Text("دنگ‌ها (تقسیمِ هزینه بینِ چند نفر)")
            }
        }
        if (counterparties.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجو تو طرف‌حساب‌ها...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        if (showAddCounterparty) {
            item {
                AppCard(label = "طرفِ‌حسابِ جدید") {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    GradientButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onAddCounterparty(newName.trim())
                                newName = ""
                                onShowAddCounterpartyChange(false)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) { Text("افزودن") }
                }
            }
        } else {
            item {
                GradientButton(
                    onClick = { onShowAddCounterpartyChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("افزودنِ طرفِ‌حساب") }
            }
        }
        if (visibleCounterparties.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Handshake,
                    title = if (counterparties.isEmpty()) "هنوز طرفِ‌حسابی نداری" else "چیزی پیدا نشد",
                    description = if (counterparties.isEmpty()) {
                        "طلب یا بدهیِ خودت با یه شخص رو اینجا ثبت کن تا فراموش نشه."
                    } else {
                        "طرفِ‌حسابی با این اسم پیدا نشد."
                    },
                )
            }
        } else {
            items(visibleCounterparties, key = { it.id }) { counterparty ->
                val balance = netBalance(counterparty.id)
                AppCard(modifier = Modifier.pressScaleClickable { onOpen(counterparty) }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(counterparty.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                when {
                                    balance > 0 -> "طلبِ تو"
                                    balance < 0 -> "بدهیِ تو"
                                    else -> "بی‌حساب"
                                },
                                color = AppMuted,
                                fontSize = 12.sp,
                            )
                        }
                        Text(
                            maskIfPrivate(privacyMode, "${fmt(kotlin.math.abs(balance))} ریال"),
                            color = when {
                                balance > 0 -> AppPrimary
                                balance < 0 -> AppDanger
                                else -> AppMuted
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterpartyDetail(
    counterparty: CounterpartyEntity,
    debts: List<DebtEntity>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onAddDebt: (amount: Double, type: DebtType, description: String, y: Int, m: Int, d: Int) -> Unit,
    onToggleSettled: (DebtEntity, Boolean) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var showAddDebt by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(DebtType.OWED_TO_ME) }
    val today = remember { JalaliCalendar.today() }
    var year by rememberSaveable { mutableStateOf(today.y) }
    var month by rememberSaveable { mutableStateOf(today.m) }
    var day by rememberSaveable { mutableStateOf(today.d) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(counterparty.name, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذفِ طرف‌حساب", tint = AppDanger)
                }
            }
        }
        item {
            if (showAddDebt) {
                AppCard(label = "ردیفِ جدید") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DebtTypeChip(
                            label = "طلبِ من",
                            selected = type == DebtType.OWED_TO_ME,
                            modifier = Modifier.weight(1f),
                            onClick = { type = DebtType.OWED_TO_ME },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DebtTypeChip(
                            label = "بدهیِ من",
                            selected = type == DebtType.I_OWE,
                            modifier = Modifier.weight(1f),
                            onClick = { type = DebtType.I_OWE },
                        )
                    }
                    Ltr {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = cleanNum(it) },
                            label = { Text("مبلغ") },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            singleLine = true,
                            suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                        )
                    }
                    val amountRial = amountText.toLongOrNull() ?: 0L
                    if (amountRial > 0) {
                        Text(
                            "${numberToWordsFa((amountRial / 10).toDouble())} تومان",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیح (اختیاری)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        singleLine = true,
                    )
                    InlineJalaliDateRow(
                        year = year,
                        month = month,
                        day = day,
                        onDateChange = { y, m, d -> year = y; month = m; day = d },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    GradientButton(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onAddDebt(amount, type, description.trim(), year, month, day)
                                amountText = ""
                                description = ""
                                showAddDebt = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) { Text("ثبت") }
                }
            } else {
                GradientButton(onClick = { showAddDebt = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("افزودنِ ردیفِ طلب/بدهی")
                }
            }
        }
        if (debts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Handshake,
                    title = "هنوز ردیفی ثبت نشده",
                    description = "اولین طلب یا بدهیت با «${counterparty.name}» رو ثبت کن.",
                )
            }
        } else {
            items(debts, key = { it.id }) { debt ->
                Column {
                    AppCard {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    maskIfPrivate(privacyMode, "${fmt(debt.amount)} ریال"),
                                    color = if (debt.type == DebtType.OWED_TO_ME.name) AppPrimary else AppDanger,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (debt.description.isNotBlank()) {
                                    Text(debt.description, color = AppMuted, fontSize = 12.sp)
                                }
                                Text(
                                    "${toFa(debt.year)}/${toFa(debt.month)}/${toFa(debt.day)}" +
                                        if (debt.settled) " - تسویه‌شده" else "",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                )
                            }
                            IconButton(onClick = { onToggleSettled(debt, !debt.settled) }) {
                                Text(if (debt.settled) "↺" else "✓", color = if (debt.settled) AppMuted else AppPrimary)
                            }
                            IconButton(onClick = { onDeleteDebt(debt) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                            }
                        }
                    }
                    HorizontalDivider(color = AppMuted.copy(alpha = 0.12f), modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun DebtTypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppCard(
            backgroundColor = if (selected) AppPrimary.copy(alpha = 0.16f) else null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                label,
                color = if (selected) AppPrimary else AppMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
