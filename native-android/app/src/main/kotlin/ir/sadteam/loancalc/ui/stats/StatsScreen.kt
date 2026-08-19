package ir.sadteam.loancalc.ui.stats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * پورت مفهومی «آمار و گزارشات» اپ رقیب (VAMMAN) - ۶ کارت آماری بزرگ + یه نمودار دونات ساده (رسم
 * دستی با Canvas، بدون کتابخونه‌ی نمودارِ جدید) + دانلود PDF (با [StatsPdfExporter]، از
 * `android.graphics.pdf.PdfDocument` خودِ Android، نه کتابخونه‌ی PDF شخص‌ثالث).
 */
@Composable
fun StatsScreen(onBack: () -> Unit, viewModel: StatsViewModel = hiltViewModel()) {
    val loans by viewModel.loans.collectAsState()
    val summary = remember(loans) { viewModel.summarize(loans) }
    // paymentHistory دیگه نمی‌تونه محاسبه‌ی همزمان (remember{}) باشه چون از رو رَدیف‌های واقعیِ Room
    // (loan_rows) می‌خونه، نه دیگه از رو JSONِ درون‌حافظه‌ای - رجوع کن به CLAUDE.md.
    var paymentHistory by remember { mutableStateOf<List<PaymentHistoryPoint>>(emptyList()) }
    LaunchedEffect(loans) {
        paymentHistory = viewModel.paymentHistory(loans)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        StatsPdfExporter.export(summary, loans, out)
                    }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    val message = if (ok) "PDF ذخیره شد" else "ذخیره‌ی PDF ناموفق بود"
                    banner.show(message, isSuccess = ok)
                }
            }
        }
    }

    val createXlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        StatsXlsxExporter.export(summary, loans, out)
                    }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    val message = if (ok) "اکسل ذخیره شد" else "ذخیره‌ی اکسل ناموفق بود"
                    banner.show(message, isSuccess = ok)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text("آمار و گزارشات", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
        }

        val heroAccent = Brush.linearGradient(listOf(AppPrimary.copy(alpha = 0.34f), AppPrimaryDim.copy(alpha = 0.10f)))
        AppCard(
            accentGradient = heroAccent,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ProgressDonut(ratio = summary.progressRatio)
                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("پرداخت‌شده", color = AppMuted, fontSize = 11.sp)
                    Text(
                        "${fmt(summary.paidAmount)} ریال",
                        color = AppPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text("مانده", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                    Text(
                        "${fmt(summary.remainingAmount)} ریال",
                        color = AppText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatsExportTile(
                icon = Icons.Outlined.Description,
                label = "دانلود PDF",
                onClick = { createDocumentLauncher.launch("gozaresh-vamha.pdf") },
                modifier = Modifier.weight(1f),
            )
            StatsExportTile(
                icon = Icons.Outlined.GridOn,
                label = "دانلود اکسل",
                onClick = { createXlsxLauncher.launch("gozaresh-vamha.xlsx") },
                modifier = Modifier.weight(1f),
            )
        }

        val statItems = listOf(
            StatItem("تعداد وام‌ها", toFa(summary.loanCount), null),
            StatItem("مجموع مبلغ وام‌ها", fmt(summary.totalAmount), "ریال"),
            StatItem("مجموع پرداخت‌شده", fmt(summary.paidAmount), "ریال"),
            StatItem("مانده‌ی کل", fmt(summary.remainingAmount), "ریال"),
            StatItem("اقساط پرداخت‌شده", toFa(summary.paidInstallments), "از ${toFa(summary.totalInstallments)}"),
            StatItem("درصد پیشرفت", toFa((summary.progressRatio * 100).toInt()), "٪"),
        )
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            statItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { item -> StatTile(item = item, modifier = Modifier.weight(1f)) }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (paymentHistory.isNotEmpty()) {
            AppCard(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("تاریخچه پرداخت (تجمعی)", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${fmt(paymentHistory.last().cumulativeAmount)} ریال", color = AppInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                PaymentHistoryLineChart(points = paymentHistory, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** مدتِ انیمیشنِ ورودِ هر دو نمودار (میلی‌ثانیه). */
private const val CHART_ANIM_MS = 900

@Composable
private fun ProgressDonut(ratio: Double) {
    val targetSweep = (ratio.coerceIn(0.0, 1.0) * 360f).toFloat()
    // قبلاً کمانِ پیشرفت یهو کاملاً رسم می‌شد. حالا هر بار مقدارِ واقعی عوض بشه (یا صفحه اولین بار
    // باز بشه)، از صفر تا مقدارِ واقعی می‌چرخه - همون حسِ نموداری که تویِ اپ‌های مالیِ خوب هست.
    val animatedSweep = remember { Animatable(0f) }
    LaunchedEffect(targetSweep) {
        animatedSweep.animateTo(targetSweep, tween(CHART_ANIM_MS, easing = FastOutSlowInEasing))
    }
    val trackColor = AppLine
    val progressColor = AppPrimary
    Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(112.dp)) {
            val strokeWidth = 15.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedSweep.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                // درصدِ متنی هم هم‌قدمِ خودِ کمان بالا می‌ره - چیدنِ عدد و کمانِ درحالِ‌رشد
                // یهویی/ناهماهنگ به‌نظر می‌رسید.
                "${toFa((animatedSweep.value / 360f * 100).toInt())}٪",
                color = AppText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("پرداخت‌شده", color = AppMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun StatsExportTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppInfo.copy(alpha = 0.12f))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
        Text(label, color = AppInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

private data class StatItem(val title: String, val value: String, val unit: String?)

@Composable
private fun StatTile(item: StatItem, modifier: Modifier = Modifier) {
    // مقادیرِ کوتاه (تعدادها) ۲۰.sp، مبالغ (رشته‌ی طولانی‌تر) ۱۷.sp - همون معیارِ طولِ رشته که
    // استایلِ قبلی هم ضمنی رعایت می‌کرد.
    val big = item.value.length <= 3
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(26.dp).background(AppPrimary.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {}
            Text(item.title, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
            Text(item.value, color = AppText, fontSize = if (big) 20.sp else 17.sp, fontWeight = FontWeight.Black)
            if (item.unit != null) {
                Text(" ${item.unit}", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 1.dp))
            }
        }
    }
}

/** پورت نمودار خطی «تاریخچه پرداخت» اپ رقیب (VAMMAN) - رسم دستی با Canvas (بدون کتابخونه‌ی نمودارِ
 * جدید)، عین دونات بالا؛ نقاط از [StatsViewModel.paymentHistory] (مجموع تجمعی اقساط پرداخت‌شده به
 * تفکیک ماه شمسی) میان. */
@Composable
private fun PaymentHistoryLineChart(points: List<PaymentHistoryPoint>, modifier: Modifier = Modifier) {
    val lineColor = AppInfo
    val fillColors = listOf(AppInfo.copy(alpha = 0.34f), Color.Transparent)
    val gridColor = AppLine
    // قبلاً کلِ خط یهو رسم می‌شد. حالا از چپ به راست «کشیده» می‌شه - با کلیپ‌کردنِ بومِ رسم به یه
    // عرضِ روبه‌رشد، نه با استخراجِ بخشی از مسیر (که برای این تعداد نقطه‌ی کم اضافه‌کاریه).
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(CHART_ANIM_MS, easing = FastOutSlowInEasing))
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
        ) {
            val maxAmount = points.maxOf { it.cumulativeAmount }.coerceAtLeast(1.0)
            val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f

            drawLine(
                color = gridColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )

            clipRect(right = size.width * reveal.value) {
                if (points.size == 1) {
                    val y = size.height - (points[0].cumulativeAmount / maxAmount * size.height).toFloat()
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(size.width / 2f, y))
                    return@clipRect
                }

                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = stepX * index
                    val y = size.height - (point.cumulativeAmount / maxAmount * size.height).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // پرشدنِ گرادیانیِ زیرِ خط - مسیرِ خط تا کفِ بوم بسته می‌شه (بدون تغییرِ خودِ path).
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(stepX * (points.size - 1), size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = fillPath, brush = Brush.verticalGradient(fillColors))
                drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                points.forEachIndexed { index, point ->
                    val x = stepX * index
                    val y = size.height - (point.cumulativeAmount / maxAmount * size.height).toFloat()
                    drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(points.first().label, color = AppMuted, fontSize = 11.sp)
            if (points.size > 1) {
                Text(points.last().label, color = AppMuted, fontSize = 11.sp)
            }
        }
    }
}
