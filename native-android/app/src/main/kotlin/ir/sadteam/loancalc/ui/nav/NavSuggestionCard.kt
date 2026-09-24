package ir.sadteam.loancalc.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.NavSuggestion
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **کارتِ پیشنهادِ خودکارِ نوار** - فریمِ `41a`.
 *
 * قاعده‌های صریحِ طرح که اینجا رعایت شدن:
 * - **بالای صفحه‌ی خانه، زیرِ هدر. هرگز مودال نمی‌شود** - «پیشنهادی که کاربر نخواسته حق ندارد
 *   جلوی کار را بگیرد». پس این یه `AppCard`ِ معمولیه، نه دیالوگ.
 * - **هیچ کلمه‌ی مبهمی نمی‌گوید؛ عددِ واقعی می‌گوید** («۴۱ بار» / «۲ بار»).
 * - **نوارِ قبل و بعد کنارِ هم** تا کاربر ببینه دقیقاً چی عوض می‌شه.
 * - سه جواب؛ «نه» بی‌حاشیه و کم‌رنگ ولی **هم‌عرضِ بقیه**.
 */
@Composable
fun NavSuggestionCard(
    suggestion: NavSuggestion.Result,
    currentSlots: List<NavDestination>,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val promote = NavDestination.byId(suggestion.promote) ?: return
    val demote = NavDestination.byId(suggestion.demote) ?: return
    val after = NavDestination.sanitize(NavSuggestion.apply(currentSlots.map { it.id }, suggestion))

    // هم‌بندِ ایمنیِ کارتِ ترمیم (`HomeScreen`): این هم آیتمِ بالای همان فهرست است و نباید
    // بتواند ارتفاعِ صفحه را بگیرد.
    AppCard(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = AppPrimaryInk,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text("نوارت را عوض کنم؟", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "در سه هفته‌ی گذشته ${toFa(suggestion.promoteCount)} بار ${promote.label} " +
                    "را باز کردی و ${toFa(suggestion.demoteCount)} بار ${demote.label} را. " +
                    "${promote.label} سرِ نوار برود، ${demote.label} به کشو؟",
                color = AppMuted,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniNavPreview("الان", currentSlots, highlight = demote, modifier = Modifier.weight(1f))
                MiniNavPreview("بعد", after, highlight = promote, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text("عوضش کن", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                GradientButton(
                    onClick = onEdit,
                    variant = AppButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("خودم می‌چینم", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                // «نه» - بی‌حاشیه و کم‌رنگ، ولی هم‌عرضِ بقیه (قاعده‌ی صریحِ `41a`).
                GradientButton(
                    onClick = onDismiss,
                    variant = AppButtonVariant.NEUTRAL,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("نه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** نمایشِ ریزِ نوار - فقط برچسب‌ها، چون آیکونِ ۱۸ تو این اندازه ناخوانا می‌شه. */
@Composable
private fun MiniNavPreview(
    title: String,
    slots: List<NavDestination>,
    highlight: NavDestination,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(title, color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadius.icon))
                .background(AppLineRow)
                .padding(horizontal = 3.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            slots.forEach { dest ->
                val on = dest == highlight
                Text(
                    dest.label,
                    color = if (on) AppPrimary else AppMuted,
                    fontSize = 8.sp,
                    fontWeight = if (on) FontWeight.Black else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
