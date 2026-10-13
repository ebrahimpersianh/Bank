package ir.sadteam.loancalc.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val inbox: InboxRepository,
    private val accounts: AccountRepository,
    private val api: ApiService,
    private val authPrefs: ir.sadteam.loancalc.data.prefs.AuthPrefs,
    private val categories: ir.sadteam.loancalc.data.CategoryRepository,
) : ViewModel() {

    init {
        // هر بار که صفحه‌ی پیام‌ها باز می‌شود، اطلاعیه‌های تازه‌ی جیبک گرفته می‌شوند (بی‌صدا در خطا).
        viewModelScope.launch {
            runCatching {
                val auth = authPrefs.authToken.first()?.let { "Bearer $it" }
                inbox.mergeAnnouncements(api.getAnnouncements(authHeader = auth).items)
            }
        }
    }

    val messages: StateFlow<List<InboxMessageEntity>> = inbox.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * ۲۱ مهر (خواسته‌ی کاربر): دسته‌ی فعلیِ تراکنشِ هر پیامِ «برداشت/واریزِ تازه» (کلید: txId)، تا بعد
     * از انتخابِ دسته، متنِ کارت به‌جای «دسته‌بندیش نامشخصه…» اسم و آیکونِ دسته را نشان دهد. از خودِ
     * تراکنش خوانده می‌شود، پس انتخاب از هر جا (پنجره‌ی دسته، اعلان، صفحه‌ی تراکنش) دیده می‌شود.
     */
    val txCategories: StateFlow<Map<Long, TxCategoryLabel>> = kotlinx.coroutines.flow.combine(
        accounts.observeTransactions(),
        categories.orderedCategories(ir.sadteam.loancalc.core.TransactionType.WITHDRAWAL),
        categories.orderedCategories(ir.sadteam.loancalc.core.TransactionType.DEPOSIT),
    ) { txs, out, inc ->
        val byKey = (out + inc).associateBy { it.type.name to it.name }
        txs.mapNotNull { t ->
            val label = if (t.sourceType == ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER) {
                TxCategoryLabel("جابه‌جایی بینِ حساب‌های خودم", androidx.compose.material.icons.Icons.Filled.SwapHoriz, null, t.confirmed)
            } else {
                // «سایر…» یعنی هنوز دسته‌ای انتخاب نشده - همان متنِ اصلیِ پیام می‌ماند.
                val c = t.category?.takeIf { !it.startsWith("سایر") }?.let { byKey[t.type to it] } ?: return@mapNotNull null
                TxCategoryLabel(c.name, c.icon, c.color, t.confirmed)
            }
            t.id to label
        }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** عددِ روی زنگ - فقط اقدام‌دارهای باز. */
    val actionableCount: StateFlow<Int> = inbox.observeActionableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** نقطه‌ی سبز - خبرِ خوانده‌نشده. */
    val unreadNews: StateFlow<Int> = inbox.observeUnreadNewsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * حسابی که پیامِ لمس‌شده به آن می‌رسد - «رفتن به منبع».
     *
     * از `refId` (شناسه‌ی تراکنشِ تاییدنشده) به حسابش می‌رسیم؛ اگر تراکنش پاک شده باشد
     * `null` می‌ماند و کارت فقط متنِ خام را نشان می‌دهد.
     */
    private val _sourceAccount = MutableStateFlow<AccountEntity?>(null)
    val sourceAccount: StateFlow<AccountEntity?> = _sourceAccount.asStateFlow()

    fun openSourceAccount(message: InboxMessageEntity) = viewModelScope.launch {
        val txId = message.refId?.toLongOrNull() ?: return@launch
        val accountId = accounts.transactionById(txId)?.accountId ?: return@launch
        _sourceAccount.value = accounts.observeAccounts().first().firstOrNull { it.id == accountId }
    }

    fun closeSourceAccount() { _sourceAccount.value = null }

    fun markRead(id: Long) = viewModelScope.launch {
        inbox.markRead(id)
        // پیامِ جیبک (شناسه‌ی منفی = شناسه‌ی سرور): «دیده شد» برای آمارِ پیامِ گروهیِ ادمین.
        if (id < 0) runCatching {
            val auth = authPrefs.authToken.first()?.let { "Bearer $it" } ?: return@runCatching
            api.markAnnouncementRead(-id, auth)
        }
    }

    fun markAllNewsRead() = viewModelScope.launch { inbox.markAllNewsRead() }

    /**
     * تاییدِ تراکنشِ تشخیص‌داده‌شده - از این لحظه رو موجودی اثر می‌ذاره.
     * ⚠️ اول تراکنش تایید می‌شه بعد پیام بسته می‌شه، تا اگه وسطش چیزی خطا داد پیام باز بمونه
     * و کاربر دوباره ببیندش (نه اینکه پیام بسته بشه و تراکنش معلق بمونه).
     */
    fun confirmTransaction(message: InboxMessageEntity) = viewModelScope.launch {
        message.refId?.toLongOrNull()?.let { accounts.confirmTransaction(it) }
        inbox.resolve(message.id, done = true)
    }

    /** ردِ تراکنش - خودِ تراکنشِ تاییدنشده هم پاک می‌شه، وگرنه برای همیشه معلق می‌مونه. */
    fun rejectTransaction(message: InboxMessageEntity) = viewModelScope.launch {
        message.refId?.toLongOrNull()?.let { id ->
            // فقط تراکنشِ هنوز-تاییدنشده پاک می‌شود؛ اگر در این فاصله با پرداختِ دستیِ قسط/چک/قبض
            // یکی شده (تأییدشده)، «نه» نباید پرداختِ واقعی را پاک کند.
            accounts.transactionById(id)?.takeIf { !it.confirmed }?.let {
                SmsRecycleBin.add(context, it)
                accounts.deleteTransaction(it)
            }
        }
        inbox.resolve(message.id, done = false)
        _bin.value = SmsRecycleBin.all(context)
    }

    private val _bin = MutableStateFlow(SmsRecycleBin.all(context))
    val recycleBin: StateFlow<List<SmsRecycleBin.Item>> = _bin.asStateFlow()

    /** برگرداندن از سطل‌زباله - این بار **تأییدشده**، چون کاربر خودش صریحاً برش گرداند. */
    fun restore(item: SmsRecycleBin.Item) = viewModelScope.launch {
        val t = item.tx
        accounts.addTransaction(
            accountId = t.accountId,
            type = ir.sadteam.loancalc.core.TransactionType.valueOf(t.type),
            amount = t.amount,
            description = t.description,
            year = t.year, month = t.month, day = t.day,
            category = t.category,
            id = t.id,
            originLabel = t.originLabel,
        )
        SmsRecycleBin.remove(context, t.id)
        _bin.value = SmsRecycleBin.all(context)
    }

    /**
     * ۲۱ مهر: «موجودی با بانک فرق دارد» ← ثبتِ همان اختلاف به‌عنوانِ یک تراکنش، تا موجودیِ جیبک با
     * بانک یکی شود. بانک بیشتر دارد = واریز؛ کمتر = برداشت. پیام «انجام شد» می‌شود تا دوباره ثبت نشود.
     */
    fun fixBalanceGap(message: InboxMessageEntity) = viewModelScope.launch {
        if (message.actionState == InboxMessageEntity.ActionState.DONE) return@launch
        val gap = parseBalanceGap(message) ?: return@launch
        val all = accounts.observeAccounts().first()
        val account = all.firstOrNull { it.id == gap.accountId } ?: all.firstOrNull { it.name == gap.accountName } ?: return@launch
        val today = ir.sadteam.loancalc.core.JalaliCalendar.today()
        inbox.resolve(message.id, done = true)
        accounts.addTransaction(
            accountId = account.id,
            type = if (gap.gapRial > 0) ir.sadteam.loancalc.core.TransactionType.DEPOSIT else ir.sadteam.loancalc.core.TransactionType.WITHDRAWAL,
            amount = kotlin.math.abs(gap.gapRial),
            description = "اختلاف با مانده‌ی بانک",
            year = today.y, month = today.m, day = today.d,
        )
    }

    fun dismiss(message: InboxMessageEntity) = viewModelScope.launch {
        if (InboxMessageEntity.Kind.isActionable(message.kind)) {
            inbox.resolve(message.id, done = false)
        } else {
            inbox.delete(message.id)
        }
    }
}

/** دسته‌ی تراکنشِ پشتِ یک پیامِ «برداشت/واریزِ تازه» - برای جایگزینیِ متنِ «دسته‌بندیش نامشخصه». `color` تهی = رنگِ اصلی. */
data class TxCategoryLabel(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color?,
    val confirmed: Boolean,
)
