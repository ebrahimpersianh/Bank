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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورتِ گزینه‌های این صفحه، یه‌به‌یک با [ir.sadteam.loancalc.MainActivity]`.BottomTab` (چهار تبِ
 * اصلی) + یه گزینه‌ی «امور چک» که تبِ مستقل نداره (زیرِ تبِ وام بانکیه) - برای همین [route]ش هم
 * `"bank_loan"`ه. **این چهار رشته‌ی route باید همیشه با `BottomTab.route` تو MainActivity.kt یکی
 * بمونن** - چون BottomTab اونجا private هست، نمی‌شه مستقیم استفاده کرد، پس دستی همگام نگه داشته
 * می‌شن.
 */
enum class PersonalizationChoice(val route: String, val label: String, val icon: ImageVector) {
    BANK_LOAN("bank_loan", "می‌خوام ببینم وامم چقدر قسط می‌شه", Icons.Outlined.Payments),
    AFFORD("afford", "می‌خوام ببینم چقدر وام می‌تونم بگیرم", Icons.Outlined.RequestQuote),
    MY_LOANS("my_loans", "وام‌هایی که دارم رو مدیریت کنم", Icons.Outlined.FolderOpen),
    CHEQUE("bank_loan", "چک دارم که باید پیگیریش کنم", Icons.Outlined.ReceiptLong),
}

/**
 * «الان دنبالِ چی هستی؟» - یه سوالِ تک‌مرحله‌ای، فقط یه‌بار بینِ [BenefitsScreen] و گیتِ ورود/مهمان
 * نشون داده می‌شه (رجوع کن به AuthPrefs.personalizationSeen). جوابش تعیین می‌کنه اولین باری که
 * کاربر به صفحه‌ی اصلی می‌رسه کدوم تب باز باشه - نه یه فرمِ طولانی، فقط یه لمس و تمومه.
 */
@Composable
fun PersonalizationScreen(onSelect: (PersonalizationChoice) -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "الان دنبالِ چی هستی؟",
            color = AppText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "بر اساسِ جوابت، مستقیم می‌بریمت همون بخش",
            color = AppMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
        )

        PersonalizationChoice.entries.forEach { choice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .background(AppSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .clickable { onSelect(choice) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    choice.icon,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .background(AppPrimary.copy(alpha = 0.12f), CircleShape)
                        .padding(7.dp),
                )
                Text(choice.label, color = AppText, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "فعلاً رد کن",
            color = AppMuted,
            fontSize = 12.5.sp,
            modifier = Modifier
                .clickable(onClick = onSkip)
                .padding(10.dp),
        )
    }
}
