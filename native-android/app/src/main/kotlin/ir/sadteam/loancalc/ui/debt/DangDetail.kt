package ir.sadteam.loancalc.ui.debt

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.DangMethod
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.DangEventEntity
import ir.sadteam.loancalc.data.db.DangParticipantEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppTxIn
import ir.sadteam.loancalc.ui.theme.AppWarning
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import kotlinx.coroutines.launch

/*
 * فهرست و جزئیاتِ دنگ - بازطراحیِ ۱۶ مهر (طرحِ تصویریِ کاربر، بی تصویرِ بزرگِ کیف/سکه).
 *
 * معنای تیک‌ها (خواسته‌ی صریحِ کاربر):
 * - «خودم» تیک ندارد؛ فقط «سهمِ من از این دنگ» نشان داده می‌شود.
 * - تیکِ هر دوست = سهمش را داده (پولش به حساب برمی‌گردد).
 * - وقتی همه‌ی دوست‌ها تیک خوردند دنگ **خودش** تسویه می‌شود؛ کلیدِ دستیِ «تسویه‌شده» حذف شد.
 */

private fun dangMoney(amount: Double): String =
    "${fmt(amount / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}"

private fun dangDate(event: DangEventEntity): String =
    "${toFa(event.day)} ${ir.sadteam.loancalc.ui.jibak.faMonthName(event.month)} ${toFa(event.year)}"

