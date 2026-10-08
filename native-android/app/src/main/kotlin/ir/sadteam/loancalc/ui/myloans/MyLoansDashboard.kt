package ir.sadteam.loancalc.ui.myloans

import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.IncomeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.components.TrendLineChart
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import ir.sadteam.loancalc.ui.components.LocalHeroChartStyle
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.roundToInt

@Composable
internal fun DashboardSummary(
    loans: List<LoanEntity>,
    incomes: List<IncomeEntity>,
    // جمعِ مبلغِ اقساطِ معوق (مورد ۱۹) + مجموعِ اقساطِ ماهانه‌ی واقعی (مورد ۱۴/۳۵) - هردو از بیرون
    // پاس داده می‌شن چون محاسبه‌شون suspend ئه (رجوع کن به LaunchedEffect(loans) تو MyLoansScreen)؛
    // totalMonthlyInstallment قبلاً همین‌جا از loan.installmentِ کهنه حساب می‌شد.
    totalOverdue: Double,
    overdueCount: Int,
    totalMonthlyInstallment: Double,
    onAddIncome: (label: String, amount: Double, type: IncomeType) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
) {
    // بازبینیِ ۹ مهر: «قسط × تعدادِ مانده» برای قرض‌الحسنه (قسط‌های کارمزد) و قسط‌های ویرایش‌شده
    // غلط بود؛ جمعِ واقعیِ ردیف‌های پرداخت‌نشده جایش می‌نشیند (اولش همان تخمین تا بار شود).
    val loansVm: MyLoansViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val totalRemainingDebtState = remember { mutableStateOf(loans.sumOf { it.installment * (it.n - it.paidCount) }) }
    var totalRemainingDebt by totalRemainingDebtState
    androidx.compose.runtime.LaunchedEffect(loans) {
        totalRemainingDebt = loans.sumOf { loan ->
            loansVm.getRows(loan).filter { it["paid"] != true }.sumOf { (it["installment"] as? Number)?.toDouble() ?: 0.0 }
        }
    }
    val totalIncome = remember(incomes) { incomes.sumOf { it.amount } }
    val ratio = if (totalIncome > 0) totalMonthlyInstallment / totalIncome else 0.0
    // `null` یعنی هیچ منبعِ درآمدی ثبت نشده - و آن‌جا به‌جای «۰٪»ِ گمراه‌کننده، درِ ورودی
    // نشان داده می‌شود.
    val incomeRatio: Float? = if (totalIncome > 0) ratio.toFloat() else null
    // 🚨 **آیکونِ نمودارِ هدر حذف شد** (جوابِ دورِ ۱۲): درِ ورودیِ درآمد حالا همان قرصِ
    // درصد است، یعنی همان‌جایی که عدد دیده می‌شود. آیکونِ هدر یک درِ دومِ بی‌نشانه بود.
    val showIncomeState = remember { mutableStateOf(false) }
    var showIncome by showIncomeState
    val onOpenIncome: () -> Unit = { showIncome = !showIncome }
    val statusLabel: String?
    val statusColor: Color
    when {
        totalIncome <= 0 -> {
            statusLabel = null
            statusColor = AppMuted
        }
        ratio <= 0.65 -> {
            statusLabel = "وضعیت مطلوب"
            statusColor = AppPrimary
        }
        else -> {
            statusLabel = "فشار مالی بالا"
            statusColor = AppDanger
        }
    }

    val showAddIncomeState = remember { mutableStateOf(false) }
    var showAddIncome by showAddIncomeState
    val labelState = remember { mutableStateOf("") }
    var label by labelState
    val amountTextState = remember { mutableStateOf("") }
    var amountText by amountTextState
    val typeState = remember { mutableStateOf(IncomeType.FIXED) }
    var type by typeState
    val incomePendingDeleteState = remember { mutableStateOf<IncomeEntity?>(null) }
    var incomePendingDelete by incomePendingDeleteState

    // ماهِ جاری برای برچسبِ نقطه‌ی موج. `remember` بی‌کلید کافی است - روزِ تقویم وسطِ
    // یک ترکیب عوض نمی‌شود.
    val todayForWave = remember { JalaliCalendar.today() }

    // شمارش صعودی اعداد بزرگ داشبورد (پورت animateNumber وب) - حس «پریمیوم» موقع ورود به تب.
    val animatedDebt = countUpDouble(totalRemainingDebt)
    val animatedMonthly = countUpDouble(totalMonthlyInstallment)
    val privacyMode = LocalPrivacyMode.current
    // ۱۴ مهر (ساده‌سازی): حلقه، نمودار و «تا آزادی» پشتِ «جزئیات» - چیزی حذف نشد.
    val heroDetails = true // ۱۴ مهر: کاربر نسخه‌ی کامل را قشنگ‌تر دانست - همیشه باز.

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // ⚠️ **بازطراحیِ سبکِ «جیبک»** - کارتِ خلاصه‌ی وام (کارتِ `27a`ی فایلِ طراحی).
        // قبلاً سه کارتِ سفیدِ جدا بود (بدهی/قسطِ ماهانه/معوق). طرح یه **کارتِ کاغذِ طلاییِ
        // واحد** با سه ردیفِ جداشده می‌خواد - طلایی اینجا موجهه چون این کارتِ «پول»ه، همون
        // کاربردی که قاعده‌ی «طلایی فقط پرمیوم/پول» اجازه می‌ده.
        // 🚨 **کارتِ طلایی سه لایه‌ی اطلاعاتی دارد، نه چهار ردیفِ هم‌وزن** (بندِ ۳ فریمِ `76c`
        // و خواسته‌ی دوباره‌ی کاربر در دورِ ۱۲: «اصلاً وام‌های من پیدا نیستند»).
        //
        // چهار ردیفِ تمام‌عرضِ برچسب/مقدار با سه خطِ جداکننده ~۲۲۰dp می‌خورد، یعنی کاربر
        // برای رسیدن به اولین وام باید اسکرول کند. حالا:
        //   بالا  - بجِ معوق و درصدِ پرداخت‌شده، هر دو ریز و در یک ردیف
        //   وسط  - **قسطِ این ماه** درشت (تنها عددِ قابلِ‌اقدام)
        //   پایین - ماندهٔ کل و تا آزادی، کنارِ هم و ریز
        // ترتیب **دلیل** دارد: از «چقدر عقبم» به «حالا چقدر بدهم» به «کلِ ماجرا».
        val monthsLeft = remember(loans) { loans.maxOfOrNull { it.n - it.paidCount } ?: 0 }
        val paidPct = remember(loans) {
            val totalRows = loans.sumOf { it.n }
            if (totalRows > 0) loans.sumOf { it.paidCount } * 100 / totalRows else 0
        }
        // 🎨 **کارتِ قهرمانِ مشترک، نه کارتِ طلاییِ اختصاصی** (خواسته‌ی کاربر: «مثلِ بقیه‌ی
        // تب‌ها بشود و با تمِ رنگی عوض شود»).
        //
        // کارتِ طلایی رنگِ ثابت داشت، پس تنها جای برنامه بود که با تمِ خریدنیِ کاربر
        // هم‌قدم نمی‌شد - کسی که تمِ لاجوردی خریده بود، تبِ وام را همچنان کرم می‌دید.
        // [AppHeroCard] همان چیزی است که خانه و دارایی و بودجه دارند و لحنِ سبزش از
        // خودِ تمِ فعال می‌آید.
        //
        // ⚠️ لحنِ **قرمز** فقط وقتی قسطِ معوق هست - همان تنها مصرفِ مجازش در سیستمِ طراحی.
        // خواسته‌ی کاربر (۱ مهر): مثلِ بقیه‌ی کارت‌ها رنگِ تم + برگ؛ قسطِ معوق فقط یک هاله‌ی
        // قرمزِ کناری می‌گیرد، نه کلِ کارت قرمز (همان قاعده‌ی کارتِ بودجه).
        LoansHeroCard(
            loans = loans,
            totalOverdue = totalOverdue,
            overdueCount = overdueCount,
            totalMonthlyInstallment = totalMonthlyInstallment,
            incomeRatio = incomeRatio,
            onOpenIncome = onOpenIncome,
            todayForWave = todayForWave,
            animatedDebt = animatedDebt,
            animatedMonthly = animatedMonthly,
            privacyMode = privacyMode,
            heroDetails = heroDetails,
            monthsLeft = monthsLeft,
            paidPct = paidPct,
            totalRemainingDebtState = totalRemainingDebtState,
        )

        // یه پس‌زمینه‌ی سبزِ اختصاصیِ نیمه‌شفاف اینجا امتحان شده بود، ولی رو Surface (که خودش
        // tonalElevation داره) رنگ‌ها بهم می‌ریخت و دوتُنی/کثیف به‌نظر می‌رسید. کاربر خواست دقیقاً
        // مثل بقیه‌ی کارت‌های داشبورد (DashboardStatCard بالا) باشه - پس همون پیش‌فرضِ AppCard.
        // 🚨 **بندِ ۴ فریمِ ۷۶c**: این کارت قبلاً همیشه بود، حتی وقتی هیچ منبعِ درآمدی ثبت
        // نشده بود - یعنی یک کارتِ تمام‌عرض که تنها محتوایش یک دکمه‌ی «+ افزودن» بود.
        // کارتی که هیچ‌وقت پر نیست، وزنِ محتوا می‌گیرد برای محتوایی که وجود ندارد.
        LoansIncomeCard(
            incomes = incomes,
            onAddIncome = onAddIncome,
            onDeleteIncome = onDeleteIncome,
            totalIncome = totalIncome,
            ratio = ratio,
            statusLabel = statusLabel,
            statusColor = statusColor,
            privacyMode = privacyMode,
            showIncomeState = showIncomeState,
            showAddIncomeState = showAddIncomeState,
            labelState = labelState,
            amountTextState = amountTextState,
            typeState = typeState,
            incomePendingDeleteState = incomePendingDeleteState,
        )
    }
}
