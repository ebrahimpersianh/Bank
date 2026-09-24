package ir.sadteam.loancalc.ui.myloans

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
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
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroTone
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.BankTile
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
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerBorder
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.pillOverSurface
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import ir.sadteam.loancalc.ui.theme.AppDueNextPill
import ir.sadteam.loancalc.ui.theme.AppDueOverdueBorder
import ir.sadteam.loancalc.ui.theme.AppDueOverduePill
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryPillBorder
import ir.sadteam.loancalc.ui.theme.AppPurpleInk
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.filled.Eco
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// نامِ ماه از `persianMonthName`ِ مشترک میاد - این لیستِ محلی کپیِ سومش بود.
// ذخیره ریال است و نمایش تومان (بندِ ۲ی README): تنها نقطه‌ی تبدیلِ این فایل.
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

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
            val today = JalaliCalendar.today()
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

    if (showEditMetaDialog) {
        JibakAlertDialog(
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
                    // انتخابِ لوگو - مورد ۱۷: قبلاً فقط یه فیلدِ متنیِ خام بود، کاربر باید اسمِ بانک
                    // رو دقیقاً درست تایپ می‌کرد تا لوگوش تو LoanDetailScreen/MyLoansScreen پیدا
                    // بشه (که با BankBadge از رو تطبیقِ اسم لوگو رو نشون می‌ده). این ردیف همون
                    // BankTileیِ BankLoanScreen رو استفاده می‌کنه - لمسِ یه لوگو اسمِ دقیقش رو تو
                    // فیلدِ بالا می‌ذاره؛ فیلد همچنان برای بانک/فروشنده‌ی خارج از لیست دستی باز می‌مونه.
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(banks, key = { it.name }) { b ->
                            BankTile(
                                bank = b,
                                selected = editMetaBank == b.name,
                                onClick = { editMetaBank = b.name },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editMetaBorrower,
                        onValueChange = { editMetaBorrower = it },
                        label = { Text("وام‌گیرنده (اختیاری)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // ═══ نوعِ وام ═══
                    // تا امروز نشانِ ردیفِ فهرست فقط از **نامِ وام** حدس زده می‌شد؛ نامی مثل
                    // «وام ۹۵ میلیونی» هیچ کلیدواژه‌ای ندارد و همیشه نشانِ پیش‌فرض می‌گرفت.
                    // این ردیف همان حدس را به انتخاب تبدیل می‌کند.
                    Text("نوعِ وام", color = AppMuted, fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LoanCategory.entries, key = { it.id }) { category ->
                            val selected = editMetaCategory == category.id
                            AppChip(
                                label = category.label,
                                selected = selected,
                                // دوباره‌زدنِ نوعِ انتخاب‌شده آن را برمی‌دارد و به حدسِ
                                // خودکار برمی‌گرداند - وگرنه راهی برای بازگشت نبود.
                                onClick = { editMetaCategory = if (selected) null else category.id },
                            )
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
                            category = editMetaCategory,
                        )
                    } else {
                        viewModel.updateLoanMeta(
                            loan = loan,
                            name = editMetaName.trim().ifEmpty { loan.name },
                            bank = editMetaBank.trim(),
                            borrower = editMetaBorrower.trim().ifEmpty { "—" },
                            startDate = PersianDate(editMetaYear, editMetaMonth, editMetaDay),
                            onSaved = {},
                            category = editMetaCategory,
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
                    value = editAmountText,
                    onValueChange = { editAmountText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                )
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
                }) { Text("ذخیره") }
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
                    applyPayment(PendingLoanPayment(selectedBulkMs.toList(), JalaliCalendar.today()))
                    selectedBulkMs = emptySet()
                    bulkPayChoiceOpen = false
                    bulkPayMode = false
                }) { Text("امروز (با تاخیر)") }
            },
        )
    }

    if (lateDateM != null) {
        val m = lateDateM!!
        JibakAlertDialog(
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
                        options = (1..12).map { it to persianMonthName(it) },
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
                    applyPayment(PendingLoanPayment(listOf(m), PersianDate(lateYear, lateMonth, lateDay)))
                    lateDateM = null
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { lateDateM = null }) { Text("انصراف") }
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
                        x > swipeWidth / 4 && detailTab < 2 -> detailTab + 1
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
                val atEdge = (tabSwipe.value + dx > 0 && detailTab == 2) || (tabSwipe.value + dx < 0 && detailTab == 0)
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
            if (d != null && mo != null) "${toFa(d)} ${persianMonthName(mo)}" else null
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
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
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
                    editMetaAmountText = loan.amount.toLong().toString()
                    editMetaNText = loan.n.toString()
                    showEditMetaDialog = true
                }
            }) {
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش مشخصات وام", tint = AppMuted)
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
            options = listOf("جدولِ اقساط", "جزئیاتِ وام", "پرداخت‌ها"),
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
                    var expandedAttachment by remember(loan.id) { mutableStateOf<String?>(null) }
                    var noteText by remember(loan.id) { mutableStateOf(viewModel.getLoanNotes(loan)) }
                    var noteDirty by remember(loan.id) { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AttachmentToggleButton(
                                label = "عکس رسید",
                                icon = Icons.Filled.PhotoCamera,
                                filled = loan.photoPath != null,
                                expanded = expandedAttachment == "photo",
                                onClick = { expandedAttachment = if (expandedAttachment == "photo") null else "photo" },
                                modifier = Modifier.weight(1f),
                            )
                            AttachmentToggleButton(
                                label = "یادداشت",
                                icon = Icons.Filled.EditNote,
                                filled = noteText.isNotBlank(),
                                expanded = expandedAttachment == "note",
                                onClick = { expandedAttachment = if (expandedAttachment == "note") null else "note" },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        AnimatedVisibility(visible = expandedAttachment == "photo") {
                            PhotoAttachmentCard(
                                photoPath = loan.photoPath,
                                onPick = { uri -> viewModel.setLoanPhoto(loan, uri) },
                                onRemove = { viewModel.removeLoanPhoto(loan) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )
                        }
                        AnimatedVisibility(visible = expandedAttachment == "note") {
                            AppCard(label = "یادداشت", modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    "مثلاً شماره حساب یا شماره کارتِ مربوط به این وام",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
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
                                    trailingIcon = {
                                        if (noteText.isNotEmpty()) {
                                            IconButton(onClick = { noteText = ""; noteDirty = true }) {
                                                Icon(Icons.Filled.Close, contentDescription = "پاک‌کردنِ یادداشت")
                                            }
                                        }
                                    },
                                )
                                if (noteDirty) {
                                    GradientButton(
                                        onClick = {
                                            viewModel.updateLoanNotes(loan, noteText)
                                            noteDirty = false
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    ) {
                                        Text("ذخیره یادداشت")
                                    }
                                }
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

        item {
            ReminderOverrideCard(
                currentOffsets = loan.reminderDayOffsets,
                onChange = { viewModel.setLoanReminderOffsets(loan, it) },
                modifier = Modifier.padding(horizontal = 14.dp),
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
            consequence = "وامِ «${loan.name}» حذف بشه؟ این کار قابلِ‌برگشت نیست.",
            actionLabel = "حذف وام",
            onConfirm = { showDeleteConfirm = false; onDelete() },
            onDismiss = { showDeleteConfirm = false },
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

private val installmentRowHeight = 64.dp

/** اندیسِ اولین ردیفِ قسط در لیستِ اصلی: آیتمِ ۰ سرصفحه/کارت‌ها/تب‌ها، آیتمِ ۱ سرِ «اقساط». */
private const val DETAIL_ROWS_START = 2


/**
 * **کارتِ «مشخصات»** — فریمِ `80a`، جانشینِ فهرستِ شش‌ردیفیِ `29p`.
 *
 * 🚨 **سه سلول در سطح، بقیه زیرِ «بیشتر»**: مبلغِ وام · نرخ · پایان، چون هر سه در جمله‌ی
 * «این چه وامی است» می‌آیند. تعدادِ قسط و سودِ کل و ضامن یک پله پایین‌ترند — همان الگوی
 * فیلدهای اضافه‌ی فرمِ چک، پس الگوی تازه‌ای به سیستم اضافه نشد.
 *
 * ⚠️ **بانک این‌جا نیست**، به سرصفحه رفت: نامِ بانک هویتِ وام است نه یکی از مشخصاتش.
 * ⚠️ **وامِ بی‌نرخ سلولِ نرخ را حذف می‌کند، صفر نمی‌گذارد** (قاعده‌ی ۳): «۰٪» گمراه‌کننده است.
 */
@Composable
private fun LoanSpecsCard(
    amount: Double,
    ratePct: Double,
    n: Int,
    borrower: String,
    endLabel: String?,
    totalInterest: Double?,
    privacyMode: Boolean,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCell("مبلغِ وام", null, amount, privacyMode, Modifier.weight(1f))
            // بی‌نرخ: سلول **حذف** می‌شود و دو سلولِ دیگر با همان `weight` پهن‌تر می‌شوند —
            // چیدمانِ تازه‌ای لازم نشد.
            if (ratePct > 0.0) {
                SpecCell("نرخ", "${toFa(fmtRate(ratePct))}٪ سالانه", null, privacyMode, Modifier.weight(1f))
            }
            SpecCell("پایان", endLabel ?: "—", null, privacyMode, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .defaultMinSize(minHeight = AppSpacing.minTouchTarget)
                .pressScaleClickable(onClick = { expanded = !expanded }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (expanded) "کمتر" else "بیشتر",
                color = AppPrimary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SpecRow("تعدادِ قسط", "${toFa(n)} قسط")
                if (totalInterest != null && totalInterest > 0.0) {
                    SpecRow("سودِ کل", null, totalInterest, privacyMode)
                }
                SpecRow("ضامن", if (borrower.isBlank() || borrower == "—") "ندارد" else borrower)
            }
        }
    }
}

/** یک سلولِ سه‌تاییِ بالای کارتِ مشخصات. */
@Composable
private fun SpecCell(
    label: String,
    value: String?,
    amount: Double?,
    privacyMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        if (value != null) {
            Text(value, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        } else {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(amount ?: 0.0)),
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

/** یه ردیفِ «برچسبِ راست ← مقدارِ چپ» تو کارتِ مشخصات. مبلغ با حالتِ خصوصی ماسک می‌شه. */
@Composable
private fun SpecRow(
    label: String,
    value: String?,
    amount: Double? = null,
    privacyMode: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        if (value != null) {
            Text(value, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        } else {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(amount ?: 0.0)),
                    color = AppText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

/**
 * **کارتِ هویتِ وام** - طرحِ مرجعِ کاربر (۳۱ شهریور).
 *
 * یک کارتِ رنگیِ بالای صفحه که در یک نگاه می‌گوید «این کدام وام است و چه شکلی است»:
 * نشانِ بانک در یک دایره، نامِ وام، بجِ وضعیت، و نوارِ چهار عددِ ثابتِ وام.
 *
 * ⚠️ **چهار عددِ این نوار هیچ‌وقت عوض نمی‌شوند** (مبلغِ وام، مدت، نرخ، تاریخِ شروع) -
 * برعکسِ کارتِ زیرش که همه‌چیزش با هر پرداخت تغییر می‌کند. همین مرز دلیلِ دو کارت
 * جدا بودن است، نه سلیقه.
 */
@Composable
private fun LoanIdentityCard(
    name: String,
    bank: String,
    settled: Boolean,
    overdue: Boolean,
    amount: Double,
    months: Int,
    ratePct: Double,
    startLabel: String,
    paidFraction: Float,
    privacyMode: Boolean,
) {
    val statusLabel = when {
        settled -> "تسویه‌شده"
        overdue -> "معوق"
        else -> "فعال"
    }
    // وضعیت فقط روی **قرصِ سفید** رنگ می‌گیرد؛ خودِ کارت همیشه رنگِ تم است (خواسته‌ی کاربر:
    // «باکسِ بالا با تم عوض بشه») - قرمزِ کلِ کارت همان شلوغی‌ای بود که طرحِ تازه برداشت.
    val statusColor = if (overdue && !settled) AppDanger else AppPrimary
    AppHeroCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // کاشیِ برگ - همان نشانِ کارت‌های قهرمانِ دیگر (کیف، بودجه) تا این کارت هم خانواده‌شان باشد.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp)) {
                Text(
                    name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (bank.isNotBlank() && bank != "—") {
                    Text(
                        bank,
                        color = HeroMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusColor),
                    )
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
            // حلقه‌ی «٪ پرداخت‌شده» گوشه‌ی چپِ کارت (طرحِ ChatGPT).
            PaidRing(
                fraction = paidFraction,
                ringColor = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                centerTop = "${toFa((paidFraction * 100).roundToInt())}٪",
                centerBottom = "پرداخت شده",
                centerTopColor = Color.White,
                centerBottomColor = HeroMuted,
                size = 70.dp,
                stroke = 7.dp,
                centerTopSize = 15,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoanIdentityStat(
                label = "مبلغِ وام",
                value = maskIfPrivate(privacyMode, amountToman(amount)),
                unit = "تومان",
                modifier = Modifier.weight(1.3f),
            )
            HeroStatDivider()
            LoanIdentityStat(label = "مدتِ کل", value = toFa(months), unit = "ماه", modifier = Modifier.weight(1f))
            HeroStatDivider()
            LoanIdentityStat(label = "نرخِ سود", value = "${fmtRate(ratePct).faDigits()}٪", unit = "سالانه", modifier = Modifier.weight(1f))
            HeroStatDivider()
            LoanIdentityStat(label = "تاریخِ شروع", value = startLabel, unit = "", modifier = Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun HeroStatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .fillMaxHeight(0.8f)
            .background(Color.White.copy(alpha = 0.25f)),
    )
}

/**
 * باکسِ سه‌عددیِ زیرِ کارتِ هویت (طرحِ ChatGPT): تعدادِ کلِ اقساط · مبلغِ هر قسط · سررسیدِ بعدی.
 * جانشینِ سه کارتِ جدای قبلی - **یک** کارت با دو خطِ جداکننده.
 */
@Composable
private fun LoanKeyStatsCard(
    total: Int,
    installment: Double,
    installmentLabel: String,
    nextDueLabel: String?,
    dueInDays: Int?,
    privacyMode: Boolean,
) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyStat(
                icon = Icons.Filled.EventNote,
                tint = AppPrimaryInk,
                bg = AppPrimaryPill,
                label = "تعدادِ کلِ اقساط",
                value = toFa(total),
                unit = "قسط",
                modifier = Modifier.weight(1f),
            )
            KeyStatDivider()
            KeyStat(
                icon = Icons.Filled.Payments,
                tint = AppInfo,
                bg = AppInfoPill,
                label = installmentLabel,
                value = maskIfPrivate(privacyMode, amountToman(installment)),
                unit = "تومان",
                modifier = Modifier.weight(1.2f),
            )
            KeyStatDivider()
            KeyStat(
                icon = Icons.Filled.CalendarMonth,
                tint = AppPurpleInk,
                bg = AppPurplePill,
                label = "سررسیدِ بعدی",
                value = nextDueLabel ?: "—",
                unit = "",
                modifier = Modifier.weight(1f),
            ) {
                if (dueInDays != null) {
                    val late = dueInDays < 0
                    Text(
                        when {
                            late -> "${toFa(-dueInDays)} روز گذشته"
                            dueInDays == 0 -> "امروز"
                            else -> "${toFa(dueInDays)} روز مانده"
                        },
                        color = if (late || dueInDays == 0) AppDangerInk else AppInfo,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (late || dueInDays == 0) AppDangerPill else AppInfoPill)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyStatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .fillMaxHeight(0.75f)
            .background(AppLine),
    )
}

@Composable
private fun KeyStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(999.dp)).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            color = AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            value,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(unit, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        extra()
    }
}

/** یک دکمه‌ی نوارِ چسبانِ پایین (پرداخت / تقویم / حذف) - کپسولِ رنگی با آیکون. */
@Composable
private fun LoanActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    ink: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .alpha(if (enabled) 1f else 0.6f)
            .pressScaleClickable { if (enabled) onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
        Text(
            label,
            color = ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun LoanIdentityStat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = HeroMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            value,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(unit, color = HeroMuted, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/**
 * **ریتمِ پرداخت** — فریمِ `80a`، جانشینِ حلقه‌ی درصد.
 *
 * 🚨 حلقه **جابه‌جا نشد، حذف شد**: همان ۱۷٪ حالا نوارِ تهِ هیروست، و یک درصد دو بار کشیدن
 * یعنی دو گرافیک برای یک عدد. جایش المانی آمد که **داده‌ی تازه** دارد: یک میله برای هر
 * قسط. سه میله‌ی قرمزِ پشتِ‌هم یعنی دیرکرد **پیوسته** بوده نه پراکنده — چیزی که هیچ عددِ
 * این صفحه نمی‌گوید.
 *
 * ⚠️ **میله‌ها ماسکِ حالتِ خصوصی نمی‌گیرند**: مبلغ نشان نمی‌دهند، فقط وضعیت.
 * ⚠️ بالای **۶۰** قسط میله‌ها به یک لکه می‌رسند، پس آن‌جا هر میله **یک سال** می‌شود و
 *    زیرنویس هم عوض می‌شود.
 * ⚠️ `ProgressRing.kt` حذف نشد — جای دیگری استفاده می‌شود؛ فقط از این صفحه برداشته شد.
 */
@Composable
private fun PaymentRhythm(
    rows: List<Map<String, Any?>>,
    today: PersianDate,
    onOpenAll: () -> Unit,
) {
    if (rows.isEmpty()) return
    val paidColor = AppPrimary
    val lateColor = AppDanger
    val nextColor = AppAccent
    val emptyColor = AppSurface2

    // چهار حالتِ هر قسط، از همان داده‌ی فهرستِ اقساط. `MyLoansViewModel` تغییری لازم نداشت.
    val states = remember(rows, today) {
        var nextMarked = false
        rows.map { row ->
            val paid = row["paid"] == true
            val due = row["dueDate"] as? Map<*, *>
            val y = (due?.get("y") as? Number)?.toInt()
            val mo = (due?.get("m") as? Number)?.toInt()
            val d = (due?.get("d") as? Number)?.toInt()
            val past = y != null && mo != null && d != null &&
                (y < today.y || (y == today.y && (mo < today.m || (mo == today.m && d < today.d))))
            when {
                paid -> 0
                past -> 1
                !nextMarked -> {
                    nextMarked = true
                    2
                }
                else -> 3
            }
        }
    }
    val byYear = states.size > 60
    // بالای ۶۰ قسط هر میله یک سال است و **بدترین** حالتِ همان سال را می‌گیرد، چون خبرِ بد
    // نباید زیرِ میانگین گم شود.
    val allBars = if (!byYear) states else states.chunked(12).map { chunk -> chunk.minOrNull() ?: 3 }
    val allLabels = remember(rows, byYear) {
        if (byYear) {
            allBars.indices.map { "سالِ ${toFa(it + 1)}" }
        } else {
            rows.map { row ->
                val due = row["dueDate"] as? Map<*, *>
                (due?.get("m") as? Number)?.toInt()?.let { persianMonthName(it) } ?: ""
            }
        }
    }
    // 🎨 طرحِ مرجعِ کاربر (۳ مهر): ده میله‌ی کپسولی با نقطه روی خطِ پایه و نامِ ماه. با
    // قسط‌های زیاد یک **پنجره‌ی ده‌تایی** دورِ قسطِ جاری نشان داده می‌شود؛ «دیدنِ همه»
    // کلِ جدول را باز می‌کند.
    val window = 10
    val focus = allBars.indexOfFirst { it == 2 }.let { if (it < 0) allBars.lastIndex else it }
    val start = (focus - 5).coerceIn(0, (allBars.size - window).coerceAtLeast(0))
    val bars = allBars.drop(start).take(window)
    val labels = allLabels.drop(start).take(window)
    val futureColor = AppLine

    fun colorOf(state: Int) = when (state) {
        0 -> paidColor
        1 -> lateColor
        2 -> nextColor
        else -> futureColor
    }

    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ریتمِ پرداخت", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(
                    if (byYear) "هر میله یک سال" else "هر میله یک قسط",
                    color = AppMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "دیدنِ همه",
                color = AppPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.pressScaleClickable(onClick = onOpenAll),
            )
        }
        val lineColor = AppLine
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .pressScaleClickable(scale = 0.99f, onClick = onOpenAll),
        ) {
            bars.forEachIndexed { index, state ->
                val current = state == 2
                val color = colorOf(state)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        // خطِ پایه‌ی نازک از وسطِ نقطه‌ها می‌گذرد.
                        .drawBehind {
                            val y = 58.dp.toPx() + 4.dp.toPx()
                            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(0.86f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (current) nextColor.copy(alpha = 0.16f) else Color.Transparent),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .width(16.dp)
                                .height(if (state == 3) 38.dp else if (current) 46.dp else 44.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.verticalGradient(
                                        0f to color.copy(alpha = 0.35f),
                                        0.28f to color.copy(alpha = 0.55f),
                                        0.3f to color,
                                        1f to color,
                                    ),
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier.padding(top = 2.dp).size(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (current) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(AppSurface)
                                    .border(2.dp, nextColor, CircleShape),
                            )
                        } else {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
                        }
                    }
                    Text(
                        labels.getOrElse(index) { "" },
                        color = if (current) AppText else AppMuted,
                        fontSize = 8.5.sp,
                        fontWeight = if (current) FontWeight.Black else FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // جمله‌ی «هر میله یک قسط» جدا بالای راهنما نشست تا در گوشیِ باریک راهنما جا شود.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, AppLine, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(0 to "پرداخت‌شده", 2 to "قسطِ جاری", 1 to "پرداخت‌نشده", 3 to "آینده").forEach { (st, name) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(colorOf(st)))
                        Text(
                            name,
                            color = AppMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

/** نرخ بدونِ اعشارِ اضافه: ۱۸ نه ۱۸٫۰، ولی ۴٫۵ سرِ جاش می‌مونه. */
private fun fmtRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()

@Composable
private fun InstallmentRow(
    row: Map<String, Any?>,
    loan: LoanEntity,
    privacyMode: Boolean,
    onTogglePaid: (m: Int, paid: Boolean) -> Unit,
    onUnmark: (m: Int) -> Unit,
    onOpenPhoto: (m: Int) -> Unit,
    onEditAmount: (m: Int, installment: Double) -> Unit,
    modifier: Modifier = Modifier,
    dueInDays: Int? = null,
    isNext: Boolean = false,
    bulkPayMode: Boolean = false,
    selected: Boolean = false,
) {
    val m = (row["m"] as? Number)?.toInt() ?: 0
    val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
    val paid = row["paid"] == true
    val paidLate = paid && row["paidLate"] == true
    val hasPhoto = (row["photoPath"] as? String) != null
    val due = row["dueDate"] as? Map<*, *>
    val dueLabel = due?.let {
        "${toFa(it["y"].toString())}/${toFa(it["m"].toString())}/${toFa(it["d"].toString())}"
    } ?: ""
    val overdue = !paid && dueInDays != null && dueInDays < 0
    // تاریخ با نامِ ماه («۲ مرداد ۱۴۰۵») - طرحِ ChatGPT؛ شکلِ عددیِ قبلی برای منو می‌ماند.
    val dueLong = due?.let {
        val y = (it["y"] as? Number)?.toInt()
        val mo = (it["m"] as? Number)?.toInt()
        val d = (it["d"] as? Number)?.toInt()
        if (y != null && mo != null && d != null) "${toFa(d)} ${persianMonthName(mo)} ${toFa(y)}" else dueLabel
    } ?: ""

    // چهار حالتِ ردیف (طرحِ ChatGPT): پرداخت‌شده · معوق · بعدی · آتی. همه از توکن‌های
    // `AppDue*` که تمِ شب را هم می‌چرخانند - هگزِ هاردکد این‌جا ممنوع.
    val upcoming = !paid && !overdue && isNext
    val stateLabel = when {
        paidLate -> "با تأخیر"
        paid -> "پرداخت شده"
        overdue -> "معوق"
        else -> "آتی"
    }
    val stateInk = when {
        paidLate -> AppDangerInk
        paid -> AppPrimaryInk
        overdue -> AppDangerInk
        upcoming -> AppInfo
        else -> AppMuted
    }
    val rowBg = when {
        selected -> AppPrimary.pillOverSurface(0.10f)
        paid -> AppPrimaryPill.copy(alpha = 0.45f)
        overdue -> AppDueOverduePill
        upcoming -> AppDueNextPill
        else -> AppSurface
    }
    val borderColor = when {
        selected -> AppPrimary
        paid -> AppPrimaryPillBorder
        overdue -> AppDueOverdueBorder
        upcoming -> AppDueNextBorder
        else -> AppLineRow
    }
    val stateIcon = when {
        paidLate -> Icons.Filled.Check
        paid -> Icons.Filled.Check
        overdue -> Icons.Filled.PriorityHigh
        else -> Icons.Filled.Schedule
    }
    // دایره‌ی وضعیت: پرداخت‌شده سبز، معوق قرمزِ پُر، بقیه قابِ روشن.
    val iconBg = when {
        paid -> AppPrimaryPill
        overdue -> AppDanger
        upcoming -> AppInfoPill
        else -> AppChipBg
    }
    val iconTint = when {
        overdue -> Color.White
        paid -> AppPrimaryInk
        upcoming -> AppInfo
        else -> AppMuted
    }
    // قرصِ زیرِ تاریخ فقط برای معوق و «بعدی» - ردیف‌های آتیِ دور چیزی برای گفتن ندارند.
    val chip = when {
        overdue -> "${toFa(-(dueInDays ?: 0))} روز گذشته"
        upcoming && dueInDays == 0 -> "امروز"
        upcoming && dueInDays != null -> "${toFa(dueInDays)} روز مانده"
        else -> null
    }
    val rowShape = RoundedCornerShape(16.dp)
    val bulkSelectable = bulkPayMode && !paid
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = installmentRowHeight)
            .alpha(if (bulkPayMode && paid) 0.5f else 1f)
            .clip(rowShape)
            .background(rowBg, rowShape)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, rowShape)
            // تپِ ردیف = پرداخت (بی‌تغییر). منوی کارهای ردیف پشتِ شِورونِ سمتِ راست است.
            .pressScaleClickable(scale = 0.975f) { onTogglePaid(m, paid) }
            .padding(start = 2.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bulkPayMode) {
            Checkbox(checked = selected, onCheckedChange = null, enabled = bulkSelectable)
        } else {
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = "کارهای این قسط",
                        tint = if (overdue) AppDangerInk else AppMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    Text(
                        "قسط ${toFa(m)} · $dueLabel",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                    if (paid) {
                        DropdownMenuItem(
                            text = { Text("برداشتنِ پرداخت", color = AppDangerInk, fontSize = 13.sp) },
                            onClick = { menuOpen = false; onUnmark(m) },
                        )
                        DropdownMenuItem(
                            text = { Text("پیوستِ عکسِ رسید", fontSize = 13.sp) },
                            onClick = { menuOpen = false; onOpenPhoto(m) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("ویرایشِ مبلغِ این قسط", fontSize = 13.sp) },
                        onClick = { menuOpen = false; onEditAmount(m, installment) },
                    )
                }
            }
        }
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(999.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(stateIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("قسط ${toFa(m)}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                if (hasPhoto) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = "رسید دارد",
                        tint = AppMuted,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(stateLabel, color = stateInk, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Column(
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                dueLong,
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            if (chip != null) {
                Text(
                    chip,
                    color = if (overdue) AppDangerInk else AppInfo,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (overdue) AppDangerPill else AppInfoPill)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 6.dp)) {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(installment)),
                    color = if (overdue) AppDangerInk else AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Text("تومان", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// `LoanDonut` حذف شد - از وقتی `LoanSummaryCard` (حلقه‌ی ۸۸ی فریمِ `27b`) جاش رو
// گرفت، هیچ‌جا صدا زده نمی‌شد؛ توکنِ `AppAccent` هم فقط همین‌جا استفاده می‌شد.

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

/** دکمه‌ی فشرده‌ی toggle برای «عکس رسید»/«یادداشت» - نقطه‌ی کوچیکِ [filled] یعنی محتوا از قبل داره. */
@Composable
private fun AttachmentToggleButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (expanded) AppPrimary else AppMuted.copy(alpha = 0.35f)
    val bg = if (expanded) AppPrimary.pillOverSurface(0.10f) else AppSurface2
    Row(
        modifier = modifier
            .pressScaleClickable(goldBorderShape = shape, onClick = onClick)
            .background(bg, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (expanded) AppPrimary else AppMuted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            label,
            color = if (expanded) AppPrimary else AppText,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 6.dp),
        )
        if (filled) {
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(6.dp)
                    .background(AppPrimary, androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}
