package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppPrimary

private val faMonthNamesManual = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * پورت فرم افزودن وام دستی (view-manual تو www/index.html؛ saveManualLoan برای اعتبارسنجی/ذخیره).
 */
@Composable
fun AddManualLoanScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MyLoansViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var installmentText by remember { mutableStateOf("") }
    var totalCountText by remember { mutableStateOf("") }
    var paidCountText by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf(1404) }
    var startMonth by remember { mutableStateOf(1) }
    var startDay by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            AppCard(label = "اسم وام") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "اسم بانک یا فروشنده") {
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "تاریخ دریافت وام") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ManualDateDropdown(
                        options = (1380..1410).map { it to toFa(it) },
                        selected = startYear,
                        onSelect = { startYear = it },
                        modifier = Modifier.weight(1f),
                    )
                    ManualDateDropdown(
                        options = faMonthNamesManual.mapIndexed { idx, name2 -> (idx + 1) to name2 },
                        selected = startMonth,
                        onSelect = { startMonth = it },
                        modifier = Modifier.weight(1f),
                    )
                    ManualDateDropdown(
                        options = (1..31).map { it to toFa(it) },
                        selected = startDay,
                        onSelect = { startDay = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            AppCard(label = "مبلغ هر قسط") {
                OutlinedTextField(
                    value = installmentText,
                    onValueChange = { installmentText = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "تعداد کل اقساط") {
                OutlinedTextField(
                    value = totalCountText,
                    onValueChange = { totalCountText = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "تعداد اقساط پرداخت‌شده (اختیاری)") {
                OutlinedTextField(
                    value = paidCountText,
                    onValueChange = { paidCountText = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        if (error != null) {
            item {
                Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val installment = installmentText.toDoubleOrNull() ?: 0.0
                        val n = totalCountText.toIntOrNull() ?: 0
                        val paidCount = paidCountText.toIntOrNull() ?: 0

                        error = when {
                            name.trim().isEmpty() -> "اسم وام رو وارد کن"
                            bank.trim().isEmpty() -> "اسم بانک یا فروشنده رو وارد کن"
                            installment <= 0 -> "مبلغ قسط رو وارد کن"
                            n <= 0 -> "تعداد کل اقساط رو وارد کن"
                            paidCount > n -> "تعداد پرداخت‌شده نمی‌تونه از کل اقساط بیشتر باشه"
                            else -> null
                        }
                        if (error == null) {
                            viewModel.saveManualLoan(
                                name = name.trim(),
                                bank = bank.trim(),
                                installment = installment,
                                n = n,
                                paidCount = paidCount,
                                startDate = PersianDate(startYear, startMonth, startDay),
                                onSaved = onSaved,
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ذخیره وام")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("انصراف")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualDateDropdown(
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
