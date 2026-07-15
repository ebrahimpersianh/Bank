package ir.sadteam.loancalc.ui.cheque

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.data.AttachmentStorage
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.data.db.ChequeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChequeViewModel @Inject constructor(
    private val chequeRepository: ChequeRepository,
    private val attachmentStorage: AttachmentStorage,
) : ViewModel() {
    val cheques: StateFlow<List<ChequeEntity>> = chequeRepository.observeCheques()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chequeBooks: StateFlow<List<ChequeBookEntity>> = chequeRepository.observeChequeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCheque(
        type: ChequeType,
        amount: Double,
        chequeNumber: String,
        bankName: String,
        branchName: String,
        ownerName: String,
        dueYear: Int,
        dueMonth: Int,
        dueDay: Int,
        notes: String,
        chequeBookId: Long?,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            chequeRepository.addCheque(
                type, amount, chequeNumber, bankName, branchName, ownerName,
                dueYear, dueMonth, dueDay, notes, chequeBookId,
            )
            onSaved()
        }
    }

    fun updateCheque(cheque: ChequeEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            chequeRepository.updateCheque(cheque)
            onSaved()
        }
    }

    fun setStatus(cheque: ChequeEntity, status: ChequeStatus) {
        viewModelScope.launch { chequeRepository.setStatus(cheque, status) }
    }

    fun setArchived(cheque: ChequeEntity, archived: Boolean) {
        viewModelScope.launch { chequeRepository.setArchived(cheque, archived) }
    }

    fun deleteCheque(id: Long) {
        viewModelScope.launch { chequeRepository.deleteCheque(id) }
    }

    /** پورت «پیوست عکس رسید» اپ رقیب - عکس انتخابی رو به فضای داخلی اپ کپی می‌کنه، عکس قبلی (اگه بود)
     * رو پاک می‌کنه، و مسیر جدید رو رو خودِ چک ذخیره می‌کنه. */
    fun setChequePhoto(cheque: ChequeEntity, uri: Uri) {
        viewModelScope.launch {
            val newPath = attachmentStorage.copyToInternalStorage(uri) ?: return@launch
            attachmentStorage.delete(cheque.photoPath)
            chequeRepository.updateCheque(cheque.copy(photoPath = newPath))
        }
    }

    fun removeChequePhoto(cheque: ChequeEntity) {
        viewModelScope.launch {
            attachmentStorage.delete(cheque.photoPath)
            chequeRepository.updateCheque(cheque.copy(photoPath = null))
        }
    }

    fun addChequeBook(ownerName: String, bankName: String, startSerial: Long, endSerial: Long) {
        viewModelScope.launch { chequeRepository.addChequeBook(ownerName, bankName, startSerial, endSerial) }
    }

    fun deleteChequeBook(book: ChequeBookEntity) {
        viewModelScope.launch { chequeRepository.deleteChequeBook(book) }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(chequeRepository.exportBackupJson()) }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(chequeRepository.importBackupJson(json)) }
    }
}
