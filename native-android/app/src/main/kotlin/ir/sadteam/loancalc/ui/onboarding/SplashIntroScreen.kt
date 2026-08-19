package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// اسپلش الان عیناً همون تصویرِ مرجعِ کاربره (assets/splash/splash.jpg) - رجوع کن به کامنتِ
// SplashIntroScreen پایین. چون خودِ تصویر زمینه‌ی مشکی داره، پس‌زمینه‌ی صفحه هم همیشه مشکیه
// (دیگه با تمِ روشن/تیره عوض نمی‌شه) - این یه تصمیمِ آگاهانه‌ست، نه یه قلمِ جامونده.
private val SplashBg = Color(0xFF050706)
private val SplashSub = Color(0xFF4A5570)

/**
 * اینتروِ باز شدن اپ.
 *
 * **تاریخچه‌ی مهم برای چت‌های بعدی**: سه دورِ متوالی تلاش شد این صفحه با شکل‌های خودِ Compose
 * (اول یه حلقه‌ی دوناتِ Canvas، بعد پشته‌ی کارتِ دستی‌کشیده + آیکونِ TrendingUp، بعد یه بریدهٔ
 * فقط-آیکون از تصویرِ مرجع) بازسازی بشه؛ کاربر هر سه بار با اسکرین‌شات نشون داد نتیجه شبیهِ
 * طرحی که می‌خواد نیست - جزئیاتِ رندرِ سه‌بعدی (شیشه‌ی نیمه‌شفاف، حلقه‌ی نوریِ دورگرد با عمق،
 * بازتاب‌های زمین، بارشِ ذرات) اصلاً با ابزارهای رسمِ ساده قابلِ بازسازی نبودن.
 *
 * تصمیمِ نهایی: **کلِ فریمِ نهاییِ طرحِ کاربر مستقیم به‌عنوانِ تصویر نمایش داده می‌شه** - شاملِ
 * خودِ نوشته‌ی «جیبک» و شعارِ زیرش که تو همون تصویر پخته شده. یعنی دیگه هیچ بخشی از این
 * صفحه از نو کشیده نمی‌شه. نتیجه دقیقاً همونیه که کاربر تایید کرده، به قیمتِ اینکه متنِ اسپلش
 * دیگه با تم/فونتِ سیستم عوض نمی‌شه (پذیرفته‌شده).
 *
 * تصویر با [ContentScale.Crop] تمام‌صفحه می‌شه. نسبتِ خودِ تصویر ۰.۴۱ ئه و گوشی‌های رایج بینِ
 * ۰.۴۲ تا ۰.۴۶ ان، پس فقط چند ده پیکسل از بالا/پایین (که هر دو فضای خالیِ مشکی‌ان) بریده می‌شه -
 * نه خودِ آیکون یا متن.
 *
 * فرمتِ JPEG عمدیه نه PNG: تصویر شفافیت لازم نداره و همین یه فایل به‌صورتِ PNG حدودِ ۷۰۰ کیلوبایت
 * می‌شد در برابرِ ~۹۵ کیلوبایتِ JPEG - رو حجمِ کلِ اپ اثرِ محسوسی داشت.
 */
@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    // ظهورِ نرمِ کلِ صحنه - چون خودِ تصویر ثابته، همین یه محوشدن/بزرگ‌شدنِ ملایم حسِ «ظاهر شدن»
    // رو می‌ده بدونِ اینکه با چیزی که تو تصویر پخته شده تداخل کنه.
    val reveal = remember { Animatable(0f) }
    val poweredByAlpha = remember { Animatable(0f) }
    val poweredByOffset = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        launch {
            reveal.animateTo(1f, animationSpec = tween(700, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
        }
        // زمان‌بندی‌ها با کوتاه‌شدنِ کلِ اسپلش جلو کشیده شدن، وگرنه «Powered By» درست وسطِ محوشدنِ
        // خروج تازه کامل ظاهر می‌شد.
        launch {
            delay(600)
            launch { poweredByAlpha.animateTo(1f, animationSpec = tween(450)) }
            launch {
                poweredByOffset.animateTo(0f, animationSpec = tween(450, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
            }
        }
    }
    // خواسته‌ی کاربر: «بعد از اسپلش یه انیمیشن، ولی سریع وارد برنامه بشیم». پس هم کلِ مدتِ اسپلش
    // کوتاه‌تر شد (۲۱۵۰ → ۱۴۵۰ میلی‌ثانیه)، هم به‌جای ناپدیدشدنِ ناگهانی، صحنه با یه بزرگ‌نماییِ
    // خیلی کوتاه محو می‌شه و بلافاصله [AnimatedAppEntrance] صفحه‌ی اصلی رو با همون حس میاره تو.
    val exit = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1450)
        exit.animateTo(1f, animationSpec = tween(260, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)))
        onDone()
    }

    // نفسِ خیلی آرومِ صحنه بعد از ظاهرشدن - فقط اونقدر که تصویر «مُرده» به‌نظر نیاد.
    val breathe = rememberInfiniteTransition(label = "splashBreathe")
    val breatheScale by breathe.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(animation = tween(2600), repeatMode = RepeatMode.Reverse),
        label = "splashBreatheScale",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = "file:///android_asset/splash/splash.jpg",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val enter = 0.94f + 0.06f * reveal.value
                    // خروج: کمی بزرگ‌تر می‌شه و هم‌زمان محو - حسِ «رفتن به داخلِ برنامه».
                    val leave = 1f + 0.08f * exit.value
                    scaleX = enter * breatheScale * leave
                    scaleY = enter * breatheScale * leave
                    alpha = reveal.value * (1f - exit.value)
                },
        )

        Text(
            "Powered By SadTeam",
            color = SplashSub,
            fontSize = 10.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .offset(y = poweredByOffset.value.dp)
                .alpha(poweredByAlpha.value * (1f - exit.value)),
        )
    }
}
