package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.AdminDigestMetric
import ir.sadteam.loancalc.data.network.AdminDigestResponse
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarning
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminDigestViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {
    private val _period = MutableStateFlow("day")
    val period: StateFlow<String> = _period
    private val _data = MutableStateFlow<AdminDigestResponse?>(null)
    val data: StateFlow<AdminDigestResponse?> = _data
    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed

    fun load(p: String = _period.value) {
        _period.value = p
        _data.value = null
        _failed.value = false
        viewModelScope.launch {
            val r = repo.adminDigest(p)
            _data.value = r
            _failed.value = r == null
        }
    }
}

private val METRIC_LABELS = linkedMapOf(
    "active" to "کاربرِ فعال",
    "new_installs" to "نصبِ تازه",
    "new_users" to "ثبت‌نامِ تازه",
    "transactions" to "تراکنشِ ثبت‌شده",
    "purchases" to "خریدِ اشتراک",
    "revenue" to "فروشِ ناخالص (تومان)",
    "revenue_net" to "فروشِ خالص تقریبی (تومان)",
    "revenue_cafebazaar" to "فروشِ کافه‌بازار",
    "revenue_myket" to "فروشِ مایکت",
    "support" to "پیامِ پشتیبانی",
    "crashes" to "کرش",
)

/** متریک‌هایی که بالا رفتنشان **بد** است (قرمز). */
private val BAD_WHEN_UP = setOf("crashes", "support")

/**
 * 📰 **گزارشِ روز/هفته/ماه** (۸ مهر، خواسته‌ی کاربر): «هر روز بگوید چه شد». روز از ۷ صبح تا ۷ صبحِ
 * فردا. هر عدد کنارِ دوره‌ی قبلِ هم‌اندازه با فلشِ بالا/پایین.
 */
@Composable
fun AdminDigestScreen(onBack: () -> Unit, vm: AdminDigestViewModel = hiltViewModel()) {
    val period by vm.period.collectAsState()
    val data by vm.data.collectAsState()
    val failed by vm.failed.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().background(AppBg).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.padding(start = 4.dp)) {
                Text("گزارشِ روز", color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("هر روز از ۷ صبح تا ۷ صبحِ فردا", color = AppMuted, fontSize = 12.sp)
            }
        }
        val periods = listOf("day", "week", "month")
        SegmentedToggle(
            options = listOf("امروز", "۷ روز", "۳۰ روز"),
            selectedIndex = periods.indexOf(period).coerceAtLeast(0),
            onSelect = { vm.load(periods[it]) },
        )
        val d = data
        when {
            failed -> Text("گزارش نرسید؛ اینترنت را چک کن.", color = AppDanger, fontSize = 13.sp)
            d == null -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppPrimary) }
            else -> {
                Text(
                    "${toFa(d.fromIran)} تا ${toFa(d.toIran)} · مقایسه با دوره‌ی قبل",
                    color = AppMuted, fontSize = 11.sp,
                )
                // هشدارها اول: چیزی که باید امروز به آن رسید.
                d.notes.forEach { n ->
                    val text = when {
                        n.startsWith("open_support:") -> "${toFa(n.removePrefix("open_support:"))} پیامِ بی‌جواب منتظرِ توست"
                        n == "crashes_up" -> "کرش‌ها نسبت به دوره‌ی قبل بیشتر شده"
                        n == "active_down" -> "کاربرِ فعال بیش از ۲۰٪ کم شده"
                        else -> n
                    }
                    Text(
                        "⚠ $text", color = AppWarning, fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppWarning.copy(alpha = 0.12f)).padding(12.dp),
                    )
                }
                val byKey = d.metrics.associateBy { it.key }
                METRIC_LABELS.keys.mapNotNull { byKey[it] }.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { MetricTile(it, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Text(
                    "خالص = سهمِ تو بعد از کارمزدِ استور (کافه‌بازار و مایکت هر دو ۷۶٫۹٪).",
                    color = AppMuted, fontSize = 10.5.sp,
                )
                if (d.topActions.isNotEmpty()) AppCard(label = "بیشترین کارها") {
                    d.topActions.forEach { Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(actionLabel(it.name), color = AppText, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                        Text(toFa(it.count), color = AppMuted, fontSize = 12.5.sp)
                    } }
                }
                if (d.topScreens.isNotEmpty()) AppCard(label = "بیشترین صفحه‌ها") {
                    d.topScreens.forEach { Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(screenLabel(it.name), color = AppText, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                        Text(toFa(it.count), color = AppMuted, fontSize = 12.5.sp)
                    } }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(m: AdminDigestMetric, modifier: Modifier) {
    val diff = m.now - m.prev
    val pct = if (m.prev > 0) (diff * 100 / m.prev) else null
    val bad = (m.key in BAD_WHEN_UP) == (diff > 0)
    val color = when { diff == 0L -> AppMuted; bad -> AppDanger; else -> AppPrimary }
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(AppSurface2).padding(12.dp)) {
        Text(METRIC_LABELS[m.key] ?: m.key, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Text(fmtNum(m.now), color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        Text(
            when {
                diff == 0L -> "بدونِ تغییر"
                pct != null -> (if (diff > 0) "▲ " else "▼ ") + "${toFa(kotlin.math.abs(pct))}٪ · قبلی ${fmtNum(m.prev)}"
                else -> "قبلی ${fmtNum(m.prev)}"
            },
            color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        )
    }
}

private fun fmtNum(v: Long): String = ir.sadteam.loancalc.core.fmt(v.toDouble())
