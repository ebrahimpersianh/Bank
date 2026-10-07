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
    var totalRemainingDebt by remember { mutableStateOf(loans.sumOf { it.installment * (it.n - it.paidCount) }) }
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
    var showIncome by remember { mutableStateOf(false) }
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

    var showAddIncome by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(IncomeType.FIXED) }
    var incomePendingDelete by remember { mutableStateOf<IncomeEntity?>(null) }

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
        AppHeroCard {
            // 🚨 **حلقه جای نوارِ تخت** (طرحِ مرجعِ کاربر، ۳۱ شهریور): نوار درصد را
            // بی‌عدد می‌گفت و «چند قسط مانده» هیچ‌جای این کارت نبود. حلقه هر دو را
            // می‌دهد و ارتفاعِ تازه‌ای هم نمی‌گیرد چون کنارِ عددِ قهرمان می‌نشیند.
            // خواسته‌ی کاربر (۱ مهر): کارت کوتاه‌تر، هم‌قدِ بقیه. برچسبِ «قسطِ معوق» دیگر ردیفِ
            // جدا نمی‌گیرد؛ کنارِ «قسطِ این ماه» می‌نشیند.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "قسطِ این ماه",
                        color = HeroMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                    if (overdueCount > 0) {
                        Text(
                            "${toFa(overdueCount)} قسطِ معوق",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        "${maskIfPrivate(masked, animatedMonthly.rialToFaCompact())} تومان", // ۱۶ مهر: فشرده، هم‌قالبِ بقیه‌ی صفحه‌ها
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
                val rowsLeft = remember(loans) { loans.sumOf { it.n - it.paidCount }.coerceAtLeast(0) }
                val rowsAll = remember(loans) { loans.sumOf { it.n } }
                // 🚨 **حلقه ضخیم‌تر شد و «٪ پرداخت‌شده» زیرش نشست** (طرحِ مرجعِ کاربر).
                // پیش از این حلقه فقط «چند قسط مانده» را می‌گفت و درصد هیچ‌جای کارت نبود؛
                // آن دو یک جفت‌اند - «چقدر مانده» بی «چقدر رفته» نصفِ خبر است.
                if (heroDetails) Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    PaidRing(
                        fraction = paidPct / 100f,
                        ringColor = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        centerTop = "${toFa(paidPct)}٪",
                        centerBottom = "${toFa(rowsAll - rowsLeft)} از ${toFa(rowsAll)}",
                        centerTopColor = Color.White,
                        centerBottomColor = HeroMuted,
                        // کوچک‌تر (۷۸→۶۶) تا کارتِ وام هم‌قدِ بقیه‌ی کارت‌های قهرمان شود.
                        // ۱۴ مهر: بزرگ‌تر تا «از ۴۹۸ قسط» کامل دیده شود (بریده می‌شد).
                        size = 64.dp,
                        stroke = 7.dp,
                        centerTopSize = 15,
                    )
                    // ۱۶ مهر: «۱۸۴ پرداخت شده» حالا داخلِ حلقه است («۱۸۴ از ۴۹۸»)؛ قرصِ جدا برای کوتاه‌ترشدنِ کارت رفت.
                    if (false) Row(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp),
                        )
                        Text(
                            "${toFa(rowsAll - rowsLeft)} پرداخت شده",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                    }
                }
            }
            // 🚨 **نسبتِ قسط به درآمد آمد داخلِ کارت** (بندِ ۱ جوابِ دورِ ۱۲).
            //
            // این عدد کسرِ همین «قسطِ این ماه» بر درآمد است، پس جایش زیرِ همان عدد است نه
            // در تهِ یک کارتِ جمع‌شونده - و وقتی از ۱۰۰ بگذرد یعنی قسط از درآمد بیشتر
            // است، که مهم‌ترین خبرِ کلِ صفحه است.
            //
            // ⚠️ در عوض «٪ پرداخت‌شده» از کارت رفت: **یک کارت، یک عددِ درصددار**. دو
            // درصد کنارِ هم روی یک کارت، هر دو را بی‌معنی می‌کند (کدام مالِ کدام؟).
            // خودِ پیشرفت با نوارِ زیر گفته می‌شود، بی عدد.
            if (incomeRatio != null) {
                val over = incomeRatio > 1f
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = if (over) 0.3f else 0.16f))
                        .pressScaleClickable(onClick = onOpenIncome)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        if (over) "اقساطت از درآمدِ ثبت‌شده‌ات بیشتر است · ${toFa((incomeRatio * 100).roundToInt())}٪"
                        else "${toFa((incomeRatio * 100).roundToInt())}٪ از درآمدت صرفِ اقساط می‌شه",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            } else {
                // بی منبعِ درآمد، درصدی وجود ندارد - پس به‌جای عددِ دروغ، درِ ورودی.
                // ۱۰ مهر: قرصِ پُر + «+» تا معلوم باشد دکمه است (متنِ کم‌رنگ دیده نمی‌شد).
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
                        .pressScaleClickable(onClick = onOpenIncome)
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("ثبتِ درآمدِ ماهانه", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            // 🌊 **موجِ ماندهٔ بدهی** - خواسته‌ی کاربر با طرحِ مرجع: «آن خطِ پایینِ رقم».
            // شش نقطه = ماندهٔ بدهی در شش ماهِ پیشِ رو، و نقطه‌ی برجسته ماهِ جاری است.
            // پس شیبِ خط دقیقاً همان چیزی را می‌گوید که کاربر می‌خواهد بداند: دارد کم می‌شود.
            val debtCurve = remember(loans, totalMonthlyInstallment, totalRemainingDebt) {
                val monthly = totalMonthlyInstallment
                (0 until 6).map { month ->
                    (totalRemainingDebt - monthly * month).coerceAtLeast(0.0).toFloat()
                }
            }
            // ۱۴ مهر: نمودارِ مانده به خواسته‌ی کاربر برداشته شد («اضافی است»).
            if (false && debtCurve.any { it > 0f } && totalMonthlyInstallment > 0) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    // لمس‌پذیر، همان نمودارِ مشترکِ دارایی. جهت مثلِ بقیه‌ی نمودارهای برنامه
                    // (خواسته‌ی کاربر): زمان چپ‌به‌راست - ماهِ جاری چپ، ماه‌های آینده به راست.
                    val waveMonths = (0 until debtCurve.size).map { ahead ->
                        persianMonthName(((todayForWave.m - 1 + ahead) % 12) + 1)
                    }
                    val waveValue: (Double) -> String = { value -> if (privacyMode) "•••" else "مانده ${value.rialToFaCompact()} تومان" }
                    // سبکِ خریده‌شده از فروشگاه روی این موج هم می‌نشیند؛ بی خرید، همان خطِ کم‌رنگِ قبلی.
                    if (LocalHeroChartStyle.current?.itemId != null) {
                        HeroChart(
                            values = debtCurve.map { it.toDouble() },
                            labels = waveMonths,
                            valueLabel = waveValue,
                            currentIndex = 0,
                            natural = HeroChartStyle.LINE,
                            height = 22.dp,
                        )
                    } else {
                        TrendLineChart(
                            values = debtCurve.map { it.toDouble() },
                            lineColor = Color.White.copy(alpha = 0.6f),
                            fillTop = Color.White.copy(alpha = 0.16f),
                            dotColor = Color.White,
                            height = 22.dp,
                            labels = waveMonths,
                            // ماهِ جاری (اولِ فهرست) برجسته می‌ماند، نه ششمین ماهِ آینده.
                            restIndex = 0,
                            valueLabel = waveValue,
                        )
                    }
                    Text(
                        persianMonthName(todayForWave.m),
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(AbsoluteAlignment.TopLeft)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            // ⚠️ نوارِ تختِ پیشرفت **حذف شد**: همان درصد حالا در حلقه است و دو گرافیک
            // برای یک عدد، همان چیزی است که این صفحه یک‌بار از آن پاک شد.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column {
                    Text(
                        "ماندهٔ کل",
                        color = HeroMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, animatedDebt.rialToFaCompact()),
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                if (heroDetails && monthsLeft > 0) {
                    Column {
                        Text(
                            "تا آزادی",
                            color = HeroMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${toFa(monthsLeft)} ماه",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                // مبلغِ معوق فقط وقتی واقعاً هست - وگرنه ستونِ صفرِ همیشگی.
                if (totalOverdue > 0) {
                    Column {
                        Text(
                            "معوق",
                            color = AppDangerInk.copy(alpha = 0.75f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                        )
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                maskIfPrivate(masked, totalOverdue.rialToFaCompact()),
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 1.dp),
                            )
                        }
                    }
                }
            }
        }

        // یه پس‌زمینه‌ی سبزِ اختصاصیِ نیمه‌شفاف اینجا امتحان شده بود، ولی رو Surface (که خودش
        // tonalElevation داره) رنگ‌ها بهم می‌ریخت و دوتُنی/کثیف به‌نظر می‌رسید. کاربر خواست دقیقاً
        // مثل بقیه‌ی کارت‌های داشبورد (DashboardStatCard بالا) باشه - پس همون پیش‌فرضِ AppCard.
        // 🚨 **بندِ ۴ فریمِ ۷۶c**: این کارت قبلاً همیشه بود، حتی وقتی هیچ منبعِ درآمدی ثبت
        // نشده بود - یعنی یک کارتِ تمام‌عرض که تنها محتوایش یک دکمه‌ی «+ افزودن» بود.
        // کارتی که هیچ‌وقت پر نیست، وزنِ محتوا می‌گیرد برای محتوایی که وجود ندارد.
        if (showIncome) AppCard(label = "تحلیل درآمد") {
            // تصویرِ سه‌بعدی (۸ مهر، ChatGPT).
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.jibak_income_chart),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(96.dp).padding(bottom = 8.dp),
            )
            if (incomes.isNotEmpty()) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    incomes.forEach { income ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(income.label, color = AppText, fontSize = 13.sp)
                                Text(
                                    if (income.type == IncomeType.FIXED.name) "ثابت" else "متغیر",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text(
                                        "${maskIfPrivate(masked, amountToman(income.amount))} تومان",
                                        color = AppMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 6.dp),
                                    )
                                }
                                IconButton(onClick = { incomePendingDelete = income }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف منبع درآمد", tint = AppDanger)
                                }
                            }
                        }
                    }
                    incomePendingDelete?.let { income ->
                        ConfirmDeleteDialog(
                            title = "حذف منبع درآمد",
                            text = "منبعِ درآمدِ «${income.label}» حذف بشه؟",
                            onConfirm = { onDeleteIncome(income) },
                            onDismiss = { incomePendingDelete = null },
                        )
                    }
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "جمع درآمد: ${maskIfPrivate(masked, amountToman(totalIncome))} تومان",
                            color = AppText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (showAddIncome) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم منبع درآمد (مثلاً حقوق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    // مبلغ با جداکننده‌ی هزارگان نشون داده می‌شه و زیرش معادل حروفی (مثل «مبلغ وام»).
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = cleanNum(it) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        label = { Text("مبلغ ماهانه (تومان)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    // ورودی تومان است، پس معادلِ حروفی هم مستقیم از همین عدد - تقسیمِ دستیِ
                    // «/ ۱۰» رفت؛ تنها مرجعِ تبدیل tomanToRial/rialToToman است.
                    val incomeToman = amountText.toLongOrNull() ?: 0L
                    if (incomeToman > 0) {
                        AutoShrinkText(
                            text = "${numberToWordsFa(incomeToman.toDouble())} تومان",
                            color = AppMuted,
                            maxFontSize = 11.sp,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppChip(label = "ثابت", selected = type == IncomeType.FIXED, onClick = { type = IncomeType.FIXED })
                        AppChip(label = "متغیر", selected = type == IncomeType.VARIABLE, onClick = { type = IncomeType.VARIABLE })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                if (label.trim().isNotEmpty() && amount > 0) {
                                    // ذخیره ریال است، ورودی تومان.
                                    onAddIncome(label.trim(), tomanToRial(amount.toLong()).toDouble(), type)
                                    label = ""
                                    amountText = ""
                                    showAddIncome = false
                                }
                            },
                            enabled = label.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("افزودن")
                        }
                        OutlinedButton(onClick = { showAddIncome = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                OutlinedButton(onClick = { showAddIncome = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ افزودن منبع درآمد")
                }
            }

            if (totalIncome > 0) {
                // گیجِ «سلامتِ مالی»: درصدِ درآمدی که صرفِ اقساط می‌شه، به‌شکل یه حلقه‌ی انیمیشنی -
                // تو حالتِ فشارِ بالا (ratio > 0.65، همون آستانه‌ی statusColor بالا) کلِ حلقه قرمز
                // می‌شه، وگرنه همون گرادیانِ سبزآبی→طلاییِ استانداردِ ProgressRing.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    ProgressRing(
                        progress = ratio.toFloat(),
                        size = 64.dp,
                        strokeWidth = 7.dp,
                        colors = if (ratio > 0.65) listOf(AppDanger, AppDanger) else listOf(AppPrimaryDim, AppPrimary, AppAccent),
                    ) {
                        Text(
                            "${toFa((ratio * 100).roundToInt())}٪",
                            color = AppText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "از درآمدت صرف اقساط می‌شه",
                            color = AppMuted,
                            fontSize = 12.sp,
                        )
                        if (statusLabel != null) {
                            Text(
                                statusLabel,
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
