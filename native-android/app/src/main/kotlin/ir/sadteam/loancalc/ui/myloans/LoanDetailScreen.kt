package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت openDetail/renderTable تو www/index.html، برای وام‌های دستی (method=manual): هر قسط
 * وضعیت پرداخت مستقل داره (`rows[].paid`، با تپ‌کردن رو خودِ ردیف toggle می‌شه) و تاریخ سررسید
 * واقعی (از startDate + intervalDays محاسبه می‌شه). برخلاف وب، `paidLate`/`paidDate` (تاخیر در
 * پرداخت) هنوز پورت نشده. ویرایش دستی مبلغ هر قسط هم هست (پورت confirmEditInstallment)، همراه
 * سوال «رو همه‌ی اقساط هم اعمال کنم؟» بعد از ذخیره.
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

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(loan.name, color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            AppCard(label = loan.bank, modifier = Modifier.padding(horizontal = 14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("مبلغ هر قسط", fontSize = 11.sp, color = AppMuted)
                        Text("${fmt(loan.installment)} ریال", fontSize = 13.sp, color = AppText)
                    }
                    Column {
                        Text("پرداخت‌شده", fontSize = 11.sp, color = AppMuted)
                        Text("${toFa(loan.paidCount)} از ${toFa(loan.n)}", fontSize = 13.sp, color = AppPrimary)
                    }
                }
            }
        }

        items(rows, key = { (it["m"] as? Number)?.toInt() ?: 0 }) { row ->
            val m = (row["m"] as? Number)?.toInt() ?: 0
            val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
            val paid = row["paid"] == true
            val due = row["dueDate"] as? Map<*, *>
            val dueLabel = due?.let {
                "${toFa(it["y"].toString())}/${toFa(it["m"].toString())}/${toFa(it["d"].toString())}"
            } ?: ""

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clickable { viewModel.setRowPaid(loan, m, !paid) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("قسط ${toFa(m)}", color = AppText, fontSize = 12.5.sp)
                    Text(dueLabel, color = AppMuted, fontSize = 10.5.sp)
                }
                Text("${fmt(installment)} ریال", color = AppMuted, fontSize = 11.sp)
                Text(
                    if (paid) "پرداخت‌شده ✓" else "در انتظار",
                    color = if (paid) AppPrimary else AppMuted,
                    fontSize = 11.5.sp,
                )
                IconButton(onClick = {
                    editingRowM = m
                    editAmountText = installment.toLong().toString()
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش مبلغ", tint = AppMuted)
                }
            }
        }

        item {
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
}
