package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppTxOut
import ir.sadteam.loancalc.ui.theme.AppTxIn
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import kotlinx.coroutines.launch
import ir.sadteam.loancalc.ui.components.GradientButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.expenseCategories
import ir.sadteam.loancalc.data.incomeCategories
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AccountBadge
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/** آبیِ تبِ «دخل» - خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع: خرج قرمز، دخل آبی، جابجایی سبز. عمداً
 * اینجا محلی تعریف شده و به پالتِ سراسری اضافه نشده، چون تنها جای مصرفش همین سه‌تاگله. */
private val IncomeBlue = Color(0xFF4C7DF0)

/** سه حالتِ شیتِ «تراکنش جدید». */
enum class NewTxKind { EXPENSE, INCOME, TRANSFER }

/** رنگِ هر نوعِ تراکنش - قرص‌های `17a` رنگِ **همه‌ی** نوع‌ها رو لازم دارن نه فقط فعال. */
@Composable
private fun accentOf(kind: NewTxKind): Color = when (kind) {
    NewTxKind.EXPENSE -> AppDanger
    NewTxKind.INCOME -> IncomeBlue
    // بنفش (خواسته‌ی کاربر، ۳ مهر): درآمد آبی و خرج قرمز است؛ انتقال نباید با درآمد یکی دیده شود.
    NewTxKind.TRANSFER -> AppPurple
}

