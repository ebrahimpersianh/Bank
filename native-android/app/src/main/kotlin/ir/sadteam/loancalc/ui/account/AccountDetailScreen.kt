package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.toFaDate
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.jibak.toFaSignedMoney
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * جزئیات یه حساب - موجودی فعلی بزرگ بالای صفحه، فرم افزودن تراکنش (واریز/برداشت + توضیح + تاریخ
 * شمسی)، و دفترچه‌ی تراکنش‌ها (تاریخ نزولی، هر ردیف قابل‌حذف).
 *
 * ⚠️ `onEdit`/`onDelete` **اختیاری**اند - از تبِ دارایی ویرایش هست، حذف نیست: کاربر همون‌جا
 * می‌بینه اسم/فرستنده‌ی پیامک غلطه و باید بتونه درستش کنه، ولی حذف از مسیرِ تماشا جای درستی نیست.
 */
@Composable
fun AccountDetailScreen(
    account: AccountEntity,
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val allTransactions by viewModel.transactions.collectAsState()
    val transactions = remember(allTransactions, account.id) {
        allTransactions.filter { it.accountId == account.id }
    }
    val balance = remember(account, allTransactions) { viewModel.balanceOf(account, allTransactions) }

    // `71a`: انتخابِ ماه. پیش‌فرض ماهِ جاری؛ فلشِ ماهِ آینده وقتی داده ندارد **خاموش** است
    // نه غایب (قاعده‌ی ۵ِ `71c`) - جای خالی کاربر را دنبالِ دکمه‌ی گم‌شده می‌فرستد.
    val today = remember { JalaliCalendar.today() }
    var shownYear by rememberSaveable { mutableStateOf(today.y) }
    var shownMonth by rememberSaveable { mutableStateOf(today.m) }
    val monthTransactions = remember(transactions, shownYear, shownMonth) {
        transactions.filter { it.year == shownYear && it.month == shownMonth }
            .sortedWith(compareByDescending<AccountTransactionEntity> { it.day }.thenByDescending { it.createdAt })
    }
    val monthIn = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount }
    }
    val monthOut = remember(monthTransactions) {
        monthTransactions.filter { it.type != TransactionType.DEPOSIT.name }.sumOf { it.amount }
    }
    val hasNextMonth = remember(transactions, shownYear, shownMonth) {
        transactions.any { it.year > shownYear || (it.year == shownYear && it.month > shownMonth) }
    }

    var showAddTransaction by remember { mutableStateOf(false) }
    var txType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var txDate by remember { mutableStateOf(JalaliCalendar.today()) }
    // فرم هر بار که باز می‌شه امروز رو دوباره حساب می‌کنه - `remember` تنها یک‌بار حساب می‌شد
    // و اگه گوشی از نیمه‌شب رد می‌شد، تاریخِ پیش‌فرض دیروز می‌موند.
    LaunchedEffect(showAddTransaction) {
        if (showAddTransaction) txDate = JalaliCalendar.today()
    }
    var showCalendar by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }
    var editingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }

    // تقویم **روی** صفحه می‌نشیند، نه به‌جایش. با `return` کلِ LazyColumn از کامپوزیشن بیرون
    // می‌رفت و اسکرولِ دفترچه‌ی تراکنش‌ها با هر انتخابِ تاریخ صفر می‌شد.
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(account.name, color = AppText, fontSize = 16.sp)
                    Text(account.bankName, color = AppMuted, fontSize = 12.sp)
                }
            }
        }

        item {
            BalanceHero(balance = balance, monthIn = monthIn, monthOut = monthOut, monthName = persianMonthName(shownMonth))
        }

        item {
            MonthBar(
                year = shownYear,
                month = shownMonth,
                hasNext = hasNextMonth,
                onChange = { y, m -> shownYear = y; shownMonth = m },
            )
        }

        if (onEdit != null || onDelete != null) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onEdit?.let {
                        OutlinedButton(onClick = it, modifier = Modifier.weight(1f)) {
                            Text("ویرایش حساب")
                        }
                    }
                    onDelete?.let {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("حذف حساب")
                        }
                    }
                }
            }
        }

        item {
            if (showAddTransaction) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppCard(label = "نوع تراکنش") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppChip(
                                label = "واریز",
                                selected = txType == TransactionType.DEPOSIT,
                                onClick = { txType = TransactionType.DEPOSIT },
                            )
                            AppChip(
                                label = "برداشت",
                                selected = txType == TransactionType.WITHDRAWAL,
                                onClick = { txType = TransactionType.WITHDRAWAL },
                            )
                        }
                    }
                    AppCard(label = "مبلغ") {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = cleanNum(it) },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                        )
                        val amountToman = amountText.toLongOrNull() ?: 0L
                        if (amountToman > 0) {
                            Text(
                                "${numberToWordsFa(amountToman.toDouble())} تومان",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    AppCard(label = "توضیح (اختیاری)") {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    AppCard(label = "تاریخ") {
                        Row(
                            modifier = Modifier.fillMaxWidth().pressScaleClickable { showCalendar = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                            Text(
                                toFaDate(txDate.y, txDate.m, txDate.d),
                                color = AppText,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                            )
                        }
                    }
                    if (error != null) {
                        Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val toman = amountText.toLongOrNull() ?: 0L
                                error = if (toman <= 0L) "مبلغ رو وارد کن" else null
                                if (error == null) {
                                    viewModel.addTransaction(
                                        accountId = account.id,
                                        type = txType,
                                        amount = tomanToRial(toman).toDouble(),
                                        description = description.trim(),
                                        year = txDate.y,
                                        month = txDate.m,
                                        day = txDate.d,
                                    )
                                    amountText = ""
                                    description = ""
                                    showAddTransaction = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("ثبت تراکنش")
                        }
                        OutlinedButton(onClick = { showAddTransaction = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                GradientButton(onClick = { showAddTransaction = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ ثبت تراکنش")
                }
            }
        }

        if (monthTransactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.SwapVert,
                    title = if (transactions.isEmpty()) "هنوز تراکنشی ثبت نشده" else "این ماه تراکنشی نداری",
                    description = if (transactions.isEmpty()) {
                        "واریز و برداشت‌های این حساب که ثبت بشن، همین‌جا به‌ترتیبِ تاریخ می‌بینیشون."
                    } else {
                        "ماهِ دیگری را از نوارِ بالا انتخاب کن."
                    },
                )
            }
        } else {
            // قاعده‌ی ۴ِ `71c`: تاریخ یک‌بار بالای گروه، نه روی تک‌تکِ ردیف‌ها.
            monthTransactions.groupBy { it.day }.forEach { (day, rows) ->
                item(key = "day-$shownYear-$shownMonth-$day") {
                    Text(
                        "${toFa(day)} ${persianMonthName(shownMonth)}",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                    )
                }
                items(rows, key = { it.id }) { tx ->
                    SwipeToDeleteRow(
                        onDelete = { deletingTx = tx },
                        confirmDismiss = false,
                        modifier = Modifier.animateItem(),
                    ) {
                        CompactTransactionRow(tx = tx, onClick = { editingTx = tx })
                    }
                }
            }
            item {
                // نشانه‌ی کشف‌پذیری - یک‌بار زیرِ فهرست، نه زیرِ هر ردیف (که ردیف را دوبرابر می‌کرد).
                Text(
                    "برای ویرایش بزن · برای حذف بکش",
                    color = AppMuted.copy(alpha = 0.7f),
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
        if (showCalendar) {
            // زمینه اجباری است - CalendarPickerScreen خودش زمینه ندارد و بی این، صفحه‌ی زیرش
            // از لابه‌لایش دیده می‌شود.
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                CalendarPickerScreen(
                    initialDate = txDate,
                    onDateSelected = { txDate = it; showCalendar = false },
                    onBack = { showCalendar = false },
                )
            }
        }

        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "حذف حساب",
                text = "حسابِ «${account.name} - ${account.bankName}» حذف بشه؟ این کار قابلِ‌برگشت نیست.",
                // پرچم را خودش پایین می‌آورد - وابسته‌بودن به این‌که onDelete صفحه را ببندد
                // یک وابستگیِ نامرئی بود.
                onConfirm = { showDeleteConfirm = false; onDelete?.invoke() },
                onDismiss = { showDeleteConfirm = false },
            )
        }
        editingTx?.let { tx ->
            EditTransactionDialog(
                tx = tx,
                onSave = { amountRial, description ->
                    viewModel.updateTransaction(tx, amountRial, description)
                    editingTx = null
                },
                onDismiss = { editingTx = null },
            )
        }

        deletingTx?.let { tx ->
            ConfirmDeleteDialog(
                title = "حذفِ تراکنش",
                text = "این تراکنش حذف بشه؟ این کار قابلِ‌برگشت نیست.",
                onConfirm = { viewModel.deleteTransaction(tx); deletingTx = null },
                onDismiss = { deletingTx = null },
            )
        }
    }
}

/**
 * هیرویِ `71a` - موجودی + دو قرصِ جمعِ **همان ماهِ انتخاب‌شده**.
 *
 * قاعده‌ی ۵ِ `71c`: انتخابِ ماه بی جمعِ همان ماه فقط فهرست را کوتاه می‌کند و خبری نمی‌دهد،
 * پس این دو قرص جزوِ خودِ انتخابِ ماه‌اند نه تزئین.
 */
@Composable
private fun BalanceHero(balance: Double, monthIn: Double, monthOut: Double, monthName: String) {
    val privacyMode = LocalPrivacyMode.current
    AppHeroCard {
        Text("موجودیِ نقدی", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, rialToToman(balance.toLong()).toFaMoney()) + " تومان",
                color = Color.White,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MonthSumPill("واریزِ $monthName", monthIn, positive = true, privacyMode = privacyMode, modifier = Modifier.weight(1f))
            MonthSumPill("برداشتِ $monthName", monthOut, positive = false, privacyMode = privacyMode, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MonthSumPill(
    label: String,
    amount: Double,
    positive: Boolean,
    privacyMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.row))
            .background(HeroPillBg)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(label, color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(
                    masked,
                    rialToToman(amount.toLong()).let { if (positive) it else -it }.toFaSignedMoney(),
                ),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * نوارِ ماه - همان الگوی تبِ گزارش (قاعده‌ی ۵ِ `71c`: «بله، قرضش بگیر»).
 *
 * ⚠️ در RTL فلشِ **قبلی سمتِ راست** است و «بعدی» سمتِ چپ (قاعده‌ی سیستمِ طراحی)، پس
 * `ChevronRight` ماهِ قبل را می‌آورد نه بعد را.
 */
@Composable
private fun MonthBar(year: Int, month: Int, hasNext: Boolean, onChange: (Int, Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (month == 1) onChange(year - 1, 12) else onChange(year, month - 1) }) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "ماهِ قبل", tint = AppText)
        }
        Text(
            "${persianMonthName(month)} ${toFa(year)}",
            color = AppText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { if (month == 12) onChange(year + 1, 1) else onChange(year, month + 1) }, enabled = hasNext) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "ماهِ بعد",
                // خاموش، نه غایب - جای خالی کاربر را دنبالِ دکمه‌ی گم‌شده می‌فرستد.
                tint = if (hasNext) AppText else AppMuted.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * اصلاحِ مبلغ و توضیحِ یک تراکنشِ ثبت‌شده - خواسته‌ی کاربر: «گاهی اشتباه چیزی اضافه می‌شود».
 *
 * **نوع، حساب و تاریخ عمداً اینجا نیستند.** عوض‌کردنشان یعنی یک تراکنشِ دیگر، و در آن
 * حالت حذف‌وثبتِ دوباره هم صادقانه‌تر است هم تاریخچه را درست نگه می‌دارد.
 *
 * الگوی استانداردِ فیلدِ مبلغِ پروژه: state فقط رقمِ خام (`cleanNum`)، جداکننده فقط بصری
 * با [ThousandsSeparatorTransformation]، و کپشنِ حروفی زیرش. ورودی **تومان** است و
 * تبدیل به ریال دقیقاً یک بار، لحظه‌ی ذخیره.
 */
@Composable
private fun EditTransactionDialog(
    tx: AccountTransactionEntity,
    onSave: (amountRial: Double, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf(rialToToman(tx.amount.toLong()).toString()) }
    var description by remember { mutableStateOf(tx.description) }
    val toman = amountText.toLongOrNull() ?: 0L

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("ویرایشِ تراکنش", color = AppText, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanNum(it) },
                    label = { Text("مبلغ") },
                    suffix = { Text("تومان") },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (toman > 0) {
                    Text(
                        numberToWordsFa(toman.toDouble()) + " تومان",
                        color = AppMuted,
                        fontSize = 11.sp,
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tomanToRial(toman).toDouble(), description) },
                enabled = toman > 0,
            ) { Text("ذخیره", color = if (toman > 0) AppPrimaryInk else AppMuted) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = AppMuted) } },
    )
}
