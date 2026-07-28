package ir.sadteam.loancalc.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.delay
import kotlin.math.hypot

/** مدتِ باز شدنِ دایره (میلی‌ثانیه) - تلگرام حدودِ نیم‌ثانیه‌ست، همین حس رو می‌ده. */
private const val REVEAL_DURATION_MS = 520

/**
 * اگه به هر دلیلی تمِ واقعی بعد از این مدت عوض نشد (مثلاً نوشتنِ DataStore گیر کرد)، بازم
 * انیمیشن رو شروع می‌کنیم تا صفحه زیرِ یه اسنپ‌شاتِ کهنه قفل نمونه - یعنی بدترین حالت این
 * افکت «یه تعویضِ معمولی» می‌شه، نه یه صفحه‌ی فریزشده.
 */
private const val THEME_APPLY_TIMEOUT_MS = 350L

/**
 * افکتِ تعویضِ تم به سبکِ تلگرام: به‌جای اینکه رنگ‌ها یهو بپرن، یه عکسِ لحظه‌ایِ صفحه‌ی *قبلی*
 * روی صفحه می‌مونه و یه دایره از محلِ خودِ دکمه‌ی تم باز می‌شه و بزرگ می‌شه تا کلِ صفحه - از
 * داخلِ اون دایره تمِ جدید دیده می‌شه.
 *
 * فوت‌وفنِ کار: هیچ‌جا تمِ جدید «کشیده» نمی‌شه؛ تمِ جدید همون محتوای واقعیِ زیرِ اورلیه که از قبل
 * رندر شده. کاری که این کلاس می‌کنه اینه که اسنپ‌شاتِ تمِ قدیمی رو روش می‌ندازه و با
 * [BlendMode.Clear] یه سوراخِ گردِ روبه‌رشد توش می‌زنه.
 *
 * ترتیبِ حیاتی موقعِ تپ: **اول** اسنپ‌شات گرفته بشه، **بعد** تم عوض بشه ([startReveal] قبل از
 * `cycleThemeMode()`) - وگرنه چیزی که به‌عنوانِ «تمِ قدیمی» ثبت می‌شه خودِ تمِ جدیده و افکت
 * بی‌معنی می‌شه.
 */
@Stable
class ThemeRevealState {
    internal var snapshot by mutableStateOf<ImageBitmap?>(null)
        private set
    internal var origin by mutableStateOf(Offset.Zero)
        private set

    /**
     * مقدارِ تم در لحظه‌ی گرفتنِ اسنپ‌شات. [ThemeRevealHost] تا وقتی تمِ فعلی با این یکی فرق نکنه
     * انیمیشن رو شروع نمی‌کنه - چون نوشتنِ تم تو DataStore غیرهمزمانه و اگه دایره زودتر باز بشه،
     * از توش دوباره همون تمِ قدیمی دیده می‌شه (یعنی افکت اصلاً دیده نمی‌شه).
     */
    internal var keyAtCapture: Any? = null
        private set

    internal val radius = Animatable(0f)

    /** تا وقتی true ئه، تپِ دوباره‌ی دکمه‌ی تم نادیده گرفته می‌شه (جلوگیری از تعویضِ دوتایی). */
    var inProgress by mutableStateOf(false)
        private set

    /** توسطِ [ThemeRevealHost] پر می‌شه (چون فقط اون به `LocalView` دسترسی داره). */
    internal var capturer: ((Offset, Any?) -> Boolean)? = null

    /**
     * اسنپ‌شاتِ تمِ فعلی رو می‌گیره و افکت رو مسلح می‌کنه. باید **بلافاصله قبل از** عوض‌کردنِ
     * واقعیِ تم صدا زده بشه.
     *
     * @param origin مرکزِ دایره تو مختصاتِ کلِ صفحه (معمولاً مرکزِ خودِ دکمه‌ی تم).
     * @param currentKey مقدارِ فعلیِ تم، برای تشخیصِ اینکه کِی واقعاً عوض شد.
     * @return false یعنی افکت اجرا نشد (یا وسطِ یه افکتِ دیگه‌ایم یا گرفتنِ عکس شکست خورد) - تو
     *   این حالت تم بازم باید عوض بشه، فقط بدونِ انیمیشن.
     */
    fun startReveal(origin: Offset, currentKey: Any?): Boolean {
        if (inProgress) return false
        return capturer?.invoke(origin, currentKey) ?: false
    }

    internal fun arm(bitmap: ImageBitmap, from: Offset, key: Any?) {
        snapshot = bitmap
        origin = from
        keyAtCapture = key
        inProgress = true
    }

