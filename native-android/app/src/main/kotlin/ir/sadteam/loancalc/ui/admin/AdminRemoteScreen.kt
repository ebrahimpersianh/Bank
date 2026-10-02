package ir.sadteam.loancalc.ui.admin

import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.RemoteApp
import ir.sadteam.loancalc.data.RemoteAppConfig
import ir.sadteam.loancalc.data.RemoteFaq
import ir.sadteam.loancalc.data.RemotePromo
import ir.sadteam.loancalc.data.RemoteSurvey
import ir.sadteam.loancalc.data.network.AdminNamedCount
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch

// ── تبدیلِ متنِ چندخطی ↔ فهرست/نگاشت ─────────────────────────────────────────────
private fun lines(s: String) = s.lines().map { it.trim() }.filter { it.isNotEmpty() }
private fun toMap(s: String): Map<String, String> =
    lines(s).mapNotNull { l -> l.split("=", limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }
        .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }.toMap()
private fun fromMap(m: Map<String, String>) = m.entries.joinToString("\n") { "${it.key} = ${it.value}" }

@Composable
private fun NumField(
    label: String, value: Int?, hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Filled.Tune,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF475569),
    subtitle: String? = null,
    onChange: (Int?) -> Unit,
) {
    // ردیفِ کارت‌شکل با کاشیِ آیکونِ رنگی و شمارنده‌ی − / + (طرحِ ChatGPT، ۱۰ مهر).
    val def = hint.map { if (it in '۰'..'۹') '0' + (it - '۰') else it }.joinToString("").toIntOrNull() ?: 0
    val cur = value ?: def
    Row(
        Modifier.padding(bottom = 8.dp).fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(ir.sadteam.loancalc.ui.theme.AppSurface2).padding(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(44.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp)).background(color),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) { androidx.compose.material3.Icon(icon, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(22.dp)) }
        androidx.compose.foundation.layout.Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(label, color = ir.sadteam.loancalc.ui.theme.AppText, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
            Text(subtitle ?: "پیش‌فرض: $hint", color = ir.sadteam.loancalc.ui.theme.AppMuted, fontSize = 11.sp)
        }
        Stepper(cur, onMinus = { onChange((cur - 1).coerceAtLeast(0)) }, onPlus = { onChange(cur + 1) })
    }
}

