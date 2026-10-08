package ir.sadteam.loancalc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.runtime.MutableState

@Composable
internal fun ResultSaveSection(
    outcome: BankLoanOutcome,
    myLoansViewModel: MyLoansViewModel,
    gateState: GateState?,
    canSaveAnotherLoan: Boolean,
    savedState: MutableState<Boolean>,
    saveMessageState: MutableState<String?>,
    savingState: MutableState<Boolean>,
    paidCountTextState: MutableState<String>,
) {
    var saved by savedState
    var saveMessage by saveMessageState
    var saving by savingState
    var paidCountText by paidCountTextState
            // این صفحه بعدِ ذخیره جایی نمی‌ره (برخلافِ فرمِ افزودنِ دستی/چک که می‌بندن) - همینجا
            // دکمه با این متنِ تاییدی عوض می‌شه. قبلاً این تعویض یهویی بود؛ الان با AnimatedVisibility
            // یه ورودِ فنریِ کوچیک (بزرگ‌شدن از ۰.۸ + محو) داره.
            AnimatedVisibility(
                visible = saved,
                enter = fadeIn(tween(Motion.FADE_IN_MS)) + scaleIn(animationSpec = Motion.snappy(), initialScale = 0.8f),
            ) {
                Text(
                    "✓ وام تو «وام‌های من» ذخیره شد",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            }
            if (!saved) {
                AppCard(label = "تعداد اقساط پرداخت‌شده (اختیاری)", modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(
                        "اگه این وام از قبل هست و چندتا قسطش رو پرداخت کردی، اینجا بنویس",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedTextField(
                        value = paidCountText,
                        onValueChange = { paidCountText = cleanNum(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                }
                GradientButton(
                    enabled = !saving,
                    onClick = {
                        if (saving) return@GradientButton
                        val paidCount = (paidCountText.toIntOrNull() ?: 0).coerceIn(0, outcome.n)
                        when {
                            canSaveAnotherLoan -> {
                                saving = true
                                myLoansViewModel.saveComputedLoan(outcome, paidCount) {
                                    saving = false
                                    saved = true
                                    saveMessage = null
                                }
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
                    if (saving) {
                        LottieSpinner(modifier = Modifier.size(18.dp))
                    } else {
                        Text("ذخیره وام", fontWeight = FontWeight.Bold)
                    }
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

@Composable
internal fun ResultActionsSection(
    privacyMode: Boolean,
    outcome: BankLoanOutcome,
    result: ir.sadteam.loancalc.core.LoanResult,
    interval: Int,
    dueDates: List<PersianDate>,
    endDate: PersianDate,
) {
            StaggerIn(4) {
            AppCard {
                // فریمِ `68b` بندِ ۳: جدول **خلاصه** می‌گیرد - همان گپی که در فرمِ افزودن
                // هم بود. کاربر جدولِ دوازده‌ردیفی را اسکرول می‌کرد تا جمع و تاریخِ پایان
                // را پیدا کند، و هیچ‌کدام در جدول نبودند.
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text("جدولِ کاملِ اقساط", color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "${toFa(outcome.n)} قسط · جمعِ ${maskIfPrivate(masked, amountToman(result.totalPaid))} تومان · تا ${toFa(endDate.d)} ${persianMonthName(endDate.m)} ${toFa(endDate.y)}",
                            color = AppMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                // و **سرستون** می‌گیرد: سه ستونِ بی‌برچسب یعنی کاربر باید حدس بزند عددِ
                // سمتِ چپ مبلغِ قسط است یا ماندهٔ بدهی.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("شماره", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                    Text("سررسید", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                    Text("مبلغِ قسط", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                }
                HorizontalDivider(color = AppLine)

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
                    // start=10.dp (نه end) چون RTLه - رجوع کن به همین رفع رو LoanDetailScreen: start
                    // تو RTL یعنی سمتِ راست، دقیقاً همونجایی که اسکرول‌بار (رسمِ raw canvas، مستقل از
                    // جهت) کشیده می‌شه؛ بدونش متنِ «قسط N» زیرِ اسکرول‌بار می‌رفت.
                    contentPadding = PaddingValues(start = 10.dp),
                ) {
                    itemsIndexed(result.rows, key = { _, row -> row.month }) { idx, row ->
                        val due = dueDates[idx]
                        val dateLabel = if (interval >= 28) {
                            "${persianMonthName(due.m)} ${toFa(due.y)}"
                        } else {
                            "${toFa(due.d)} ${persianMonthName(due.m)}"
                        }
                        Column(Modifier.fillMaxWidth().height(rowH)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("قسط ${toFa(row.month)}", fontSize = 12.sp)
                                Text(dateLabel, fontSize = 12.sp)
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text(
                                        "${maskIfPrivate(masked, amountToman(row.installment))} تومان",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            if (idx != result.rows.lastIndex) HorizontalDivider(color = AppLine)
                        }
                    }
                }
            }
            }
}
