package ir.sadteam.loancalc.ui.myloans

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
import androidx.compose.material3.AlertDialog
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
        AlertDialog(
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
    // کل صفحه یه LazyColumn واحده (نه اسکرولِ تودرتوی قبلی): قبلاً لیستِ اقساط یه LazyColumn با
    // ارتفاعِ محدود داخلِ یه Columnِ اسکرول‌دار بود که با یه آستانه‌ی اسکرول بین ۵ و ۱۰ ردیف
    // باز/جمع می‌شد - تغییرِ ارتفاع وسطِ درگ محتوا رو زیرِ انگشت می‌پروند و نزدیکِ آستانه هم
    // رفت‌وبرگشتی می‌شد (گزارشِ کاربر: «یهو بزرگ می‌شه... قاطی می‌کنه»). الان اقساط آیتم‌های خودِ
    // لیستِ اصلی‌ان: هرچی پایین‌تر بری طبیعتاً کلِ صفحه رو می‌گیرن، روون و بدونِ هیچ پرش/حالتِ خاصی.
    val detailListState = rememberLazyListState()

    // ── فریمِ `27b` + تصمیمِ ساختاریِ `36i` ────────────────────────────────────────────────
    // جزئیاتِ وام فقط یه **پیش‌نمایشِ سه‌ردیفه‌ی بی‌دکمه** از اقساط نشون می‌ده؛ ثبتِ پرداخت
    // عمداً فقط تو نمای کاملِ اقساط (`29p`) ممکنه، چون تپِ اشتباه رو ردیف‌های ریزِ خلاصه راحت
    // رخ می‌ده و پس‌گرفتنِ «پرداخت شد» سخته. استثنای صریحِ خودِ طرح: قسطِ عقب‌افتاده، که یه
    // نوارِ قرمز بالای جدول میاره و **مستقیم به همون ردیف تو نمای کامل** می‌بره - نه اینکه
    // خودش پرداخت رو ثبت کنه.
    var showAllInstallments by rememberSaveable { mutableStateOf(false) }
    val today = remember { JalaliCalendar.today() }
    val overdueRow = remember(rows, today) {
        rows.firstOrNull { row ->
            if (row["paid"] == true) return@firstOrNull false
            val due = row["dueDate"] as? Map<*, *> ?: return@firstOrNull false
            val y = (due["y"] as? Number)?.toInt() ?: return@firstOrNull false
            val mo = (due["m"] as? Number)?.toInt() ?: return@firstOrNull false
            val d = (due["d"] as? Number)?.toInt() ?: return@firstOrNull false
            y < today.y || (y == today.y && (mo < today.m || (mo == today.m && d < today.d)))
        }
    }

    // اولین قسطِ پرداخت‌نشده - هم هیرو لازمش دارد هم پیش‌نمایشِ پایین، پس یک‌جا حساب می‌شود.
    val nextRow = remember(rows) { rows.firstOrNull { it["paid"] != true } }

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
            // 🚨 **نامِ بانک از جدولِ مشخصات به سرصفحه آمد** (بندِ ۲ی بخشِ ۸۰): نامِ بانک
            // **هویتِ** وام است نه یکی از مشخصاتش، پس کنارِ نامِ وام می‌نشیند نه در فهرست.
            // نامِ وام از سرصفحه به **کارتِ هویت** رفت (طرحِ مرجعِ کاربر، ۳۱ شهریور):
            // آن‌جا کنارِ نشانِ بانک و وضعیت می‌نشیند و یک‌جا می‌گوید «این کدام وام است».
            Column(modifier = Modifier.padding(start = 4.dp).weight(1f)) {
                Text("وام", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "جزئیاتِ وام و وضعیتِ پرداخت",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 1.dp),
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
        // «مانده» = جمعِ مبلغِ **ردیف‌های پرداخت‌نشده**. فرمولِ قبلی
        // (totalPaid − paidCount × loan.installment) روی فیلدِ کهنه‌ی loan.installment حساب
        // می‌کرد - همون فیلدی که خودِ این فایل چند خط بالاتر عمداً دورش می‌زنه، چون مبلغِ هر
        // قسط دستی ویرایش‌شدنی‌ست. با یه قسطِ ویرایش‌شده، عددِ هیرو با جمعِ جدول جور نمی‌شد.
        val remaining = remember(rows, loan) {
            if (rows.isEmpty()) {
                (loan.totalPaid - loan.paidCount * loan.installment).coerceAtLeast(0.0)
            } else {
                rows.filter { it["paid"] != true }
                    .sumOf { (it["installment"] as? Number)?.toDouble() ?: loan.installment }
            }
        }
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
        val overdueAmount = remember(rows, overdueCount) {
            rows.filter { it["paid"] != true }.take(overdueCount)
                .sumOf { (it["installment"] as? Number)?.toDouble() ?: loan.installment }
        }
        LoanIdentityCard(
            name = loan.name,
            bank = loan.bank,
            settled = loan.n > 0 && loan.paidCount >= loan.n,
            overdue = overdueCount > 0,
            amount = loan.amount,
            months = loan.n,
            ratePct = ratePct,
            startLabel = remember(loan) {
                val sd = viewModel.getLoanStartDate(loan)
                "${toFa(sd.y)}/${sd.m}/${sd.d}".let { _ ->
                    "${toFa(sd.y)}/${toFa(sd.m)}/${toFa(sd.d)}"
                }
            },
            privacyMode = privacyMode,
        )

        LoanHeroCard(
            remaining = remaining,
            installment = displayInstallment,
            installmentLabel = if (installmentsVary) "قسطِ بعدی" else "قسط",
            nextDueLabel = nextRow?.let { row ->
                (row["dueDate"] as? Map<*, *>)?.let {
                    val d = (it["d"] as? Number)?.toInt()
                    val mo = (it["m"] as? Number)?.toInt()
                    if (d != null && mo != null) "${toFa(d)} ${persianMonthName(mo)}" else null
                }
            },
            paidCount = loan.paidCount,
            total = loan.n,
            paidFraction = paidFraction,
            overdueCount = overdueCount,
            overdueAmount = overdueAmount,
            settled = loan.n > 0 && loan.paidCount >= loan.n,
            installmentNote = if (installmentsVary) "چون اقساطِ این وام باهم فرق دارن" else null,
            privacyMode = privacyMode,
            onPay = { showAllInstallments = true },
        )

        // سه کارتِ ریزِ حقایقِ وام (طرحِ مرجعِ کاربر). «سررسیدِ بعدی» همان تاریخِ هیروست،
        // ولی این‌جا با بجِ «N روز گذشته» وقتی از سررسید رد شده.
        LoanQuickFacts(
            total = loan.n,
            installment = displayInstallment,
            nextDueLabel = nextRow?.let { row ->
                (row["dueDate"] as? Map<*, *>)?.let {
                    val d = (it["d"] as? Number)?.toInt()
                    val mo = (it["m"] as? Number)?.toInt()
                    if (d != null && mo != null) "${toFa(d)} ${persianMonthName(mo)}" else null
                }
            },
            overdueDays = nextRow?.let { row ->
                (row["dueDate"] as? Map<*, *>)?.let { due ->
                    val y = (due["y"] as? Number)?.toInt()
                    val mo = (due["m"] as? Number)?.toInt()
                    val d = (due["d"] as? Number)?.toInt()
                    if (y != null && mo != null && d != null) {
                        val days = JalaliCalendar.daysBetween(PersianDate(y, mo, d), today)
                        days.takeIf { it > 0 }
                    } else {
                        null
                    }
                }
            },
            privacyMode = privacyMode,
        )

        // ریتمِ پرداخت - جانشینِ حلقه‌ی درصد. تنها المانِ صفحه که **ریتم** را می‌گوید نه یک لحظه.
        PaymentRhythm(rows = rows, today = today, onOpenAll = { showAllInstallments = true })

        // کارتِ «مشخصات» - فریمِ `29p`. تا حالا هیچ‌جای صفحه نرخ/تعدادِ قسط/ضامن کنارِ هم نبود.
        // تاریخِ پایان از سررسیدِ **آخرین ردیف** میاد، نه محاسبه‌ی دوباره - همون چیزی که خودِ
        // جدولِ اقساط نشون می‌ده، پس نمی‌تونه با اون ناهماهنگ باشه.
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
            // سودِ کل = جمعِ همه‌ی اقساط منهای اصلِ وام. از همان ردیف‌ها می‌آید، پس با
            // جدولِ اقساط نمی‌تواند ناهماهنگ باشد.
            totalInterest = remember(rows, loan) {
                if (rows.isEmpty()) null else {
                    val sum = rows.sumOf { (it["installment"] as? Number)?.toDouble() ?: loan.installment }
                    (sum - loan.amount).takeIf { it > 0.0 }
                }
            },
            privacyMode = privacyMode,
        )

        // کارتِ سومِ «مبلغ هر قسط / پرداخت‌شده» حذف شد: هر سه عددش از قبل تو
        // LoanSummaryCard (قسط + «۱۲ از ۳۶») و LoanSpecsCard (اسمِ بانک) هست، و فریمِ
        // `27b` بالای صفحه فقط **دو** کارت دارد. توضیحِ «چون اقساطِ این وام باهم فرق دارن»
        // به‌جاش زیرِ همون ردیفِ کارتِ خلاصه نشسته.

        // دو دکمه‌ی فشرده‌ی هم‌اندازه («عکس رسید»/«یادداشت»، نصف‌نصف) به‌جای دو باکسِ همیشه‌بازِ
        // قبلی که کلی جای صفحه رو می‌گرفتن - هرکدوم بزنیم محتواش دقیقاً همون‌جا زیرِ ردیف باز
        // می‌شه، دوباره زدنش می‌بندتش. isDirty به‌جای مقایسه با یه «آخرین مقدارِ ذخیره‌شده»ی جدا
        // نگه داشته می‌شه - چون بعدِ ذخیره، خودِ loan (پارامترِ این کامپوزیبل) با یه تاخیر از رو
        // Room/Flow آپدیت می‌شه، مقایسه‌ی مستقیم می‌تونست دکمه‌ی ذخیره رو حتی بعدِ ذخیره‌ی موفق
        // هنوز نمایان نگه داره.
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

        // هر قسط یه باکس مینیمالِ گوشه‌گرد با حاشیه‌ی سبزه (خواسته‌ی کاربر). وضعیت پرداخت:
        // به‌موقع=سبز «پرداخت شد»، با تأخیر=قرمز «با تأخیر»، پرداخت‌نشده=مشکی «پرداخت نشده».
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

        if (!showAllInstallments) {
            item {
                InstallmentsPreviewCard(
                    rows = rows,
                    loan = loan,
                    privacyMode = privacyMode,
                    overdueRow = overdueRow,
                    onSeeAll = { showAllInstallments = true },
                )
            }
            // ⚠️ کارتِ «قسطِ بعدی» (`29p`) این‌جا **حذف شد**: بخشِ ۸۰ آن را خطِ دومِ هیرو کرد.
            // دو کارت برای یک قسط یعنی همان دوازده عددِ هم‌وزنی که کلِ بازطراحی برای رفعش بود.
            // خودِ کامپوزبلش هم پاک شد؛ تنها دکمه‌ی پرداختِ صفحه حالا در هیروست.

            // کارتِ «تسویه‌ی زودتر» - فریمِ `27b`. فقط وقتی عددِ معناداری در بیاد.
            val unpaidTotal = rows.filter { it["paid"] != true }
                .sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
            // ⚠️ `remember` اینجا **نمی‌شه** - این بلوک `LazyListScope`ه نه یه @Composable؛
            // کشِ محاسبه باید داخلِ خودِ `item` بشینه.
            item {
                val saving = remember(loan, unpaidTotal) {
                    viewModel.earlySettlementSaving(loan, unpaidTotal)
                }
                if (saving != null) {
                    EarlySettlementCard(saving = saving, privacyMode = privacyMode)
                }
            }
        }

        if (showAllInstallments) {
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
                        val m = (row["m"] as? Number)?.toInt() ?: 0
                        InstallmentRow(
                            // وقتی تعدادِ اقساطِ یه وام ویرایش می‌شه، ردیف‌های اضافه/کم‌شده
                            // به‌جای پرشِ ناگهانی نرم میان و می‌رن.
                            modifier = Modifier.animateItem(),
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
                            isNext = row === rows.firstOrNull { it["paid"] != true },
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
                                    // 🚨 تپ **فقط می‌زند، برنمی‌دارد** (`64c` + تفکیکِ `46c`).
                                    // طراح در `66c` اول گفت تپ هم بردارد، بعد **پس گرفت**: شکایتِ
                                    // کاربر «پیدا نکردم» بود نه «کار نمی‌کند» - مسئله‌ی کشف است نه
                                    // مسیر. برداشتن از منویِ سه‌نقطه (ردیفِ اول) با دیالوگِ تایید.
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
                    // جا برای نوارِ چسبانِ پایین، وگرنه آخرین ردیف زیرش گم می‌شود.
                    if (bulkPayMode && selectedBulkMs.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(64.dp)) }
                    }
                }
                // **تنها جایی که جعبه‌ی اقساط دکمه‌ی چسبان می‌گیرد** (فریمِ `64b`): بی آن،
                // کاربر انتخاب می‌کند و بعد باید دنبالِ دکمه بگردد.
                if (bulkPayMode && selectedBulkMs.isNotEmpty()) {
                    val bulkSum = rows.filter { (it["m"] as? Number)?.toInt() in selectedBulkMs }
                        .sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(AppSurface, RoundedCornerShape(AppRadius.card))
                            .border(2.dp, AppLineRow, RoundedCornerShape(AppRadius.card))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text("حذف وام")
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
/**
 * **کارتِ «قسطِ بعدی» - فریمِ `29p`.**
 *
 * زمینه‌ی کرمِ `#FFF7E6` با حاشیه‌ی `#EBD9B4` و دکمه‌ی سبزِ «پرداخت شد». وقتی قسطِ **عقب‌افتاده**
 * وجود داره این کارت جاش رو به نوارِ قرمزِ `27b` می‌ده - دو تا فراخوانِ هم‌زمان گیج‌کننده‌ست و
 * اولویت با اونیه که دیرکرد داره.
 */
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
 * **کارتِ «تسویه‌ی زودتر» - فریمِ `27b`.**
 *
 * زمینه‌ی کرم با حاشیه‌ی طلایی - **همون توکن‌های کارتِ پاداشِ خانه** (`AppWarningPill` /
 * `AppGoldPillSoft` / `AppGoldInkSoft`)، نه رنگِ تازه؛ فریم برای هر دو یه پالت داده. عمداً **دکمه ندارد**: فقط
 * اطلاع‌رسانیه، چون تسویه‌ی واقعی کاری‌ست که باید با بانک انجام بشه نه تو اپ.
 *
 * لحنِ متن عمداً تخمینیه («حدودِ …») - رجوع کن به هشدارِ [MyLoansViewModel.earlySettlementSaving]:
 * مبلغِ تکِ اقساط قابلِ ویرایشِ دستیه ولی مانده‌ی اصل از فرمولِ اولیه میاد.
 */
@Composable
private fun EarlySettlementCard(saving: Double, privacyMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(AppWarningPill)
            .border(2.dp, AppGoldPillSoft, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(AppGoldPillSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Savings,
                contentDescription = null,
                tint = AppGoldInkSoft,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("تسویه‌ی زودتر", color = AppGoldInkSoft, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    "اگه الان یک‌جا بدی، حدودِ ${maskIfPrivate(masked, amountToman(saving))} تومان سود کم می‌شه",
                    color = AppGoldInkSoft,
                    fontSize = 9.5.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/**
 * **کارتِ خلاصه‌ی وام - فریمِ `27b`.**
 *
 * حلقه‌ی ۸۸ی با درصدِ پرداخت‌شده در وسط + چهار ردیفِ عدد (مانده / قسط / اقساط / سود).
 * ردیفِ «سود» فقط وقتی میاد که نرخ ثبت شده باشه - وامِ دستی‌ای که نرخ نداره یه ردیفِ
 * «۰٪»ی گمراه‌کننده نشون نمی‌ده.
 */
/**
 * **هیروِ وام** — فریمِ `80a`. جانشینِ کارتِ خلاصه و کارتِ «قسطِ بعدی».
 *
 * 🚨 **یک عددِ قهرمان، نه دوازده عددِ هم‌اندازه.** ایرادِ مرکزیِ صفحه این بود که هر دوازده
 * عدد یک وزن داشتند و چشم جایی برای فرود نداشت. جوابش رنگ و سایه‌ی بیشتر نیست: **مانده**
 * قهرمان است (۳۱sp) و **قسطِ بعدی** یک پله پایین‌تر (۱۷sp) با تاریخ و دکمه‌ی خودش.
 * **مبلغِ وام از قهرمان‌ها بیرون رفت** — عددی است که یک‌بار پرسیده می‌شود، نه هر بار.
 *
 * 🚨 **حالتِ عقب‌افتاده کارتِ جدا ندارد**، پارامترِ همین هیروست: نوارِ قرمزِ مستقل حذف شد و
 * به‌جایش کلِ کارت از گرادیانِ طلایی به سطحِ عادی با جوهرِ قرمز می‌رود. دلیلش قاعده‌ی رنگِ
 * برنامه است — طلایی زبانِ خبرِ خوب است و خبرِ بد را نباید در قابِ جشن گذاشت.
 *
 * ⚠️ دکمه طبقِ `36i` **پرداخت نمی‌کند**، به نمای کاملِ اقساط می‌برد؛ برچسبش هم همین را
 * می‌گوید. تنها جای ثبتِ پرداخت همان نمای کامل است.
 */
@Composable
private fun LoanHeroCard(
    remaining: Double,
    installment: Double,
    installmentLabel: String,
    nextDueLabel: String?,
    paidCount: Int,
    total: Int,
    paidFraction: Float,
    overdueCount: Int,
    overdueAmount: Double,
    settled: Boolean,
    installmentNote: String?,
    privacyMode: Boolean,
    onPay: () -> Unit,
) {
    val overdue = overdueCount > 0
    val ink = if (overdue) AppDangerInk else AppGoldInk
    val muted = ink.copy(alpha = 0.7f)
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        variant = if (overdue || settled) AppCardVariant.DEFAULT else AppCardVariant.GOLD,
    ) {
        if (settled) {
            // 🚨 مدال **جای عددِ قهرمان** را می‌گیرد، چون مانده صفر است و صفر قهرمان نمی‌شود.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(34.dp),
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text("تسویه شد", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        "${toFa(total)} قسط · ${toFa(total - overdueCount)} به‌وقت",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        } else {
            // 🚨 **بج، نه یک خطِ متن** (طرحِ مرجعِ کاربر، ۳۱ شهریور): «۱۰ قسط عقب‌افتاده»
            // مهم‌ترین خبرِ این صفحه است و کنارِ برچسبِ «مانده» گم می‌شد.
            if (overdue) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppDangerPill)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Filled.PriorityHigh,
                        contentDescription = null,
                        tint = AppDanger,
                        modifier = Modifier.size(11.dp),
                    )
                    Text(
                        "${toFa(overdueCount)} قسط عقب‌افتاده",
                        color = AppDangerInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            // عدد و حلقه کنارِ هم: حلقه درصد را در یک نگاه می‌دهد، عدد مبلغ را.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = if (overdue) 9.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("مانده", color = muted, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, amountToman(remaining)) + " تومان",
                            color = ink,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                PaidRing(
                    fraction = paidFraction,
                    ringColor = if (overdue) AppDanger else AppPrimary,
                    trackColor = AppSurface2,
                    centerTop = "${toFa((paidFraction * 100).roundToInt())}٪",
                    centerBottom = "پرداخت‌شده",
                    centerTopColor = if (overdue) AppDanger else AppPrimary,
                    centerBottomColor = AppMuted,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (overdue) {
                            "جمعِ معوق"
                        } else if (nextDueLabel != null) {
                            "$installmentLabel · $nextDueLabel"
                        } else {
                            installmentLabel
                        },
                        color = muted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, amountToman(if (overdue) overdueAmount else installment)) + " تومان",
                            color = ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                GradientButton(onClick = onPay) {
                    Text(
                        if (overdue) "ببین و پرداخت کن" else "پرداخت شد",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            if (installmentNote != null) {
                Text(
                    installmentNote,
                    color = muted,
                    fontSize = 8.5.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        // نوارِ درصد تهِ هیرو - همان ۱۷٪ که قبلاً حلقه بود. در حالتِ عقب‌افتاده **دو تکه**
        // می‌شود: سبزِ پرداخت‌شده و قرمزِ معوق.
        val overdueFraction = if (total > 0) (overdueCount.toFloat() / total).coerceIn(0f, 1f) else 0f
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (overdue || settled) AppSurface2 else AppGoldInk.copy(alpha = 0.18f)),
        ) {
            if (paidFraction > 0f) {
                Box(modifier = Modifier.weight(paidFraction).fillMaxHeight().background(AppPrimary))
            }
            if (overdueFraction > 0f) {
                Box(modifier = Modifier.weight(overdueFraction).fillMaxHeight().background(AppDanger))
            }
            val rest = (1f - paidFraction - overdueFraction).coerceAtLeast(0.001f)
            Box(modifier = Modifier.weight(rest).fillMaxHeight())
        }
        Text(
            "${toFa(paidCount)} از ${toFa(total)} · ${toFa((paidFraction * 100).roundToInt())}٪",
            color = if (overdue || settled) AppMuted else muted,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

/**
 * حلقه‌ی «٪ پرداخت‌شده» کنارِ مبلغِ مانده (طرحِ مرجعِ کاربر).
 *
 * ⚠️ این همان درصدِ نوارِ تهِ کارت است، ولی **جایگزینش نمی‌شود**: نوار نسبتِ سه‌تکه‌ی
 * پرداخت‌شده/معوق/مانده را می‌دهد و حلقه فقط یک عددِ درشت. کاربر در یک نگاه عدد را
 * می‌خواند و در نگاهِ دوم تفکیک را.
 */
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
    privacyMode: Boolean,
) {
    val statusLabel = when {
        settled -> "تسویه‌شده"
        overdue -> "معوق"
        else -> "فعال"
    }
    val statusColor = when {
        settled -> AppPrimary
        overdue -> AppDanger
        else -> AppPrimary
    }
    AppHeroCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        tone = if (overdue) HeroTone.RED else HeroTone.GREEN,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // دایره‌ی نشانِ بانک - همان عنصرِ گردِ گوشه‌ی طرح.
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
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
                        fontSize = 10.sp,
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LoanIdentityStat(
                label = "مبلغِ وام",
                value = maskIfPrivate(privacyMode, amountToman(amount)),
                unit = "تومان",
                modifier = Modifier.weight(1.2f),
            )
            LoanIdentityStat(label = "مدتِ کل", value = toFa(months), unit = "ماه", modifier = Modifier.weight(1f))
            LoanIdentityStat(label = "نرخِ سود", value = "${fmtRate(ratePct).faDigits()}٪", unit = "سالانه", modifier = Modifier.weight(1f))
            LoanIdentityStat(label = "تاریخِ شروع", value = startLabel, unit = "", modifier = Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun LoanIdentityStat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = HeroMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            value,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(unit, color = HeroMuted, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/**
 * سه کارتِ ریزِ زیرِ کارتِ اصلی: تعدادِ کلِ اقساط · مبلغِ هر قسط · سررسیدِ بعدی.
 *
 * ⚠️ هیچ‌کدام عددِ تازه‌ای نیستند - همه از قبل در صفحه بودند، ولی پخش یا پشتِ دکمه‌ی
 * «بیشتر». کنارِ هم گذاشتنشان همان سه سوالی است که کاربر پشتِ‌هم می‌پرسد.
 */
@Composable
private fun LoanQuickFacts(
    total: Int,
    installment: Double,
    nextDueLabel: String?,
    overdueDays: Int?,
    privacyMode: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LoanFactCard(
            icon = Icons.Filled.EventNote,
            tint = AppPrimaryInk,
            bg = AppPrimaryPill,
            label = "تعدادِ کلِ اقساط",
            value = toFa(total),
            unit = "قسط",
            modifier = Modifier.weight(1f),
        )
        LoanFactCard(
            icon = Icons.Filled.Payments,
            tint = AppInfo,
            bg = AppIconFrame,
            label = "مبلغِ هر قسط",
            value = maskIfPrivate(privacyMode, amountToman(installment)),
            unit = "تومان",
            modifier = Modifier.weight(1f),
        )
        LoanFactCard(
            icon = Icons.Filled.CalendarMonth,
            tint = AppPurple,
            bg = AppIconFrame,
            label = "سررسیدِ بعدی",
            value = nextDueLabel ?: "—",
            unit = overdueDays?.let { "${toFa(it)} روز گذشته" } ?: "",
            unitIsWarning = overdueDays != null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LoanFactCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    unitIsWarning: Boolean = false,
) {
    AppCard(contentPadding = 9.dp, modifier = modifier) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(999.dp)).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        }
        Text(
            label,
            color = AppMuted,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            value,
            color = AppText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(
                unit,
                color = if (unitIsWarning) AppDangerInk else AppLabel,
                fontSize = 8.sp,
                fontWeight = if (unitIsWarning) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
            )
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
    val bars = if (!byYear) states else states.chunked(12).map { chunk -> chunk.minOrNull() ?: 3 }

    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ریتمِ پرداخت", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "دیدنِ همه",
                color = AppPrimary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.pressScaleClickable(onClick = onOpenAll),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(26.dp)
                .pressScaleClickable(onClick = onOpenAll),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { state ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        // قسطِ نیامده میله‌ی **کوتاه** می‌گیرد نه رنگِ کم‌رنگ: ارتفاع در یک
                        // نگاه از رنگ سریع‌تر خوانده می‌شود.
                        .fillMaxHeight(if (state == 3) 0.45f else 1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when (state) {
                                0 -> paidColor
                                1 -> lateColor
                                2 -> nextColor
                                else -> emptyColor
                            },
                        ),
                )
            }
        }
        // 🚨 **برچسبِ ماه زیرِ میله‌ها** (طرحِ مرجعِ کاربر): بی آن معلوم نبود میله‌ی قرمز
        // مالِ کدام ماه است. برچسب فقط وقتی می‌آید که میله‌ها کم باشند؛ با ۳۶ قسط،
        // ۳۶ نامِ ماه روی هم می‌افتد، پس یکی‌درمیان نوشته می‌شود.
        val monthLabels = remember(rows, byYear) {
            if (byYear) emptyList() else rows.map { row ->
                val due = row["dueDate"] as? Map<*, *>
                (due?.get("m") as? Number)?.toInt()?.let { persianMonthName(it) } ?: ""
            }
        }
        if (monthLabels.isNotEmpty() && monthLabels.size <= 24) {
            val everyOther = monthLabels.size > 12
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            ) {
                monthLabels.forEachIndexed { index, name ->
                    Text(
                        if (everyOther && index % 2 == 1) "" else name,
                        color = AppMuted,
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Text(
            if (byYear) "هر میله یک سال" else "هر میله یک قسط",
            color = AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}

/** نرخ بدونِ اعشارِ اضافه: ۱۸ نه ۱۸٫۰، ولی ۴٫۵ سرِ جاش می‌مونه. */
private fun fmtRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()

/**
 * **پیش‌نمایشِ جدولِ اقساط - فریمِ `27b`.**
 *
 * سه ردیف: آخرین قسطِ پرداخت‌شده، قسطِ بعدی (برجسته)، و قسطِ بعد از اون (کم‌رنگ). هیچ دکمه‌ی
 * پرداختی نداره - تصمیمِ صریحِ `36i`. تنها راهِ رفتن به پرداخت، «دیدنِ همه‌ی N قسط»ه که طبقِ
 * همون بند **همیشه** دیده می‌شه، حتی وقتی جدول خالیه.
 */
@Composable
private fun InstallmentsPreviewCard(
    rows: List<Map<String, Any?>>,
    loan: LoanEntity,
    privacyMode: Boolean,
    overdueRow: Map<String, Any?>?,
    onSeeAll: () -> Unit,
) {
    val muted = AppMuted
    val text = AppText
    val danger = AppDanger

    // انتخابِ سه ردیف: آخرین پرداخت‌شده، اولین پرداخت‌نشده، و بعدیش.
    val nextIndex = rows.indexOfFirst { it["paid"] != true }
    val preview = remember(rows, nextIndex) {
        when {
            rows.isEmpty() -> emptyList()
            nextIndex < 0 -> rows.takeLast(3)
            else -> listOfNotNull(
                rows.getOrNull(nextIndex - 1),
                rows.getOrNull(nextIndex),
                rows.getOrNull(nextIndex + 1),
            )
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        // نوارِ قرمزِ قسطِ عقب‌افتاده - استثنای بندِ ۳۶i.
        if (overdueRow != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppDangerPill)
                    .border(2.dp, AppDanger, RoundedCornerShape(16.dp))
                    .pressScaleClickable(onClick = onSeeAll)
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = danger, modifier = Modifier.size(18.dp))
                Text(
                    "قسطِ ${toFa((overdueRow["m"] as? Number)?.toInt() ?: 0)} عقب افتاده",
                    color = danger,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Text("پرداختِ قسطِ عقب‌افتاده", color = danger, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("جدولِ اقساط", color = text, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(10.dp))
            preview.forEachIndexed { index, row ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppLineRow),
                    )
                }
                PreviewInstallmentRow(row, loan, privacyMode, isNext = row === rows.getOrNull(nextIndex))
            }
            Text(
                if (rows.isEmpty()) "دیدنِ اقساط" else "دیدنِ همه‌ی ${toFa(rows.size)} قسط",
                color = AppPrimaryInk,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScaleClickable(onClick = onSeeAll)
                    .padding(top = 8.dp),
            )
        }
    }
}

/** یه ردیفِ پیش‌نمایش - سه حالتِ فریم: پرداخت‌شده (تیکِ سبز) · بعدی (نقطه‌ی قرمز) · آینده (کم‌رنگ). */
@Composable
private fun PreviewInstallmentRow(
    row: Map<String, Any?>,
    loan: LoanEntity,
    privacyMode: Boolean,
    isNext: Boolean,
) {
    val paid = row["paid"] == true
    val m = (row["m"] as? Number)?.toInt() ?: 0
    val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
    val due = row["dueDate"] as? Map<*, *>
    val dueLabel = due?.let {
        "${toFa(it["d"].toString())} ${persianMonthName((it["m"] as? Number)?.toInt() ?: 1)}"
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (!paid && !isNext) 0.6f else 1f)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (paid) AppPrimaryPill else if (isNext) AppDangerPill else AppSurface2),
            contentAlignment = Alignment.Center,
        ) {
            when {
                paid -> Icon(Icons.Filled.Check, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
                isNext -> Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppDanger))
                else -> Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, AppLine, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("قسطِ ${toFa(m)}", color = AppText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            Text(
                dueLabel,
                color = if (isNext) AppDanger else AppMuted,
                fontSize = 8.5.sp,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
            )
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, amountToman(installment)),
                color = if (paid) AppMuted else AppText,
                fontSize = 10.5.sp,
                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
            )
        }
    }
}

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

    // 🚨 بجِ «پرداخت نشده» حذف شد (فریمِ `64a`): در فهرستی که بیشترش پرداخت‌نشده است، آن بج
    // روی ده ردیف تکرار می‌شود و خبری نمی‌دهد. جایش **مهلت** نشسته - همان زبانِ `27b`.
    // ردیفِ آینده‌ی دور عمداً هیچ برچسبی نمی‌گیرد.
    val statusLabel = when {
        paidLate -> "با تأخیر"
        paid -> "پرداخت شد"
        overdue -> "${toFa(-(dueInDays ?: 0))} روز گذشته"
        dueInDays == 0 -> "امروز"
        dueInDays != null && dueInDays in 1..7 -> "${toFa(dueInDays)} روز مانده"
        isNext -> "بعدی"
        else -> null
    }
    // «پرداخت شد» **سبز** است (خواسته‌ی کاربر، ۲۵ شهریور) - تنها خبرِ خوبِ این ستون، و
    // خاکستری‌بودنش آن را هم‌سطحِ «هیچ» نشان می‌داد.
    // ⚠️ فقط **همین برچسب** سبز می‌شود؛ مبلغ و تاریخِ ردیفِ پرداخت‌شده خاکستری می‌مانند
    // (`inkColor`) وگرنه فهرستِ بیشتر-پرداخت‌شده یک دیوارِ سبز می‌شود.
    // «با تأخیر» عمداً سبز **نمی‌شود** - پرداخت شده ولی سرِ وقت نه، و همین تفاوت تنها
    // چیزی است که آن دو حالت را از هم جدا می‌کند.
    // 🚨 فریمِ `66a`: بجِ وضعیت **قرص** شد. `64a` متنِ لخت گذاشته بود و استدلالش این بود
    // که در فهرستِ یک‌ستونه قرص فقط شلوغی است - ولی چهار وضعیتِ متفاوت در یک ستون بی قاب
    // از هم جدا نمی‌شوند، و «پرداخت شد»ِ لخت هم‌سطحِ نبودِ برچسب دیده می‌شد.
    //
    // ⚠️ هر سه مقدار **توکن** است نه هگز: جدولِ شبِ `65b` همین‌ها را می‌چرخاند. هگزِ هاردکد
    // این‌جا همان دامی است که در تبِ دارایی چهار رنگِ نچرخنده ساخت.
    val statusInk = when {
        paidLate -> AppDangerInk
        paid -> AppPrimaryInk
        overdue || dueInDays == 0 -> AppDangerInk
        isNext || (dueInDays != null && dueInDays in 1..7) -> AppGoldInk
        else -> AppLabel
    }
    val statusBg = when {
        paidLate -> AppDangerPill
        paid -> AppPrimaryPill
        overdue || dueInDays == 0 -> AppDangerPill
        isNext || (dueInDays != null && dueInDays in 1..7) -> AppGoldPillSoft
        else -> AppChipBg
    }
    // حاشیه‌ی قرص **فقط** برای دو حالتِ فوری. `AppGoldBorder` از قبل در این فایل استفاده
    // شده؛ اگر `AppDangerBorder` تعریف نشده، `AppDanger.copy(alpha = 0.35f)` بگذارید.
    val statusBorder = when {
        overdue || dueInDays == 0 -> AppDangerBorder
        !paid && isNext -> AppGoldBorder
        else -> Color.Transparent
    }
    val rowShape = RoundedCornerShape(14.dp)
    // 🚨 حاشیه‌ی سبزِ همه‌ی ردیف‌ها رفت (فریمِ `64a`): وقتی هر ردیف حاشیه‌ی سبز دارد، حاشیه
    // هیچ چیزی نمی‌گوید. حالا فقط **دو حالتِ فوری** حاشیه‌ی رنگی می‌گیرند - قاعده‌ی `51a`:
    // رنگ از فوریت می‌آید.
    val bulkSelectable = bulkPayMode && !paid
    val borderColor = when {
        selected -> AppPrimary
        overdue -> AppDanger
        !paid && isNext -> AppGoldBorder
        else -> AppLineRow
    }
    // متنِ ردیفِ پرداخت‌شده خاکستری می‌شود ولی ردیف **محو نمی‌شود** - تفکیکِ پرداخت‌شده کارِ
    // جوهر است نه alpha؛ محوکردنِ کلِ ردیف مبلغ و تاریخ را هم ناخوانا می‌کند. تنها استثنا
    // حالتِ گروهی است که آن ردیف واقعاً بی‌کار است.
    val inkColor = if (paid) AppMuted else AppText
    var menuOpen by remember { mutableStateOf(false) }

    // 🚨 فریمِ `66b`: کشفِ لمس‌پذیری. `64a` پالس را برداشت و استدلالش («ده ردیفِ هم‌زمان
    // نفس‌کشنده نویز است») هنوز درست است - ولی فشردگیِ تنها کافی نبود، چون **قبل از تپ**
    // دیده نمی‌شود.
    //
    // راهِ سوم انتخاب شد: ردیف **شکلِ دکمه** می‌گیرد، همان زبانِ `GradientButton` - یک
    // سایه‌ی ۱dp که لبه‌ی پایین را جدا می‌کند. ساکن است، پس نویزِ حرکت ندارد، و روی
    // **همه‌ی** ردیف‌های لمس‌پذیر هست نه فقط یکی.
    //
    // ردیفِ پرداخت‌شده سایه **نمی‌گیرد** و تو-رفته می‌مانَد (`AppChipBg`): تپش کاری می‌کند
    // ولی کارِ برگشتی است نه پیش‌رونده، و ظاهرِ متفاوت همان را می‌گوید.
    val rowElevation = if (paid) 0.dp else 1.dp
    val rowBg = when {
        selected -> AppPrimary.pillOverSurface(0.10f)
        paid -> AppChipBg
        else -> AppSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(installmentRowHeight)
            .alpha(if (bulkPayMode && paid) 0.5f else 1f)
            .shadow(rowElevation, rowShape, clip = false)
            .background(rowBg, rowShape)
            .border(if (selected || overdue) 1.5.dp else 1.dp, borderColor, rowShape)
            // پرتپ‌ترین المانِ کلِ اپ - فشرده می‌شه و یه tick هپتیک می‌ده. کشفِ لمس‌پذیری کارِ
            // همین فشردگیه، نه پالسِ بج (که طبقِ `64a` برداشته شد: ده ردیفِ هم‌زمان
            // نفس‌کشنده نویز است نه راهنما).
            .pressScaleClickable(scale = 0.975f) { onTogglePaid(m, paid) }
            .padding(start = 12.dp, end = if (bulkPayMode) 12.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bulkPayMode) {
            Checkbox(checked = selected, onCheckedChange = null, enabled = bulkSelectable)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("قسط ${toFa(m)}", color = inkColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                // گیره‌ی ریز یعنی «رسید دارد» - قبلاً فقط از رنگِ آیکونِ دوربین فهمیده می‌شد
                // که هم نامرئی بود هم با حذفِ آن آیکون از دست می‌رفت.
                if (hasPhoto) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = "رسید دارد",
                        tint = AppMuted,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(dueLabel, color = AppMuted, fontSize = 12.sp)
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text("${maskIfPrivate(masked, amountToman(installment))} تومان", color = inkColor, fontSize = 13.sp)
        }
        if (statusLabel != null) {
            Text(
                statusLabel,
                color = statusInk,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusBg)
                    .border(1.dp, statusBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        if (!bulkPayMode) {
            // 🚨 دو آیکونِ لختِ ۲۴پیکسلی (دوربین و مداد) هر دو زیرِ هدفِ لمسیِ ۴۴ بودند و کنارِ
            // بجِ لمس‌پذیر می‌نشستند - سه هدفِ چسبیده. یک سه‌نقطه‌ی ۴۴ جای هر دو (فریمِ `64c`).
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "کارهای این قسط", tint = AppMuted)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    // منو شماره‌ی قسط را در سرش می‌گوید - در فهرستِ دوازده‌ردیفی کاربر باید
                    // بداند منو مالِ کدام ردیف است.
                    Text(
                        "قسط ${toFa(m)} · $dueLabel",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                    // «برداشتنِ پرداخت» **اولِ منو** آمد: کاربر گزارش داد پیدایش نکرده، و در
                    // منوی سه‌آیتمی آخرین ردیف کمترین شانس را دارد. حالا دو راه به یک کار
                    // می‌رسد (تپ و منو) و هر دو دیالوگِ تایید می‌گیرند.
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
