package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
 *
 * مورد ۱۳C: فقط **پیشرفتِ ۰ تا ۱** با `Float` انیمیت می‌شود و خودِ مبلغ همیشه با `Double`
 * حساب می‌شود. قبلاً خودِ مبلغ با `Float` انیمیت می‌شد و `Float`ی ۳۲بیتی برای عددهای بزرگ
 * دقت ندارد (۹۶٬۴۲۴٬۳۳۲٫۶۹ ریال → ۹۶٬۴۲۴٬۳۳۶) و «خرجِ هفته» ۱ تومان اضافه نشان می‌داد.
 * حالا مقدارِ نهایی دقیقاً همان هدف است.
 */
@Composable
fun countUpAmount(
    target: Double,
    enabled: Boolean = true,
    durationMillis: Int = 700,
): Double {
    if (!enabled) return target

    val progress = remember { Animatable(0f) }
    // مبدأِ انیمیشنِ جاری (بارِ اول صفر، بعداً هدفِ قبلی یا مقدارِ وسطِ انیمیشنِ قبلی).
    var from by remember { mutableStateOf(0.0) }
    var prevTarget by remember { mutableStateOf(target) }

    LaunchedEffect(target) {
        // هدف عوض شد: مبدأِ انیمیشنِ جدید، مقداری است که همین حالا دیده می‌شود.
        from = from + (prevTarget - from) * progress.value.toDouble()
        prevTarget = target
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        )
    }
    return from + (target - from) * progress.value.toDouble()
}
