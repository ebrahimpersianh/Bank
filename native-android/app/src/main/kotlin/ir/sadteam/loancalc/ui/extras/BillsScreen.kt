package ir.sadteam.loancalc.ui.extras

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Event
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppLine
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import ir.sadteam.loancalc.ui.components.dashedBorder
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.BillEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/** نوع‌های قبض - کلید ثابت، برچسبِ فارسی. */
val BILL_KINDS = listOf(
    "water" to "آب", "power" to "برق", "gas" to "گاز", "mobile" to "موبایل",
    "phone" to "تلفنِ ثابت", "internet" to "اینترنت", "other" to "سایر",
)

fun billKindLabel(kind: String) = BILL_KINDS.firstOrNull { it.first == kind }?.second ?: "قبض"

/** آیا قبض در دوره‌ی جاری (این ماه) هنوز پرداخت نشده و موعدش رسیده یا تا ۳ روزِ دیگر می‌رسد؟ */
fun BillEntity.isDueSoon(y: Int, m: Int, d: Int): Boolean {
    if (periodMonths > 1 && (m - 1) % periodMonths != 0) return false
    if (lastPaidKey == "$y-$m") return false
    if (createdAfterDueIn(y, m)) return false
    return d >= dueDay - 3
}

/**
 * ۱۱ آبان (تستِ شبیه‌ساز): قبضی که **امروز** ساخته شده و روزِ موعدش در همین ماه گذشته، فوراً
 * «گذشته» نشان داده می‌شد. موعدِ این ماه مالِ قبل از ساختنش بوده؛ اولین موعد، دوره‌ی بعد است.
 * شناسه‌ی قبض زمانِ ساختِ آن است (`System.currentTimeMillis()` در فرمِ افزودن).
 */
fun BillEntity.createdAfterDueIn(y: Int, m: Int): Boolean {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = id }
    val made = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(
        c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH),
    )
    return made.y == y && made.m == m && made.d > dueDay
}

/** دسترسیِ بی‌ایمپورت به [isDueSoon] از فایل‌های دیگر. */
object BillsDue {
    fun BillEntity.dueSoon(y: Int, m: Int, d: Int) = isDueSoon(y, m, d)
}

