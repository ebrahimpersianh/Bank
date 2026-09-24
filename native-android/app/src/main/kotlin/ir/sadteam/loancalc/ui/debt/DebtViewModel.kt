package ir.sadteam.loancalc.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DebtEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
) : ViewModel() {
    val counterparties: StateFlow<List<CounterpartyEntity>> = debtRepository.observeCounterparties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = debtRepository.observeDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun netBalance(counterpartyId: Long, allDebts: List<DebtEntity>): Double =
        debtRepository.netBalance(counterpartyId, allDebts)

    fun addCounterparty(name: String, phone: String? = null, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch { onResult(debtRepository.addCounterparty(name, phone)) }
    }

    fun updateCounterparty(counterparty: CounterpartyEntity) {
        viewModelScope.launch { debtRepository.updateCounterparty(counterparty) }
    }

    fun deleteCounterparty(counterparty: CounterpartyEntity) {
        viewModelScope.launch { debtRepository.deleteCounterparty(counterparty) }
    }

    fun addDebt(
        counterpartyId: Long,
        amount: Double,
        type: DebtType,
        description: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        viewModelScope.launch {
            debtRepository.addDebt(counterpartyId, amount, type, description, year, month, day)
        }
    }

    fun setSettled(debt: DebtEntity, settled: Boolean) {
        viewModelScope.launch { debtRepository.setSettled(debt, settled) }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { debtRepository.deleteDebt(debt) }
    }
}
