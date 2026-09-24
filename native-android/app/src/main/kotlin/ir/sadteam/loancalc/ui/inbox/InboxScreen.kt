package ir.sadteam.loancalc.ui.inbox

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EditNote
import ir.sadteam.loancalc.ui.support.BugReportScreen
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppWarningInk
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
    var showAllPersonal by remember { mutableStateOf(false) }
    var showAllJibak by remember { mutableStateOf(false) }
    var showContact by remember { mutableStateOf(false) }
    fun visible(m: InboxMessageEntity): Boolean {
        val q = query.trim()
        val textOk = q.isEmpty() || m.title.contains(q) || m.body.contains(q) || (m.sourceLabel?.contains(q) == true)
        val kindOk = when (filter) {
            InboxFilter.ALL -> true
            InboxFilter.FINANCE -> m.kind == InboxMessageEntity.Kind.DETECTED_TX
            InboxFilter.REMINDERS -> isReminder(m)
            InboxFilter.UNREAD -> m.readAt == null
        }
        return textOk && kindOk
    }
    // دو بخشِ کاملاً جدا: پیام‌های شخصیِ کاربر، و اطلاعیه‌های عمومیِ جیبک از سرور.
    val personal = news.filter { it.kind != InboxMessageEntity.Kind.ANNOUNCEMENT && visible(it) }
    val jibak = news.filter { it.kind == InboxMessageEntity.Kind.ANNOUNCEMENT && visible(it) }

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
        // تب‌های نوع - همان فیلترِ منو، همیشه دیده می‌شوند.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InboxFilter.entries.forEach { f -> FilterTab(f, selected = f == filter) { filter = f } }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

            // ── پیام‌های شما ─────────────────────────────────────────────────────
            item(key = "personal") {
                MessageSection(
                    icon = Icons.Filled.Person,
                    title = "پیام‌های شما",
                    subtitle = "تراکنش‌ها، پرداخت‌ها، یادآوری‌ها و رویدادهای حساب شما",
                    count = personal.size,
                    green = false,
                    onMarkAllRead = if (personal.any { it.readAt == null }) {
                        { viewModel.markAllNewsRead() }
                    } else {
                        null
                    },
                ) {
                    if (personal.isEmpty()) {
                        EmptyLine(if (news.isEmpty()) "هنوز پیامی نداری." else "پیامی با این جستجو یا فیلتر پیدا نشد.")
                    }
                    val list = if (showAllPersonal) personal else personal.take(3)
                    list.forEach { message ->
                        NewsCard(
                            message = message,
                            onClick = {
                                viewModel.markRead(message.id)
                                if (message.sourceText != null) sourceOf = message
                            },
                        )
                    }
                    if (personal.size > 3) {
                        SeeAllButton(
                            if (showAllPersonal) "نمایشِ کمتر" else "مشاهده همه پیام‌های شما",
                            green = false,
                        ) { showAllPersonal = !showAllPersonal }
                    }
                }
            }

            // ── پیام‌های جیبک (اطلاعیه‌های عمومیِ سرور) ───────────────────────────
            item(key = "jibak") {
                MessageSection(
                    icon = Icons.Filled.Campaign,
                    title = "پیام‌های جیبک",
                    subtitle = "خبرها، به‌روزرسانی‌ها، اطلاعیه‌های مهم و قابلیت‌های جدید",
                    count = jibak.size,
                    green = true,
                    onMarkAllRead = null,
                ) {
                    if (jibak.isEmpty()) EmptyLine("فعلاً اطلاعیه‌ی تازه‌ای نیست.")
                    val list = if (showAllJibak) jibak else jibak.take(2)
                    list.forEach { message ->
                        AnnouncementCard(message) { viewModel.markRead(message.id) }
                    }
                    if (jibak.size > 2) {
                        SeeAllButton(
                            if (showAllJibak) "نمایشِ کمتر" else "مشاهده همه پیام‌های جیبک",
                            green = true,
                        ) { showAllJibak = !showAllJibak }
                    }
                }
            }

            // ── ارتباط با جیبک - پیامِ کاربر به ما؛ با پیام‌های دریافتی قاطی نمی‌شود ──
            item(key = "contact") { ContactCard { showContact = true } }
        }
    }

    // همان صفحه‌ی «گزارشِ مشکل / تماس» که در تنظیمات هم هست - نسخه‌ی دوم ساخته نشد.
    if (showContact) {
        Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
            BugReportScreen(onBack = { showContact = false })
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

// «مهم»ِ طرح ساخته نشد: برنامه قابلیتِ نشان‌کردنِ پیام ندارد؛ «خوانده‌نشده» واقعی است.
private enum class InboxFilter(val label: String, val icon: ImageVector) {
    ALL("همه", Icons.Filled.Inbox),
    FINANCE("مالی", Icons.Filled.CreditCard),
    REMINDERS("یادآوری‌ها", Icons.Filled.CalendarMonth),
    UNREAD("خوانده‌نشده", Icons.Filled.MarkEmailUnread),
}

/** «اعلانِ blu» → («اعلان»، «blu») · «پیامک از 3000123» → («پیامک»، «3000123»). */
private fun splitSource(label: String): Pair<String, String?> = when {
    label.startsWith("اعلانِ ") -> "اعلان" to label.removePrefix("اعلانِ ").trim().ifBlank { null }
    label.startsWith("پیامک از ") -> "پیامک" to label.removePrefix("پیامک از ").trim().ifBlank { null }
    else -> label to null
}

/** یادآوریِ قسط/چک/پرداخت - سیستم آن‌ها را با نوعِ SYSTEM و عنوانِ «یادآوریِ …» می‌سازد. */
private fun isReminder(m: InboxMessageEntity): Boolean =
    m.kind == InboxMessageEntity.Kind.LOAN_DUE ||
        (m.kind == InboxMessageEntity.Kind.SYSTEM && m.title.startsWith("یادآوری"))

@Composable
private fun FilterTab(f: InboxFilter, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) AppPrimary else AppSurface)
            .border(1.dp, if (selected) AppPrimary else AppLine, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(f.icon, contentDescription = null, tint = if (selected) Color.White else AppMuted, modifier = Modifier.size(17.dp))
        Text(
            f.label,
            color = if (selected) Color.White else AppText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/** قابِ هر بخش: آیکون، عنوان، زیرنویس و شمارنده؛ آبی برای پیام‌های شما، سبز برای جیبک. */
@Composable
private fun MessageSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    green: Boolean,
    onMarkAllRead: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tint = if (green) AppTxIn else AppPrimary
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tint.copy(alpha = 0.06f))
            .border(1.dp, tint.copy(alpha = 0.16f), shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(if (green) tint else tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (green) Color.White else tint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = AppMuted, fontSize = 10.5.sp, lineHeight = 16.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${toFa(count)} مورد",
                    color = if (green) tint else AppText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (green) tint.copy(alpha = 0.14f) else AppSurface2)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                )
                if (onMarkAllRead != null) {
                    Text(
                        "همه خوانده شد",
                        color = AppPrimaryDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.pressScaleClickable(onClick = onMarkAllRead).padding(top = 6.dp, bottom = 2.dp),
                    )
                }
            }
        }
        content()
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
    )
}

