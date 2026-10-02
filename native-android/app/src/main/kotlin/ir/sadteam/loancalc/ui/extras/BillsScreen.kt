package ir.sadteam.loancalc.ui.extras

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
import androidx.compose.material3.Text
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
    return d >= dueDay - 3
}

/** دسترسیِ بی‌ایمپورت به [isDueSoon] از فایل‌های دیگر. */
object BillsDue {
    fun BillEntity.dueSoon(y: Int, m: Int, d: Int) = isDueSoon(y, m, d)
}

/** فهرست و افزودنِ قبض‌ها (برگرفته از پولکس، ۶ مهر). */
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText) }
                Text("قبض‌ها", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
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
        } else {
            item {
                GradientButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) { Text("+ افزودنِ قبض") }
            }
        }
        items(bills, key = { it.id }) { bill ->
            val due = bill.isDueSoon(today.y, today.m, today.d)
            AppCard(modifier = Modifier.clickable { editing = bill }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (kIcon, kColor) = billKindIcon(bill.kind)
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.icon)).background(kColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(kIcon, contentDescription = null, tint = kColor, modifier = Modifier.size(22.dp)) }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("${billKindLabel(bill.kind)} · ${bill.name}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            buildString {
                                append("موعد: ${toFa(bill.dueDay)}ِ ")
                                append(if (bill.periodMonths > 1) "هر ${toFa(bill.periodMonths)} ماه" else "هر ماه")
                                if (bill.lastPaidKey == "${today.y}-${today.m}") append(" · این دوره پرداخت شد ✓")
                            },
                            color = if (due) AppDanger else AppMuted,
                            fontSize = 11.sp,
                        )
                    }
                    if (due) {
                        TextButton(onClick = { paying = bill }) {
                            Text("پرداخت شد", color = AppPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    paying?.let { bill ->
        var amountText by remember(bill) { mutableStateOf(if (bill.lastAmount > 0) (bill.lastAmount.toLong() / 10).toString() else "") }
        var accountId by remember(bill) { mutableStateOf(accounts.singleOrNull()?.id) }
        var payId by remember(bill) { mutableStateOf("") }
        JibakAlertDialog(
            onDismissRequest = { paying = null },
            title = { Text("پرداختِ قبضِ ${bill.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // مبلغ داخلِ شناسه‌ی پرداخت است؛ با واردکردنش مبلغ خودش پر می‌شود.
                    Ltr {
                        OutlinedTextField(
                            value = payId,
                            onValueChange = {
                                payId = cleanNum(it).take(13)
                                ir.sadteam.loancalc.core.BillCodes.parsePaymentId(payId, bill.billId)?.let { p ->
                                    amountText = (p.amountRial / 10).toString()
                                }
                            },
                            singleLine = true,
                            placeholder = { Text("شناسه‌ی پرداخت (اختیاری)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = cleanNum(it).take(12) },
                        singleLine = true,
                        placeholder = { Text("مبلغ (تومان)") },
                        visualTransformation = ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    if (accounts.size > 1) {
                        Text("از کدام حساب؟", color = AppMuted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            accounts.forEach { a -> AppChip(a.name, accountId == a.id, onClick = { accountId = a.id }) }
                        }
                    }
                }
            },
            confirmButton = {
                GradientButton(onClick = {
                    val rial = (amountText.toLongOrNull() ?: 0L) * 10.0
                    viewModel.markBillPaid(bill, today.y, today.m, rial, accountId)
                    paying = null
                }) { Text("پرداخت شد") }
            },
            dismissButton = { TextButton(onClick = { paying = null }) { Text("انصراف") } },
        )
    }

    if (adding || editing != null) {
        val base = editing
        var name by remember(base) { mutableStateOf(base?.name ?: "") }
        var kind by remember(base) { mutableStateOf(base?.kind ?: "power") }
        var billId by remember(base) { mutableStateOf(base?.billId ?: "") }
        var dayText by remember(base) { mutableStateOf(base?.dueDay?.toString() ?: "") }
        var period by remember(base) { mutableStateOf(base?.periodMonths ?: 1) }
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        var pasteNote by remember(base) { mutableStateOf<String?>(null) }
        var dayError by remember(base) { mutableStateOf(false) }
        val close = { adding = false; editing = null }
        ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = base == null, key = "bills", label = "قبض‌ها", onBlocked = close)
        JibakAlertDialog(
            onDismissRequest = close,
            title = { Text(if (base == null) "قبضِ جدید" else "ویرایشِ قبض") },
            text = {
                // بازطراحیِ ۸ مهر: کاشی‌های آیکون‌دارِ نوعِ قبض، کارتِ خط‌چینِ «چسباندن»، کادرهای گرد.
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    BILL_KINDS.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (k, l) ->
                                val (ic, tint) = billKindIcon(k)
                                val sel = kind == k
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (sel) tint.copy(alpha = 0.16f) else AppSurface2)
                                        .border(if (sel) 1.5.dp else 1.dp, if (sel) tint else AppLine, RoundedCornerShape(16.dp))
                                        .clickable { kind = k }
                                        .padding(vertical = 10.dp),
                                ) {
                                    Icon(ic, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                                    Text(l, color = if (sel) AppText else AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    // 📋 پیامکِ قبض (آب/برق/گاز/…) را کپی کن و اینجا بزن: شناسه و نوع خودکار پر می‌شوند.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppPrimary.copy(alpha = 0.08f))
                            .dashedBorder(radius = 16.dp, color = AppPrimary.copy(alpha = 0.5f), width = 1.5.dp)
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
                                        r.amountRial?.let { append(" · مبلغِ این دوره ${toFa(it / 10)} تومان") }
                                    }
                                }
                            }
                            .padding(12.dp),
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(AppPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.ContentPaste, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp)) }
                        Column(Modifier.padding(start = 10.dp)) {
                            Text("چسباندنِ پیامکِ قبض", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("پیامک را کپی کن و بزن؛ شناسه و نوع خودکار پر می‌شوند", color = AppMuted, fontSize = 11.sp)
                        }
                    }
                    pasteNote?.let { Text(it, color = if (it.startsWith("✓")) AppPrimary else AppDanger, fontSize = 11.5.sp) }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(30) },
                        singleLine = true,
                        label = { Text("نام") },
                        placeholder = { Text("مثلاً خانه") },
                        leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null, tint = AppMuted) },
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Ltr {
                        OutlinedTextField(
                            value = billId,
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
                            if (k != null) "✓ شناسه درست است · قبضِ ${billKindLabel(k)}" else "این شناسه درست نیست؛ یک بار دیگر نگاه کن",
                            color = if (k != null) AppPrimary else AppDanger,
                            fontSize = 11.sp,
                        )
                    }
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = cleanNum(it).take(2); dayError = false },
                        singleLine = true,
                        label = { Text("روزِ موعد در ماه") },
                        isError = dayError,
                        supportingText = if (dayError) ({ Text("روزِ موعد را بنویس (۱ تا ۳۰)") }) else null,
                        placeholder = { Text("۱ تا ۳۰") },
                        leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null, tint = AppMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(1 to "ماهانه", 2 to "دوماهه").forEach { (v, l) ->
                            val sel = period == v
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (sel) AppPrimary else AppSurface2)
                                    .clickable { period = v },
                            ) { Text(l, color = if (sel) androidx.compose.ui.graphics.Color.White else AppMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            },
            confirmButton = {
                GradientButton(onClick = {
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
                            ),
                        )
                        close()
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = {
                if (base != null) {
                    IconButton(onClick = { deleting = base; close() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                    }
                } else {
                    TextButton(onClick = close) { Text("انصراف") }
                }
            },
        )
    }
}


/** آیکون و رنگِ هر نوعِ قبض (طرحِ Claude Design). */
private fun billKindIcon(kind: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color> = when (kind) {
    "water" -> Icons.Filled.WaterDrop to androidx.compose.ui.graphics.Color(0xFF2B7BD6)
    "power" -> Icons.Filled.Bolt to androidx.compose.ui.graphics.Color(0xFFD69E2E)
    "gas" -> Icons.Filled.LocalFireDepartment to androidx.compose.ui.graphics.Color(0xFFDD6B20)
    "mobile" -> Icons.Filled.Smartphone to androidx.compose.ui.graphics.Color(0xFF7C4DDB)
    "phone" -> Icons.Filled.Call to androidx.compose.ui.graphics.Color(0xFF5B6B62)
    "internet" -> Icons.Filled.Wifi to androidx.compose.ui.graphics.Color(0xFF0E9F8E)
    else -> Icons.Filled.Receipt to androidx.compose.ui.graphics.Color(0xFF7A8A81)
}
