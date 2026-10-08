@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ir.sadteam.loancalc.ui.myloans

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.lazy.LazyListScope

internal fun LazyListScope.loanIdentityItem(
    loan: LoanEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: MyLoansViewModel,
    isManualLoan: Boolean,
    privacyMode: Boolean,
    displayInstallment: Double,
    installmentsVary: Boolean,
    paidFraction: Float,
    ratePct: Double,
    overdueCount: Int,
    settled: Boolean,
    nextDueLabel: String?,
    nextDueInDays: Int?,
    showEditMetaDialogState: MutableState<Boolean>,
    editMetaNameState: MutableState<String>,
    editMetaBankState: MutableState<String>,
    editMetaBorrowerState: MutableState<String>,
    editMetaYearState: MutableState<Int>,
    editMetaMonthState: MutableState<Int>,
    editMetaDayState: MutableState<Int>,
    editMetaGraceMonthsState: MutableState<Int>,
    editMetaAmountTextState: MutableState<String>,
    editMetaNTextState: MutableState<String>,
    detailTabState: MutableState<Int>,
) {
    var showEditMetaDialog by showEditMetaDialogState
    var editMetaName by editMetaNameState
    var editMetaBank by editMetaBankState
    var editMetaBorrower by editMetaBorrowerState
    var editMetaYear by editMetaYearState
    var editMetaMonth by editMetaMonthState
    var editMetaDay by editMetaDayState
    var editMetaGraceMonths by editMetaGraceMonthsState
    var editMetaAmountText by editMetaAmountTextState
    var editMetaNText by editMetaNTextState
    var detailTab by detailTabState
        item {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
            }
            // 🚨 **نامِ بانک از جدولِ مشخصات به سرصفحه آمد** (بندِ ۲ی بخشِ ۸۰): نامِ بانک
            // **هویتِ** وام است نه یکی از مشخصاتش، پس کنارِ نامِ وام می‌نشیند نه در فهرست.
            // نامِ وام از سرصفحه به **کارتِ هویت** رفت (طرحِ مرجعِ کاربر، ۳۱ شهریور):
            // آن‌جا کنارِ نشانِ بانک و وضعیت می‌نشیند و یک‌جا می‌گوید «این کدام وام است».
            Column(modifier = Modifier.padding(start = 4.dp).weight(1f)) {
                Text(
                    loan.name,
                    color = AppText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = {
                if (isManualLoan) {
                    onEdit()
                } else {
                    editMetaName = loan.name
                    editMetaBank = loan.bank
                    editMetaBorrower = viewModel.getLoanBorrower(loan).let { if (it == "—") "" else it }
                    val sd = viewModel.getLoanStartDate(loan)
                    editMetaYear = sd.y
                    editMetaMonth = sd.m
                    editMetaDay = sd.d
                    editMetaGraceMonths = viewModel.getLoanGraceMonths(loan)
                    // بازبینیِ ۹ مهر: `loan.amount` برای وامِ با دوره‌ی تنفس «اصل + سودِ تنفس» است؛ اگر همان
                    // دوباره به محاسبه برود، سودِ تنفس با هر ذخیره یک بارِ دیگر اضافه می‌شد. اصلِ وام برگردانده می‌شود.
                    val graceForEdit = viewModel.getLoanGraceMonths(loan)
                    val originalPrincipal = if (graceForEdit > 0) {
                        loan.amount / (1 + viewModel.getLoanRatePct(loan) / 100.0 / 365.0 * (graceForEdit * 30))
                    } else loan.amount
                    editMetaAmountText = rialToToman(Math.round(originalPrincipal)).toString()
                    editMetaNText = loan.n.toString()
                    showEditMetaDialog = true
                }
            }) {
                // کاربر فکر می‌کرد ویرایش حذف شده (۶ مهر) - مدادِ خاکستری دیده نمی‌شد.
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش مشخصات وام", tint = AppPrimaryInk, modifier = Modifier.size(19.dp))
                }
            }
        }

        LoanIdentityCard(
            name = loan.name,
            bank = loan.bank,
            settled = settled,
            overdue = overdueCount > 0,
            amount = loan.amount,
            months = loan.n,
            ratePct = ratePct,
            startLabel = remember(loan) {
                val sd = viewModel.getLoanStartDate(loan)
                "${toFa(sd.y)}/${toFa(sd.m)}/${toFa(sd.d)}"
            },
            paidFraction = paidFraction,
            privacyMode = privacyMode,
        )
        LoanKeyStatsCard(
            total = loan.n,
            installment = displayInstallment,
            installmentLabel = if (installmentsVary) "قسطِ بعدی" else "مبلغِ هر قسط",
            nextDueLabel = nextDueLabel,
            dueInDays = nextDueInDays,
            privacyMode = privacyMode,
        )
        SegmentedToggle(
            // «پرداخت‌ها» حذف شد (۶ مهر): همان جدولِ اقساط بود، فقط فیلترشده.
            options = listOf("جدولِ اقساط", "جزئیاتِ وام"),
            selectedIndex = detailTab,
            onSelect = { detailTab = it },
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        }
        }
}

