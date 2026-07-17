package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface

/** پورت .card تو www/index.html - از `Surface` (نه یه `Column` دستی با background/border خام)
 * استفاده می‌کنه تا هم سایه‌ی ظریف (`shadowElevation`) هم روشن‌شدن تونالِ سطح (`tonalElevation`) رو
 * مجانی داشته باشیم. یه دوره حاشیه‌ی سبزآبیِ کم‌رنگِ پیش‌فرض داشت، ولی کاربر بعداً این رو هم برگردوند:
 * همه‌ی باکس‌های اپ (نه فقط چندتای خاص) باید بدون خط دور باشن، حس مدرن‌تری داره - فقط سایه‌ی ظریف
 * تشخیصشون می‌ده. [borderColor] برای مواردی که یه کارتِ خاص واقعاً به خط دور نیاز داره (مثل کارت
 * وام‌های پرتکرار که خط مشکی می‌خواد) override می‌شه. لیبلِ بالای کارت قبلاً یه دور طلایی‌رنگ شده بود
 * (لهجهٔ طلایی بیشتر)، ولی کاربر بعداً این تصمیم رو هم برگردوند: طلایی نباید رو فونت باشه، فقط رو
 * پس‌زمینه/حاشیه‌ی باکس‌ها (مثل حاشیه‌ی هنگام لمس تو `PressScale`، یا رینگ آواتار مشترکین) - پس لیبل
 * به همون خاکستریِ AppMuted قبلی برگشت. [backgroundColor] برای مواردی که کارت باید رنگ زمینه‌ی خاص
 * خودش رو داشته باشه (مثل کارتِ تحلیلِ درآمد که یه سبزِ ملایم می‌خواد) override می‌شه. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor ?: AppSurface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor ?: Color.Transparent),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (label != null) {
                Text(
                    text = label,
                    color = AppMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }
            content()
        }
    }
}
