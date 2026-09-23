package ir.sadteam.loancalc.ui.coin

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.coin.CoinReason
import ir.sadteam.loancalc.data.db.CoinEventEntity
import ir.sadteam.loancalc.ui.background.LiveBackgroundLayer
import ir.sadteam.loancalc.ui.background.LiveBackgroundState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.profile.CoinWalletScreen
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.shop.ShopScreen
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.theme.LocalAppColors
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **سکه** - ادغامِ کیف و فروشگاه (`75a`/`75b`).
 *
 * سه چیزی که این صفحه حل می‌کند:
 *
 * ۱. **هیرو بالای تب‌ها، مشترک.** موجودی واحدِ پولِ فروشگاه است؛ اگر داخلِ تبِ کیف
 *    بماند، کاربرِ فروشگاه قیمت می‌بیند بی این‌که بداند چقدر دارد. سقفِ روزانه و زنجیره
 *    هم همان‌جا، چون جوابِ «چطور بیشتر بگیرم» است.
 * ۲. **تب، نه اسکرول.** اگر فروشگاه زیرِ کیف اسکرول شود، پشتِ ده‌ها ردیفِ تاریخچه
 *    می‌افتد - یعنی همان نادیده‌ماندنی که این کار می‌خواست رفعش کند، با لباسِ تازه.
 * ۳. **فروشگاه تبِ پیش‌فرض است، نه کیف** (خواسته‌ی صریحِ کاربر: «اولویت این است که
 *    فروشگاه دیده شود»). کیف یک تپ دورتر می‌شود و چیزی از دست نمی‌رود، چون عددِ موجودی
 *    از قبل در هیروست.
 *
 * ⚠️ **چهار در → یک در**: سکه‌ی هدرِ خانه تنها ورودی است. ردیفِ «کیفِ سکه»ی ابزارها و
 * ردیفِ «فروشگاهِ سکه»ی تنظیمات حذف شدند.
 */
@Composable
fun CoinHubScreen(
    onBack: () -> Unit,
    todayHasEntry: Boolean,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val coins by viewModel.coins.collectAsState()
    val events by viewModel.events.collectAsState()
    val activeDays by viewModel.activeDays.collectAsState()
    /** `false` = فروشگاه (پیش‌فرض). */
    var onWallet by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onBack)
    // 🚨 **کدر است، نه شفاف** (گزارشِ کاربر ۳۱ شهریور: «رو سکه که می‌زنم صفحه اون‌طوری
    // می‌شه»). این صفحه از تبِ خانه به‌صورتِ **رویه** باز می‌شود، پس اگر زمینه نداشته
    // باشد خانه از زیرش پیداست. زمینه‌ی خودش را می‌گیرد و پس‌زمینه‌ی زنده‌ی خریداری‌شده
    // را هم خودش می‌کشد تا با کدرشدن از دست نرود (همان الگوی `BackdropPreview`).
    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LiveBackgroundLayer(
            background = LiveBackgroundState.active,
            primary = AppPrimary,
            primaryLight = AppPrimaryInkLight,
            isDark = LocalAppColors.current.isDark,
            modifier = Modifier.fillMaxSize(),
        )
    // 🚨 **سربرگِ جمع‌شونده** (خواسته‌ی کاربر، ۱ مهر: «وقتی به بالا می‌کشم، بالا محو شود و
    // همه‌ی آیکون‌ها دیده شوند»). اسکرولِ فهرستِ پایین اول سربرگ را بالا می‌بَرد و محو
    // می‌کند، بعد خودِ فهرست می‌رود؛ برعکسش هم با کشیدن به پایین برمی‌گردد.
    var headerPx by remember { mutableFloatStateOf(0f) }
    var headerOffset by remember { mutableFloatStateOf(0f) }
    val collapse = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f) return Offset.Zero
                val next = (headerOffset + available.y).coerceIn(-headerPx, 0f)
                val used = next - headerOffset
                headerOffset = next
                return Offset(0f, used)
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f) return Offset.Zero
                val next = (headerOffset + available.y).coerceIn(-headerPx, 0f)
                val used = next - headerOffset
                headerOffset = next
                return Offset(0f, used)
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize().nestedScroll(collapse)) {
        Column(
            modifier = Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    headerPx = placeable.height.toFloat()
                    val shown = (placeable.height + headerOffset).roundToInt().coerceAtLeast(0)
                    layout(placeable.width, shown) { placeable.place(0, headerOffset.roundToInt()) }
                }
                .graphicsLayer { alpha = if (headerPx > 0f) 1f + headerOffset / headerPx else 1f },
        ) {
            // ═══ سربرگ طبقِ طرحِ ChatGPT: عنوان و توضیح راست، کارتِ موجودیِ جمع‌وجور چپ ═══
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(if (onWallet) "کیفِ سکه" else "فروشگاه", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        "با سکه‌ها، امکاناتِ بیشتری باز کن",
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                CoinHero(
                    coins = coins,
                    activeDays = activeDays,
                    // کلیدِ روز **از خودِ همان تابعی** می‌آید که دفتر با آن می‌نویسد
                    // (`ActiveStreak.dateKey`) - قالبِ دستی یعنی دو تعریف از «امروز».
                    earnedToday = earnedToday(events, ActiveStreak.dateKey(JalaliCalendar.today())),
                )
            }

            SegmentedToggle(
                options = listOf("فروشگاه", "کیف"),
                selectedIndex = if (onWallet) 1 else 0,
                onSelect = { onWallet = it == 1 },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (onWallet) {
                CoinWalletScreen(onBack = onBack, todayHasEntry = todayHasEntry, embedded = true)
            } else {
                ShopScreen(onBack = onBack, embedded = true)
            }
        }
    }
    }
}

/**
 * سکه‌ی امروز از دفتر شمرده می‌شود، نه از یک شمارنده - همان قاعده‌ی «موجودی همیشه
 * مشتق است». فقط ردیف‌های **مثبتِ** امروز، چون سقف مالِ کسب است نه خرج.
 */
private fun earnedToday(events: List<CoinEventEntity>, todayKey: String): Int =
    events.filter { it.amount > 0 && it.dateKey == todayKey }.sumOf { it.amount }

/**
 * کارتِ موجودیِ **جمع‌وجور** (طرحِ ChatGPT): سکه، عدد، و قرصِ سبزِ «امروز +X سکه».
 * سقفِ روزانه و زنجیره داخلِ همان قرص می‌مانند تا کاربر بفهمد چرا ثبتِ بعدی سکه نداد.
 */
@Composable
private fun CoinHero(coins: Int, activeDays: Int, earnedToday: Int) {
    AppCard(modifier = Modifier.width(170.dp), contentPadding = 10.dp, horizontalPadding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(toFa(coins), color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text("سکه", color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                }
                Text(
                    "امروز +${toFa(earnedToday)} از ${toFa(CoinReason.DAILY_COIN_CAP)}" +
                        if (activeDays > 0) " · ${toFa(activeDays)} روز" else "",
                    color = Color(0xFF0B8C57),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF0EA968).copy(alpha = 0.14f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            CoinIcon(size = 34.dp, modifier = Modifier.padding(start = 6.dp))
        }
    }
}