internal fun LazyListScope.loanTabZeroItems(
    swipeShift: Modifier,
    rowsState: MutableState<List<Map<String, Any?>>>,
    bulkPayModeState: MutableState<Boolean>,
    selectedBulkMsState: MutableState<Set<Int>>,
    detailTabState: MutableState<Int>,
) {
    var rows by rowsState
    var bulkPayMode by bulkPayModeState
    var selectedBulkMs by selectedBulkMsState
    var detailTab by detailTabState
        if (detailTab == 0) {
            item {
                Column(modifier = swipeShift) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "اقساط",
                            color = AppMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        // پرداختِ گروهی: به‌جای تک‌تک زدنِ هر قسط، چندتا رو انتخاب می‌کنیم و یه‌جا پرداخت
                        // می‌کنیم - فقط وقتی حداقل یه قسطِ پرداخت‌نشده داشته باشیم معنی داره.
                        if (rows.any { it["paid"] != true }) {
                            TextButton(onClick = {
                                bulkPayMode = !bulkPayMode
                                selectedBulkMs = emptySet()
                            }) {
                                Text(
                                    if (bulkPayMode) "انصراف" else "پرداخت گروهی",
                                    color = if (bulkPayMode) AppDanger else AppPrimary,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = bulkPayMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (selectedBulkMs.isEmpty()) "چندتا قسطِ پرداخت‌نشده رو انتخاب کن" else "${toFa(selectedBulkMs.size)} قسط انتخاب شده",
                                color = AppMuted,
                                fontSize = 12.sp,
                            )
                        }
                    }

                }
            }
        }
}

