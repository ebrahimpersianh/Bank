package ir.sadteam.loancalc.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.UsageStats
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
        /** فاصله‌ی حداقلی بین دو نمایشِ «بعد از موفقیت» تا پشتِ سرِ هم نیاید (۳ روز). */
        const val SUCCESS_COOLDOWN_MILLIS = 3L * 24 * 60 * 60 * 1000
        /** یک درخواستِ «بعد از موفقیت» تا این‌قدر تازه حساب می‌شود (۶۰ ثانیه). */
        const val PENDING_FRESH_MILLIS = 60_000L
    }

    /**
     * ثبتِ «دیالوگ نشان داده شد» - فقط روی همین نمونه (نمونه‌ای که MainActivity
     * می‌بیند) و فقط وقتی از پنهان به نمایان می‌رود، تا یک نمایش دو بار شمرده
     * نشود. «بعد از موفقیت» از هر صفحه‌ای که بیاید، اول به‌صورتِ درخواستِ در
     * انتظار (pending) به این نمونه می‌رسد و همین‌جا ثبت می‌شود؛ پس
     * [maybeShowAfterSuccess] خودش چیزی ثبت نمی‌کند. رویدادها با همان سازوکارِ
     * [UsageStats.action] می‌روند که بقیه‌ی کارها (`transaction_added` و…)
     * را به گزارشِ ادمین می‌رساند؛ آن سازوکار فیلد ندارد، پس منشأ (دوره‌ای /
     * بعد از موفقیت) تو خودِ نامِ رویداد است.
     */
    private fun showAndLog(source: String) {
        if (_shouldShow.value) return
        _shouldShow.value = true
        UsageStats.action("rate_prompt_shown_$source")
    }

    init {
        // لحظه‌ی موفقیت از هر صفحه‌ای (دنگ، وام، چک) که باشد، درخواستش را تو UiPrefs
        // (DataStoreِ مشترک) می‌نویسد؛ نمونه‌ای که MainActivity می‌بیند این‌جا آن را
        // برمی‌دارد و دیالوگ را نشان می‌دهد، بعد پاکش می‌کند تا دوباره نیاید. این‌طوری
        // فرقی نمی‌کند درخواست از کدام نمونه‌ی ViewModel آمده باشد.
        viewModelScope.launch {
            uiPrefs.ratePromptSuccessPendingAt.collect { at ->
                if (at == null) return@collect
                if (System.currentTimeMillis() - at > PENDING_FRESH_MILLIS) return@collect
                if (uiPrefs.rateDialogDismissedForever.first()) return@collect
                uiPrefs.setRatePromptSuccessPendingAt(null)
                showAndLog("after_success")
            }
        }
    }

    fun onAppOpened() {
        viewModelScope.launch {
            if (uiPrefs.rateDialogDismissedForever.first()) return@launch
            val opens = uiPrefs.incrementRateDialogOpens()
            val lastShown = uiPrefs.rateDialogLastShownAtOpens.first()
            val due = if (lastShown == null) {
                opens >= (ir.sadteam.loancalc.data.RemoteApp.config.rateFirst ?: FIRST_THRESHOLD)
            } else {
                opens - lastShown >= (ir.sadteam.loancalc.data.RemoteApp.config.rateEvery ?: REPEAT_INTERVAL)
            }
            if (due) {
                uiPrefs.setRateDialogLastShownAtOpens(opens)
                showAndLog("periodic")
            }
        }
    }

    fun onRateNow() {
        UsageStats.action("rate_prompt_rate_now")
        viewModelScope.launch { uiPrefs.setRateDialogDismissedForever(true) }
        _shouldShow.value = false
    }

    /**
     * نظر ثبت شد (متن کپی و صفحه‌ی نظرِ استور باز شد) - برای همیشه دیگر نشان
     * نده و دیالوگ را هم ببند، چون استور باز می‌شود و صفحه‌ی «ممنون»ی در کار
     * نیست.
     */
    fun onFeedbackSent() {
        UsageStats.action("rate_prompt_feedback_sent")
        viewModelScope.launch { uiPrefs.setRateDialogDismissedForever(true) }
        _shouldShow.value = false
    }

    /**
     * درخواستِ امتیاز بعد از یک «لحظه‌ی موفقیت» (در حالِ حاضر: تسویه‌ی دنگ،
     * `DebtScreen.onSettleAll`؛ ذخیره‌ی وام؛ ثبتِ چک) - با همان دیالوگِ سفارشیِ [RatePromptDialog]، ولی با
     * سقفِ جدا: حداکثر ۳ بار در هر نسخه، و هیچ‌وقت بعد از «دیگر نپرس».
     *
     * نمایشِ واقعی دستِ `MainActivity` است (`StartupPopups.canShowRate`): اگر تور،
     * آپدیتِ اجباری، یادآورِ انقضا یا چک‌اینِ روزانه باز باشد، دیالوگ روی آن‌ها نمی‌آید -
     * پس جریانِ پرداخت/اشتراک را سد نمی‌کند.
     *
     * فاصله‌ی حداقلی: اگر همین چند روز پیش یک‌بار بعد از موفقیت نشان داده شده باشد،
     * این درخواست نادیده گرفته می‌شود تا پشتِ سرِ هم نیاید. درخواست از طریقِ UiPrefs
     * (مشترک) به MainActivity می‌رسد، پس فرقی نمی‌کند از کدام صفحه آمده باشد.
     */
    fun maybeShowAfterSuccess(version: String) {
        viewModelScope.launch {
            if (uiPrefs.rateDialogDismissedForever.first()) return@launch
            val now = System.currentTimeMillis()
            val lastShown = uiPrefs.ratePromptSuccessLastShownAtMillis.first()
            if (lastShown != null && now - lastShown < SUCCESS_COOLDOWN_MILLIS) return@launch
            if (uiPrefs.claimRatePromptAfterSuccess(version)) {
                uiPrefs.setRatePromptSuccessLastShownAtMillis(now)
                uiPrefs.setRatePromptSuccessPendingAt(now)
                // ثبتِ «نشان داده شد» این‌جا نیست: همین درخواست از راهِ pending به
                // نمونه‌ی MainActivity می‌رسد و آن‌جا یک بار ثبت می‌شود.
                _shouldShow.value = true
            }
        }
    }

    fun onLater() {
        UsageStats.action("rate_prompt_later")
        _shouldShow.value = false
    }

    fun onDismissForever() {
        UsageStats.action("rate_prompt_dismiss_forever")
        viewModelScope.launch { uiPrefs.setRateDialogDismissedForever(true) }
        _shouldShow.value = false
    }
}
