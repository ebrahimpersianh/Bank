package ir.sadteam.loancalc.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.unit.IntOffset

/**
 * توکن‌های مشترکِ «حرکت» (motion) اپ.
 *
 * چرا لازم شد: تقریباً همه‌ی انیمیشن‌های اپ با `tween` (سرعتِ ثابت/خطی) نوشته شده بودن. `tween`
 * برای محوشدنِ رنگ خوبه، ولی برای هرچیزی که **جابه‌جا یا بزرگ/کوچک** می‌شه حسِ ماشینی می‌ده -
 * چون تو دنیای واقعی هیچ‌چیزی با سرعتِ ثابت راه نمی‌افته و با سرعتِ ثابت نمی‌ایسته. اون حسِ
 * «گران‌قیمت»ی که تو iOS/اپ‌های درجه‌یک هست، از فنر (spring) میاد: تند شروع می‌شه، نرم می‌ایسته،
 * و یه سرریزِ خیلی ریز داره.
 *
 * قانونِ ساده برای ادامه‌ی کار:
 * - **موقعیت/اندازه** (slide, scale, ارتفاع) → [standard]/[snappy]/[heavy].
 * - **شفافیت/رنگ** (alpha, color) → `tween` بمونه؛ فنری‌کردنِ محوشدن هیچ سودی نداره و فقط
 *   باعثِ سوسو زدن می‌شه.
 *
 * اسمِ توابع عمداً `spring` نیست تا با `androidx.compose.animation.core.spring` (که خودشون
 * داخل همین فایل صداش می‌زنن) قاطی نشه.
 */
object Motion {

    /** پیش‌فرضِ عمومی - برای اکثرِ جابه‌جایی/تغییرِ اندازه‌ها. کمی سرریز داره ولی نمی‌لرزه. */
    fun <T> standard(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** برای المان‌های ریز و پرتکرار (آیکون، چیپ، دکمه) - سریع‌تر و قاطع‌تر. */
    fun <T> snappy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessMedium,
    )

    /** برای چیزهای بزرگ (صفحه، شیت، پنل) - سنگین‌تر، بدونِ سرریزِ محسوس. */
    fun <T> heavy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.90f,
        stiffness = Spring.StiffnessLow,
    )

    /** حرکتِ فنریِ مخصوصِ آفستِ پیکسلی (بنر، شیت). */
    fun offset(): FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** مدتِ استانداردِ محوشدن (میلی‌ثانیه) - عمداً `tween`، رجوع کن به قانونِ بالا. */
    const val FADE_IN_MS = 210
    const val FADE_OUT_MS = 150

    /**
     * ورود/خروجِ استانداردِ «تعویضِ محتوا» - جایگزینِ الگوی تکراریِ
     * `fadeIn(tween(200)) togetherWith fadeOut(tween(150))` که تو ۴-۵ تا `AnimatedContent`
     * کپی شده بود. حالا محوشدن با یه بزرگ‌شدنِ فنریِ خیلی ریز همراهه، پس صفحه‌ی جدید
     * «میاد جلو» به‌جای اینکه فقط ظاهر بشه.
     */
    val contentEnter: EnterTransition
        get() = fadeIn(tween(FADE_IN_MS)) +
            scaleIn(animationSpec = standard(), initialScale = 0.97f)

    val contentExit: ExitTransition
        get() = fadeOut(tween(FADE_OUT_MS)) +
            scaleOut(animationSpec = standard(), targetScale = 0.98f)
}
