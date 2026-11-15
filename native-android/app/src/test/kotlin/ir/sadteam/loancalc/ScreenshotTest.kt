package ir.sadteam.loancalc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.ThemeMode
import org.junit.Rule
import org.junit.Test

/**
 * تستِ اسکرین‌شاتِ Compose رو JVM (بدونِ گوشی/امولاتور، با layoutlib) - برای گرفتنِ باگ‌های ظاهریِ
 * ساده (سایه/گوشه/همپوشانی/رنگِ متنِ نامرئی روی دارک‌مود، دقیقاً همون دسته‌باگی که این نشست چندبار
 * با گزارشِ کاربر پیدا شد) قبل از رسیدنِ به دستِ کاربر. اجرا: `./gradlew :app:testDebugUnitTest`
 * (رکوردِ snapshot اولیه با `-Ptestpk="record"` یا `./gradlew recordPaparazziDebug`). چون سندباکس به
 * `dl.google.com` دسترسی نداره، این تست فقط تو CI واقعی (build-native-android.yml) قابلِ‌اجراست، نه
 * اینجا - رجوع کن به CLAUDE.md.
 *
 * `LocalLayoutDirection` عمداً صریح Rtl ست شده - چون MainActivity.kt کلِ اپِ واقعی رو تو یه Rtl
 * provider می‌پیچه (رجوع کن به AppRoot تو MainActivity.kt)، ولی Paparazzi کامپوزیبل‌ها رو مستقل از
 * درختِ ترکیبِ واقعیِ اپ رندر می‌کنه - بدونِ این خط، تستِ اسکرین‌شات جهتِ چیدمانِ واقعیِ اپ رو نشون
 * نمی‌ده (منطقِ RTL خودِ کامپوننت‌ها دست‌نخورده می‌مونه، فقط جهتِ چیدمانِ Row/Column فرق می‌کنه).
 */
class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private fun snapshotRtl(themeMode: ThemeMode, content: @androidx.compose.runtime.Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LoanCalcTheme(themeMode = themeMode) {
                    content()
                }
            }
        }
    }

    @Test
    fun appCardLight() = snapshotRtl(ThemeMode.LIGHT) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCard(label = "وام مسکن") {
                Text("مبلغ قسط: ۱۲,۷۴۹,۰۰۰ ریال")
            }
        }
    }

    @Test
    fun appCardDark() = snapshotRtl(ThemeMode.DARK) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCard(label = "وام مسکن") {
                Text("مبلغ قسط: ۱۲,۷۴۹,۰۰۰ ریال")
            }
        }
    }

    /**
     * تکرارِ عمدیِ باگِ امروز (رجوع کن به CLAUDE.md، «باگِ درهم‌ریختنِ متنِ پرداخت‌شده») - یه Row با
     * دو ستون، ستونِ اول با یه متنِ حروفیِ خیلی بلند (دقیقاً هم‌الگو با «مبلغ هر قسط» تو
     * LoanDetailScreen، با weight(1f)) و ستونِ دومِ کوتاه («پرداخت‌شده»/«X از Y»). اگه یه‌روزِ دیگه
     * weight از رو ستونِ اول برداشته بشه، این عکس دوباره همون باگِ حرف‌به‌حرف‌شکستن رو نشون می‌ده و
     * تست fail می‌شه - عکسِ مرجع باید با کدِ درست (weight-دار) ضبط بشه.
     */
    @Test
    fun twoColumnRowWithLongWordsTextDoesNotSqueezeSibling() = snapshotRtl(ThemeMode.LIGHT) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCard(label = "بلوبانک") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("مبلغ هر قسط", color = AppMuted)
                        Text("۱۷,۶۵۲,۲۸۲ ریال", color = AppText)
                        Text(
                            "یک میلیون و هفتصد و شصت و پنج هزار و دویست و بیست و هشت تومان",
                            color = AppMuted,
                        )
                    }
                    Column {
                        Text("پرداخت‌شده", color = AppMuted)
                        Text("۵ از ۶", color = AppPrimary)
                    }
                }
            }
        }
    }
}