@Composable
internal fun Stepper(value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    Row(
        Modifier.clip(shape).border(1.dp, ir.sadteam.loancalc.ui.theme.AppLine, shape),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        StepBtn("−", onMinus)
        Text(toFa(value), color = ir.sadteam.loancalc.ui.theme.AppText, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, modifier = Modifier.widthIn(min = 40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        StepBtn("+", onPlus)
    }
}

@Composable
private fun StepBtn(t: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        Modifier.size(36.dp).background(ir.sadteam.loancalc.ui.theme.AppSurface).clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) { Text(t, color = ir.sadteam.loancalc.ui.theme.AppPrimary, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black) }
}

@Composable
private fun TextArea(label: String, value: String, hint: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, placeholder = { Text(hint) }, minLines = 2,
        shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * ⚙️ **تنظیماتِ از راهِ دور** (۸ مهر). هر بخش بسته است و خلاصه‌اش را نشان می‌دهد. «ذخیره برای همه» =
 * همه‌ی گوشی‌ها دفعه‌ی بعدی که برنامه را باز کنند می‌گیرند. خالی = رفتارِ خودِ برنامه.
 */
@Composable
fun AdminRemoteScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()
    var c by remember { mutableStateOf(RemoteApp.config) }
    var saving by remember { mutableStateOf(false) }
    // متن‌های چندخطی جدا نگه داشته می‌شوند تا تایپِ «=» یا خطِ خالی وسطِ کار پاک نشود.
    var smsDep by remember { mutableStateOf(c.smsDeposit.joinToString("\n")) }
    var smsWd by remember { mutableStateOf(c.smsWithdrawal.joinToString("\n")) }
    var smsIgn by remember { mutableStateOf(c.smsIgnore.joinToString("\n")) }
    var catWd by remember { mutableStateOf(fromMap(c.catWithdrawal)) }
    var catDep by remember { mutableStateOf(fromMap(c.catDeposit)) }
    var bins by remember { mutableStateOf(fromMap(c.bins)) }
    var faq by remember { mutableStateOf(c.faq.joinToString("\n\n") { "${it.q}\n${it.a}" }) }
    var surveyOpts by remember { mutableStateOf(c.survey?.options.orEmpty().joinToString("\n")) }
    var appCode by remember { mutableStateOf("") }
    var changelog by remember { mutableStateOf("") }
    var surveyResult by remember { mutableStateOf<List<AdminNamedCount>?>(null) }
    LaunchedEffect(Unit) {
        vm.repo.adminAppVersion()?.let { appCode = it.code.toString(); changelog = it.changelog }
        c.survey?.id?.takeIf { it.isNotBlank() }?.let { surveyResult = vm.repo.adminSurvey(it) }
    }

    fun build(): RemoteAppConfig = c.copy(
        smsDeposit = lines(smsDep), smsWithdrawal = lines(smsWd), smsIgnore = lines(smsIgn),
        catWithdrawal = toMap(catWd), catDeposit = toMap(catDep), bins = toMap(bins).filterKeys { it.length == 6 && it.all(Char::isDigit) },
        faq = faq.split(Regex("\\n\\s*\\n")).mapNotNull { b -> b.trim().lines().takeIf { it.size >= 2 }?.let { RemoteFaq(it.first().trim(), it.drop(1).joinToString("\n").trim()) } },
        survey = c.survey?.takeIf { it.question.isNotBlank() }?.copy(options = lines(surveyOpts)),
    )

    Box(Modifier.fillMaxSize()) {
        AdminPage("تنظیماتِ از راهِ دور", "عوض کن بی‌آپدیتِ برنامه · خالی = پیش‌فرض", onBack) {
            AdminSection("دوره‌ی مجانیِ کاربرِ تازه", "${toFa(c.trialDays ?: 30)} روز") {
                NumField("چند روز (۱ تا ۹۰)", c.trialDays, "۳۰") { c = c.copy(trialDays = it?.coerceIn(1, 90)) }
                AdminNote("فقط برای کسانی که از این به بعد ثبت‌نام کنند؛ کاربرانِ فعلی همان مقدارِ قبلی را دارند.")
            }
            AdminSection("نسخه‌ی رایگان", "تراکنش ${c.freeTx?.let { toFa(it) } ?: "۳۰"} · حساب ${c.freeAccounts?.let { toFa(it) } ?: "۱"} · وام ${c.freeLoans?.let { toFa(it) } ?: "۱"} · چک ${c.freeCheques?.let { toFa(it) } ?: "۱"}") {
                NumField("تراکنش در ماه", c.freeTx, "۳۰") { c = c.copy(freeTx = it) }
                NumField("حساب", c.freeAccounts, "۱") { c = c.copy(freeAccounts = it) }
                NumField("وام", c.freeLoans, "۱") { c = c.copy(freeLoans = it) }
                NumField("چک", c.freeCheques, "۱") { c = c.copy(freeCheques = it) }
            }
            AdminSection("خاموش کردنِ قابلیت", if (c.disabled.isEmpty()) "همه روشن" else "${toFa(c.disabled.size)} خاموش") {
                AdminNote("میان‌برِ خاموش از دسترسیِ سریع پنهان می‌شود - برای وقتی بخشی خراب شده تا آپدیت برسد.")
                ir.sadteam.loancalc.allShortcutPool.forEach { sc ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(sc.label, color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = sc.id !in c.disabled, onCheckedChange = { on -> c = c.copy(disabled = if (on) c.disabled - sc.id else c.disabled + sc.id) })
                    }
                }
            }
            AdminSection("آپدیتِ اجباری", c.minVersion?.let { "زیرِ نسخه‌ی ${toFa(it)} باید آپدیت کند" } ?: "خاموش") {
                OutlinedTextField(
                    value = c.minVersion?.toString() ?: "", onValueChange = { c = c.copy(minVersion = cleanNum(it).take(6).toIntOrNull()) },
                    label = { Text("حداقل نسخه (versionCode)") }, placeholder = { Text("خالی = خاموش") }, singleLine = true,
                    shape = AdminFieldShape, colors = adminFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextArea("متنِ پنجره", c.minVersionText.orEmpty(), "این نسخه دیگر پشتیبانی نمی‌شود…") { c = c.copy(minVersionText = it.ifBlank { null }) }
                AdminNote("نسخه‌ی فعلیِ همین گوشی: ${toFa(ir.sadteam.loancalc.BuildConfig.VERSION_CODE)}. عددی بزرگ‌تر از آخرین نسخه‌ی منتشرشده نزن!")
            }
            AdminSection("نسخه‌ی تازه و «چه چیز تازه است»", if (appCode.isBlank()) "…" else "آخرین نسخه ${toFa(appCode)}") {
                OutlinedTextField(appCode, { appCode = cleanNum(it).take(6) }, label = { Text("آخرین نسخه (versionCode)") }, singleLine = true, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                TextArea("تغییرات (هر خط یک بند)", changelog, "قابلیتِ تازه…") { changelog = it }
                GradientButton(onClick = {
                    scope.launch {
                        val ok = vm.repo.adminSetAppVersion(appCode.toIntOrNull() ?: 0, changelog)
                        banner.show(if (ok) "نسخه ذخیره شد" else "نرسید", isSuccess = ok)
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("ذخیره‌ی نسخه") }
                AdminNote("بعد از هر انتشار این را بزن تا پنجره‌ی «نسخه‌ی تازه» برای بقیه بیاید.")
            }
            AdminSection("اشتراک: تخفیف و متن‌ها", c.promo?.title?.takeIf { it.isNotBlank() } ?: "بی‌تخفیف", gold = true) {
                val p = c.promo ?: RemotePromo()
                OutlinedTextField(p.title, { c = c.copy(promo = p.copy(title = it.take(60))) }, label = { Text("عنوانِ نوارِ تخفیف") }, singleLine = true, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                TextArea("توضیح", p.text, "۳۰٪ تخفیف تا آخرِ هفته") { c = c.copy(promo = p.copy(text = it.take(200))) }
                OutlinedTextField(p.until.orEmpty(), { c = c.copy(promo = p.copy(until = it.take(10).ifBlank { null })) }, label = { Text("تا تاریخ (۱۴۰۵-۰۷-۱۵)") }, singleLine = true, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                AdminNote("قیمتِ واقعی را در پنلِ استور کم کن؛ این فقط نوارِ اطلاع‌رسانی است.", gold = true)
                OutlinedTextField(c.paywallA.orEmpty(), { c = c.copy(paywallA = it.ifBlank { null }) }, label = { Text("جمله‌ی گروهِ A") }, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(c.paywallB.orEmpty(), { c = c.copy(paywallB = it.ifBlank { null }) }, label = { Text("جمله‌ی گروهِ B") }, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
            }
            AdminSection("پیامکِ بانک‌ها", "${toFa(lines(smsDep).size + lines(smsWd).size + lines(smsIgn).size)} کلیدواژه‌ی اضافه") {
                AdminNote("هر خط یک کلیدواژه. فقط به فهرستِ داخلی اضافه می‌شود.")
                TextArea("نشانه‌ی واریز", smsDep, "مثلاً: واریز به حساب") { smsDep = it }
                TextArea("نشانه‌ی برداشت", smsWd, "مثلاً: برداشت از حساب") { smsWd = it }
                TextArea("نادیده بگیر (تبلیغ)", smsIgn, "مثلاً: قرعه‌کشی") { smsIgn = it }
            }
            AdminSection("حدسِ دسته", "${toFa(toMap(catWd).size + toMap(catDep).size)} کلیدواژه") {
                AdminNote("هر خط: کلیدواژه = دسته (نامِ دسته دقیقاً مثلِ برنامه، مثلاً «خوراک»).")
                TextArea("خرج", catWd, "دیجی‌کالا = خرید") { catWd = it }
                TextArea("درآمد", catDep, "حقوق = حقوق") { catDep = it }
            }
            AdminSection("شماره‌کارتِ بانک‌ها", "${toFa(toMap(bins).size)} مورد") {
                TextArea("شش رقمِ اول = نامِ بانک", bins, "585983 = تجارت") { bins = it }
            }
            AdminSection("سکه و امتیاز", "ورودِ روزانه ${toFa(c.dailyCoins ?: 5)} · هفت روز ${toFa(c.weekCoins ?: 30)}") {
                NumField("سکه‌ی ورودِ روزانه", c.dailyCoins, "۵", androidx.compose.material.icons.Icons.Filled.CalendarMonth, androidx.compose.ui.graphics.Color(0xFF16A34A), "هر روز وارد شو و سکه بگیر") { c = c.copy(dailyCoins = it) }
                NumField("جایزه‌ی روزِ هفتم", c.weekCoins, "۳۰", androidx.compose.material.icons.Icons.Filled.CardGiftcard, androidx.compose.ui.graphics.Color(0xFFE11D48), "با هفت روز فعالیت، جایزه‌ی ویژه") { c = c.copy(weekCoins = it) }
                NumField("اولین «امتیاز بده»", c.rateFirst, "۵", androidx.compose.material.icons.Icons.Filled.RocketLaunch, androidx.compose.ui.graphics.Color(0xFF7C3AED), "بعد از چند بار باز کردن") { c = c.copy(rateFirst = it) }
                NumField("تکرار هر چند بار", c.rateEvery, "۱۵", androidx.compose.material.icons.Icons.Filled.Autorenew, androidx.compose.ui.graphics.Color(0xFFEA580C), "با تکرارِ استفاده، امتیاز بگیر") { c = c.copy(rateEvery = it) }
            }
            AdminSection("متنِ اعلانِ «برگرد»", c.comeBackTitle ?: "پیش‌فرض") {
                AdminNote("«{days}» جای تعدادِ روز می‌نشیند.")
                OutlinedTextField(c.comeBackTitle.orEmpty(), { c = c.copy(comeBackTitle = it.ifBlank { null }) }, label = { Text("عنوان") }, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                TextArea("متن", c.comeBackText.orEmpty(), "{days} روزه سر نزدی…") { c = c.copy(comeBackText = it.ifBlank { null }) }
            }
            AdminSection("پرسش‌های پرتکرار", "${toFa(faq.split(Regex("\\n\\s*\\n")).count { it.trim().lines().size >= 2 })} پرسش") {
                AdminNote("خطِ اول پرسش، خط‌های بعد جواب؛ بینِ دو پرسش یک خطِ خالی. در صفحه‌ی پشتیبانی دیده می‌شود.")
                TextArea("پرسش و جواب", faq, "چطور حساب اضافه کنم؟\nاز دارایی ← …") { faq = it }
            }
            AdminSection("نظرسنجی", c.survey?.question?.takeIf { it.isNotBlank() } ?: "خاموش") {
                val sv = c.survey ?: RemoteSurvey()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("روشن", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(checked = c.survey != null, onCheckedChange = { on -> c = c.copy(survey = if (on) sv.copy(id = sv.id.ifBlank { "s" + System.currentTimeMillis().toString(36) }) else null) })
                }
                if (c.survey != null) {
                    OutlinedTextField(sv.question, { c = c.copy(survey = sv.copy(question = it.take(120))) }, label = { Text("سؤال") }, shape = AdminFieldShape, colors = adminFieldColors(), modifier = Modifier.fillMaxWidth())
                    TextArea("گزینه‌ها (هر خط یکی)", surveyOpts, "بودجه\nگزارش\nچک") { surveyOpts = it }
                    AdminNote("هر کاربر یک بار می‌بیند. برای سؤالِ تازه، خاموش و دوباره روشن کن (شناسه‌ی تازه).")
                    surveyResult?.let { r ->
                        AdminSubTitle("جواب‌ها (${toFa(r.sumOf { it.count })} نفر)")
                        r.forEach { Text("${it.name}: ${toFa(it.count)}", color = AppMuted, fontSize = 12.5.sp) }
                    }
                }
            }
            val next = build()
            GradientButton(
                enabled = !saving && next != RemoteApp.config,
                onClick = {
                    saving = true
                    scope.launch {
                        val json = RemoteApp.toJson(next)
                        val ok = vm.repo.adminSetRemoteConfig("app", json)
                        if (ok) { RemoteApp.update(ctx, json); c = RemoteApp.config }
                        banner.show(if (ok) "ذخیره شد؛ برای همه اعمال می‌شود" else "نرسید؛ اینترنت را چک کن", isSuccess = ok)
                        saving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) { Text(if (next == RemoteApp.config) "تغییری نیست" else "ذخیره برای همه") }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
