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
) : ViewModel() {
    val pendingLoanId: StateFlow<Long?> = deepLinkTarget.pendingLoanId

    fun consume() {
        deepLinkTarget.consume()
    }
}
