package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

/**
 * جایگزینِ `Toast.makeText` تو کل اپ - کاربر گزارش کرد Android 12+ (و بعضی رام‌ها مثل MIUI) خودکار
 * یه آیکونِ اپ رو کنارِ متنِ هر Toast می‌چسبونن («لوگوی آندروید خام» که جا جا تو اپ دیده)؛ این یه
 * بنرِ داخلِ خودِ اپه که همچین آرایشی نداره - پورتِ همون الگویی که قبلاً برای هینتِ خروج تو
 * MainActivity و پیامِ export تقویم تو LoanDetailScreen استفاده شده بود، این‌بار دوباره‌قابل‌استفاده
 * برای هر صفحه‌ای.
 *
 * استفاده: `val banner = rememberInAppBanner()` تو ریشه‌ی صفحه، صفحه رو تو یه `Box` بذار، و
 * `InAppBannerHost(banner)` رو به‌عنوان آخرین فرزندِ همون Box اضافه کن. هر جا قبلاً Toast بود،
 * به‌جاش `banner.show("متن")` صدا بزن.
 */
@Composable
fun rememberInAppBanner(): InAppBannerState {
    var message by remember { mutableStateOf<String?>(null) }
    return remember { InAppBannerState(get = { message }, set = { message = it }) }
}

class InAppBannerState internal constructor(
    private val get: () -> String?,
    private val set: (String?) -> Unit,
) {
    val message: String? get() = get()
    fun show(text: String) = set(text)
    fun clear() = set(null)
}

@Composable
fun InAppBannerHost(state: InAppBannerState, modifier: Modifier = Modifier) {
    val message = state.message
    LaunchedEffect(message) {
        if (message != null) {
            delay(2600)
            state.clear()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
        modifier = modifier.padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .background(AppText.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(message ?: "", color = AppSurface, fontSize = 13.sp)
        }
    }
}
