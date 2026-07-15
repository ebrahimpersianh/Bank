package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

/**
 * پورت انیمیشن خوش‌آمدگویی شبیه چت‌بات اپ رقیب (VAMMAN): «خوش اومدی [نام]» + یه معرفی کوتاه، هر دو
 * با fade+slide تدریجی ظاهر می‌شن، بعد خودکار (بدون نیاز به تپ کاربر) بعد از ~۱.۸ ثانیه [onDone]
 * صدا زده می‌شه. این هر بار که اپ به صفحه‌ی اصلی می‌رسه نشون داده می‌شه (نه فقط یه‌بار مثل
 * [BenefitsScreen]) - state ش تو AppRoot نگه داشته می‌شه، نه DataStore.
 */
@Composable
fun WelcomeMessageScreen(name: String, onDone: () -> Unit) {
    var showIntro by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(350)
        showIntro = true
        delay(1450)
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "خوش اومدی، $name 👋",
            color = AppText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        AnimatedVisibility(visible = showIntro) {
            Text(
                "بیا اقساط و وام‌هات رو مرتب و بی‌دردسر پیگیری کنیم",
                color = AppMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/**
 * پورت افکت «box-swoosh» رقیب: وقتی محتوا (صفحه‌ی اصلی) اولین‌بار ظاهر می‌شه، سریع (۳۵۰ میلی‌ثانیه)
 * از بالا-چپ (offset منفی هم رو X هم رو Y) + fade میاد تو، نه این‌که یهو ظاهر بشه. چون
 * [MutableTransitionState] از false شروع و بلافاصله targetState=true می‌شه، این انیمیشن خودکار رو
 * اولین composition اجرا می‌شه.
 */
@Composable
fun AnimatedAppEntrance(content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(350)) +
            slideInHorizontally(animationSpec = tween(350)) { -it / 3 } +
            slideInVertically(animationSpec = tween(350)) { -it / 3 },
    ) {
        content()
    }
}
