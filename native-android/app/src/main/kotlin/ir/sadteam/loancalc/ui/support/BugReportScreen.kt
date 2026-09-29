package ir.sadteam.loancalc.ui.support

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppSurface2
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.settings.SettingsHero
import ir.sadteam.loancalc.core.toFa

private const val MAX_REPORT = 1000
private const val MAX_SHOTS = 4
private const val MAX_VIDEO_BYTES = 20L * 1024 * 1024

/** نوعِ پیام - همان مقادیرِ سمتِ سرور. */
internal val SUPPORT_CATEGORIES = listOf("bug" to "مشکل", "design" to "طراحی", "idea" to "پیشنهاد", "question" to "سؤال")

/** پیوستِ انتخاب‌شده: نشانی در گوشی + آیا فیلم است. */
private data class Attachment(val uri: Uri, val isVideo: Boolean)

/**
 * 🐞 **پشتیبانی** - پیام **مستقیم به سرور** می‌رود (۷ مهر: ایمیل به خواسته‌ی کاربر کاملاً حذف شد).
 * صاحبِ برنامه آن را در «گزارشِ برنامه ← پیام‌های کاربران» می‌بیند و جوابش در «پیام‌های جیبک»ِ
 * همین کاربر می‌نشیند. پیوست فقط عکس یا فیلم؛ عکس روی گوشی کوچک و به JPEG تبدیل می‌شود و
 * سرور هم دوباره می‌سازدش (رجوع کن به `server/SupportFiles.kt`).
 */
