package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

private val faMonthNamesDetail = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * پورت openDetail/renderTable تو www/index.html، برای وام‌های دستی (method=manual): هر قسط
 * وضعیت پرداخت مستقل داره و تاریخ سررسید واقعی (از startDate + intervalDays محاسبه می‌شه). تپ رو
 * قسطِ پرداخت‌نشده پورت openPayModal رو انجام می‌ده (انتخاب «به‌موقع» یا «با تاخیر» + تاریخ واقعی
 * پرداخت)؛ تپ رو قسطِ پرداخت‌شده مثل handlePayButton فوری برمی‌گردونه به حالت پرداخت‌نشده. ویرایش
 * دستی مبلغ هر قسط هم هست (پورت confirmEditInstallment)، همراه سوال «رو همه‌ی اقساط هم اعمال
 * کنم؟» بعد از ذخیره.
 */
@Composable
fun LoanDetailScreen(
    loan: LoanEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    viewModel: MyLoansViewModel = hiltViewModel(),
) {
    val rows = remember(loan) { viewModel.getRows(loan) }
    var editingRowM by remember { mutableStateOf<Int?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var applyAllPromptAmount by remember { mutableStateOf<Double?>(null) }
    var payChoiceM by remember { mutableStateOf<Int?>(null) }
    var lateDateM by remember { mutableStateOf<Int?>(null) }
    var lateYear by remember { mutableStateOf(1404) }
    var lateMonth by remember { mutableStateOf(1) }
    var lateDay by remember { mutableStateOf(1) }

    if (editingRowM != null) {
        AlertDialog(
            onDismissRequest = { editingRowM = null },
            title = { Text("ویرایش مبلغ قسط ${toFa(editingRowM ?: 0)}") },
            text = {
                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newAmount = editAmountText.toDoubleOrNull()
                    val m = editingRowM
                    if (newAmount != null && newAmount > 0 && m != null) {
                        viewModel.setRowInstallment(loan, m, newAmount) {
                            applyAllPromptAmount = newAmount
                        }
                    }
                    editingRowM = null
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { editingRowM = null }) { Text("انصراف") }
            },
        )
    }

    if (applyAllPromptAmount != null) {
        AlertDialog(
            onDismissRequest = { applyAllPromptAmount = null },
            title = { Text("اعمال به همه‌ی اقساط") },
            text = { Text("می‌خوای این مبلغ رو برای همه‌ی اقساط اعمال کنی؟") },
            confirmButton = {
                TextButton(onClick = {
                    applyAllPromptAmount?.let { viewModel.setAllRowsInstallment(loan, it) }
                    applyAllPromptAmount = null
                }) { Text("بله، رو همه اعمال کن") }
            },
            dismissButton = {
                TextButton(onClick = { applyAllPromptAmount = null }) { Text("نه") }
            },
        )
    }

    if (payChoiceM != null) {
        val m = payChoiceM!!
        AlertDialog(
            onDismissRequest = { payChoiceM = null },
            title = { Text("ثبت پرداخت قسط ${toFa(m)}") },
            text = { Text("این قسط سر موعد پرداخت شده یا با تاخیر؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setRowPaidOnTime(loan, m)
                    payChoiceM = null
                }) { Text("پرداخت به‌موقع") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val due = rows.firstOrNull { (it["m"] as? Number)?.toInt() == m }?.get("dueDate") as? Map<*, *>
                    lateYear = (due?.get("y") as? Number)?.toInt() ?: lateYear
                    lateMonth = (due?.get("m") as? Number)?.toInt() ?: lateMonth
                    lateDay = (due?.get("d") as? Number)?.toInt() ?: lateDay
                    lateDateM = m
                    payChoiceM = null
                }) { Text("پرداخت با تاخیر") }
            },
        )
    }

    if (lateDateM != null) {
        val m = lateDateM!!
        AlertDialog(
            onDismissRequest = { lateDateM = null },
            title = { Text("تاریخ واقعی پرداخت قسط ${toFa(m)}") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailDateDropdown(
                        options = (1380..1410).map { it to toFa(it) },
                        selected = lateYear,
                        onSelect = { lateYear = it },
                        modifier = Modifier.weight(1f),
                    )
                    DetailDateDropdown(
                        options = faMonthNamesDetail.mapIndexed { idx, name -> (idx + 1) to name },
                        selected = lateMonth,
                        onSelect = { lateMonth = it },
                        modifier = Modifier.weight(1f),
                    )
                    DetailDateDropdown(
                        options = (1..31).map { it to toFa(it) },
                        selected = lateDay,
                        onSelect = { lateDay = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setRowPaidLate(loan, m, PersianDate(lateYear, lateMonth, lateDay))
                    lateDateM = null
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { lateDateM = null }) { Text("انصراف") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            Text(loan.name, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        }

        // دایره‌ی شیک بالای وام (سبز = اصل، طلایی = سود) با قسط ماهانه تو مرکز - مثل نسخه‌ی وب.
        val principalFrac = if (loan.totalPaid > 0) (loan.amount / loan.totalPaid).toFloat() else 1f
        LoanDonut(
            principalFraction = principalFrac,
            centerTop = fmt(loan.installment),
            centerBottom = "قسط ماهانه (ریال)",
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )

        AppCard(label = loan.bank, modifier = Modifier.padding(horizontal = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("مبلغ هر قسط", fontSize = 13.sp, color = AppMuted)
                    Text("${fmt(loan.installment)} ریال", fontSize = 15.sp, color = AppText, fontWeight = FontWeight.Bold)
                    Text(
                        "${numberToWordsFa(loan.installment / 10)} تومان",
                        fontSize = 11.sp,
                        color = AppAccent,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Column {
                    Text("پرداخت‌شده", fontSize = 13.sp, color = AppMuted)
                    Text("${toFa(loan.paidCount)} از ${toFa(loan.n)}", fontSize = 15.sp, color = AppPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        PhotoAttachmentCard(
            photoPath = loan.photoPath,
            onPick = { uri -> viewModel.setLoanPhoto(loan, uri) },
            onRemove = { viewModel.removeLoanPhoto(loan) },
            modifier = Modifier.padding(horizontal = 14.dp),
        )

        // حداکثر ۵ قسط تو صفحه، بقیه با اسکرول مستقل - کنارش اسکرول‌بار سبز نشون می‌ده کجاییم.
        // هر قسط یه باکس مینیمالِ گوشه‌گرد با حاشیه‌ی سبزه (خواسته‌ی کاربر). وضعیت پرداخت:
        // به‌موقع=سبز «پرداخت شد»، با تأخیر=قرمز «با تأخیر»، پرداخت‌نشده=مشکی «پرداخت نشده».
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth(),
        ) {
            Text(
                "اقساط",
                color = AppMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
            val listState = rememberLazyListState()
            val rowShape = RoundedCornerShape(14.dp)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(installmentRowHeight * 5 + 24.dp)
                    .lazyColumnScrollbar(listState, AppPrimary),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(rows, key = { (it["m"] as? Number)?.toInt() ?: 0 }) { row ->
                    val m = (row["m"] as? Number)?.toInt() ?: 0
                    val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
                    val paid = row["paid"] == true
                    val paidLate = paid && row["paidLate"] == true
                    val due = row["dueDate"] as? Map<*, *>
                    val dueLabel = due?.let {
                        "${toFa(it["y"].toString())}/${toFa(it["m"].toString())}/${toFa(it["d"].toString())}"
                    } ?: ""
                    val statusLabel = when {
                        paidLate -> "با تأخیر"
                        paid -> "پرداخت شد"
                        else -> "پرداخت نشده"
                    }
                    val statusColor = when {
                        paidLate -> AppDanger
                        paid -> AppPrimary
                        else -> AppText
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .height(installmentRowHeight)
                            .background(AppSurface, rowShape)
                            .border(1.dp, AppPrimary.copy(alpha = 0.4f), rowShape)
                            .clickable {
                                if (paid) viewModel.setRowUnpaid(loan, m) else payChoiceM = m
                            }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قسط شماره ${toFa(m)}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(dueLabel, color = AppMuted, fontSize = 12.sp)
                        }
                        Text("${fmt(installment)} ریال", color = AppText, fontSize = 13.sp)
                        Text(
                            statusLabel,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp),
                        )
                        IconButton(onClick = {
                            editingRowM = m
                            editAmountText = installment.toLong().toString()
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "ویرایش مبلغ", tint = AppMuted)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text("حذف وام")
            }
        }
    }
}

private val installmentRowHeight = 56.dp

/** دایره‌ی وام (سبز = اصل، طلایی = سود) با قسط ماهانه تو مرکز - پورت حس دونات نتیجه‌ی وب. */
@Composable
private fun LoanDonut(
    principalFraction: Float,
    centerTop: String,
    centerBottom: String,
    modifier: Modifier = Modifier,
) {
    val track = AppSurface2
    val primary = AppPrimary
    val accent = AppAccent
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(150.dp).aspectRatio(1f)) {
            val stroke = size.minDimension * 0.1f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val tl = Offset(stroke / 2, stroke / 2)
            drawArc(track, -90f, 360f, false, tl, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(primary, -90f, 360f * principalFraction, false, tl, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(
                accent,
                -90f + 360f * principalFraction,
                360f * (1f - principalFraction),
                false,
                tl,
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerTop, color = AppText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(centerBottom, color = AppMuted, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailDateDropdown(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
