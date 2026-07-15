package ir.sadteam.loancalc.ui.stats

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

        OutlinedButton(
            onClick = { createDocumentLauncher.launch("gozaresh-vamha.pdf") },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
        ) {
            Text("دانلود گزارش PDF")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            ProgressDonut(ratio = summary.progressRatio)
        }

        val statItems = listOf(
            "تعداد وام‌ها" to toFa(summary.loanCount),
            "مجموع مبلغ وام‌ها" to "${fmt(summary.totalAmount)} ریال",
            "مجموع پرداخت‌شده" to "${fmt(summary.paidAmount)} ریال",
            "مانده‌ی کل" to "${fmt(summary.remainingAmount)} ریال",
            "اقساط پرداخت‌شده" to "${toFa(summary.paidInstallments)} از ${toFa(summary.totalInstallments)}",
            "درصد پیشرفت" to "${toFa((summary.progressRatio * 100).toInt())}٪",
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
                    rowItems.forEach { (title, value) ->
                        AppCard(label = title, modifier = Modifier.weight(1f)) {
                            Text(value, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressDonut(ratio: Double) {
    val sweep = (ratio.coerceIn(0.0, 1.0) * 360f).toFloat()
    val trackColor = AppLine
    val progressColor = AppPrimary
    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val strokeWidth = 16.dp.toPx()
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
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${toFa((ratio * 100).toInt())}٪",
                color = AppText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("پرداخت‌شده", color = AppMuted, fontSize = 11.sp)
        }
    }
}