@Composable
fun BugReportScreen(onBack: () -> Unit, authViewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val clipboard: ClipboardManager = LocalClipboardManager.current
    val banner = rememberInAppBanner()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val gateState by authViewModel.gateState.collectAsState()
    var message by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var ticket by rememberSaveable { mutableStateOf<String?>(null) }
    var category by rememberSaveable { mutableStateOf("bug") }

    var shots by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_SHOTS),
    ) { uris ->
        val added = uris.take(MAX_SHOTS - shots.size).map { uri ->
            Attachment(uri, context.contentResolver.getType(uri)?.startsWith("video/") == true)
        }
        shots = shots + added
    }

    val device = remember { "${Build.MANUFACTURER} ${Build.MODEL} · اندروید ${Build.VERSION.RELEASE}" }

    /** عکس → کوچک + JPEG (متادیتا و مکان هم دور ریخته می‌شود)؛ فیلم → همان بایت‌ها با سقفِ حجم. */
    suspend fun prepare(a: Attachment): Pair<ByteArray, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            if (a.isVideo) {
                val size = context.contentResolver.openAssetFileDescriptor(a.uri, "r")?.use { it.length } ?: -1L
                if (size <= 0 || size > MAX_VIDEO_BYTES) return@runCatching null
                context.contentResolver.openInputStream(a.uri)!!.use { it.readBytes() } to "video/mp4"
            } else {
                val bmp = context.contentResolver.openInputStream(a.uri)!!.use { android.graphics.BitmapFactory.decodeStream(it) }
                    ?: return@runCatching null
                val scale = minOf(1f, 1600f / maxOf(bmp.width, bmp.height))
                val scaled = android.graphics.Bitmap.createScaledBitmap(
                    bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true,
                )
                val out = java.io.ByteArrayOutputStream()
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                out.toByteArray() to "image/jpeg"
            }
        }.getOrNull()
    }

    BackHandler(onBack = onBack)
    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text("پشتیبانی", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(
                            "چی درست کار نکرد؟ هرچه دقیق‌تر، زودتر درست می‌شود.",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                SettingsHero(
                    icon = Icons.Filled.BugReport,
                    title = "حرفت رو بزن",
                    subtitle = "مشکل، ایده‌ی طراحی یا پیشنهاد - اگه به بهتر شدنِ جیبک کمک کنه، هدیه‌ی اشتراک می‌گیری.",
                )
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SUPPORT_CATEGORIES.forEach { (key, label) ->
                        ir.sadteam.loancalc.ui.components.AppChip(
                            label = label,
                            selected = category == key,
                            onClick = { category = key },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                AppCard {
                    Text("توضیحِ مشکل", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(
                        value = message,
                        onValueChange = { if (it.length <= MAX_REPORT) message = it },
                        placeholder = {
                            Text(
                                "مثلاً: تو تبِ وام، دکمه‌ی پرداخت را می‌زنم و هیچ اتفاقی نمی‌افتد.",
                                fontSize = 11.sp,
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 140.dp),
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "نسخه و مدلِ گوشی خودکار اضافه می‌شود.",
                            color = AppLabel,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${toFa(message.length)}/${toFa(MAX_REPORT)}",
                            color = AppMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        shots.forEach { file ->
                            Box(modifier = Modifier.padding(end = 8.dp).size(56.dp)) {
                                AsyncImage(
                                    model = file.uri,
                                    contentDescription = "عکسِ پیوست",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(AppBg.copy(alpha = 0.8f))
                                        .pressScaleClickable { shots = shots - file },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذف", tint = AppText, modifier = Modifier.size(13.dp))
                                }
                                if (file.isVideo) {
                                    Icon(
                                        Icons.Filled.PlayCircle, contentDescription = "فیلم", tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center).size(22.dp),
                                    )
                                }
                            }
                        }
                        if (shots.size < MAX_SHOTS) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppSurface2)
                                    .pressScaleClickable {
                                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (shots.isEmpty()) "عکس یا فیلم (تا ${toFa(MAX_SHOTS)})" else "یکی دیگر",
                                    color = AppPrimaryInk,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                    }
                }
            }

            item {
                GradientButton(
                    enabled = message.trim().length >= 5 && !sending,
                    onClick = {
                        if (gateState != GateState.LOGGED_IN) {
                            banner.show("برای فرستادنِ پیام اول وارد حسابت شو")
                            return@GradientButton
                        }
                        sending = true
                        scope.launch {
                            val prepared = shots.map { prepare(it) }
                            val skipped = prepared.count { it == null }
                            authViewModel.reportBug(
                                message = message.trim(),
                                appVersion = BuildConfig.VERSION_NAME,
                                device = device,
                                attachments = prepared.filterNotNull(),
                                category = category,
                            ) { code ->
                                sending = false
                                ticket = code
                                when {
                                    code == null -> banner.show("فرستاده نشد؛ اینترنت را چک کن و دوباره بزن")
                                    skipped > 0 -> banner.show("پیام رفت؛ ${toFa(skipped)} فیلمِ بزرگ‌تر از ۲۰ مگ جا ماند")
                                    else -> {
                                        message = ""
                                        shots = emptyList()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            if (sending) "در حالِ ارسال…" else "ارسال",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }

            ticket?.let { code ->
                item {
                    AppCard {
                        Text("کدِ پیگیری", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // کد لاتین است، پس مثلِ شماره‌ی موبایل چپ‌به‌راست می‌مانَد.
                            Ltr {
                                Text(
                                    code,
                                    color = AppPrimaryInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(AppPrimaryPill)
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString(code))
                                banner.show("کد کپی شد", isSuccess = true)
                            }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "کپی", tint = AppMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            "💚 ممنون که وقت گذاشتی! پیامت به دستمون رسید و به‌زودی نتیجه‌اش توی «پیام‌های جیبک» " +
                                "بهت اطلاع داده می‌شه. اگه به بهتر شدنِ جیبک کمک کنه، هدیه‌ی اشتراک هم می‌گیری.",
                            color = AppMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            if (gateState != GateState.LOGGED_IN) {
                item {
                    Text(
                        "برای فرستادنِ پیام باید وارد حسابت باشی.",
                        color = AppDangerInk,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
