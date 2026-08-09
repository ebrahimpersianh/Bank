package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect

/**
 * مقدارِ عددیِ «بالارونده» - وقتی صفحه باز می‌شه (یا عدد عوض می‌شه) به‌جای ظاهر شدنِ ناگهانی،
 * سریع از مقدارِ قبلی تا مقدارِ جدید می‌شمره. خواسته‌ی صریحِ کاربر (بستهٔ ارتقاهای گرافیکی).
 *
 * عمداً یه تابعِ محاسبه‌ی مقداره نه یه `Text` آماده: هر جای اپ فرمتِ خودش رو داره (ریال/تومان/
 * درصد/حروفی) و بعضی جاها باید تو [ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade] بپیچه.
 * پس این فقط عدد رو می‌ده، نمایش با خودِ صفحه‌ست:
 * ```
 * val shown = countUpAmount(totalBalance)
 * Text("${fmt(shown)} ریال")
 * ```
 *
 * **چرا از صفر نه؟** فقط بارِ اولِ ورود از صفر می‌شمره؛ دفعه‌های بعد (مثلاً بعدِ ثبتِ تراکنش) از
 * مقدارِ قبلی به جدید می‌ره - وگرنه هر بار پرشِ صفر تا عددِ کامل حواس‌پرت‌کن می‌شد.
 *
 * اگه [enabled] خاموش باشه (مثلاً حالتِ خصوصی، که عدد پشتِ ••• مخفیه) انیمیشن کاملاً دور زده
 * می‌شه و مقدارِ نهایی مستقیم برمی‌گرده.
 */
@Composable
fun countUpAmount(
    target: Double,
    enabled: Boolean = true,
    durationMillis: Int = 700,
): Double {
    if (!enabled) return target

    // بارِ اول از صفر، دفعه‌های بعد از مقدارِ قبلی - رجوع کن به توضیحِ بالا.
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val animated by animateFloatAsState(
        targetValue = if (started) target.toFloat() else 0f,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "countUpAmount",
    )
    return animated.toDouble()
}
