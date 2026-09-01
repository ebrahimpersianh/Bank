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
) : ViewModel() {
    val events: StateFlow<List<DangEventEntity>> = dangRepository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        viewModelScope.launch {
            dangRepository.createEvent(title, method, totalAmount, year, month, day, isEventMode, participants, items)
            onSaved()
        }
    }

    fun setEventSettled(event: DangEventEntity, settled: Boolean) {
        viewModelScope.launch { dangRepository.setEventSettled(event, settled) }
    }

    fun setParticipantSettled(participant: DangParticipantEntity, settled: Boolean) {
        viewModelScope.launch { dangRepository.setParticipantSettled(participant, settled) }
    }

    fun deleteEvent(event: DangEventEntity) {
        viewModelScope.launch { dangRepository.deleteEvent(event) }
    }
}
