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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
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
                description = "هر تراکنشی که خودکار تشخیص داده بشه و هر خبرِ مهمی اینجا میاد.",
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (actionable.isNotEmpty()) {
                item { SectionLabel("نیاز به بررسی · ${toFa(actionable.size)}") }
                items(actionable, key = { it.id }) { message ->
                    ActionableCard(
                        message = message,
                        onConfirm = { viewModel.confirmTransaction(message) },
                        onReject = { viewModel.rejectTransaction(message) },
                    )
                }
            }
            if (news.isNotEmpty()) {
                item { SectionLabel("خبرها") }
                items(news, key = { it.id }) { message ->
                    NewsCard(message = message, onClick = { viewModel.markRead(message.id) })
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
            }
        }
    }
}
