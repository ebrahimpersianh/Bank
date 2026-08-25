package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroTone
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.pillOverSurface

/**
 * «گزارش‌دهی» - آمار کامل امور چک (که قبلاً فقط تیکه‌تیکه بالای لیست پخش بود: «وضعیت چک‌های
 * وضع‌نشده»، نوار نرخ پاس‌شدن) + خروجی PDF/اکسل که قبلاً مستقیم تو منوی سه‌خط بودن، حالا زیرِ
 * همین یه صفحه جمع شدن - [stats] رو خودِ ChequeScreen حساب می‌کنه (`computeChequeStats`)، اینجا
 * فقط نمایشه.
 *
 * بازطراحیِ Liquid Glass: هیرویِ حلقه‌ی نرخِ پاس‌شدن + مانده‌ی خالص یکی شدن، خلاصه‌ی وضعیت با
 * نوارِ سهم، چک‌های وضع‌نشده به دو کاشیِ آیکون‌دار، خروجی‌ها به کارت‌های آبیِ AppInfo (ابزار نه
 * پول) - همون هشت مقدارِ ChequeStats، چیزی محاسبه یا حذف نشد.
 */
@Composable
internal fun ChequeReportScreen(
    stats: ChequeStats,
    onBack: () -> Unit,
    onDownloadPdf: () -> Unit,
    onDownloadXlsx: () -> Unit,
    onAddCheque: () -> Unit,
) {
    // بخشِ ۳۳ فایلِ طراحی (کارتِ `33b` و جدولِ `33d`): گزارشِ چک حالتِ خالیِ اختصاصیِ خودش رو
    // داره - قبلاً با صفرِ چک یه صفحه‌ی پر از عددِ صفر و نمودارِ خالی نشون داده می‌شد.
    if (stats.total == 0) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("گزارش‌دهی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
            EmptyState(
                icon = Icons.Outlined.Description,
                title = "هنوز چکی ثبت نکردی",
                description = "چکِ صادرشده و دریافتی را که وارد کنی، گزارشِ ماهانه و سررسیدها همین‌جا ساخته می‌شود.",
                actionLabel = "ثبتِ اولین چک",
                onAction = onAddCheque,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
        }
        return
    }
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
            // گزارشِ چک - هم‌خانواده‌ی «آمار»ه، پس **بنفش** (همون توکنِ «بنفش = بودجه و آمار»).
            AppHeroCard(tone = HeroTone.PURPLE) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = (stats.passRatePercent / 100.0).toFloat(),
                        size = 100.dp,
                        strokeWidth = 13.dp,
                        // رو زمینه‌ی بنفشِ مات، حلقه و متن‌ها سفیدن نه تم‌آگاه.
                        colors = listOf(Color.White, Color.White),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${toFa(stats.passRatePercent.toInt())}٪", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("پاس‌شده", color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 15.dp)) {
                        Text("مانده‌ی خالص", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${fmt(stats.netBalance)}",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "ریال — دریافتیِ پاس‌شده منهای پرداختیِ پاس‌شده",
                            color = HeroMuted,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
        item {
            AppCard {
                Column {
                    Text("خلاصه‌ی وضعیت", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ReportStat(label = "کل چک‌ها", value = toFa(stats.total), color = AppText)
                        ReportStat(label = "پاس‌شده", value = toFa(stats.passed), color = AppPrimary)
                        ReportStat(label = "برگشت‌خورده", value = toFa(stats.bounced), color = AppDanger)
                        ReportStat(label = "وضع‌نشده", value = toFa(stats.pending), color = AppMuted)
                    }
                    // نوارِ سهمِ passed/bounced/pending - همون سه عدد به‌صورتِ weight، دادهٔ جدید نیست.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 11.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                    ) {
                        val total = stats.total.coerceAtLeast(1)
                        Box(modifier = Modifier.weight(stats.passed.toFloat().coerceAtLeast(0.001f) / total).fillMaxSize().background(AppPrimary))
                        Box(modifier = Modifier.weight(stats.bounced.toFloat().coerceAtLeast(0.001f) / total).fillMaxSize().background(AppDanger))
                        Box(modifier = Modifier.weight(stats.pending.toFloat().coerceAtLeast(0.001f) / total).fillMaxSize().background(AppPrimaryPill))
                    }
                }
            }
        }
        item {
            AppCard {
                Column {
                    Text("چک‌های وضع‌نشده", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        PendingChequeTile(
                            icon = Icons.Filled.ArrowDownward,
                            color = AppPrimary,
                            value = toFa(stats.pendingReceived),
                            label = "دریافتی",
                            modifier = Modifier.weight(1f),
                        )
                        PendingChequeTile(
                            icon = Icons.Filled.ArrowUpward,
                            color = AppDanger,
                            value = toFa(stats.pendingPaid),
                            label = "پرداختی",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        item {
            AppCard {
                Column {
                    Text("خروجی گزارش", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        ChequeReportExportTile(
                            icon = Icons.Outlined.Description,
                            label = "دانلود PDF",
                            onClick = onDownloadPdf,
                            modifier = Modifier.weight(1f),
                        )
                        ChequeReportExportTile(
                            icon = Icons.Outlined.GridOn,
                            label = "دانلود اکسل",
                            onClick = onDownloadXlsx,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Text(label, color = AppMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun PendingChequeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.pillOverSurface(0.12f))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(color.pillOverSurface(0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label, color = AppMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ChequeReportExportTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppInfoPill)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
        Text(label, color = AppInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
