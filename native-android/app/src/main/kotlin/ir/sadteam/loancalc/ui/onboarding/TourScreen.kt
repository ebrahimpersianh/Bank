package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private data class TourStep(val icon: ImageVector, val title: String, val description: String)

private val tourSteps = listOf(
    TourStep(
        Icons.Outlined.Payments,
        "وام بانکی",
        "قسطِ وام‌های بانکی و قرض‌الحسنه رو دقیق و لحظه‌ای محاسبه کن - از تبِ اولِ پایینِ صفحه.",
    ),
    TourStep(
        Icons.Outlined.RequestQuote,
        "محاسبه‌گر",
        "با پرداختِ ماهانه‌ای که مقدوره، ببین چقدر وام می‌تونی بگیری.",
    ),
    TourStep(
        Icons.Outlined.TrendingUp,
        "سود سپرده",
        "قبل از سپرده‌گذاری، سودِ نهایی رو از قبل حساب کن.",
    ),
    TourStep(
        Icons.Outlined.FolderOpen,
        "وام‌های من",
        "وام‌ها و چک‌هات رو یه‌جا ذخیره کن، وضعیتِ هر قسط رو پیگیری کن، و یادآوریِ سررسید بگیر.",
    ),
)

/**
 * تورِ راهنمای اولین ورود - یه‌بار تو کل عمر نصب، درست بعد از اولین [WelcomeMessageScreen] نشون داده
 * می‌شه (رجوع کن به AuthPrefs.tourSeen). به‌جای یه overlayِ spotlight رو دکمه‌های واقعی (که نیازمندِ
 * اندازه‌گیریِ دقیقِ مختصاتِ چندتا Composableی جداست، ریسکِ جنکِ قابل‌توجه بدونِ تستِ رو گوشیِ واقعی)،
 * یه سری کارتِ ساده و متوالیه - همون الگویی که اپ‌های شناخته‌شده (Duolingo, Slack) برای تورِ اولیه
 * استفاده می‌کنن.
 */
@Composable
fun TourScreen(onDone: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val step = tourSteps[stepIndex]
    val isLastStep = stepIndex == tourSteps.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(
            targetState = stepIndex,
            transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(150))) },
            label = "tourStep",
        ) { index ->
            val s = tourSteps[index]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(AppPrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(s.icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(44.dp))
                }
                Text(
                    s.title,
                    color = AppText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Text(
                    s.description,
                    color = AppMuted,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tourSteps.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == stepIndex) 9.dp else 7.dp)
                        .background(if (i == stepIndex) AppPrimary else AppLine, CircleShape),
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        GradientButton(
            onClick = { if (isLastStep) onDone() else stepIndex++ },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLastStep) "بزن بریم" else "بعدی")
        }
        TextButton(onClick = onDone, modifier = Modifier.padding(top = 4.dp)) {
            Text("رد کن", color = AppMuted, fontSize = 12.5.sp)
        }
    }
}
