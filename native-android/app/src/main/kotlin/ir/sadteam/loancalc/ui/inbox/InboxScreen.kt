package ir.sadteam.loancalc.ui.inbox

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

    Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "پیام‌ها",
                color = AppText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            // ⚠️ «همه خوانده شد» عمداً فقط خبرها رو می‌خونه - تاییدِ ضمنیِ هیچ تراکنشی نیست
            // (قاعده‌ی صریحِ طرح).
            if (news.any { it.readAt == null }) {
                Text(
                    "همه خوانده شد",
                    color = AppPrimaryDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .pressScaleClickable { viewModel.markAllNewsRead() }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
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
                item { SectionLabel("پیشینه") }
                items(news, key = { it.id }) { message ->
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

/** کارتِ خبر - سطحِ ساده با نقطه‌ی سبزِ خوانده‌نشده. */
@Composable
private fun NewsCard(message: InboxMessageEntity, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().pressScaleClickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            if (message.readAt == null) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(AppPrimary))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    message.title,
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = if (message.readAt == null) FontWeight.Black else FontWeight.Bold,
                )
                Text(
                    message.body,
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
                message.sourceLabel?.let { label ->
                    Text(
                        label,
                        color = AppPrimaryDim,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
