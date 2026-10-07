package ir.sadteam.loancalc.ui.settings

import androidx.compose.material.icons.filled.Build
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppInfo
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **«ابزارها» - دو گروهِ نام‌دار** (بندِ ۵ فریمِ `75c`، و خواسته‌ی کاربر: «خیلی ساده است»).
 *
 * قبلاً چهار ردیفِ بی‌عنوان در دو کارتِ بی‌اسم بود، یعنی گروه‌بندی‌اش هیچ حرفی نمی‌زد.
 * حالا هر گروه **کاری** را نام می‌برد که کاربر آمده انجام دهد، نه جنسِ فنیِ ابزار را:
 * کسی که دنبالِ تقویم است «پولم را کِی باید بدهم» در ذهنش است، نه «ابزارِ تقویمی».
 */
@Composable
internal fun ToolsSettings(onOpenTool: (String) -> Unit) {
    SettingsHero(Icons.Filled.Build, "ابزارهای مالی", "برنامه‌ریزی، پس‌انداز و مرورِ محاسبه‌ها")
    SettingsGroupLabel("برنامه‌ریزی", accent = AppInfo)
    SettingsGroup {
        SettingsRowItem("تقویمِ مالی", Icons.Filled.DateRange, SettingsTone.BLUE, status = "سررسیدِ اقساط و چک‌ها روی تقویم") { onOpenTool("calendar") }
        SettingsDivider()
        SettingsRowItem("هدف‌های پس‌انداز", Icons.Filled.Savings, SettingsTone.GREEN, status = "پول کنار بذار و پیشرفتش رو ببین") { onOpenTool("goals") }
    }
    SettingsGroupLabel("بررسی و محاسبه", accent = AppPurple)
    SettingsGroup {
        SettingsRowItem("تاریخچه‌ی محاسبات", Icons.Filled.History, SettingsTone.PURPLE, status = "محاسبه‌های قبلیِ وام، سقفِ وام و سودِ سپرده") { onOpenTool("history") }
    }
    // ⚠️ ردیفِ «آمار و گزارشات» از این‌جا **حذف** شد (گزارشِ ۶.۵ی کاربر: «پرتی هست»).
    // محتوایش دربارهٔ وام است، پس به تبِ گزارش رفت (دورِ ۱۲). عمداً این‌جا یک ردیفِ
    // لینک‌دهنده نماند: ردیفی که فقط کاربر را جای دیگری می‌فرستد یک پرش است.
    //
    // ⚠️ **ردیفِ «شارژِ آزمایشیِ سکه» حذف شد** (خواسته‌ی صریحِ کاربر، دورِ ۱۳). ابزارِ
    // تستِ فروشگاه بود و کارش تمام شد؛ ماندنش در نسخه‌ی عمومی یعنی هر کاربری می‌توانست
    // موجودی‌اش را یک‌میلیون کند و کلِ اقتصادِ سکه بی‌معنی می‌شد.
    //
    // ⏳ سه ابزارِ دیگرِ فریمِ `75c` (اشتراک‌یاب · دنگ · استعلامِ صیادی) هنوز این‌جا
    // نیامده‌اند: هر سه از جای دیگری پارامتر می‌گیرند و بردنشان به این‌جا یک لایه‌ی
    // داده‌ی تازه می‌خواهد، نه یک ردیف.
}
/** سرگروهِ «ابزارها» - متنِ ریزِ خاکستری بالای هر کارت، نه عنوانِ داخلِ کارت. */
@Composable
private fun ToolGroupTitle(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 14.dp, start = 4.dp),
    )
}
@Composable
private fun ToolRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(title, color = AppText, fontSize = 14.sp)
            Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}
