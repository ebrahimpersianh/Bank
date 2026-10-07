package ir.sadteam.loancalc.ui.inbox

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.defaultMinSize
import ir.sadteam.loancalc.R
import androidx.compose.foundation.clickable
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EditNote
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import ir.sadteam.loancalc.ui.theme.AppTxIn
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppLabel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

@Composable
internal fun SectionLabel(text: String) {
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
internal fun ActionableCard(
    message: InboxMessageEntity,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onShowSource: () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
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
/** قابِ هر بخش: آیکون، عنوان، زیرنویس و شمارنده؛ آبی برای پیام‌های شما، سبز برای جیبک. */
@Composable
internal fun MessageSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    green: Boolean,
    onMarkAllRead: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tint = if (green) AppTxIn else AppPrimary
    val shape = RoundedCornerShape(AppRadius.card)
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
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(if (green) tint else tint.copy(alpha = 0.14f)),
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
internal fun EmptyLine(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
    )
}
@Composable
internal fun SeeAllButton(label: String, green: Boolean, onClick: () -> Unit) {
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
internal fun AnnouncementCard(message: InboxMessageEntity, onAction: (String) -> Unit = {}, onClick: () -> Unit) {
    val kind = message.refId
    // هدیه‌ی گرفته‌شده (پنجره‌اش یک بار نشان داده شد) خاکستری می‌شود تا معلوم باشد استفاده شده.
    val claimed = isGift(message) && message.readAt != null
    val gift = isGift(message) && !claimed
    val (icon, tint, chip) = when {
        claimed -> Triple(Icons.Filled.CardGiftcard, AppMuted, "✓ دریافت شد")
        gift -> Triple(Icons.Filled.CardGiftcard, ir.sadteam.loancalc.ui.theme.AppGoldInk, "هدیه · بزن")
        else -> when (kind) {
        "update" -> Triple(Icons.Filled.CardGiftcard, AppTxIn, "جدید")
        "outage" -> Triple(Icons.Filled.Warning, AppWarningInk, "اطلاعیه")
        "feature" -> Triple(Icons.Filled.AutoAwesome, AppPurple, "قابلیتِ تازه")
        else -> Triple(Icons.Filled.Campaign, AppPrimary, "اطلاعیه")
        }
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (gift) ir.sadteam.loancalc.ui.theme.AppGoldPillSoft else AppSurface)
            .border(if (gift) 2.dp else 1.dp, if (gift) ir.sadteam.loancalc.ui.theme.AppGoldBorder else AppLine, shape)
            .pressScaleClickable(scale = 0.99f, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(tint.copy(alpha = 0.14f)),
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
                    maxLines = 2,
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
            val action = message.sourceLabel?.removePrefix("action:")?.takeIf { message.sourceLabel?.startsWith("action:") == true }
            val actionLabel = when (action) {
                "shop" -> "برو به فروشگاه"
                "subscription" -> "دیدنِ اشتراک‌ها"
                "update" -> "آپدیت کن"
                else -> null
            }
            if (action != null && actionLabel != null) {
                Text(
                    actionLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(AppPrimary)
                        .pressScaleClickable(scale = 0.95f) { onAction(action) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
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
internal fun ContactCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
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
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPrimaryPill),
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
/** کارتِ «پیشینه» - زنگ و یک جمله؛ «همه خوانده شد» هم این‌جاست. */
@Composable
private fun HistoryHeaderCard(onMarkAllRead: (() -> Unit)?) {
    val shape = RoundedCornerShape(AppRadius.card)
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
 * کارتِ خبر (طرحِ ChatGPT): نشانِ نوع (برداشت قرمز، واریز سبز، یادآوریِ قسط زرد)، عنوان و
 * متن، قرصِ منبع، زمان و فلش. نقطه‌ی کنارِ عنوان یعنی خوانده‌نشده.
 */
@Composable
internal fun NewsCard(message: InboxMessageEntity, onClick: () -> Unit, onRead: () -> Unit = {}) {
    // متنِ بلند ۲ خط نشان داده می‌شود؛ زدن روی کارت بازش می‌کند (و کارِ قبلیِ کارت هم انجام می‌شود).
    var expanded by remember { mutableStateOf(false) }
    val isTx = message.kind == InboxMessageEntity.Kind.DETECTED_TX
    val isDeposit = isTx && message.title.contains("واریز")
    val isDue = isReminder(message)
    val (tint, fill, icon) = when {
        isDeposit -> Triple(AppTxIn, AppTxIn.copy(alpha = 0.14f), Icons.Filled.AddCircle)
        isTx -> Triple(AppDanger, AppDangerPill, Icons.Filled.RemoveCircle)
        // یادآوری زرد (فریمِ `32`) - جدا از قرمز/سبزِ پیام‌های مالی.
        isDue -> Triple(AppWarningInk, AppWarningPill, Icons.Filled.CalendarMonth)
        else -> Triple(AppPrimary, AppPrimaryPill, Icons.Filled.NotificationsNone)
    }
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = AppPrimary.copy(alpha = 0.10f), spotColor = AppPrimary.copy(alpha = 0.10f))
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            // ۱۶ مهر: لمسِ خودِ کارت = بازشدنِ متنِ کامل (همان‌جا)؛ پنجره/دسته فقط با فلشِ گوشه باز می‌شود.
            .pressScaleClickable(scale = 0.99f, onClick = { expanded = !expanded; onRead() })
            .padding(11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.icon)).background(fill),
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
                    maxLines = if (expanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                message.body,
                color = AppMuted,
                fontSize = 11.5.sp,
                lineHeight = 18.sp,
                // حداکثر ۲ خط؛ متنِ کامل با زدن روی کارت باز می‌شود.
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        // ستونِ کناری (طرحِ ChatGPT): زمان بالا؛ پایین نامِ برنامه/فرستنده به آبی **بالای** قرصِ نوع
        // (خواسته‌ی کاربر، ۳ مهر: «blu» دقیقاً بالای «اعلان»).
        Column(
            modifier = Modifier.padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDue) Icons.Filled.Event else Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(14.dp),
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
            val source = message.sourceLabel?.let { splitSource(it) }
            val chipText = source?.first ?: if (isDue) "یادآوری" else null
            if (chipText != null) {
                val warm = isDue
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    source?.second?.let { origin ->
                        Text(
                            origin,
                            color = AppPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 90.dp).padding(bottom = 3.dp),
                        )
                    }
                    Text(
                        chipText,
                        color = if (warm) AppWarningInk else AppPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (warm) AppWarningPill else AppPrimaryPill)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
        // فلشِ گوشه: بازکردنِ جزئیات (پنجره‌ی منبع یا انتخابِ دسته) - جدا از بازشدنِ متنِ کارت.
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.KeyboardArrowLeft,
                contentDescription = "جزئیات",
                tint = AppLabel,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
/** هدیه = نوعِ «gift» از سرور، یا پیام‌های قدیمیِ هدیه که با 🎁 شروع می‌شوند. */
internal fun isGift(m: InboxMessageEntity): Boolean = m.refId == "gift" || m.title.startsWith("🎁")
/**
 * 🎉 **جشنِ هدیه** (۹ مهر): پنجره‌ای با جعبه‌ی هدیه‌ی تپنده، سکه‌هایی که به بالا پخش می‌شوند و
 * متنِ ادمین. سکه همان لحظه‌ی رسیدنِ پیام به کیف رفته؛ این فقط نشانش می‌دهد.
 */
@Composable
internal fun GiftCelebration(message: InboxMessageEntity, onDone: () -> Unit) {
    val gold = ir.sadteam.loancalc.ui.theme.AppGoldInk
    val goldSoft = ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
    val anim = remember { androidx.compose.animation.core.Animatable(0f) }
    val pulse = androidx.compose.animation.core.rememberInfiniteTransition(label = "gift")
    val scale by pulse.animateFloat(
        0.94f, 1.06f,
        androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(700), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "giftScale",
    )
    androidx.compose.runtime.LaunchedEffect(Unit) {
        anim.animateTo(1f, androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.FastOutSlowInEasing))
    }
    val buzz = ir.sadteam.loancalc.ui.haptics.rememberBuzz()
    androidx.compose.runtime.LaunchedEffect(Unit) { runCatching { buzz() } }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDone) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(AppRadius.sheet)).background(AppSurface).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                // سکه‌های پخش‌شونده
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val t = anim.value
                    repeat(12) { i ->
                        val a = Math.toRadians(i * 30.0 - 90)
                        val r = size.minDimension * 0.48f * t
                        val c = androidx.compose.ui.geometry.Offset(
                            center.x + (kotlin.math.cos(a) * r).toFloat(),
                            center.y + (kotlin.math.sin(a) * r).toFloat(),
                        )
                        drawCircle(gold.copy(alpha = (1f - t * 0.6f)), radius = 7.dp.toPx() * (1f - t * 0.3f), center = c)
                    }
                }
                Box(
                    Modifier.size(84.dp).graphicsLayerScale(scale).clip(CircleShape).background(goldSoft),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.CardGiftcard, null, tint = gold, modifier = Modifier.size(46.dp)) }
            }
            Text(message.title.removePrefix("🎁").trim(), color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("به کیفت اضافه شد", color = gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            if (message.body.isNotBlank()) {
                Text(
                    "«${message.body}»", color = AppMuted, fontSize = 14.sp, lineHeight = 23.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 12.dp),
                )
            }
            ir.sadteam.loancalc.ui.components.GradientButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
                Text("ممنون!", fontWeight = FontWeight.Black)
            }
        }
    }
}
private fun Modifier.graphicsLayerScale(s: Float): Modifier = this.graphicsLayer(scaleX = s, scaleY = s)
/** سطلِ زباله: خالی = درِ بسته (خاکستری)؛ پر = درِ باز و کاغذ (فیروزه‌ای) + شمارنده + تکانِ ریز. */
@Composable
internal fun TrashBinIcon(count: Int) {
    val full = count > 0
    val wiggle = if (full) {
        val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "bin")
        t.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                androidx.compose.animation.core.keyframes {
                    durationMillis = 2600
                    0f at 0; 0f at 2000; -1f at 2120; 1f at 2260; -0.6f at 2400; 0f at 2600
                },
            ),
            label = "binWiggle",
        ).value
    } else 0f
    // ۱۶ مهر: شمارنده روی درِ سطل می‌افتاد؛ حالا سطل در یک سمت و شمارنده در گوشه‌ی دیگر،
    // با حاشیه‌ی هم‌رنگِ پس‌زمینه تا مثلِ نشانِ واقعی دیده شود.
    Box(Modifier.width(56.dp).height(40.dp)) { // ۱۶ مهر: پهن‌تر تا شمارنده کاملاً بیرونِ سطل بماند
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(if (full) R.drawable.trash_full else R.drawable.trash_empty),
            contentDescription = "سطلِ زباله",
            modifier = Modifier.size(34.dp).align(Alignment.BottomStart).graphicsLayer { rotationZ = wiggle * 6f },
        )
        if (full) Box(
            Modifier.align(Alignment.TopEnd)
                .offset(x = (-1).dp, y = 0.dp)
                .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                .border(1.5.dp, ir.sadteam.loancalc.ui.theme.AppSurface, androidx.compose.foundation.shape.CircleShape)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFFE5484D))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) { Text(toFa(count), color = Color.White, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Black) }
    }
}
