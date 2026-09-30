package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/** «قوانینِ استفاده» (۸ مهر، متنِ تأییدشده‌ی کاربر). هم از صفحه‌ی ورود (تیکِ پذیرش) هم از تنظیمات باز می‌شود. */
@Composable
internal fun TermsScreen(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(AppBg)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("قوانینِ استفاده", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("آخرین به‌روزرسانی: مهر ۱۴۰۵ · با ورود به جیبک این‌ها را می‌پذیری.", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).navigationBarsPadding(),
        ) {
            SettingsDisclosure(title = "1. جیبک چیست", icon = Icons.Filled.Gavel) {
                SettingsParagraph("جیبک ابزاری برای ثبت و مدیریتِ دخل‌وخرج، حساب‌ها، وام، چک، بودجه و دارایی‌های شخصی است. جیبک بانک، مؤسسه‌ی مالی یا مشاورِ مالی نیست و هیچ پولی را جابه‌جا نمی‌کند.")
            }
            SettingsDisclosure(title = "2. درستیِ اطلاعات", icon = Icons.Filled.Gavel) {
                SettingsParagraph("اعداد و گزارش‌های جیبک بر اساسِ اطلاعاتی است که خودت وارد می‌کنی یا از پیامک و اعلانِ بانک خوانده می‌شود. خواندنِ خودکار ممکن است گاهی اشتباه کند، پس مسئولیتِ بررسیِ درستیِ اعداد با خودِ توست. محاسبه‌های وام و سود و قیمتِ روزِ طلا و ارز تقریبی‌اند؛ پیش از هر تصمیمِ مالی رقمِ دقیق را از بانک یا منبعِ رسمی بپرس. جیبک مسئولِ زیانِ ناشی از تصمیم‌هایی نیست که بر پایه‌ی این اعداد گرفته شود.")
            }
            SettingsDisclosure(title = "3. حساب و ورود", icon = Icons.Filled.Gavel) {
                SettingsParagraph("ورود با شماره‌ی موبایل است و هر شماره یک حساب دارد. نگه‌داریِ گوشی، رمز و قفلِ برنامه با خودِ توست. دوره‌ی مجانیِ کاربرِ تازه برای هر گوشی فقط یک بار است؛ ساختنِ حساب‌های پشتِ‌هم برای گرفتنِ دوباره‌ی آن مجاز نیست.")
            }
            SettingsDisclosure(title = "4. اشتراک و پرداخت", icon = Icons.Filled.Gavel) {
                SettingsParagraph("خریدِ اشتراک فقط از طریقِ کافه‌بازار یا مایکت انجام می‌شود و قوانینِ همان استور درباره‌ی پرداخت و بازگرداندنِ پول برقرار است. اشتراک پس از پایانِ مدتش خودکار تمدید نمی‌شود. با تمام شدنِ اشتراک هیچ داده‌ای پاک یا پنهان نمی‌شود؛ فقط امکاناتِ اشتراکی قفل می‌شوند. سکه‌های داخلِ برنامه ارزشِ پولی ندارند، قابلِ فروش یا تبدیل به پول نیستند و فقط برای خریدِ تم و امکاناتِ ظاهری‌اند.")
            }
            SettingsDisclosure(title = "5. داده‌های تو", icon = Icons.Filled.Gavel) {
                SettingsParagraph("داده‌های مالی‌ات مالِ خودِ توست. ذخیره‌ی ابری فقط برای نگه‌داری و بازگرداندنِ همین داده‌ها روی گوشی‌های خودت است. برای حذفِ حساب و داده‌هایت از سرور، از «پشتیبانی» داخلِ برنامه درخواست بده. جزئیاتِ اینکه چه چیزی جمع می‌شود و چه چیزی نه، در «حریمِ خصوصی» آمده است.")
            }
            SettingsDisclosure(title = "6. استفاده‌ی مجاز", icon = Icons.Filled.Gavel) {
                SettingsParagraph("استفاده از جیبک برای کارِ غیرقانونی، تلاش برای نفوذ به سرور، دور زدنِ قفلِ اشتراک یا کپی و فروشِ برنامه مجاز نیست؛ در این موارد جیبک می‌تواند حساب را مسدود کند.")
            }
            SettingsDisclosure(title = "7. تغییر در قوانین", icon = Icons.Filled.Gavel) {
                SettingsParagraph("این قوانین ممکن است به‌روز شوند. تغییرِ مهم از طریقِ «پیام‌های جیبک» اطلاع داده می‌شود.")
            }
            SettingsDisclosure(title = "8. ارتباط با ما", icon = Icons.Filled.Gavel) {
                SettingsParagraph("برای هر سؤال یا مشکل، از «پشتیبانی» داخلِ برنامه پیام بده. جیبک، تیمِ Sad Team.")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