/**
 * شیتِ «تراکنش جدید» - نقطه‌ی واحدِ ثبتِ دخل/خرج/جابجایی تو کلِ اپ (خانه، گزارش، دارایی…).
 * خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ مرجع (پولکی): سه تبِ بالا با رنگ‌های متفاوت، بعد مبلغ،
 * حساب‌کتاب، تاریخ، توضیحات، دسته‌بندی + سه دسته‌ی پیشنهادی.
 *
 * **جابجایی schema جدید نمی‌خواد**: یه انتقال دقیقاً یعنی یه «برداشت» از مبدا و یه «واریز» به
 * مقصد. هر دو تراکنش با `sourceType = "transfer"` و یه `sourceId`ِ مشترک ثبت می‌شن تا بعداً
 * بشه جفتشون رو به‌هم ربط داد (مثلاً برای حذفِ هم‌زمان) بدونِ اینکه جدولِ جدیدی لازم باشه.
 *
 * ⚠️ تو حالتِ جابجایی عمداً **دسته‌بندی گرفته نمی‌شه** - پول از جیبی به جیبِ دیگه رفته، نه خرج
 * شده؛ اگه دسته می‌گرفت، تو گزارشِ خرج دوباره‌حسابی می‌شد.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NewTransactionSheet(
    onDismiss: () -> Unit,
    initialKind: NewTxKind = NewTxKind.EXPENSE,
    accountViewModel: AccountViewModel = hiltViewModel(),
    extrasViewModel: ir.sadteam.loancalc.ui.extras.ExtrasViewModel = hiltViewModel(),
) {
    val accounts by accountViewModel.accounts.collectAsState()
    val templates by extrasViewModel.templates.collectAsState()
    // قابلیت‌های برگرفته از مقایسه با پارمیس/پولکس (۶ مهر): برچسب، بازپرداخت، رسید، تقسیم، الگو.
    var tagsText by remember { mutableStateOf("") }
    var reimbursable by remember { mutableStateOf(false) }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var splitMode by remember { mutableStateOf(false) }
    val splits = remember { androidx.compose.runtime.mutableStateListOf<Pair<String?, String>>() }
    var showSaveTemplate by remember { mutableStateOf(false) }
    var deletingTemplate by remember { mutableStateOf<ir.sadteam.loancalc.data.db.TxTemplateEntity?>(null) }
    deletingTemplate?.let { t ->
        ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog(
            title = "حذفِ الگو",
            text = "الگوی «${t.name}» پاک شود؟ تراکنش‌هایی که قبلاً با آن ثبت کردی دست نمی‌خورند.",
            onConfirm = { extrasViewModel.deleteTemplate(t); deletingTemplate = null },
            onDismiss = { deletingTemplate = null },
        )
    }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val pickReceipt = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri -> if (uri != null) scope.launch { receiptPath = extrasViewModel.importReceipt(uri) } }

    var kind by remember { mutableStateOf(initialKind) }
    var amountText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(JalaliCalendar.today()) }
    var description by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf<Long?>(null) }
    var fromAccountId by remember { mutableStateOf<Long?>(null) }
    var toAccountId by remember { mutableStateOf<Long?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf<AccountSlot?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // عوض‌کردنِ تب دسته‌بندیِ انتخاب‌شده رو باطل می‌کنه - دسته‌ی خرج تو دخل بی‌معنیه.
    val accent = accentOf(kind)
    val categories: List<CategoryEntry> = if (kind == NewTxKind.INCOME) incomeCategories else expenseCategories

    // ⚠️ سه لایه‌ی داخلی، و بازگشتِ سیستمی به هیچ‌کدام وصل نبود: دکمه‌ی back کلِ شیتِ
    // نیمه‌پرشده را می‌بست. ترتیب از بازترین لایه.
    BackHandler {
        when {
            showCalendar -> showCalendar = false
            picking != null -> picking = null
            showCategoryPicker -> showCategoryPicker = false
            else -> onDismiss()
        }
    }

    if (picking != null) {
        AccountPickerSheet(
            accounts = accounts,
            onPick = { picked ->
                when (picking) {
                    AccountSlot.MAIN -> accountId = picked.id
                    AccountSlot.FROM -> fromAccountId = picked.id
                    AccountSlot.TO -> toAccountId = picked.id
                    null -> Unit
                }
                picking = null
            },
            onDismiss = { picking = null },
        )
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            categories = categories,
            isIncome = kind == NewTxKind.INCOME,
            onPick = { category = it; showCategoryPicker = false },
            onDismiss = { showCategoryPicker = false },
        )
    }

    // ⚠️ تقویم قبلاً با `return` صدا زده می‌شد و کلِ Column (به‌همراهِ rememberScrollStateِ
    // داخلِ مدیفایرش) از کامپوزیشن بیرون می‌رفت: کاربر مبلغ و حساب و توضیح را پر می‌کرد،
    // تاریخ را انتخاب می‌کرد، و فرم از سرِ صفحه برمی‌گشت. ششمین جای این الگو در برنامه.
    // 🚨 پس‌زمینه‌ی **مات** لازم است: بی این، صفحه‌ی زیرین از پشتِ فرم پیدا بود و متن‌ها
    // روی هم می‌افتادند (کاربر با اسکرین‌شات گزارش کرد - «ثبتِ اولین خرج» و «خوش آمدی»
    // روی فرم دیده می‌شدند). `clickable`ِ بی‌جلوه هم تپ‌های عبوری به صفحه‌ی زیر را می‌گیرد.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            )
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // هدرِ فریمِ `17a`: قابِ ۳۲یِ بستن · عنوانِ **پویا** (خرجِ تازه / دخلِ تازه / جابجایی) ·
        // قابِ ۳۲یِ هم‌رنگِ نوعِ تراکنش. عنوان دیگه ثابتِ «تراکنش جدید» نیست چون خودِ فریم
        // نوعِ فعال رو تو عنوان می‌گه - یه تاییدِ بصریِ رایگان که چی داری ثبت می‌کنی.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppSurface)
                    .border(1.5.dp, AppLine, RoundedCornerShape(10.dp))
                    .pressScaleClickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted, modifier = Modifier.size(17.dp))
            }
            Text(
                when (kind) {
                    NewTxKind.EXPENSE -> "خرجِ تازه"
                    NewTxKind.INCOME -> "دخلِ تازه"
                    NewTxKind.TRANSFER -> "جابجایی"
                },
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when (kind) {
                        NewTxKind.EXPENSE -> Icons.Filled.ArrowUpward
                        NewTxKind.INCOME -> Icons.Filled.ArrowDownward
                        NewTxKind.TRANSFER -> Icons.Filled.SwapHoriz
                    },
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(17.dp),
                )
            }
        }

        // سه **قرصِ مستقل** به‌جای تاگلِ لغزنده - فریمِ `17a` هر سه رو هم‌عرض و جدا نشون می‌ده
        // و فقط فعال زمینه/حاشیه‌ی رنگی می‌گیره. حاشیه‌ی رنگی همون‌جایی‌ست که تاگلِ لغزنده
        // نمی‌تونست بده (اون فقط پس‌زمینه‌ی نشانگر رو رنگ می‌کرد).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NewTxKind.entries.forEach { entry ->
                val entryAccent = accentOf(entry)
                val selected = entry == kind
                val shape = RoundedCornerShape(999.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (selected) entryAccent.copy(alpha = 0.14f) else AppSurface2)
                        .then(
                            if (selected) Modifier.border(1.5.dp, entryAccent, shape) else Modifier,
                        )
                        .pressScaleClickable {
                            kind = entry
                            category = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when (entry) {
                            NewTxKind.EXPENSE -> "خرج"
                            NewTxKind.INCOME -> "درآمد"
                            NewTxKind.TRANSFER -> "انتقال"
                        },
                        color = if (selected) entryAccent else AppMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        // ── کارتِ مبلغ - فریمِ `17a` ────────────────────────────────────────────────────
        // فریم یه کارتِ **وسط‌چینِ بدونِ کادرِ ورودی** می‌خواد: برچسبِ ریزِ «مبلغ · ریال»، عددِ
        // ۳۴یِ درشت، و زیرش حروفیِ همون عدد. کادرِ `OutlinedTextField` عمداً حذف شد (فریم
        // هیچ کادری دورِ عدد نداره) ولی خودِ فیلد سرِ جاشه - فقط شفاف و وسط‌چین شده، پس
        // تایپ/کرسر/صفحه‌کلیدِ عددی همون‌طور کار می‌کنن.
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ⚠️ برچسب «ریال» بود و کپشنِ زیرش «تومان» - یعنی کاربر در یک کارت دو واحد
                // می‌دید و باید حدس می‌زد عددی که تایپ می‌کند کدام است. این تنها جای
                // باقی‌مانده‌ی برنامه بود که ورودی ریالی می‌گرفت. ستونِ دیتابیس ریال می‌ماند؛
                // `tomanToRial` در لبه‌ی ثبت تبدیل می‌کند.
                Text("مبلغ · تومان", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                BasicTextField(
                    value = amountText,
                    // خطا با اولین اصلاح پاک می‌شود، نه با ثبتِ بعدی: پیامِ «مبلغ رو وارد کن»
                    // زیرِ فیلدی که دارد پر می‌شود، نویز است.
                    onValueChange = { amountText = cleanNum(it); error = null },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = accent,
                        letterSpacing = (-1).sp,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (amountText.isEmpty()) {
                                Text(
                                    "۰",
                                    color = AppMuted.copy(alpha = 0.5f),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            inner()
                        }
                    },
                )
                val toman = amountText.toLongOrNull() ?: 0L
                if (toman > 0) {
                    Text(
                        // دیگر تقسیم بر ده لازم نیست - خودِ فیلد تومان است.
                        "${numberToWordsFa(toman.toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        if (kind == NewTxKind.TRANSFER) {
            // مبدا ← مقصد (به‌جای یه حساب‌کتابِ تکی). چیدمانِ RTL: مبدا سمت راست، فلش وسط.
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AccountSlotTile(
                        label = "مبدا",
                        account = accounts.firstOrNull { it.id == fromAccountId },
                        onClick = { picking = AccountSlot.FROM },
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.padding(horizontal = 8.dp).size(20.dp),
                    )
                    AccountSlotTile(
                        label = "مقصد",
                        account = accounts.firstOrNull { it.id == toAccountId },
                        onClick = { picking = AccountSlot.TO },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            AppCard {
                SheetRow(
                    // ⚠️ `Category` آیکونِ **دسته‌بندی** است و ردیفِ حساب هم همان را داشت،
                    // پس دو ردیفِ متفاوتِ همین شیت آیکونِ یکسان می‌گرفتند.
                    icon = Icons.Filled.AccountBalanceWallet,
                    text = accounts.firstOrNull { it.id == accountId }?.name ?: "حساب‌کتاب",
                    filled = accountId != null,
                    onClick = { picking = AccountSlot.MAIN },
                )
            }
        }

        AppCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                Text(
                    dateLabel(date),
                    color = AppText,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .pressScaleClickable(onClick = { showCalendar = true }),
                )
                IconButton(onClick = { date = PersianCalendar.addDays(date, -1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "روز قبل", tint = AppMuted)
                }
                IconButton(onClick = { date = PersianCalendar.addDays(date, 1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "روز بعد", tint = AppMuted)
                }
            }
        }

        AppCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notes, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    singleLine = true,
                    placeholder = { Text("توضیحات", color = AppMuted, fontSize = 13.sp) },
                )
            }
        }

        if (kind != NewTxKind.TRANSFER) {
            val kindType = if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT.name else TransactionType.WITHDRAWAL.name
            val mine = templates.filter { it.type == kindType }
            if (mine.isNotEmpty()) {
                // الگوها: یک تپ همه‌ی فیلدها را پر می‌کند؛ نگه‌داشتن = حذفِ الگو.
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    mine.forEach { t ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(AppSurface)
                                .border(1.dp, AppLine, RoundedCornerShape(999.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (t.amount > 0) amountText = rialToToman(t.amount.toLong()).toString()
                                        category = t.category
                                        if (t.accountId != null && accounts.any { it.id == t.accountId }) accountId = t.accountId
                                        description = t.name
                                    },
                                    onLongClick = { deletingTemplate = t },
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(t.name, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (kind != NewTxKind.TRANSFER) {
            AppCard {
                SheetRow(
                    icon = Icons.Filled.Category,
                    text = category ?: "دسته‌بندی",
                    filled = category != null,
                    onClick = { showCategoryPicker = true },
                )
                // سه دسته‌ی پیشنهادی - میان‌برِ سریع بدونِ بازکردنِ لیستِ کامل (طبقِ اپِ مرجع).
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.take(3).forEach { entry ->
                        SuggestedCategoryChip(
                            entry = entry,
                            selected = category == entry.name,
                            onClick = { category = entry.name },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (kind != NewTxKind.TRANSFER) {
            AppCard {
                // تقسیمِ یک خرید بینِ چند دسته - هر ردیف یک تراکنشِ جدا با شناسه‌ی مشترک.
                if (kind == NewTxKind.EXPENSE) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("تقسیم بینِ چند دسته", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        ir.sadteam.loancalc.ui.settings.AppSwitch(checked = splitMode, onCheckedChange = {
                            splitMode = it
                            if (it && splits.isEmpty()) { splits.add(null to ""); splits.add(null to "") }
                        })
                    }
                    if (splitMode) {
                        // فریمِ `41`ِ ChatGPT: خلاصه‌ی «جمع / باقی‌مانده» بالای ردیف‌ها + نوارِ پیشرفت،
                        // هر ردیف = دسته (آیکون و رنگِ خودش) + مبلغ + حذف.
                        val totalToman = amountText.toLongOrNull() ?: 0L
                        val splitSum = splits.sumOf { it.second.toLongOrNull() ?: 0L }
                        val remaining = totalToman - splitSum
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppChipBg)
                                .padding(12.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("جمعِ ردیف‌ها", color = AppMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("باقی‌مانده برای تقسیم", color = AppMuted, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                Text(
                                    splitSum.toFaMoney() + " تومان",
                                    color = AppText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    if (totalToman == 0L) "—" else remaining.toFaMoney() + " تومان",
                                    color = when {
                                        totalToman == 0L -> AppMuted
                                        remaining == 0L -> AppTxIn
                                        remaining < 0 -> AppTxOut
                                        else -> accent
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            if (totalToman > 0) {
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { (splitSum.toFloat() / totalToman).coerceIn(0f, 1f) },
                                    color = if (remaining < 0) AppTxOut else AppTxIn,
                                    trackColor = AppLine,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                )
                            } else {
                                Text("مبلغِ کل را بالا بنویس تا باقی‌مانده حساب شود.", color = AppMuted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                        splits.forEachIndexed { i, (cat, amt) ->
                            val entry = categories.firstOrNull { it.name == cat }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Box(modifier = Modifier.weight(1.15f)) {
                                    var open by remember { mutableStateOf(false) }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, if (cat == null) accent.copy(alpha = 0.6f) else AppLine, RoundedCornerShape(12.dp))
                                            .clickable { open = true }
                                            .padding(horizontal = 10.dp, vertical = 14.dp),
                                    ) {
                                        if (entry != null) {
                                            Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(18.dp).padding(end = 2.dp))
                                        }
                                        Text(
                                            cat ?: "انتخابِ دسته",
                                            color = if (cat == null) accent else AppText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                                        )
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                                    }
                                    androidx.compose.material3.DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                        categories.forEach { c ->
                                            androidx.compose.material3.DropdownMenuItem(
                                                leadingIcon = { Icon(c.icon, contentDescription = null, tint = c.color) },
                                                text = { Text(c.name) },
                                                onClick = { splits[i] = c.name to amt; open = false },
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = amt,
                                    onValueChange = { v -> splits[i] = cat to cleanNum(v).take(13) },
                                    placeholder = { Text("تومان", fontSize = 11.sp) },
                                    visualTransformation = ThousandsSeparatorTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppTxOut.copy(alpha = 0.14f))
                                        .clickable { splits.removeAt(i); if (splits.isEmpty()) splitMode = false },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذفِ ردیف", tint = AppTxOut, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Text(
                            "+ ردیفِ دیگر",
                            color = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { splits.add(null to "") }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it.take(80) },
                    placeholder = { Text("برچسب (مثلاً سفرِ شمال، عروسی) - با «،» جدا کن", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                if (kind == NewTxKind.EXPENSE) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("بازپرداخت می‌شود (خرجِ کاری و…)", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        ir.sadteam.loancalc.ui.settings.AppSwitch(checked = reimbursable, onCheckedChange = { reimbursable = it })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        if (receiptPath == null) "📎 افزودنِ عکسِ رسید" else "✓ رسید پیوست شد",
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).clickable { pickReceipt.launch("image/*") }.padding(vertical = 10.dp),
                    )
                }
            }
        }

        if (showSaveTemplate) {
            var tName by remember { mutableStateOf(description.ifBlank { category ?: "" }) }
            ir.sadteam.loancalc.ui.components.JibakAlertDialog(
                onDismissRequest = { showSaveTemplate = false },
                title = { Text("ذخیره به‌عنوانِ الگو") },
                text = {
                    OutlinedTextField(value = tName, onValueChange = { tName = it.take(30) }, singleLine = true, placeholder = { Text("مثلاً نون، بنزین") })
                },
                confirmButton = {
                    GradientButton(onClick = {
                        if (tName.isNotBlank()) {
                            extrasViewModel.saveTemplate(
                                tName,
                                if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT.name else TransactionType.WITHDRAWAL.name,
                                tomanToRial(amountText.toLongOrNull() ?: 0L).toDouble(),
                                category,
                                accountId,
                            )
                        }
                        showSaveTemplate = false
                    }) { Text("ذخیره") }
                },
                dismissButton = { androidx.compose.material3.TextButton(onClick = { showSaveTemplate = false }) { Text("انصراف") } },
            )
        }

        if (error != null) {
            Text(error ?: "", color = AppDanger, fontSize = 12.sp)
        }

        // ── دو دکمه‌ی پایین - فریمِ `17a` ───────────────────────────────────────────────
        // «ثبت · ۱۰ سکه» جایزه‌ی سکه رو **روی خودِ دکمه** می‌گه (ابتکارِ خودِ فریم: پاداش رو
        // قبل از عمل نشون بده نه بعدش)، و «+ باز» ثبت می‌کنه ولی شیت رو باز نگه می‌داره -
        // برای وقتی چند تراکنشِ پشتِ‌هم وارد می‌کنی و هر بار بازکردنِ دوباره‌ی شیت اذیت‌کننده‌ست.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
        // ⚠️ منطقِ ثبت قبلاً **دو بار** نوشته شده بود، یک‌بار در هر دکمه (سی خطِ یکسان).
        // یک اصلاح در یکی به دیگری نمی‌رسید. حالا یک تابعِ محلی، و هر دکمه فقط می‌گوید بعدش
        // چه کند.
        val submit: (andClose: Boolean) -> Unit = { andClose ->
            // فیلد تومان است، ستونِ دیتابیس ریال - تبدیل فقط همین‌جا، در لبه.
            val splitRows = if (splitMode && kind == NewTxKind.EXPENSE) {
                splits.mapNotNull { (c, a) -> a.toLongOrNull()?.takeIf { it > 0 }?.let { c to tomanToRial(it).toDouble() } }
            } else emptyList()
            val toman = amountText.toLongOrNull() ?: 0L
            val amount = if (splitRows.isNotEmpty()) splitRows.sumOf { it.second } else tomanToRial(toman).toDouble()
            error = validate(kind, amount, accountId, fromAccountId, toAccountId)
            val tags = tagsText.split('،', ',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",").ifBlank { null }
            if (error == null) {
                val onSaved = {
                    if (andClose) {
                        onDismiss()
                    } else {
                        // فقط مبلغ و توضیح پاک می‌شوند؛ حساب/تاریخ/دسته می‌مانند چون معمولاً
                        // تراکنش‌های پشتِ‌هم همان حساب و همان روزند.
                        amountText = ""
                        description = ""
                        receiptPath = null
                        splits.clear()
                        splitMode = false
                    }
                }
                // ثبتِ مالی نباید با یک exceptionِ پس‌زمینه‌ای برنامه را ببندد. تا وقتی DAO
                // موفق نشده، فرم باز می‌ماند؛ در خطا هم کاربر همان‌جا پیام می‌بیند و اطلاعاتش
                // را از دست نمی‌دهد.
                val onSaveFailure: (Throwable) -> Unit = {
                    error = "ثبت تراکنش ناموفق بود؛ دوباره تلاش کن."
                }
                if (kind == NewTxKind.TRANSFER) {
                    accountViewModel.addTransfer(
                        fromAccountId = fromAccountId!!,
                        toAccountId = toAccountId!!,
                        amount = amount,
                        description = description.trim(),
                        year = date.y,
                        month = date.m,
                        day = date.d,
                        onSuccess = onSaved,
                        onFailure = onSaveFailure,
                    )
                } else if (splitRows.isNotEmpty()) {
                    // هر ردیف یک تراکنش؛ شناسه‌ی صریح و پشتِ‌هم (قاعده‌ی حلقه در CLAUDE.md).
                    val base = System.currentTimeMillis()
                    splitRows.forEachIndexed { i, (c, a) ->
                        accountViewModel.addTransaction(
                            accountId = accountId!!,
                            type = TransactionType.WITHDRAWAL,
                            amount = a,
                            description = description.trim(),
                            year = date.y,
                            month = date.m,
                            day = date.d,
                            category = c ?: category,
                            sourceType = "split",
                            sourceId = base.toString(),
                            id = base + i,
                            receiptPath = receiptPath,
                            tags = tags,
                            reimbursable = reimbursable,
                            onSuccess = if (i == splitRows.lastIndex) onSaved else ({}),
                            onFailure = onSaveFailure,
                        )
                    }
                } else {
                    accountViewModel.addTransaction(
                        accountId = accountId!!,
                        type = if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT else TransactionType.WITHDRAWAL,
                        amount = amount,
                        description = description.trim(),
                        year = date.y,
                        month = date.m,
                        day = date.d,
                        category = category,
                        receiptPath = receiptPath,
                        tags = tags,
                        reimbursable = reimbursable && kind == NewTxKind.EXPENSE,
                        onSuccess = onSaved,
                        onFailure = onSaveFailure,
                    )
                }
            }
        }

        AccentPillButton(
            text = "ثبت · ${toFa(GamificationRepository.Reward.DAILY_LOG)} سکه",
            accent = accent,
            onClick = { submit(true) },
            modifier = Modifier.weight(1f),
        )

            // «ذخیره به‌عنوانِ الگو» کنارِ دکمه‌ی ثبت - قبلاً ته کارتِ گزینه‌ها گم بود (خواسته‌ی کاربر).
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.5.dp, AppLine, RoundedCornerShape(999.dp))
                    .pressScaleClickable { showSaveTemplate = true }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.BookmarkAdd, contentDescription = "ذخیره به‌عنوانِ الگو", tint = AppMuted, modifier = Modifier.size(22.dp))
            }
            // «+ باز» - ثبت می‌کنه و فرم رو برای واردکردنِ تراکنشِ بعدی خالی می‌کنه.
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.5.dp, AppLine, RoundedCornerShape(999.dp))
                    .pressScaleClickable { submit(false) }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("+ باز", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }

        if (showCalendar) {
            // زمینه اجباری است - CalendarPickerScreen خودش زمینه ندارد و بی این، فرمِ زیرش
            // از لابه‌لایش دیده می‌شود.
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                CalendarPickerScreen(
                    initialDate = date,
                    onDateSelected = { date = it; showCalendar = false },
                    onBack = { showCalendar = false },
                )
            }
        }
    }
}

/** دکمه‌ی قرصیِ توپُرِ رنگِ accent - جایگزینِ [GradientButton]ِ سبزِ ثابت برای این شیت که accentش
 * با تبِ فعال عوض می‌شه (طرحِ Liquid Glass: «بدونِ گرادیان رو CTAهای اصلی»). رنگِ متن بر اساسِ
 * روشنیِ خودِ accent انتخاب می‌شه تا کنتراست همیشه کافی بمونه. */
