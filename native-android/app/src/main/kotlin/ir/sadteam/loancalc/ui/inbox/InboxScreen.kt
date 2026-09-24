package ir.sadteam.loancalc.ui.inbox

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppTxIn
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppLabel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.ui.account.AccountDetailScreen
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **مرکزِ پیام‌ها - فریمِ `40a`.**
 *
 * قاعده‌ی مرکزیِ طرح **دو دسته‌ست، نه شش**: «اقدام‌دار» (تراکنشِ تشخیص‌داده‌شده و سررسیدِ وام)
 * حاشیه‌ی رنگی و ردیفِ دکمه داره و با کشیدن بسته نمی‌شه؛ بقیه «خبر»ن و فقط خونده می‌شن.
 */
@Composable
fun InboxScreen(onBack: () -> Unit, viewModel: InboxViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val sourceAccount by viewModel.sourceAccount.collectAsState()
    // اعلان و صفحهٔ «دارایی» باید دقیقاً از همان مسیرِ داده و ViewModel استفاده کنند؛
    // ساختنِ ViewModel تازه در دلِ دیالوگِ اعلان روی بعضی گوشی‌ها موقع بازشدنِ جزئیات کرش می‌کرد.
    val accountViewModel: AccountViewModel = hiltViewModel()
    // پیامی که کاربر «منبعش» را لمس کرده - متنِ خامِ همان پیامک/اعلان را نشان می‌دهیم.
    var sourceOf by remember { mutableStateOf<InboxMessageEntity?>(null) }
    val actionable = messages.filter {
        InboxMessageEntity.Kind.isActionable(it.kind) &&
            it.actionState == InboxMessageEntity.ActionState.OPEN
    }
    val news = messages.filterNot {
        InboxMessageEntity.Kind.isActionable(it.kind) &&
            it.actionState == InboxMessageEntity.ActionState.OPEN
    }

    // جستجو و فیلترِ سربرگ (طرحِ ChatGPT، ۳ مهر) - هر دو فقط روی همین فهرست کار می‌کنند.
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(InboxFilter.ALL) }
    var filterMenu by remember { mutableStateOf(false) }
    fun visible(m: InboxMessageEntity): Boolean {
        val q = query.trim()
        val textOk = q.isEmpty() || m.title.contains(q) || m.body.contains(q) || (m.sourceLabel?.contains(q) == true)
        val kindOk = when (filter) {
            InboxFilter.ALL -> true
            InboxFilter.TRANSACTIONS -> m.kind == InboxMessageEntity.Kind.DETECTED_TX
            InboxFilter.REMINDERS -> m.kind == InboxMessageEntity.Kind.LOAN_DUE
            InboxFilter.UNREAD -> m.readAt == null
        }
        return textOk && kindOk
    }
    val shownNews = news.filter(::visible)

    // 🚨 `statusBarsPadding`: عنوانِ قبلی زیرِ نوارِ وضعیتِ گوشی می‌رفت (اسکرین‌شاتِ کاربر).
    Column(modifier = Modifier.fillMaxSize().background(AppBg).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "پیام‌ها",
                color = AppText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Box {
                HeaderCircle(Icons.Filled.Tune, "فیلتر", active = filter != InboxFilter.ALL) { filterMenu = true }
                DropdownMenu(expanded = filterMenu, onDismissRequest = { filterMenu = false }) {
                    InboxFilter.entries.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.label, fontWeight = if (f == filter) FontWeight.Black else FontWeight.Normal) },
                            onClick = { filter = f; filterMenu = false },
                        )
                    }
                }
            }
            Box(modifier = Modifier.width(8.dp))
            HeaderCircle(Icons.Filled.Search, "جستجو", active = searching) {
                searching = !searching
                if (!searching) query = ""
            }
        }
        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو در پیام‌ها…") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }

        if (messages.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.NotificationsNone,
                title = "پیامی نداری",
                description = "هر تراکنشی که خودکار تشخیص داده بشه و هر اعلانی که برنامه می‌فرسته اینجا می‌مونه.",
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (actionable.isNotEmpty()) {
                item { SectionLabel("در انتظارِ تو · ${toFa(actionable.size)}") }
                items(actionable, key = { it.id }) { message ->
                    ActionableCard(
                        message = message,
                        onConfirm = { viewModel.confirmTransaction(message) },
                        onReject = { viewModel.rejectTransaction(message) },
                        onShowSource = { sourceOf = message },
                    )
                }
            }
            if (news.isNotEmpty()) {
                item {
                    HistoryHeaderCard(
                        // ⚠️ «همه خوانده شد» عمداً فقط خبرها رو می‌خونه - تاییدِ ضمنیِ هیچ
                        // تراکنشی نیست (قاعده‌ی صریحِ طرح).
                        onMarkAllRead = if (news.any { it.readAt == null }) {
                            { viewModel.markAllNewsRead() }
                        } else {
                            null
                        },
                    )
                }
                if (shownNews.isEmpty()) {
                    item {
                        Text(
                            "پیامی با این جستجو یا فیلتر پیدا نشد.",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(shownNews, key = { it.id }) { message ->
                    NewsCard(
                        message = message,
                        onClick = {
                            viewModel.markRead(message.id)
                            if (message.sourceText != null) sourceOf = message
                        },
                    )
                }
            }
        }
    }

    sourceOf?.let { message ->
        SourceDialog(
            message = message,
            onDismiss = { sourceOf = null },
            onOpenAccount = {
                viewModel.openSourceAccount(message)
                sourceOf = null
            },
        )
    }

    // «رفتن به منبع» - همان حساب‌کتابی که تراکنش رویش نشسته؛ خودِ تراکنش در فهرستش هست و
    // با لمس قابلِ ویرایش است.
    sourceAccount?.let { account ->
        key(account.id) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AccountDetailScreen(
                    account = account,
                    onBack = { viewModel.closeSourceAccount() },
                    viewModel = accountViewModel,
                )
            }
        }
    }
}

