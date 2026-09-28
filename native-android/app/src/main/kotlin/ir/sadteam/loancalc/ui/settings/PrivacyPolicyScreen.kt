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

/** «حریمِ خصوصی» تمام‌صفحه و بخش‌بندی‌شده - بسته‌ی ChatGPT (۳ مهر)؛ متنِ قدیمیِ «فقط وام» کهنه بود. */
@Composable
internal fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(AppBg)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("حریمِ خصوصی", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("شفاف و خلاصه؛ هر بخش را خواستی باز کن.", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).navigationBarsPadding(),
        ) {
            SettingsDisclosure(title = "چه چیزی روی گوشی می‌ماند", icon = Icons.Filled.PhoneAndroid) {
                SettingsParagraph("وام‌ها، حساب‌ها، تراکنش‌ها، بودجه، دارایی‌ها، چک‌ها و تنظیمات روی گوشیِ خودت و در یک پایگاه‌داده‌ی رمزنگاری‌شده ذخیره می‌شوند.")
            }
            SettingsDisclosure(title = "چه چیزی به سرورِ ما می‌رود", icon = Icons.Filled.Cloud) {
                SettingsParagraph("شماره‌ی موبایلت برای ورود، و داده‌هایت برای همگام‌سازی بین گوشی‌ها و پشتیبانِ ابری، روی سرورِ اختصاصیِ جیبک نگه‌داری می‌شوند و به هیچ شرکت یا سرویسِ دیگری داده نمی‌شوند. قیمتِ طلا و ارز هم از سرورِ خودِ جیبک گرفته می‌شود.")
            }
            SettingsDisclosure(title = "چه چیزی هرگز جمع نمی‌شود", icon = Icons.Filled.Shield) {
                SettingsParagraph("جیبک هیچ تبلیغ، ابزارِ ردیابی یا کدِ شخصِ ثالث ندارد و اطلاعاتت را به هیچ‌کس نمی‌فروشد.")
            }
            SettingsDisclosure(title = "مجوزها؛ پیامک و تقویم اختیاری‌اند", icon = Icons.Filled.Info) {
                SettingsParagraph("مجوزِ پیامک و تقویم به‌طورِ پیش‌فرض خاموش است و فقط وقتی همان قابلیت را خودت روشن کنی درخواست می‌شود. پیامکِ بانکی فقط روی خودِ گوشی خوانده می‌شود و به هیچ سروری نمی‌رود.")
            }
            SettingsDisclosure(title = "حذفِ حساب", icon = Icons.Filled.DeleteForever) {
                SettingsParagraph("از بخشِ حساب می‌توانی حسابت را حذف کنی؛ با این کار داده‌هایت از سرورِ جیبک پاک می‌شوند. داده‌های روی گوشی هم با پاک‌کردنِ داده‌ی برنامه حذف می‌شوند.")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
