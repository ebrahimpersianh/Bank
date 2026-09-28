package ir.sadteam.loancalc.ui.extras

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.BillEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/** نوع‌های قبض - کلید ثابت، برچسبِ فارسی. */
val BILL_KINDS = listOf(
    "water" to "آب", "power" to "برق", "gas" to "گاز", "mobile" to "موبایل",
    "phone" to "تلفنِ ثابت", "internet" to "اینترنت", "other" to "سایر",
)

fun billKindLabel(kind: String) = BILL_KINDS.firstOrNull { it.first == kind }?.second ?: "قبض"

/** آیا قبض در دوره‌ی جاری (این ماه) هنوز پرداخت نشده و موعدش رسیده یا تا ۳ روزِ دیگر می‌رسد؟ */
fun BillEntity.isDueSoon(y: Int, m: Int, d: Int): Boolean {
    if (periodMonths > 1 && (m - 1) % periodMonths != 0) return false
    if (lastPaidKey == "$y-$m") return false
    return d >= dueDay - 3
}

/** فهرست و افزودنِ قبض‌ها (برگرفته از پولکس، ۶ مهر). */
@Composable
fun BillsScreen(onBack: () -> Unit, viewModel: ExtrasViewModel = hiltViewModel()) {
    val bills by viewModel.bills.collectAsState()
    val today = remember { JalaliCalendar.today() }
    var editing by remember { mutableStateOf<BillEntity?>(null) }
    var adding by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                Text("قبض‌ها", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
        item {
            GradientButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) { Text("+ افزودنِ قبض") }
        }
        if (bills.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Receipt,
                    title = "هنوز قبضی نداری",
                    description = "آب، برق، گاز، موبایل… را اضافه کن تا نزدیکِ موعد یادت بیندازیم.",
                )
            }
        }
        items(bills, key = { it.id }) { bill ->
            val due = bill.isDueSoon(today.y, today.m, today.d)
            AppCard(modifier = Modifier.clickable { editing = bill }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${billKindLabel(bill.kind)} · ${bill.name}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            buildString {
                                append("موعد: ${toFa(bill.dueDay)}ِ ")
                                append(if (bill.periodMonths > 1) "هر ${toFa(bill.periodMonths)} ماه" else "هر ماه")
                                if (bill.lastPaidKey == "${today.y}-${today.m}") append(" · این دوره پرداخت شد ✓")
                            },
                            color = if (due) AppDanger else AppMuted,
                            fontSize = 11.sp,
                        )
                    }
                    if (due) {
                        TextButton(onClick = { viewModel.markBillPaid(bill, today.y, today.m, bill.lastAmount) }) {
                            Text("پرداخت شد", color = AppPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (adding || editing != null) {
        val base = editing
        var name by remember(base) { mutableStateOf(base?.name ?: "") }
        var kind by remember(base) { mutableStateOf(base?.kind ?: "power") }
        var billId by remember(base) { mutableStateOf(base?.billId ?: "") }
        var dayText by remember(base) { mutableStateOf(base?.dueDay?.toString() ?: "") }
        var period by remember(base) { mutableStateOf(base?.periodMonths ?: 1) }
        val close = { adding = false; editing = null }
        JibakAlertDialog(
            onDismissRequest = close,
            title = { Text(if (base == null) "قبضِ جدید" else "ویرایشِ قبض") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        BILL_KINDS.take(4).forEach { (k, l) -> AppChip(l, kind == k, onClick = { kind = k }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        BILL_KINDS.drop(4).forEach { (k, l) -> AppChip(l, kind == k, onClick = { kind = k }) }
                    }
                    OutlinedTextField(value = name, onValueChange = { name = it.take(30) }, singleLine = true, placeholder = { Text("نام (مثلاً خانه)") })
                    Ltr {
                        OutlinedTextField(
                            value = billId,
                            onValueChange = { billId = cleanNum(it).take(18) },
                            singleLine = true,
                            placeholder = { Text("شناسه‌ی قبض (اختیاری)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = cleanNum(it).take(2) },
                        singleLine = true,
                        placeholder = { Text("روزِ موعد در ماه (۱ تا ۳۰)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppChip("ماهانه", period == 1, onClick = { period = 1 })
                        AppChip("دوماهه", period == 2, onClick = { period = 2 })
                    }
                }
            },
            confirmButton = {
                GradientButton(onClick = {
                    val day = dayText.toIntOrNull()?.coerceIn(1, 30)
                    if (day != null) {
                        viewModel.saveBill(
                            (base ?: BillEntity(System.currentTimeMillis(), "", kind, null, day)).copy(
                                name = name.trim().ifBlank { billKindLabel(kind) },
                                kind = kind,
                                billId = billId.ifBlank { null },
                                dueDay = day,
                                periodMonths = period,
                            ),
                        )
                        close()
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = {
                if (base != null) {
                    IconButton(onClick = { viewModel.deleteBill(base); close() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                    }
                } else {
                    TextButton(onClick = close) { Text("انصراف") }
                }
            },
        )
    }
}
