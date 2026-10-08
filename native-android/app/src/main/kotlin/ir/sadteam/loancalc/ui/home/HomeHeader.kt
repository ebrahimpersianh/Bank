package ir.sadteam.loancalc.ui.home

import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.components.ActiveChip
import ir.sadteam.loancalc.ui.components.FramedAvatar
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
/**
 * تاریخ + سلام + دو نشانه.
 *
 * ⚠️ **آدمکِ پروفایل تو این فریم نیست** و برداشته شد - کارتِ `32c` گفته بود «نوارِ بالای خانه
 * ۳۲px»، ولی خودِ فریمِ `15a` نشونش نمی‌ده و **تصویر بر متن مقدمه**. آدمک سرِ جاش تو
 * «حساب کاربری» می‌مونه.
 */
/** زیرِ این عرض هدر تنگ حساب می‌شود - گوشیِ ۳۶۰ منهای حاشیه‌ی ۱۶ی دو طرف. */
private val NARROW_HEADER_WIDTH = 330.dp
@Composable
internal fun HomeHeader(
    today: ir.sadteam.loancalc.core.PersianDate,
    userName: String?,
    activeDays: Int,
    coins: Int,
    onOpenSettings: () -> Unit,
    inboxCount: Int,
    inboxUnreadNews: Int,
    onOpenInbox: () -> Unit,
    onOpenCoins: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    /** امروز تراکنشی ثبت شده یا نه - شرطِ برگشتنِ قرصِ «فعال» به هدر (فریمِ `55a`). */
    todayHasEntry: Boolean,
) {
    val avatarViewModel: AvatarViewModel = hiltViewModel()
    val avatar by avatarViewModel.avatar.collectAsState()
    val avatarFrame by avatarViewModel.frame.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    val themeVm: ir.sadteam.loancalc.ui.theme.ThemeViewModel = hiltViewModel()
    val themeMode by themeVm.themeMode.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = themeMode == ir.sadteam.loancalc.ui.theme.ThemeMode.DARK ||
        (themeMode == ir.sadteam.loancalc.ui.theme.ThemeMode.SYSTEM && systemDark)
    // آستانه‌ی حالتِ باریک که طراح نگذاشته بود چون عددش دستِ ماست: زیرِ این عرض، عددِ
    // سکه برداشته می‌شود و فقط خودِ سکه می‌مانَد (ترتیبِ `55b`: عددِ سکه ← تاریخ ← نام).
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val compactChips = maxWidth < NARROW_HEADER_WIDTH
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // ⚠️ **آدمک اینجاست، و کلیک‌پذیر نیست** - فریمِ `55a`.
        //
        // تا امروز آدمک و چرخ‌دنده **هر دو** `onOpenSettings` را صدا می‌زدند: دو در به یک
        // اتاق. علتش این بود که آدمک را *به‌جای* درِ تنظیمات گذاشته بودیم نه در کنارش، و
        // گزارشِ «دکمه‌ی تنظیمات اصلاً نیست» هم دقیقاً از همین آمد - کسی آدمکِ گوشه را
        // تنظیمات نمی‌شناسد.
        //
        // پس آدمک می‌مانَد (شخصی‌سازی است و پالتش به تمِ خریدنی وصل می‌شود) ولی **مقصد
        // ندارد**: کنارِ «سلام» می‌نشیند، جایی که همان جمله را تصویر می‌کند. چرخ‌دنده تنها
        // درِ تنظیمات است.
        //
        // ⚠️ **تنها استثنای قاعده‌ی ۴۴ در هدر، و عمدی**: جعبه‌ی ۴۴ نمی‌گیرد چون کنش نیست.
        // هدفِ لمسی برای چیزی که هیچ کاری نمی‌کند، تپ‌های اطرافش را می‌خورد. اندازه ۳۰ شد
        // نه ۳۲، تا کنارِ متن بنشیند و ارتفاعِ ردیف را بالا نبرد.
        // قابِ خریداری‌شده دورِ همین آواتار می‌نشیند - جایی که خرید نتیجه می‌دهد (`72a`).
        // ⚠️ قطرِ بیرونی همان ۳۰ می‌مانَد؛ خودِ آدمک کوچک‌تر می‌شود، پس ارتفاعِ ردیف
        // عوض نمی‌شود.
        // «خوش آمدی» کمی به راست (خواسته‌ی کاربر، ۳ مهر): جعبه‌ی آدمک ۴۴ → ۳۴.
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenProfile,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // کمی بزرگ‌تر (خواسته‌ی کاربر، ۶ مهر).
            FramedAvatar(avatar = avatar, size = 37.dp, frame = avatarFrame)
            // درِ تنظیمات (۱۵ مهر): آدمک حالا تنظیمات را باز می‌کند؛ چرخ‌دنده‌ی ریز نشانِ همین است.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(AppSurface)
                    .border(1.dp, AppLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = AppMuted, modifier = Modifier.size(11.dp))
            }
            // پیامِ پشتیبانیِ خوانده‌نشده (فقط ادمین) - قبلاً روی چرخ‌دنده‌ی صفحه‌ی وام بود.
            val adminUnreadHome by ir.sadteam.loancalc.ui.admin.AdminSignals.unreadSupport.collectAsState()
            if (adminUnreadHome > 0) ir.sadteam.loancalc.ui.admin.UnreadDot(adminUnreadHome, Modifier.align(Alignment.TopEnd))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
            // تاریخ **دومین چیزی است که در تنگنا می‌رود** (بعدِ عددِ سکه، قبلِ نام).
            // `Row`ِ بیرونی `SpaceBetween` است و ستون `weight(1f)` دارد، پس خودِ Compose
            // نام را کوتاه می‌کند؛ `maxLines`/`Ellipsis` اجباری است وگرنه نامِ بلند قرص‌ها
            // را از صفحه بیرون می‌راند.
            Text(
                // سال هم آمد (خواسته‌ی کاربر با طرحِ مرجع). در ۱۱sp چهار رقمِ بیشتر
                // ارتفاع را عوض نمی‌کند و ستون خودش کوتاهش می‌کند اگر تنگ شد.
                "${(today.d).toFa()} ${persianMonthName(today.m)} ${today.y.toFa()}",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // خواسته‌ی کاربر (۳۱ شهریور): «خوش آمدی جمع‌تر بشود و برگ کنارش بنشیند».
            // ۱۹ به ۱۶٫۵ آمد - هنوز بزرگ‌ترین متنِ هدر است ولی دیگر ارتفاعِ ردیف را
            // نمی‌کشد، و برگ جای حروفِ کم‌شده را پر می‌کند.
            // ⚠️ برگ **تصویرِ همان جمله است، نه مسکات**: هیچ صورت/شخصیتی ندارد و در
            // حالتِ خالی یا جای دیگری تکرار نمی‌شود (تصمیمِ صریحِ کاربر: مسکات هیچ‌جا).
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                Text(
                    if (userName.isNullOrBlank()) "خوش آمدی" else "سلامْ $userName",
                    color = AppText,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text("🌱", fontSize = 14.sp, modifier = Modifier.padding(start = 5.dp))
            }
        }
        // خواسته‌ی کاربر (۳ مهر): سکه و زنگ کمی به چپ و به هم نزدیک‌تر.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            // ⚠️ **قرصِ «فعال» از هدر رفت** - فریمِ `55a`.
            //
            // شمارنده‌ی روزهای پیاپی است: کنش نیست، مقصد ندارد، و هر روز همان عدد
            // به‌علاوه‌ی یک را تکرار می‌کند. جایش صفحه‌ی سکه است (`45a`) کنارِ بقیه‌ی
            // بازی‌سازی، و درش همین قرصِ سکه‌ی کناری است.
            //
            // **تنها حالتی که برمی‌گردد**: رشته در خطرِ پاره‌شدن باشد - یعنی رشته‌ی هفت‌روزه
            // یا بیشتر روی میز است و امروز هنوز چیزی ثبت نشده. آن‌وقت خبر است، نه زینت.
            // و **جای** قرصِ سکه می‌نشیند نه کنارش، وگرنه همان ردیفِ شلوغ برمی‌گردد.
            //
            // ⚠️ `todayHasEntry` را باید فراخوان بدهد. در `HomeScreen` از همان داده‌ای
            // می‌آید که `notifyDailyExpenseReminder` استفاده می‌کند: «امروز تراکنشی ثبت
            // شده یا نه». امضایش را حدس نزدم.
            // جستجوی کلی (۸ مهر، خواسته‌ی کاربر) - اولین دکمه‌ی ردیف.
            // ترتیبِ کاربر (۱۰ مهر، مثلِ هدرِ وام): جستجو، زنگ، فروشگاه - هر سه با قابِ خطی. تنظیمات و تیره/روشن در منوی آدمک.
            val streakAtRisk = activeDays >= 7 && !todayHasEntry
            if (streakAtRisk) ActiveChip(days = activeDays, onClick = onOpenCoins)
            // سربرگِ یکدست (۱۵ مهر): جستجو، پیام‌ها، فروشگاه و چشمِ مبلغ (چپ‌ترین) - هم‌شکلِ بقیه‌ی صفحه‌ها.
            ir.sadteam.loancalc.ui.components.HeaderIconButton(
                icon = Icons.Filled.Search, description = "جستجو", onClick = onOpenSearch,
            )
            InboxBell(
                count = inboxCount,
                hasUnreadNews = inboxUnreadNews > 0,
                onClick = onOpenInbox,
            )
            ir.sadteam.loancalc.ui.components.HeaderIconButton(
                icon = Icons.Filled.Storefront, description = "فروشگاه", onClick = onOpenShop,
            )
            // چشمِ مبلغ از صفحه‌ی خانه برداشته شد (خواسته‌ی کاربر، ۱۶ مهر) - در بقیه‌ی صفحه‌ها هست.
        }
    }
    }
}
/**
 * دکمه‌ی ۳۲×۳۲ی هدر - فریمِ `37b`. برای حالتِ خصوصی و چرخ‌دنده‌ی تنظیمات یه شکلِ واحد.
 *
 * خاموش: زمینه‌ی `AppIconFrame`، حاشیه‌ی ۱٫۵ `AppLine`، جوهرِ `AppMuted`.
 * روشن: زمینه‌ی `AppWarningPill`، حاشیه‌ی ۱٫۵ `AppAssetBorder`، جوهرِ `AppWarningInk`.
 */
