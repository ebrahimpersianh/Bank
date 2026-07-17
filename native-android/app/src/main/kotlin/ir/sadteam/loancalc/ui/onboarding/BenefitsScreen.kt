package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

private val freeFeatures = listOf(
    "محاسبه‌ی اقساط وام بانکی و قرض‌الحسنه",
    "محاسبه‌گر سقف وام بر اساس درآمد",
    "محاسبه‌ی سود سپرده",
    "ذخیره‌ی یک وام و پیگیری وضعیت اقساط",
    "یادآوری سررسید قسط‌ها",
    "پشتیبان‌گیری و بازیابی اطلاعات",
)

// تم تاریک و هپتیک فیدبک الان برای همه (نه فقط مشترک‌ها) رایگانن، پس دیگه تو این لیست نیستن.
private val subscriptionFeatures = listOf(
    "ذخیره‌سازی نامحدود وام‌ها",
    "ذخیره‌سازی نامحدود چک‌ها و دسته‌چک‌ها",
    "همگام‌سازی ابری بین چند دستگاه",
    "پشتیبان‌گیری ابری از تمامی وام‌ها",
    "ارسال بازخورد مستقیم به تیم پشتیبانی",
)

/**
 * پورت مفهومی صفحه‌ی خوش‌آمد امکانات اپ رقیب (VAMMAN): «امکانات عادی» (رایگان، چک‌دار) در برابر
 * «امکانات اشتراکی» (قفل، طلایی) - فقط یه‌بار درست بعد از گیت مجوز و قبل از گیت ورود/مهمان نشون
 * داده می‌شه (رجوع کن به AuthViewModel.benefitsSeen). محتوا دقیقاً منطبق با مدل واقعی این اپه (نه
 * کپی متن رقیب): محدودیت واقعی ما فقط «یک وام رایگان» + سینک ابریه، نه امکانات ساختگی دیگه.
 */
@Composable
fun BenefitsScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "به «وام من» خوش اومدی",
            color = AppText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "قبل از شروع، ببین چه چیزهایی در انتظارته",
            color = AppMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )

        FeatureBox(
            title = "امکانات رایگان",
            titleColor = AppPrimary,
            items = freeFeatures,
            itemIcon = Icons.Filled.Check,
            iconColor = AppPrimary,
            modifier = Modifier.padding(top = 24.dp),
        )

        FeatureBox(
            title = "امکانات اشتراکی",
            titleColor = AppText,
            items = subscriptionFeatures,
            itemIcon = Icons.Filled.Lock,
            iconColor = AppAccent,
            modifier = Modifier.padding(top = 14.dp),
            borderColor = AppAccent,
        )

        GradientButton(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
        ) {
            Text("بزن بریم")
        }

        // به‌جای فقط متن، پایینِ صفحه یه لینکِ کوچیکِ مخصوصِ خریدِ نسخه‌ی اشتراکی هم داره (پورت
        // مفهومیِ لینکِ «برای فعال‌سازی لمس کنید» تو صفحه‌ی خوش‌آمدِ اپ رقیب) - چون اینجا هنوز قبل
        // از گیتِ ورودیم (هیچ کاربری شناخته‌شده نیست)، این هم دقیقاً همون onContinue رو صدا می‌زنه؛
        // قدمِ بعدی (ورود، بعد صفحه‌ی اشتراک) طبق همون قانونِ همیشگیِ اپ پیش می‌ره.
        Text(
            "می‌خوای نسخه‌ی اشتراکی رو بخری؟ اول وارد شو ←",
            color = AppText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 14.dp)
                .clickable(onClick = onContinue)
                .background(AppAccent.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun FeatureBox(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color,
    items: List<String>,
    itemIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    borderColor: androidx.compose.ui.graphics.Color = AppLine,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppSurface, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        items.forEach { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    itemIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(20.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape)
                        .padding(3.dp),
                )
                Text(label, color = AppText, fontSize = 13.sp)
            }
        }
    }
}