    internal fun clear() {
        snapshot = null
        inProgress = false
    }
}

val LocalThemeReveal = staticCompositionLocalOf { ThemeRevealState() }

/**
 * محتوای اپ رو می‌پیچه و اورلیِ افکتِ تعویضِ تم رو *بالای* همه‌چیز نگه می‌داره.
 *
 * باید بیرونی‌ترین لایه‌ی محتوا باشه (بالاتر از Scaffold/پنلِ تنظیمات/بنرها) وگرنه دایره فقط
 * روی بخشی از صفحه دیده می‌شه.
 *
 * @param revealKey تمِ فعلی - هر وقت عوض شد یعنی «تمِ جدید زیرِ اورلی آماده‌ست، دایره رو باز کن».
 */
@Composable
fun ThemeRevealHost(
    state: ThemeRevealState,
    revealKey: Any,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current

    // تو SideEffect (نه مستقیم وسطِ composition) تا اگه composition دور ریخته/دوباره اجرا شد،
    // یه لامبدای نصفه‌کاره جا نمونه. هر بار هم به‌روز می‌شه تا همیشه به viewِ فعلی اشاره کنه.
    SideEffect {
        state.capturer = capturer@{ origin, currentKey ->
            // drawToBitmap رندرِ نرم‌افزاریه: چون این پروژه عمداً از بلورِ سخت‌افزاری
            // (RenderEffect) استفاده نمی‌کنه (رجوع کن به کامنتِ AuroraBackground)، خروجیش
            // دقیقاً همون چیزیه که کاربر می‌بینه.
            if (view.width <= 0 || view.height <= 0) return@capturer false
            val bitmap = runCatching { view.drawToBitmap().asImageBitmap() }.getOrNull()
                ?: return@capturer false
            state.arm(bitmap, origin, currentKey)
            true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        val snapshot = state.snapshot
        if (snapshot != null) {
            // منتظرِ اعمالِ واقعیِ تمِ جدید می‌مونیم، بعد دایره رو باز می‌کنیم. اگه تا
            // THEME_APPLY_TIMEOUT_MS عوض نشد، بازم شروع می‌کنیم (رجوع کن به کامنتِ همون ثابت).
            LaunchedEffect(snapshot, revealKey) {
                if (revealKey == state.keyAtCapture) {
                    delay(THEME_APPLY_TIMEOUT_MS)
                }
                state.radius.snapTo(0f)
                state.radius.animateTo(
                    targetValue = maxRevealRadius(
                        origin = state.origin,
                        size = Size(view.width.toFloat(), view.height.toFloat()),
                    ),
                    animationSpec = tween(REVEAL_DURATION_MS, easing = FastOutSlowInEasing),
                )
                state.clear()
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawInteriorHole(
                    snapshot = snapshot,
                    origin = state.origin,
                    holeRadius = state.radius.value,
                )
            }
        }
    }
}

/** فاصله تا دورترین گوشه‌ی صفحه - یعنی شعاعی که دایره باید بهش برسه تا کلِ صفحه پوشیده بشه. */
private fun maxRevealRadius(origin: Offset, size: Size): Float {
    val dx = maxOf(origin.x, size.width - origin.x)
    val dy = maxOf(origin.y, size.height - origin.y)
    return hypot(dx, dy)
}

/**
 * اسنپ‌شاتِ تمِ قدیمی رو می‌کشه و یه سوراخِ گرد توش می‌زنه.
 *
 * [saveLayer] اجباریه: [BlendMode.Clear] فقط داخلِ یه لایه‌ی جدا معنی می‌ده - بدونش به‌جای
 * پاک‌کردنِ اسنپ‌شات، کلِ بومِ زیرین (یعنی خودِ تمِ جدید) رو سیاه/خالی می‌کنه.
 */
private fun DrawScope.drawInteriorHole(
    snapshot: ImageBitmap,
    origin: Offset,
    holeRadius: Float,
) {
    drawIntoCanvas { canvas ->
        canvas.saveLayer(Rect(Offset.Zero, size), Paint())
        drawImage(
            image = snapshot,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(snapshot.width, snapshot.height),
            dstOffset = IntOffset.Zero,
            // اگه اندازه‌ی بیت‌مپ و بوم یکی نبود (چرخشِ صفحه وسطِ کار)، کش میاد به‌جای اینکه
            // بخشی از صفحه خالی بمونه.
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        )
        drawCircle(
            color = Color.Black,
            radius = holeRadius,
            center = origin,
            blendMode = BlendMode.Clear,
        )
        canvas.restore()
    }
}
