package ir.sadteam.loancalc.ui.cheque

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.SuccessCheckmarkOverlay
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * فرم افزودن/ویرایش چک - هم‌الگو با BankLoanScreen (تاریخِ اینلاینِ چرخونه‌ای + آیکونِ تقویمِ گریدیِ
 * کامل، اعتبارسنجی ساده، GradientButton برای ذخیره). انتخاب دسته‌چک اختیاریه و اگه شماره‌ی چک هنوز
 * خالی باشه، شماره‌ی سریال بعدی همون دسته‌چک به‌عنوان پیشنهاد اولیه ست می‌شه.
 */
@Composable
fun AddEditChequeScreen(
    existing: ChequeEntity?,
    chequeBooks: List<ChequeBookEntity>,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ChequeViewModel,
) {
    var type by remember { mutableStateOf(existing?.let { ChequeType.valueOf(it.type) } ?: ChequeType.RECEIVED) }
    var amountText by remember { mutableStateOf(existing?.amount?.toLong()?.toString() ?: "") }
    var chequeNumber by remember { mutableStateOf(existing?.chequeNumber ?: "") }
    var sayadId by remember { mutableStateOf(existing?.sayadId ?: "") }
    var bankName by remember { mutableStateOf(existing?.bankName ?: "") }
    var branchName by remember { mutableStateOf(existing?.branchName ?: "") }
    var ownerName by remember { mutableStateOf(existing?.ownerName ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var dueYear by remember { mutableStateOf(existing?.dueYear ?: 1404) }
    var dueMonth by remember { mutableStateOf(existing?.dueMonth ?: 1) }
    var dueDay by remember { mutableStateOf(existing?.dueDay ?: 1) }
    var chequeBookId by remember { mutableStateOf(existing?.chequeBookId) }
    var photoPath by remember { mutableStateOf(existing?.photoPath) }
    var showMoreInfo by remember { mutableStateOf(false) }
    // بعدِ ذخیره‌ی موفق، یه تیکِ سبزِ متحرک قبل از بستنِ صفحه - رجوع کن به SuccessCheckmark.kt.
    var savedOk by remember { mutableStateOf(false) }
    var nationalId by remember { mutableStateOf(existing?.nationalId ?: "") }
    var previousBalanceText by remember { mutableStateOf(existing?.previousBalance?.let { fmt(it) } ?: "") }
    var depositAmountText by remember { mutableStateOf(existing?.depositAmount?.let { fmt(it) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    if (showCalendarPicker) {
        CalendarPickerScreen(
            initialDate = PersianDate(dueYear, dueMonth, dueDay),
            onDateSelected = { date ->
                dueYear = date.y
                dueMonth = date.m
                dueDay = date.d
                showCalendarPicker = false
            },
            onBack = { showCalendarPicker = false },
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(if (existing == null) "افزودن چک" else "ویرایش چک", fontSize = 16.sp)
        }
        item {
            // دوتا کارتِ بزرگ به‌جای دو چیپِ کوچیک - هم‌الگو با toggleِ «نوع چک» تو عکسِ مرجعِ کاربر
            // (هرکدوم یه زیرنویسِ توضیحی هم داره، نه فقط یه اسم تنها).
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChequeTypeToggleCard(
                    title = "پرداختی",
                    subtitle = "چکی که شما صادر کرده‌اید",
                    selected = type == ChequeType.PAID,
                    onClick = { type = ChequeType.PAID },
                    modifier = Modifier.weight(1f),
                )
                ChequeTypeToggleCard(
                    title = "دریافتی",
                    subtitle = "چکی که به شما داده شده",
                    selected = type == ChequeType.RECEIVED,
                    onClick = { type = ChequeType.RECEIVED },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            AppCard(label = "مبلغ") {
                // هم‌الگو با بقیه‌ی فیلدهای مبلغِ اپ (وام/درآمد): جداکننده‌ی هزارگان تو خودِ فیلد +
                // معادلِ حروفی تومانی زیرش - قبلاً این یکی فرمتِ ساده‌ی بدونِ کاما داشت. واحدِ «ریال»
                // به‌جای این‌که تو لیبلِ بالای باکس باشه، حالا هم‌الگو با بقیه‌ی فیلدهای مبلغِ اپ،
                // suffix داخلِ خودِ فیلده.
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val amountVal = amountText.toLongOrNull() ?: 0L
                if (amountVal > 0) {
                    AutoShrinkText(
                        text = "${numberToWordsFa((amountVal / 10).toDouble())} تومان",
                        color = AppMuted,
                        maxFontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        item {
            // پیوست عکس مستقیم تو خودِ فرمِ افزودن (نه فقط بعد از ذخیره، تو جزئیات) - برای چکِ جدید
            // (existing == null) هنوز id نداریم، پس عکس بلافاصله با pickPhotoForNewCheque به فضای
            // داخلی اپ کپی می‌شه و مسیرش موقتاً تو state خودِ فرم می‌مونه تا موقعِ «ذخیره چک» مستقیم
            // با خودِ چک ذخیره بشه؛ برای ویرایش (existing != null) رفتار عیناً مثل قبل (ChequeDetailScreen).
            PhotoAttachmentCard(
                photoPath = photoPath,
                onPick = { uri ->
                    viewModel.pickPhotoForNewCheque(uri) { newPath ->
                        if (newPath != null) {
                            viewModel.deleteOrphanPhoto(photoPath.takeIf { existing == null })
                            photoPath = newPath
                        }
                    }
                },
                onRemove = {
                    if (existing == null) viewModel.deleteOrphanPhoto(photoPath)
                    photoPath = null
                },
            )
        }
        item {
            // بخشِ اختیاریِ «اطلاعات بیشتر» (شناسه/کد ملی + مانده‌ی قبلی/واریزی حساب) - جمع/مانده از
            // رو همین دو مقدار محاسبه می‌شه، فیلدِ جدا برای اون‌ها نیست.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMoreInfo = !showMoreInfo }
                    .animateContentSize(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("اطلاعات بیشتر", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        if (showMoreInfo) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = AppMuted,
                    )
                }
                if (showMoreInfo) {
                    val previousBalanceVal = cleanNumDecimal(previousBalanceText).toDoubleOrNull() ?: 0.0
                    val depositAmountVal = cleanNumDecimal(depositAmountText).toDoubleOrNull() ?: 0.0
                    val sumVal = previousBalanceVal + depositAmountVal
                    val remainingVal = sumVal - (amountText.toLongOrNull()?.toDouble() ?: 0.0)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppCard(label = "شناسه ملی / کد ملی") {
                            // Ltr: بدونش، تایپِ عدد زیرِ RTLِ کلِ اپ از سمتِ راست جا می‌گرفت - رجوع
                            // کن به کامنتِ Ltr.kt.
                            Ltr {
                                OutlinedTextField(
                                    value = nationalId,
                                    onValueChange = { nationalId = cleanNum(it).take(11) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }
                        AppCard(label = "مانده قبلی (تومان)") {
                            OutlinedTextField(
                                value = previousBalanceText,
                                onValueChange = { previousBalanceText = cleanNumDecimal(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Text(
                                "موجودی حساب قبل از واریز مبلغ جدید",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        AppCard(label = "واریزی (تومان)") {
                            OutlinedTextField(
                                value = depositAmountText,
                                onValueChange = { depositAmountText = cleanNumDecimal(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Text(
                                "مبلغی که امروز به حساب واریز کرده‌اید",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppCard(label = "جمع (تومان)", modifier = Modifier.weight(1f)) {
                                Text(fmt(sumVal), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            AppCard(label = "مانده (تومان)", modifier = Modifier.weight(1f)) {
                                Text(fmt(remainingVal), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        if (chequeBooks.isNotEmpty()) {
            item {
                AppCard(label = "اطلاعات دسته چک") {
                    Text(
                        "از بین دسته‌چک‌های ثبت‌شده انتخاب کنید (اختیاری)",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ChequeBookDropdown(
                        chequeBooks = chequeBooks,
                        selected = chequeBookId,
                        onSelect = { book ->
                            chequeBookId = book?.id
                            if (chequeNumber.isBlank() && book != null) {
                                chequeNumber = book.nextSerial.toString()
                            }
                        },
                    )
                }
            }
        }
        item {
            AppCard(label = "شماره چک") {
                // Ltr: رجوع کن به کامنتِ Ltr.kt.
                Ltr {
                    OutlinedTextField(
                        value = chequeNumber,
                        onValueChange = { chequeNumber = cleanNum(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        }
        item {
            AppCard(label = "شناسه ۱۶ رقمی صیادی (اختیاری)") {
                // Ltr: رجوع کن به کامنتِ Ltr.kt.
                Ltr {
                    OutlinedTextField(
                        value = sayadId,
                        onValueChange = { sayadId = cleanNum(it).take(16) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        }
        item {
            AppCard(label = "بانک") {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "شعبه (اختیاری)") {
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = if (type == ChequeType.RECEIVED) "نام پرداخت‌کننده" else "نام دریافت‌کننده") {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            // هم‌الگو با تاریخِ اینلاینِ چرخونه‌ای «وام بانکی» (InlineJalaliDateRow) - قبلاً این‌جا
            // سه تا دراپ‌داون بود که با تقویمِ بقیه‌ی اپ هم‌شکل نبود.
            AppCard(label = "تاریخ سررسید") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InlineJalaliDateRow(
                        year = dueYear,
                        month = dueMonth,
                        day = dueDay,
                        onDateChange = { y, m, d -> dueYear = y; dueMonth = m; dueDay = d },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showCalendarPicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "انتخاب از تقویم")
                    }
                }
            }
        }
        item {
            AppCard(label = "بابت (اختیاری)") {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
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
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        error = when {
                            amount <= 0 -> "مبلغ چک رو وارد کن"
                            chequeNumber.trim().isEmpty() -> "شماره چک رو وارد کن"
                            bankName.trim().isEmpty() -> "اسم بانک رو وارد کن"
                            ownerName.trim().isEmpty() -> "این فیلد رو وارد کن"
                            else -> null
                        }
                        if (error == null) {
                            if (existing == null) {
                                viewModel.addCheque(
                                    type = type,
                                    amount = amount,
                                    chequeNumber = chequeNumber.trim(),
                                    sayadId = sayadId.trim(),
                                    bankName = bankName.trim(),
                                    branchName = branchName.trim(),
                                    ownerName = ownerName.trim(),
                                    dueYear = dueYear,
                                    dueMonth = dueMonth,
                                    dueDay = dueDay,
                                    notes = notes.trim(),
                                    chequeBookId = chequeBookId,
                                    photoPath = photoPath,
                                    nationalId = nationalId.trim(),
                                    previousBalance = cleanNumDecimal(previousBalanceText).toDoubleOrNull(),
                                    depositAmount = cleanNumDecimal(depositAmountText).toDoubleOrNull(),
                                    onSaved = { savedOk = true },
                                )
                            } else {
                                viewModel.updateCheque(
                                    existing.copy(
                                        type = type.name,
                                        amount = amount,
                                        chequeNumber = chequeNumber.trim(),
                                        sayadId = sayadId.trim().takeIf { it.isNotBlank() },
                                        bankName = bankName.trim(),
                                        branchName = branchName.trim(),
                                        ownerName = ownerName.trim(),
                                        dueYear = dueYear,
                                        dueMonth = dueMonth,
                                        dueDay = dueDay,
                                        notes = notes.trim(),
                                        chequeBookId = chequeBookId,
                                        photoPath = photoPath,
                                        nationalId = nationalId.trim().takeIf { it.isNotBlank() },
                                        previousBalance = cleanNumDecimal(previousBalanceText).toDoubleOrNull(),
                                        depositAmount = cleanNumDecimal(depositAmountText).toDoubleOrNull(),
                                    ),
                                    onSaved = { savedOk = true },
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ذخیره چک")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("انصراف")
                }
            }
        }
    }
    }
    SuccessCheckmarkOverlay(visible = savedOk, onFinished = onSaved)
}

@Composable
private fun ChequeTypeToggleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AppPrimary.copy(alpha = 0.16f) else AppSurface,
        border = BorderStroke(1.dp, if (selected) AppPrimary else AppLine),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                color = if (selected) AppPrimary else AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                subtitle,
                color = AppMuted,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChequeBookDropdown(
    chequeBooks: List<ChequeBookEntity>,
    selected: Long?,
    onSelect: (ChequeBookEntity?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBook = chequeBooks.firstOrNull { it.id == selected }
    val selectedLabel = selectedBook?.let { "${it.ownerName} - ${it.bankName}" } ?: "بدون دسته‌چک"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("بدون دسته‌چک") }, onClick = { onSelect(null); expanded = false })
            chequeBooks.forEach { book ->
                DropdownMenuItem(
                    text = { Text("${book.ownerName} - ${book.bankName}") },
                    onClick = { onSelect(book); expanded = false },
                )
            }
        }
    }
}