/**
 * **متنِ خامِ منبع.** تنها راهی که کاربر می‌تواند تشخیصِ غلط را ردیابی کند: عیناً همان
 * پیامک/اعلانی که به این تراکنش تعبیر شده، بی هیچ خلاصه‌سازی.
 */
@Composable
private fun SourceDialog(
    message: InboxMessageEntity,
    onDismiss: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                message.sourceLabel ?: "منبعِ نامشخص",
                color = AppText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                message.sourceText ?: "متنِ اصلیِ این پیام ذخیره نشده - پیام‌های قدیمی متنِ خام ندارند.",
                color = AppMuted,
                fontSize = 11.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (message.refId != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppPrimary)
                            .pressScaleClickable(onClick = onOpenAccount)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("رفتن به حساب‌کتاب", color = AppBg, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .border(1.5.dp, AppLine, RoundedCornerShape(999.dp))
                        .pressScaleClickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("بستن", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
    )
}

/** کارتِ اقدام‌دار - حاشیه‌ی رنگی + ردیفِ دکمه. با کشیدن بسته نمی‌شه (طرح صریحاً می‌گه). */
@Composable
private fun ActionableCard(
    message: InboxMessageEntity,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onShowSource: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppPrimaryPill)
            .border(2.dp, AppPrimary, shape)
            .padding(14.dp),
    ) {
        Text(message.title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
        Text(
            message.body,
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        // خطِ منبع - «این از کجا آمد؟». لمسش متنِ خامِ همان پیامک/اعلان را باز می‌کند.
        message.sourceLabel?.let { label ->
            Text(
                "$label · دیدنِ متن",
                color = AppPrimaryDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .pressScaleClickable(onClick = onShowSource)
                    .padding(vertical = 4.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppPrimary)
                    .pressScaleClickable(onClick = onConfirm)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = AppBg, modifier = Modifier.size(15.dp))
                    Text("تایید", color = AppBg, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.5.dp, AppLine, RoundedCornerShape(999.dp))
                    .pressScaleClickable(onClick = onReject)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("رد", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private enum class InboxFilter(val label: String) {
    ALL("همه"),
    TRANSACTIONS("تراکنش‌ها"),
    REMINDERS("یادآوریِ قسط"),
    UNREAD("خوانده‌نشده"),
}

/** دکمه‌ی گردِ سفیدِ سربرگ (جستجو/فیلتر) - هدفِ لمسیِ ۴۸. */
@Composable
private fun HeaderCircle(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(4.dp, CircleShape, ambientColor = AppPrimary.copy(alpha = 0.15f), spotColor = AppPrimary.copy(alpha = 0.15f))
            .clip(CircleShape)
            .background(if (active) AppPrimaryPill else AppSurface)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (active) AppPrimary else AppText, modifier = Modifier.size(22.dp))
    }
}

/** کارتِ «پیشینه» - زنگ و یک جمله؛ «همه خوانده شد» هم این‌جاست. */
@Composable
private fun HistoryHeaderCard(onMarkAllRead: (() -> Unit)?) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(shape)
            .background(AppPrimaryPill.copy(alpha = 0.5f))
            .border(1.dp, AppPrimary.copy(alpha = 0.18f), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text("پیشینه", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(
                "همه‌ی پیام‌ها و یادآوری‌های تو این‌جا نمایش داده می‌شود.",
                color = AppMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (onMarkAllRead != null) {
            Text(
                "همه خوانده شد",
                color = AppPrimaryDim,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .pressScaleClickable(onClick = onMarkAllRead)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
    }
}

/** «امروز ۱۹:۱۲» / «دیروز ۰۸:۱۰» / «۱۲ مهر» - زمانِ رسیدنِ پیام. */
private fun timeLabel(millis: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val hm = String.format(java.util.Locale.US, "%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
    val today = (System.currentTimeMillis() + java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis())) / 86_400_000L
    val local = (millis + java.util.TimeZone.getDefault().getOffset(millis)) / 86_400_000L
    return when (today - local) {
        0L -> "امروز ${toFa(hm)}"
        1L -> "دیروز ${toFa(hm)}"
        else -> {
            val d = JalaliCalendar.fromGregorian(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
            )
            "${toFa(d.d)} ${persianMonthName(d.m)}"
        }
    }
}

/**
 * کارتِ خبر (طرحِ ChatGPT): نشانِ نوع (برداشت قرمز، واریز سبز، یادآوریِ قسط آبی)، عنوان و
 * متن، قرصِ منبع، زمان و فلش. نقطه‌ی کنارِ عنوان یعنی خوانده‌نشده.
 */
@Composable
private fun NewsCard(message: InboxMessageEntity, onClick: () -> Unit) {
    val isTx = message.kind == InboxMessageEntity.Kind.DETECTED_TX
    val isDeposit = isTx && message.title.contains("واریز")
    val isDue = message.kind == InboxMessageEntity.Kind.LOAN_DUE
    val (tint, fill, icon) = when {
        isDeposit -> Triple(AppTxIn, AppTxIn.copy(alpha = 0.14f), Icons.Filled.AddCircle)
        isTx -> Triple(AppDanger, AppDangerPill, Icons.Filled.RemoveCircle)
        isDue -> Triple(AppInfo, AppInfoPill, Icons.Filled.CalendarMonth)
        else -> Triple(AppPrimary, AppPrimaryPill, Icons.Filled.NotificationsNone)
    }
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = AppPrimary.copy(alpha = 0.10f), spotColor = AppPrimary.copy(alpha = 0.10f))
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(scale = 0.99f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (message.readAt == null) {
                    Box(modifier = Modifier.padding(end = 6.dp).size(7.dp).clip(CircleShape).background(AppPrimary))
                }
                Text(
                    message.title,
                    color = AppText,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (isDue) Icons.Filled.Event else Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.padding(start = 6.dp).size(14.dp),
                )
                Text(
                    timeLabel(message.createdAt),
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                message.body,
                color = AppMuted,
                fontSize = 11.5.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 5.dp),
            )
            message.sourceLabel?.let { label ->
                Text(
                    label,
                    color = AppPrimaryDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppSurface2)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Icon(
            Icons.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AppLabel,
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 6.dp).size(20.dp),
        )
    }
}
