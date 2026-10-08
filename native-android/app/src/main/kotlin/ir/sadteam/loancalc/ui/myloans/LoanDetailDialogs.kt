@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import kotlinx.coroutines.launch
import androidx.compose.runtime.MutableState

@Composable
internal fun LoanEditMetaDialog(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    canEditComputedAmount: Boolean,
    showEditMetaDialogState: MutableState<Boolean>,
    editMetaNameState: MutableState<String>,
    editMetaBankState: MutableState<String>,
    editMetaBorrowerState: MutableState<String>,
    editMetaYearState: MutableState<Int>,
    editMetaMonthState: MutableState<Int>,
    editMetaDayState: MutableState<Int>,
    editMetaGraceMonthsState: MutableState<Int>,
    editMetaCategoryState: MutableState<String?>,
    editMetaAmountTextState: MutableState<String>,
    editMetaNTextState: MutableState<String>,
) {
    var showEditMetaDialog by showEditMetaDialogState
    var editMetaName by editMetaNameState
    var editMetaBank by editMetaBankState
    var editMetaBorrower by editMetaBorrowerState
    var editMetaYear by editMetaYearState
    var editMetaMonth by editMetaMonthState
    var editMetaDay by editMetaDayState
    var editMetaGraceMonths by editMetaGraceMonthsState
    var editMetaCategory by editMetaCategoryState
    var editMetaAmountText by editMetaAmountTextState
    var editMetaNText by editMetaNTextState
    if (showEditMetaDialog) {
        FullScreenDialog(onDismissRequest = { showEditMetaDialog = false }) {
            Column(modifier = Modifier.fillMaxSize().background(AppBg).imePadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showEditMetaDialog = false }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                    }
                    Text("ویرایش مشخصات وام", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = editMetaName,
                        onValueChange = { editMetaName = it },
                        label = { Text("اسم وام") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    // بانک فقط از فهرستِ جستجودار با لوگو (خواسته‌ی کاربر، ۷ مهر) - جای فیلدِ متنی + نوارِ لوگوها.
                    ir.sadteam.loancalc.ui.components.BankPickerField(value = editMetaBank, onValueChange = { editMetaBank = it }, includeCreditServices = true)
                    OutlinedTextField(
                        value = editMetaBorrower,
                        onValueChange = { editMetaBorrower = it },
                        label = { Text("وام‌گیرنده (اختیاری)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    // ═══ نوعِ وام ═══
                    // تا امروز نشانِ ردیفِ فهرست فقط از **نامِ وام** حدس زده می‌شد؛ نامی مثل
                    // «وام ۹۵ میلیونی» هیچ کلیدواژه‌ای ندارد و همیشه نشانِ پیش‌فرض می‌گرفت.
                    // این ردیف همان حدس را به انتخاب تبدیل می‌کند.
                    Text("نوعِ وام", color = AppMuted, fontSize = 11.sp)
                    // نوعِ وام با آیکون (فریمِ `36b`)، سه‌تایی در هر ردیف - نه نوارِ اسکرولیِ بی‌آیکون.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LoanCategory.entries.chunked(3).forEach { rowCats ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowCats.forEach { category ->
                                    val selected = editMetaCategory == category.id
                                    LoanTypeOption(
                                        category = category,
                                        selected = selected,
                                        // دوباره‌زدنِ نوعِ انتخاب‌شده آن را برمی‌دارد و به حدسِ خودکار برمی‌گرداند.
                                        onClick = { editMetaCategory = if (selected) null else category.id },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                repeat(3 - rowCats.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                    // برچسب + توضیحِ دینامیک قبلاً دو تیکه‌ی جدا بودن - همون رفعِ مورد ۳ که تو
                    // BankLoanScreen انجام شد، اینجا هم یکی‌شون کردیم به یه جمله‌ی تمیز.
                    Text(
                        if (editMetaGraceMonths > 0) {
                            "تاریخ دریافت وام (قسطِ اول ${toFa(editMetaGraceMonths)} ماه بعد، به‌خاطرِ دوره‌ی تنفس)"
                        } else {
                            "تاریخ دریافت وام (سررسیدِ قسطِ اول)"
                        },
                        fontSize = 13.sp,
                        color = AppMuted,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InlineJalaliDateRow(
                            year = editMetaYear,
                            month = editMetaMonth,
                            day = editMetaDay,
                            onDateChange = { y, m, d -> editMetaYear = y; editMetaMonth = m; editMetaDay = d },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // مبلغ/تعدادِ اقساط فقط وقتی هیچ قسطی پرداخت نشده قابلِ‌ویرایشه - رجوع کن به
                    // کامنتِ بالای canEditComputedAmount. اگه یه قسط پرداخت شده باشه، تغییرشون یعنی
                    // کلِ فرمول دوباره اجرا بشه و تاریخچه‌ی پرداخت گم بشه، برای همین قفله.
                    if (canEditComputedAmount) {
                        OutlinedTextField(
                            value = editMetaAmountText,
                            onValueChange = { editMetaAmountText = cleanNum(it) },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            label = { Text("مبلغ وام") },
                            suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                        editMetaAmountText.toLongOrNull()?.takeIf { it > 0 }?.let {
                            Text("${ir.sadteam.loancalc.core.numberToWordsFa(it.toDouble())} تومان", color = AppMuted, fontSize = 11.sp)
                        }
                        OutlinedTextField(
                            value = editMetaNText,
                            onValueChange = { editMetaNText = cleanNum(it).take(3) },
                            label = { Text("تعداد اقساط") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                        Text(
                            "چون هنوز هیچ قسطی پرداخت نشده، عوض‌کردنِ این دوتا کلِ جدولِ اقساط رو از نو می‌سازه.",
                            color = AppMuted,
                            fontSize = 11.sp,
                        )
                    } else {
                        Text(
                            "چون قبلاً حداقل یه قسط پرداخت شده، مبلغ/تعدادِ اقساط دیگه قابلِ‌ویرایش نیست.",
                            color = AppMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
                GradientButton(
                    onClick = {
                    // نامِ وام وقتی بانک انتخاب نشده از خودِ بانک («مشخص‌نشده») ساخته می‌شود؛ اگر کاربر
                    // اسم را دست نزده و فقط بانک را عوض کرده، اسمِ فهرست هم با بانکِ تازه هماهنگ شود.
                    val newBank = editMetaBank.trim()
                    val nameIsAuto = editMetaName.trim() == loan.name &&
                        (loan.name == loan.bank || loan.name == "مشخص‌نشده")
                    val effectiveName = if (nameIsAuto && newBank.isNotEmpty()) newBank
                    else editMetaName.trim().ifEmpty { loan.name }
                    if (canEditComputedAmount) {
                        viewModel.updateComputedLoanAmount(
                            loan = loan,
                            name = effectiveName,
                            bank = editMetaBank.trim(),
                            borrower = editMetaBorrower.trim().ifEmpty { "—" },
                            principalAmount = editMetaAmountText.toLongOrNull()?.let { tomanToRial(it).toDouble() } ?: loan.amount,
                            n = editMetaNText.toIntOrNull()?.takeIf { it > 0 } ?: loan.n,
                            startDate = PersianDate(editMetaYear, editMetaMonth, editMetaDay),
                            onSaved = {},
                            category = editMetaCategory,
                        )
                    } else {
                        viewModel.updateLoanMeta(
                            loan = loan,
                            name = effectiveName,
                            bank = editMetaBank.trim(),
                            borrower = editMetaBorrower.trim().ifEmpty { "—" },
                            startDate = PersianDate(editMetaYear, editMetaMonth, editMetaDay),
                            onSaved = {},
                            category = editMetaCategory,
                        )
                    }
                    showEditMetaDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text("ذخیره‌ی تغییرات", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
internal fun LoanInstallmentDetailHost(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    photoRowMState: MutableState<Int?>,
    rowsState: MutableState<List<Map<String, Any?>>>,
) {
    var photoRowM by photoRowMState
    var rows by rowsState
    if (photoRowM != null) {
        val m = photoRowM!!
        val row = rows.firstOrNull { (it["m"] as? Number)?.toInt() == m }
        val rawPhoto = row?.get("photoPath") as? String
        val photoPaths = rawPhoto?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
        val paidDate = row?.get("paidDate") as? Map<*, *>
        val paidDateLabel = paidDate?.let {
            val d = (it["d"] as? Number)?.toInt()
            val mo = (it["m"] as? Number)?.toInt()
            if (d != null && mo != null) "${toFa(d)} ${persianMonthName(mo)}" else null
        }
        FullScreenDialog(onDismissRequest = { photoRowM = null }) {
            InstallmentDetailScreen(
                loan = loan,
                m = m,
                amount = (row?.get("installment") as? Number)?.toDouble() ?: 0.0,
                paid = row?.get("paid") == true,
                paidDateLabel = paidDateLabel,
                photoPaths = photoPaths,
                note = row?.get("note") as? String,
                trackingNumber = row?.get("trackingNumber") as? String,
                onBack = { photoRowM = null },
                onPickPhoto = { uri -> viewModel.setRowPhoto(loan, m, uri) },
                onRemovePhoto = { path -> viewModel.removeRowPhoto(loan, m, path) },
                onSaveDetails = { note, tracking ->
                    viewModel.setRowDetails(loan, m, note, tracking)
                    photoRowM = null
                },
            )
        }
    }
}

@Composable
internal fun LoanEditRowAmountDialog(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    editingRowMState: MutableState<Int?>,
    editAmountTextState: MutableState<String>,
    applyAllPromptAmountState: MutableState<Double?>,
) {
    var editingRowM by editingRowMState
    var editAmountText by editAmountTextState
    var applyAllPromptAmount by applyAllPromptAmountState
    if (editingRowM != null) {
        JibakAlertDialog(
            onDismissRequest = { editingRowM = null },
            title = { Text("ویرایش مبلغ قسط ${toFa(editingRowM ?: 0)}") },
            text = {
                OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                    value = editAmountText,
                    onValueChange = { editAmountText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            },
            confirmButton = {
                TextButton(onClick = {
                    // 🚨 فیلد **تومان** می‌گیرد (برچسبش هم همین را می‌گفت) ولی مقدارش ریالِ
                    // خام می‌نشست - یعنی عددی که کاربر می‌دید ده برابر بود و ذخیره‌اش هم
                    // ده‌برابرِ چیزی که تایپ کرده. قاعده‌ی «دیتابیس ریال، نمایش تومان».
                    val newAmount = editAmountText.toLongOrNull()?.let { tomanToRial(it).toDouble() }
                    val m = editingRowM
                    if (newAmount != null && newAmount > 0 && m != null) {
                        viewModel.setRowInstallment(loan, m, newAmount) {
                            applyAllPromptAmount = newAmount
                        }
                    }
                    editingRowM = null
                }, enabled = (editAmountText.toLongOrNull() ?: 0L) > 0) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { editingRowM = null }) { Text("انصراف") }
            },
        )
    }
}

@Composable
internal fun LoanApplyAllAmountDialog(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    applyAllPromptAmountState: MutableState<Double?>,
) {
    var applyAllPromptAmount by applyAllPromptAmountState
    if (applyAllPromptAmount != null) {
        JibakAlertDialog(
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
}

@Composable
internal fun LoanPayChoiceDialog(
    applyPayment: (PendingLoanPayment) -> Unit,
    payChoiceMState: MutableState<Int?>,
    lateDateMState: MutableState<Int?>,
    lateYearState: MutableState<Int>,
    lateMonthState: MutableState<Int>,
    lateDayState: MutableState<Int>,
) {
    var payChoiceM by payChoiceMState
    var lateDateM by lateDateMState
    var lateYear by lateYearState
    var lateMonth by lateMonthState
    var lateDay by lateDayState
    if (payChoiceM != null) {
        val m = payChoiceM!!
        JibakAlertDialog(
            onDismissRequest = { payChoiceM = null },
            title = { Text("ثبت پرداخت قسط ${toFa(m)}") },
            text = { Text("این قسط سر موعد پرداخت شده یا با تاخیر؟") },
            confirmButton = {
                TextButton(onClick = {
                    applyPayment(PendingLoanPayment(listOf(m), null))
                    payChoiceM = null
                }) { Text("پرداخت به‌موقع") }
            },
            dismissButton = {
                TextButton(onClick = {
                    // پیش‌فرض = امروز (نه سررسید) تا تراکنش در ماهِ واقعیِ پرداخت بیفتد.
                    val t = JalaliCalendar.today()
                    lateYear = t.y; lateMonth = t.m; lateDay = t.d
                    lateDateM = m
                    payChoiceM = null
                }) { Text("پرداخت با تاخیر") }
            },
        )
    }
}

@Composable
internal fun LoanBulkPayChoiceDialog(
    applyPayment: (PendingLoanPayment) -> Unit,
    bulkPayChoiceOpenState: MutableState<Boolean>,
    bulkLateMsState: MutableState<List<Int>?>,
    bulkPayModeState: MutableState<Boolean>,
    lateYearState: MutableState<Int>,
    lateMonthState: MutableState<Int>,
    lateDayState: MutableState<Int>,
    rowsState: MutableState<List<Map<String, Any?>>>,
    selectedBulkMsState: MutableState<Set<Int>>,
) {
    var bulkPayChoiceOpen by bulkPayChoiceOpenState
    var bulkLateMs by bulkLateMsState
    var bulkPayMode by bulkPayModeState
    var lateYear by lateYearState
    var lateMonth by lateMonthState
    var lateDay by lateDayState
    var rows by rowsState
    var selectedBulkMs by selectedBulkMsState
    if (bulkPayChoiceOpen) {
        val count = selectedBulkMs.size
        JibakAlertDialog(
            onDismissRequest = { bulkPayChoiceOpen = false },
            title = { Text("ثبت پرداختِ ${toFa(count)} قسط") },
            text = {
                // جمعِ مبلغ در متنِ تایید تکرار می‌شود (فریمِ `64b`) - «پرداختِ ۲ قسط» بی عدد
                // یعنی تاییدِ کور، و واگردِ پرداختِ گروهی ردیف‌به‌ردیف است نه یک تپ.
                val sum = rows.filter { (it["m"] as? Number)?.toInt() in selectedBulkMs }
                    .sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
                Text("جمعاً ${amountToman(sum)} تومان. این اقساط سرِ موعد پرداخت شدن یا با تاخیر؟")
            },
            confirmButton = {
                TextButton(onClick = {
                    applyPayment(PendingLoanPayment(selectedBulkMs.toList(), null))
                    selectedBulkMs = emptySet()
                    bulkPayChoiceOpen = false
                    bulkPayMode = false
                }) { Text("پرداخت به‌موقع") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val t = JalaliCalendar.today()
                    lateYear = t.y; lateMonth = t.m; lateDay = t.d
                    bulkLateMs = selectedBulkMs.sorted()
                    bulkPayChoiceOpen = false
                }) { Text("پرداخت با تاخیر") }
            },
        )
    }
}

@Composable
internal fun LoanLateDateDialog(
    applyPayment: (PendingLoanPayment) -> Unit,
    lateDateMState: MutableState<Int?>,
    lateYearState: MutableState<Int>,
    lateMonthState: MutableState<Int>,
    lateDayState: MutableState<Int>,
    rowsState: MutableState<List<Map<String, Any?>>>,
) {
    var lateDateM by lateDateMState
    var lateYear by lateYearState
    var lateMonth by lateMonthState
    var lateDay by lateDayState
    var rows by rowsState
    if (lateDateM != null) {
        val m = lateDateM!!
        JibakAlertDialog(
            onDismissRequest = { lateDateM = null },
            title = { Text("تاریخ واقعی پرداخت قسط ${toFa(m)}") },
            text = {
                // فریمِ `30c`: چرخِ تاریخ (همان کامپوننتِ فرمِ وام/چک) جای سه منوی کشویی، و زیرش
                // «چند روز دیرتر از سررسید» که زنده عوض می‌شود - با رنگِ هشدار، نه سبز.
                val dueMap = rows.firstOrNull { (it["m"] as? Number)?.toInt() == m }?.get("dueDate") as? Map<*, *>
                val dueDate = dueMap?.let {
                    val y = (it["y"] as? Number)?.toInt(); val mo = (it["m"] as? Number)?.toInt(); val d = (it["d"] as? Number)?.toInt()
                    if (y != null && mo != null && d != null) PersianDate(y, mo, d) else null
                }
                Column {
                    ir.sadteam.loancalc.ui.components.InlineJalaliDateRow(
                        year = lateYear,
                        month = lateMonth,
                        day = lateDay,
                        onDateChange = { y, mo, d -> lateYear = y; lateMonth = mo; lateDay = d },
                    )
                    if (dueDate != null) {
                        val late = JalaliCalendar.daysBetween(dueDate, PersianDate(lateYear, lateMonth, lateDay))
                        val (label, ink) = when {
                            late > 0 -> "${toFa(late)} روز دیرتر از سررسید" to AppDangerInk
                            late == 0 -> "همان روزِ سررسید" to AppMuted
                            else -> "${toFa(-late)} روز زودتر از سررسید" to AppMuted
                        }
                        Text(
                            label,
                            color = ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ink.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    applyPayment(PendingLoanPayment(listOf(m), PersianDate(lateYear, lateMonth, lateDay)))
                    lateDateM = null
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { lateDateM = null }) { Text("انصراف") }
            },
        )
    }
}

@Composable
internal fun LoanBulkLateDialog(
    applyPayment: (PendingLoanPayment) -> Unit,
    bulkLateMsState: MutableState<List<Int>?>,
    bulkPayModeState: MutableState<Boolean>,
    lateYearState: MutableState<Int>,
    lateMonthState: MutableState<Int>,
    lateDayState: MutableState<Int>,
    selectedBulkMsState: MutableState<Set<Int>>,
) {
    var bulkLateMs by bulkLateMsState
    var bulkPayMode by bulkPayModeState
    var lateYear by lateYearState
    var lateMonth by lateMonthState
    var lateDay by lateDayState
    var selectedBulkMs by selectedBulkMsState
    bulkLateMs?.let { ms ->
        JibakAlertDialog(
            onDismissRequest = { bulkLateMs = null },
            title = { Text("تاریخ واقعی پرداختِ ${toFa(ms.size)} قسط") },
            text = {
                Column {
                    Text(
                        "همه‌ی اقساطِ انتخاب‌شده با همین تاریخ ثبت می‌شوند.",
                        color = AppMuted,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    ir.sadteam.loancalc.ui.components.InlineJalaliDateRow(
                        year = lateYear,
                        month = lateMonth,
                        day = lateDay,
                        onDateChange = { y, mo, d -> lateYear = y; lateMonth = mo; lateDay = d },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    applyPayment(PendingLoanPayment(ms, PersianDate(lateYear, lateMonth, lateDay)))
                    selectedBulkMs = emptySet()
                    bulkPayMode = false
                    bulkLateMs = null
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { bulkLateMs = null }) { Text("انصراف") }
            },
        )
    }
}

@Composable
internal fun LoanDetailBottomBar(
    loan: LoanEntity,
    privacyMode: Boolean,
    settled: Boolean,
    nextRow: Map<String, Any?>?,
    context: android.content.Context,
    runCalendarExport: () -> Unit,
    calendarPermissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    bulkPayChoiceOpenState: MutableState<Boolean>,
    bulkPayModeState: MutableState<Boolean>,
    calendarMessageState: MutableState<String?>,
    isExportingCalendarState: MutableState<Boolean>,
    payChoiceMState: MutableState<Int?>,
    rowsState: MutableState<List<Map<String, Any?>>>,
    selectedBulkMsState: MutableState<Set<Int>>,
    showDeleteConfirmState: MutableState<Boolean>,
) {
    var bulkPayChoiceOpen by bulkPayChoiceOpenState
    var bulkPayMode by bulkPayModeState
    var calendarMessage by calendarMessageState
    var isExportingCalendar by isExportingCalendarState
    var payChoiceM by payChoiceMState
    var rows by rowsState
    var selectedBulkMs by selectedBulkMsState
    var showDeleteConfirm by showDeleteConfirmState
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurface)
            .border(1.dp, AppLineRow)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (bulkPayMode && selectedBulkMs.isNotEmpty()) {
            val bulkSum = rows.filter { (it["m"] as? Number)?.toInt() in selectedBulkMs }
                .sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("جمعِ انتخاب‌شده", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, amountToman(bulkSum)),
                            color = AppText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                GradientButton(onClick = { bulkPayChoiceOpen = true }) {
                    Text("پرداختِ ${toFa(selectedBulkMs.size)} قسط", fontSize = 13.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ترتیبِ راست‌به‌چپ: پرداخت (اصلی) ← تقویم ← حذف (کم‌رنگ‌ترین).
                if (!settled && nextRow != null) {
                    LoanActionButton(
                        label = "پرداخت قسط",
                        icon = Icons.Filled.CreditCard,
                        ink = Color.White,
                        bg = AppPrimary,
                        modifier = Modifier.weight(1.1f),
                        onClick = { payChoiceM = (nextRow["m"] as? Number)?.toInt() },
                    )
                }
                LoanActionButton(
                    label = when {
                        isExportingCalendar -> "در حال افزودن…"
                        loan.calendarExported -> "در تقویم هست"
                        else -> "افزودن به تقویم"
                    },
                    icon = Icons.Filled.CalendarMonth,
                    ink = AppInfo,
                    bg = AppInfoPill,
                    enabled = !isExportingCalendar,
                    modifier = Modifier.weight(1.1f),
                    onClick = {
                        if (loan.calendarExported) {
                            // دیگه دوباره درج نمی‌کنیم (جلوگیری از رویدادهای تکراری تو تقویم گوشی با
                            // هر بار کلیک) - فقط یادآوری می‌کنیم قبلاً اضافه شده.
                            calendarMessage = "سررسیدهای این وام قبلاً به تقویم گوشی اضافه شده‌اند"
                            return@LoanActionButton
                        }
                        isExportingCalendar = true
                        val perms = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        val allGranted = perms.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (allGranted) runCalendarExport() else calendarPermissionLauncher.launch(perms)
                    },
                )
                LoanActionButton(
                    label = "حذف وام",
                    icon = Icons.Filled.Delete,
                    ink = AppDangerInk,
                    bg = AppDangerPill,
                    modifier = Modifier.weight(0.9f),
                    onClick = { showDeleteConfirm = true },
                )
            }
        }
    }
}

@Composable
internal fun LoanUnmarkConfirmDialog(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    confirmUnmarkMState: MutableState<Int?>,
) {
    var confirmUnmarkM by confirmUnmarkMState
    confirmUnmarkM?.let { unmarkM ->
        ConfirmDialog(
            tone = ConfirmTone.DESTRUCTIVE,
            title = "پرداختِ قسط ${toFa(unmarkM)} برداشته شود؟",
            consequence = "این ردیف به حالتِ پرداخت‌نشده برمی‌گردد.",
            actionLabel = "بردار",
            onConfirm = { confirmUnmarkM = null; viewModel.setRowUnpaid(loan, unmarkM) },
            onDismiss = { confirmUnmarkM = null },
        )
    }
}

@Composable
internal fun LoanDeleteConfirmDialog(
    loan: LoanEntity,
    showDeleteConfirmState: MutableState<Boolean>,
    showDeletePaymentsAskState: MutableState<Boolean>,
) {
    var showDeleteConfirm by showDeleteConfirmState
    var showDeletePaymentsAsk by showDeletePaymentsAskState
    if (showDeleteConfirm) {
        // قالبِ واحدِ بخشِ ۴۶: حذفِ وام بازگشت‌پذیر نیست، پس دیالوگ می‌گیره
        // (نه واگردِ نواری) و لحنش DESTRUCTIVE ئه.
        ConfirmDialog(
            tone = ConfirmTone.DESTRUCTIVE,
            title = "حذف وام",
            consequence = "وامِ «${loan.name}» و همه‌ی قسط‌ها و عکس‌هایش حذف بشه؟ این کار قابلِ‌برگشت نیست.",
            actionLabel = "حذف وام",
            onConfirm = { showDeleteConfirm = false; showDeletePaymentsAsk = true },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
internal fun LoanDeletePaymentsDialog(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    onDelete: () -> Unit,
    showDeletePaymentsAskState: MutableState<Boolean>,
) {
    var showDeletePaymentsAsk by showDeletePaymentsAskState
    if (showDeletePaymentsAsk) {
        // قسط‌های پرداخت‌شده خرج ثبت کرده‌اند؛ کاربر انتخاب می‌کند بمانند یا بروند.
        // 🚨 بستنِ پنجره (لمسِ بیرون/بازگشت) فقط **انصراف** است و وامی پاک نمی‌شود؛
        // حذف فقط با یکی از دو دکمه انجام می‌شود.
        JibakAlertDialog(
            onDismissRequest = { showDeletePaymentsAsk = false },
            title = { Text("تراکنش‌های پرداخت چه شود؟") },
            text = { Text("خرج‌هایی که برای قسط‌های «${loan.name}» ثبت شده هم پاک شود و پولش به حساب برگردد؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeletePaymentsAsk = false
                    viewModel.deleteLoanPayments(loan.id)
                    onDelete()
                }) { Text("پاک شود") }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePaymentsAsk = false; onDelete() }) { Text("نه، فقط وام حذف شود") }
            },
        )
    }
}
