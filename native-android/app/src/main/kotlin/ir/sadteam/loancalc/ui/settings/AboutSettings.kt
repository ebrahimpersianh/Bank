package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.ui.support.ContactSupportContent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.components.InAppBannerState
import ir.sadteam.loancalc.ui.components.JibakLogo
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Build
import android.os.Build
import ir.sadteam.loancalc.core.toFa

@Composable
internal fun AboutSettings(banner: InAppBannerState, onOpenBugReport: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showContact by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    // ۷ ضربه روی «جیبک» = داده‌ی نمونه برای عکس‌های استور (فقط روی برنامه‌ی خالی - `SampleData.kt`).
    var logoTaps by remember { mutableStateOf(0) }
    var showSample by remember { mutableStateOf(false) }
    val sampleVm: SampleDataViewModel = hiltViewModel()
    val sampleScope = rememberCoroutineScope()
    if (showSample) {
        JibakAlertDialog(
            onDismissRequest = { showSample = false },
            title = { Text("داده‌ی نمونه برای عکس", fontWeight = FontWeight.Black) },
            text = { Text("چند حساب، تراکنش، بودجه، وام، چک و داراییِ ساختگی ثبت می‌شود تا برای عکس‌های استور آماده باشد. فقط روی برنامه‌ی خالی کار می‌کند؛ با حسابِ تست انجامش بده.") },
            confirmButton = {
                TextButton(onClick = {
                    showSample = false
                    sampleScope.launch {
                        val ok = sampleVm.fill()
                        banner.show(if (ok) "داده‌ی نمونه ثبت شد" else "برنامه خالی نیست - اول با حسابِ تست وارد شو", isSuccess = ok)
                    }
                }) { Text("پر کن") }
            },
            dismissButton = { TextButton(onClick = { showSample = false }) { Text("نه") } },
        )
    }

    // ── بلوکِ نشان - **تنها جای تنظیمات که چیزی بی‌کارت رو بستر می‌شینه** ───────
    // همین تفاوته که این صفحه رو «صفحه‌ی هویت» می‌کنه نه یه فهرستِ دیگه.
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        JibakLogo(width = 80.dp)
        Text(
            "جیبک",
            color = AppText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 14.dp).clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
            ) { if (++logoTaps >= 7) { logoTaps = 0; showSample = true } },
        )
        // ⚠️ **ضربه‌ی طولانی رو نسخه، اطلاعاتِ فنی رو کپی می‌کنه.** ارزون‌ترین کاری که
        // می‌شه برای پشتیبانی کرد - بی این، هر گفت‌وگو با سه پرسشِ اضافه شروع می‌شه.
        // ضربه‌ی معمولی عمداً هیچ کاری نمی‌کنه.
        Ltr {
            Text(
                "${toFa(BuildConfig.VERSION_NAME)} (${toFa(BuildConfig.VERSION_CODE)})",
                color = AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .pressScaleClickable(scale = 0.98f) {
                        clipboard.setText(AnnotatedString(diagnosticsText()))
                        banner.show("اطلاعاتِ فنی کپی شد", isSuccess = true)
                    },
            )
        }
    }

    // ── گروهِ کمک ─────────────────────────────────────────────────────────────
    // ⚠️ ردیفِ «راهنما» عمداً نیست: محتوای پرسش‌های پرتکرار هنوز نوشته نشده، و ردیفی
    // که به صفحه‌ی خالی می‌ره بدتر از نبودنشه (همون قاعده‌ی ردیفِ ایمیل).
    SettingsGroupLabel("کمک")
    SettingsGroup(modifier = Modifier.padding(top = 28.dp)) {
        // 🐞 **بالای «تماس با ما»** و نه زیرش: کسی که مشکل دارد اول این را می‌خواهد،
        // و این مسیر گزارش را روی سرور هم ثبت می‌کند (شرطِ هدیه‌ی اشتراک).
        // ۷ مهر: «گزارشِ مشکل» و «تماس با ما» یکی شدند - یک راه: پیامِ مستقیم به سرور.
        SettingsRowItem(
            title = "پشتیبانی",
            icon = Icons.Filled.SupportAgent,
            tone = SettingsTone.GREEN,
            status = "پیام، مشکل یا پیشنهاد - با عکس یا فیلم",
            onClick = { onOpenBugReport() },
        )
        SettingsDivider()
        SettingsRowItem(
            // مقصد از **فلیورِ نصب‌شده** میاد، نه یه لینکِ ثابت - ردیفی که به فروشگاهِ
            // اشتباه بره بدتر از نبودنشه.
            title = if (BuildConfig.FLAVOR == "myket") "امتیاز در مایکت" else "امتیاز در کافه‌بازار",
            icon = Icons.Filled.Star,
            tone = SettingsTone.ORANGE,
            onClick = {
                val storeUrl = if (BuildConfig.FLAVOR == "myket") {
                    "https://myket.ir/app/ir.sadteam.loancalc"
                } else {
                    "https://cafebazaar.ir/app/ir.sadteam.loancalc"
                }
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
                } catch (e: ActivityNotFoundException) {
                    banner.show("اپِ فروشگاه رو گوشیت پیدا نشد")
                }
            },
        )
    }

    // ── گروهِ متن‌ها ──────────────────────────────────────────────────────────
    SettingsGroupLabel("متن‌ها")
    SettingsGroup {
        SettingsRowItem(
            title = "حریمِ خصوصی",
            icon = Icons.Filled.Shield,
            tone = SettingsTone.NEUTRAL,
            onClick = { showPrivacy = true },
        )
        val guideAuthVm: ir.sadteam.loancalc.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
        SettingsRowItem(
            title = "راهنمای برنامه",
            icon = Icons.Filled.Explore,
            tone = SettingsTone.NEUTRAL,
            onClick = { guideAuthVm.replayTour() },
        )
        SettingsRowItem(
            title = "قوانینِ استفاده",
            icon = Icons.Filled.Gavel,
            tone = SettingsTone.NEUTRAL,
            onClick = { showTerms = true },
        )
        // ذکرِ منبعِ قیمت (Servix) به تصمیمِ صریحِ کاربر (۳ مهر) از کلِ برنامه حذف شد.
    }

    Text(
        "ساخته‌شده در ایران · ۱۴۰۵",
        color = AppLabel,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
    )

    if (showContact) {
        FullScreenDialog(onDismissRequest = { showContact = false }) {
            ContactSupportContent(onClose = { showContact = false })
        }
    }
    if (showPrivacy) {
        FullScreenDialog(onDismissRequest = { showPrivacy = false }) {
            PrivacyPolicyScreen(onBack = { showPrivacy = false })
        }
    }
    if (showTerms) {
        FullScreenDialog(onDismissRequest = { showTerms = false }) {
            TermsScreen(onBack = { showTerms = false })
        }
    }
}
/**
 * اطلاعاتِ فنی برای پشتیبانی - نسخه، بیلد، مدلِ گوشی، اندروید، و فلیور.
 * عمداً **شناسه‌ی کاربر توش نیست**: شماره‌ی موبایل داده‌ی شخصیه و کپیِ ناخواسته‌ش
 * می‌تونه جایی که نباید پیست بشه.
 */
