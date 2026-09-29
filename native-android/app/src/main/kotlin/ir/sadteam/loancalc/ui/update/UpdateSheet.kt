package ir.sadteam.loancalc.ui.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakLogo
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * برگه‌ی پایینِ «نسخه‌ی جدید» - هم‌شکلِ برگه‌ی آپدیتِ بازار که کاربر از دیجی‌کالا نشان داد
 * (جای بنرِ باریکِ قبلیِ بالای صفحه). برای هر دو فلیور یکی است؛ فقط نامِ استور و لینک فرق دارد.
 * منبعِ «آپدیت آمده» همان [AppUpdateViewModel] و سرورِ خودمان است.
 */
@Composable
fun BoxScope.UpdateSheet(visible: Boolean, changes: List<String>, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    val storeName = if (BuildConfig.FLAVOR == "myket") "مایکت" else "کافه‌بازار"
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)), exit = fadeOut(tween(150)), modifier = Modifier.matchParentSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) { it },
        exit = slideOutVertically(tween(180)) { it },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(AppSurface)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("به‌روزرسانی از $storeName", color = AppMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(AppChipBg),
                    contentAlignment = Alignment.Center,
                ) { JibakLogo(width = 44.dp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("جیبک | حسابداری شخصی", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("نسخه‌ی جدید آماده است", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppChipBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text("تغییراتِ نسخه‌ی جدید", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                (changes.ifEmpty { listOf("بهبودِ کارایی، رفعِ اشکال و قابلیت‌های تازه") }).forEach { line ->
                    Text("• $line", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
            GradientButton(
                onClick = onUpdate,
                variant = AppButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
            ) {
                Text("به‌روزرسانی", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
