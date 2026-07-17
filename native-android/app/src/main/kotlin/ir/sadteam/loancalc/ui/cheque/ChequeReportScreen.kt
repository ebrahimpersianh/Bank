package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * «گزارش‌دهی» - آمار کامل امور چک (که قبلاً فقط تیکه‌تیکه بالای لیست پخش بود: «وضعیت چک‌های
 * وضع‌نشده»، نوار نرخ پاس‌شدن) + خروجی PDF/اکسل که قبلاً مستقیم تو منوی سه‌خط بودن، حالا زیرِ
 * همین یه صفحه جمع شدن - [stats] رو خودِ ChequeScreen حساب می‌کنه (`computeChequeStats`)، اینجا
 * فقط نمایشه.
 */
@Composable
fun ChequeReportScreen(
    stats: ChequeStats,
    onBack: () -> Unit,
    onDownloadPdf: () -> Unit,
    onDownloadXlsx: () -> Unit,
) {
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
                Text("گزارش‌دهی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            AppCard(label = "خلاصه‌ی وضعیت") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReportStat(label = "کل چک‌ها", value = toFa(stats.total), color = AppText)
                    ReportStat(label = "پاس‌شده", value = toFa(stats.passed), color = AppPrimary)
                    ReportStat(label = "برگشت‌خورده", value = toFa(stats.bounced), color = AppDanger)
                    ReportStat(label = "وضع‌نشده", value = toFa(stats.pending), color = AppMuted)
                }
            }
        }
        item {
            AppCard(label = "نرخ پاس‌شدن") {
                Text(
                    "${toFa(stats.passRatePercent.toInt())}٪ از چک‌های ثبت‌شده تا الان پاس شدن",
                    color = AppText,
                    fontSize = 13.sp,
                )
            }
        }
        item {
            AppCard(label = "مانده‌ی خالص (چک‌های پاس‌شده)") {
                Text(
                    "${fmt(stats.netBalance)} ریال",
                    color = if (stats.netBalance >= 0) AppPrimary else AppDanger,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "دریافتیِ پاس‌شده منهای پرداختیِ پاس‌شده",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        item {
            AppCard(label = "چک‌های وضع‌نشده") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ReportStat(label = "دریافتی", value = toFa(stats.pendingReceived), color = AppPrimary)
                    ReportStat(label = "پرداختی", value = toFa(stats.pendingPaid), color = AppDanger)
                }
            }
        }
        item {
            AppCard(label = "خروجی گزارش") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDownloadPdf, modifier = Modifier.fillMaxWidth()) {
                        Text("دانلود PDF")
                    }
                    OutlinedButton(onClick = onDownloadXlsx, modifier = Modifier.fillMaxWidth()) {
                        Text("دانلود اکسل")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
