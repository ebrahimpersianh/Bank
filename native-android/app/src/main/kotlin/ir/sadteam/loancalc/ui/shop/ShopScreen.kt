package ir.sadteam.loancalc.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.res.painterResource
import ir.sadteam.loancalc.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.coin.BASE_THEME_IDS
import ir.sadteam.loancalc.data.coin.BuyResult
import ir.sadteam.loancalc.data.coin.CoinSpend
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.data.coin.catalogOf
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText

/*
 * فروشگاهِ سکه - فریم‌های `60a`..`60d` (تکمیلِ `45b` روی کاتالوگِ واقعی).
 *
 * ترتیبِ دسته‌ها و قیمت‌ها از `SHOP_CATALOG` می‌آیند، پس این فایل هیچ قیمت و شناسه‌ای
 * **نمی‌داند** - همان قاعده‌ی `CoinEconomy.kt` که تنها مرجعِ اعداد باشد.
 */

/** حالتِ یک ردیف. از موجودی و مالکیت و نشان‌ها مشتق می‌شود، جایی ذخیره نمی‌شود. */
private enum class RowState { BUY, POOR, OWNED, ACTIVE, BADGE_LOCKED, SOON }

@Composable
fun ShopScreen(onBack: () -> Unit, viewModel: ShopViewModel = hiltViewModel()) {
    val balance by viewModel.balance.collectAsState()
    val owned by viewModel.owned.collectAsState()
    val active by viewModel.active.collectAsState()
    val earnedBadges by viewModel.earnedBadges.collectAsState()
    val result by viewModel.lastResult.collectAsState()
    var confirming by remember { mutableStateOf<ShopItem?>(null) }
    val banner = rememberInAppBanner()

    // نتیجه‌ی خرید در بنرِ داخلی دیده می‌شود، نه Toast - قاعده‌ی پروژه. «سکه کم» و
    // «نشان لازم» بی این، بی‌صدا رد می‌شدند و کاربر فکر می‌کرد دکمه خراب است.
    LaunchedEffect(result) {
        val outcome = result ?: return@LaunchedEffect
        banner.show(
            when (outcome) {
                is BuyResult.Ok -> "خریدی! همین حالا روشن شد."
                is BuyResult.AlreadyOwned -> "این را از قبل داری."
                is BuyResult.NotEnough -> "${toFa(outcome.missing)} سکه کم داری."
                is BuyResult.BadgeLocked -> "اول باید نشانِ «${outcome.badgeLabel}» را بگیری."
                is BuyResult.WindowClosed -> "بازه‌ی این قلم تمام شده."
            },
            isSuccess = outcome is BuyResult.Ok,
        )
        viewModel.consumeResult()
    }

    fun stateOf(item: ShopItem): RowState = when {
        item.comingSoon -> RowState.SOON
        active[item.kind.category] == item.id -> RowState.ACTIVE
        item.id in owned -> RowState.OWNED
        item.unlockBadge != null && item.unlockBadge !in earnedBadges -> RowState.BADGE_LOCKED
        balance < item.price -> RowState.POOR
        else -> RowState.BUY
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
        }
        Text(
            "فروشگاهِ سکه",
            color = AppText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { BalanceCard(balance) }

        // ═══ تم - دو سرگروه (`60a`) ═══
        //
        // ده ردیفِ پشتِ‌هم بلندترین دسته‌ی فروشگاه می‌شد و کاربر نمی‌فهمید کدام از قبل
        // مالِ اوست. `BASE_THEME_IDS` مرزِ همین دو گروه است.
        val themes = catalogOf(ShopCategory.THEME)
        val base = themes.filter { it.id.removePrefix("theme:") in BASE_THEME_IDS }
        val colorful = themes - base.toSet()

        item { GroupHeader("تمِ پایه", "${toFa(base.size)} تمِ اولِ برنامه") }
        items(base.size) { index ->
            val shopItem = base[index]
            ThemeRow(shopItem, stateOf(shopItem), balance, viewModel::activate) { confirming = it }
        }
        item { GroupHeader("تمِ رنگی", "${toFa(CoinSpend.THEME_PALETTE.price)} سکه هرکدام") }
        items(colorful.size) { index ->
            val shopItem = colorful[index]
            ThemeRow(shopItem, stateOf(shopItem), balance, viewModel::activate) { confirming = it }
        }

        // ═══ آیکونِ برنامه (`60b`) ═══
        val icons = catalogOf(ShopCategory.ICON)
        item { GroupHeader("آیکونِ برنامه", "${toFa(CoinSpend.APP_ICON.price)} سکه") }
        items(icons.size) { index ->
            // ⚠️ ویترین **همیشه پله‌ی صفر** است، حتی اگر آیکونِ فعالِ کاربر پژمرده باشد
            // (`49d`) - وگرنه کاربر فکر می‌کند جنسِ خراب می‌خرد. پس این‌جا هیچ‌جا
            // `witherStage` خوانده نمی‌شود؛ اگر روزی اضافه‌اش کردید، همین را می‌شکنید.
            val shopItem = icons[index]
            // 🚨 لامبدای انتهایی به **آخرین** پارامتر می‌چسبد و آخرینِ `ShopRow` همان
            // `leading` است، نه `onConfirm` - پس نامش صریح نوشته می‌شود (بیلدِ ۵۴۱).
            ShopRow(
                shopItem,
                stateOf(shopItem),
                balance,
                viewModel::activate,
                onConfirm = { confirming = it },
                // خواسته‌ی کاربر (۲۶ شهریور): «کنارِ هرکدام یک عکسی چیزی باشد که معلوم شود
                // چیست». برای آیکونِ برنامه، **خودِ آیکون** درست‌ترین پیش‌نمایش است.
                leading = { AppIconPreview(shopItem.id) },
            )
        }
        // بندِ ۵ی `60d`: بی این ردیف، آیکونِ پیش‌فرض بی‌راهِ‌بازگشت است - «کیفِ پول» در
        // کاتالوگ نیست چون فروشی نیست، پس ردیفی هم ندارد که فعالش کند.
        if (active[ShopCategory.ICON] != null) {
            item { ResetRow("بازگشت به آیکونِ پیش‌فرض", viewModel::resetIcon) }
        }

        // ═══ نمادها و جایزه ═══
        val rest = catalogOf(ShopCategory.SYMBOL) + catalogOf(ShopCategory.REWARD)
        item { GroupHeader("نماد و جایزه", "به‌زودی") }
        items(rest.size) { index ->
            val shopItem = rest[index]
            // 🚨 لامبدای انتهایی به **آخرین** پارامتر می‌چسبد و آخرینِ `ShopRow` همان
            // `leading` است، نه `onConfirm` - پس نامش صریح نوشته می‌شود (بیلدِ ۵۴۱).
            ShopRow(
                shopItem,
                stateOf(shopItem),
                balance,
                viewModel::activate,
                onConfirm = { confirming = it },
                leading = { GenericItemPreview(shopItem) },
            )
        }
    }
    }
        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // خرید دیالوگِ تایید می‌گیرد (`46a`)؛ فعال‌کردنِ چیزی که داری نه - اولی واگرد ندارد،
    // دومی یک تپ برمی‌گردد.
    confirming?.let { item ->
        ConfirmDialog(
            title = "«${item.label}» را بخرم؟",
            consequence = "${toFa(item.price)} سکه کم می‌شود و برگشت ندارد. " +
                "بعدش ${toFa(balance - item.price)} سکه می‌مانَد.",
            actionLabel = "بخر",
            tone = ConfirmTone.HEAVY_CHANGE,
            onConfirm = {
                confirming = null
                viewModel.buy(item)
            },
            onDismiss = { confirming = null },
        )
    }
}

