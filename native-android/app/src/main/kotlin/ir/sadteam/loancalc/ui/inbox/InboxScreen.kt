package ir.sadteam.loancalc.ui.inbox

import ir.sadteam.loancalc.ui.jibak.rialToToman
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Campaign
import ir.sadteam.loancalc.ui.support.BugReportScreen
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
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
fun InboxScreen(onBack: () -> Unit, onOpenShop: () -> Unit = {}, viewModel: InboxViewModel = hiltViewModel()) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    // دکمه‌ی داخلِ پیامِ گروهیِ ادمین (۱۰ مهر).
    val runAction: (String) -> Unit = { action ->
        when (action) {
            "shop" -> onOpenShop()
            "subscription" -> ir.sadteam.loancalc.ui.subscription.PremiumPaywall.showPlans = true
            "update" -> runCatching {
                val url = if (ir.sadteam.loancalc.BuildConfig.FLAVOR == "myket") "https://myket.ir/app/ir.sadteam.loancalc"
                else "https://cafebazaar.ir/app/ir.sadteam.loancalc"
                ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
    val messages by viewModel.messages.collectAsState()
    val bin by viewModel.recycleBin.collectAsState()
    var binOpen by remember { mutableStateOf(false) }
    val sourceAccount by viewModel.sourceAccount.collectAsState()
    // اعلان و صفحهٔ «دارایی» باید دقیقاً از همان مسیرِ داده و ViewModel استفاده کنند؛
    // ساختنِ ViewModel تازه در دلِ دیالوگِ اعلان روی بعضی گوشی‌ها موقع بازشدنِ جزئیات کرش می‌کرد.
    val accountViewModel: AccountViewModel = hiltViewModel()
    // پیامی که کاربر «منبعش» را لمس کرده - متنِ خامِ همان پیامک/اعلان را نشان می‌دهیم.
    var sourceOf by remember { mutableStateOf<InboxMessageEntity?>(null) }
    var quickCatOf by remember { mutableStateOf<InboxMessageEntity?>(null) }
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
    // 🎁 هدیه‌ی خوانده‌نشده: با باز شدنِ صندوق، جشنش خودش باز می‌شود (۹ مهر، خواسته‌ی کاربر).
    var celebrate by remember { mutableStateOf<InboxMessageEntity?>(null) }
    val firstGift = jibak.firstOrNull { isGift(it) && it.readAt == null }
    androidx.compose.runtime.LaunchedEffect(firstGift?.id) { if (firstGift != null && celebrate == null) celebrate = firstGift }
    celebrate?.let { g ->
        GiftCelebration(g) { viewModel.markRead(g.id); celebrate = null }
    }

    // 🚨 `statusBarsPadding`: عنوانِ قبلی زیرِ نوارِ وضعیتِ گوشی می‌رفت (اسکرین‌شاتِ کاربر).
    Column(modifier = Modifier.fillMaxSize().background(AppBg).statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ir.sadteam.loancalc.ui.components.PageHeaderHeight)
                .padding(start = 8.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "پیام‌ها",
                color = AppText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Box {
                ir.sadteam.loancalc.ui.components.HeaderIconButton(
                    icon = Icons.Filled.Tune, description = "فیلتر",
                    active = filter != InboxFilter.ALL, onClick = { filterMenu = true },
                )
                DropdownMenu(expanded = filterMenu, onDismissRequest = { filterMenu = false }) {
                    InboxFilter.entries.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.label, fontWeight = if (f == filter) FontWeight.Black else FontWeight.Normal) },
                            onClick = { filter = f; filterMenu = false },
                        )
                    }
                }
            }
            ir.sadteam.loancalc.ui.components.HeaderIconButton(
                icon = Icons.Filled.Search, description = "جستجو", active = searching,
                onClick = {
                    searching = !searching
                    if (!searching) query = ""
                },
            )
        }
        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو در پیام‌ها…") },
                singleLine = true,
                shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(),)
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

            // ── سطل‌زباله‌ی تراکنش‌های ردشده (۳۰ روز) ──────────────────────────────
            run {
                item {
                    // ۱۶ مهر: سطلِ زباله‌ی واقعی - خالی = درِ نیمه‌باز، پر = کاغذ بیرون زده و تکانِ ریز.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            .background(ir.sadteam.loancalc.ui.theme.AppSurface)
                            .clickable(enabled = bin.isNotEmpty()) { binOpen = !binOpen }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrashBinIcon(count = bin.size)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text("سطلِ زباله", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (bin.isEmpty()) "خالی است · تراکنش‌های ردشده ۳۰ روز این‌جا می‌مانند"
                                else "${toFa(bin.size)} تراکنشِ ردشده · بزن تا ببینی",
                                color = AppMuted,
                                fontSize = 10.5.sp,
                            )
                        }
                        if (bin.isNotEmpty()) Text(if (binOpen) "▴" else "▾", color = AppMuted, fontSize = 16.sp)
                    }
                }
                if (binOpen) {
                    items(bin, key = { "bin_" + it.tx.id }) { item ->
                        ir.sadteam.loancalc.ui.components.AppCard(contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        (if (item.tx.type == "DEPOSIT") "واریز " else "برداشت ") +
                                            rialToToman(item.tx.amount.toLong()).let { toFa(it) } + " تومان",
                                        color = AppText,
                                        fontSize = 13.sp,
                                    )
                                    Text(item.tx.originLabel ?: "", color = AppMuted, fontSize = 10.sp)
                                }
                                androidx.compose.material3.TextButton(onClick = { viewModel.restore(item) }) { Text("برگردون") }
                            }
                        }
                    }
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
                        EmptyLine(if (query.isBlank() && filter == InboxFilter.ALL) "فعلاً پیامی برای حسابت نیست." else "پیامی با این جستجو یا فیلتر پیدا نشد.")
                    }
                    val list = if (showAllPersonal) personal else personal.take(3)
                    list.forEach { message ->
                        NewsCard(
                            message = message,
                            onRead = { viewModel.markRead(message.id) },
                            onClick = {
                                viewModel.markRead(message.id)
                                // ۱۴ مهر: تراکنشِ خودکار → پنجره‌ی دسته (متنِ پیام از همان‌جا).
                                val txId = message.refId?.toLongOrNull()
                                if (message.kind == InboxMessageEntity.Kind.DETECTED_TX && txId != null) quickCatOf = message
                                else if (message.sourceText != null) sourceOf = message
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
                Spacer(Modifier.height(10.dp))
                MessageSection(
                    icon = Icons.Filled.Campaign,
                    title = "پیام‌های جیبک",
                    subtitle = "خبرها و قابلیت‌های جدید",
                    count = jibak.size,
                    green = true,
                    onMarkAllRead = null,
                ) {
                    if (jibak.isEmpty()) EmptyLine("فعلاً اطلاعیه‌ی تازه‌ای نیست.")
                    val list = if (showAllJibak) jibak else jibak.take(2)
                    list.forEach { message ->
                        AnnouncementCard(message, onAction = { a -> viewModel.markRead(message.id); runAction(a) }) { if (isGift(message) && message.readAt == null) celebrate = message else viewModel.markRead(message.id) }
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

    quickCatOf?.let { message ->
        QuickCategoryDialog(
            txId = message.refId!!.toLong(),
            onDismiss = { quickCatOf = null },
            onShowSource = if (message.sourceText != null) ({ quickCatOf = null; sourceOf = message }) else null,
        )
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
// «مهم»ِ طرح ساخته نشد: برنامه قابلیتِ نشان‌کردنِ پیام ندارد؛ «خوانده‌نشده» واقعی است.
private enum class InboxFilter(val label: String, val icon: ImageVector) {
    ALL("همه", Icons.Filled.Inbox),
    FINANCE("مالی", Icons.Filled.CreditCard),
    REMINDERS("یادآوری‌ها", Icons.Filled.CalendarMonth),
    UNREAD("خوانده‌نشده", Icons.Filled.MarkEmailUnread),
}
/** «اعلانِ blu» → («اعلان»، «blu») · «پیامک از 3000123» → («پیامک»، «3000123»). */
internal fun splitSource(label: String): Pair<String, String?> = when {
    label.startsWith("اعلانِ ") -> "اعلان" to label.removePrefix("اعلانِ ").trim().ifBlank { null }
    label.startsWith("پیامک از ") -> "پیامک" to label.removePrefix("پیامک از ").trim().ifBlank { null }
    else -> label to null
}
/** یادآوریِ قسط/چک/پرداخت - سیستم آن‌ها را با نوعِ SYSTEM و عنوانِ «یادآوریِ …» می‌سازد. */
internal fun isReminder(m: InboxMessageEntity): Boolean =
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
            .padding(horizontal = 13.dp, vertical = 7.dp),
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
