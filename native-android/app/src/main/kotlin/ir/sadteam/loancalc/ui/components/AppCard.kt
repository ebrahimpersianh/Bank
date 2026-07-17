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
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface

/** پورت .card تو www/index.html - از `Surface` (نه یه `Column` دستی با background/border خام)
 * استفاده می‌کنه تا هم سایه‌ی ظریف (`shadowElevation`) هم روشن‌شدن تونالِ سطح (`tonalElevation`) رو
 * مجانی داشته باشیم. حالا گوشه‌گردتر (مینیمال‌تر) و یه حاشیه‌ی سبزآبیِ کم‌رنگ دورش داره (به‌درخواست
 * کاربر «دور همه‌ی باکس‌ها یه خط سبزآبیِ کم‌رنگ مینیمال») - با [borderColor] می‌شه برای موارد خاص
 * (مثل کارت وام‌های پرتکرار که خط مشکی می‌خواد) override کرد. لیبلِ بالای کارت هم عمداً طلایی‌رنگه
 * (نه خاکستریِ AppMuted قبلی) - خواسته‌ی کاربر برای لهجهٔ طلاییِ بیشتر ولی همچنان ظریف تو کل اپ؛ چون
 * این کامپوننت تقریباً همه‌جای اپ استفاده می‌شه، همین یه تغییر کافیه که سراسری و یکدست اعمال بشه. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppSurface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor ?: AppPrimary.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (label != null) {
                Text(
                    text = label,
                    color = AppAccent.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }
            content()
        }
    }
}
