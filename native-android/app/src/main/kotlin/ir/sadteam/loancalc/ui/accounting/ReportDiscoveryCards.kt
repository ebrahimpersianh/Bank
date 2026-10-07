package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.layout.heightIn
import androidx.compose.animation.AnimatedVisibility
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppAssetInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppUrgentBorder
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/** کارتِ کشف - گوشه ۲۰ · پدینگ ۱۴×۱۶ · حاشیه ۲ · قابِ آیکونِ ۳۴ با گوشه‌ی ۱۱ · فاصله ۱۱. */
@Composable
internal fun DiscoveryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bg: Color,
    border: Color,
    pill: Color,
    ink: Color,
    subInk: Color,
    iconInk: Color,
    // فلشِ کارت از اول تو فریم بود ولی هیچ‌کاری نمی‌کرد؛ کارتِ اشتراک‌یاب اولین کارتیه که
    // واقعاً یه صفحه باز می‌کنه، پس onClick اختیاری اضافه شد نه اجباری.
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    /** بسته‌ی ChatGPT (۷ مهر): عددِ درشت + «ببین چرا» که توضیح را درجا باز می‌کند. */
    bigValue: String? = null,
    bigInk: Color = ink,
    whyText: String? = null,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    var whyOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(2.dp, border, shape),
    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(AppRadius.icon)).background(pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconInk, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ink, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = subInk, fontSize = 9.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
        // فلش فقط وقتی می‌آید که جایی برود. سه کارت فلش داشتند و دو تایشان با تپ کاری
        // نمی‌کردند.
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = ChevronInk,
                modifier = Modifier.size(13.dp),
            )
        }
        if (onDismiss != null) {
            // هدفِ لمسیِ ۴۴ با `.size()` **قبل از** `pressScaleClickable` ساخته می‌شود و
            // آیکون داخلش ۱۳ می‌مانَد - پدینگ بعدِ کلیک‌پذیری هدف را کوچک می‌کرد (قاعده‌ی ۵).
            Box(
                modifier = Modifier.size(44.dp).pressScaleClickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "نادیده بگیر",
                    tint = ChevronInk,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
    if (bigValue != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            Text(bigValue, color = bigInk, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            if (whyText != null) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AppSurface)
                        .border(1.dp, border, RoundedCornerShape(13.dp))
                        .pressScaleClickable { whyOpen = !whyOpen }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (whyOpen) "بستن" else "ببین چرا", color = bigInk, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        AnimatedVisibility(visible = whyOpen && whyText != null) {
            Text(
                whyText.orEmpty(),
                color = subInk,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
    }
}
/**
 * حالتِ «این ماه چیزی پیدا نشد» - فریمِ `52a`.
 *
 * بی این کارت، ماهی که هیچ شرطی برقرار نبود بخشِ کشف را **کاملاً غیب** می‌کرد و کاربر
 * فرق «بررسی شد، چیزی نبود» با «کار نمی‌کند» را نمی‌فهمید.
 */
@Composable
internal fun NoDiscoveryCard(checkedCount: Int, monthsOfHistory: Int) {
    // ۱۴ مهر: وقتی چیزی پیدا نشده یک خطِ کوچک کافی است، نه یک کارتِ کامل.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(16.dp))
        Text(
            if (monthsOfHistory < 4) {
                "خرجِ غیرعادی نداشتی · ${(checkedCount).toFa()} تراکنش بررسی شد"
            } else {
                "خرجِ غیرعادی نداشتی · ${(checkedCount).toFa()} تراکنش با سه ماهِ قبل سنجیده شد"
            },
            color = AppMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
internal val DiscoverWarnBg: Color
    @Composable get() = AppWarningPill
internal val DiscoverWarnBorder: Color
    @Composable get() = AppDueNextBorder
internal val DiscoverWarnPill: Color
    @Composable get() = AppDueNextBorder
internal val DiscoverWarnInk: Color
    @Composable get() = AppWarningInk
internal val DiscoverWarnSubInk: Color
    @Composable get() = AppAssetInk
internal val DiscoverWarnIconInk: Color
    @Composable get() = AppWarningInk
internal val DiscoverDangerBg: Color
    @Composable get() = AppDangerPill
internal val DiscoverDangerBorder: Color
    @Composable get() = AppUrgentBorder
internal val DiscoverDangerPill: Color
    @Composable get() = AppUrgentShadow
private val ChevronInk: Color
    @Composable get() = AppMarkOff
