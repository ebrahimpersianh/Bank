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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/** لیستِ رویدادهای دنگ - فریمِ `22c`. */
@Composable
fun DangListScreen(
    events: List<DangEventEntity>,
    onBack: () -> Unit,
    onOpen: (DangEventEntity) -> Unit,
    onAddNew: () -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("دنگ‌ها", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            GradientButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
                Text("+ دنگِ جدید")
            }
        }
        if (events.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Celebration,
                    title = "هنوز دنگی ثبت نشده",
                    description = "هزینه‌ی یه مهمونی یا خریدِ گروهی رو اینجا بینِ چند نفر تقسیم کن.",
                )
            }
        } else {
            items(events, key = { it.id }) { event ->
                AppCard(modifier = Modifier.pressScaleClickable { onOpen(event) }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                DangMethod.valueOf(event.method).label +
                                    (if (event.isEventMode) " · مهمانی" else "") +
                                    (if (event.settled) " · تسویه‌شده" else ""),
                                color = AppMuted,
                                fontSize = 11.5.sp,
                            )
                        }
                        Text(
                            maskIfPrivate(privacyMode, "${fmt(event.totalAmount)} ریال"),
                            color = if (event.settled) AppMuted else AppPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

/** جزئیاتِ یه رویدادِ دنگ - سهمِ هرکس + تسویه‌ی تکی/کلی. */
@Composable
fun DangDetailScreen(
    event: DangEventEntity,
    participants: List<DangParticipantEntity>,
    counterpartyNameFor: (Long?) -> String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onToggleEventSettled: (Boolean) -> Unit,
    onToggleParticipantSettled: (DangParticipantEntity, Boolean) -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    // صورت‌حسابِ عکسی - جوابِ سوالِ ۹: تکنیکِ استانداردِ پروژه (rememberGraphicsLayer، رجوع کن
    // به ThemeReveal.kt) رو یه کارتِ ساده‌ی هم‌رنگِ اپ ضبط می‌کنه و با SAF (هم‌الگو با خروجیِ
    // PDF/اکسلِ چک) ذخیره می‌شه - طرحِ دقیقِ ظاهری لازم نبود (تاییدِ صریحِ طراح).
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiptLayer = rememberGraphicsLayer()
    // رو یا پشتِ رسید (`81e`). عکسِ خروجی همان رویی است که دیده می‌شود.
    var receiptBack by rememberSaveable { mutableStateOf(false) }
    var pendingReceiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val saveReceiptLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bitmap = pendingReceiptBitmap
        if (uri != null && bitmap != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }
        pendingReceiptBitmap = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(event.title, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        scope.launch {
                            pendingReceiptBitmap = receiptLayer.toImageBitmap().asAndroidBitmap()
                            saveReceiptLauncher.launch("dang-${event.title}.png")
                        }
                    },
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "ذخیره‌ی رسیدِ عکسی")
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذفِ دنگ", tint = AppDanger)
                }
            }
        }
        item {
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            maskIfPrivate(privacyMode, "${fmt(event.totalAmount)} ریال"),
                            color = AppText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${DangMethod.valueOf(event.method).label} · ${toFa(event.day)}/${toFa(event.month)}/${toFa(event.year)}" +
                                if (event.isEventMode) " · حالتِ مهمانی (خارج از میانگینِ گزارش)" else "",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("تسویه‌شده", color = AppMuted, fontSize = 11.sp)
                        Switch(checked = event.settled, onCheckedChange = onToggleEventSettled, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
        items(participants, key = { it.id }) { participant ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(counterpartyNameFor(participant.counterpartyId), color = AppText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            maskIfPrivate(privacyMode, "${fmt(participant.shareAmount)} ریال"),
                            color = AppMuted,
                            fontSize = 12.sp,
                        )
                    }
                    Checkbox(checked = participant.settled, onCheckedChange = { onToggleParticipantSettled(participant, it) })
                }
            }
        }
        item {
            DangReceiptCard(
                event = event,
                participants = participants,
                counterpartyNameFor = counterpartyNameFor,
                showBack = receiptBack,
                onFlip = { receiptBack = !receiptBack },
                modifier = Modifier.drawWithContent {
                    receiptLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(receiptLayer)
                },
            )
        }
    }
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "حذفِ دنگ",
            text = "دنگِ «${event.title}» و سهمِ همه‌ی شرکت‌کننده‌ها حذف بشه؟",
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

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
private fun DangReceiptCard(
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
                "${fmt(event.totalAmount)} ریال",
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
                            "${fmt(participant.shareAmount)} ریال",
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
    onCancel: () -> Unit,
    onCreateCounterparty: (name: String, onCreated: (Long) -> Unit) -> Unit,
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

    val participants = remember { mutableStateOf(listOf(DangParticipantDraft(counterpartyId = null, name = ME_NAME))) }
    val items = remember { mutableStateOf(listOf<DangItemDraft>()) }

    val totalAmount = totalText.toDoubleOrNull() ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("دنگِ جدید", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            AppCard(label = "عنوان") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("مثلاً: شامِ جمعه") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "مبلغِ کل") {
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
            }
        }
        item {
            AppCard(label = "تاریخ") {
                InlineJalaliDateRow(year = year, month = month, day = day, onDateChange = { y, m, d -> year = y; month = m; day = d })
            }
        }
        item {
            AppCard(label = "روش تقسیم") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DangMethod.entries.forEach { m ->
                            AppChip(label = m.label, selected = method == m, onClick = { method = m })
                        }
                    }
                }
            }
        }
        item {
            AppCard(label = "حالتِ مهمانی/مناسبت") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "پاکتِ موقت تا تسویه‌شدن - از میانگینِ ماهانه‌ی گزارش حذف می‌شه",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = isEventMode, onCheckedChange = { isEventMode = it })
                }
            }
        }
        item {
            AppCard(label = "شرکت‌کننده‌ها") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    participants.value.forEachIndexed { index, draft ->
                        DangParticipantRow(
                            draft = draft,
                            method = method,
                            removable = draft.counterpartyId != null,
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
                    if (showAddParticipant) {
                        // خودِ دیالوگ پایینِ کامپوزبل رندر می‌شه (نه اینجا) چون AlertDialog باید
                        // بیرونِ Column بشینه؛ این فقط پرچمِ نمایششه.
                    }
                    OutlinedButton(onClick = { showAddParticipant = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("+ افزودنِ شرکت‌کننده")
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
                            "جمعِ مبلغ‌ها: ${fmt(sum)} از ${fmt(totalAmount)} ریال",
                            color = if (sum == totalAmount) AppPrimary else AppDanger,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
        if (method == DangMethod.ITEMIZED) {
            item {
                AppCard(label = "قلم‌های فاکتور") {
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
                            "جمعِ قلم‌ها: ${fmt(itemsSum)} از ${fmt(totalAmount)} ریال",
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
                onClick = {
                    error = when {
                        title.trim().isEmpty() -> "عنوان رو وارد کن"
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
                    if (error != null) return@GradientButton

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
                    onSave(title.trim(), method, totalAmount, year, month, day, isEventMode, participantInputs, itemInputs)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ثبتِ دنگ")
            }
        }
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
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(draft.name, color = AppText, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
        when (method) {
            DangMethod.PERCENTAGE -> {
                Ltr {
                    OutlinedTextField(
                        value = draft.percentageText,
                        onValueChange = onPercentageChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.size(width = 76.dp, height = 52.dp),
                        singleLine = true,
                        suffix = { Text("٪") },
                    )
                }
            }
            DangMethod.CUSTOM -> {
                Ltr {
                    OutlinedTextField(
                        value = draft.customAmountText,
                        onValueChange = { onCustomAmountChange(cleanNum(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.size(width = 110.dp, height = 52.dp),
                        singleLine = true,
                    )
                }
            }
            else -> Unit
        }
        if (removable) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger, modifier = Modifier.size(18.dp))
            }
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
                singleLine = true,
            )
            Ltr {
                OutlinedTextField(
                    value = item.amountText,
                    onValueChange = { onAmountChange(cleanNum(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.size(width = 110.dp, height = 56.dp).padding(start = 6.dp),
                    singleLine = true,
                )
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