@Composable
private fun SeeAllButton(label: String, green: Boolean, onClick: () -> Unit) {
    val tint = if (green) AppTxIn else AppPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.10f))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = if (green) tint else AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** کارتِ اطلاعیه‌ی جیبک؛ نشان و برچسب از نوعِ اطلاعیه (`refId`). */
@Composable
private fun AnnouncementCard(message: InboxMessageEntity, onClick: () -> Unit) {
    val kind = message.refId
    val (icon, tint, chip) = when (kind) {
        "update" -> Triple(Icons.Filled.CardGiftcard, AppTxIn, "جدید")
        "outage" -> Triple(Icons.Filled.Warning, AppWarningInk, "اطلاعیه")
        "feature" -> Triple(Icons.Filled.AutoAwesome, AppPurple, "قابلیتِ تازه")
        else -> Triple(Icons.Filled.Campaign, AppPrimary, "اطلاعیه")
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(scale = 0.99f, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    message.title,
                    color = AppText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (message.readAt == null) {
                    Box(modifier = Modifier.padding(start = 6.dp).size(7.dp).clip(CircleShape).background(tint))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(timeLabel(message.createdAt), color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text(message.body, color = AppMuted, fontSize = 11.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
            Text(
                chip,
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(tint.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

/** «ارتباط با جیبک» - فرستادنِ پیام از طرفِ کاربر؛ جدا از پیام‌های دریافتی. */
@Composable
private fun ContactCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Chat, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text("ارتباط با جیبک", color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Black)
            Text(
                "ارسالِ پیام، گزارشِ مشکل، پیشنهاد یا درخواستِ ویژگی",
                color = AppMuted,
                fontSize = 10.5.sp,
                lineHeight = 16.sp,
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.EditNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text("ارسال پیام به ما", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 5.dp))
        }
    }
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
    val isDue = isReminder(message)
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
            // منبع: نامِ برنامه/فرستنده **بالای** قرصِ نوع (خواسته‌ی کاربر، ۳ مهر) - «blu» روی «اعلان».
            message.sourceLabel?.let { label ->
                val (type, origin) = splitSource(label)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (origin != null) {
                        Text(
                            origin,
                            color = AppText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        type,
                        color = AppPrimaryDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSurface2)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
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