internal fun LazyListScope.loanTabOneItems(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    privacyMode: Boolean,
    today: PersianDate,
    swipeShift: Modifier,
    ratePct: Double,
    rowsState: MutableState<List<Map<String, Any?>>>,
    detailTabState: MutableState<Int>,
) {
    var rows by rowsState
    var detailTab by detailTabState
        if (detailTab == 1) {
            item { Box(swipeShift) { PaymentRhythm(rows = rows, today = today, onOpenAll = { detailTab = 0 }) } }
            item { Box(swipeShift) {
                val lastDue = rows.lastOrNull()?.get("dueDate") as? Map<*, *>
                LoanSpecsCard(
                    amount = loan.amount,
                    ratePct = ratePct,
                    n = loan.n,
                    borrower = remember(loan) { viewModel.getLoanBorrower(loan) },
                    endLabel = lastDue?.let {
                        val y = (it["y"] as? Number)?.toInt()
                        val mo = (it["m"] as? Number)?.toInt()
                        if (y != null && mo != null) "${persianMonthName(mo)} ${toFa(y)}" else null
                    },
                    totalInterest = remember(rows, loan) {
                        if (rows.isEmpty()) null else {
                            val sum = rows.sumOf { (it["installment"] as? Number)?.toDouble() ?: loan.installment }
                            (sum - loan.amount).takeIf { it > 0.0 }
                        }
                    },
                    privacyMode = privacyMode,
                )
            } }
            item { Box(swipeShift) {
                run {
                    var attachmentTab by remember(loan.id) { mutableStateOf(0) }
                    var noteText by remember(loan.id) { mutableStateOf(viewModel.getLoanNotes(loan)) }
                    var noteDirty by remember(loan.id) { mutableStateOf(false) }

                    // طرحِ ChatGPT (۷ مهر، پایینِ جزئیاتِ وام): یک کارت با دو تبِ «یادداشت / عکس رسید» -
                    // به‌جای دو دکمه‌ی بازشونده که محتوایشان پایین‌ترِ صفحه گم می‌شد.
                    AppCard(modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                        Column {
                            SegmentedToggle(
                                options = listOf("یادداشت", "عکس رسید"),
                                selectedIndex = attachmentTab,
                                onSelect = { attachmentTab = it },
                                icons = listOf(Icons.Filled.EditNote, Icons.Filled.PhotoCamera),
                            )
                            if (attachmentTab == 0) {
                                Text(
                                    "برای افزودن عنوان، یکی از موارد زیر را انتخاب کنید.",
                                    color = AppMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                )
                                androidx.compose.foundation.layout.FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                ) {
                                    listOf("شماره حساب", "شماره کارت", "شماره پیگیری", "توضیحات").forEach { preset ->
                                        AppChip(
                                            label = preset,
                                            selected = false,
                                            onClick = {
                                                noteText = if (noteText.isBlank()) "$preset: " else "$noteText\n$preset: "
                                                noteDirty = true
                                            },
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it; noteDirty = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                                    trailingIcon = {
                                        if (noteText.isNotEmpty()) {
                                            IconButton(onClick = { noteText = ""; noteDirty = true }) {
                                                Icon(Icons.Filled.Close, contentDescription = "پاک‌کردنِ یادداشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                                            }
                                        }
                                    }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(),)
                                GradientButton(
                                    onClick = {
                                        viewModel.updateLoanNotes(loan, noteText)
                                        noteDirty = false
                                    },
                                    enabled = noteDirty,
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                ) {
                                    Text(if (noteDirty) "ذخیره" else "ذخیره شد")
                                }
                            } else {
                                PhotoAttachmentCard(
                                    photoPath = loan.photoPath,
                                    onPick = { uri -> viewModel.setLoanPhoto(loan, uri) },
                                    onRemove = { viewModel.removeLoanPhoto(loan) },
                                    withCard = false,
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                )
                            }
                        }
                    }
                }

            } }
        }
}

internal fun LazyListScope.loanInstallmentItems(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    privacyMode: Boolean,
    nextRow: Map<String, Any?>?,
    swipeShift: Modifier,
    shownRows: List<Map<String, Any?>>,
    editingRowMState: MutableState<Int?>,
    editAmountTextState: MutableState<String>,
    payChoiceMState: MutableState<Int?>,
    confirmUnmarkMState: MutableState<Int?>,
    photoRowMState: MutableState<Int?>,
    bulkPayModeState: MutableState<Boolean>,
    selectedBulkMsState: MutableState<Set<Int>>,
) {
    var editingRowM by editingRowMState
    var editAmountText by editAmountTextState
    var payChoiceM by payChoiceMState
    var confirmUnmarkM by confirmUnmarkMState
    var photoRowM by photoRowMState
    var bulkPayMode by bulkPayModeState
    var selectedBulkMs by selectedBulkMsState
        items(shownRows, key = { "row-" + ((it["m"] as? Number)?.toInt() ?: 0) }) { row ->
            val m = (row["m"] as? Number)?.toInt() ?: 0
            InstallmentRow(
                modifier = Modifier.animateItem().then(swipeShift).padding(horizontal = 14.dp),
                row = row,
                loan = loan,
                privacyMode = privacyMode,
                bulkPayMode = bulkPayMode,
                selected = m in selectedBulkMs,
                dueInDays = (row["dueDate"] as? Map<*, *>)?.let { due ->
                    val y = (due["y"] as? Number)?.toInt()
                    val mm = (due["m"] as? Number)?.toInt()
                    val d = (due["d"] as? Number)?.toInt()
                    if (y != null && mm != null && d != null) {
                        viewModel.daysUntilToday(PersianDate(y, mm, d))
                    } else {
                        null
                    }
                },
                isNext = row === nextRow,
                onTogglePaid = { rowM, paid ->
                    if (bulkPayMode) {
                        if (row["paid"] != true) {
                            selectedBulkMs = if (rowM in selectedBulkMs) {
                                selectedBulkMs - rowM
                            } else {
                                selectedBulkMs + rowM
                            }
                        }
                    } else if (!paid) {
                        // تپ **فقط می‌زند، برنمی‌دارد** (`64c`)؛ برداشتن از منویِ ردیف با دیالوگِ تایید.
                        payChoiceM = rowM
                    }
                },
                onUnmark = { rowM -> confirmUnmarkM = rowM },
                onOpenPhoto = { rowM -> photoRowM = rowM },
                onEditAmount = { rowM, installment ->
                    editingRowM = rowM
                    editAmountText = rialToToman(installment.toLong()).toString()
                },
            )
        }
}
