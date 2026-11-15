package ir.sadteam.loancalc.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * جای بنرِ تبلیغاتی. هرجای اپ می‌شه صداش زد؛ خودش تصمیم می‌گیره چیزی نشون بده یا نه.
 *
 * **الان عمداً همیشه هیچی نشون نمی‌ده**، چون هنوز شناسه‌ای تنظیم نشده. وقتی کاربر شناسه‌های
 * کافه‌بازار ادز/مایکت ادز رو داد، فقط کافیه پیاده‌سازیِ فلیورِ متناظر (`AdManager.bannerZoneId`)
 * پر بشه و ویوی واقعیِ SDK اینجا رندر بشه - هیچ‌جای دیگه‌ای از اپ لازم نیست عوض بشه.
 *
 * @param subscribed اگه true، هیچ‌وقت تبلیغ نشون داده نمی‌شه (شرطِ صریحِ کاربر).
 */
@Composable
fun AdBanner(subscribed: Boolean, modifier: Modifier = Modifier) {
    // مشترک = بدونِ تبلیغ. این چک عمداً اولین خطه تا حتی وارد منطقِ بارگذاری هم نشه.
    if (subscribed) return
    val manager = LocalAdManager.current ?: return
    if (!manager.isReady()) return

    // ⏳ اینجا ویوی واقعیِ بنرِ SDK رندر می‌شه (AndroidView). تا وقتی شناسه نرسیده، isReady()
    // همیشه false برمی‌گردونه و کد هیچ‌وقت به اینجا نمی‌رسه - پس هیچ جای خالی‌ای تو UI دیده نمی‌شه.
}
