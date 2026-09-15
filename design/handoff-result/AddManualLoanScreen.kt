package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.SuccessCheckmarkOverlay
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت فرم افزودن وام دستی (view-manual تو www/index.html؛ saveManualLoan برای اعتبارسنجی/ذخیره).
 * وقتی [editingLoan] پاس داده بشه، همین فرم برای ویرایشِ مشخصاتِ کلیِ یه وامِ دستیِ ازقبل‌ذخیره‌شده
 * (اسم/بانک/مبلغِ قسط/تعدادِ کل/تاریخ) استفاده می‌شه به‌جای ساختنِ یه وامِ جدید - خواسته‌ی کاربر بعد
 * از اینکه یه‌بار به‌جای ۱۰ قسط اشتباهی ۱۱ تا ثبت کرد و راهی برای اصلاحش نبود. فیلدِ «تعداد پرداخت‌شده»
 * تو حالتِ ویرایش نشون داده نمی‌شه چون منبعِ حقیقتِ وضعیتِ پرداختِ هر قسط از این به بعد خودِ
 * تک‌تکِ ردیف‌هاست (قابلِ تغییر تو LoanDetailScreen)، نه این فیلدِ خلاصه.
 */
@Composable
fun AddManualLoanScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    editingLoan: LoanEntity? = null,
    viewModel: MyLoansViewModel = hiltViewModel(),
) {
    val initialStartDate = remember(editingLoan) {
        // پیش‌فرضِ وامِ تازه **امروز**ه. عددِ ثابتِ ۱۴۰۴/۱/۱ با گذشتِ سال کهنه می‌شد و چون
        // مبنای جدولِ اقساط است، وامِ تازه سررسیدهای گذشته می‌گرفت.
        editingLoan?.let { viewModel.getLoanStartDate(it) } ?: viewModel.todayJalali()
    }
    var name by remember(editingLoan) { mutableStateOf(editingLoan?.name ?: "") }
    var bank by remember(editingLoan) { mutableStateOf(editingLoan?.bank ?: "") }
    // فیلد **تومان**ه (بندِ ۲ی README): ذخیره ریال، نمایش و ورودی تومان. پس مقدارِ
    // پیش‌پرکرده‌ی حالتِ ویرایش هم باید از ریالِ دیتابیس به تومان برگرده.
    var installmentText by remember(editingLoan) {
        mutableStateOf(
            editingLoan?.installment?.let { rialToToman(it.toLong()).toString() } ?: "",
        )
    }
    var totalCountText by remember(editingLoan) { mutableStateOf(editingLoan?.n?.toString() ?: "") }
    var paidCountText by remember { mutableStateOf("") }
    var startYear by remember(editingLoan) { mutableIntStateOf(initialStartDate.y) }
    var startMonth by remember(editingLoan) { mutableIntStateOf(initialStartDate.m) }
    var startDay by remember(editingLoan) { mutableIntStateOf(initialStartDate.d) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    // موقعِ ذخیره (خصوصاً وقتی لاگین باشیم و پوشِ شبکه‌ای به سرور طول بکشه) دکمه هیچ نشونه‌ای نداشت -
    // کاربر چندبار زد و ۶-۷ تا وامِ تکراری ساخته شد. الان دکمه موقعِ saving غیرفعال می‌شه و اسپینر
    // نشون می‌ده، همون الگوی LoginScreen.
    var saving by remember { mutableStateOf(false) }
    // بعدِ ذخیره‌ی موفق، به‌جای بستنِ فوریِ صفحه، یه تیکِ سبزِ متحرک نشون داده می‌شه - رجوع کن به
    // SuccessCheckmark.kt. onSaved واقعی همون‌جا (بعدِ یه تاخیرِ کوتاه) صدا زده می‌شه.
    var savedOk by remember { mutableStateOf(false) }

    // 🚨 قبلاً این بلوک با `return` کلِ فرم را از کامپوزیشن برمی‌داشت - چهارمین موردِ همان
    // باگی که در `AssetTradeSheet` و `AccountDetailScreen` رفع شد. نتیجه: اسکرولِ فرم صفر
    // می‌شد و کاربر بعدِ انتخابِ تاریخ خودش را بالای صفحه پیدا می‌کرد.
    //
    // حالا شیت **روی** فرم می‌آید، پس فرم زنده می‌مانَد. تقویمِ تمام‌صفحه است، پس
    // `ModalBottomSheet` تا سقفِ ارتفاع باز می‌شود.
    val calendarSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showCalendarPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCalendarPicker = false },
            sheetState = calendarSheet,
        ) {
            CalendarPickerScreen(
                initialDate = PersianDate(startYear, startMonth, startDay),
                onDateSelected = { d ->
                    startYear = d.y
                    startMonth = d.m
                    startDay = d.d
                    showCalendarPicker = false
                },
                onBack = { showCalendarPicker = false },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                if (editingLoan != null) "ویرایش وام" else "افزودن وام دستی",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        // فریمِ `67c` بندِ ۴: در حالتِ ویرایش، سقفِ مجازِ «تعدادِ کل» **بالای فرم** گفته
        // می‌شود نه به‌عنوانِ خطای بعدِ ذخیره. گاردش از قبل بود، ولی کاربر فقط وقتی
        // می‌دیدش که اشتباه کرده باشد - و آن‌وقت هم نمی‌دانست عددِ مجاز چند است.
        if (editingLoan != null && editingLoan.paidCount > 0) {
            item {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppGoldPillSoft)
                        .padding(13.dp),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AppGoldInk,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        "${toFa(editingLoan.paidCount)} قسط از این وام پرداخت شده. تعدادِ کل نمی‌تواند کمتر از ${toFa(editingLoan.paidCount)} باشد.",
                        color = AppGoldInk,
                        fontSize = 10.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // فریمِ `67a`: کارتِ خلاصه‌ی زنده.
        //
        // فرم پنج عدد می‌پرسید و **هیچ‌وقت نمی‌گفت جمعشان چقدر است**؛ کاربری که یک‌بار ۱۱
        // قسط به‌جای ۱۰ ثبت کرد، تا لحظه‌ی ذخیره راهی برای دیدنِ نتیجه نداشت.
        //
        // بندِ ۲ی `67c`: تا وقتی قسط و تعداد نیامده کارت **نمی‌آید** (نه این‌که «—» نشان
        // بدهد) - کارتی که خالی باشد یک ردیفِ مُرده است.
        item {
            val instToman = cleanNum(installmentText).toLongOrNull() ?: 0L
            val n = totalCountText.toIntOrNull() ?: 0
            if (instToman > 0 && n > 0) {
                AppHeroCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("کلِ بازپرداخت", color = AppHeroLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                            Text(
                                fmt((instToman * n).toDouble()).faDigits(),
                                color = AppHeroInk,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                "تومان",
                                color = AppHeroLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 5.dp, bottom = 3.dp),
                            )
                        }
                        // آخرین سررسید = سررسیدِ اول + (n−1) ماه. وامِ دستی تنفس ندارد
                        // (`graceMonths` همیشه ۰)، پس این محاسبه بی‌قید درست است.
                        val lastMonthIndex = (startMonth - 1) + (n - 1)
                        val lastY = startYear + lastMonthIndex / 12
                        val lastM = lastMonthIndex % 12 + 1
                        Text(
                            "${toFa(n)} قسط · آخرین سررسید ${toFa(startDay)} ${persianMonthName(lastM)} ${toFa(lastY)}",
                            color = AppHeroLabel,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            } else {
                Text(
                    "مبلغِ قسط و تعداد را بزن تا جمعش را ببینی.",
                    color = AppLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // فریمِ `67c` بندِ ۳: فیلدها سه گروه‌اند (شناسه · عدد · وضعیت) با سه کارت، نه شش
        // کارتِ تک‌فیلدی - شش کارت یعنی شش سرصفحه و پنج فاصله‌ی بزرگ برای فرمی که در یک
        // صفحه جا می‌شد.
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FieldLabel("اسمِ وام")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                    )
                    FieldLabel("بانک یا فروشنده")
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                    )
                }
            }
        }
        item {
            // ⚠️ اسمِ لیبل عوض شد به «تاریخ سررسید اولین قسط» - وامِ دستی هیچ‌وقت دوره‌ی تنفس نداره
            // (رجوع کن به addManualLoan تو LoanRepository، graceMonths همیشه ۰ ئه)، پس این تاریخ
            // همیشه دقیقاً سررسیدِ قسطِ اوله - برخلافِ فرمِ وامِ بانکی که تنفس هم داره و اسمِ فیلدش
            // عوض نشده، فقط یه توضیحِ دینامیک زیرش اضافه شده.
            AppCard(label = "تاریخ سررسید اولین قسط") {
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
                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                )
                // ورودی از اول تومانه، پس معادلِ حروفی مستقیم از همین عدد میاد - تقسیمِ
                // دستیِ «/ ۱۰» رفت؛ تنها مرجعِ تبدیل tomanToRial/rialToToman ئه.
                val instToman = cleanNum(installmentText).toLongOrNull() ?: 0L
                if (instToman > 0) {
                    Text(
                        "${numberToWordsFa(instToman.toDouble())} تومان",
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
        if (editingLoan == null) {
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
        }
        if (error != null) {
            item {
                Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    enabled = !saving,
                    onClick = {
                        // نبودِ این گارد باعث می‌شد اگه پوشِ شبکه‌ایِ بعدِ ذخیره (syncIfLoggedIn) کند
                        // بود، دکمه بی‌حرکت به‌نظر برسه، کاربر چندبار بزنه، و هر تپ یه وامِ کاملاً
                        // جدید بسازه (باگِ گزارش‌شده: ۶-۷ تا وامِ تکراری از یه ذخیره).
                        if (saving) return@GradientButton
                        // ورودی تومانه و ذخیره ریال - تبدیل فقط همین یک نقطه.
                        val installmentToman = cleanNum(installmentText).toLongOrNull() ?: 0L
                        val installment = tomanToRial(installmentToman).toDouble()
                        val n = totalCountText.toIntOrNull() ?: 0
                        val paidCount = paidCountText.toIntOrNull() ?: 0

                        error = when {
                            name.trim().isEmpty() -> "اسم وام رو وارد کن"
                            bank.trim().isEmpty() -> "اسم بانک یا فروشنده رو وارد کن"
                            installment <= 0 -> "مبلغ قسط رو وارد کن"
                            n <= 0 -> "تعداد کل اقساط رو وارد کن"
                            editingLoan == null && paidCount > n -> "تعداد پرداخت‌شده نمی‌تونه از کل اقساط بیشتر باشه"
                            // باگِ رفع‌شده (مورد ۲۳): این گارد قبلاً فقط مسیرِ ساختنِ وامِ جدید رو
                            // چک می‌کرد، نه مسیرِ ویرایش - کاربر می‌تونست n رو کمتر از تعدادِ
                            // اقساطِ ازقبل‌پرداخت‌شده بذاره، که تشخیصِ «تسویه‌شده»/داشبورد رو خراب
                            // می‌کرد.
                            editingLoan != null && n < editingLoan.paidCount ->
                                "تعداد کل اقساط نمی‌تونه از تعدادِ اقساطِ ازقبل‌پرداخت‌شده (${toFa(editingLoan.paidCount)}) کمتر باشه"
                            else -> null
                        }
                        if (error == null) {
                            saving = true
                            val startDate = PersianDate(startYear, startMonth, startDay)
                            if (editingLoan != null) {
                                viewModel.updateManualLoan(
                                    loan = editingLoan,
                                    name = name.trim(),
                                    bank = bank.trim(),
                                    installment = installment,
                                    n = n,
                                    startDate = startDate,
                                    onSaved = { savedOk = true },
                                )
                            } else {
                                viewModel.saveManualLoan(
                                    name = name.trim(),
                                    bank = bank.trim(),
                                    installment = installment,
                                    n = n,
                                    paidCount = paidCount,
                                    startDate = startDate,
                                    onSaved = { savedOk = true },
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (saving) {
                        LottieSpinner(modifier = Modifier.size(18.dp))
                    } else {
                        Text(if (editingLoan != null) "ذخیره تغییرات" else "ذخیره وام")
                    }
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth(), enabled = !saving) {
                    Text("انصراف")
                }
            }
        }
    }
    }
    SuccessCheckmarkOverlay(visible = savedOk, onFinished = onSaved)
}

/**
 * برچسبِ فیلد.
 *
 * فریمِ `67c` بندِ ۳: با یکی‌شدنِ سه کارت، `AppCard(label = …)` دیگر کافی نیست - یک کارت
 * چند فیلد دارد و هر فیلد برچسبِ خودش را می‌خواهد.
 */
@Composable
private fun FieldLabel(text: String) {
    Text(text, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
}

/**
 * جوهرِ روی هیرو.
 *
 * ⚠️ این دو **هگزِ هاردکد نیستند به آن معنا که نگرانش بودیم**: گرادیانِ `AppHeroCard` در
 * هر دو تم سبزِ برند است (نه توکنِ چرخنده)، پس سفید روی آن در تمِ تیره هم درست می‌مانَد -
 * همان کاری که `AccountsTotalHero` می‌کند.
 *
 * اگر `AppHeroCard` توکنِ جوهرِ خودش را دارد (چیزی مثلِ `AppHeroInk`)، این دو را حذف کنید
 * و همان را بگذارید. من امضای `AppHeroCard` را ندیدم.
 */
private val AppHeroInk = Color.White
private val AppHeroLabel = Color(0xFFBFEBD5)
