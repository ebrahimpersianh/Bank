package ir.sadteam.loancalc.ui.profile

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.CoinEventEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * **کیفِ سکه** - کارتِ `20d` فایلِ طراحی.
 *
 * قاعده‌های صریحِ طرح:
 * - «سکه‌ی بزرگِ بالا موجودی را مثلِ یک سکه‌ی واقعی نشان می‌دهد، نه یک عددِ کنارِ آیکون».
 * - «زیرش معادلِ ریالی نوشته می‌شود تا سکه عددِ بی‌معنا نباشد» - نرخ: **هر ۱۰ سکه = ۱٬۰۰۰ ریال**.
 * - «خرج با ردیفِ قرمز و علامتِ منفی از کسب جدا می‌شود».
 */
@Composable
fun CoinWalletScreen(
    onBack: () -> Unit,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val coins by viewModel.coins.collectAsState()
    val events by viewModel.events.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("کیفِ سکه", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CoinIcon(size = 84.dp)
                    Text(
                        toFa(coins),
                        color = AppText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text("سکه", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "معادلِ ${fmt(coinsToRial(coins))} ریال تخفیف",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        item {
            Text("تاریخچه", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }
        if (events.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = "هنوز سکه‌ای جمع نکردی",
                    description = "با ثبتِ روزانه‌ی تراکنش، وصل‌کردنِ پیامکِ بانک و گرفتنِ نشان، سکه جمع می‌شود.",
                )
            }
        }
        items(events, key = { it.id }) { event ->
            CoinEventRow(event)
        }
    }
}

@Composable
private fun CoinEventRow(event: CoinEventEntity) {
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(coinEventLabel(event.type), color = AppText, fontSize = 13.sp)
                Text(
                    formatEventTime(event.createdAt),
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            val spent = event.amount < 0
            Text(
                (if (spent) "−" else "+") + toFa(kotlin.math.abs(event.amount)),
                color = if (spent) AppDangerInk else AppPrimaryInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

/** برچسبِ فارسیِ هر نوعِ رویداد - عیناً واژه‌های جدولِ `20e`. */
private fun coinEventLabel(type: String): String = when (type) {
    GamificationRepository.Type.NEW_PHONE_GIFT -> "هدیه‌ی شروع"
    GamificationRepository.Type.DAILY_LOG -> "ثبتِ روزانه"
    GamificationRepository.Type.WEEK_COMPLETE -> "هفته‌ی کاملِ فعال بودن"
    GamificationRepository.Type.CONNECT_SMS -> "وصل‌کردنِ پیامکِ بانکی"
    GamificationRepository.Type.CONNECT_NOTIFICATION -> "وصل‌کردنِ اعلانِ بانک"
    GamificationRepository.Type.COMPLETE_PROFILE -> "تکمیلِ پروفایل"
    GamificationRepository.Type.FIRST_BUDGET -> "اولین بودجه"
    GamificationRepository.Type.FIRST_BACKUP -> "اولین پشتیبان‌گیری"
    GamificationRepository.Type.BADGE -> "نشانِ تازه"
    GamificationRepository.Type.SPEND_SUBSCRIPTION -> "تخفیفِ تمدیدِ اشتراک"
    else -> "سکه"
}

/** نرخِ تبدیلِ صریحِ طرح: **هر ۱۰ سکه = ۱٬۰۰۰ ریال تخفیف**. */
internal fun coinsToRial(coins: Int): Double = coins * 100.0

/** «۲۶ مرداد · ۲۱:۱۴» - تاریخِ شمسی، دقیقاً مثلِ ردیف‌های کارتِ `20d`. */
private fun formatEventTime(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val jalali = JalaliCalendar.fromGregorian(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
    )
    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(millis))
    return "${toFa(jalali.d)} ${persianMonthName(jalali.m)} · ${toFa(time)}"
}
