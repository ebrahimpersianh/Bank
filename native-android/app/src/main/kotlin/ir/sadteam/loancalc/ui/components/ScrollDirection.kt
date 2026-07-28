package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow

/**
 * true یعنی «داره رو‌به‌بالا اسکرول می‌شه یا اصلاً هنوز اسکرول نکرده» - برای مخفی‌شدنِ نوارِ پایینِ
 * تب‌ها موقعِ رفتن به پایینِ لیست و ظاهرشدنِ دوباره‌ش با برگشت به بالا (الگویِ رایجِ اپ‌های موبایل).
 *
 * منطق: موقعیتِ فعلی (ایندکسِ اولین آیتمِ دیده‌شده + آفستش) رو با موقعیتِ فریمِ قبل مقایسه می‌کنه؛
 * اگه ایندکس عوض شده باشه، جهت از رو خودِ ایندکس مشخصه؛ اگه نه (تویِ یه آیتمِ ثابت داره اسکرول
 * می‌شه)، از رو آفست تشخیص داده می‌شه.
 */
@Composable
fun rememberIsScrollingUp(listState: LazyListState): State<Boolean> {
    return produceState(initialValue = true, listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                value = if (previousIndex != index) {
                    previousIndex > index
                } else {
                    previousOffset >= offset
                }
                previousIndex = index
                previousOffset = offset
            }
    }
}
