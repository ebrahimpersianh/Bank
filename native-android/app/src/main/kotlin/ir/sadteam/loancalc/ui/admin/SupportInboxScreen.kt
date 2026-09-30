package ir.sadteam.loancalc.ui.admin

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.SupportAttachment
import ir.sadteam.loancalc.data.network.SupportMessage
import ir.sadteam.loancalc.notifications.SupportAlerts
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SupportInboxViewModel @Inject constructor(
    private val repo: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _items = MutableStateFlow<List<SupportMessage>?>(null)
    val items: StateFlow<List<SupportMessage>?> = _items
    private val _newCount = MutableStateFlow(0)
    /** پیام‌های تازه از آخرین بار (برای بنرِ «پیامِ تازه» وقتی برنامه باز است). */
    val newCount: StateFlow<Int> = _newCount
    private var watching = false

    fun load() = viewModelScope.launch { _items.value = repo.adminSupport()?.items ?: emptyList() }

    /** فقط برای ادمین: هر ۲۰ ثانیه وقتی برنامه باز است + کارِ پس‌زمینه‌ی ۱۵ دقیقه‌ای. */
    fun watch() {
        if (watching) return
        watching = true
        viewModelScope.launch {
            if (!repo.isAdmin()) return@launch
            SupportAlerts.schedule(context)
            while (true) {
                val n = runCatching { SupportAlerts.check(context, repo) }.getOrDefault(0)
                if (n > 0) _newCount.value += n
                delay(20_000)
            }
        }
    }

    fun consumeNew() { _newCount.value = 0 }

    fun reply(id: Long, text: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = repo.adminSupportReply(id, text)
        if (ok) load()
        done(ok)
    }

    /** هدیه‌ی مستقیم به شماره‌ی کاربری (از صفحه‌ی ادمین، نه از یک پیام). */
    fun giftUser(user: String, days: Int, coins: Int, text: String, done: (String?) -> Unit) = viewModelScope.launch {
        done(repo.adminGift(user, days, coins, text))
    }

    fun gift(id: Long, days: Int, text: String, done: (String?) -> Unit) = viewModelScope.launch {
        val err = repo.adminSupportGift(id, days, text)
        if (err == null) load()
        done(err)
    }

    fun close(id: Long) = viewModelScope.launch { if (repo.adminSupportStatus(id, "closed")) load() }

    suspend fun file(id: String): ByteArray? = repo.adminSupportFile(id)
}

private val CATEGORY_LABEL = ir.sadteam.loancalc.ui.support.SUPPORT_CATEGORIES.toMap()
private val GIFT_DAYS = (1..10).toList()

private fun defaultGiftText(category: String, days: Int): String {
    val what = when (category) {
        "design" -> "ایده‌ی طراحی‌ات"
        "idea" -> "پیشنهادت"
        else -> "گزارشت"
    }
    return "ممنون بابتِ $what! به پاسِ کمکت به بهتر شدنِ جیبک، ${toFa(days)} روز اشتراکِ هدیه برات فعال شد 💚"
}

private val STATUS_LABEL = mapOf("open" to "باز", "answered" to "جواب داده شد", "closed" to "بسته")
private val FILTERS = listOf("open" to "باز", "answered" to "جواب‌داده", "closed" to "بسته")

/** شناسه‌ی کاربر همیشه `Uid:7405024` با رقمِ لاتین - مثلِ استثنای «نسخه». */
internal fun supportUid(id: Long?): String = id?.let { "Uid:$it" } ?: "مهمان"
internal fun supportUid(id: Int?): String = supportUid(id?.toLong())

/** `2026-09-30T10:12:00` → «۸ مهر · ۱۰:۱۲». */
internal fun supportTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    val time = createdAt.drop(11).take(5).takeIf { it.length == 5 && it[2] == ':' }
    return listOfNotNull(adminDay(createdAt), time?.let { toFa(it) }).joinToString(" · ")
}

