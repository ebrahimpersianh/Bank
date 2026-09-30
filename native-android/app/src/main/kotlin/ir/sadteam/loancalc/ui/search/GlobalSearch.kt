package ir.sadteam.loancalc.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.NoteRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.BillDao
import ir.sadteam.loancalc.ui.account.AccountDetailScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDashedBorder
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppWarning
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** جستجوی کلیِ برنامه (خواسته‌ی کاربر ۷ مهر). همه‌چیز روی گوشی؛ چیزی به سرور نمی‌رود. */
enum class SearchKind(val label: String, val icon: ImageVector) {
    TX("تراکنش", Icons.Filled.SwapHoriz),
    LOAN("وام", Icons.Filled.Payments),
    CHEQUE("چک", Icons.Filled.Description),
    ACCOUNT("حساب", Icons.Filled.AccountBalance),
    PERSON("طرف‌حساب", Icons.Filled.Person),
    BILL("قبض", Icons.Filled.Receipt),
    NOTE("یادداشت", Icons.Filled.StickyNote2),
}

data class SearchHit(
    val kind: SearchKind,
    val id: Long,
    val title: String,
    val subtitle: String,
    val amountRial: Double?,
    /** متنی که جستجو رویش انجام می‌شود (نرمال‌شده). */
    val haystack: String,
    val accountId: Long? = null,
)

