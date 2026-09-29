package ir.sadteam.loancalc.ui.admin

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppBg
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

    fun close(id: Long) = viewModelScope.launch { if (repo.adminSupportStatus(id, "closed")) load() }

    suspend fun file(id: String): ByteArray? = repo.adminSupportFile(id)
}

private val STATUS_LABEL = mapOf("open" to "باز", "answered" to "جواب داده شد", "closed" to "بسته")

@Composable
fun SupportInboxScreen(onBack: () -> Unit, viewModel: SupportInboxViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    val banner = rememberInAppBanner()
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.load()
            delay(20_000)
        }
    }
    BackHandler(onBack = onBack)
    Box(Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text("پیام‌های کاربران", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        val open = items?.count { it.status == "open" } ?: 0
                        Text("${toFa(open)} پیامِ بی‌جواب", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            when {
                items == null -> item { Text("در حالِ گرفتن…", color = AppMuted) }
                items!!.isEmpty() -> item { Text("هنوز پیامی نیامده.", color = AppMuted) }
                else -> items(items!!, key = { it.id }) { msg ->
                    SupportMessageCard(msg, viewModel) { ok ->
                        banner.show(if (ok) "جواب فرستاده شد" else "نشد؛ دوباره بزن", isSuccess = ok)
                    }
                }
            }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SupportMessageCard(msg: SupportMessage, viewModel: SupportInboxViewModel, onReplied: (Boolean) -> Unit) {
    var replying by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "کاربرِ ${toFa(msg.userId ?: 0)} · ${msg.ticket}",
                color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f),
            )
            Text(
                STATUS_LABEL[msg.status] ?: msg.status,
                color = AppPrimaryInk, fontSize = 10.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(AppPrimaryPill).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Text(msg.message, color = AppText, fontSize = 12.5.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
        Text(
            listOfNotNull(msg.createdAt, msg.appVersion?.let { "نسخه $it" }, msg.device).joinToString(" · "),
            color = AppMuted, fontSize = 9.5.sp, modifier = Modifier.padding(top = 6.dp),
        )
        if (msg.attachments.isNotEmpty()) {
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                msg.attachments.forEach { AttachmentThumb(it, viewModel) }
            }
        }
        if (replying) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 1000) text = it },
                placeholder = { Text("جواب (در «پیام‌های جیبک»ِ همین کاربر می‌نشیند)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
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
