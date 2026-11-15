package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/** ارتفاعِ ثابتِ ردیفِ عنوانِ همه‌ی صفحه‌های اصلی - محتوا همیشه از یک ارتفاع شروع می‌شود. */
val PageHeaderHeight = 56.dp

/**
 * **دکمه‌ی یکدستِ سربرگِ همه‌ی صفحه‌ها** (خانه، دارایی، گزارش، وام، پیام‌ها): جعبه‌ی گردگوشه‌ی
 * ۳۸ با آیکونِ ۱۹، داخلِ هدفِ لمسیِ ۴۴. قاعده‌ها: اسمِ صفحه سمتِ راست، دکمه‌ها سمتِ چپ،
 * فاصله‌ی دکمه‌ها ۶، و «چشمِ مبلغ» در هر صفحه‌ای که پول نشان می‌دهد آخرین دکمه (چپ‌ترین).
 *
 * - [primary]: دکمه‌ی اصلیِ صفحه (سبز).
 * - [active]: حالتِ روشن (مثلاً چشمِ «پنهان» یا جستجوی بازشده).
 * - [badge]: نشانِ گوشه (شمارنده/نقطه) که روی همین دکمه می‌نشیند.
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    active: Boolean = false,
    warn: Boolean = false,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    val fill = when {
        warn -> AppWarningPill
        primary || active -> AppPrimaryPill
        else -> AppIconFrame
    }
    val line = when {
        warn -> AppAssetBorder
        primary || active -> AppPrimaryBorder
        else -> AppLine
    }
    val ink = when {
        warn -> AppWarningInk
        primary || active -> AppPrimaryInk
        else -> AppMuted
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(44.dp)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(shape)
                .background(fill)
                .border(1.5.dp, line, shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = description, tint = ink, modifier = Modifier.size(19.dp))
        }
        badge?.invoke(this)
    }
}

/** «چشمِ مبلغ» - همه‌جا همین یک شکل؛ روشن (کهربایی) یعنی مبلغ‌ها پنهان است. */
@Composable
fun PrivacyEyeHeaderButton(privacyMode: Boolean, onToggle: () -> Unit) {
    HeaderIconButton(
        icon = if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
        description = "پنهان‌کردنِ مبلغ‌ها",
        onClick = onToggle,
        warn = privacyMode,
    )
}
