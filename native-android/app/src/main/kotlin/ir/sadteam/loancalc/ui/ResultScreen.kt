package ir.sadteam.loancalc.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
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

    // شمارش صعودی اعداد (پورت animateNumber وب) - رو Double تا برای مبالغ میلیاردی خطای گردکردن
    // Float (که تا چند صد ریال می‌رسید) پیش نیاد.
    var animatedInstallment by remember { mutableStateOf(0.0) }
    LaunchedEffect(result.installment) {
        animateValue(0.0, result.installment) { animatedInstallment = it }
    }
    var animatedTotal by remember { mutableStateOf(0.0) }
    LaunchedEffect(result.totalPaid) {
        animateValue(0.0, result.totalPaid) { animatedTotal = it }
    }
    // حلقه‌ی دونات با یه sweep از صفر «کشیده» می‌شه (حس پریمیوم‌تر از ظاهر شدن یهویی).
    val ringProgress = remember { Animatable(0f) }
    LaunchedEffect(result) {
        ringProgress.snapTo(0f)
        ringProgress.animateTo(1f, animationSpec = tween(900, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }

    // دکمه‌ی «ذخیره وام»: همون محدودیتِ «۱ وام رایگان» تب «وام‌های من» رو رعایت می‌کنه
    // (canSaveAnotherLoan = هیچ وامی ذخیره نشده، یا واردشده‌ی مشترک).
    val myLoansViewModel: MyLoansViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val savedLoans by myLoansViewModel.loans.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = savedLoans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)
    var saved by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
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
                    progress = ringProgress.value,
                    modifier = Modifier.size(180.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(fmt(animatedInstallment), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("قسط ماهانه (ریال)", fontSize = 12.5.sp, color = AppMuted)
                }
            }
        }

        item {
            Text(
                text = "${numberToWordsFa(result.installment / 10)} تومان",
                color = AppAccent,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            )
        }

        item {
            if (saved) {
                Text(
                    "✓ وام تو «وام‌های من» ذخیره شد",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            } else {
                GradientButton(
                    onClick = {
                        when {
                            canSaveAnotherLoan -> myLoansViewModel.saveComputedLoan(outcome) {
                                saved = true
                                saveMessage = null
                            }
                            gateState == null -> Unit
                            gateState != GateState.LOGGED_IN ->
                                saveMessage = "برای ذخیره‌ی وام دوم اول باید وارد بشی — از تب «وام‌های من» وارد شو"
                            else ->
                                saveMessage = "برای ذخیره‌ی بیش از یک وام باید اشتراک بگیری — از تب «وام‌های من»"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                ) {
                    Text("ذخیره وام", fontWeight = FontWeight.Bold)
                }
                if (saveMessage != null) {
                    Text(
                        saveMessage!!,
                        color = AppDanger,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatBox("کل بازپرداخت (ریال)", fmt(animatedTotal), Modifier.weight(1.3f))
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
                // حداکثر ۵ قسط تو صفحه جا می‌شه، بقیه با اسکرول - کنارش یه اسکرول‌بار سبز نشون می‌ده
                // چقدر پایین رفتیم (خواسته‌ی کاربر).
                val tableState = rememberLazyListState()
                val rowH = 48.dp
                val visibleRows = minOf(result.rows.size, 5)
                LazyColumn(
                    state = tableState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowH * visibleRows)
                        .lazyColumnScrollbar(tableState, AppPrimary),
                ) {
                    itemsIndexed(result.rows) { idx, row ->
                        val due = dueDates[idx]
                        val dateLabel = if (interval >= 28) {
                            "${faMonthNamesResult[due.m - 1]} ${toFa(due.y)}"
                        } else {
                            "${toFa(due.d)} ${faMonthNamesResult[due.m - 1]}"
                        }
                        Column(Modifier.fillMaxWidth().height(rowH)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("قسط ${toFa(row.month)}", fontSize = 12.sp)
                                Text(dateLabel, fontSize = 12.sp)
                                Text("${fmt(row.installment)} ریال", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            if (idx != result.rows.lastIndex) HorizontalDivider(color = AppLine)
                        }
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
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(label, fontSize = 9.sp, color = AppMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun LoanRing(principal: Double, interest: Double, progress: Float, modifier: Modifier = Modifier) {
    val total = principal + interest
    val principalFrac = if (total > 0) (principal / total).toFloat() else 0f
    // drawArc اجرا می‌شه تو DrawScope که @Composable نیست - رنگ‌های تم (که حالا @Composable
    // get() هستن، برای پشتیبانی از تم روشن) باید همینجا تو بدنه‌ی @Composable گرفته بشن، نه
    // مستقیم تو بلوک Canvas.
    val trackColor = AppSurface2
    val primaryColor = AppPrimary
    val accentColor = AppAccent
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.1f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        // هر دو کمان با [progress] از صفر «کشیده» می‌شن (انیمیشن sweep ورود به صفحه).
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = 360f * principalFrac * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = accentColor,
            startAngle = -90f + 360f * principalFrac * progress,
            sweepAngle = 360f * (1f - principalFrac) * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

private suspend fun animateValue(from: Double, to: Double, durationMs: Long = 500, onUpdate: (Double) -> Unit) {
    val start = System.currentTimeMillis()
    while (true) {
        val elapsed = System.currentTimeMillis() - start
        val p = (elapsed.toDouble() / durationMs).coerceIn(0.0, 1.0)
        val eased = 1.0 - (1.0 - p) * (1.0 - p) * (1.0 - p)
        onUpdate(from + (to - from) * eased)
        if (p >= 1.0) break
        delay(16)
    }
}
