package ir.sadteam.loancalc

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.nav.NavDestination
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill

/** جهت اسلاید تعویض تب (پورت محاسبه‌ی جهت switchTab تو www/index.html): تو RTL رفتن به تبِ با
 * ایندکس بالاتر یعنی حرکت به سمت چپ، پس صفحه‌ی جدید از چپ (آفست منفی) میاد تو؛ برگشتن برعکس. */
internal fun slideDirection(fromRoute: String?, toRoute: String?): Int {
    val from = BottomTab.entries.indexOfFirst { it.route == fromRoute }
    val to = BottomTab.entries.indexOfFirst { it.route == toRoute }
    return if (to >= from) -1 else 1
}
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun RowScope.BottomNavItem(
    dest: NavDestination,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onPositioned: (Rect) -> Unit = {},
) {
    // **بازطراحیِ سبکِ «جیبک»** - مقادیر مو‌به‌مو از کارتِ `15a`ی فایلِ طراحی (نه از حدس):
    //   تبِ فعال    → قرصِ #E9F7EF پشتِ آیکون (۴۲×۲۸، گوشه‌ی ۱۱) · آیکونِ ۱۸ سبز · برچسبِ ۹٫۵/۹۰۰ سبز
    //   تبِ غیرفعال → بدونِ قرص · آیکونِ ۱۸ خاکستری · برچسبِ ۹٫۵/۷۰۰ خاکستری
    //   فاصله‌ی آیکون تا برچسب ۴ · عرضِ هر تب ۵۲ · پدینگِ نوار ۹×۶
    //
    // ⚠️ قرصِ پشتِ آیکون یه دورِ اشتباهاً حذف شده بود (فرضِ غلط: «طرح نشانگر نداره»). خودِ طرح
    // داره - فقط به‌جای نشانگرِ **لغزنده**ی دورِ قبل، یه قرصِ ثابتِ پشتِ آیکونِ همون تبه.
    // انتقالِ نرمِ ۲۲۰ms فقط روی رنگ‌ها و مقیاسِ خیلی جزئی - هیچ اندازه‌ای عوض نمی‌شود، پس
    // نوار هنگامِ جابه‌جایی نمی‌لرزد.
    val ink by animateColorAsState(if (selected) AppPrimaryInk else AppLabel, tween(220), label = "navInk")
    val pill by animateColorAsState(if (selected) AppPrimaryPill else Color.Transparent, tween(220), label = "navPill")
    val iconScale by animateFloatAsState(if (selected) 1.06f else 1f, tween(220), label = "navIconScale")
    val dotAlpha by animateFloatAsState(if (selected) 1f else 0f, tween(220), label = "navDot")
    val buzz = rememberBuzz()
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(pill)
            // **بخشِ ۴۱**: فشارِ طولانی رو هر خانه‌ی نوار، ویرایشگرِ چیدمان رو باز می‌کنه.
            .combinedClickable(
                onClick = { buzz(); onClick() },
                onLongClick = { buzz(); onLongClick() },
            )
            .padding(top = 7.dp, bottom = 5.dp)
            // مختصاتِ خودِ تب برای AppTourOverlay.
            .onGloballyPositioned { coordinates -> onPositioned(coordinates.boundsInRoot()) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // **بخشِ ۴۱**: عوض‌شدنِ مقصدِ خانه = «۱۸۰ms محو + scale .9→1»، نه جابه‌جاییِ افقی (`41c`).
        AnimatedContent(
            targetState = dest,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.9f))
                    .togetherWith(fadeOut(tween(180)))
            },
            label = "navIconSwap",
        ) { current ->
            Icon(
                if (selected) current.selectedIcon else current.icon,
                contentDescription = current.label,
                tint = ink,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
            )
        }
        Text(
            dest.label,
            color = ink,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 4.dp),
        )
        // نقطه‌ی ریزِ زیرِ تبِ فعال (طرحِ مرجع). همیشه جا دارد و فقط شفافیتش عوض می‌شود.
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .graphicsLayer { alpha = dotAlpha }
                .clip(CircleShape)
                .background(AppPrimary),
        )
        // ⚠️ راهنمای «نگه‌دار برای چیدمان» از این‌جا **برداشته شد**. زیرِ تبِ فعال که
        // می‌نشست، یعنی «همین یکی جابه‌جا می‌شود»، در حالی که نگه‌داشتن روی **هر** دکمه
        // ویرایشگر را باز می‌کند - و ۷sp هم خواندنی نبود. حالا یک خطِ مشترک بالای
        // کلِ نوار است.
    }
}