/** ی/ک عربی، رقمِ فارسی/عربی، نیم‌فاصله و فاصله را یکی می‌کند تا «بانک‌ملت» و «بانك ملت» هر دو پیدا شوند. */
internal fun normalizeForSearch(s: String): String = buildString {
    for (c in s.lowercase()) {
        when (c) {
            'ي', 'ى' -> append('ی')
            'ك' -> append('ک')
            'ة' -> append('ه')
            'أ', 'إ', 'آ' -> append('ا')
            in '۰'..'۹' -> append('0' + (c - '۰'))
            in '٠'..'٩' -> append('0' + (c - '٠'))
            '‌', ' ', '-', '_', '٬', ',' -> Unit
            else -> append(c)
        }
    }
}

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    accountRepository: AccountRepository,
    loanRepository: LoanRepository,
    chequeRepository: ChequeRepository,
    debtRepository: DebtRepository,
    noteRepository: NoteRepository,
    billDao: BillDao,
) : ViewModel() {
    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val finance = combine(
        accountRepository.observeAccounts(),
        accountRepository.observeTransactions(),
        loanRepository.observeLoans(),
        chequeRepository.observeCheques(),
    ) { accs, txs, loans, cheques ->
        val accName = accs.associate { it.id to it.name }
        buildList {
            accs.forEach { a ->
                add(SearchHit(SearchKind.ACCOUNT, a.id, a.name, a.bankName, null,
                    normalizeForSearch("${a.name} ${a.bankName} ${a.cardNumber.orEmpty()} ${a.accountNumber.orEmpty()} ${a.sheba.orEmpty()}")))
            }
            txs.sortedByDescending { it.year * 10_000 + it.month * 100 + it.day }.forEach { t ->
                val title = t.description.ifBlank { t.category ?: "تراکنش" }
                add(SearchHit(SearchKind.TX, t.id, title,
                    "${toFa(t.year)}/${toFa(t.month)}/${toFa(t.day)} · ${accName[t.accountId] ?: ""}" + (t.category?.let { " · $it" } ?: ""),
                    t.amount,
                    normalizeForSearch("${t.description} ${t.category.orEmpty()} ${t.tags.orEmpty()} ${accName[t.accountId].orEmpty()} ${t.amount.toLong()} ${(t.amount / 10).toLong()}"),
                    accountId = t.accountId))
            }
            loans.forEach { l ->
                add(SearchHit(SearchKind.LOAN, l.id, l.name, l.bank, l.amount,
                    normalizeForSearch("${l.name} ${l.bank} ${l.amount.toLong()} ${(l.amount / 10).toLong()}")))
            }
            cheques.forEach { c ->
                add(SearchHit(SearchKind.CHEQUE, c.id, c.ownerName.ifBlank { "چک ${c.chequeNumber}" },
                    "${c.bankName} · ${toFa(c.dueYear)}/${toFa(c.dueMonth)}/${toFa(c.dueDay)}", c.amount,
                    normalizeForSearch("${c.ownerName} ${c.bankName} ${c.branchName} ${c.chequeNumber} ${c.sayadId.orEmpty()} ${c.notes} ${c.amount.toLong()} ${(c.amount / 10).toLong()}")))
            }
        }
    }

    private val others = combine(
        debtRepository.observeCounterparties(),
        billDao.observeAll(),
        noteRepository.observeNotes(),
    ) { people, bills, notes ->
        buildList {
            people.forEach { p ->
                add(SearchHit(SearchKind.PERSON, p.id, p.name, p.phone?.let { toFa(it) } ?: "طلب و بدهی", null,
                    normalizeForSearch("${p.name} ${p.phone.orEmpty()}")))
            }
            bills.forEach { b ->
                add(SearchHit(SearchKind.BILL, b.id, b.name, "سررسید روزِ ${toFa(b.dueDay)}", null,
                    normalizeForSearch("${b.name} ${b.billId.orEmpty()}")))
            }
            notes.forEach { n ->
                add(SearchHit(SearchKind.NOTE, n.id, n.text.lineSequence().firstOrNull().orEmpty().take(60),
                    "${toFa(n.year)}/${toFa(n.month)}/${toFa(n.day)}", null, normalizeForSearch(n.text)))
            }
        }
    }

    val index: StateFlow<List<SearchHit>> = combine(finance, others) { a, b -> a + b }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun GlobalSearchScreen(
    onClose: () -> Unit,
    onOpenLoan: (Long) -> Unit,
    onOpenCheque: (Long) -> Unit,
    onOpenPerson: (Long) -> Unit,
    onOpenBills: () -> Unit,
    onOpenNotes: () -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel(),
) {
    val index by viewModel.index.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val privacy = LocalPrivacyMode.current
    var query by rememberSaveable { mutableStateOf("") }
    var kindFilter by rememberSaveable { mutableStateOf<SearchKind?>(null) }
    var openAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    val openAccount = openAccountId?.let { id -> accounts.firstOrNull { it.id == id } }
    if (openAccount != null) {
        Box(Modifier.fillMaxSize()) {
            AccountDetailScreen(account = openAccount, onBack = { openAccountId = null })
        }
        BackHandler { openAccountId = null }
        return
    }

    val q = remember(query) { normalizeForSearch(query.trim()) }
    val allHits = remember(q, index) {
        if (q.length < 2) emptyList() else index.filter { it.haystack.contains(q) }
    }
    val kindsFound = remember(allHits) { allHits.map { it.kind }.distinct() }
    // فیلترِ نوعی که در نتیجه‌ی تازه نیست، خودبه‌خود «همه» می‌شود.
    val activeKind = kindFilter?.takeIf { it in kindsFound }
    val hits = remember(allHits, activeKind) {
        allHits.filter { activeKind == null || it.kind == activeKind }.take(200)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.ArrowForward, contentDescription = "بستن", tint = AppText) }
            Text("جستجو", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        // کادرِ جستجو هم‌سبکِ جستجوی «وام‌های من»: حاشیه‌ی سبز، نه طلایی (طلایی فقط نشانِ اشتراک است).
        ir.sadteam.loancalc.ui.components.PillSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "جستجو در همه‌ی برنامه…",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            textFieldModifier = Modifier.focusRequester(focus),
        )
        if (kindsFound.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterPill("همه (${toFa(allHits.size)})", activeKind == null) { kindFilter = null }
                kindsFound.forEach { k ->
                    FilterPill("${k.label} (${toFa(allHits.count { it.kind == k })})", activeKind == k) { kindFilter = k }
                }
            }
        }
        when {
            q.length < 2 -> SearchIntro()
            hits.isEmpty() -> SearchMessage(
                icon = Icons.Filled.SearchOff,
                title = "چیزی پیدا نشد",
                body = "با «${query.trim()}» هیچ تراکنش، وام، چک یا حسابی پیدا نکردیم. املای دیگری را امتحان کن.",
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(hits, key = { "${it.kind}-${it.id}" }) { hit ->
                    val tint = hit.kind.tint()
                    AppCard(
                        modifier = Modifier.clickable {
                            when (hit.kind) {
                                SearchKind.TX -> openAccountId = hit.accountId
                                SearchKind.ACCOUNT -> openAccountId = hit.id
                                SearchKind.LOAN -> onOpenLoan(hit.id)
                                SearchKind.CHEQUE -> onOpenCheque(hit.id)
                                SearchKind.PERSON -> onOpenPerson(hit.id)
                                SearchKind.BILL -> onOpenBills()
                                SearchKind.NOTE -> onOpenNotes()
                            }
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Icon(hit.kind.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(hit.title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Text(
                                        hit.kind.label,
                                        color = tint,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(tint.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                    Text(
                                        hit.subtitle,
                                        color = AppMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 6.dp),
                                    )
                                }
                            }
                            hit.amountRial?.let { rial ->
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                                    Text(maskIfPrivate(privacy, toFa(fmt(rial / 10))), color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Text("تومان", color = AppMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchKind.tint(): Color = when (this) {
    SearchKind.TX -> AppPrimary
    SearchKind.LOAN -> AppPurple
    SearchKind.CHEQUE -> AppInfo
    SearchKind.ACCOUNT -> AppPrimary
    SearchKind.PERSON -> AppWarning
    SearchKind.BILL -> AppDanger
    SearchKind.NOTE -> AppWarning
}

/** حالتِ پیش از تایپ: کارتِ خط‌چینِ سبکِ حالت‌های خالیِ برنامه + این‌که کجاها را می‌گردد. */
@Composable
private fun SearchIntro() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        SearchMessage(
            icon = Icons.Filled.Search,
            title = "دنبالِ چی می‌گردی؟",
            body = "اسمِ بانک، طرف‌حساب، شرحِ تراکنش، مبلغ، شماره‌ی چک یا کارت را بنویس.",
            padded = false,
        )
        Text(
            "جستجو در این بخش‌ها",
            color = AppLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
        )
        SearchKind.values().toList().chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k ->
                    val tint = k.tint()
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(tint.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(k.icon, contentDescription = null, tint = tint, modifier = Modifier.size(23.dp)) }
                        Text(k.label, color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SearchMessage(icon: ImageVector, title: String, body: String, padded: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (padded) 16.dp else 0.dp)
            .dashedBorder(radius = 24.dp, color = AppDashedBorder, width = 1.5.dp)
            .padding(horizontal = 20.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(32.dp)) }
        Text(title, color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 14.dp))
        Text(
            body,
            color = AppMuted,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) AppPrimary else AppChipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, color = if (selected) Color.White else AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