@Composable
private fun BalanceCard(balance: Int) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("سکه‌های تو", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                Text(toFa(balance), color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                CoinIcon(size = 18.dp, modifier = Modifier.padding(start = 5.dp))
            }
            // بن‌بست نساز - تنها جایی که کاربر انگیزه دارد بداند سکه از کجا می‌آید.
            Text(
                "هر روزِ ثبت ۱۰ سکه · هفت روزِ پشتِ‌سرهم ۵۰ · نشانِ تازه ۲۵ تا ۱۵۰",
                color = AppLabel,
                fontSize = 9.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun GroupHeader(title: String, trailing: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
    ) {
        Text(title, color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(AppLine))
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(trailing, color = AppLabel, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

/** ردیفِ تم. تفاوتش با ردیفِ عادی فقط سه‌رنگیِ سمتِ راست است. */
@Composable
private fun ThemeRow(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
) {
    val paletteId = item.id.removePrefix("theme:")
    ShopRow(
        item = item,
        state = state,
        balance = balance,
        onActivate = onActivate,
        onConfirm = onConfirm,
        leading = {
            // سه‌رنگیِ هر ردیف **سه‌رنگِ واقعیِ پالت** است، نه نمونه‌ی تزئینی.
            // نامِ متغیر عمداً `palette` نیست: `palette.py` هر `palette.X` را دسترسی به
            // فیلدِ `AppColorPalette` می‌خواند و این‌جا مثبتِ کاذب می‌داد.
            val swatch = themeById(paletteId)
            Row(
                modifier = Modifier.size(width = 38.dp, height = 24.dp).clip(RoundedCornerShape(9.dp)),
            ) {
                if (swatch == null) {
                    Box(modifier = Modifier.weight(1f).background(AppIconFrame))
                } else {
                    Box(modifier = Modifier.weight(1f).background(Color(swatch.dark)))
                    Box(modifier = Modifier.weight(1f).background(Color(swatch.primary)))
                    Box(modifier = Modifier.weight(1f).background(Color(swatch.light)))
                }
            }
        },
    )
}

@Composable
private fun ShopRow(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    val dimmed = state == RowState.POOR || state == RowState.BADGE_LOCKED || state == RowState.SOON
    val tap: (() -> Unit)? = when (state) {
        RowState.OWNED -> ({ onActivate(item) })
        RowState.BUY -> ({ onConfirm(item) })
        else -> null
    }
    AppCard(
        modifier = Modifier
            .alpha(if (dimmed) 0.62f else 1f)
            .then(if (tap != null) Modifier.pressScaleClickable(scale = 0.99f, onClick = tap) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(11.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.label, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                Text(
                    item.blurb,
                    color = AppLabel,
                    fontSize = 9.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            when (state) {
                RowState.ACTIVE -> Pill("در حالِ استفاده", AppPrimaryPill, AppPrimaryInk)
                RowState.OWNED -> Pill("داری · بزن", AppPrimaryPill, AppPrimaryInk)
                // ردیفی که هنوز مقصد ندارد پنهان **نمی‌شود**: هدفی که دیده نشود،
                // جمع‌کردنِ سکه را بی‌معنی می‌کند. ولی خریدنی هم نیست.
                RowState.SOON -> Pill("به‌زودی", AppIconFrame, AppMuted)
                RowState.BADGE_LOCKED -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = AppMuted, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("نشان لازم است", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                }
                // ردیفِ گران خاموش می‌شود، پنهان نه - با کمبودِ نوشته‌شده (`18c`).
                RowState.POOR -> Column(horizontalAlignment = Alignment.End) {
                    Pill("${toFa(item.price)} سکه", AppIconFrame, AppMuted)
                    Text(
                        "${toFa(item.price - balance)} سکه کم داری",
                        color = AppDangerInk,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                RowState.BUY -> Pill("${toFa(item.price)} سکه", AppGoldPillSoft, AppText)
            }
        }
    }
}

@Composable
private fun ResetRow(label: String, onClick: () -> Unit) {
    AppCard(modifier = Modifier.pressScaleClickable(scale = 0.99f, onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("بازگردان", color = AppPrimaryInk, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, ink: Color) {
    Text(
        text,
        color = ink,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 7.dp),
    )
}


/**
 * پیش‌نمایشِ آیکونِ برنامه - **خودِ فایلِ آیکون**، نه یک نمادِ جایگزین.
 *
 * دو لایه‌ی `adaptive-icon` دستی روی هم می‌نشینند (پس‌زمینه و پیش‌زمینه)، چون
 * `mipmap-anydpi-v26` را `painterResource` مستقیم نمی‌کشد.
 *
 * ⚠️ همیشه **پله‌ی صفر** است، حتی اگر آیکونِ فعالِ کاربر پژمرده باشد (`49d`).
 */
@Composable
private fun AppIconPreview(itemId: String) {
    val (bg, fg) = when (itemId) {
        "icon:coin" -> R.drawable.ic_launcher_coin_background to R.drawable.ic_launcher_coin_foreground
        "icon:letter" -> R.drawable.ic_launcher_letter_background to R.drawable.ic_launcher_letter_foreground
        "icon:piggy" -> R.drawable.ic_launcher_piggy_background to R.drawable.ic_launcher_piggy_foreground
        // پیش‌فرض «کیفِ پول» است؛ پیش‌زمینه‌اش PNGِ mipmap است نه وکتورِ drawable.
        else -> R.drawable.ic_launcher_background to R.mipmap.ic_launcher_foreground
    }
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(fg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** پیش‌نمایشِ بقیه‌ی قلم‌ها - نمادی که کارِ قلم را می‌گوید، در همان قابِ ۳۸ِ آیکون. */
@Composable
private fun GenericItemPreview(item: ShopItem) {
    val icon = when {
        item.id.startsWith("coinskin:") -> Icons.Filled.Savings
        item.id.startsWith("symbolset:") -> Icons.Filled.Category
        else -> Icons.Filled.WorkspacePremium
    }
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(AppIconFrame),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(19.dp))
    }
}
