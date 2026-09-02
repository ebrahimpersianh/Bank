package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.RecurringExpense
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroTone
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.settings.SmsAutoImportViewModel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color

/**
 * **اشتراک‌یاب** - خرج‌های تکرارشونده‌ای که خودِ اپ از رو تاریخچه کشف کرده.
 *
 * چرا این برگ‌برنده‌ی جیبکه: بقیه‌ی اپ‌ها فقط چیزی رو نشون می‌دن که کاربر خودش ثبت کرده.
 * این صفحه چیزی رو نشون می‌ده که کاربر **فراموش کرده** - قبضِ سرویسی که سالِ پیش فعال کرده و
 * هنوز ماهانه کسر می‌شه. رجوع کن به [ir.sadteam.loancalc.core.RecurringDetector].
 *
 * دو اقدام روی هر ردیف:
 * - **افزودن به پرداخت‌های تکراری** → از این به بعد تو «ثابت در برابرِ آزاد» و یادآورها میاد.
 * - **نادیده بگیر** → دیگه اینجا نشون داده نمی‌شه (تو `UiPrefs.ignoredSubscriptions` می‌مونه).
 */
@Composable
fun SubscriptionFinderScreen(
    subscriptions: List<RecurringExpense>,
    onBack: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
    prefsViewModel: SmsAutoImportViewModel = hiltViewModel(),
) {
    val privacyMode = LocalPrivacyMode.current
    val ignored by prefsViewModel.ignoredSubscriptions.collectAsState()
    val declared by accountViewModel.recurringPayments.collectAsState()
    val banner = rememberInAppBanner()

    // بازگشتِ سیستمی هم باید صفحه را ببندد، نه کلِ تب را. `onBack` همان کاری را می‌کند که
    // دکمه‌ی فلش می‌کند.
    BackHandler(onBack = onBack)

    val visible = subscriptions.filter { it.label !in ignored }
    val monthlyTotal = visible.sumOf { it.typicalAmountRial }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                    Text("اشتراک‌یاب", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (visible.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Autorenew,
                        title = "فعلاً اشتراکِ تکراری‌ای پیدا نشد",
                        description = "وقتی یه خرج تو سه ماهِ مختلف با مبلغِ تقریباً یکسان تکرار بشه، " +
                            "جیبک خودش پیداش می‌کنه و همین‌جا بهت نشون می‌ده.",
                    )
                }
            } else {
                item {
                    // عددِ قهرمان: چیزی که کاربر تا حالا هیچ‌جا یکجا ندیده.
                    AppHeroCard(tone = HeroTone.PURPLE) {
                        Text("ماهانه بابتِ اشتراک‌ها", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            // fmt() عددِ ریال با جداکننده‌ی لاتین می‌داد.
                            maskIfPrivate(privacyMode, rialToToman(monthlyTotal.toLong()).toFaMoney()),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            // عددِ سالانه فشرده می‌آید: کاملش کنارِ عددِ ماهانه دو عددِ
                            // دوازده‌رقمیِ پشتِ‌هم می‌شد و هیچ‌کدام خوانده نمی‌شد.
                            "تومان — ${toFa(visible.size)} موردِ تکرارشونده · سالانه حدودِ " +
                                (monthlyTotal * 12).rialToFaCompact() + " تومان",
                            color = HeroMuted,
                            fontSize = 10.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                items(visible, key = { it.label }) { sub ->
                    val alreadyDeclared = declared.any { it.name == sub.label }
                    AppCard {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.label, color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${toFa(sub.monthsSeen)} ماهِ پیاپی · حدودِ روزِ ${toFa(sub.dayOfMonth)} هر ماه" +
                                            (sub.category?.let { " · $it" } ?: ""),
                                        color = AppMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                                Text(
                                    // واحد در ردیف نمی‌آید (قاعده‌ی عدد) - یک‌بار در هیرو آمد.
                                    maskIfPrivate(privacyMode, sub.typicalAmountRial.rialToFaCompact()),
                                    color = AppText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (!alreadyDeclared) {
                                    GradientButton(
                                        onClick = {
                                            accountViewModel.addRecurringPayment(
                                                name = sub.label,
                                                amount = sub.typicalAmountRial,
                                                type = TransactionType.WITHDRAWAL,
                                                categoryName = sub.category,
                                                accountId = null,
                                                dayOfMonth = sub.dayOfMonth.coerceIn(1, 31),
                                                reminderDayOffsets = null,
                                            )
                                            banner.show("«${sub.label}» به پرداخت‌های تکراری اضافه شد.")
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("افزودن به تکراری‌ها", fontSize = 12.sp)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { prefsViewModel.ignoreSubscription(sub.label) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("نادیده بگیر", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
