package ir.sadteam.loancalc.ui.debt

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppTxOut
import ir.sadteam.loancalc.ui.theme.AppTxIn
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.jibak.toFaSignedMoney
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.components.persianMonthName
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import ir.sadteam.loancalc.ui.components.AppButtonVariant
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Search
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import ir.sadteam.loancalc.ui.components.Avatar
import ir.sadteam.loancalc.ui.components.AvatarColor
import ir.sadteam.loancalc.ui.components.AvatarShape
import ir.sadteam.loancalc.ui.components.AvatarView
import ir.sadteam.loancalc.ui.components.CoinCelebration
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.ConfirmPayDialog
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
    /** تپ روی ردیفِ طلب‌وبدهی در تبِ سررسید مستقیم همان طرفِ‌حساب را باز می‌کند - رسیدن به
     * فهرستِ کامل و گشتن دنبالِ همان نام، همان نیم‌کنشی است که سرِ چک بسته شد. */
    initialCounterpartyId: Long? = null,
    viewModel: DebtViewModel = hiltViewModel(),
    dangViewModel: DangViewModel = hiltViewModel(),
) {
    val counterparties by viewModel.counterparties.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    // «این پول از/به کدام حساب رفت؟» - بعد از تسویه یا پرداختِ بخشی. رد کردن = ثبت نشود.
    data class MoneyPrompt(val sourceId: String, val amount: Double, val deposit: Boolean, val description: String)
    var moneyPrompt by remember { mutableStateOf<MoneyPrompt?>(null) }
    moneyPrompt?.let { p ->
        ir.sadteam.loancalc.ui.components.AccountPickerDialog(
            accounts = accounts,
            title = if (p.deposit) "این پول به کدام حساب آمد؟" else "این پول از کدام حساب رفت؟",
            onSelect = { acc -> viewModel.recordMoney(acc.id, p.sourceId, p.amount, p.deposit, p.description); moneyPrompt = null },
            onDismiss = { moneyPrompt = null },
        )
    }
    var openedCounterpartyId by rememberSaveable { mutableStateOf(initialCounterpartyId) }
    var showAddCounterparty by rememberSaveable { mutableStateOf(false) }
    val isPremium = ir.sadteam.loancalc.ui.subscription.LocalIsPremium.current
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
                onCreateCounterparty = { name, onCreated -> viewModel.addCounterparty(name, onResult = onCreated) },
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
                    onUpdate = { viewModel.updateCounterparty(it) },
                    onAddDebt = { amount, type, description, y, m, d ->
                        val partial = accounts.isNotEmpty() && (description.startsWith("دریافتِ بخشی") || description.startsWith("پرداختِ بخشی"))
                        viewModel.addDebt(counterparty.id, amount, type, description, y, m, d) { newId ->
                        // پرداخت/دریافتِ بخشی یعنی پول واقعاً جابه‌جا شد. شناسه = همان ردیف تا حذفش پول را برگرداند.
                        if (partial) {
                            moneyPrompt = MoneyPrompt(
                                newId.toString(),
                                amount,
                                deposit = description.startsWith("دریافتِ بخشی"),
                                description = "$description - ${counterparty.name}",
                            )
                        }
                        }
                    },
                    onToggleSettled = { debt, settled ->
                        viewModel.setSettled(debt, settled)
                        if (settled && accounts.isNotEmpty()) {
                            moneyPrompt = MoneyPrompt(
                                debt.id.toString(),
                                debt.amount,
                                deposit = debt.type == DebtType.OWED_TO_ME.name,
                                description = "تسویه با ${counterparty.name}",
                            )
                        } else if (!settled) {
                            viewModel.unrecordMoney(debt.id.toString())
                        }
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
                onShowAddCounterpartyChange = {
                    if (it && !isPremium) ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("debts", "طلب و بدهی")
                    else showAddCounterparty = it
                },
                onAddCounterparty = { n ->
                    viewModel.addCounterparty(n.name, n.phone) { id ->
                        if (n.amountRial > 0) {
                            viewModel.addDebt(id, n.amountRial, n.type, n.note, n.year, n.month, n.day)
                        }
                    }
                },
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

/** فیلترِ فهرست - فریمِ `25a`. */
private enum class DebtFilter(val label: String) { ALL("همه"), OWED("طلب‌ها"), OWE("بدهی‌ها"), SETTLED("تسویه‌شده") }

/**
 * فهرستِ طرف‌حساب‌ها - بازطراحیِ فریمِ `25a`ِ ChatGPT: سه کاشیِ «طلب دارم / بدهکارم / خالص»،
 * قرص‌های فیلتر، ردیفِ طرف‌حساب با تاریخِ آخرین ردیف و مبلغِ رنگی، و دکمه‌ی اصلیِ «افزودن» که
 * برگه‌ی پایینِ `25b` را باز می‌کند (جای کارتِ ثابتِ وسطِ فهرست).
 */
@Composable
private fun DebtList(
    counterparties: List<CounterpartyEntity>,
    debts: List<DebtEntity>,
    onBack: () -> Unit,
    onOpen: (CounterpartyEntity) -> Unit,
    showAddCounterparty: Boolean,
    onShowAddCounterpartyChange: (Boolean) -> Unit,
    onAddCounterparty: (NewCounterparty) -> Unit,
    netBalance: (Long) -> Double,
    onOpenDang: () -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(DebtFilter.ALL) }
    val balances = remember(counterparties, debts) { counterparties.associate { it.id to netBalance(it.id) } }
    val totalOwed = balances.values.filter { it > 0 }.sum()
    val totalOwe = -balances.values.filter { it < 0 }.sum()
    val visibleCounterparties = remember(counterparties, searchQuery, filter, balances) {
        val q = searchQuery.trim()
        counterparties
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
            .filter {
                val bal = balances[it.id] ?: 0.0
                when (filter) {
                    DebtFilter.ALL -> true
                    DebtFilter.OWED -> bal > 0
                    DebtFilter.OWE -> bal < 0
                    DebtFilter.SETTLED -> bal == 0.0
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                Text("طلب و بدهی", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            // «دنگ» - کاشیِ واضح، نه دکمه‌ی خطیِ کم‌رنگ (بریفِ ۲۵).
            AppCard(modifier = Modifier.pressScaleClickable(onClick = onOpenDang)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("دنگ‌ها", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("تقسیمِ هزینه بینِ چند نفر", color = AppMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted)
                }
            }
        }
        if (counterparties.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryTile("طلب دارم", totalOwed, AppTxIn, privacyMode, Modifier.weight(1f))
                    SummaryTile("بدهکارم", totalOwe, AppTxOut, privacyMode, Modifier.weight(1f))
                    SummaryTile("خالص", totalOwed - totalOwe, if (totalOwed >= totalOwe) AppTxIn else AppTxOut, privacyMode, Modifier.weight(1f), signed = true)
                }
            }
            item {
                ir.sadteam.loancalc.ui.components.PillSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "جستجو تو طرف‌حساب‌ها...",
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    DebtFilter.entries.forEach { f ->
                        DebtTypeChip(label = f.label, selected = filter == f, modifier = Modifier.weight(1f)) { filter = f }
                    }
                }
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
                        "طرفِ‌حسابی با این شرط پیدا نشد."
                    },
                )
            }
        } else {
            items(visibleCounterparties, key = { it.id }) { counterparty ->
                val balance = balances[counterparty.id] ?: 0.0
                val last = debts.filter { it.counterpartyId == counterparty.id }.maxByOrNull { it.year * 10000 + it.month * 100 + it.day }
                val ink = when {
                    balance > 0 -> AppTxIn
                    balance < 0 -> AppTxOut
                    else -> AppMuted
                }
                AppCard(modifier = Modifier.pressScaleClickable { onOpen(counterparty) }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(avatar = counterparty.toAvatar(), size = 40.dp, modifier = Modifier.padding(end = 10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(counterparty.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                listOfNotNull(
                                    when {
                                        balance > 0 -> "طلبِ تو"
                                        balance < 0 -> "بدهیِ تو"
                                        else -> "تسویه"
                                    },
                                    last?.let { "آخرین: ${toFa(it.day)} ${persianMonthName(it.month)}" },
                                ).joinToString(" · "),
                                color = AppMuted,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        Text(
                            maskIfPrivate(privacyMode, rialToToman(kotlin.math.abs(balance).toLong()).toFaMoney() + " تومان"),
                            color = ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }

    // دکمه‌ی اصلیِ پایینِ صفحه (فریمِ `25a`).
    GradientButton(
        onClick = { onShowAddCounterpartyChange(true) },
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Text("افزودنِ طرفِ‌حساب", modifier = Modifier.padding(start = 6.dp))
    }

    AddCounterpartySheet(
        visible = showAddCounterparty,
        onDismiss = { onShowAddCounterpartyChange(false) },
        onSave = { onAddCounterparty(it); onShowAddCounterpartyChange(false) },
    )
    }
}

@Composable
private fun SummaryTile(label: String, rial: Double, ink: androidx.compose.ui.graphics.Color, privacyMode: Boolean, modifier: Modifier, signed: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ink.copy(alpha = 0.12f))
            .border(1.dp, ink.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Text(label, color = ink, fontSize = 11.sp)
        val toman = rialToToman(rial.toLong())
        Text(
            maskIfPrivate(privacyMode, (if (signed) toman.toFaSignedMoney() else toman.toFaMoney())),
            color = ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 3.dp),
        )
        Text("تومان", color = AppMuted, fontSize = 9.5.sp)
    }
}

/** داده‌ی برگه‌ی `25b`: مبلغ صفر یعنی فقط طرف‌حساب ساخته شود، بی ردیفِ طلب/بدهی. */
data class NewCounterparty(
    val name: String,
    val phone: String?,
    val amountRial: Double,
    val type: DebtType,
    val note: String,
    val year: Int,
    val month: Int,
    val day: Int,
)

/** برگه‌ی پایینِ «طرف‌حسابِ جدید» - فریمِ `25b`؛ اسکرول‌پذیر تا دکمه‌ی «افزودن» هرگز بریده نشود. */
@Composable
private fun BoxScope.AddCounterpartySheet(visible: Boolean, onDismiss: () -> Unit, onSave: (NewCounterparty) -> Unit) {
    androidx.compose.animation.AnimatedVisibility(visible = visible, enter = androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.fadeOut(), modifier = Modifier.matchParentSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
                .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null, onClick = onDismiss),
        )
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInVertically { it },
        exit = androidx.compose.animation.slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        var name by rememberSaveable { mutableStateOf("") }
        var phone by rememberSaveable { mutableStateOf("") }
        var type by rememberSaveable { mutableStateOf(DebtType.OWED_TO_ME) }
        var amount by rememberSaveable { mutableStateOf("") }
        var note by rememberSaveable { mutableStateOf("") }
        val today = remember { JalaliCalendar.today() }
        var y by rememberSaveable { mutableStateOf(today.y) }
        var m by rememberSaveable { mutableStateOf(today.m) }
        var d by rememberSaveable { mutableStateOf(today.d) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(AppSurface)
                .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {}
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(width = 42.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(AppLine))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(AppPrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Handshake, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp)) }
                Text("طرفِ‌حسابِ جدید", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(start = 10.dp))
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted) }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = AppMuted) },
                singleLine = true,
                shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                colors = appFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
            Ltr {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = cleanNum(it) },
                    label = { Text("موبایل (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    trailingIcon = { Icon(Icons.Filled.PhoneIphone, contentDescription = null, tint = AppMuted) },
                    singleLine = true,
                    shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                    colors = appFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                DirectionButton("من طلب دارم", type == DebtType.OWED_TO_ME, AppTxIn, Modifier.weight(1f), Icons.Filled.ArrowDownward) { type = DebtType.OWED_TO_ME }
                DirectionButton("من بدهکارم", type == DebtType.I_OWE, AppTxOut, Modifier.weight(1f), Icons.Filled.ArrowUpward) { type = DebtType.I_OWE }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = cleanNum(it).take(13) },
                label = { Text("مبلغ (اختیاری)") },
                suffix = { Text("تومان") },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                colors = appFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            amount.toLongOrNull()?.takeIf { it > 0 }?.let {
                Text("${numberToWordsFa(it.toDouble())} تومان", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
            if ((amount.toLongOrNull() ?: 0L) > 0) {
                Text("تاریخ", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                InlineJalaliDateRow(year = y, month = m, day = d, onDateChange = { yy, mm, dd -> y = yy; m = mm; d = dd })
                OutlinedTextField(value = note, onValueChange = { note = it.take(120) }, label = { Text("یادداشت (اختیاری)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            }
            GradientButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            NewCounterparty(
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                amountRial = tomanToRial(amount.toLongOrNull() ?: 0L).toDouble(),
                                type = type,
                                note = note.trim(),
                                year = y, month = m, day = d,
                            ),
                        )
                        name = ""; phone = ""; amount = ""; note = ""
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) { Text("افزودن") }
        }
    }
}

@Composable
private fun DirectionButton(
    label: String,
    selected: Boolean,
    ink: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) ink.copy(alpha = 0.16f) else AppChipBg)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) ink else AppLine, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = if (selected) ink else AppMuted, modifier = Modifier.size(18.dp).padding(end = 2.dp))
        Text(label, color = if (selected) ink else AppMuted, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun CounterpartyDetail(
    counterparty: CounterpartyEntity,
    debts: List<DebtEntity>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (CounterpartyEntity) -> Unit,
    onAddDebt: (amount: Double, type: DebtType, description: String, y: Int, m: Int, d: Int) -> Unit,
    onToggleSettled: (DebtEntity, Boolean) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var showAddDebt by rememberSaveable { mutableStateOf(false) }
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var confirmSettleDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var confirmDeleteDebt by remember { mutableStateOf<DebtEntity?>(null) }
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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                AvatarView(avatar = counterparty.toAvatar(), size = 32.dp, modifier = Modifier.padding(end = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(counterparty.name, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    val phone = counterparty.phone
                    if (!phone.isNullOrBlank()) {
                        Text(toFa(phone), color = AppMuted, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایشِ طرف‌حساب")
                }
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
                            suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    }
                    val amountRial = (amountText.toLongOrNull() ?: 0L) * 10
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
                        singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    InlineJalaliDateRow(
                        year = year,
                        month = month,
                        day = day,
                        onDateChange = { y, m, d -> year = y; month = m; day = d },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    GradientButton(
                        onClick = {
                            val amount = (amountText.toDoubleOrNull() ?: 0.0) * 10
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
                // پرداختِ تکه‌تکه (پیشنهادِ گزارشِ پولکی): یک ردیفِ مخالف با مانده ثبت می‌شود، پس
                // مانده‌ی خالص کم می‌شود و خودِ فهرستِ ردیف‌ها تاریخچه‌ی پرداخت‌هاست.
                val net = debts.filter { !it.settled }
                    .sumOf { if (it.type == DebtType.OWED_TO_ME.name) it.amount else -it.amount }
                if (net != 0.0) {
                    GradientButton(
                        onClick = {
                            type = if (net > 0) DebtType.I_OWE else DebtType.OWED_TO_ME
                            description = if (net > 0) "دریافتِ بخشی" else "پرداختِ بخشی"
                            showAddDebt = true
                        },
                        variant = AppButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Text(if (net > 0) "ثبتِ دریافتِ بخشی" else "ثبتِ پرداختِ بخشی")
                    }
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
                                    maskIfPrivate(privacyMode, "${fmt((debt.amount) / 10)} تومان"),
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
                            IconButton(
                                onClick = {
                                    if (debt.settled) onToggleSettled(debt, false) else confirmSettleDebt = debt
                                },
                            ) {
                                Text(if (debt.settled) "↺" else "✓", color = if (debt.settled) AppMuted else AppPrimary)
                            }
                            IconButton(onClick = { confirmDeleteDebt = debt }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                            }
                        }
                    }
                    HorizontalDivider(color = AppMuted.copy(alpha = 0.12f), modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
    if (showEdit) {
        EditCounterpartyDialog(
            counterparty = counterparty,
            onSave = { updated -> onUpdate(updated); showEdit = false },
            onDismiss = { showEdit = false },
        )
    }
    confirmSettleDebt?.let { debt ->
        ConfirmPayDialog(
            title = "ثبتِ تسویه",
            text = "«${fmt((debt.amount) / 10)} تومان» تسویه‌شده علامت بخوره؟",
            onConfirm = { onToggleSettled(debt, true) },
            onDismiss = { confirmSettleDebt = null },
        )
    }
    confirmDeleteDebt?.let { debt ->
        ConfirmDeleteDialog(
            title = "حذفِ ردیف",
            text = "این ردیفِ «${fmt((debt.amount) / 10)} تومان» حذف بشه؟",
            onConfirm = { onDeleteDebt(debt) },
            onDismiss = { confirmDeleteDebt = null },
        )
    }
}

/** ویرایشِ اسم/موبایل/شکلِ آدمکِ یه طرف‌حساب - فریمِ `22d`. رنگِ آدمک عمداً اینجا قابلِ‌تغییر
 * نیست (جوابِ سوالِ ۱۱: قطعی از رو اسمه، نه دستی). */
@Composable
private fun EditCounterpartyDialog(
    counterparty: CounterpartyEntity,
    onSave: (CounterpartyEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(counterparty.name) }
    var phone by remember { mutableStateOf(counterparty.phone ?: "") }
    var shape by remember { mutableStateOf(runCatching { AvatarShape.valueOf(counterparty.avatarShape) }.getOrDefault(AvatarShape.BOY)) }

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایشِ طرف‌حساب") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                Ltr {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = cleanNum(it) },
                        label = { Text("موبایل (اختیاری)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                }
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    AvatarShape.entries.forEach { s ->
                        TextButton(onClick = { shape = s }) {
                            AvatarView(
                                avatar = Avatar(shape = s, color = runCatching { AvatarColor.valueOf(counterparty.avatarColor) }.getOrDefault(AvatarColor.NEUTRAL)),
                                size = if (shape == s) 44.dp else 38.dp,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(counterparty.copy(name = name.trim(), phone = phone.trim().takeIf { it.isNotBlank() }, avatarShape = shape.name))
                    }
                },
            ) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

private fun CounterpartyEntity.toAvatar(): Avatar = Avatar(
    shape = runCatching { AvatarShape.valueOf(avatarShape) }.getOrDefault(AvatarShape.BOY),
    color = runCatching { AvatarColor.valueOf(avatarColor) }.getOrDefault(AvatarColor.NEUTRAL),
)

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
