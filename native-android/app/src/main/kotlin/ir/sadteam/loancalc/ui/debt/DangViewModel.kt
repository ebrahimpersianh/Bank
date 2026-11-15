package ir.sadteam.loancalc.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.DangMethod
import ir.sadteam.loancalc.data.DangItemInput
import ir.sadteam.loancalc.data.DangParticipantInput
import ir.sadteam.loancalc.data.DangRepository
import ir.sadteam.loancalc.data.db.DangEventEntity
import ir.sadteam.loancalc.data.db.DangItemEntity
import ir.sadteam.loancalc.data.db.DangParticipantEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DangViewModel @Inject constructor(
    private val dangRepository: DangRepository,
    private val accountRepository: ir.sadteam.loancalc.data.AccountRepository,
) : ViewModel() {
    val events: StateFlow<List<DangEventEntity>> = dangRepository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** همه‌ی سهم‌ها (همه‌ی دنگ‌ها) - طلبِ دنگ در «طلب و بدهی» از همین حساب می‌شود. */
    val allParticipants: StateFlow<List<DangParticipantEntity>> = dangRepository.observeAllParticipants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun loadForEdit(eventId: Long) = dangRepository.loadForEdit(eventId)

    fun participants(eventId: Long): Flow<List<DangParticipantEntity>> = dangRepository.observeParticipants(eventId)
    fun items(eventId: Long): Flow<List<DangItemEntity>> = dangRepository.observeItems(eventId)

    fun createEvent(
        title: String,
        method: DangMethod,
        totalAmount: Double,
        year: Int,
        month: Int,
        day: Int,
        isEventMode: Boolean,
        participants: List<DangParticipantInput>,
        items: List<DangItemInput> = emptyList(),
        onSaved: () -> Unit,
    ) {
        ir.sadteam.loancalc.data.UsageStats.action("dang_created")
        viewModelScope.launch {
            dangRepository.createEvent(title, method, totalAmount, year, month, day, isEventMode, participants, items)
            onSaved()
        }
    }

    fun updateEvent(
        event: DangEventEntity,
        title: String,
        method: DangMethod,
        totalAmount: Double,
        year: Int,
        month: Int,
        day: Int,
        isEventMode: Boolean,
        participants: List<DangParticipantInput>,
        items: List<DangItemInput> = emptyList(),
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            val (removed, changed) = dangRepository.updateEvent(event, title, method, totalAmount, year, month, day, isEventMode, participants, items)
            // پولی که با سهمِ حذف‌شده جابه‌جا شده بود برمی‌گردد؛ سهمِ تسویه‌شده‌ی تغییرکرده مبلغِ تازه می‌گیرد.
            removed.forEach { accountRepository.removeLinkedPaymentsByPrefix("dang", "dang-$it@") }
            changed.forEach { accountRepository.updateLinkedAmountByPrefix("dang", "dang-${it.id}@", it.shareAmount) }
            onSaved()
        }
    }

    fun setEventSettled(event: DangEventEntity, settled: Boolean) {
        viewModelScope.launch { dangRepository.setEventSettled(event, settled) }
    }

    /** «همه دادند» - هر سهمی که تازه تیک خورد به [onNewlySettled] می‌رود تا پولش در حساب ثبت شود. */
    fun settleAll(event: DangEventEntity, onNewlySettled: (List<DangParticipantEntity>) -> Unit) {
        viewModelScope.launch { onNewlySettled(dangRepository.settleAllOthers(event)) }
    }

    fun setParticipantSettled(participant: DangParticipantEntity, settled: Boolean) {
        viewModelScope.launch { dangRepository.setParticipantSettled(participant, settled) }
    }

    fun deleteEvent(event: DangEventEntity) {
        viewModelScope.launch { dangRepository.deleteEvent(event) }
    }
}
