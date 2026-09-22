package ir.sadteam.loancalc.ui.archive

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
import androidx.compose.material.icons.outlined.Archive
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
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

@Composable
fun AnnualArchiveScreen(onBack: () -> Unit, accountViewModel: AccountViewModel = hiltViewModel()) {
    val transactions by accountViewModel.transactions.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    val years = remember(transactions) { transactions.groupBy { it.year }.toSortedMap(compareByDescending { it }) }
    BackHandler(onBack = onBack)
    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText) }
                    Text("آرشیو سالانه", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            if (years.isEmpty()) {
                item { EmptyState(icon = Icons.Outlined.Archive, title = "هنوز آرشیوی نداری", description = "با ثبت تراکنش‌ها، خلاصهٔ هر سال اینجا نگه‌داری می‌شود.") }
            } else {
                years.forEach { (year, rows) ->
                    val income = rows.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount }
                    val expense = rows.filter { it.type != TransactionType.DEPOSIT.name }.sumOf { it.amount }
                    val net = income - expense
                    // 🚨 **پرخرج‌ترین ماه، نه شمارشِ ماهِ فعال** (بندِ ۷ی بخشِ ۸۱): شمارشِ ماه
                    // خبری نمی‌داد - تقریباً همیشه ۱۲ بود، و در سالِ جاری همان شماره‌ی ماهِ
                    // امروز. پرخرج‌ترین ماه از همین تراکنش‌ها درمی‌آید و جوابِ سوالی است که
                    // کاربر واقعاً دارد.
                    val topMonth = rows
                        .filter { it.type != TransactionType.DEPOSIT.name }
                        .groupBy { it.month }
                        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                        ?.key
                    val isCurrentYear = year == today.year
                    item(key = year) {
                        AppCard {
                            Text("${year.toFa()} · ${rows.size.toFa()} تراکنش", color = AppText, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            AppHeroCard(modifier = Modifier.padding(top = 10.dp)) {
                                Text("خالصِ سال", color = HeroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                // سالِ منفی با رنگِ سالِ مثبت، تنها عددِ قهرمانِ کارت را
                                // بی‌معنی می‌کند - پس علامت و رنگ می‌گیرد (بندِ ۷ی بخشِ ۸۱).
                                // منفی با «−» است نه پرانتز، طبقِ قاعده‌ی عددهای برنامه.
                                val sign = if (net < 0) "−" else "+"
                                Text(
                                    sign + " " + maskIfPrivate(privacyMode, rialToToman(kotlin.math.abs(net).toLong()).toFaMoney()) + " تومان",
                                    color = if (net < 0) AppDanger else AppPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                ArchiveStat("واریز", income, AppPrimary, privacyMode = privacyMode)
                                ArchiveStat("برداشت", expense, AppMuted, privacyMode = privacyMode)
                                if (isCurrentYear) {
                                    // «۹ ماه» کنارِ «۱۲ ماه» شبیهِ داده‌ی ناقص به‌نظر می‌رسد نه
                                    // سالِ نیمه‌تمام، پس سالِ جاری بج می‌گیرد نه عدد.
                                    Column {
                                        Text("وضعیت", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        Text("در جریان", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
                                    }
                                } else {
                                    Column {
                                        Text("پرخرج‌ترین", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            topMonth?.let { persianMonthName(it) } ?: "—",
                                            color = AppText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(top = 3.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveStat(
    label: String,
    value: Double,
    color: androidx.compose.ui.graphics.Color,
    privacyMode: Boolean,
) {
    Column {
        Text(label, color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        Text(maskIfPrivate(privacyMode, rialToToman(value.toLong()).toFaMoney()), color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
    }
}
