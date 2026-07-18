package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * جایگزینِ متنِ ساکنِ «...» تو دکمه‌هایی که در-حال-پردازش‌ان (خرید اشتراک، حذف حساب و ...) - یه
 * حلقه‌ی درحال‌چرخشِ واقعی (Lottie، فایلش تو assets/anim/spinner.json دست‌ساز و ساده‌ست: یه کمانِ
 * سفیدِ ۷۵٪ که مدام می‌چرخه) به‌جای یه علامتِ ثابتِ بی‌جون. رنگش تو خودِ JSON سفید ثابته (نه رنگِ
 * پویا) تا وابسته به یه API پیچیده‌تر (dynamic properties) نباشه؛ سفید رو هر دو پس‌زمینه‌ی
 * فعلی‌ای که ازش استفاده می‌شه (دکمه‌ی گرادیانِ تیل، دکمه‌ی قرمزِ حذف) به‌قدرِ کافی خوانا می‌مونه.
 */
@Composable
fun LottieSpinner(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/spinner.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
    )
}
