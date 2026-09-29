package ir.sadteam.loancalc.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * کارتِ اصلیِ حالتِ خالی - بازطراحیِ ChatGPT (۷ مهر، دورِ دوم): تصویرِ تختِ ۱۰۴dp، عنوان،
 * یک جمله توضیح، یک دکمه‌ی اصلی. [tint] زمینه‌ی ملایمِ کارت است (برای هر صفحه یک رنگ).
 */
@Composable
fun EmptyHeroCard(
    @DrawableRes illustration: Int,
    tint: Color,
    title: String,
    description: String,
    action: String,
    onAction: () -> Unit,
) {
    AppCard(backgroundColor = tint) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(painterResource(illustration), contentDescription = null, modifier = Modifier.size(104.dp))
            Text(
                title, color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                description, color = AppMuted, fontSize = 12.5.sp, lineHeight = 22.sp, textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp).padding(top = 6.dp),
            )
            GradientButton(onClick = onAction, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("+ $action", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** فهرستِ «با … چه چیزهایی می‌بینی؟» - هر ردیف آیکونِ رنگیِ تخت + متن. */
@Composable
fun EmptyFeatureList(title: String, items: List<Pair<Int, String>>) {
    AppCard {
        Text(title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
            items.forEach { (icon, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppLineRow.copy(alpha = 0.35f)).padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(label, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