@Composable
private fun AccentPillButton(text: String, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor = if (accent.luminance() > 0.45f) Color.Black else Color.White
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = accent,
        contentColor = contentColor,
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

private enum class AccountSlot { MAIN, FROM, TO }

private fun validate(
    kind: NewTxKind,
    amount: Double,
    accountId: Long?,
    fromAccountId: Long?,
    toAccountId: Long?,
): String? = when {
    amount <= 0.0 -> "مبلغ رو وارد کن"
    kind == NewTxKind.TRANSFER && fromAccountId == null -> "حساب‌کتابِ مبدا رو انتخاب کن"
    kind == NewTxKind.TRANSFER && toAccountId == null -> "حساب‌کتابِ مقصد رو انتخاب کن"
    kind == NewTxKind.TRANSFER && fromAccountId == toAccountId -> "مبدا و مقصد نمی‌تونن یکی باشن"
    kind != NewTxKind.TRANSFER && accountId == null -> "حساب‌کتاب رو انتخاب کن"
    else -> null
}

/** «(امروز) یکشنبه ۱۸ مرداد ۱۴۰۵» - پیشوندِ «امروز» فقط وقتی واقعاً امروزه. */
private fun dateLabel(date: PersianDate): String {
    val today = JalaliCalendar.today()
    val prefix = if (date.y == today.y && date.m == today.m && date.d == today.d) "(امروز) " else ""
    return "$prefix${toFa(date.d)} ${persianMonthName(date.m)} ${toFa(date.y)}"
}

@Composable
private fun SheetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
        Text(
            text,
            color = if (filled) AppText else AppMuted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AccountSlotTile(
    label: String,
    account: AccountEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = AppMuted, fontSize = 11.sp)
        Column(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .background(AppSurface2, RoundedCornerShape(14.dp))
                .pressScaleClickable(onClick = onClick)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (account != null) {
                AccountBadge(account = account, size = 30.dp)
            } else {
                Box(
                    modifier = Modifier.size(30.dp).background(AppChipBg, CircleShape),
                )
            }
            Text(
                account?.name ?: "حساب‌کتاب",
                color = if (account != null) AppText else AppMuted,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun SuggestedCategoryChip(
    entry: CategoryEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                if (selected) entry.color.copy(alpha = 0.22f) else AppSurface2,
                RoundedCornerShape(12.dp),
            )
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(15.dp))
        Text(
            entry.name,
            color = if (selected) AppText else AppMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}
