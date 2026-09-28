package ir.sadteam.loancalc.ui.tools

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.RecurringDetector
import ir.sadteam.loancalc.core.RecurringInput
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.accounting.SubscriptionFinderScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/** درِ واحدِ ابزارهای مالی: اشتراک‌یاب، دنگ و استعلام چک صیادی. */
@Composable
fun ToolsHubScreen(
    onBack: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenDeng: () -> Unit,
    onOpenSayad: () -> Unit,
    onOpenNotes: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val transactions by accountViewModel.transactions.collectAsState()
    val subscriptions = remember(transactions) {
        RecurringDetector.detect(
            transactions
                .filter { it.type == TransactionType.WITHDRAWAL.name }
                .map {
                    RecurringInput(
                        description = it.description,
                        amountRial = it.amount,
                        year = it.year,
                        month = it.month,
                        day = it.day,
                        category = it.category,
                    )
                },
        )
    }
    var openSubscriptions = remember { androidx.compose.runtime.mutableStateOf(false) }

    if (openSubscriptions.value) {
        SubscriptionFinderScreen(
            subscriptions = subscriptions,
            onBack = { openSubscriptions.value = false },
            accountViewModel = accountViewModel,
        )
        return
    }
    BackHandler(onBack = onBack)

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                    }
                    Text("ابزارها", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            item {
                ToolCard(
                    icon = Icons.Outlined.Description,
                    title = "آرشیو سالانه",
                    text = "خلاصهٔ درآمد، هزینه و خالص هر سال را از تراکنش‌ها ببین.",
                    action = "باز کردن آرشیو",
                    onClick = onOpenArchive,
                )
            }
            item {
                ToolCard(
                    icon = Icons.Outlined.Autorenew,
                    title = "اشتراک‌یاب",
                    text = "خرج‌های ماهانهٔ تکراری را از روی تراکنش‌ها پیدا می‌کند.",
                    action = "دیدن اشتراک‌ها",
                    onClick = { openSubscriptions.value = true },
                )
            }
            item {
                ToolCard(
                    icon = Icons.Outlined.Groups,
                    title = "دنگ",
                    // متنِ قبلی توضیحِ «طلب و بدهی» بود نه دنگ (گزارشِ خودِ بازبینی).
                    text = "یک هزینه را بینِ چند نفر تقسیم کن و سهمِ هرکس را پیگیری کن.",
                    action = "رفتن به دنگ",
                    onClick = onOpenDeng,
                )
            }
            item {
                // 🚨 **صفحه‌ی یادداشت از قبل ساخته شده بود ولی هیچ دری نداشت** - موقعِ
                // بازطراحیِ تنظیمات ورودی‌اش رفت و جایگزین نشد، پس قابلیتی کامل روی
                // دیسک می‌خوابید. این کارت همان در است.
                ToolCard(
                    icon = Icons.Outlined.EditNote,
                    title = "یادداشت‌ها",
                    text = "یادداشتِ تاریخ‌دار با یادآور - مستقل از وام و چک.",
                    action = "رفتن به یادداشت‌ها",
                    onClick = onOpenNotes,
                )
            }
            item {
                ToolCard(
                    icon = Icons.Outlined.Description,
                    title = "استعلام صیادی",
                    text = "راهنمای استعلام اعتبار چک صیادی از سایت یا پیامک.",
                    action = "استعلام چک",
                    onClick = onOpenSayad,
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    text: String,
    action: String,
    onClick: () -> Unit,
) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AppMuted)
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(title, color = AppText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(text, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
        }
        GradientButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text(action)
        }
    }
}