@Composable
fun PrivacyEyeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    Box(
        modifier = Modifier.size(AppSpacing.minTouchTarget).pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (active) AppWarningPill else AppIconFrame)
                .border(
                    1.5.dp,
                    if (active) AppAssetBorder else AppLine,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (active) AppWarningInk else AppMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
/**
 * زنگِ مرکزِ پیام‌ها - فریمِ `40a`.
 *
 * ⚠️ **عدد و نقطه دو چیزِ متفاوتن** (قاعده‌ی صریحِ طرح): عددِ روی زنگ فقط شمارِ «اقدام‌دارهای
 * باز»ه (تراکنشِ منتظرِ تایید، سررسیدِ وام)؛ خبرِ خوانده‌نشده هرگز عدد نمی‌گیره، فقط نقطه‌ی سبز.
 * دلیلش اینه که عدد یعنی «کاری با توئه»، نه «چیزی برای خوندن هست».
 */
@Composable
private fun InboxBell(count: Int, hasUnreadNews: Boolean, onClick: () -> Unit) {
    ir.sadteam.loancalc.ui.components.HeaderIconButton(
        icon = Icons.Filled.NotificationsNone,
        description = "پیام‌ها",
        onClick = onClick,
        badge = {
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppDanger)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text((count).toFa(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            } else if (hasUnreadNews) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(AppPrimary),
                )
            }
        },
    )
}