/** 💬 پیام‌های کاربران (بخشِ ۸۲): زبانه‌ی وضعیت با شمارش، کارتِ بسته که با تپ باز می‌شود. */
@Composable
fun SupportInboxScreen(onBack: () -> Unit, viewModel: SupportInboxViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    val banner = rememberInAppBanner()
    var filter by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.load()
            delay(20_000)
        }
    }
    BackHandler(onBack = onBack)
    val all = items
    val counts = FILTERS.map { (k, _) -> all?.count { it.status == k } ?: 0 }
    val shown = all.orEmpty().filter { it.status == FILTERS[filter].first }
    Box(Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { AdminHeader("پیام‌های کاربران", "${toFa(counts[0])} پیامِ بی‌جواب", onBack) }
            item {
                AdminTabs(
                    tabs = FILTERS.mapIndexed { i, (_, label) -> "$label ${toFa(counts[i])}" },
                    selected = filter,
                    onSelect = { filter = it },
                    dots = if (counts[0] > 0) setOf(0) else emptySet(),
                )
            }
            when {
                all == null -> item { Text("در حالِ گرفتن…", color = AppMuted) }
                shown.isEmpty() -> item { Text(if (filter == 0) "همه جواب گرفته‌اند." else "پیامی در این دسته نیست.", color = AppMuted, modifier = Modifier.padding(8.dp)) }
                else -> items(shown, key = { it.id }) { msg ->
                    SupportMessageCard(msg, viewModel, onGiftResult = { err ->
                        banner.show(
                            when (err) {
                                null -> "هدیه و پیامش فرستاده شد"
                                "already_rewarded" -> "این پیام قبلاً هدیه گرفته"
                                else -> "نشد؛ دوباره بزن"
                            },
                            isSuccess = err == null,
                        )
                    }) { ok ->
                        banner.show(if (ok) "جواب فرستاده شد" else "نشد؛ دوباره بزن", isSuccess = ok)
                    }
                }
            }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SupportMessageCard(
    msg: SupportMessage,
    viewModel: SupportInboxViewModel,
    onGiftResult: (String?) -> Unit,
    onReplied: (Boolean) -> Unit,
) {
    var expanded by rememberSaveable(msg.id) { mutableStateOf(false) }
    var replying by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var gifting by remember { mutableStateOf(false) }
    var giftDays by remember { mutableStateOf(3) }
    var giftText by remember { mutableStateOf(defaultGiftText(msg.category, 3)) }
    val (pill, ink) = when (msg.status) {
        "open" -> AppDangerPill to AppDangerInk
        "answered" -> AppPrimaryPill to AppPrimaryInk
        else -> AppSurface2 to AppMuted
    }
    AppCard(modifier = Modifier.clickable { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(CATEGORY_LABEL[msg.category] ?: "پیام", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
            Ltr { Text(supportUid(msg.userId), color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) }
            Box(Modifier.weight(1f))
            if (msg.attachments.isNotEmpty()) Icon(Icons.Filled.Attachment, "پیوست", tint = AppLabel, modifier = Modifier.padding(end = 6.dp).size(16.dp))
            Text(
                STATUS_LABEL[msg.status] ?: msg.status,
                color = ink, fontSize = 11.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(pill).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Text(
            msg.message, color = AppText, fontSize = 12.5.sp, lineHeight = 22.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(supportTime(msg.createdAt), if (expanded) msg.appVersion?.let { "نسخه $it" } else null, if (expanded) msg.device else null, if (expanded) msg.ticket else null).joinToString(" · "),
                color = AppLabel, fontSize = 11.sp, modifier = Modifier.weight(1f),
            )
            if (msg.rewardedDays > 0) {
                Row(
                    Modifier.clip(RoundedCornerShape(999.dp)).background(AppGoldPillSoft).padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CardGiftcard, null, tint = AppGoldInkSoft, modifier = Modifier.size(14.dp))
                    Text("${toFa(msg.rewardedDays)} روز هدیه", color = AppGoldInkSoft, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        if (!expanded) return@AppCard

        if (msg.attachments.isNotEmpty()) {
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                msg.attachments.forEach { AttachmentThumb(it, viewModel) }
            }
        }
        if (gifting) {
            Row(Modifier.fillMaxWidth().padding(top = 10.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GIFT_DAYS.forEach { d ->
                    ir.sadteam.loancalc.ui.components.AppChip(
                        label = "${toFa(d)} روز",
                        selected = giftDays == d,
                        onClick = {
                            // متن فقط اگر دست نخورده باشد با روزِ تازه به‌روز می‌شود.
                            if (giftText == defaultGiftText(msg.category, giftDays)) giftText = defaultGiftText(msg.category, d)
                            giftDays = d
                        },
                    )
                }
            }
            OutlinedTextField(
                value = giftText,
                onValueChange = { if (it.length <= 1000) giftText = it },
                label = { Text("متنی که همراهِ هدیه می‌رود", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
            )
            GradientButton(
                onClick = {
                    if (giftText.isNotBlank()) viewModel.gift(msg.id, giftDays, giftText.trim()) { err ->
                        if (err == null) gifting = false
                        onGiftResult(err)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("فرستادنِ ${toFa(giftDays)} روز هدیه") }
        }
        if (replying) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 1000) text = it },
                placeholder = { Text("جواب (در «پیام‌های جیبک»ِ همین کاربر می‌نشیند)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton(
                onClick = {
                    if (!replying) replying = true
                    else if (text.isNotBlank()) viewModel.reply(msg.id, text.trim()) { ok ->
                        if (ok) { replying = false; text = "" }
                        onReplied(ok)
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text(if (replying) "فرستادنِ جواب" else "جواب") }
            if (msg.rewardedDays == 0) {
                GradientButton(onClick = { gifting = !gifting }, variant = AppButtonVariant.SECONDARY) {
                    Icon(Icons.Filled.CardGiftcard, null, modifier = Modifier.size(18.dp))
                    Text("هدیه", modifier = Modifier.padding(start = 4.dp))
                }
            }
            if (msg.status != "closed") {
                GradientButton(onClick = { viewModel.close(msg.id) }, variant = AppButtonVariant.SECONDARY) { Text("بستن") }
            }
        }
    }
}

/** پیش‌نمایشِ پیوست؛ عکس همین‌جا، فیلم با پخش‌کننده‌ی گوشی (از پوشه‌ی موقتِ خودِ برنامه). */
@Composable
private fun AttachmentThumb(a: SupportAttachment, viewModel: SupportInboxViewModel) {
    val context = LocalContext.current
    var bytes by remember(a.id) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(a.id) { if (a.kind == "image") bytes = viewModel.file(a.id) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Box(
        Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(AppSurface2).clickable {
            scope.launch {
                val data = bytes ?: viewModel.file(a.id) ?: return@launch
                val dir = File(context.cacheDir, "support_view").apply { mkdirs() }
                val f = File(dir, a.id + if (a.kind == "video") ".mp4" else ".jpg").apply { writeBytes(data) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).setDataAndType(uri, if (a.kind == "video") "video/mp4" else "image/jpeg")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        val bmp = remember(bytes) { bytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
        if (bmp != null) {
            androidx.compose.foundation.Image(bmp, "عکس", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(if (a.kind == "video") Icons.Filled.PlayCircle else Icons.Filled.Image, null, tint = AppMuted, modifier = Modifier.size(28.dp))
        }
    }
}
