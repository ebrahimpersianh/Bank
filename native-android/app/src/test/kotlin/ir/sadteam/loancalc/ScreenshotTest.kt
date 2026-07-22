package ir.sadteam.loancalc

import androidx.compose.foundation.layout.Column
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
}
