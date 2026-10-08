package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoCamera
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.calendar.DeviceCalendarExporter
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.CoinCelebration
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** یه عملِ «پرداخت‌شده کردن»ِ درحالِ‌انتظار - قبل از اجرای واقعیش، اگه حسابی وجود داشته باشه اول
 * باید حساب/کارتِ پرداخت‌کننده انتخاب بشه (رجوع کن به AccountPickerDialog تو LoanDetailScreen).
 * paidDate == null یعنی «به‌موقع»، غیرِnull یعنی «با تاخیر» با همون تاریخ. */
internal data class PendingLoanPayment(val ms: List<Int>, val paidDate: PersianDate?)
/**
 * پورت openDetail/renderTable تو www/index.html، برای وام‌های دستی (method=manual): هر قسط
 * وضعیت پرداخت مستقل داره و تاریخ سررسید واقعی (از startDate + intervalDays محاسبه می‌شه). تپ رو
 * قسطِ پرداخت‌نشده پورت openPayModal رو انجام می‌ده (انتخاب «به‌موقع» یا «با تاخیر» + تاریخ واقعی
 * پرداخت)؛ تپ رو قسطِ پرداخت‌شده مثل handlePayButton فوری برمی‌گردونه به حالت پرداخت‌نشده. ویرایش
 * دستی مبلغ هر قسط هم هست (پورت confirmEditInstallment)، همراه سوال «رو همه‌ی اقساط هم اعمال
 * کنم؟» بعد از ذخیره.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LoanDetailScreen(
    loan: LoanEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    viewModel: MyLoansViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val privacyMode = LocalPrivacyMode.current
    val accounts by accountViewModel.accounts.collectAsState()
    // rows دیگه نمی‌تونه محاسبه‌ی همزمان (remember{}) باشه چون از رو رَدیف‌های واقعیِ Room
    // (loan_rows) می‌خونه، نه دیگه از رو JSONِ درون‌حافظه‌ای که همیشه از قبل تو خودِ loan بود - رجوع
    // کن به CLAUDE.md. با هر تغییرِ loan (مثلاً بعدِ پرداختِ یه قسط) دوباره لود می‌شه، دقیقاً همون
    // reactivity ای که remember(loan) قبلاً می‌داد.
    val rowsState = remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var rows by rowsState
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
    val calendarMessageState = remember { mutableStateOf<String?>(null) }
    var calendarMessage by calendarMessageState
    LaunchedEffect(calendarMessage) {
        if (calendarMessage != null) {
            kotlinx.coroutines.delay(2600)
            calendarMessage = null
        }
    }
    // نشونه‌ی فوری «داره کار می‌کنه» - قبلاً بین زدنِ دکمه تا نتیجه‌ی نهایی هیچ فیدبکی نبود، برای
    // وامی با ۱۲۰ قسط این می‌تونست چند ثانیه طول بکشه (رجوع کن به کامنتِ applyBatch تو
    // DeviceCalendarExporter) و کاربر فکر می‌کرد «هیچ اتفاقی نمی‌افته».
    val isExportingCalendarState = remember { mutableStateOf(false) }
    var isExportingCalendar by isExportingCalendarState
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
                    "[وام] قسط ${toFa(m)} ${loan.name} (${amountToman(amountByM[m] ?: loan.installment)} تومان)"
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

    val editingRowMState = remember { mutableStateOf<Int?>(null) }
    var editingRowM by editingRowMState
    val editAmountTextState = remember { mutableStateOf("") }
    var editAmountText by editAmountTextState
    val applyAllPromptAmountState = remember { mutableStateOf<Double?>(null) }
    var applyAllPromptAmount by applyAllPromptAmountState
    val payChoiceMState = remember { mutableStateOf<Int?>(null) }
    var payChoiceM by payChoiceMState
    // فریمِ `66c`: برداشتنِ پرداخت **همیشه** دیالوگ می‌گیرد - چه از تپ چه از منو. قبلاً تپ
    // روی ردیفِ پرداخت‌شده بی‌صدا پاکش می‌کرد و کاربر اصلاً خبر نداشت این کار ممکن است.
    val confirmUnmarkMState = remember { mutableStateOf<Int?>(null) }
    var confirmUnmarkM by confirmUnmarkMState
    val lateDateMState = remember { mutableStateOf<Int?>(null) }
    var lateDateM by lateDateMState
    val lateYearState = remember { mutableStateOf(1404) }
    var lateYear by lateYearState
    val lateMonthState = remember { mutableStateOf(1) }
    var lateMonth by lateMonthState
    val lateDayState = remember { mutableStateOf(1) }
    var lateDay by lateDayState
    // عکس رسیدِ مخصوص یه قسطِ خاص (نه یه عکس کلی رو کل وام) - فقط رو قسط‌های پرداخت‌شده در دسترسه؛
    // چون این دیالوگ همیشه از رو یه ردیفِ مشخصِ همینِ وام باز می‌شه، «کدوم وام و کدوم قسط» خودش
    // مشخصه (خواسته‌ی کاربر).
    val photoRowMState = remember { mutableStateOf<Int?>(null) }
    var photoRowM by photoRowMState

    // پرداختِ گروهیِ اقساط: چندتا قسطِ پرداخت‌نشده رو انتخاب می‌کنیم، بعد یه‌جا (با یه سوالِ
    // «به‌موقع یا با تاخیر» مشترک برای همه‌شون) پرداخت‌شده علامت می‌زنیم - به‌جای تک‌تک زدنِ هرکدوم.
    val bulkPayModeState = remember { mutableStateOf(false) }
    var bulkPayMode by bulkPayModeState
    val selectedBulkMsState = remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedBulkMs by selectedBulkMsState
    val bulkPayChoiceOpenState = remember { mutableStateOf(false) }
    var bulkPayChoiceOpen by bulkPayChoiceOpenState
    // پرداختِ گروهی با تأخیر: قبلاً فقط «امروز» ثبت می‌شد؛ حالا تاریخِ واقعی انتخاب می‌شود (فریمِ `11b`).
    val bulkLateMsState = remember { mutableStateOf<List<Int>?>(null) }
    var bulkLateMs by bulkLateMsState

    // سینکِ خودکارِ پرداختِ وام ↔ حسابداری (تصمیمِ صریحِ کاربر، رجوع کن به CLAUDE.md): بعدِ انتخابِ
    // «به‌موقع»/«با تاخیر»، قبلِ ثبتِ واقعیِ پرداخت، باید حساب/کارتِ پرداخت‌کننده مشخص بشه - فقط
    // وقتی حداقل یه حساب از قبل ساخته شده (وگرنه این مرحله معنی نداره، مستقیم ثبت می‌شه).
    var pendingPayment by remember { mutableStateOf<PendingLoanPayment?>(null) }
    fun commitPayment(payment: PendingLoanPayment, account: AccountEntity?) {
        if (payment.ms.size == 1) {
            val m = payment.ms.first()
            if (payment.paidDate != null) viewModel.setRowPaidLate(loan, m, payment.paidDate) else viewModel.setRowPaidOnTime(loan, m)
        } else {
            if (payment.paidDate != null) viewModel.setRowsPaidLate(loan, payment.ms, payment.paidDate) else viewModel.setRowsPaidOnTime(loan, payment.ms)
        }
        if (account != null) {
            val amount = payment.ms.sumOf { m ->
                (rows.firstOrNull { (it["m"] as? Number)?.toInt() == m }?.get("installment") as? Number)?.toDouble()
                    ?: loan.installment
            }
            // تاریخِ تراکنش = تاریخِ واقعیِ پرداخت اگر «با تأخیر» ثبت شد، وگرنه امروز.
            val today = payment.paidDate ?: JalaliCalendar.today()
            val description = if (payment.ms.size == 1) {
                "قسط ${toFa(payment.ms.first())} - ${loan.name}"
            } else {
                "${toFa(payment.ms.size)} قسط - ${loan.name}"
            }
            accountViewModel.addTransaction(
                accountId = account.id,
                type = TransactionType.WITHDRAWAL,
                amount = amount,
                description = description,
                year = today.y,
                month = today.m,
                day = today.d,
                category = "قسط/چک",
                sourceType = "loan",
                sourceId = "${loan.id}:${payment.ms.joinToString(",")}",
            )
        } else {
            // خواسته‌ی صریحِ کاربر («باید جایی اعلام کنی») - قبلاً وقتی هیچ حسابی نبود، پرداخت
            // بی‌سروصدا تو حسابداری ثبت نمی‌شد و هیچ توضیحی هم داده نمی‌شد.
            calendarMessage = "این قسط تو حسابداری ثبت نشد - برای اینکه خودکار ثبت بشه، از تبِ «دارایی» یه حساب بساز."
        }
    }
    // اگه هنوز هیچ حسابی ساخته نشده، مرحله‌ی انتخابِ حساب معنی نداره - مستقیم ثبت می‌شه (بدونِ
    // تراکنشِ حسابداری)؛ وگرنه اول AccountPickerDialog باز می‌شه (رجوع کن به تصمیمِ کاربر - فعلاً
    // اجباری).
    fun applyPayment(payment: PendingLoanPayment) {
        if (accounts.isEmpty()) commitPayment(payment, null) else pendingPayment = payment
    }
    if (pendingPayment != null && accounts.isNotEmpty()) {
        AccountPickerDialog(
            accounts = accounts,
            onSelect = { account ->
                commitPayment(pendingPayment!!, account)
                pendingPayment = null
            },
            onDismiss = { pendingPayment = null },
        )
    }

    // ویرایشِ مشخصاتِ وام‌های محاسبه‌شده/قرض‌الحسنه - برخلافِ وام‌های دستی که فرمِ کاملِ
    // AddManualLoanScreen رو باز می‌کنن (onEdit، از MyLoansScreen)، این یه دیالوگِ سبکِ همین‌جاست.
    // مبلغ/تعدادِ اقساط این نوع وام‌ها فقط وقتی هنوز هیچ قسطی پرداخت نشده قابلِ‌ویرایشه (رجوع کن به
    // canEditComputedAmount پایین‌تر + LoanRepository.updateComputedLoanAmount) - چون عوض‌کردنشون
    // نیازمندِ اجرای دوباره‌ی فرمولِ کاملِ LoanCalculator و بازسازیِ کاملِ ردیف‌هاست، که اگه قبلاً
    // پرداختی ثبت شده باشه، تاریخچه‌ش گم می‌شه.
    val showEditMetaDialogState = remember { mutableStateOf(false) }
    var showEditMetaDialog by showEditMetaDialogState
    val showDeleteConfirmState = remember { mutableStateOf(false) }
    var showDeleteConfirm by showDeleteConfirmState
    val showDeletePaymentsAskState = remember { mutableStateOf(false) }
    var showDeletePaymentsAsk by showDeletePaymentsAskState
    val editMetaNameState = remember { mutableStateOf("") }
    var editMetaName by editMetaNameState
    val editMetaBankState = remember { mutableStateOf("") }
    var editMetaBank by editMetaBankState
    val editMetaBorrowerState = remember { mutableStateOf("") }
    var editMetaBorrower by editMetaBorrowerState
    val editMetaYearState = remember { mutableStateOf(1404) }
    var editMetaYear by editMetaYearState
    val editMetaMonthState = remember { mutableStateOf(1) }
    var editMetaMonth by editMetaMonthState
    val editMetaDayState = remember { mutableStateOf(1) }
    var editMetaDay by editMetaDayState
    val editMetaGraceMonthsState = remember { mutableStateOf(0) }
    var editMetaGraceMonths by editMetaGraceMonthsState
    // نوعِ وام - نشانِ ردیفِ فهرست از همین می‌آید. `null` یعنی هنوز انتخاب نشده و نشان از
    // نامِ وام حدس زده می‌شود (رجوع کن به `loanGlyphFor`).
    val editMetaCategoryState = remember(loan.id) { mutableStateOf(viewModel.categoryOf(loan)) }
    var editMetaCategory by editMetaCategoryState
    // مبلغ/تعدادِ اقساطِ وامِ محاسبه‌شده فقط وقتی هنوز هیچ قسطی پرداخت نشده قابلِ‌ویرایشه - رجوع کن
    // به کامنتِ LoanRepository.updateComputedLoanAmount برای دلیلِ این محدودیت.
    val canEditComputedAmount = loan.paidCount == 0
    val editMetaAmountTextState = remember { mutableStateOf("") }
    var editMetaAmountText by editMetaAmountTextState
    val editMetaNTextState = remember { mutableStateOf("") }
    var editMetaNText by editMetaNTextState

    // **فریمِ `36b`**: ویرایشِ مشخصاتِ وام صفحه‌ی کامل است نه پنجره‌ی شلوغ - محتوا اسکرول
    // می‌خورد و «ذخیره‌ی تغییرات» پایینِ صفحه ثابت می‌ماند.
    LoanEditMetaDialog(
        loan = loan,
        viewModel = viewModel,
        canEditComputedAmount = canEditComputedAmount,
        showEditMetaDialogState = showEditMetaDialogState,
        editMetaNameState = editMetaNameState,
        editMetaBankState = editMetaBankState,
        editMetaBorrowerState = editMetaBorrowerState,
        editMetaYearState = editMetaYearState,
        editMetaMonthState = editMetaMonthState,
        editMetaDayState = editMetaDayState,
        editMetaGraceMonthsState = editMetaGraceMonthsState,
        editMetaCategoryState = editMetaCategoryState,
        editMetaAmountTextState = editMetaAmountTextState,
        editMetaNTextState = editMetaNTextState,
    )

    // **کارتِ `36d`**: تپ رو هر ردیفِ قسط یه **صفحه‌ی کامل** باز می‌کنه (یادداشت + چند عکسِ
    // رسید + شماره‌ی پیگیری)، نه دیگه دیالوگِ کوچیکِ تک‌عکسیِ قبلی.
    LoanInstallmentDetailHost(
        loan = loan,
        viewModel = viewModel,
        photoRowMState = photoRowMState,
        rowsState = rowsState,
    )

    LoanEditRowAmountDialog(
        loan = loan,
        viewModel = viewModel,
        editingRowMState = editingRowMState,
        editAmountTextState = editAmountTextState,
        applyAllPromptAmountState = applyAllPromptAmountState,
    )

    LoanApplyAllAmountDialog(
        loan = loan,
        viewModel = viewModel,
        applyAllPromptAmountState = applyAllPromptAmountState,
    )

    LoanPayChoiceDialog(
        applyPayment = ::applyPayment,
        payChoiceMState = payChoiceMState,
        lateDateMState = lateDateMState,
        lateYearState = lateYearState,
        lateMonthState = lateMonthState,
        lateDayState = lateDayState,
    )

    LoanBulkPayChoiceDialog(
        applyPayment = ::applyPayment,
        bulkPayChoiceOpenState = bulkPayChoiceOpenState,
        bulkLateMsState = bulkLateMsState,
        bulkPayModeState = bulkPayModeState,
        lateYearState = lateYearState,
        lateMonthState = lateMonthState,
        lateDayState = lateDayState,
        rowsState = rowsState,
        selectedBulkMsState = selectedBulkMsState,
    )

    LoanLateDateDialog(
        applyPayment = ::applyPayment,
        lateDateMState = lateDateMState,
        lateYearState = lateYearState,
        lateMonthState = lateMonthState,
        lateDayState = lateDayState,
        rowsState = rowsState,
    )

    LoanBulkLateDialog(
        applyPayment = ::applyPayment,
        bulkLateMsState = bulkLateMsState,
        bulkPayModeState = bulkPayModeState,
        lateYearState = lateYearState,
        lateMonthState = lateMonthState,
        lateDayState = lateDayState,
        selectedBulkMsState = selectedBulkMsState,
    )

    Box(modifier = Modifier.fillMaxSize()) {
    // ── بازطراحیِ «داخلِ وام» (طرحِ ChatGPT، ۲ مهر) ─────────────────────────────────────
    // یک صفحه‌ی پیوسته: کارتِ هویت ← باکسِ سه‌عددی ← سه تب (پیش‌فرض: جدولِ اقساط) ← یادآوری.
    // اقساط حالا **آیتم‌های خودِ لیستِ اصلی‌اند** (نه جعبه‌ی اسکرولِ تودرتو)، پس با پایین‌رفتن
    // کلِ صفحه مالِ همان‌ها می‌شود. «تسویه‌ی زودتر» به خواسته‌ی کاربر حذف شد.
    // کارهای اصلی (حذف / تقویم / پرداخت) در نوارِ چسبانِ پایین‌اند.
    val detailListState = rememberLazyListState()
    val today = remember { JalaliCalendar.today() }
    val nextRow = remember(rows) { rows.firstOrNull { it["paid"] != true } }
    val detailTabState = rememberSaveable { mutableStateOf(0) }
    var detailTab by detailTabState
    if (detailTab > 1) detailTab = 0
    // کشیدنِ افقی روی صفحه تب را عوض می‌کند، مثلِ ورق‌زدنِ گالری (خواسته‌ی کاربر): محتوای تب
    // دنبالِ انگشت می‌آید، اگر از یک‌چهارمِ عرض رد شد بیرون می‌رود و تبِ کناری از سمتِ دیگر
    // می‌آید، وگرنه برمی‌گردد. در RTL تبِ بعدی سمتِ چپ است، پس کشیدن به راست = تبِ بعدی.
    val tabSwipe = remember { Animatable(0f) }
    var swipeWidth by remember { mutableStateOf(1f) }
    val swipeShift = Modifier.graphicsLayer {
        translationX = tabSwipe.value
        alpha = 1f - (kotlin.math.abs(tabSwipe.value) / swipeWidth).coerceIn(0f, 1f) * 0.7f
    }
    val tabSwipeGesture = Modifier
        .onSizeChanged { swipeWidth = it.width.toFloat().coerceAtLeast(1f) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val x = tabSwipe.value
                    val target = when {
                        x > swipeWidth / 4 && detailTab < 1 -> detailTab + 1
                        x < -swipeWidth / 4 && detailTab > 0 -> detailTab - 1
                        else -> null
                    }
                    scope.launch {
                        if (target == null) {
                            tabSwipe.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                        } else {
                            val out = if (x > 0) swipeWidth else -swipeWidth
                            tabSwipe.animateTo(out, tween(140))
                            detailTab = target
                            tabSwipe.snapTo(-out)
                            tabSwipe.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                        }
                    }
                },
                onDragCancel = { scope.launch { tabSwipe.animateTo(0f) } },
            ) { change, dx ->
                change.consume()
                // لبه‌ها مقاومت دارند: بعد از تبِ آخر فقط کمی جلو می‌آید.
                val atEdge = (tabSwipe.value + dx > 0 && detailTab == 1) || (tabSwipe.value + dx < 0 && detailTab == 0)
                scope.launch { tabSwipe.snapTo(tabSwipe.value + if (atEdge) dx * 0.25f else dx) }
            }
        }
    // عددِ بالای صفحه همیشه باید مبلغِ *واقعیِ* اولین قسطِ پرداخت‌نشده رو نشون بده، نه
    // loan.installmentِ کهنه - رجوع کن به مورد ۱۴/۱۶ تو CLAUDE.md.
    // باگِ رفع‌شده: نسخه‌ی قبلی فقط وقتی «قسط‌های پرداخت‌نشده با هم فرق دارن»
    // (unpaidAmounts.distinct().size > 1) این مبلغِ واقعی رو نشون می‌داد - اگه فقط یه قسطِ
    // پرداخت‌نشده مونده باشه (مثلاً وامِ ۲قسطی که قسطِ ۱ با مبلغِ دیگه‌ای پرداخت شده)، distinct
    // size همیشه ۱ می‌شه (چیزی برای «تنوع» نیست) و کد اشتباهی fallback به فیلدِ کهنه می‌کرد،
    // با اینکه اون قسطِ تکیِ باقی‌مونده هم می‌تونست کاملاً با loan.installment فرق داشته باشه.
    val unpaidAmounts = remember(rows) {
        rows.filter { (it["paid"] as? Boolean) != true }.mapNotNull { (it["installment"] as? Number)?.toDouble() }
    }
    val nextUnpaidAmount = unpaidAmounts.firstOrNull()
    val displayInstallment = nextUnpaidAmount ?: loan.installment
    // فقط برای انتخابِ برچسب («قسطِ بعدی» در برابرِ «قسط ماهانه») - اگه قسطِ بعدی با فیلدِ کهنه
    // فرق داره یعنی دیگه «همه‌ی اقساط» یکسان نیستن.
    val installmentsVary = nextUnpaidAmount != null && nextUnpaidAmount != loan.installment

    // ── خلاصه‌ی وام - فریمِ `27b` ────────────────────────────────────────────────────
    // نسخه‌ی قبلی یه دوناتِ ۱۵۰ی تمام‌عرض بود که فقط مبلغِ قسط رو نشون می‌داد؛ فریم به‌جاش یه
    // کارتِ فشرده می‌خواد: حلقه‌ی ۸۸ی با **درصدِ پرداخت‌شده** در وسط، و چهار عددِ کلیدی کنارش.
    val paidFraction = if (loan.n > 0) (loan.paidCount.toFloat() / loan.n).coerceIn(0f, 1f) else 0f
    val ratePct = remember(loan) { viewModel.getLoanRatePct(loan) }
    // ── هیروِ وام - بخشِ ۸۰ ───────────────────────────────────────────────────────
    // سه کارتِ هم‌وزن دو کارت شد و دوازده عددِ هم‌اندازه سلسله‌مراتب گرفت.
    val overdueCount = remember(rows, today) {
        rows.count { row ->
            if (row["paid"] == true) return@count false
            val due = row["dueDate"] as? Map<*, *> ?: return@count false
            val y = (due["y"] as? Number)?.toInt() ?: return@count false
            val mo = (due["m"] as? Number)?.toInt() ?: return@count false
            val d = (due["d"] as? Number)?.toInt() ?: return@count false
            y < today.y || (y == today.y && (mo < today.m || (mo == today.m && d < today.d)))
        }
    }

    val settled = loan.n > 0 && loan.paidCount >= loan.n
    val nextDueLabel = nextRow?.let { row ->
        (row["dueDate"] as? Map<*, *>)?.let {
            val d = (it["d"] as? Number)?.toInt()
            val mo = (it["m"] as? Number)?.toInt()
            val yr = (it["y"] as? Number)?.toInt()
            // اگر سررسید در سالِ دیگری است، سال هم نوشته شود (وگرنه «۱۲۱۵ روز مانده» گیج‌کننده است).
            if (d != null && mo != null) {
                "${toFa(d)} ${persianMonthName(mo)}" + if (yr != null && yr != today.y) " ${toFa(yr)}" else ""
            } else null
        }
    }
    val nextDueInDays = nextRow?.let { row ->
        (row["dueDate"] as? Map<*, *>)?.let { due ->
            val y = (due["y"] as? Number)?.toInt()
            val mo = (due["m"] as? Number)?.toInt()
            val d = (due["d"] as? Number)?.toInt()
            if (y != null && mo != null && d != null) viewModel.daysUntilToday(PersianDate(y, mo, d)) else null
        }
    }
    // اولین بار که وام باز می‌شود، مستقیم می‌رود روی مرزِ آخرین قسطِ پرداخت‌شده («اگه ده تا
    // رفتم، از اول نیاد») - ولی فقط وقتی قسط‌های پرداخت‌شده آن‌قدر زیادند که ردیفِ بعدی دیده نشود.
    var hasAutoScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(rows) {
        if (hasAutoScrolled || rows.isEmpty()) return@LaunchedEffect
        hasAutoScrolled = true
        val firstUnpaid = rows.indexOfFirst { it["paid"] != true }
        if (firstUnpaid > 3) detailListState.scrollToItem(DETAIL_ROWS_START + firstUnpaid - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = detailListState,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .then(tabSwipeGesture)
            .lazyColumnScrollbar(detailListState, AppPrimary),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // آیتمِ ۰: سرصفحه + کارتِ هویت + باکسِ سه‌عددی + تب‌ها. `DETAIL_ROWS_START` به همین بسته است.
        loanIdentityItem(
            loan = loan,
            onBack = onBack,
            onEdit = onEdit,
            viewModel = viewModel,
            isManualLoan = isManualLoan,
            privacyMode = privacyMode,
            displayInstallment = displayInstallment,
            installmentsVary = installmentsVary,
            paidFraction = paidFraction,
            ratePct = ratePct,
            overdueCount = overdueCount,
            settled = settled,
            nextDueLabel = nextDueLabel,
            nextDueInDays = nextDueInDays,
            showEditMetaDialogState = showEditMetaDialogState,
            editMetaNameState = editMetaNameState,
            editMetaBankState = editMetaBankState,
            editMetaBorrowerState = editMetaBorrowerState,
            editMetaYearState = editMetaYearState,
            editMetaMonthState = editMetaMonthState,
            editMetaDayState = editMetaDayState,
            editMetaGraceMonthsState = editMetaGraceMonthsState,
            editMetaAmountTextState = editMetaAmountTextState,
            editMetaNTextState = editMetaNTextState,
            detailTabState = detailTabState,
        )

        loanTabZeroItems(
            swipeShift = swipeShift,
            rowsState = rowsState,
            bulkPayModeState = bulkPayModeState,
            selectedBulkMsState = selectedBulkMsState,
            detailTabState = detailTabState,
        )

        loanTabOneItems(
            loan = loan,
            viewModel = viewModel,
            privacyMode = privacyMode,
            today = today,
            swipeShift = swipeShift,
            ratePct = ratePct,
            rowsState = rowsState,
            detailTabState = detailTabState,
        )

        // تبِ اول همه‌ی اقساط، تبِ سوم فقط پرداخت‌شده‌ها - هر دو از همان `rows`.
        val shownRows = when (detailTab) {
            0 -> rows
            2 -> rows.filter { it["paid"] == true }
            else -> emptyList()
        }
        if (detailTab == 2 && shownRows.isEmpty()) {
            item {
                Text(
                    "هنوز قسطی پرداخت نشده",
                    color = AppMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = swipeShift.fillMaxWidth().padding(vertical = 24.dp),
                )
            }
        }
        loanInstallmentItems(
            loan = loan,
            viewModel = viewModel,
            privacyMode = privacyMode,
            nextRow = nextRow,
            swipeShift = swipeShift,
            shownRows = shownRows,
            editingRowMState = editingRowMState,
            editAmountTextState = editAmountTextState,
            payChoiceMState = payChoiceMState,
            confirmUnmarkMState = confirmUnmarkMState,
            photoRowMState = photoRowMState,
            bulkPayModeState = bulkPayModeState,
            selectedBulkMsState = selectedBulkMsState,
        )

        if (detailTab == 1) item {
            ReminderOverrideCard(
                currentOffsets = loan.reminderDayOffsets,
                onChange = { viewModel.setLoanReminderOffsets(loan, it) },
                modifier = Modifier.padding(horizontal = 14.dp),
                title = "یادآوری قسط وام",
            )
        }
    }

    // ── نوارِ چسبانِ پایین ─────────────────────────────────────────────────────────
    // در حالتِ پرداختِ گروهی جایش را به «جمعِ انتخاب‌شده + پرداخت» می‌دهد (فریمِ `64b`).
    LoanDetailBottomBar(
        loan = loan,
        privacyMode = privacyMode,
        settled = settled,
        nextRow = nextRow,
        context = context,
        runCalendarExport = ::runCalendarExport,
        calendarPermissionLauncher = calendarPermissionLauncher,
        bulkPayChoiceOpenState = bulkPayChoiceOpenState,
        bulkPayModeState = bulkPayModeState,
        calendarMessageState = calendarMessageState,
        isExportingCalendarState = isExportingCalendarState,
        payChoiceMState = payChoiceMState,
        rowsState = rowsState,
        selectedBulkMsState = selectedBulkMsState,
        showDeleteConfirmState = showDeleteConfirmState,
    )
    }
    LoanUnmarkConfirmDialog(
        loan = loan,
        viewModel = viewModel,
        confirmUnmarkMState = confirmUnmarkMState,
    )
    LoanDeleteConfirmDialog(
        loan = loan,
        showDeleteConfirmState = showDeleteConfirmState,
        showDeletePaymentsAskState = showDeletePaymentsAskState,
    )
    LoanDeletePaymentsDialog(
        loan = loan,
        viewModel = viewModel,
        onDelete = onDelete,
        showDeletePaymentsAskState = showDeletePaymentsAskState,
    )


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
                    .background(AppText.copy(alpha = 0.92f), RoundedCornerShape(AppRadius.button))
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
/** اندیسِ اولین ردیفِ قسط در لیستِ اصلی: آیتمِ ۰ سرصفحه/کارت‌ها/تب‌ها، آیتمِ ۱ سرِ «اقساط». */
private const val DETAIL_ROWS_START = 2
