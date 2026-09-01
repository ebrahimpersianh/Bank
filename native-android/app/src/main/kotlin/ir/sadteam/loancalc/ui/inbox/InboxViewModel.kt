package ir.sadteam.loancalc.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val inbox: InboxRepository,
    private val accounts: AccountRepository,
) : ViewModel() {

    val messages: StateFlow<List<InboxMessageEntity>> = inbox.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** عددِ روی زنگ - فقط اقدام‌دارهای باز. */
    val actionableCount: StateFlow<Int> = inbox.observeActionableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** نقطه‌ی سبز - خبرِ خوانده‌نشده. */
    val unreadNews: StateFlow<Int> = inbox.observeUnreadNewsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
