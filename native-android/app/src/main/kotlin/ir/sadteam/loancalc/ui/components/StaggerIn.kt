package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** فاصله‌ی زمانی بینِ ورودِ هر آیتم و آیتمِ بعدی (میلی‌ثانیه). */
private const val STAGGER_STEP_MS = 55

/** مدتِ ورودِ خودِ هر آیتم. */
private const val STAGGER_DURATION_MS = 340

/**
 * سقفِ پله‌ها. مهمه چون تو `LazyColumn` آیتم‌های پایینِ صفحه تازه موقعِ اسکرول ساخته می‌شن -
 * بدونِ این سقف، کارتِ دهم با نیم‌ثانیه تاخیر ظاهر می‌شد و حسِ کندی/باگ می‌داد. با سقف، هرچی
 * پایین‌تر از این باشه همون تاخیرِ ثابت رو می‌گیره و صرفاً یه محوشدنِ ملایمِ موقعِ اسکروله.
 */
private const val STAGGER_MAX_STEPS = 4

/**
 * ورودِ پلکانیِ آیتم‌های یه صفحه: هر بخش با یه تاخیرِ کوچیک بعد از قبلی محو-و-سُر می‌خوره بالا،
 * پس صفحه حسِ «چیده شدن» می‌ده به‌جای اینکه همه‌چیز یهو با هم ظاهر بشه.
 *
 * فقط یه‌بار موقعِ ساخته‌شدنِ صفحه اجرا می‌شه ([MutableTransitionState] تو یه `remember`)، نه با هر
 * recomposition - وگرنه هر بار که یه عددِ صفحه عوض بشه کلِ چیدمان دوباره می‌رقصید.
 *
 * ⚠️ داخلِ `LazyColumn` استفاده نکن: اونجا `Modifier.animateItem()` کارِ درست‌تریه (هم اضافه/حذف
 * هم جابه‌جایی رو پوشش می‌ده)، و ترکیبِ این دوتا باعثِ دوبار انیمیشن‌شدنِ یه آیتم می‌شه.
 *
 * @param index شماره‌ی ترتیبِ آیتم (از صفر) - تاخیرش از همین حساب می‌شه.
 */
@Composable
fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    // «انیمیشنِ کم» (تنظیمات ← ظاهر و تم): ورودِ کارت‌ها جزوِ **تزئین**ه، پس اینجا حذف می‌شه.
    // بازخوردِ لمس (فشرده‌شدنِ دکمه، جابه‌جاییِ دستگیره‌ی کلید) عمداً دست‌نخورده می‌مونه.
    if (LocalReducedMotion.current) {
        content()
        return
    }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val delay = index.coerceIn(0, STAGGER_MAX_STEPS) * STAGGER_STEP_MS
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(STAGGER_DURATION_MS, delayMillis = delay)) +
            slideInVertically(tween(STAGGER_DURATION_MS, delayMillis = delay)) { it / 8 },
    ) {
        content()
    }
}

/**
 * «انیمیشنِ کم» - از `ThemeViewModel` تو `MainActivity` پر می‌شه و به کلِ درختِ UI می‌رسه.
 * قاعده‌ی طراح: هرچه فقط تزئینه می‌ره، هرچه بازخوردِ لمسه می‌مونه.
 */
val LocalReducedMotion = androidx.compose.runtime.staticCompositionLocalOf { false }
