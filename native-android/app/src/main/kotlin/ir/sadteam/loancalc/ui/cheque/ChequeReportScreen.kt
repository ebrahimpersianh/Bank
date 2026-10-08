package ir.sadteam.loancalc.ui.cheque

import ir.sadteam.loancalc.ui.jibak.rialToToman
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                Text("گزارش‌دهی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            // گزارشِ چک - هم‌خانواده‌ی «آمار»ه، پس **بنفش** (همون توکنِ «بنفش = بودجه و آمار»).
            // جوابِ سوالِ ۵: به‌جای حلقه‌ی تک‌مقداریِ ProgressRing، همون CategoryDonutِ چندبخشیِ
            // گزارشِ حسابداری (سهمِ پاس/برگشتی/وضع‌نشده به‌جای سهمِ دسته‌بندی).
            AppHeroCard(tone = HeroTone.GREEN) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CategoryDonut(
                        slices = listOf(
                            DonutSlice(stats.passed.toDouble(), Color.White),
                            DonutSlice(stats.bounced.toDouble(), Color.White.copy(alpha = 0.45f)),
                            DonutSlice(stats.pending.toDouble(), Color.White.copy(alpha = 0.2f)),
                        ),
                        size = 100.dp,
                        strokeWidth = 13.dp,
                        trackColor = Color.White.copy(alpha = 0.15f),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${toFa(stats.passRatePercent.toInt())}٪", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Text("پاس‌شده", color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 15.dp)) {
                        Text("مانده‌ی خالص", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            ir.sadteam.loancalc.ui.privacy.maskIfPrivate(ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current, "${fmt(rialToToman((stats.netBalance).toLong()).toDouble())}"),
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "${ir.sadteam.loancalc.ui.jibak.unitFa()} — دریافتیِ پاس‌شده منهای پرداختیِ پاس‌شده",
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
                    Text("خلاصه‌ی وضعیت", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    // طرحِ Claude Design (۸ مهر): چهار کاشیِ رنگی.
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ReportStat(label = "کل چک‌ها", value = toFa(stats.total), color = AppText, modifier = Modifier.weight(1f))
                        ReportStat(label = "پاس‌شده", value = toFa(stats.passed), color = AppPrimary, modifier = Modifier.weight(1f))
                        ReportStat(label = "برگشت‌خورده", value = toFa(stats.bounced), color = AppDanger, modifier = Modifier.weight(1f))
                        ReportStat(label = "وضع‌نشده", value = toFa(stats.pending), color = AppMuted, modifier = Modifier.weight(1f))
                    }
                    // نوارِ سهمِ passed/bounced/pending - همون سه عدد به‌صورتِ weight، دادهٔ جدید نیست.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 11.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                    ) {
                        // فقط سهم‌های واقعی کشیده می‌شوند؛ قبلاً سهمِ صفر هم ۰٫۰۰۱ می‌گرفت و با یک
                        // چکِ بایگانی‌شده نوار سه تکه‌ی مساوی (سبز/قرمز/خاکستری) نشان می‌داد.
                        val parts = listOf(stats.passed to AppPrimary, stats.bounced to AppDanger, stats.pending to AppPrimaryPill)
                            .filter { it.first > 0 }
                        if (parts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().background(AppPrimaryPill))
                        } else {
                            parts.forEach { (count, color) ->
                                Box(modifier = Modifier.weight(count.toFloat()).fillMaxSize().background(color))
                            }
                        }
                    }
                }
            }
        }
        item {
            AppCard {
                Column {
                    Text("چک‌های وضع‌نشده", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
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
            val exportGuard = ir.sadteam.loancalc.ui.subscription.premiumGuard()
            AppCard {
                Column {
                    Text("خروجی گزارش", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        ChequeReportExportTile(
                            icon = Icons.Outlined.Description,
                            label = "دانلود PDF",
                            onClick = { exportGuard("export", "خروجیِ PDF و اکسل", onDownloadPdf) },
                            modifier = Modifier.weight(1f),
                        )
                        ChequeReportExportTile(
                            icon = Icons.Outlined.GridOn,
                            label = "دانلود اکسل",
                            onClick = { exportGuard("export", "خروجیِ PDF و اکسل", onDownloadXlsx) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (color == AppText || color == AppMuted) AppMuted.pillOverSurface(0.08f) else color.pillOverSurface(0.10f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            .height(62.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(color.pillOverSurface(0.12f))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(ir.sadteam.loancalc.ui.theme.AppSurface, CircleShape),
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
    // دکمه‌ی کپسولیِ حاشیه‌دار (طرحِ Claude Design).
    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(999.dp))
            .border(1.5.dp, ir.sadteam.loancalc.ui.theme.AppLine, RoundedCornerShape(999.dp))
            .pressScaleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(19.dp))
        Text(label, color = AppPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}
