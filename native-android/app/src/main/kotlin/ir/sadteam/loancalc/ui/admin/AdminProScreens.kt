package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.AdminAbGroup
import ir.sadteam.loancalc.data.network.AdminMoneyResponse
import ir.sadteam.loancalc.data.network.AdminUserTimeline
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminProViewModel @Inject constructor(val repo: AuthRepository) : ViewModel() {
    private val _money = MutableStateFlow<AdminMoneyResponse?>(null)
    val money: StateFlow<AdminMoneyResponse?> = _money
    private val _moneyFailed = MutableStateFlow(false)
    val moneyFailed: StateFlow<Boolean> = _moneyFailed

    fun loadMoney() = viewModelScope.launch {
        _moneyFailed.value = false
        val r = repo.adminMoney()
        _money.value = r
        _moneyFailed.value = r == null
    }
}

/** صفحه‌ی ساده‌ی ادمین با سربرگِ برگشت. */
@Composable
private fun AdminPage(title: String, subtitle: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().background(AppBg).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.padding(start = 4.dp)) {
                Text(title, color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = AppMuted, fontSize = 12.sp)
            }
        }
        content()
    }
}

@Composable
private fun BigStat(label: String, value: String, sub: String, modifier: Modifier = Modifier, color: Color = AppText) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(AppSurface2).padding(12.dp)) {
        Text(label, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        Text(sub, color = AppMuted, fontSize = 11.sp)
    }
}

private fun pct(a: Int, b: Int): String = if (b <= 0) "—" else "${toFa(a * 100 / b)}٪"

