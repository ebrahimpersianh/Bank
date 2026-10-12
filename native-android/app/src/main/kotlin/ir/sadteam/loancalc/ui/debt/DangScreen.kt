package ir.sadteam.loancalc.ui.debt

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import ir.sadteam.loancalc.ui.components.dashedBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.LocalAppColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.DangMethod
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.equalDangShares
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.percentageDangShares
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.DangItemInput
import ir.sadteam.loancalc.data.DangItemShareInput
import ir.sadteam.loancalc.data.DangParticipantInput
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.data.db.DangEventEntity
import ir.sadteam.loancalc.data.db.DangParticipantEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.CounterpartyPickerDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.JibakLogo
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch

private const val ME_NAME = "خودم"

/** یه شرکت‌کننده تو فرمِ درحالِ‌ساختنِ دنگ - [counterpartyId] نال یعنی «خودم» (رجوع کن به کامنتِ
 * DangParticipantEntity). فیلدهای متنیِ درصد/مبلغِ‌دلخواه فقط تو روش‌های PERCENTAGE/CUSTOM پر می‌شن. */
private data class DangParticipantDraft(
    val counterpartyId: Long?,
    val name: String,
    var percentageText: String = "",
    var customAmountText: String = "",
)

/** یه قلمِ فاکتور تو فرمِ ITEMIZED - [sharedByIndices] اندیسِ شرکت‌کننده‌هایی که این قلم بینشون
 * مساوی تقسیم می‌شه (چک‌باکسِ همون قلم). */
private data class DangItemDraft(
    var description: String = "",
    var amountText: String = "",
    var sharedByIndices: Set<Int> = emptySet(),
)

/**
 * صورت‌حسابِ عکسیِ دنگ — **رو و پشت** (فریمِ `81e`).
 *
 * 🚨 **پشت جدولِ سهم‌هاست و نه چیزِ دیگر**: همان چیزی که رو نمی‌تواند بگوید. رو یک عدد و
 * یک شمارش دارد؛ پشت شش ردیف. نسخه‌ی قبلی هر دو را در یک کارت ریخته بود، پس نه خلاصه‌ی
 * فرستادنی بود نه جدولِ خواندنی.
 *
 * 🚨 **کاغذِ طلایی در پشت نمی‌آید**: گرادیانِ طلایی زیرِ جدولِ شش‌ردیفی خوانایی را می‌خورد.
 * پشت زمینه‌ی استخوانی می‌گیرد و جنسِ خانواده با حاشیه و رنگِ متن حفظ می‌شود.
 *
 * رمزِ حالتِ خصوصی این‌جا اثر ندارد — رسیدِ خروجی همیشه عددِ واقعی دارد، چون همان چیزی
 * است که کاربر می‌خواهد بفرستد.
 */
