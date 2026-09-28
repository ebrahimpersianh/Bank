package ir.sadteam.loancalc.ui.support

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppSurface2
import java.io.File
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

/** همان صندوقی که «تماس با ما» هم به آن می‌فرستد - یک نشانی، نه دو تا. */
const val SUPPORT_EMAIL = "jibak.support@gmail.com"

private const val MAX_REPORT = 1000
private const val MAX_SHOTS = 3

/**
 * 🐞 **گزارشِ مشکل** - خواسته‌ی کاربر (۳۱ شهریور).
 *
 * دو کار پشتِ‌هم انجام می‌شود و **ترتیبش عمدی است**:
 *
 * ۱. گزارش روی **سرور** ثبت می‌شود و یک **کدِ پیگیری** می‌گیرد. این قدم است که گزارش را
 *    به حسابِ کاربر می‌چسباند - بی آن، هدیه‌دادن ممکن نیست چون ایمیل نمی‌گوید فرستنده
 *    کدام حسابِ برنامه است (کاربر از ایمیلِ شخصی‌اش می‌فرستد).
 * ۲. بعد صندوقِ ایمیلِ گوشی با **موضوعِ آماده‌ی «مشکل برنامه»** و متنِ پرشده باز می‌شود.
 *
 * ⚠️ اگر ثبتِ سرور نشد (اینترنت قطع)، باز هم ایمیل باز می‌شود ولی **بی کدِ پیگیری** و با
 * یک هشدارِ صریح؛ چیزی بی‌صدا از دست نمی‌رود.
 */
@Composable
fun BugReportScreen(onBack: () -> Unit, authViewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val clipboard: ClipboardManager = LocalClipboardManager.current
    val banner = rememberInAppBanner()
    val gateState by authViewModel.gateState.collectAsState()
    var message by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var ticket by rememberSaveable { mutableStateOf<String?>(null) }

    // عکس‌ها به پوشه‌ی موقتِ خودِ برنامه کپی می‌شوند تا بشود با FileProvider به ایمیل داد؛
    // هیچ‌کدام به سرورِ ما نمی‌رود.
    var shots by remember { mutableStateOf<List<File>>(emptyList()) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_SHOTS),
    ) { uris ->
        val dir = File(context.cacheDir, "bug_shots").apply { mkdirs() }
        val copied = uris.take(MAX_SHOTS - shots.size).mapNotNull { uri ->
            runCatching {
                val out = File(dir, "shot_${System.nanoTime()}.jpg")
                context.contentResolver.openInputStream(uri)!!.use { input -> out.outputStream().use { input.copyTo(it) } }
                out
            }.getOrNull()
        }
        shots = shots + copied
    }

    val device = remember { "${Build.MANUFACTURER} ${Build.MODEL} · اندروید ${Build.VERSION.RELEASE}" }

    fun openEmail(withTicket: String?) {
        val body = buildString {
            append(message.trim())
            append("\n\n---\n")
            if (withTicket != null) append("کدِ پیگیری: ").append(withTicket).append("\n")
            append("نسخه: ").append(BuildConfig.VERSION_NAME).append("\n")
            append("دستگاه: ").append(device)
        }
        // 🚨 موضوع **همیشه** «مشکل برنامه» است (خواسته‌ی صریحِ کاربر): صندوقِ پشتیبانی
        // با یک موضوعِ ثابت قابلِ فیلترکردن است، ولی با موضوعِ دست‌نوشته نه.
        val uri = Uri.parse(
            "mailto:" + Uri.encode(SUPPORT_EMAIL) +
                "?subject=" + Uri.encode("مشکل برنامه") +
                "&body=" + Uri.encode(body),
        )
        try {
            if (shots.isEmpty()) {
                context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
            } else {
                // ضمیمه با SENDTO نمی‌رود؛ SEND_MULTIPLE + selectorِ mailto تا فقط اپ‌های ایمیل بیایند.
                val uris = arrayListOf<Uri>().apply {
                    shots.forEach { add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)) }
                }
                val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "مشکل برنامه")
                    putExtra(Intent.EXTRA_TEXT, body)
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                }
                context.startActivity(send)
            }
        } catch (e: ActivityNotFoundException) {
            banner.show("اپ ایمیلی پیدا نشد؛ کدِ پیگیری را نگه دار و از راهِ دیگری بفرست")
        }
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
                        Text("گزارشِ مشکل", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
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
                    title = "یه مشکل دیدی؟",
                    subtitle = "جزئیاتش را بنویس؛ اگر به رفعش کمک کند هدیه‌ی اشتراک می‌گیری.",
                )
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
                                    model = file,
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
                                        .pressScaleClickable {
                                            file.delete()
                                            shots = shots - file
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذف", tint = AppText, modifier = Modifier.size(13.dp))
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
                                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (shots.isEmpty()) "افزودنِ عکس (تا ${toFa(MAX_SHOTS)})" else "عکسِ دیگر",
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
                            // بی ورود، گزارش به هیچ حسابی بسته نمی‌شود؛ ایمیل باز می‌شود
                            // ولی صادقانه گفته می‌شود که هدیه‌ای در کار نیست.
                            banner.show("برای گرفتنِ کدِ پیگیری و هدیه باید وارد حساب شوی")
                            openEmail(null)
                            return@GradientButton
                        }
                        sending = true
                        authViewModel.reportBug(
                            message = message.trim(),
                            appVersion = BuildConfig.VERSION_NAME,
                            device = device,
                        ) { code ->
                            sending = false
                            ticket = code
                            if (code == null) {
                                banner.show("ثبت روی سرور نشد؛ ایمیل بی کدِ پیگیری باز می‌شود")
                            }
                            openEmail(code)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            if (sending) "در حالِ ثبت…" else "ثبت و ارسالِ ایمیل",
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
                            "این کد روی سرور به حسابِ تو وصل است؛ اگر گزارشت به رفعِ مشکل کمک کند، " +
                                "هدیه‌ی اشتراک به همین حساب داده می‌شود.",
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
                        "برای ثبتِ گزارش روی سرور (و گرفتنِ هدیه) باید وارد حسابت باشی.",
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