/** 💰 پول: تبدیلِ مجانی به خرید، تمدید، درآمد به‌ازای کاربر، و نتیجه‌ی آزمایشِ A/B. */
@Composable
fun AdminMoneyScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    val m by vm.money.collectAsState()
    val failed by vm.moneyFailed.collectAsState()
    LaunchedEffect(Unit) { vm.loadMoney() }
    AdminPage("پول و فروش", "از کلِ عمرِ برنامه", onBack) {
        val d = m
        when {
            failed -> Text("نرسید؛ اینترنت را چک کن.", color = AppDanger)
            d == null -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppPrimary) }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BigStat("مجانی ← خرید", pct(d.converted, d.trialEnded), "${toFa(d.converted)} از ${toFa(d.trialEnded)} نفری که ۳۰ روزشان تمام شد", Modifier.weight(1f), AppPrimary)
                    BigStat("تمدید", pct(d.renewed, d.expiredPayers), "${toFa(d.renewed)} از ${toFa(d.expiredPayers)} اشتراکِ تمام‌شده", Modifier.weight(1f), AppPrimary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BigStat("فروشِ کل (ناخالص)", fmt(d.grossTotal.toDouble()), "تومان", Modifier.weight(1f))
                    BigStat("فروشِ کل (خالص)", fmt(d.netTotal.toDouble()), "تومان · سهمِ تو", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BigStat("درآمد به‌ازای هر کاربر", if (d.users > 0) fmt((d.netTotal / d.users).toDouble()) else "—", "خالص · ${toFa(d.users)} کاربر", Modifier.weight(1f))
                    BigStat("درآمد به‌ازای هر خریدار", if (d.payers > 0) fmt((d.netTotal / d.payers).toDouble()) else "—", "خالص · ${toFa(d.payers)} خریدار", Modifier.weight(1f))
                }
                AppCard(label = "آزمایشِ A/B - جمله‌ی صفحه‌ی اشتراک") {
                    AbRow("A · «امکاناتِ بیشتر، تجربه‌ی کامل‌تر»", d.abA)
                    AbRow("B · «همه‌ی امکانات، بدونِ هیچ محدودیتی»", d.abB)
                    val ra = if (d.abA.installs > 0) d.abA.purchases * 1000 / d.abA.installs else 0
                    val rb = if (d.abB.installs > 0) d.abB.purchases * 1000 / d.abB.installs else 0
                    Text(
                        when {
                            d.abA.installs + d.abB.installs < 100 -> "هنوز زود است - دست‌کم ۱۰۰ نفر لازم است تا نتیجه قابلِ اعتماد شود."
                            ra == rb -> "فعلاً فرقی ندارند."
                            ra > rb -> "فعلاً A بهتر می‌فروشد."
                            else -> "فعلاً B بهتر می‌فروشد."
                        },
                        color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AbRow(label: String, g: AdminAbGroup) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text(label, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        Text(
            "${toFa(g.installs)} نفر دیدند · ${toFa(g.paywallViews)} بازدید · ${toFa(g.purchases)} خرید · نرخِ خرید ${pct(g.purchases, g.installs)}",
            color = AppMuted, fontSize = 11.5.sp,
        )
    }
}

/** 🔎 تاریخچه‌ی یک کاربر با شماره‌ی کاربری. */
@Composable
fun AdminUserScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AdminUserTimeline?>(null) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AdminPage("تاریخچه‌ی کاربر", "با شماره‌ی کاربری (Uid)", onBack) {
        Ltr {
            OutlinedTextField(
                value = code, onValueChange = { code = it.take(20) }, singleLine = true,
                label = { Text("شماره‌ی کاربری") }, placeholder = { Text("7405024") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = appFieldColors(), shape = AppFieldShape, modifier = Modifier.fillMaxWidth(),
            )
        }
        GradientButton(
            onClick = {
                loading = true; failed = false
                scope.launch { result = vm.repo.adminUser(code.trim()); failed = result == null; loading = false }
            },
            enabled = code.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "در حالِ گشتن…" else "نشان بده") }
        val r = result
        when {
            failed -> Text("نرسید؛ اینترنت را چک کن.", color = AppDanger)
            r == null -> {}
            !r.found -> Text("کاربری با این شماره پیدا نشد.", color = AppDanger)
            else -> {
                AppCard(label = "Uid:${r.code}") {
                    Text("ثبت‌نام: ${toFa(r.createdAt.take(16))}", color = AppText, fontSize = 12.5.sp)
                    Text("اشتراک تا: ${r.subscribedUntil?.let { toFa(it.take(10)) } ?: "ندارد"}", color = AppText, fontSize = 12.5.sp)
                    Text("پیام‌های پشتیبانی: ${toFa(r.supportCount)}", color = AppText, fontSize = 12.5.sp)
                    r.lastSupport?.let { Text("آخرین پیام: $it", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
                }
                if (r.purchases.isNotEmpty()) AppCard(label = "خریدها") {
                    r.purchases.forEach { p ->
                        Text("${p.product} · ${if (p.store == "myket") "مایکت" else "کافه‌بازار"} · ${toFa(p.at.take(10))} تا ${toFa(p.until.take(10))}", color = AppText, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                if (r.installs.isNotEmpty()) AppCard(label = "گوشی‌ها") {
                    r.installs.forEach { i ->
                        Text(
                            "${i.model ?: "گوشی"} · نسخه‌ی ${i.version?.let { toFa(it) } ?: "?"} · ${toFa(i.activeDays)} روزِ فعال · از ${toFa(i.firstDay)} تا ${toFa(i.lastDay)}",
                            color = AppText, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
                if (r.topScreens.isNotEmpty()) AppCard(label = "بیشترین صفحه‌ها") {
                    r.topScreens.forEach { Text("${screenLabel(it.name)} · ${toFa(it.count)}", color = AppText, fontSize = 12.sp) }
                }
                if (r.days.isNotEmpty()) AppCard(label = "۱۴ روزِ آخر") {
                    r.days.forEach { d -> Text("${toFa(d.day)} · ${toFa(d.screens)} صفحه · ${toFa(d.events)} رویداد", color = AppText, fontSize = 12.sp) }
                }
            }
        }
    }
}

private val SEGMENT_LABELS = linkedMapOf(
    "inactive7" to "۷ روز است نیامده‌اند",
    "trial_ending" to "مجانی‌شان ۳ روزِ دیگر تمام می‌شود",
    "expired" to "اشتراکشان تمام شده و تمدید نکرده‌اند",
    "free" to "هیچ‌وقت نخریده‌اند",
    "paid" to "اشتراکِ فعال دارند",
    "all" to "همه‌ی کاربران",
)

/** 📣 پیامِ هدفمند به یک گروه - در «پیام‌های جیبک»ِ همان کاربرها. */
@Composable
fun AdminBroadcastScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    var segment by remember { mutableStateOf("inactive7") }
    var count by remember { mutableStateOf<Int?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(segment) { count = null; count = vm.repo.adminBroadcast(segment, "", "", dryRun = true)?.count }
    AdminPage("پیامِ گروهی", "فقط به یک گروهِ خاص", onBack) {
        SEGMENT_LABELS.forEach { (key, label) ->
            val sel = key == segment
            Text(
                label, color = if (sel) Color.White else AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (sel) AppPrimary else AppSurface2)
                    .clickable { segment = key }.padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
        Text(count?.let { "${toFa(it)} نفر در این گروه‌اند" } ?: "در حالِ شمردن…", color = AppPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = title, onValueChange = { title = it.take(80) }, label = { Text("عنوان") }, singleLine = true, colors = appFieldColors(), shape = AppFieldShape, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = body, onValueChange = { body = it.take(600) }, label = { Text("متنِ پیام") }, minLines = 3, colors = appFieldColors(), shape = AppFieldShape, modifier = Modifier.fillMaxWidth())
        GradientButton(onClick = { confirm = true }, enabled = title.isNotBlank() && body.isNotBlank() && (count ?: 0) > 0, modifier = Modifier.fillMaxWidth()) { Text("بفرست") }
        status?.let { Text(it, color = AppPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold) }
    }
    if (confirm) {
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("فرستادن به ${toFa(count ?: 0)} نفر؟", fontWeight = FontWeight.Black) },
            text = { Text("«$title» در «پیام‌های جیبک»ِ همه‌ی این کاربرها می‌نشیند و پس گرفته نمی‌شود.") },
            confirmButton = {
                GradientButton(onClick = {
                    confirm = false
                    scope.launch {
                        val r = vm.repo.adminBroadcast(segment, title.trim(), body.trim(), dryRun = false)
                        status = if (r?.sent == true) "✓ برای ${toFa(r.count)} نفر فرستاده شد" else "فرستاده نشد؛ دوباره امتحان کن"
                        if (r?.sent == true) { title = ""; body = "" }
                    }
                }) { Text("بفرست") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirm = false }) { Text("انصراف") } },
        )
    }
}