private fun diagnosticsText(): String = buildString {
    append("نسخه: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
    append("فروشگاه: ${BuildConfig.FLAVOR}\n")
    append("گوشی: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
    append("اندروید: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})\n")
    append(ir.sadteam.loancalc.crash.SlowMainWatcher.report())
}
@Composable
private fun SupportRow(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScaleClickable(scale = 0.98f, onClick = onClick)
            .background(AppBg, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(label, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
// نسخه‌ی «۱» قبلاً هاردکد بود (همیشه ثابت، هیچ‌وقت آپدیت نمی‌شد) - خواسته‌ی کاربر: نسخه‌ی واقعیِ
// نصب‌شده رو نشون بده. BuildConfig.VERSION_NAME همون versionNameِ CI (مثلاً "1.0.332") ئه.
private const val privacyText = "چه اطلاعاتی ذخیره می‌شه؟\n" +
    "وام‌ها، تنظیمات و یادآوری‌هایی که تو اپ می‌سازی، فقط روی گوشی خودت ذخیره می‌شن. این اپ هیچ " +
    "تبلیغ، ابزار ردیابی (analytics) یا کد شخص ثالثی نداره و اطلاعاتت رو به‌جایی نمی‌فروشه.\n\n" +
    "ورود با شماره تلفن\n" +
    "بدون ورود هم می‌تونی از اپ به‌عنوان مهمان استفاده کنی. اگه با شماره موبایل وارد بشی، فقط " +
    "شماره‌ت و لیست وام‌هات (برای همگام‌سازی بین گوشی‌هات) روی سرور اختصاصی همین اپ ذخیره می‌شه؛ " +
    "این اطلاعات جای دیگه‌ای فرستاده نمی‌شه و در اختیار شرکت یا سرویس ثالثی قرار نمی‌گیره.\n\n" +
    "اشتراک\n" +
    "بدون اشتراک فقط یک وام قابل ذخیره‌ست؛ برای ذخیره‌ی وام بیشتر اول باید وارد بشی و بعد اشتراک " +
    "تهیه کنی.\n\n" +
    // ⚠️ این بند **اجباریه**: اپ هم پیامک می‌خونه هم اعلان، و کاربری که مجوزِ عجیبی داده و
    // توضیحش رو تو متن پیدا نمی‌کنه، اپ رو پاک می‌کنه. مسئله‌ی اعتماده، نه حقوقی.
    "خواندنِ پیامک و اعلانِ بانکی\n" +
    "اگه خودت این قابلیت رو روشن کنی (پیش‌فرض خاموشه)، اپ متنِ پیامک‌های بانکی و اعلان‌های " +
    "بانک‌ها رو می‌خونه تا مبلغ و نوعِ تراکنش رو دربیاره و خودکار ثبتش کنه. این خوندن " +
    "**کاملاً روی خودِ گوشیه**: هیچ پیامکی، هیچ اعلانی و هیچ تکه‌ای از متنشون به هیچ سروری " +
    "— نه سرورِ ما، نه جای دیگه — فرستاده نمی‌شه. هر لحظه می‌تونی از تنظیمات خاموشش کنی."
