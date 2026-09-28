package ir.sadteam.loancalc.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** پوششِ ViewModel-یِ [DeepLinkTarget] برای مصرف تو Compose با hiltViewModel() - رجوع کن به کامنتِ
 * DeepLinkTarget برای دلیلِ وجودش. */
@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    private val deepLinkTarget: DeepLinkTarget,
    private val pendingTxDeepLink: PendingTxDeepLink,
    private val pendingChequeDeepLink: PendingChequeDeepLink,
    private val pendingDebtDeepLink: PendingDebtDeepLink,
    private val pendingSharedSms: PendingSharedSms,
) : ViewModel() {
    val pendingCounterpartyId: StateFlow<Long?> = pendingDebtDeepLink.pendingCounterpartyId

    fun openDebt(id: Long) {
        pendingDebtDeepLink.setCounterpartyId(id)
    }

    fun consumeDebt() {
        pendingDebtDeepLink.consume()
    }

    /** چکی که باید باز شود - هم از اعلانِ سررسیدِ چک، هم از تپ روی ردیفِ تبِ سررسید. */
    val pendingChequeId: StateFlow<Long?> = pendingChequeDeepLink.pendingChequeId

    fun openCheque(id: Long) {
        pendingChequeDeepLink.setChequeId(id)
    }

    fun consumeCheque() {
        pendingChequeDeepLink.consume()
    }

    val pendingLoanId: StateFlow<Long?> = deepLinkTarget.pendingLoanId

    fun consume() {
        deepLinkTarget.consume()
    }

    /** بازکردنِ یه وامِ مشخص از هر جای اپ (ردیفِ سررسید، نوتیفیکیشن، ویجت). */
    fun openLoan(id: Long) {
        deepLinkTarget.setLoanId(id)
    }

    /** تپ روی اعلانِ تراکنشِ خودکار - `(txId, pickCategory)`. رجوع کن به [PendingTxDeepLink]. */
    val pendingTx: StateFlow<Pair<Long, Boolean>?> = pendingTxDeepLink.pending

    fun consumeTx() {
        pendingTxDeepLink.consume()
    }

    val pendingShortcut: StateFlow<String?> = deepLinkTarget.pendingShortcut

    fun consumeShortcut() {
        deepLinkTarget.consumeShortcut()
    }

    /** متنِ پیامکِ اشتراک‌گذاری‌شده از برنامه‌ی پیامکِ گوشی - رجوع کن به [PendingSharedSms]. */
    val sharedSmsText: StateFlow<String?> = pendingSharedSms.text

    fun consumeSharedSms() {
        pendingSharedSms.consume()
    }
}
