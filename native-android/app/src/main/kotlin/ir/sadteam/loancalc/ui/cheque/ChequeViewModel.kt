package ir.sadteam.loancalc.ui.cheque

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.ChequeRiskScore
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.data.AttachmentStorage
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChequeViewModel @Inject constructor(
    private val chequeRepository: ChequeRepository,
    private val attachmentStorage: AttachmentStorage,
    private val authPrefs: AuthPrefs,
) : ViewModel() {
    /** پورت syncIfLoggedIn تو MyLoansViewModel - چک‌ها/دسته‌چک‌ها قبلاً فقط با AutoBackupWorkerِ
     * روزانه سینک می‌شدن، نه بعد از هر تغییر؛ کاربر خواسته با کوچیک‌ترین تغییری هم بی‌صدا آنلاین
     * بکاپ بگیره، دقیقاً مثل وام‌ها. */
    private suspend fun syncIfLoggedIn() {
        val token = authPrefs.authToken.first()
        if (!token.isNullOrEmpty()) chequeRepository.pushToServer(token)
    }
    val cheques: StateFlow<List<ChequeEntity>> = chequeRepository.observeCheques()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chequeBooks: StateFlow<List<ChequeBookEntity>> = chequeRepository.observeChequeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCheque(
        type: ChequeType,
        amount: Double,
        chequeNumber: String,
        sayadId: String,
        bankName: String,
        branchName: String,
        ownerName: String,
        dueYear: Int,
        dueMonth: Int,
        dueDay: Int,
        notes: String,
        chequeBookId: Long?,
        photoPath: String?,
        nationalId: String?,
        previousBalance: Double?,
        depositAmount: Double?,
        counterpartyId: Long? = null,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            chequeRepository.addCheque(
                type, amount, chequeNumber, sayadId, bankName, branchName, ownerName,
                dueYear, dueMonth, dueDay, notes, chequeBookId,
                photoPath, nationalId, previousBalance, depositAmount, counterpartyId,
            )
            syncIfLoggedIn()
            onSaved()
        }
    }

    /** پیک‌کردن عکسِ رسید تو خودِ فرمِ افزودن چک (قبل از این‌که چک هنوز ذخیره شده باشه، پس id نداره) -
     * عکس فوراً به فضای داخلی اپ کپی می‌شه و مسیرش برمی‌گرده تا فرم موقتاً تو state خودش نگهش داره؛
     * وقتی کاربر «ذخیره چک» رو زد همین مسیر مستقیم تو [addCheque] پاس داده می‌شه. */
    fun pickPhotoForNewCheque(uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch { onResult(attachmentStorage.copyToInternalStorage(uri)) }
    }

    /** اگه کاربر تو فرمِ افزودن یه عکس پیک کرد ولی بعد حذفش کرد/فرم رو کنسل کرد، فایلِ یتیمِ کپی‌شده
     * تو فضای داخلی اپ رو پاک می‌کنه (چون هیچ چکی بهش اشاره نمی‌کنه). */
    fun deleteOrphanPhoto(path: String?) {
        viewModelScope.launch { attachmentStorage.delete(path) }
    }

    fun updateCheque(cheque: ChequeEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            chequeRepository.updateCheque(cheque)
            syncIfLoggedIn()
            onSaved()
        }
    }

    fun setStatus(cheque: ChequeEntity, status: ChequeStatus) {
        viewModelScope.launch {
            chequeRepository.setStatus(cheque, status)
            syncIfLoggedIn()
        }
    }

    fun setArchived(cheque: ChequeEntity, archived: Boolean) {
        viewModelScope.launch {
            chequeRepository.setArchived(cheque, archived)
            syncIfLoggedIn()
        }
    }

    fun deleteCheque(id: Long) {
        viewModelScope.launch {
            chequeRepository.deleteCheque(id)
            syncIfLoggedIn()
        }
    }

    /** پورت «پیوست عکس رسید» اپ رقیب - عکس انتخابی رو به فضای داخلی اپ کپی می‌کنه، عکس قبلی (اگه بود)
     * رو پاک می‌کنه، و مسیر جدید رو رو خودِ چک ذخیره می‌کنه. */
    fun setChequePhoto(cheque: ChequeEntity, uri: Uri) {
        viewModelScope.launch {
            val newPath = attachmentStorage.copyToInternalStorage(uri) ?: return@launch
            attachmentStorage.delete(cheque.photoPath)
            chequeRepository.updateCheque(cheque.copy(photoPath = newPath))
            syncIfLoggedIn()
        }
    }

    fun removeChequePhoto(cheque: ChequeEntity) {
        viewModelScope.launch {
            attachmentStorage.delete(cheque.photoPath)
            chequeRepository.updateCheque(cheque.copy(photoPath = null))
            syncIfLoggedIn()
        }
    }

    fun addChequeBook(
        ownerName: String,
        bankName: String,
        startSerial: Long,
        endSerial: Long,
        sayadId: String? = null,
        last4: String? = null,
    ) {
        viewModelScope.launch {
            chequeRepository.addChequeBook(ownerName, bankName, startSerial, endSerial, sayadId, last4)
            syncIfLoggedIn()
        }
    }

    fun deleteChequeBook(book: ChequeBookEntity) {
        viewModelScope.launch {
            chequeRepository.deleteChequeBook(book)
            syncIfLoggedIn()
        }
    }

    /** بستنِ یه دسته‌چکِ تمام‌شده - فریمِ `29k`. */
    fun closeChequeBook(book: ChequeBookEntity) {
        viewModelScope.launch {
            chequeRepository.closeChequeBook(book)
            syncIfLoggedIn()
        }
    }

    /** یادآوریِ اختصاصیِ این چک رو ست می‌کنه - null یعنی از پیش‌فرضِ سراسری استفاده کن، رشته‌ی خالی
     * یعنی برای این چک کاملاً خاموش باشه، وگرنه CSVِ روزهای انتخاب‌شده. رجوع کن به
     * [ir.sadteam.loancalc.ui.myloans.MyLoansViewModel.setLoanReminderOffsets] برای معادلِ وام. */
    fun setChequeReminderOffsets(cheque: ChequeEntity, offsets: String?) {
        viewModelScope.launch {
            chequeRepository.updateCheque(cheque.copy(reminderDayOffsets = offsets))
            syncIfLoggedIn()
        }
    }

    /** امتیازِ ریسکِ برگشتِ صادرکننده - جوابِ سوالِ ۲، فریمِ `29l`. */
    fun riskScoreFor(counterpartyId: Long, onResult: (ChequeRiskScore) -> Unit) {
        viewModelScope.launch { onResult(chequeRepository.riskScoreFor(counterpartyId)) }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(chequeRepository.exportBackupJson()) }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = chequeRepository.importBackupJson(json)
            if (ok) syncIfLoggedIn()
            onResult(ok)
        }
    }
}
