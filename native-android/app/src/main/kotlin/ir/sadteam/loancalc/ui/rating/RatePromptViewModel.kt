package ir.sadteam.loancalc.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * یادآوریِ دوره‌ایِ امتیازدهی تو استور (مورد ۲۵ تو CLAUDE.md) - رجوع کن به توضیحِ کلیدهای
 * rateDialog* تو UiPrefs. اولین‌بار بعدِ [FIRST_THRESHOLD]امین بازشدنِ اپ نشون داده می‌شه؛ اگه
 * کاربر «بعداً» زد، هر [REPEAT_INTERVAL] بازشدنِ دیگه دوباره می‌پرسه؛ اگه «بله امتیاز می‌دم» یا
 * «نه ممنون» زد، دیگه هیچ‌وقت نشون داده نمی‌شه.
 */
@HiltViewModel
class RatePromptViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
) : ViewModel() {
    private val _shouldShow = MutableStateFlow(false)
    val shouldShow: StateFlow<Boolean> = _shouldShow.asStateFlow()

    private companion object {
        const val FIRST_THRESHOLD = 5
        const val REPEAT_INTERVAL = 15
    }

    fun onAppOpened() {
        viewModelScope.launch {
            if (uiPrefs.rateDialogDismissedForever.first()) return@launch
            val opens = uiPrefs.incrementRateDialogOpens()
            val lastShown = uiPrefs.rateDialogLastShownAtOpens.first()
            val due = if (lastShown == null) {
                opens >= FIRST_THRESHOLD
            } else {
                opens - lastShown >= REPEAT_INTERVAL
            }
            if (due) {
                uiPrefs.setRateDialogLastShownAtOpens(opens)
                _shouldShow.value = true
            }
        }
    }

    fun onRateNow() {
        viewModelScope.launch { uiPrefs.setRateDialogDismissedForever(true) }
        _shouldShow.value = false
    }

    fun onLater() {
        _shouldShow.value = false
    }

    fun onDismissForever() {
        viewModelScope.launch { uiPrefs.setRateDialogDismissedForever(true) }
        _shouldShow.value = false
    }
}
