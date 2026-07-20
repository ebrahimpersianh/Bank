package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted

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
    var showCalendarPicker by remember { mutableStateOf(false) }

    if (showCalendarPicker) {
        CalendarPickerScreen(
            initialDate = PersianDate(startYear, startMonth, startDay),
            onDateSelected = { d -> startYear = d.y; startMonth = d.m; startDay = d.d; showCalendarPicker = false },
            onBack = { showCalendarPicker = false },
        )
        return
    }

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
                    colors = appFieldColors(),
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
                    colors = appFieldColors(),
                )
            }
        }
        item {
            AppCard(label = "تاریخ دریافت وام") {
                // هم‌الگو با تاریخِ «وام بانکی» و فرمِ چک (InlineJalaliDateRow): اعدادِ روز/ماه/سال
                // درجا قابلِ تغییرن + آیکونِ تقویمِ گریدی - قبلاً فقط یه متنِ تپ‌شونده به چرخونه بود
                // که کاربر خواست مثل صفحه‌ی اصلی بشه.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InlineJalaliDateRow(
                        year = startYear,
                        month = startMonth,
                        day = startDay,
                        onDateChange = { y, m, d -> startYear = y; startMonth = m; startDay = d },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showCalendarPicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "انتخاب از تقویم")
                    }
                }
            }
        }
        item {
            AppCard(label = "مبلغ هر قسط") {
                // state فقط رقم نگه می‌داره؛ کاما با ThousandsSeparatorTransformation فقط «نمایشیه» -
                // فرمت‌کردن تو onValueChange (الگوی قبلی) مکان‌نما رو می‌پروند و رقمِ تایپ‌شده وسطِ
                // عدد می‌افتاد (باگِ گزارش‌شده: ۱۲۷۴۹۰۰۰ → ۱۲,۷۴۰,۰۰۹).
                OutlinedTextField(
                    value = installmentText,
                    onValueChange = { installmentText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val instRial = cleanNum(installmentText).toLongOrNull() ?: 0L
                if (instRial > 0) {
                    Text(
                        "${numberToWordsFa((instRial / 10).toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
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
                    colors = appFieldColors(),
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
                    colors = appFieldColors(),
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
                GradientButton(
                    onClick = {
                        val installment = cleanNum(installmentText).toDoubleOrNull() ?: 0.0
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
