package ir.sadteam.loancalc.ui.myloans

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.calendar.DeviceCalendarExporter
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinCelebration
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    onEdit: () -> Unit,
    viewModel: MyLoansViewModel = hiltViewModel(),
) {
    val privacyMode = LocalPrivacyMode.current
    // rows دیگه نمی‌تونه محاسبه‌ی همزمان (remember{}) باشه چون از رو رَدیف‌های واقعیِ Room
    // (loan_rows) می‌خونه، نه دیگه از رو JSONِ درون‌حافظه‌ای که همیشه از قبل تو خودِ loan بود - رجوع
    // کن به CLAUDE.md. با هر تغییرِ loan (مثلاً بعدِ پرداختِ یه قسط) دوباره لود می‌شه، دقیقاً همون
    // reactivity ای که remember(loan) قبلاً می‌داد.
    var rows by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    LaunchedEffect(loan) {
        rows = viewModel.getRows(loan)
    }
    // آیکونِ ویرایشِ هدر رو هر وامی هست، ولی مقصدش فرق می‌کنه: وام‌های دستی فرمِ کاملِ
    // AddManualLoanScreen (onEdit، شاملِ مبلغ/تعدادِ اقساط) رو باز می‌کنن؛ وام‌های محاسبه‌شده/
    // قرض‌الحسنه (ساختارِ اقساطِ نامساوی دارن) فقط دیالوگِ سبکِ اسم/بانک/تاریخ رو - رجوع کن به
    // showEditMetaDialog پایین‌تر.
    val isManualLoan = remember(loan) { viewModel.isManualLoan(loan) }

    // جشنِ تسویه‌ی کامل (بارش سکه + ویبره): فقط وقتی «همین الان» آخرین قسط تو همین صفحه پرداخت
    // بشه (گذر از ناتمام→تمام)، نه موقعِ باز کردنِ وامی که از قبل تسویه‌شده بوده - برای همین
    // wasFullyPaid با وضعیتِ لحظه‌ی ورود مقداردهی می‌شه.
    var celebrate by remember { mutableStateOf(false) }
    var wasFullyPaid by remember { mutableStateOf(loan.n > 0 && loan.paidCount >= loan.n) }
    val celebrationBuzz = rememberBuzz(durationMs = 60)
    LaunchedEffect(loan.paidCount, loan.n) {
        val fullyPaid = loan.n > 0 && loan.paidCount >= loan.n
        if (fullyPaid && !wasFullyPaid) {
            celebrate = true
            celebrationBuzz()
        }
        wasFullyPaid = fullyPaid
    }

    // «افزودن سررسیدها به تقویم گوشی»: همه‌ی اقساط (پرداخت‌شده‌ها هم، با خط‌خورده) به‌صورت رویدادِ
    // تمام‌روز تو تقویم خودِ گوشی درج می‌شن (خواسته‌ی کاربر که قبلاً دستی این‌کارو می‌کرد). درج تو
    // IO انجام می‌شه چون یه وام می‌تونه ۱۲۰ قسط داشته باشه. یه‌بارمصرفه (رجوع کن به
    // LoanEntity.calendarExported) تا با هر بار کلیک رویدادهای تکراری درج نشه.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // نتیجه با یه بنرِ داخلِ خودِ اپ نشون داده می‌شه، نه Toast سیستمی - چون Android 12+ (و بعضی
    // رام‌ها مثل MIUI) خودکار آیکونِ اپ رو کنارِ متنِ Toast می‌چسبونن که کاربر خواست حذف بشه (دقیقاً
    // همون دلیلی که هینتِ خروج تو MainActivity هم Toast نیست).
    var calendarMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(calendarMessage) {
        if (calendarMessage != null) {
            kotlinx.coroutines.delay(2600)
            calendarMessage = null
        }
    }
    // نشونه‌ی فوری «داره کار می‌کنه» - قبلاً بین زدنِ دکمه تا نتیجه‌ی نهایی هیچ فیدبکی نبود، برای
    // وامی با ۱۲۰ قسط این می‌تونست چند ثانیه طول بکشه (رجوع کن به کامنتِ applyBatch تو
    // DeviceCalendarExporter) و کاربر فکر می‌کرد «هیچ اتفاقی نمی‌افته».
    var isExportingCalendar by remember { mutableStateOf(false) }
    fun runCalendarExport() {
        isExportingCalendar = true
        // قبلاً قسط‌های پرداخت‌شده اصلاً درج نمی‌شدن؛ حالا همه درج می‌شن (پرداخت‌شده‌ها با
        // خط‌خورده - رجوع کن به DeviceCalendarExporter.strikethrough) تا یه رکورد کامل باشه.
        val items = rows.mapNotNull { row ->
            val m = (row["m"] as? Number)?.toInt() ?: return@mapNotNull null
            val due = row["dueDate"] as? Map<*, *> ?: return@mapNotNull null
            val y = (due["y"] as? Number)?.toInt() ?: return@mapNotNull null
            val mo = (due["m"] as? Number)?.toInt() ?: return@mapNotNull null
            val d = (due["d"] as? Number)?.toInt() ?: return@mapNotNull null
            DeviceCalendarExporter.InstallmentItem(m, PersianDate(y, mo, d), paid = row["paid"] == true)
        }
        val amountByM = rows.associate {
            ((it["m"] as? Number)?.toInt() ?: 0) to ((it["installment"] as? Number)?.toDouble() ?: loan.installment)
        }
        scope.launch(Dispatchers.IO) {
            // قبلاً هیچ try/catch ای اینجا نبود: بعضی گوشی‌ها (مخصوصاً رام‌های سفارشی مثل MIUI) با
            // اینکه مجوز رو گرفتی، سرِ خودِ insert بازم SecurityException/IllegalArgumentException
            // پرت می‌کنن - چون این کوروتین رو Dispatchers.IO بدون هیچ catchی بود، این استثنا مستقیم
            // می‌رفت رو CrashReporter (Thread.UncaughtExceptionHandler سراسری) که فقط گزارشش می‌کنه،
            // نه نمایشش؛ نتیجه برای کاربر دقیقاً «هیچ اتفاقی نمی‌افته» بود (نه پیام موفقیت، نه خطا).
            val result = runCatching {
                DeviceCalendarExporter.insertInstallmentEvents(context, items) { m ->
                    "[وام] قسط ${toFa(m)} ${loan.name} (${fmt(amountByM[m] ?: loan.installment)} ریال)"
                }
            }
            withContext(Dispatchers.Main) {
                isExportingCalendar = false
                calendarMessage = result.fold(
                    onSuccess = { inserted ->
                        if (inserted > 0) {
                            // یه‌بار موفق شد - persist می‌شه تا این دکمه دیگه هیچ‌وقت (نه فقط تو
                            // همین session) دوباره درج نکنه؛ رجوع کن به AppCard پایین‌تر.
                            viewModel.markCalendarExported(loan)
                            "${toFa(inserted)} قسط به تقویم گوشی اضافه شد"
                        } else {
                            "تقویم قابل‌نوشتنی رو گوشی پیدا نشد"
                        }
                    },
                    onFailure = { "این گوشی اجازه نداد به تقویم اضافه بشه" },
                )
            }
        }
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.all { it }) {
            runCalendarExport()
        } else {
            isExportingCalendar = false
            calendarMessage = "بدون مجوز تقویم نمی‌شه سررسیدها رو اضافه کرد"
        }
    }

    var editingRowM by remember { mutableStateOf<Int?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var applyAllPromptAmount by remember { mutableStateOf<Double?>(null) }
    var payChoiceM by remember { mutableStateOf<Int?>(null) }
    var lateDateM by remember { mutableStateOf<Int?>(null) }
    var lateYear by remember { mutableStateOf(1404) }
    var lateMonth by remember { mutableStateOf(1) }
    var lateDay by remember { mutableStateOf(1) }
    // عکس رسیدِ مخصوص یه قسطِ خاص (نه یه عکس کلی رو کل وام) - فقط رو قسط‌های پرداخت‌شده در دسترسه؛
    // چون این دیالوگ همیشه از رو یه ردیفِ مشخصِ همینِ وام باز می‌شه، «کدوم وام و کدوم قسط» خودش
    // مشخصه (خواسته‌ی کاربر).
    var photoRowM by remember { mutableStateOf<Int?>(null) }

    // ویرایشِ مشخصاتِ وام‌های محاسبه‌شده/قرض‌الحسنه - برخلافِ وام‌های دستی که فرمِ کاملِ
    // AddManualLoanScreen رو باز می‌کنن (onEdit، از MyLoansScreen)، این یه دیالوگِ سبکِ همین‌جاست.
    // مبلغ/تعدادِ اقساط این نوع وام‌ها فقط وقتی هنوز هیچ قسطی پرداخت نشده قابلِ‌ویرایشه (رجوع کن به
    // canEditComputedAmount پایین‌تر + LoanRepository.updateComputedLoanAmount) - چون عوض‌کردنشون
    // نیازمندِ اجرای دوباره‌ی فرمولِ کاملِ LoanCalculator و بازسازیِ کاملِ ردیف‌هاست، که اگه قبلاً
    // پرداختی ثبت شده باشه، تاریخچه‌ش گم می‌شه.
    var showEditMetaDialog by remember { mutableStateOf(false) }
    var editMetaName by remember { mutableStateOf("") }
    var editMetaBank by remember { mutableStateOf("") }
    var editMetaBorrower by remember { mutableStateOf("") }
    var editMetaYear by remember { mutableStateOf(1404) }
    var editMetaMonth by remember { mutableStateOf(1) }
    var editMetaDay by remember { mutableStateOf(1) }
    var editMetaGraceMonths by remember { mutableStateOf(0) }
    // مبلغ/تعدادِ اقساطِ وامِ محاسبه‌شده فقط وقتی هنوز هیچ قسطی پرداخت نشده قابلِ‌ویرایشه - رجوع کن
    // به کامنتِ LoanRepository.updateComputedLoanAmount برای دلیلِ این محدودیت.
    val canEditComputedAmount = loan.paidCount == 0
    var editMetaAmountText by remember { mutableStateOf("") }
    var editMetaNText by remember { mutableStateOf("") }

    if (showEditMetaDialog) {
        AlertDialog(
            onDismissRequest = { showEditMetaDialog = false },
            title = { Text("ویرایش مشخصات وام") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editMetaName,
                        onValueChange = { editMetaName = it },
                        label = { Text("اسم وام") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = editMetaBank,
                        onValueChange = { editMetaBank = it },
                        label = { Text("اسم بانک یا فروشنده") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = editMetaBorrower,
                        onValueChange = { editMetaBorrower = it },
                        label = { Text("وام‌گیرنده (اختیاری)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // این ردیف از قبل قابلِ‌ویرایش بود ولی هیچ لیبلی نداشت (برخلافِ فرمِ وامِ دستی
                    // که همینو تو یه AppCard با عنوانِ «تاریخ دریافت وام» نشون می‌ده) - خواسته‌ی
                    // کاربر: این گزینه واضح/قابلِ‌کشف باشه.
                    Text("تاریخ دریافت وام", fontSize = 13.sp, color = AppMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InlineJalaliDateRow(
                            year = editMetaYear,
                            month = editMetaMonth,
                            day = editMetaDay,
                            onDateChange = { y, m, d -> editMetaYear = y; editMetaMonth = m; editMetaDay = d },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // همون توضیحِ دینامیکِ BankLoanScreen - این وام (محاسبه‌شده) ممکنه دوره‌ی تنفس
                    // داشته باشه، پس این تاریخ همیشه سررسیدِ قسطِ اول نیست.
                    Text(
                        if (editMetaGraceMonths > 0) {
                            "قسطِ اول ${toFa(editMetaGraceMonths)} ماه بعد از این تاریخه (به‌خاطرِ دوره‌ی تنفس)"
                        } else {
                            "این تاریخ = سررسیدِ قسطِ اول"
                        },
                        fontSize = 11.sp,
                        color = AppMuted,
                    )
                    // مبلغ/تعدادِ اقساط فقط وقتی هیچ قسطی پرداخت نشده قابلِ‌ویرایشه - رجوع کن به
                    // کامنتِ بالای canEditComputedAmount. اگه یه قسط پرداخت شده باشه، تغییرشون یعنی
                    // کلِ فرمول دوباره اجرا بشه و تاریخچه‌ی پرداخت گم بشه، برای همین قفله.
                    if (canEditComputedAmount) {
                        OutlinedTextField(
                            value = editMetaAmountText,
                            onValueChange = { editMetaAmountText = cleanNum(it) },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            label = { Text("مبلغ وام") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = editMetaNText,
                            onValueChange = { editMetaNText = cleanNum(it) },
                            label = { Text("تعداد اقساط") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
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
            },
            confirmButton = {
                TextButton(onClick = {
                    if (canEditComputedAmount) {
                        viewModel.updateComputedLoanAmount(
                            loan = loan,
                            name = editMetaName.trim().ifEmpty { loan.name },
                            bank = editMetaBank.trim(),
                            borrower = editMetaBorrower.trim().ifEmpty { "—" },
                            principalAmount = editMetaAmountText.toDoubleOrNull() ?: loan.amount,
                            n = editMetaNText.toIntOrNull()?.takeIf { it > 0 } ?: loan.n,
                            startDate = PersianDate(editMetaYear, editMetaMonth, editMetaDay),
                            onSaved = {},
                        )
                    } else {
                        viewModel.updateLoanMeta(
                            loan = loan,
                            name = editMetaName.trim().ifEmpty { loan.name },
                            bank = editMetaBank.trim(),
                            borrower = editMetaBorrower.trim().ifEmpty { "—" },
                            startDate = PersianDate(editMetaYear, editMetaMonth, editMetaDay),
                            onSaved = {},
                        )
                    }
                    showEditMetaDialog = false
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { showEditMetaDialog = false }) { Text("انصراف") }
            },
        )
    }

    if (photoRowM != null) {
        val m = photoRowM!!
        val row = rows.firstOrNull { (it["m"] as? Number)?.toInt() == m }
        val photoPath = row?.get("photoPath") as? String
        AlertDialog(
            onDismissRequest = { photoRowM = null },
            title = { Text("رسید قسط") },
            text = {
                Column {
                    Text("وام: ${loan.name}", color = AppMuted, fontSize = 12.sp)
                    Text("قسط شماره ${toFa(m)}", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    PhotoAttachmentCard(
                        photoPath = photoPath,
                        onPick = { uri -> viewModel.setRowPhoto(loan, m, uri, photoPath) },
                        onRemove = { viewModel.removeRowPhoto(loan, m, photoPath) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { photoRowM = null }) { Text("بستن") }
            },
        )
    }

    if (editingRowM != null) {
        AlertDialog(
            onDismissRequest = { editingRowM = null },
            title = { Text("ویرایش مبلغ قسط ${toFa(editingRowM ?: 0)}") },
            text = {
                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
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
                        options = (1350..1410).map { it to toFa(it) },
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

    Box(modifier = Modifier.fillMaxSize()) {
    // کل صفحه یه LazyColumn واحده (نه اسکرولِ تودرتوی قبلی): قبلاً لیستِ اقساط یه LazyColumn با
    // ارتفاعِ محدود داخلِ یه Columnِ اسکرول‌دار بود که با یه آستانه‌ی اسکرول بین ۵ و ۱۰ ردیف
    // باز/جمع می‌شد - تغییرِ ارتفاع وسطِ درگ محتوا رو زیرِ انگشت می‌پروند و نزدیکِ آستانه هم
    // رفت‌وبرگشتی می‌شد (گزارشِ کاربر: «یهو بزرگ می‌شه... قاطی می‌کنه»). الان اقساط آیتم‌های خودِ
    // لیستِ اصلی‌ان: هرچی پایین‌تر بری طبیعتاً کلِ صفحه رو می‌گیرن، روون و بدونِ هیچ پرش/حالتِ خاصی.
    val detailListState = rememberLazyListState()
    LazyColumn(
        state = detailListState,
        modifier = Modifier
            .fillMaxWidth()
            .lazyColumnScrollbar(detailListState, AppPrimary),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
            Text(
                loan.name,
                color = AppText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp).weight(1f),
            )
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
                    editMetaAmountText = loan.amount.toLong().toString()
                    editMetaNText = loan.n.toString()
                    showEditMetaDialog = true
                }
            }) {
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش مشخصات وام", tint = AppMuted)
            }
        }

        // اگه اقساطِ پرداخت‌نشده با هم برابر نباشن (مثلاً بعدِ ویرایشِ تکیِ یه قسط، یا وامِ
        // قرض‌الحسنه‌ای که ذاتاً اقساطِ کارمزدی/اصلِ‌وام نابرابر داره)، loan.installment دیگه
        // نماینده‌ی «همه‌ی اقساط» نیست - عددِ بالای صفحه به‌جاش مبلغِ اولین قسطِ پرداخت‌نشده
        // («قسطِ بعدی») رو نشون می‌ده، که هم درسته هم مفیدتره (خواسته‌ی کاربر - قبلاً بعدِ ویرایشِ
        // تکی، عددِ بالا مبلغِ قدیمی/گمراه‌کننده می‌موند).
        val unpaidAmounts = remember(rows) {
            rows.filter { (it["paid"] as? Boolean) != true }.mapNotNull { (it["installment"] as? Number)?.toDouble() }
        }
        val installmentsVary = unpaidAmounts.distinct().size > 1
        val displayInstallment = if (installmentsVary) unpaidAmounts.first() else loan.installment

        // دایره‌ی شیک بالای وام (سبز = اصل، طلایی = سود) با قسط ماهانه تو مرکز - مثل نسخه‌ی وب.
        val principalFrac = if (loan.totalPaid > 0) (loan.amount / loan.totalPaid).toFloat() else 1f
        LoanDonut(
            principalFraction = principalFrac,
            centerTop = {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, fmt(displayInstallment)), color = AppText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            centerBottom = if (installmentsVary) "قسطِ بعدی (ریال)" else "قسط ماهانه (ریال)",
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )

        AppCard(label = loan.bank, modifier = Modifier.padding(horizontal = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // weight(1f) فقط رو ستونِ مبلغ - چون متنِ حروفیِ مبلغ (numberToWordsFa) طولش به
                // اندازه‌ی خودِ عدد فرق می‌کنه، بدونِ weight یه Row معمولی هر دو ستون رو مستقل و با
                // عرضِ کاملِ Row اندازه می‌گرفت؛ وقتی این متن برای یه مبلغِ بزرگ خیلی بلند می‌شد، جای
                // کافی برای ستونِ «پرداخت‌شده» نمی‌موند و اون یکی کلمه‌به‌کلمه (حتی حرف‌به‌حرف) می‌شکست
                // (گزارشِ کاربر با اسکرین‌شات، بعدِ ویرایشِ مبلغ). با weight رو این ستون، Row اول
                // عرضِ ثابتِ ستونِ «پرداخت‌شده» رو تضمین می‌کنه، بعد باقیِ فضا رو به این ستون می‌ده.
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(if (installmentsVary) "قسطِ بعدی" else "مبلغ هر قسط", fontSize = 13.sp, color = AppMuted)
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text("${maskIfPrivate(masked, fmt(displayInstallment))} ریال", fontSize = 15.sp, color = AppText, fontWeight = FontWeight.Bold)
                    }
                    if (!privacyMode) {
                        Text(
                            "${numberToWordsFa(displayInstallment / 10)} تومان",
                            fontSize = 11.sp,
                            color = AppMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (installmentsVary) {
                        Text(
                            "چون اقساطِ این وام باهم فرق دارن",
                            fontSize = 10.sp,
                            color = AppMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
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

        // هر قسط یه باکس مینیمالِ گوشه‌گرد با حاشیه‌ی سبزه (خواسته‌ی کاربر). وضعیت پرداخت:
        // به‌موقع=سبز «پرداخت شد»، با تأخیر=قرمز «با تأخیر»، پرداخت‌نشده=مشکی «پرداخت نشده».
            Text(
                "اقساط",
                color = AppMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 18.dp, top = 4.dp),
            )
        }
        }

        item {
            // جعبه‌ی اقساط - حالا «هوشمند»: کوچیک وقتی داری از کنارش رد می‌شی، ولی وقتی واقعاً بهش
            // رسیدی (لبه‌ی بالاش نزدیکِ بالای صفحه‌ست) به‌آرومی تا نزدیکِ تمام‌صفحه بزرگ می‌شه تا
            // چندتا ردیفِ بیشتر (به‌جای ۵ تا) هم‌زمان دیده بشن، و وقتی ازش دور می‌شی دوباره کوچیک
            // می‌شه - خواسته‌ی کاربر: «میام روی اقساط، بزرگ تمام صفحه رو بگیره، گوشه‌ها کوچیک‌تر بشه».
            //
            // فرقِ کلیدی با نسخه‌ی باگ‌دارِ خیلی قبل (که برگردونده شده بود به حالتِ ساده‌ی ثابت):
            // اونجا یه آستانه‌ی تکی داشت (بالاتر/پایین‌تر از یه نقطه = عوضِ حالت) که دقیقاً رو مرز
            // می‌لرزید (رفت‌وبرگشتی) و چون خودِ اسکرول با انیمیشن هم‌زمان بود، محتوا زیرِ انگشت
            // می‌پرید. اینجا: (۱) هیسترزیس بر اساسِ درصدِ دیده‌شدنِ خودِ جعبه (نه موقعیتِ مطلقش رو
            // صفحه) - آستانه‌ی ورود ۴۵٪، خروج ۲۰٪، پس هم رو مرز نمی‌لرزه هم نیازی به رد کردنِ کاملِ
            // هدر نیست، (۲) عوضِ حالت با [snapshotFlow] فقط رو مقدارِ واقعیِ اسکرول واکنش نشون می‌ده
            // (نه یه انیمیشنِ مستقلِ رقیب)، (۳) وقتی جعبه بزرگه، اسکرولِ سریعِ «رد شو» غیرفعاله (چون
            // دیگه معنی نداره - کاربر دقیقاً اومده تو همین جعبه).
            val innerListState = rememberLazyListState()
            var expanded by remember { mutableStateOf(false) }
            // باگِ رفع‌شده: نسخه‌ی قبلی با آستانه‌ی «offset ≤ 40px از بالای صفحه» فعال می‌شد - یعنی
            // کاربر باید کلِ هدر (دکمه‌ی بازگشت + دونات + کارتِ بانک + کارتِ عکس، معمولاً ۵۰۰-۷۰۰dp)
            // رو کامل از رو صفحه رد می‌کرد تا لبه‌ی جعبه دقیقاً به بالای صفحه برسه - این خیلی بیشتر از
            // یه اسکرولِ معمولیه، برای همین کاربر گزارش داد اصلاً فول‌اسکرین نمی‌شه. الان به‌جای
            // موقعیتِ مطلق، بر اساسِ چند درصد از خودِ جعبه رو صفحه دیده می‌شه تصمیم می‌گیره - با یه
            // اسکرولِ معمولی (نه یه اسکرولِ خیلی طولانی) هم قابلِ رسیدنه.
            LaunchedEffect(detailListState) {
                snapshotFlow {
                    val info = detailListState.layoutInfo
                    val itemInfo = info.visibleItemsInfo.firstOrNull { it.index == 1 }
                    if (itemInfo == null || itemInfo.size <= 0) {
                        0f
                    } else {
                        val viewportHeight = info.viewportEndOffset - info.viewportStartOffset
                        val visibleTop = itemInfo.offset.coerceAtLeast(0)
                        val visibleBottom = (itemInfo.offset + itemInfo.size).coerceAtMost(viewportHeight)
                        (visibleBottom - visibleTop).coerceAtLeast(0).toFloat() / itemInfo.size
                    }
                }.collect { visibleFraction ->
                    expanded = when {
                        !expanded && visibleFraction >= 0.45f -> true
                        expanded && visibleFraction <= 0.2f -> false
                        else -> expanded
                    }
                }
            }
            // اولین بار که وامی رو باز می‌کنی، به‌جای شروع از قسطِ ۱، مستقیم می‌ره رو مرزِ آخرین
            // قسطِ پرداخت‌شده (خواسته‌ی کاربر: «اگه ده تا رفتم، از اول نیاد، بیاد از رو ده») - با
            // یکی قبلش هم دیده بشه که معلوم باشه آخری چی پرداخت شد.
            // **باگِ رفع‌شده**: قبلاً کلیدِ این افکت خودِ `rows` بود - چون `rows` با هر تغییرِ
            // وضعیتِ پرداختِ یه قسط (حتی همینجا، وسطِ کارِ کاربر) یه لیستِ *جدید* می‌شه، هر بار که
            // کاربر رو همین صفحه یه قسط رو «پرداخت» می‌زد، این افکت دوباره اجرا و صفحه به‌زورِ
            // اسکرول به مرزِ بعدی می‌پرید - دقیقاً همون چیزی که کاربر نمی‌خواستش.
            // چون `rows` الان از رو Room (loan_rows) به‌صورتِ async لود می‌شه (رجوع کن به
            // CLAUDE.md)، اولین مقدارش همیشه یه لیستِ خالیه - کلیدِ ثابتِ `Unit` باعث می‌شد این
            // افکت دقیقاً همون لحظه‌ی خالی‌بودن اجرا بشه و اسکرول هیچ‌وقت کار نکنه. الان کلید دوباره
            // `rows`ه، ولی با یه پرچمِ «یه‌بار مصرف» (`hasAutoScrolled`) محافظت شده - همون‌قدر
            // «فقط یه‌بار، موقعِ اولین باردیدنِ دادهٔ واقعی» هست، فقط دیگه رو دادهٔ خالیِ لحظه‌ی اول
            // گیر نمی‌کنه.
            var hasAutoScrolled by remember { mutableStateOf(false) }
            LaunchedEffect(rows) {
                if (hasAutoScrolled || rows.isEmpty()) return@LaunchedEffect
                hasAutoScrolled = true
                val firstUnpaid = rows.indexOfFirst { it["paid"] != true }
                val target = (firstUnpaid - 1).coerceAtLeast(0)
                if (firstUnpaid > 0) innerListState.scrollToItem(target)
            }
            val flingConnection = remember(detailListState) {
                installmentsFlingPassthrough(
                    outerListState = detailListState,
                    scope = scope,
                    beforeBoxIndex = 0,
                    afterBoxIndex = 2,
                    isExpanded = { expanded },
                )
            }
            val density = LocalDensity.current
            val compactHeight = installmentRowHeight * 5 + 8.dp * 4
            val viewportHeightPx = detailListState.layoutInfo.viewportEndOffset - detailListState.layoutInfo.viewportStartOffset
            val expandedHeight = with(density) { (viewportHeightPx * 0.92f).toDp() }
            val boxHeight by animateDpAsState(
                targetValue = if (expanded) expandedHeight else compactHeight,
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                label = "installmentsBoxHeight",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(boxHeight)
                    .nestedScroll(flingConnection),
            ) {
                // contentPadding(start): بدونش ردیف‌ها دقیقاً تا لبه‌ی همین Box کشیده می‌شدن - همون
                // لبه‌ای که lazyColumnScrollbar خطش رو روش رسم می‌کنه (چون drawWithContent مستقیم
                // از رو size.width رسم می‌کنه، همیشه لبه‌ی فیزیکیِ راستِ همون Box، صرف‌نظر از
                // جهت)، پس خط دقیقاً رو حاشیه‌ی باکسِ هر ردیف می‌افتاد (گزارشِ کاربر: «اسکرول رفته
                // تو شکمِ باکس‌ها»).
                // **باگِ رفع‌شده (اشتباهِ RTL)**: نسخه‌ی قبلی این‌جا `end = 10.dp` بود - ولی چون کلِ
                // اپ RTL ئه، `end` تو PaddingValues یعنی سمتِ چپِ فیزیکی (نه راست)! یعنی اون فاصله
                // اصلاً رو سمتِ درستی (راست، جایی که خط واقعاً رسم می‌شه) اضافه نمی‌شد - برای همین
                // با اینکه کد قبلاً هم همین‌جوری بود، باگ همچنان دیده می‌شد. تو RTL، `start` سمتِ
                // راستِ فیزیکیه - این همون سمتیه که لازم داریم.
                LazyColumn(
                    state = innerListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .lazyColumnScrollbar(innerListState, AppPrimary),
                    contentPadding = PaddingValues(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(rows, key = { (it["m"] as? Number)?.toInt() ?: 0 }) { row ->
                        InstallmentRow(
                            // وقتی تعدادِ اقساطِ یه وام ویرایش می‌شه، ردیف‌های اضافه/کم‌شده
                            // به‌جای پرشِ ناگهانی نرم میان و می‌رن.
                            modifier = Modifier.animateItem(),
                            row = row,
                            loan = loan,
                            privacyMode = privacyMode,
                            onTogglePaid = { m, paid -> if (paid) viewModel.setRowUnpaid(loan, m) else payChoiceM = m },
                            onOpenPhoto = { m -> photoRowM = m },
                            onEditAmount = { m, installment ->
                                editingRowM = m
                                editAmountText = installment.toLong().toString()
                            },
                        )
                    }
                }
            }
        }

        item {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReminderOverrideCard(
                currentOffsets = loan.reminderDayOffsets,
                onChange = { viewModel.setLoanReminderOffsets(loan, it) },
            )
            OutlinedButton(
                enabled = !isExportingCalendar,
                onClick = {
                    if (loan.calendarExported) {
                        // دیگه دوباره درج نمی‌کنیم (جلوگیری از رویدادهای تکراری تو تقویم گوشی با
                        // هر بار کلیک) - فقط یادآوری می‌کنیم قبلاً اضافه شده.
                        calendarMessage = "سررسیدهای این وام قبلاً به تقویم گوشی اضافه شده‌اند"
                        return@OutlinedButton
                    }
                    isExportingCalendar = true
                    val perms = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                    val allGranted = perms.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) runCalendarExport() else calendarPermissionLauncher.launch(perms)
                },
                colors = if (loan.calendarExported) {
                    ButtonDefaults.outlinedButtonColors(contentColor = AppMuted)
                } else {
                    ButtonDefaults.outlinedButtonColors()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp),
                )
                Text(if (isExportingCalendar) "در حال افزودن..." else "افزودن سررسیدها به تقویم گوشی")
            }
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

        AnimatedVisibility(
            visible = calendarMessage != null,
            enter = fadeIn(tween(Motion.FADE_IN_MS)) + slideInVertically(Motion.offset()) { it / 2 },
            exit = fadeOut(tween(Motion.FADE_OUT_MS)) + slideOutVertically(Motion.offset()) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(AppText.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(calendarMessage ?: "", color = AppSurface, fontSize = 13.sp)
            }
        }

        // رو همه‌چیزِ صفحه (آخرین بچه‌ی Box) - لمس رو مصرف نمی‌کنه، فقط ~۳ ثانیه سکه می‌باره.
        if (celebrate) {
            CoinCelebration(
                modifier = Modifier.fillMaxSize(),
                onFinished = { celebrate = false },
            )
        }
    }
}

private val installmentRowHeight = 56.dp

/** آستانه‌ی سرعتِ فلینگ (px/s) که پایین‌ترش «درگِ آهسته/هدفمند» حساب می‌شه (بمون تو اقساط، فقط
 * اسکرولِ داخلی)، بالاترش «سواپِ سریع» (رد شو از کل جعبه) - رجوع کن به [installmentsFlingPassthrough]. */
private const val INSTALLMENTS_FLING_SKIP_VELOCITY = 3500f

/**
 * وقتی کاربر رو جعبه‌ی اقساط یه فلینگِ *سریع* بزنه (نه یه درگِ آهسته‌ی هدفمند برای دیدنِ اقساط)،
 * به‌جای اینکه لیستِ داخلیِ اقساط خودش فلینگ کنه (که با ۱۲۰ ردیف ممکنه چندین‌بار لازم باشه)، مستقیم
 * لیستِ بیرونیِ صفحه رو به آیتمِ قبل/بعدِ جعبه می‌بره - یعنی حسِ «از جعبه رد شو» بدونِ نیاز به رسیدنِ
 * دستی به لبه‌ی بالا/پایینِ لیستِ داخلی. درگِ آهسته (سرعتِ فلینگِ پایین) دست‌نخورده می‌مونه و طبقِ
 * رفتارِ پیش‌فرضِ nested-scrollِ Compose فقط لیستِ داخلی رو اسکرول می‌کنه.
 */
private fun installmentsFlingPassthrough(
    outerListState: LazyListState,
    scope: CoroutineScope,
    beforeBoxIndex: Int,
    afterBoxIndex: Int,
    isExpanded: () -> Boolean,
): NestedScrollConnection = object : NestedScrollConnection {
    override suspend fun onPreFling(available: Velocity): Velocity {
        // وقتی جعبه تمام‌صفحه‌ست، «رد شو»یِ سریع دیگه معنی نداره - کاربر دقیقاً همین‌جا می‌خواد
        // بمونه و لیست رو مرور کنه؛ فلینگِ سریع باید عادی خودِ لیستِ داخلی رو اسکرول کنه.
        if (isExpanded()) return Velocity.Zero
        if (kotlin.math.abs(available.y) < INSTALLMENTS_FLING_SKIP_VELOCITY) return Velocity.Zero
        val targetIndex = if (available.y < 0) afterBoxIndex else beforeBoxIndex
        scope.launch { outerListState.animateScrollToItem(targetIndex) }
        return available
    }
}

/** یه ردیفِ قسط - هر قسط یه باکس مینیمالِ گوشه‌گرد با حاشیه‌ی سبزه (خواسته‌ی کاربر). وضعیت پرداخت:
 * به‌موقع=سبز «پرداخت شد»، با تأخیر=قرمز «با تأخیر»، پرداخت‌نشده=مشکی «پرداخت نشده». */
@Composable
private fun InstallmentRow(
    row: Map<String, Any?>,
    loan: LoanEntity,
    privacyMode: Boolean,
    onTogglePaid: (m: Int, paid: Boolean) -> Unit,
    onOpenPhoto: (m: Int) -> Unit,
    onEditAmount: (m: Int, installment: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
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
    val rowShape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(installmentRowHeight)
            .background(AppSurface, rowShape)
            .border(1.dp, AppPrimary.copy(alpha = 0.4f), rowShape)
            // پرتپ‌ترین المانِ کلِ اپ (علامت‌زدنِ پرداختِ هر قسط) ولی تا الان هیچ واکنشِ لمسی
            // نداشت - حالا مثلِ بقیه‌ی کارت‌ها فشرده می‌شه و یه tick هپتیک می‌ده.
            .pressScaleClickable(scale = 0.975f) { onTogglePaid(m, paid) }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("قسط شماره ${toFa(m)}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(dueLabel, color = AppMuted, fontSize = 12.sp)
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text("${maskIfPrivate(masked, fmt(installment))} ریال", color = AppText, fontSize = 13.sp)
        }
        // وضعیت پرداخت تو یه باکس رنگیِ گوشه‌گرد (بج) - تا از بقیه‌ی متن جدا و واضح دیده بشه
        // (خواسته‌ی کاربر). رنگ پس‌زمینه نسخه‌ی کم‌رنگِ رنگ وضعیته.
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .background(statusColor.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                statusLabel,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (paid) {
            IconButton(onClick = { onOpenPhoto(m) }) {
                val hasPhoto = (row["photoPath"] as? String) != null
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = "رسید قسط",
                    tint = if (hasPhoto) AppPrimary else AppMuted,
                )
            }
        }
        IconButton(onClick = { onEditAmount(m, installment) }) {
            Icon(Icons.Filled.Edit, contentDescription = "ویرایش مبلغ", tint = AppMuted)
        }
    }
}

/** دایره‌ی وام (سبز = اصل، طلایی = سود) با قسط ماهانه تو مرکز - پورت حس دونات نتیجه‌ی وب. */
@Composable
private fun LoanDonut(
    principalFraction: Float,
    centerTop: @Composable () -> Unit,
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
            centerTop()
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
