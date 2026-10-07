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
private data class PendingLoanPayment(val ms: List<Int>, val paidDate: PersianDate?)
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

    var editingRowM by remember { mutableStateOf<Int?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var applyAllPromptAmount by remember { mutableStateOf<Double?>(null) }
    var payChoiceM by remember { mutableStateOf<Int?>(null) }
    // فریمِ `66c`: برداشتنِ پرداخت **همیشه** دیالوگ می‌گیرد - چه از تپ چه از منو. قبلاً تپ
    // روی ردیفِ پرداخت‌شده بی‌صدا پاکش می‌کرد و کاربر اصلاً خبر نداشت این کار ممکن است.
    var confirmUnmarkM by remember { mutableStateOf<Int?>(null) }
    var lateDateM by remember { mutableStateOf<Int?>(null) }
    var lateYear by remember { mutableStateOf(1404) }
    var lateMonth by remember { mutableStateOf(1) }
    var lateDay by remember { mutableStateOf(1) }
    // عکس رسیدِ مخصوص یه قسطِ خاص (نه یه عکس کلی رو کل وام) - فقط رو قسط‌های پرداخت‌شده در دسترسه؛
    // چون این دیالوگ همیشه از رو یه ردیفِ مشخصِ همینِ وام باز می‌شه، «کدوم وام و کدوم قسط» خودش
    // مشخصه (خواسته‌ی کاربر).
    var photoRowM by remember { mutableStateOf<Int?>(null) }

    // پرداختِ گروهیِ اقساط: چندتا قسطِ پرداخت‌نشده رو انتخاب می‌کنیم، بعد یه‌جا (با یه سوالِ
    // «به‌موقع یا با تاخیر» مشترک برای همه‌شون) پرداخت‌شده علامت می‌زنیم - به‌جای تک‌تک زدنِ هرکدوم.
    var bulkPayMode by remember { mutableStateOf(false) }
    var selectedBulkMs by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var bulkPayChoiceOpen by remember { mutableStateOf(false) }
    // پرداختِ گروهی با تأخیر: قبلاً فقط «امروز» ثبت می‌شد؛ حالا تاریخِ واقعی انتخاب می‌شود (فریمِ `11b`).
    var bulkLateMs by remember { mutableStateOf<List<Int>?>(null) }

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
    var showEditMetaDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeletePaymentsAsk by remember { mutableStateOf(false) }
    var editMetaName by remember { mutableStateOf("") }
    var editMetaBank by remember { mutableStateOf("") }
    var editMetaBorrower by remember { mutableStateOf("") }
    var editMetaYear by remember { mutableStateOf(1404) }
    var editMetaMonth by remember { mutableStateOf(1) }
    var editMetaDay by remember { mutableStateOf(1) }
    var editMetaGraceMonths by remember { mutableStateOf(0) }
    // نوعِ وام - نشانِ ردیفِ فهرست از همین می‌آید. `null` یعنی هنوز انتخاب نشده و نشان از
    // نامِ وام حدس زده می‌شود (رجوع کن به `loanGlyphFor`).
    var editMetaCategory by remember(loan.id) { mutableStateOf(viewModel.categoryOf(loan)) }
    // مبلغ/تعدادِ اقساطِ وامِ محاسبه‌شده فقط وقتی هنوز هیچ قسطی پرداخت نشده قابلِ‌ویرایشه - رجوع کن
    // به کامنتِ LoanRepository.updateComputedLoanAmount برای دلیلِ این محدودیت.
    val canEditComputedAmount = loan.paidCount == 0
    var editMetaAmountText by remember { mutableStateOf("") }
    var editMetaNText by remember { mutableStateOf("") }

    // **فریمِ `36b`**: ویرایشِ مشخصاتِ وام صفحه‌ی کامل است نه پنجره‌ی شلوغ - محتوا اسکرول
    // می‌خورد و «ذخیره‌ی تغییرات» پایینِ صفحه ثابت می‌ماند.
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

    // **کارتِ `36d`**: تپ رو هر ردیفِ قسط یه **صفحه‌ی کامل** باز می‌کنه (یادداشت + چند عکسِ
    // رسید + شماره‌ی پیگیری)، نه دیگه دیالوگِ کوچیکِ تک‌عکسیِ قبلی.
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

    Box(modifier = Modifier.fillMaxSize()) {
    // ── بازطراحیِ «داخلِ وام» (طرحِ ChatGPT، ۲ مهر) ─────────────────────────────────────
    // یک صفحه‌ی پیوسته: کارتِ هویت ← باکسِ سه‌عددی ← سه تب (پیش‌فرض: جدولِ اقساط) ← یادآوری.
    // اقساط حالا **آیتم‌های خودِ لیستِ اصلی‌اند** (نه جعبه‌ی اسکرولِ تودرتو)، پس با پایین‌رفتن
    // کلِ صفحه مالِ همان‌ها می‌شود. «تسویه‌ی زودتر» به خواسته‌ی کاربر حذف شد.
    // کارهای اصلی (حذف / تقویم / پرداخت) در نوارِ چسبانِ پایین‌اند.
    val detailListState = rememberLazyListState()
    val today = remember { JalaliCalendar.today() }
    val nextRow = remember(rows) { rows.firstOrNull { it["paid"] != true } }
    var detailTab by rememberSaveable { mutableStateOf(0) }
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
        item {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
            }
            // 🚨 **نامِ بانک از جدولِ مشخصات به سرصفحه آمد** (بندِ ۲ی بخشِ ۸۰): نامِ بانک
            // **هویتِ** وام است نه یکی از مشخصاتش، پس کنارِ نامِ وام می‌نشیند نه در فهرست.
            // نامِ وام از سرصفحه به **کارتِ هویت** رفت (طرحِ مرجعِ کاربر، ۳۱ شهریور):
            // آن‌جا کنارِ نشانِ بانک و وضعیت می‌نشیند و یک‌جا می‌گوید «این کدام وام است».
            Column(modifier = Modifier.padding(start = 4.dp).weight(1f)) {
                Text(
                    loan.name,
                    color = AppText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
                    // بازبینیِ ۹ مهر: `loan.amount` برای وامِ با دوره‌ی تنفس «اصل + سودِ تنفس» است؛ اگر همان
                    // دوباره به محاسبه برود، سودِ تنفس با هر ذخیره یک بارِ دیگر اضافه می‌شد. اصلِ وام برگردانده می‌شود.
                    val graceForEdit = viewModel.getLoanGraceMonths(loan)
                    val originalPrincipal = if (graceForEdit > 0) {
                        loan.amount / (1 + viewModel.getLoanRatePct(loan) / 100.0 / 365.0 * (graceForEdit * 30))
                    } else loan.amount
                    editMetaAmountText = rialToToman(Math.round(originalPrincipal)).toString()
                    editMetaNText = loan.n.toString()
                    showEditMetaDialog = true
                }
            }) {
                // کاربر فکر می‌کرد ویرایش حذف شده (۶ مهر) - مدادِ خاکستری دیده نمی‌شد.
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش مشخصات وام", tint = AppPrimaryInk, modifier = Modifier.size(19.dp))
                }
            }
        }

        LoanIdentityCard(
            name = loan.name,
            bank = loan.bank,
            settled = settled,
            overdue = overdueCount > 0,
            amount = loan.amount,
            months = loan.n,
            ratePct = ratePct,
            startLabel = remember(loan) {
                val sd = viewModel.getLoanStartDate(loan)
                "${toFa(sd.y)}/${toFa(sd.m)}/${toFa(sd.d)}"
            },
            paidFraction = paidFraction,
            privacyMode = privacyMode,
        )
        LoanKeyStatsCard(
            total = loan.n,
            installment = displayInstallment,
            installmentLabel = if (installmentsVary) "قسطِ بعدی" else "مبلغِ هر قسط",
            nextDueLabel = nextDueLabel,
            dueInDays = nextDueInDays,
            privacyMode = privacyMode,
        )
        SegmentedToggle(
            // «پرداخت‌ها» حذف شد (۶ مهر): همان جدولِ اقساط بود، فقط فیلترشده.
            options = listOf("جدولِ اقساط", "جزئیاتِ وام"),
            selectedIndex = detailTab,
            onSelect = { detailTab = it },
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        }
        }

        if (detailTab == 0) {
            item {
                Column(modifier = swipeShift) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "اقساط",
                            color = AppMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        // پرداختِ گروهی: به‌جای تک‌تک زدنِ هر قسط، چندتا رو انتخاب می‌کنیم و یه‌جا پرداخت
                        // می‌کنیم - فقط وقتی حداقل یه قسطِ پرداخت‌نشده داشته باشیم معنی داره.
                        if (rows.any { it["paid"] != true }) {
                            TextButton(onClick = {
                                bulkPayMode = !bulkPayMode
                                selectedBulkMs = emptySet()
                            }) {
                                Text(
                                    if (bulkPayMode) "انصراف" else "پرداخت گروهی",
                                    color = if (bulkPayMode) AppDanger else AppPrimary,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = bulkPayMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (selectedBulkMs.isEmpty()) "چندتا قسطِ پرداخت‌نشده رو انتخاب کن" else "${toFa(selectedBulkMs.size)} قسط انتخاب شده",
                                color = AppMuted,
                                fontSize = 12.sp,
                            )
                        }
                    }

                }
            }
        }

        if (detailTab == 1) {
            item { Box(swipeShift) { PaymentRhythm(rows = rows, today = today, onOpenAll = { detailTab = 0 }) } }
            item { Box(swipeShift) {
                val lastDue = rows.lastOrNull()?.get("dueDate") as? Map<*, *>
                LoanSpecsCard(
                    amount = loan.amount,
                    ratePct = ratePct,
                    n = loan.n,
                    borrower = remember(loan) { viewModel.getLoanBorrower(loan) },
                    endLabel = lastDue?.let {
                        val y = (it["y"] as? Number)?.toInt()
                        val mo = (it["m"] as? Number)?.toInt()
                        if (y != null && mo != null) "${persianMonthName(mo)} ${toFa(y)}" else null
                    },
                    totalInterest = remember(rows, loan) {
                        if (rows.isEmpty()) null else {
                            val sum = rows.sumOf { (it["installment"] as? Number)?.toDouble() ?: loan.installment }
                            (sum - loan.amount).takeIf { it > 0.0 }
                        }
                    },
                    privacyMode = privacyMode,
                )
            } }
            item { Box(swipeShift) {
                run {
                    var attachmentTab by remember(loan.id) { mutableStateOf(0) }
                    var noteText by remember(loan.id) { mutableStateOf(viewModel.getLoanNotes(loan)) }
                    var noteDirty by remember(loan.id) { mutableStateOf(false) }

                    // طرحِ ChatGPT (۷ مهر، پایینِ جزئیاتِ وام): یک کارت با دو تبِ «یادداشت / عکس رسید» -
                    // به‌جای دو دکمه‌ی بازشونده که محتوایشان پایین‌ترِ صفحه گم می‌شد.
                    AppCard(modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                        Column {
                            SegmentedToggle(
                                options = listOf("یادداشت", "عکس رسید"),
                                selectedIndex = attachmentTab,
                                onSelect = { attachmentTab = it },
                                icons = listOf(Icons.Filled.EditNote, Icons.Filled.PhotoCamera),
                            )
                            if (attachmentTab == 0) {
                                Text(
                                    "برای افزودن عنوان، یکی از موارد زیر را انتخاب کنید.",
                                    color = AppMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                )
                                androidx.compose.foundation.layout.FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                ) {
                                    listOf("شماره حساب", "شماره کارت", "شماره پیگیری", "توضیحات").forEach { preset ->
                                        AppChip(
                                            label = preset,
                                            selected = false,
                                            onClick = {
                                                noteText = if (noteText.isBlank()) "$preset: " else "$noteText\n$preset: "
                                                noteDirty = true
                                            },
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it; noteDirty = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                                    trailingIcon = {
                                        if (noteText.isNotEmpty()) {
                                            IconButton(onClick = { noteText = ""; noteDirty = true }) {
                                                Icon(Icons.Filled.Close, contentDescription = "پاک‌کردنِ یادداشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                                            }
                                        }
                                    }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(),)
                                GradientButton(
                                    onClick = {
                                        viewModel.updateLoanNotes(loan, noteText)
                                        noteDirty = false
                                    },
                                    enabled = noteDirty,
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                ) {
                                    Text(if (noteDirty) "ذخیره" else "ذخیره شد")
                                }
                            } else {
                                PhotoAttachmentCard(
                                    photoPath = loan.photoPath,
                                    onPick = { uri -> viewModel.setLoanPhoto(loan, uri) },
                                    onRemove = { viewModel.removeLoanPhoto(loan) },
                                    withCard = false,
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                )
                            }
                        }
                    }
                }

            } }
        }

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
        items(shownRows, key = { "row-" + ((it["m"] as? Number)?.toInt() ?: 0) }) { row ->
            val m = (row["m"] as? Number)?.toInt() ?: 0
            InstallmentRow(
                modifier = Modifier.animateItem().then(swipeShift).padding(horizontal = 14.dp),
                row = row,
                loan = loan,
                privacyMode = privacyMode,
                bulkPayMode = bulkPayMode,
                selected = m in selectedBulkMs,
                dueInDays = (row["dueDate"] as? Map<*, *>)?.let { due ->
                    val y = (due["y"] as? Number)?.toInt()
                    val mm = (due["m"] as? Number)?.toInt()
                    val d = (due["d"] as? Number)?.toInt()
                    if (y != null && mm != null && d != null) {
                        viewModel.daysUntilToday(PersianDate(y, mm, d))
                    } else {
                        null
                    }
                },
                isNext = row === nextRow,
                onTogglePaid = { rowM, paid ->
                    if (bulkPayMode) {
                        if (row["paid"] != true) {
                            selectedBulkMs = if (rowM in selectedBulkMs) {
                                selectedBulkMs - rowM
                            } else {
                                selectedBulkMs + rowM
                            }
                        }
                    } else if (!paid) {
                        // تپ **فقط می‌زند، برنمی‌دارد** (`64c`)؛ برداشتن از منویِ ردیف با دیالوگِ تایید.
                        payChoiceM = rowM
                    }
                },
                onUnmark = { rowM -> confirmUnmarkM = rowM },
                onOpenPhoto = { rowM -> photoRowM = rowM },
                onEditAmount = { rowM, installment ->
                    editingRowM = rowM
                    editAmountText = rialToToman(installment.toLong()).toString()
                },
            )
        }

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