/** فهرست و افزودنِ قبض‌ها (برگرفته از پولکس، ۶ مهر). */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(onBack: () -> Unit, viewModel: ExtrasViewModel = hiltViewModel()) {
    val bills by viewModel.bills.collectAsState()
    val today = remember { JalaliCalendar.today() }
    var editing by remember { mutableStateOf<BillEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var paying by remember { mutableStateOf<BillEntity?>(null) }
    var deleting by remember { mutableStateOf<BillEntity?>(null) }
    deleting?.let { b ->
        ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog(
            title = "حذفِ قبض",
            text = "«${b.name}» و یادآورهایش پاک می‌شود. پرداخت‌های ثبت‌شده در حساب‌ها سرِ جایشان می‌مانند.",
            onConfirm = { viewModel.deleteBill(b); deleting = null },
            onDismiss = { deleting = null },
        )
    }
    val accounts by viewModel.accounts.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText) }
                Text("قبض‌ها", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            }
        }
        if (bills.isEmpty()) {
            // طرحِ Claude Design (۸ مهر): کارتِ خط‌چین با آیکون و دکمه‌ی داخلش.
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(ir.sadteam.loancalc.ui.theme.AppSurface)
                        .dashedBorder(24.dp)
                        .padding(horizontal = 18.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(AppPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Receipt, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(36.dp)) }
                    Text("هنوز قبضی نداری", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text(
                        "آب، برق، گاز، موبایل… را اضافه کن تا نزدیکِ موعد یادت بیندازیم.",
                        color = AppMuted, fontSize = 13.sp, lineHeight = 22.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    GradientButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("+ افزودنِ قبض") }
                }
            }
        }
        items(bills, key = { it.id }) { bill ->
            val (kIcon, kColor) = billKindIcon(bill.kind)
            BillCard(
                bill = bill, icon = kIcon, tint = kColor,
                todayY = today.y, todayM = today.m, todayD = today.d,
                onEdit = { editing = bill },
                onPay = { paying = bill },
                onUnpay = { viewModel.unmarkBillPaid(bill, today.y, today.m) },
                onDelete = { deleting = bill },
            )
        }
    }
        // ۲۰ مهر (خواسته‌ی کاربر): «افزودن» مثلِ بقیه‌ی صفحه‌ها دکمه‌ی «+» شناورِ پایین است.
        if (bills.isNotEmpty()) {
            ir.sadteam.loancalc.ui.components.AppFab(
                onClick = { adding = true },
                contentDescription = "افزودنِ قبض",
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp),
            )
        }
    }

    paying?.let { bill ->
        var amountText by remember(bill) { mutableStateOf(if (bill.lastAmount > 0) (bill.lastAmount.toLong() / ir.sadteam.loancalc.ui.jibak.unitDiv).toString() else "") }
        var accountId by remember(bill) { mutableStateOf(accounts.singleOrNull()?.id) }
        var payId by remember(bill) { mutableStateOf("") }
        ir.sadteam.loancalc.ui.components.JibakFormSheet(
            onDismissRequest = { paying = null },
            title = "پرداختِ قبضِ ${bill.name}",
            primaryLabel = "پرداخت شد",
            onPrimary = {
                val rial = (amountText.toLongOrNull() ?: 0L) * ir.sadteam.loancalc.ui.jibak.unitDivD
                viewModel.markBillPaid(bill, today.y, today.m, rial, accountId)
                paying = null
            },
        ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // مبلغ داخلِ شناسه‌ی پرداخت است؛ با واردکردنش مبلغ خودش پر می‌شود.
                    Ltr {
                        OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                            value = payId,
                            visualTransformation = ir.sadteam.loancalc.ui.components.PersianDigitsTransformation,
                            onValueChange = {
                                payId = cleanNum(it).take(13)
                                ir.sadteam.loancalc.core.BillCodes.parsePaymentId(payId, bill.billId)?.let { p ->
                                    amountText = (p.amountRial / ir.sadteam.loancalc.ui.jibak.unitDiv).toString()
                                }
                            },
                            singleLine = true,
                            placeholder = { Text("شناسه‌ی پرداخت (اختیاری)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    }
                    OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        value = amountText,
                        onValueChange = { amountText = cleanNum(it).take(12) },
                        singleLine = true,
                        placeholder = { Text("مبلغ") },
                        // ۲۰ مهر (خواسته‌ی کاربر): واحد کنارِ عدد و حروفش زیرِ کادر، مثلِ بقیه‌ی فیلدهای مبلغ.
                        suffix = { Text(ir.sadteam.loancalc.ui.jibak.unitFa(), color = AppMuted, fontSize = 13.sp) },
                        visualTransformation = ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    val payDisplay = amountText.toLongOrNull() ?: 0L
                    if (payDisplay > 0) {
                        Text(
                            ir.sadteam.loancalc.ui.jibak.amountWordsCaption(payDisplay.toDouble()),
                            color = AppMuted, fontSize = 11.sp,
                        )
                    }
                    if (accounts.size > 1) {
                        Text("از کدام حساب؟", color = AppMuted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            accounts.forEach { a -> AppChip(a.name, accountId == a.id, onClick = { accountId = a.id }) }
                        }
                    }
                }
        }
    }

    if (adding || editing != null) {
        val base = editing
        var name by remember(base) { mutableStateOf(base?.name ?: "") }
        var kind by remember(base) { mutableStateOf(base?.kind ?: "power") }
        var billId by remember(base) { mutableStateOf(base?.billId ?: "") }
        var dayText by remember(base) { mutableStateOf(base?.dueDay?.toString() ?: "") }
        var period by remember(base) { mutableStateOf(base?.periodMonths ?: 1) }
        var phone by remember(base) { mutableStateOf(base?.phone ?: "") }
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        var pasteNote by remember(base) { mutableStateOf<String?>(null) }
        var dayError by remember(base) { mutableStateOf(false) }
        val close = { adding = false; editing = null }
        ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = base == null, key = "bills", label = "قبض‌ها", onBlocked = close)
        // یافته‌ی ۴۲: لمسِ بیرونِ پنجره/برگشت/«انصراف» با فرمِ دست‌خورده اول می‌پرسد.
        val billDirty = name != (base?.name ?: "") || kind != (base?.kind ?: "power") ||
            billId != (base?.billId ?: "") || dayText != (base?.dueDay?.toString() ?: "") ||
            period != (base?.periodMonths ?: 1) || phone != (base?.phone ?: "")
        val requestClose = ir.sadteam.loancalc.ui.components.rememberLeaveGuard(dirty = billDirty, onLeave = close)
        val save = {
            val day = dayText.toIntOrNull()?.coerceIn(1, 30)
            if (day == null) dayError = true
            if (day != null) {
                viewModel.saveBill(
                    (base ?: BillEntity(System.currentTimeMillis(), "", kind, null, day)).copy(
                        name = name.trim().ifBlank { billKindLabel(kind) },
                        kind = kind,
                        billId = billId.ifBlank { null },
                        dueDay = day,
                        periodMonths = period,
                        phone = if (kind == "mobile") phone.ifBlank { null } else null,
                    ),
                )
                close()
            }
        }
        // ۱۳ آبان (کاربر: «این صفحه قشنگ نیست اصلاً»): پنجره‌ی شناورِ تنگ با کاشی‌های ۴+۳، کارتِ
        // خط‌چینِ بزرگ، کادرهای ناهم‌شکل و «ذخیره»ِ کوچکِ گوشه → برگه‌ی پایینِ تمیز: نوع در یک ردیفِ
        // قرص، «چسباندن» یک پیوندِ کوچک، کادرهای یک‌شکل، روز و دوره کنارِ هم، دکمه‌ی تمام‌عرض.
        val sheet = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = requestClose,
            sheetState = sheet,
            containerColor = ir.sadteam.loancalc.ui.theme.AppSurface,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (base == null) "قبضِ جدید" else "ویرایشِ قبض", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    if (base != null) IconButton(onClick = { deleting = base; close() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذفِ قبض", tint = AppDanger)
                    }
                }
                // نوعِ قبض: یک ردیفِ قرص (آیکون + نام) - جای شبکه‌ی ۴+۳ با خانه‌ی خالی.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                ) {
                    BILL_KINDS.forEach { (k, l) ->
                        val ic = billKindIcon(k).first
                        val sel = kind == k
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (sel) AppPrimary else AppSurface2)
                                .clickable { kind = k }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(ic, contentDescription = null, tint = if (sel) androidx.compose.ui.graphics.Color.White else ir.sadteam.loancalc.ui.theme.AppPrimaryInk, modifier = Modifier.size(18.dp))
                            Text(l, color = if (sel) androidx.compose.ui.graphics.Color.White else AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
                // «چسباندنِ پیامک»: یک پیوندِ کوچک، نه کارتِ خط‌چینِ بزرگ.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val text = clipboard.getText()?.text.orEmpty()
                            val r = ir.sadteam.loancalc.core.BillCodes.parseBillSms(text)
                            if (r == null) {
                                pasteNote = "در متنِ کپی‌شده شناسه‌ی قبضی پیدا نشد"
                            } else {
                                r.billId?.let { billId = it }
                                r.kind?.let { kind = it }
                                pasteNote = buildString {
                                    append("✓ از پیامک خوانده شد")
                                    r.amountRial?.let { append(" | مبلغِ این دوره ${toFa(it / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}") }
                                }
                            }
                        }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                ) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null, tint = ir.sadteam.loancalc.ui.theme.AppPrimaryInk, modifier = Modifier.size(18.dp))
                    Text("پیامکِ قبض را کپی کرده‌ای؟ این‌جا بزن تا خودش پر شود", color = ir.sadteam.loancalc.ui.theme.AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
                pasteNote?.let { Text(it, color = if (it.startsWith("✓")) ir.sadteam.loancalc.ui.theme.AppPrimaryInk else AppDanger, fontSize = 11.5.sp) }
                OutlinedTextField(
                    textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                    value = name,
                    onValueChange = { name = it.take(30) },
                    singleLine = true,
                    label = { Text("نام (اختیاری)") },
                    placeholder = { Text("مثلاً برقِ خانه") },
                    shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                    colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Ltr {
                    OutlinedTextField(
                        textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        value = billId,
                        visualTransformation = ir.sadteam.loancalc.ui.components.PersianDigitsTransformation,
                        onValueChange = {
                            billId = cleanNum(it).take(18)
                            // نوعِ قبض از خودِ شناسه (رقمِ یکی‌مانده‌به‌آخر) - کاربر لازم نیست بداند.
                            ir.sadteam.loancalc.core.BillCodes.billIdKind(billId)?.let { k -> kind = k }
                        },
                        singleLine = true,
                        label = { Text("شناسه‌ی قبض (اختیاری)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (billId.length >= 6) {
                    val k = ir.sadteam.loancalc.core.BillCodes.billIdKind(billId)
                    Text(
                        if (k != null) "✓ شناسه درست است | قبضِ ${billKindLabel(k)}" else "این شناسه درست نیست؛ یک بار دیگر نگاه کن",
                        color = if (k != null) ir.sadteam.loancalc.ui.theme.AppPrimaryInk else AppDanger,
                        fontSize = 11.sp,
                    )
                }
                // 📱 قبضِ موبایل: شماره‌ی همان خط (خواسته‌ی کاربر، ۱۶ مهر).
                if (kind == "mobile") {
                    Ltr {
                        OutlinedTextField(
                            textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                            value = phone,
                            visualTransformation = ir.sadteam.loancalc.ui.components.PersianDigitsTransformation,
                            onValueChange = { phone = cleanNum(it).take(11) },
                            singleLine = true,
                            label = { Text("شماره‌ی موبایل") },
                            placeholder = { Text("۰۹۱۲۱۲۳۴۵۶۷") },
                            isError = phone.isNotEmpty() && !(phone.length == 11 && phone.startsWith("09")),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                            colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                // روزِ موعد و دوره کنارِ هم - هر دو جوابِ یک سؤال‌اند: «کی پرداخت می‌شود؟»
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        value = dayText,
                        visualTransformation = ir.sadteam.loancalc.ui.components.PersianDigitsTransformation,
                        onValueChange = { dayText = cleanNum(it).take(2); dayError = false },
                        singleLine = true,
                        label = { Text("روزِ موعد") },
                        isError = dayError,
                        supportingText = if (dayError) ({ Text("۱ تا ۳۰") }) else null,
                        placeholder = { Text("۱ تا ۳۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSurface2)
                            .padding(4.dp),
                    ) {
                        listOf(1 to "ماهانه", 2 to "دوماهه").forEach { (v, l) ->
                            val sel = period == v
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (sel) AppPrimary else androidx.compose.ui.graphics.Color.Transparent)
                                    .clickable { period = v },
                            ) { Text(l, color = if (sel) androidx.compose.ui.graphics.Color.White else AppMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                GradientButton(onClick = save, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(if (base == null) "ذخیره‌ی قبض" else "ذخیره‌ی تغییرات", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}


/**
 * آیکون و رنگِ هر نوعِ قبض. ۱۳ آبان (خواسته‌ی کاربر: «آیکون‌ها همه یک‌دست»): هفت رنگ (آبی، زرد،
 * نارنجی، بنفش…) → یک رنگِ برند؛ نوع را شکلِ آیکون می‌گوید.
 */
@androidx.compose.runtime.Composable
private fun billKindIcon(kind: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color> = when (kind) {
    "water" -> Icons.Filled.WaterDrop
    "power" -> Icons.Filled.Bolt
    "gas" -> Icons.Filled.LocalFireDepartment
    "mobile" -> Icons.Filled.Smartphone
    "phone" -> Icons.Filled.Call
    "internet" -> Icons.Filled.Wifi
    else -> Icons.Filled.Receipt
} to ir.sadteam.loancalc.ui.theme.AppPrimaryInk
