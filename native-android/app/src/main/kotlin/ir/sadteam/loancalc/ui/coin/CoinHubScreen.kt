package ir.sadteam.loancalc.ui.coin

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
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.profile.CoinWalletScreen
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.shop.ShopScreen
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
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
    Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "سکه",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        CoinHero(
            coins = coins,
            activeDays = activeDays,
            // کلیدِ روز همان قالبی است که خودِ دفتر می‌نویسد (جلالی)، نه ساعتِ محلی -
            // وگرنه در نیمه‌شب دو تعریفِ متفاوت از «امروز» داریم.
            // کلیدِ روز **از خودِ همان تابعی** می‌آید که دفتر با آن می‌نویسد
            // (`ActiveStreak.dateKey`) - قالبِ دستی یعنی دو تعریف از «امروز».
            earnedToday = earnedToday(events, ActiveStreak.dateKey(JalaliCalendar.today())),
        )

        SegmentedToggle(
            options = listOf("فروشگاه", "کیف"),
            selectedIndex = if (onWallet) 1 else 0,
            onSelect = { onWallet = it == 1 },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (onWallet) {
                CoinWalletScreen(onBack = onBack, todayHasEntry = todayHasEntry, embedded = true)
            } else {
                ShopScreen(onBack = onBack, embedded = true)
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

/** هیرویِ مشترک: موجودی + سقفِ امروز + زنجیره. */
@Composable
private fun CoinHero(coins: Int, activeDays: Int, earnedToday: Int) {
    AppCard(modifier = Modifier.padding(horizontal = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoinIcon(size = 44.dp)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        toFa(coins),
                        color = AppText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "سکه",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
                    )
                }
                // سقفِ روزانه **پیدا** می‌شود، وگرنه کاربر نمی‌فهمد چرا ثبتِ چهارمش سکه
                // نداد و باگ گزارش می‌کند (همان بندِ `remainingDailyCap`).
                Text(
                    "امروز ${toFa(earnedToday)} از ${toFa(CoinReason.DAILY_COIN_CAP)}" +
                        if (activeDays > 0) " · ${toFa(activeDays)} روزِ پیاپی" else "",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
