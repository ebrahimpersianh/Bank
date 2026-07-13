package ir.sadteam.loancalc.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.ordinalFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

private val faMonthNamesResult = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

@Composable
fun ResultScreen(outcome: BankLoanOutcome) {
    val result = outcome.result

    // هم‌راستا با renderTable تو www/index.html: اول گریس‌پیریود بعد فاصله‌ی هر قسط اضافه می‌شه
    val interval = result.intervalDays
    val dueDates = remember(result, outcome.startDate) {
        var c = outcome.startDate
        if (result.graceMonths > 0) c = PersianCalendar.addDays(c, result.graceMonths * 30)
        result.rows.map {
            c = PersianCalendar.addDays(c, interval)
            c
        }
    }
    val endDate = dueDates.lastOrNull() ?: outcome.startDate

    val interestPct = if (result.principal > 0) result.totalInterest / result.principal * 100 else 0.0
    val feeAmount = if (outcome.method == LoanMethod.QARZ) result.totalInterest else 0.0

    var animatedInstallment by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(result.installment) {
        animateValue(0f, result.installment.toFloat()) { animatedInstallment = it }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(6.dp, 14.dp, 6.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text(
                text = if (outcome.borrower != "—") "وام ${outcome.borrower}" else "نتیجه محاسبه",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }

        item {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                LoanRing(
                    principal = result.principal,
                    interest = result.totalInterest,
                    modifier = Modifier.size(180.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(fmt(animatedInstallment.toDouble()), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("قسط ماهانه (ریال)", fontSize = 10.5.sp, color = AppMuted)
                }
            }
        }

        item {
            Text(
                text = "${numberToWordsFa(result.installment / 10)} تومان",
                color = AppAccent,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatBox("کل بازپرداخت (ریال)", fmt(result.totalPaid), Modifier.weight(1.3f))
                StatBox("سررسید هر ماه", ordinalFa(outcome.startDate.d), Modifier.weight(1f))
                StatBox("مدت وام", "${toFa(outcome.n)} ماه", Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val pctText = if (interestPct < 10) {
                    toFa(String.format("%.1f", interestPct))
                } else {
                    toFa(interestPct.roundToLong().toString())
                }
                StatBox("سود نسبت به اصل وام", "$pctText٪", Modifier.weight(1f))
                StatBox(
                    "تاریخ پایان وام",
                    "${toFa(endDate.d)} ${faMonthNamesResult[endDate.m - 1]} ${toFa(endDate.y)}",
                    Modifier.weight(1f),
                )
            }
        }

        if (feeAmount > 0) {
            item {
                val rateLabel = if (outcome.ratePct == outcome.ratePct.toLong().toDouble()) {
                    outcome.ratePct.toLong().toString()
                } else {
                    outcome.ratePct.toString()
                }
                StatBox(
                    "کارمزد سالانه (قرض‌الحسنه)",
                    "${fmt(feeAmount)} ریال (${toFa(rateLabel)}٪ سالانه)",
                    Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            AppCard(label = "جدول کامل اقساط") {
                Column(Modifier.fillMaxWidth()) {
                    result.rows.forEachIndexed { idx, row ->
                        val due = dueDates[idx]
                        val dateLabel = if (interval >= 28) {
                            "${faMonthNamesResult[due.m - 1]} ${toFa(due.y)}"
                        } else {
                            "${toFa(due.d)} ${faMonthNamesResult[due.m - 1]}"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("قسط ${toFa(row.month)}", fontSize = 12.sp)
                            Text(dateLabel, fontSize = 12.sp)
                            Text("${fmt(row.installment)} ریال", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        if (idx != result.rows.lastIndex) HorizontalDivider(color = AppLine)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            Text(label, fontSize = 9.sp, color = AppMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun LoanRing(principal: Double, interest: Double, modifier: Modifier = Modifier) {
    val total = principal + interest
    val principalFrac = if (total > 0) (principal / total).toFloat() else 0f
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.1f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        drawArc(
            color = Color(0xFF1D2A46),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = AppPrimary,
            startAngle = -90f,
            sweepAngle = 360f * principalFrac,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = AppAccent,
            startAngle = -90f + 360f * principalFrac,
            sweepAngle = 360f * (1f - principalFrac),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

private suspend fun animateValue(from: Float, to: Float, durationMs: Long = 500, onUpdate: (Float) -> Unit) {
    val start = System.currentTimeMillis()
    while (true) {
        val elapsed = System.currentTimeMillis() - start
        val p = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
        val eased = 1f - (1f - p) * (1f - p) * (1f - p)
        onUpdate(from + (to - from) * eased)
        if (p >= 1f) break
        delay(16)
    }
}