/** آواتارِ حرفِ اول - رنگ از روی نام ثابت می‌ماند. */
@Composable
internal fun DangAvatar(name: String, size: Int = 40, me: Boolean = false) {
    val tones = listOf(AppPrimary, AppInfo, AppPurple, AppWarning, AppTxIn)
    val tone = if (me) AppPrimary else tones[(name.hashCode() and 0x7fffffff) % tones.size]
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(tone.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (me) "من" else name.trim().take(1).ifEmpty { "؟" },
            color = tone,
            fontSize = (size * 0.38f).sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/** لیستِ دنگ‌ها - باز‌ها بالا، تسویه‌شده‌ها جدا و کم‌رنگ پایین (مثلِ وام‌های تسویه‌شده، ولی خلوت). */
@Composable
fun DangListScreen(
    events: List<DangEventEntity>,
    participants: List<DangParticipantEntity>,
    onBack: () -> Unit,
    onOpen: (DangEventEntity) -> Unit,
    onAddNew: () -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    val byEvent = remember(participants) { participants.groupBy { it.eventId } }
    val (open, settled) = remember(events) { events.partition { !it.settled } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Text("دنگ‌ها", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
        if (events.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Celebration,
                    title = "هنوز دنگی ثبت نشده",
                    description = "هزینه‌ی یه مهمونی یا خریدِ گروهی رو اینجا بینِ چند نفر تقسیم کن.",
                    actionLabel = "+ دنگِ جدید",
                    onAction = onAddNew,
                )
            }
            return@LazyColumn
        }
        item {
            GradientButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) { Text("+ دنگِ جدید") }
        }
        items(open, key = { it.id }) { event ->
            val others = byEvent[event.id].orEmpty().filter { it.counterpartyId != null }
            val paid = others.count { it.settled }
            AppCard(modifier = Modifier.pressScaleClickable { onOpen(event) }) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(DangPurpleTone.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Celebration, contentDescription = null, tint = DangPurpleTone, modifier = Modifier.size(22.dp)) }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(event.title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Text(
                            "${dangDate(event)} · ${toFa(paid)} از ${toFa(others.size)} نفر داده‌اند",
                            color = AppMuted, fontSize = 11.5.sp, maxLines = 1,
                        )
                        if (others.isNotEmpty()) DangProgress(paid.toFloat() / others.size, Modifier.padding(top = 6.dp))
                    }
                    Text(
                        maskIfPrivate(privacyMode, dangMoney(event.totalAmount)),
                        color = AppPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        if (settled.isNotEmpty()) {
            item {
                Text(
                    "تسویه‌شده‌ها · ${toFa(settled.size)}",
                    color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }
            items(settled, key = { it.id }) { event ->
                AppCard(variant = AppCardVariant.DONE, dimContent = false, modifier = Modifier.pressScaleClickable { onOpen(event) }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(AppTxIn.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Check, contentDescription = "تسویه شد", tint = AppTxIn, modifier = Modifier.size(18.dp)) }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(event.title, color = AppText.copy(alpha = 0.75f), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${dangDate(event)} · تسویه شد", color = AppMuted, fontSize = 11.sp)
                        }
                        Text(
                            maskIfPrivate(privacyMode, dangMoney(event.totalAmount)),
                            color = AppMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DangProgress(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(99.dp)).background(AppLine)) {
        Box(
            modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(5.dp)
                .clip(RoundedCornerShape(99.dp)).background(AppTxIn),
        )
    }
}

/** جزئیاتِ یه رویدادِ دنگ. */
@Composable
fun DangDetailScreen(
    event: DangEventEntity,
    participants: List<DangParticipantEntity>,
    counterpartyNameFor: (Long?) -> String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSettleAll: () -> Unit,
    onOpenCounterparty: (Long) -> Unit,
    onToggleParticipantSettled: (DangParticipantEntity, Boolean) -> Unit,
) {
    val privacyMode = LocalPrivacyMode.current
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var confirmUnpay by remember { mutableStateOf<DangParticipantEntity?>(null) }
    val me = participants.firstOrNull { it.counterpartyId == null }
    val others = participants.filter { it.counterpartyId != null }
    val paidCount = others.count { it.settled }
    val waiting = others.size - paidCount
    val received = others.filter { it.settled }.sumOf { it.shareAmount }
    val othersTotal = others.sumOf { it.shareAmount }

    // رسیدِ عکسی - ضبط با graphicsLayer و ذخیره با SAF (همان روشِ قبلی).
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiptLayer = rememberGraphicsLayer()
    var receiptBack by rememberSaveable { mutableStateOf(false) }
    var pendingReceiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val saveReceiptLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bitmap = pendingReceiptBitmap
        if (uri != null && bitmap != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Text(event.title, color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایشِ دنگ", tint = AppMuted)
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            pendingReceiptBitmap = receiptLayer.toImageBitmap().asAndroidBitmap()
                            saveReceiptLauncher.launch("dang-${event.title}.png")
                        }
                    },
                ) { Icon(Icons.Filled.Share, contentDescription = "ذخیره‌ی رسیدِ عکسی", tint = AppMuted) }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذفِ دنگ", tint = AppDanger)
                }
            }
        }
        // سرِ صفحه: مبلغ، روش، تاریخ و وضعیت - با توضیحِ اینکه تسویه چطور اتفاق می‌افتد.
        item {
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(DangPurpleTone.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Celebration, contentDescription = null, tint = DangPurpleTone, modifier = Modifier.size(24.dp)) }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("مبلغِ کل", color = AppMuted, fontSize = 11.sp)
                            Text(
                                maskIfPrivate(privacyMode, dangMoney(event.totalAmount)),
                                color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black,
                            )
                        }
                        val done = event.settled
                        Text(
                            if (done) "✓ تسویه شد" else "در انتظارِ ${toFa(waiting)} نفر",
                            color = if (done) AppTxIn else AppWarningInk,
                            fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (done) AppTxIn.copy(alpha = 0.14f) else AppWarningPill)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DangInfoCell("روشِ تقسیم", DangMethod.valueOf(event.method).label, Modifier.weight(1f))
                        DangInfoCell("تاریخ", dangDate(event), Modifier.weight(1f))
                        DangInfoCell("نفرات", "${toFa(participants.size)} نفر", Modifier.weight(1f))
                    }
                    if (others.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("دریافت‌شده", color = AppMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text(
                                maskIfPrivate(privacyMode, "${dangMoney(received)} از ${dangMoney(othersTotal)}"),
                                color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                            )
                        }
                        DangProgress(if (othersTotal > 0) (received / othersTotal).toFloat() else 0f, Modifier.padding(top = 6.dp))
                    }
                    Text(
                        if (event.settled) "همه سهمشان را داده‌اند؛ این دنگ بسته شد."
                        else "هر کس سهمش را داد، دایره‌ی کنارِ اسمش را بزن. وقتی همه تیک بخورند دنگ خودش تسویه می‌شود.",
                        color = AppMuted, fontSize = 11.sp, lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
        if (me != null) {
            item {
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        DangAvatar("من", me = true)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("سهمِ من از این دنگ", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("فقط همین در گزارشِ خرج‌هایت حساب می‌شود", color = AppMuted, fontSize = 11.sp)
                        }
                        Text(
                            maskIfPrivate(privacyMode, dangMoney(me.shareAmount)),
                            color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
        if (others.isNotEmpty()) {
            item {
                Text(
                    "سهمِ دوستان",
                    color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
        }
        items(others, key = { it.id }) { participant ->
            val name = counterpartyNameFor(participant.counterpartyId)
            AppCard(
                variant = if (participant.settled) AppCardVariant.DONE else AppCardVariant.DEFAULT,
                dimContent = false,
                modifier = Modifier.pressScaleClickable { participant.counterpartyId?.let(onOpenCounterparty) },
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DangAvatar(name)
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(name, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            if (participant.settled) "سهمش را داد ✓" else "هنوز نداده",
                            color = if (participant.settled) AppTxIn else AppMuted,
                            fontSize = 11.sp, fontWeight = if (participant.settled) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    Text(
                        maskIfPrivate(privacyMode, dangMoney(participant.shareAmount)),
                        color = if (participant.settled) AppMuted else AppText,
                        fontSize = 14.sp, fontWeight = FontWeight.Black,
                    )
                    // دایره‌ی «داد» - هدفِ لمسیِ ۴۴.
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(44.dp)
                            .pressScaleClickable {
                                if (participant.settled) confirmUnpay = participant
                                else onToggleParticipantSettled(participant, true)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (participant.settled) AppTxIn else Color.Transparent)
                                .border(2.dp, if (participant.settled) AppTxIn else AppLine, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (participant.settled) {
                                Icon(Icons.Filled.Check, contentDescription = "داد", tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "صفحه‌ی ${name}", tint = AppMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
        if (waiting > 1) {
            item {
                OutlinedButton(onClick = onSettleAll, modifier = Modifier.fillMaxWidth()) {
                    Text("همه سهمشان را دادند")
                }
            }
        }
        item {
            Text(
                "رسید برای فرستادن (لمس = پشت/رو)",
                color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )
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
    confirmUnpay?.let { p ->
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { confirmUnpay = null },
            title = { Text("برداشتنِ تیک؟") },
            text = { Text("یعنی «${counterpartyNameFor(p.counterpartyId)}» هنوز سهمش را نداده. واریزی که برایش در حساب ثبت شده بود هم برمی‌گردد.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onToggleParticipantSettled(p, false); confirmUnpay = null }) { Text("بله، بردار") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirmUnpay = null }) { Text("انصراف") } },
        )
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

@Composable
private fun DangInfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(AppSurface2).padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        Text(label, color = AppMuted, fontSize = 10.sp)
        Text(value, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

internal val DangPurpleTone = Color(0xFF7C4DDB)
