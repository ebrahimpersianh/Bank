package ir.sadteam.loancalc.ui.inbox

import androidx.lifecycle.ViewModel
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
    private val inbox: InboxRepository,
    private val accounts: AccountRepository,
    private val api: ApiService,
    private val authPrefs: ir.sadteam.loancalc.data.prefs.AuthPrefs,
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

    fun markRead(id: Long) = viewModelScope.launch { inbox.markRead(id) }

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
            accounts.transactionById(id)?.let { accounts.deleteTransaction(it) }
        }
        inbox.resolve(message.id, done = false)
    }

    fun dismiss(message: InboxMessageEntity) = viewModelScope.launch {
        if (InboxMessageEntity.Kind.isActionable(message.kind)) {
            inbox.resolve(message.id, done = false)
        } else {
            inbox.delete(message.id)
        }
    }
}
