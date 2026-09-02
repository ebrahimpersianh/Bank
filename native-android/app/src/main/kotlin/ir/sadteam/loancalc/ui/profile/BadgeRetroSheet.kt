package ir.sadteam.loancalc.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SettledMedal
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * جمع‌بندیِ **یک‌باره‌ی** نشان‌های گذشته - اولین اجرایی که سنجشِ نشان‌ها روشن می‌شه،
 * کاربر قبلاً کارهاشو کرده و نباید نُه تا جشنِ پشت‌سرهم ببینه؛ همه یه‌جا اینجا اعلام می‌شن.
 */
@Composable
fun BadgeRetroSheet(badges: List<Badge>, onDismiss: () -> Unit) {
    // ⚠️ هیچ BackHandlerی نبود، پس دکمه‌ی back به HomeScreen می‌رسید و از آنجا **برنامه را
    // می‌بست** - و چون `consumeRetro` صدا نشده بود، اجرای بعدی همین شیت دوباره می‌آمد.
    // یک back هم دیدنِ پیام است، پس همان `onDismiss`. عمداً بی‌اثر نگذاشتم: قفل‌کردنِ back
    // روی یک صفحه‌ی خبری خشن است.
    BackHandler(onBack = onDismiss)

    val coins = badges.sumOf { it.coins }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "${toFa(badges.size)} نشان از کارهایی که قبلاً کرده‌ای",
            color = AppText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CoinIcon(size = 20.dp)
            Text(
                "${toFa(coins)} سکه هم به حسابت اضافه شد",
                color = AppMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        badges.forEach { badge ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettledMedal()
                    Column(modifier = Modifier.weight(1f)) {
                        Text(badge.label, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text(badge.hint, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    // Boxِ دورِ یک Text بی‌اثر بود. و سکه علامتِ خودش را می‌گیرد، وگرنه
                    // «+۱۰»ِ خاکستریِ تنها می‌توانست هر چیزی باشد.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            "+${toFa(badge.coins)}",
                            color = AppMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        CoinIcon(size = 12.dp)
                    }
                }
            }
        }
        GradientButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("عالیه", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
