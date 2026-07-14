package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * پورت فرم افزودن وام دستی (view-manual تو www/index.html؛ saveManualLoan برای اعتبارسنجی/ذخیره).
 * برخلاف وب، هنوز محدودیت «۱ وام رایگان»/اشتراک اینجا پیاده نشده - چون ورود OTP و اشتراک کافه‌بازار
 * تو این پروژه هنوز پورت نشدن (فاز بعد، طبق native-android/README.md).
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
                            viewModel.saveManualLoan(name.trim(), bank.trim(), installment, n, paidCount, onSaved)
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