@Composable
internal fun DangReceiptCard(
    event: DangEventEntity,
    participants: List<DangParticipantEntity>,
    counterpartyNameFor: (Long?) -> String,
    showBack: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = LocalAppColors.current.isDark
    // هگزِ خام این‌جا عمدی است: رسید یک **تصویرِ خروجی** است که بیرونِ برنامه فرستاده
    // می‌شود، پس مثلِ اسپلش و لوگو کاغذِ خودش را دارد و با تمِ خریداری‌شده نمی‌چرخد.
    val paper = if (dark) Color(0xFF171310) else Color(0xFFFFFDF6)
    val edge = if (dark) Color(0xFF3A3426) else Color(0xFFE8D9AE)
    val ink = if (dark) Color(0xFFE8C25A) else Color(0xFF5A4208)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (showBack) paper else AppPrimaryPill, RoundedCornerShape(14.dp))
            .then(if (showBack) Modifier.border(1.dp, edge, RoundedCornerShape(14.dp)) else Modifier)
            .pressScaleClickable(onClick = onFlip)
            .padding(16.dp),
    ) {
        // ⚠️ لوگوی واقعی، نه فقط اسم: این تصویر قراره تو گروهِ دوستان فرستاده بشه، پس تنها
        // جاییه که برندِ اپ خودش رو به آدم‌هایی نشون می‌ده که هنوز کاربرِ جیبک نیستن.
        Row(verticalAlignment = Alignment.CenterVertically) {
            JibakLogo(width = 26.dp)
            Text(
                "جیبک",
                color = if (showBack) ink else AppPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 6.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                if (showBack) "رو" else "پشت",
                color = if (showBack) ink.copy(alpha = 0.7f) else AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            event.title,
            color = if (showBack) ink else AppText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (!showBack) {
            // رو: یک عدد و یک شمارش. همین و بس.
            Text(
                "${toFa(event.day)}/${toFa(event.month)}/${toFa(event.year)} · " +
                    "${toFa(participants.size)} نفر",
                color = AppMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                "${fmt((event.totalAmount) / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                color = AppText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Text(
                "${toFa(event.day)}/${toFa(event.month)}/${toFa(event.year)}",
                color = ink.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
            )
            participants.forEach { participant ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val name = counterpartyNameFor(participant.counterpartyId)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ink.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            name.take(1),
                            color = ink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        name,
                        color = ink,
                        fontSize = 12.5.sp,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    // تسویه سبز، بدهی قرمز: قرمزِ این‌جا با قاعده‌ی رنگِ برنامه جور است —
                    // پولی است که واقعاً نرسیده.
                    if (participant.settled) {
                        Text("تسویه", color = AppPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    } else {
                        Text(
                            "${fmt((participant.shareAmount) / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                            color = AppDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
            Text(
                DangMethod.valueOf(event.method).label,
                color = ink.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/** فرمِ ساختِ دنگِ تازه - هر ۴ روش (جوابِ سوالِ ۷). */
@Composable
fun DangCreateScreen(
    counterparties: List<CounterpartyEntity>,
    expenses: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity> = emptyList(),
    initialExpenseId: Long = 0L,
    onCancel: () -> Unit,
    onCreateCounterparty: (name: String, onCreated: (Long) -> Unit) -> Unit,
    /** ویرایش (۱۶ مهر): دنگِ موجود + بارگذاریِ سهم‌ها/قلم‌هایش. */
    existing: DangEventEntity? = null,
    loadExisting: (suspend (Long) -> Triple<List<DangParticipantEntity>, List<ir.sadteam.loancalc.data.db.DangItemEntity>, List<ir.sadteam.loancalc.data.db.DangItemShareEntity>>)? = null,
    onSave: (
        title: String,
        method: DangMethod,
        totalAmount: Double,
        year: Int,
        month: Int,
        day: Int,
        isEventMode: Boolean,
        participants: List<DangParticipantInput>,
        items: List<DangItemInput>,
    ) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var totalText by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf(DangMethod.EQUAL) }
    var isEventMode by rememberSaveable { mutableStateOf(false) }
    val today = remember { JalaliCalendar.today() }
    var year by rememberSaveable { mutableStateOf(today.y) }
    var month by rememberSaveable { mutableStateOf(today.m) }
    var day by rememberSaveable { mutableStateOf(today.d) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddParticipant by remember { mutableStateOf(false) }
    var showExpensePicker by remember { mutableStateOf(false) }
    var saveRequest by remember { mutableStateOf(0) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    var pickForIndex by remember { mutableStateOf(-1) }
    var pickedExpenseId by rememberSaveable { mutableStateOf(0L) }
    var showCalc by remember { mutableStateOf(false) }
    // آمدن از «تقسیمِ دنگ» روی یک خرج: همان خرج از اول انتخاب شده است.
    LaunchedEffect(initialExpenseId, expenses.size) {
        if (initialExpenseId > 0 && pickedExpenseId == 0L) {
            expenses.firstOrNull { it.id == initialExpenseId }?.let { e ->
                pickedExpenseId = e.id
                title = e.description.take(40)
                totalText = (e.amount / ir.sadteam.loancalc.ui.jibak.unitDiv).toLong().toString()
                year = e.year; month = e.month; day = e.day
            }
        }
    }
    if (showExpensePicker) {
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { showExpensePicker = false },
            title = { Text("کدام خرج؟") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(expenses.size) { i ->
                        val e = expenses[i]
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    pickedExpenseId = e.id
                                    title = e.description.take(40)
                                    totalText = (e.amount / ir.sadteam.loancalc.ui.jibak.unitDiv).toLong().toString()
                                    year = e.year; month = e.month; day = e.day
                                    showExpensePicker = false
                                }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(e.description.ifBlank { "خرج" }, color = AppText, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                "${fmt(e.amount / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()} · ${toFa(e.year)}/${toFa(e.month)}/${toFa(e.day)}",
                                color = AppMuted, fontSize = 11.sp,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExpensePicker = false }) { Text("انصراف") } },
        )
    }

    val participants = remember { mutableStateOf(listOf(DangParticipantDraft(counterpartyId = null, name = ME_NAME), DangParticipantDraft(counterpartyId = -2L, name = "دوستِ ${toFa(2)}"))) }
    val items = remember { mutableStateOf(listOf<DangItemDraft>()) }

    // ویرایش: فرم با همان دنگ پر می‌شود (یک بار).
    var loadedExisting by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(existing?.id) {
        val ev = existing ?: return@LaunchedEffect
        if (loadedExisting || loadExisting == null) return@LaunchedEffect
        val (ps, its, shares) = loadExisting(ev.id)
        val div = ir.sadteam.loancalc.ui.jibak.unitDivD
        title = ev.title
        totalText = Math.round(ev.totalAmount / div).toString()
        method = runCatching { DangMethod.valueOf(ev.method) }.getOrDefault(DangMethod.EQUAL)
        isEventMode = ev.isEventMode
        year = ev.year; month = ev.month; day = ev.day
        showAdvanced = method != DangMethod.EQUAL || ev.isEventMode
        val ordered = ps.sortedBy { if (it.counterpartyId == null) 0 else 1 }
        participants.value = ordered.map { p ->
            DangParticipantDraft(
                counterpartyId = p.counterpartyId,
                name = if (p.counterpartyId == null) ME_NAME else counterparties.firstOrNull { it.id == p.counterpartyId }?.name ?: "دوست",
                percentageText = p.percentage?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "",
                customAmountText = Math.round(p.shareAmount / div).toString(),
            )
        }
        val indexOfPid = ordered.mapIndexed { i, p -> p.id to i }.toMap()
        items.value = its.map { item ->
            DangItemDraft(
                description = item.description,
                amountText = Math.round(item.amount / div).toString(),
                sharedByIndices = shares.filter { it.itemId == item.id }.mapNotNull { indexOfPid[it.participantId] }.toSet(),
            )
        }
        loadedExisting = true
    }

    val totalAmount = totalText.toDoubleOrNull() ?: 0.0

    // ثبت: اگر «نفرِ ۲…» هنوز دوستِ واقعی نیست، اول برایش طرف‌حساب می‌سازیم و دوباره ادامه می‌دهیم.
    LaunchedEffect(saveRequest) {
        if (saveRequest == 0) return@LaunchedEffect
        val pending = participants.value.indexOfFirst { (it.counterpartyId ?: 0L) < 0L }
        if (pending >= 0) {
            onCreateCounterparty(participants.value[pending].name) { newId ->
                participants.value = participants.value.toMutableList().also {
                    it[pending] = it[pending].copy(counterpartyId = newId)
                }
                saveRequest++
            }
            return@LaunchedEffect
        }

                    error = when {
                        totalAmount <= 0 -> "مبلغِ کل رو وارد کن"
                        participants.value.size < 2 -> "حداقل یه شرکت‌کننده‌ی دیگه اضافه کن"
                        method == DangMethod.PERCENTAGE &&
                            participants.value.sumOf { it.percentageText.toDoubleOrNull() ?: 0.0 }.toInt() != 100 ->
                            "جمعِ درصدها باید ۱۰۰٪ بشه"
                        method == DangMethod.CUSTOM &&
                            participants.value.sumOf { it.customAmountText.toLongOrNull()?.toDouble() ?: 0.0 } != totalAmount ->
                            "جمعِ مبلغ‌های دلخواه باید با مبلغِ کل برابر باشه"
                        method == DangMethod.ITEMIZED &&
                            items.value.sumOf { it.amountText.toLongOrNull()?.toDouble() ?: 0.0 } != totalAmount ->
                            "جمعِ قلم‌ها باید با مبلغِ کل برابر باشه"
                        method == DangMethod.ITEMIZED && items.value.any { it.sharedByIndices.isEmpty() } ->
                            "هر قلم باید حداقل یه شرکت‌کننده داشته باشه"
                        else -> null
                    }
                    if (error != null) return@LaunchedEffect

                    val participantInputs: List<DangParticipantInput>
                    val itemInputs: List<DangItemInput>
                    when (method) {
                        DangMethod.EQUAL -> {
                            val shares = equalDangShares(totalAmount, participants.value.size)
                            participantInputs = participants.value.mapIndexed { i, p ->
                                DangParticipantInput(p.counterpartyId, shares[i])
                            }
                            itemInputs = emptyList()
                        }
                        DangMethod.PERCENTAGE -> {
                            val pcts = participants.value.map { it.percentageText.toDoubleOrNull() ?: 0.0 }
                            val shares = percentageDangShares(totalAmount, pcts)
                            participantInputs = participants.value.mapIndexed { i, p ->
                                DangParticipantInput(p.counterpartyId, shares[i], pcts[i])
                            }
                            itemInputs = emptyList()
                        }
                        DangMethod.CUSTOM -> {
                            participantInputs = participants.value.map { p ->
                                DangParticipantInput(p.counterpartyId, p.customAmountText.toLongOrNull()?.toDouble() ?: 0.0)
                            }
                            itemInputs = emptyList()
                        }
                        DangMethod.ITEMIZED -> {
                            // سهمِ هرکس = جمعِ سهمش از همه‌ی قلم‌هایی که توشون شریکه.
                            val totals = DoubleArray(participants.value.size)
                            val itemInputsBuilt = items.value.map { item ->
                                val amount = item.amountText.toLongOrNull()?.toDouble() ?: 0.0
                                val indices = item.sharedByIndices.sorted()
                                val shares = equalDangShares(amount, indices.size)
                                val shareInputs = indices.mapIndexed { i, pIndex ->
                                    totals[pIndex] += shares[i]
                                    DangItemShareInput(participantIndex = pIndex, shareAmount = shares[i])
                                }
                                DangItemInput(item.description.trim(), amount, shareInputs)
                            }
                            participantInputs = participants.value.mapIndexed { i, p ->
                                DangParticipantInput(p.counterpartyId, totals[i])
                            }
                            itemInputs = itemInputsBuilt
                        }
                    }
                    // فرم تومان است، دیتابیس ریال - تبدیل فقط همین‌جا.
                    onSave(
                        title.trim().ifEmpty { "دنگ" }, method, totalAmount * ir.sadteam.loancalc.ui.jibak.unitDivD, year, month, day, isEventMode,
                        participantInputs.map { it.copy(shareAmount = it.shareAmount * ir.sadteam.loancalc.ui.jibak.unitDivD) },
                        itemInputs.map { item ->
                            item.copy(amount = item.amount * ir.sadteam.loancalc.ui.jibak.unitDivD, shares = item.shares.map { it.copy(shareAmount = it.shareAmount * ir.sadteam.loancalc.ui.jibak.unitDivD) })
                        },
                    )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                Text(if (existing != null) "ویرایشِ دنگ" else "دنگِ جدید", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
        if (expenses.isNotEmpty() && existing == null) {
            item {
                DangFormCard(icon = Icons.Filled.ReceiptLong, title = "از یک خرجِ ثبت‌شده") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (pickedExpenseId == 0L) "مبلغ و تاریخ خودکار پر می‌شود و در گزارش فقط سهمِ خودت حساب می‌شود."
                            else "✓ خرج انتخاب شد",
                            color = if (pickedExpenseId == 0L) AppMuted else AppPrimary, fontSize = 11.5.sp,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { showExpensePicker = true }) {
                            Text(if (pickedExpenseId == 0L) "انتخاب" else "تغییر")
                        }
                    }
                }
            }
        }
        // عنوان + برچسب‌های سریع (طرحِ کاربر).
        item {
            DangFormCard(icon = Icons.Filled.Edit, title = "عنوان") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(40) },
                    placeholder = { Text("مثلاً: شامِ جمعه") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = if (title.isNotEmpty()) ({
                        IconButton(onClick = { title = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "پاک کردن", tint = AppMuted, modifier = Modifier.size(18.dp))
                        }
                    }) else null,
                    colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("رستوران", "کافه", "خرید", "تفریح", "سفر", "سایر").forEach { tag ->
                        AppChip(label = tag, selected = title == tag, onClick = { title = tag })
                    }
                }
            }
        }
        item {
            DangFormCard(icon = Icons.Filled.Payments, title = "مبلغِ کل") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = cleanNum(it) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        suffix = { Text(ir.sadteam.loancalc.ui.jibak.unitFa(), color = AppMuted, fontSize = 13.sp) },
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppPrimaryPill)
                            .pressScaleClickable { showCalc = true },
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Calculate, contentDescription = "ماشین‌حساب", tint = AppPrimary) }
                }
                if (totalAmount > 0) {
                    Text(
                        ir.sadteam.loancalc.ui.jibak.numberToWordsTomanFa(totalAmount) + " تومان",
                        color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        item {
            DangFormCard(icon = Icons.Filled.Event, title = "تاریخ") {
                InlineJalaliDateRow(year = year, month = month, day = day, onDateChange = { y, m, d -> year = y; month = m; day = d })
            }
        }
        // شرکت‌کننده‌ها - آواتار، سهمِ هرکس، و دکمه‌ی خط‌چینِ «افزودن».
        item {
            DangFormCard(icon = Icons.Filled.Groups, title = "شرکت‌کننده‌ها · ${toFa(participants.value.size)} نفر") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val equalShares = if (method == DangMethod.EQUAL && totalAmount > 0) equalDangShares(totalAmount, participants.value.size) else null
                    participants.value.forEachIndexed { index, draft ->
                        DangParticipantRow(
                            draft = draft,
                            method = method,
                            equalShare = equalShares?.getOrNull(index),
                            removable = draft.counterpartyId != null && participants.value.size > 2,
                            onPickFriend = if ((draft.counterpartyId ?: 0L) < 0L) ({ pickForIndex = index }) else null,
                            onPercentageChange = { text ->
                                participants.value = participants.value.toMutableList().also {
                                    it[index] = it[index].copy(percentageText = text)
                                }
                            },
                            onCustomAmountChange = { text ->
                                participants.value = participants.value.toMutableList().also {
                                    it[index] = it[index].copy(customAmountText = text)
                                }
                            },
                            onRemove = {
                                participants.value = participants.value.toMutableList().also { it.removeAt(index) }
                                // اگه شرکت‌کننده‌ای حذف شد، چک‌باکسِ قلم‌های ITEMIZED هم باید هماهنگ بشه.
                                items.value = items.value.map { it.copy(sharedByIndices = it.sharedByIndices.filter { i -> i != index }.map { i -> if (i > index) i - 1 else i }.toSet()) }
                            },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .dashedBorder(14.dp, AppPrimary.copy(alpha = 0.6f))
                            .pressScaleClickable { if (participants.value.size < 20) showAddParticipant = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ افزودنِ شرکت‌کننده", color = AppPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (method == DangMethod.PERCENTAGE) {
                        val sum = participants.value.sumOf { it.percentageText.toDoubleOrNull() ?: 0.0 }
                        Text(
                            "جمعِ درصدها: ${toFa(sum.toInt())}٪ (باید ۱۰۰٪ باشه)",
                            color = if (sum.toInt() == 100) AppPrimary else AppDanger,
                            fontSize = 11.sp,
                        )
                    }
                    if (method == DangMethod.CUSTOM) {
                        val sum = participants.value.sumOf { it.customAmountText.toLongOrNull()?.toDouble() ?: 0.0 }
                        Text(
                            "جمعِ مبلغ‌ها: ${fmt(sum)} از ${fmt(totalAmount)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                            color = if (sum == totalAmount) AppPrimary else AppDanger,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
        item {
            if (!(showAdvanced || method != DangMethod.EQUAL || isEventMode)) {
                TextButton(onClick = { showAdvanced = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("تقسیمِ پیشرفته (درصدی، مبلغِ دلخواه، قلم‌به‌قلم، مهمانی)")
                }
            }
        }
        if (showAdvanced || method != DangMethod.EQUAL || isEventMode) item {
            DangFormCard(icon = Icons.Filled.Tune, title = "روشِ تقسیم") {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DangMethod.entries.forEach { m ->
                        AppChip(label = m.label, selected = method == m, onClick = { method = m })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("حالتِ مهمانی/مناسبت", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("از میانگینِ ماهانه‌ی گزارش حذف می‌شه", color = AppMuted, fontSize = 11.sp)
                    }
                    Switch(checked = isEventMode, onCheckedChange = { isEventMode = it })
                }
            }
        }
        if (method == DangMethod.ITEMIZED) {
            item {
                DangFormCard(icon = Icons.Filled.ReceiptLong, title = "قلم‌های فاکتور") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.value.forEachIndexed { itemIndex, item ->
                            DangItemRow(
                                item = item,
                                participants = participants.value,
                                onDescriptionChange = { text ->
                                    items.value = items.value.toMutableList().also { it[itemIndex] = it[itemIndex].copy(description = text) }
                                },
                                onAmountChange = { text ->
                                    items.value = items.value.toMutableList().also { it[itemIndex] = it[itemIndex].copy(amountText = text) }
                                },
                                onToggleParticipant = { pIndex ->
                                    items.value = items.value.toMutableList().also {
                                        val cur = it[itemIndex].sharedByIndices
                                        it[itemIndex] = it[itemIndex].copy(sharedByIndices = if (pIndex in cur) cur - pIndex else cur + pIndex)
                                    }
                                },
                                onRemove = {
                                    items.value = items.value.toMutableList().also { it.removeAt(itemIndex) }
                                },
                            )
                        }
                        OutlinedButton(
                            onClick = { items.value = items.value + DangItemDraft(sharedByIndices = participants.value.indices.toSet()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("+ افزودنِ قلم")
                        }
                        val itemsSum = items.value.sumOf { it.amountText.toLongOrNull()?.toDouble() ?: 0.0 }
                        Text(
                            "جمعِ قلم‌ها: ${fmt(itemsSum)} از ${fmt(totalAmount)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                            color = if (itemsSum == totalAmount) AppPrimary else AppDanger,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
        if (error != null) {
            item { Text(text = error ?: "", color = AppDanger, fontSize = 12.sp) }
        }
        item {
            GradientButton(
                onClick = { saveRequest++ },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (existing != null) "ذخیره‌ی تغییرات" else "ثبتِ دنگ")
            }
        }
    }
    if (showCalc) {
        ir.sadteam.loancalc.ui.components.MiniCalculatorDialog(
            initial = totalText,
            onResult = { totalText = it.toString() },
            onDismiss = { showCalc = false },
        )
    }
    if (pickForIndex >= 0) {
        CounterpartyPickerDialog(
            counterparties = counterparties.filter { c -> participants.value.none { it.counterpartyId == c.id } },
            onSelect = { c ->
                val i = pickForIndex
                if (i in participants.value.indices) {
                    participants.value = participants.value.toMutableList().also { it[i] = it[i].copy(counterpartyId = c.id, name = c.name) }
                }
                pickForIndex = -1
            },
            onCreateNew = { name ->
                onCreateCounterparty(name) { newId ->
                    val i = pickForIndex
                    if (i in participants.value.indices) {
                        participants.value = participants.value.toMutableList().also { it[i] = it[i].copy(counterpartyId = newId, name = name) }
                    }
                    pickForIndex = -1
                }
            },
            onDismiss = { pickForIndex = -1 },
        )
    }
    if (showAddParticipant) {
        CounterpartyPickerDialog(
            counterparties = counterparties.filter { c -> participants.value.none { it.counterpartyId == c.id } },
            onSelect = { c ->
                participants.value = participants.value + DangParticipantDraft(c.id, c.name)
                showAddParticipant = false
            },
            onCreateNew = { name ->
                onCreateCounterparty(name) { newId ->
                    participants.value = participants.value + DangParticipantDraft(newId, name)
                    showAddParticipant = false
                }
            },
            onDismiss = { showAddParticipant = false },
        )
    }
}

@Composable
private fun DangParticipantRow(
    draft: DangParticipantDraft,
    method: DangMethod,
    removable: Boolean,
    onPercentageChange: (String) -> Unit,
    onCustomAmountChange: (String) -> Unit,
    onRemove: () -> Unit,
    onPickFriend: (() -> Unit)? = null,
    equalShare: Double? = null,
) {
    val me = draft.counterpartyId == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ir.sadteam.loancalc.ui.theme.AppSurface2)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DangAvatar(draft.name, size = 36, me = me)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
                .then(if (onPickFriend != null) Modifier.clickable { onPickFriend() } else Modifier)
                .padding(vertical = 6.dp),
        ) {
            Text(
                if (me) "من" else draft.name,
                color = if (onPickFriend != null) AppPrimary else AppText,
                fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1,
            )
            if (onPickFriend != null) {
                // نفرِ ساختگی: لمس = انتخابِ دوستِ واقعی از فهرست.
                Text("لمس کن و اسمِ دوست را انتخاب کن", color = AppMuted, fontSize = 10.5.sp)
            } else if (equalShare != null) {
                Text("سهم: ${fmt(equalShare)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppMuted, fontSize = 11.sp)
            }
        }
        when (method) {
            DangMethod.PERCENTAGE -> {
                Ltr {
                    OutlinedTextField(
                        value = draft.percentageText,
                        onValueChange = onPercentageChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.size(width = 76.dp, height = 52.dp),
                        singleLine = true,
                        suffix = { Text("٪") }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                }
            }
            DangMethod.CUSTOM -> {
                Ltr {
                    OutlinedTextField(
                        value = draft.customAmountText,
                        onValueChange = { onCustomAmountChange(cleanNum(it)) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.size(width = 120.dp, height = 52.dp),
                        singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                }
            }
            else -> Unit
        }
        if (removable) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "حذف", tint = AppMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** کارتِ فرم با سرآیندِ آیکون‌دار (طرحِ «دنگِ جدید»). */
@Composable
private fun DangFormCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: @Composable () -> Unit) {
    AppCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(17.dp)) }
                Text(title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
            content()
        }
    }
}

@Composable
private fun DangItemRow(
    item: DangItemDraft,
    participants: List<DangParticipantDraft>,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onToggleParticipant: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppPrimaryPill, RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = item.description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("شرحِ قلم") },
                modifier = Modifier.weight(1f),
                singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            Ltr {
                OutlinedTextField(
                    value = item.amountText,
                    onValueChange = { onAmountChange(cleanNum(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.size(width = 110.dp, height = 56.dp).padding(start = 6.dp),
                    singleLine = true, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "حذفِ قلم", tint = AppDanger, modifier = Modifier.size(18.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            participants.forEachIndexed { index, p ->
                Row(
                    modifier = Modifier.padding(end = 8.dp).pressScaleClickable { onToggleParticipant(index) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = index in item.sharedByIndices, onCheckedChange = { onToggleParticipant(index) })
                    Text(p.name, color = AppMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

