package ir.sadteam.loancalc.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppDisabledFill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/**
 * **واژگانِ مشترکِ تنظیمات** - بخشِ «ب»ی فایلِ طراحیِ فریمِ `27d`.
 *
 * قاعده‌ی طراح: هر ده زیرصفحه‌ی تنظیمات فقط از همین چهار تا ساخته می‌شن -
 * [SettingsGroupLabel] · [SettingsGroup] · [SettingsRowItem] · [AppSwitch].
 * هیچ زیرصفحه‌ای ردیفِ دست‌سازِ خودش رو نمی‌سازه.
 */

/** جفتِ رنگِ قابِ آیکون. **معنی رو حمل می‌کنه، نه تزئین رو** - قاعده‌ی صریحِ طراح. */
enum class SettingsTone {
    /** ثبتِ خودکار، پشتیبان. */
    GREEN,

    /** اعلان. */
    BLUE,

    /** ظاهر و تم. */
    PURPLE,

    /** یادآوری. */
    ORANGE,

    /** امنیت. */
    RED,

    /** بی‌دسته. */
    NEUTRAL,
    ;

    val fill: Color
        @Composable get() = when (this) {
            GREEN -> AppPrimaryPill
            BLUE -> AppInfoPill
            PURPLE -> AppPurplePill
            ORANGE -> AppWarningPill
            RED -> AppDangerPill
            NEUTRAL -> AppSurface2
        }

    val ink: Color
        @Composable get() = when (this) {
            GREEN -> AppPrimary
            BLUE -> AppInfo
            PURPLE -> AppPurple
            ORANGE -> AppWarningInk
            RED -> AppDanger
            NEUTRAL -> AppMuted
        }
}

/** رنگِ زیرنویسِ وضعیت. اگه داده‌ای نیست زیرنویس **حذف** می‌شه - «نامشخص» نوشته نمی‌شه. */
enum class StatusTone { HEALTHY, NEUTRAL, BROKEN }

/** برچسبِ گروه - خطِ رنگیِ کوچکِ کنارش و زیرنویسِ اختیاری (بسته‌ی تنظیماتِ ChatGPT). */
@Composable
fun SettingsGroupLabel(text: String, subtitle: String? = null, accent: Color = AppPrimary) {
    Row(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = AppSpacing.betweenCards + 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(if (subtitle != null) 26.dp else 14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accent),
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
            if (subtitle != null) {
                Text(subtitle, color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * کارتِ گروه - ردیف‌ها با جداکننده‌ی **تمام‌عرضِ** ۱dp از هم جدا می‌شن (تو نمی‌ره)، و
 * ردیفِ آخر جداکننده نداره. پدینگِ کارت صفره چون خودِ ردیف پدینگ داره.
 */
@Composable
fun SettingsGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = AppPrimary.copy(alpha = 0.10f), spotColor = AppPrimary.copy(alpha = 0.10f))
            .clip(shape)
            .background(AppSurface)
            .border(AppStroke.card, AppLine, shape),
        content = content,
    )
}

/** جداکننده‌ی بینِ دو ردیفِ یک گروه. بعد از **آخرین** ردیف صداش نزن. */
@Composable
fun SettingsDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppLineRow))
}

/**
 * ردیفِ استانداردِ تنظیمات.
 *
 * انتهای ردیف **فقط یکی** از این سه: شِورونِ ورود به زیرصفحه، کلید، یا مقدارِ فقط‌خواندنی.
 * هیچ ردیفی دو تا از این‌ها نمی‌گیره - قاعده‌ی صریحِ طراح.
 *
 * @param status وضعِ فعلیِ همین ردیف («۳ بانک فعال»، «ساعتِ ۲۱:۰۰») تا کاربر مجبور نباشه بازش کنه.
 * @param checked اگه non-null باشه ردیف کلید می‌گیره؛ کلِ ردیف ناحیه‌ی کلیکه نه فقط خودِ کلید.
 * @param value مقدارِ فقط‌خواندنی مثلِ شماره‌ی نسخه.
 */
@Composable
fun SettingsRowItem(
    title: String,
    icon: ImageVector,
    tone: SettingsTone = SettingsTone.NEUTRAL,
    status: String? = null,
    statusTone: StatusTone = StatusTone.NEUTRAL,
    value: String? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowClick: (() -> Unit)? = when {
        checked != null && onCheckedChange != null -> {
            { onCheckedChange(!checked) }
        }
        else -> onClick
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (rowClick != null) Modifier.pressScaleClickable(scale = 0.99f, onClick = rowClick) else Modifier)
            .defaultMinSize(minHeight = 68.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tone.fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tone.ink, modifier = Modifier.size(21.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            if (status != null) {
                Text(
                    status,
                    color = when (statusTone) {
                        StatusTone.HEALTHY -> AppPrimaryInk
                        StatusTone.NEUTRAL -> AppMuted
                        StatusTone.BROKEN -> AppDangerInk
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        when {
            checked != null && onCheckedChange != null -> AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
            value != null -> Text(value, color = AppLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            else -> Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(AppSurface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    // تو RTL فلشِ «برو تو» رو به چپه - `KeyboardArrowLeft` خودش آینه نمی‌شه.
                    Icons.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * کلیدِ ۳۸×۲۲ با دستگیره‌ی ۱۸ - جایگزینِ `Switch`ِ متریال (که ارتفاعِ خودش و ریپلِ گردش با
 * این سبک جور نبود).
 *
 * حالتِ **خاموش** تو هیچ فریمی نبود؛ `AppDisabledFill` تصمیمِ تاییدشده‌ست (روشن‌ترین گزینه‌ای که
 * رو `AppSurface` هنوز دیده می‌شه).
 */
@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val track by animateColorAsState(
        targetValue = if (checked) AppPrimary else AppDisabledFill,
        animationSpec = tween(160),
        label = "switchTrack",
    )
    // تو RTL دستگیره‌ی حالتِ روشن سمتِ **شروع** (راست) می‌شینه؛ `offset` جهت‌آگاهه.
    val knob by animateDpAsState(
        targetValue = if (checked) 0.dp else 16.dp,
        animationSpec = tween(160),
        label = "switchKnob",
    )
    Box(
        modifier = Modifier
            .width(38.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(track)
            .pressScaleClickable(scale = 0.94f) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .offset(x = knob)
                .size(18.dp)
                .background(Color.White, CircleShape),
        )
    }
}
