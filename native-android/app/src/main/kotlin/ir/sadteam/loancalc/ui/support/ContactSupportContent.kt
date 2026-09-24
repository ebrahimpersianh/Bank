package ir.sadteam.loancalc.ui.support

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText

/** «تماس با ما» - بسته‌ی ChatGPT (۳ مهر)، به‌جای AlertDialogِ پیش‌فرض. */
@Composable
fun ContactSupportContent(onClose: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    // بنرِ خودش: بنرِ تنظیمات زیرِ این پوششِ تمام‌صفحه دیده نمی‌شود.
    val banner = rememberInAppBanner()
    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().background(AppBg).statusBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("تماس با ما", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("راهِ ارتباطِ مستقیم با تیمِ جیبک", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(AppPrimaryPill, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Email, null, tint = AppPrimaryInk, modifier = Modifier.size(22.dp)) }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("ایمیلِ پشتیبانی", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Ltr {
                        Text(SUPPORT_EMAIL, color = AppPrimaryInk, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    onClick = { clipboard.setText(AnnotatedString(SUPPORT_EMAIL)); banner.show("ایمیل کپی شد", isSuccess = true) },
                    variant = AppButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Text("کپیِ ایمیل", fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
                }
                GradientButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")))
                        } catch (e: ActivityNotFoundException) {
                            banner.show("اپِ ایمیلی پیدا نشد؛ نشانی را کپی کن")
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Send, null, modifier = Modifier.size(16.dp))
                    Text("ارسالِ ایمیل", fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        AppCard {
            Text("پیشنهاد یا بازخورد داری؟", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(
                "حتی یک جمله هم به بهترشدنِ جیبک کمک می‌کند. پاسخ‌ها از همین نشانی پیگیری می‌شوند.",
                color = AppMuted, fontSize = 11.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 7.dp),
            )
        }
    }
    InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
