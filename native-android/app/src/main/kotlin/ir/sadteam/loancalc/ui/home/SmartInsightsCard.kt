package ir.sadteam.loancalc.ui.home

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.SmartInsights
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppText

/** «بستن»ِ هر پیشنهاد آن را ۷ روز پنهان می‌کند (هر کلید جدا). */
private object InsightDismissals {
    private const val PREFS = "smart_insights"
    private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000
    fun hidden(ctx: Context, key: String) =
        System.currentTimeMillis() - ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(key, 0L) < WEEK_MS
    fun dismiss(ctx: Context, key: String) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(key, System.currentTimeMillis()).apply()
}

/**
 * 🧠 کارتِ «پیشنهادِ جیبک» در خانه (۷ مهر) - حداکثر دو پیشنهاد، هر کدام قابلِ بستن.
 * با لمس، به جای مربوط می‌رود (گزارش، بودجه، سررسید). وقتی چیزی نیست، اصلاً دیده نمی‌شود.
 */
@Composable
fun SmartInsightsCard(insights: List<SmartInsights.Insight>, onOpen: (SmartInsights.Insight) -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    val all = remember(insights, tick) { insights.filterNot { InsightDismissals.hidden(ctx, it.key) } }
    // فقط مهم‌ترین پیشنهاد؛ بقیه با «+N پیشنهادِ دیگر» (۸ مهر: صفحه‌ی خانه شلوغ بود).
    val visible = if (expanded) all.take(3) else all.take(1)
    if (visible.isEmpty()) return
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AutoAwesome, null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
            Text("پیشنهادِ جیبک", color = AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
        }
        visible.forEach { ins ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(top = 8.dp).clickable {
                    ir.sadteam.loancalc.data.UsageStats.action("insight_open_" + ins.kind.name.lowercase())
                    onOpen(ins)
                },
            ) {
                if (ins == visible.first()) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.jibak_home_insight),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(ins.title, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                    Text(ins.body, color = AppMuted, fontSize = 11.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 2.dp))
                }
                IconButton(onClick = {
                    InsightDismissals.dismiss(ctx, ins.key)
                    ir.sadteam.loancalc.data.UsageStats.action("insight_dismiss_" + ins.kind.name.lowercase())
                    tick++
                }) { Icon(Icons.Filled.Close, "بستن", tint = AppMuted, modifier = Modifier.size(16.dp)) }
            }
        }
        val more = minOf(all.size, 3) - visible.size
        if (more > 0 || expanded) {
            Text(
                if (expanded) "کمتر" else "+${ir.sadteam.loancalc.core.toFa(more)} پیشنهادِ دیگر",
                color = AppPrimaryInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp).clickable { expanded = !expanded }.padding(vertical = 6.dp),
            )
        }
    }
}
