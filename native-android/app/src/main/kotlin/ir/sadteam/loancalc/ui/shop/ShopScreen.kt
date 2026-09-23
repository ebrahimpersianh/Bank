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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NewReleases
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.SymbolStyle
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.data.categoryIconChoices
import ir.sadteam.loancalc.data.iconForKey
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.coin.BASE_THEME_IDS
import ir.sadteam.loancalc.data.coin.FEATURED_ITEM_IDS
import ir.sadteam.loancalc.data.coin.BuyResult
import ir.sadteam.loancalc.data.coin.CoinSpend
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.data.coin.THEME_CATALOG
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AvatarFramePreview
import ir.sadteam.loancalc.ui.components.AvatarFrameStyle
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppFontChoice
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.background.LiveBackgroundLayer
import androidx.compose.foundation.border
import ir.sadteam.loancalc.ui.theme.hardShadow
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.background.LiveBackground
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppBg
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.widget.IconWither

/*
 * فروشگاهِ سکه - فریم‌های `60a`..`60d` (تکمیلِ `45b` روی کاتالوگِ واقعی).
 *
 * ترتیبِ دسته‌ها و قیمت‌ها از `SHOP_CATALOG` می‌آیند، پس این فایل هیچ قیمت و شناسه‌ای
 * **نمی‌داند** - همان قاعده‌ی `CoinEconomy.kt` که تنها مرجعِ اعداد باشد.
 */


/** کالکشن کوچک اما واقعی: دو خریدی که با هم یک فضای یکپارچه می‌سازند. */
@Composable
private fun StarryNightCollectionCard(owned: Set<String>, earned: Boolean) {
    val required = setOf("theme:vangogh", "bg_night_swirl")
    val collected = required.count(owned::contains)
    val complete = collected == required.size
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp).clip(shape)
            .background(if (complete) Color(0xFF10295D) else AppSurface2)
            .border(1.dp, if (complete) Color(0xFFF2C14E).copy(alpha = 0.65f) else AppLine, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("✦", color = if (complete) Color(0xFFF2C14E) else AppMuted, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Column(modifier = Modifier.weight(1f)) {
            Text("کالکشنِ شبِ پرستاره", color = if (complete) Color.White else AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(if (complete) "تم ون‌گوگ + چرخش شب · نشان باز شد" else "تم ون‌گوگ و بسته‌ی پس‌زمینه را بگیر", color = if (complete) Color.White.copy(alpha = 0.75f) else AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
        }
        Pill(if (earned) "نشان گرفتی" else "${toFa(collected)} از ۲", if (complete) Color(0x33F2C14E) else AppIconFrame, if (complete) Color(0xFFF2C14E) else AppMuted)
    }
}

/** حالتِ یک ردیف. از موجودی و مالکیت و نشان‌ها مشتق می‌شود، جایی ذخیره نمی‌شود. */
/**
 * بازکردنِ صفحه‌ی اختصاصیِ محصول (تصمیمِ ۵ِ فروشگاه). از ردیف و کارت خوانده می‌شود تا
 * لازم نباشد یک پارامترِ تازه از همه‌ی محل‌های فراخوانی رد شود.
 */
private val LocalOpenProduct = staticCompositionLocalOf<((ShopItem) -> Unit)?> { null }

private enum class RowState { BUY, POOR, OWNED, ACTIVE, BADGE_LOCKED, SOON }

@Composable
fun ShopScreen(
    onBack: () -> Unit,
    /**
     * داخلِ «سکه»ی ادغام‌شده رندر می‌شود (`75a`)؟ آن‌وقت هدر و کارتِ موجودی را خودِ
     * میزبان می‌گذارد و این‌جا تکرار نمی‌شوند.
     */
    embedded: Boolean = false,
    viewModel: ShopViewModel = hiltViewModel(),
) {
    val balance by viewModel.balance.collectAsState()
    val frameColor by viewModel.frameColor.collectAsState()
    val owned by viewModel.owned.collectAsState()
    val active by viewModel.active.collectAsState()
    val earnedBadges by viewModel.earnedBadges.collectAsState()
    val result by viewModel.lastResult.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    var confirming by remember { mutableStateOf<ShopItem?>(null) }
    var detail by remember { mutableStateOf<ShopItem?>(null) }
    /** `null` یعنی تبِ «همه». */
    var tab by rememberSaveable { mutableStateOf<ShopCategory?>(null) }
    // 🚨 **فیلتر است، نه تبِ ششم** (بندِ ۳ی وصله‌ی بخشِ ۷۸): تبِ «مالِ من» یعنی یک ستونِ
    // دیگر در نوارِ تب و یک جای دیگر برای گم‌شدن؛ قرص کنارِ همان نوار می‌نشیند و
    // **صفر پیکسل** ارتفاع می‌گیرد. پیش‌فرض خاموش است چون ویترین برای دیدنِ نداشته‌هاست.
    var onlyMine by rememberSaveable { mutableStateOf(false) }
    val banner = rememberInAppBanner()
    // کلیدِ جلالیِ امروز - مبنای «تازه‌رسیده». یک‌بار خوانده می‌شود؛ روزِ تقویم وسطِ
    // یک ترکیب عوض نمی‌شود.
    val todayKey = remember { ActiveStreak.dateKey(JalaliCalendar.today()) }
    val activateItem: (ShopItem) -> Unit = { item ->
        viewModel.activate(item)
        banner.show("«${item.label}» فعال شد.", isSuccess = true)
    }

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
    // در حالتِ **جاسازی‌شده** (`75a`) هدر و کارتِ موجودی از بالا می‌آیند: هیرو بالای
    // تب‌ها مشترک است، وگرنه موجودی دو بار دیده می‌شود.
    if (!embedded) {
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
    }
    // ═══ تبِ افقی، نه سرگروهِ بیشتر (`72b`) ═══
    //
    // با پنج نوعِ قلم، سرگروه‌بندیِ تنها یعنی کاربر برای رسیدنِ به «قاب» باید از چهارده تم
    // عبور کند. تب فهرست را **کوتاه** می‌کند، سرگروه فقط نشانه‌گذاری‌اش - پس هر دو
    // می‌مانند: تب بیرون، سرگروه داخلِ همان تب.
    // کارتِ «قلمِ هفته» **بالای نوارِ تب**: اولین چیزی که دیده می‌شود باید یک پیشنهادِ
    // مشخص باشد، نه فهرستِ دسته‌ها. `null` یعنی کاربر همه را دارد و کارت **نمی‌آید** -
    // پیامِ «همه را داری» عمداً جایگزینش نمی‌شود (تبریکِ بی‌کار، ارتفاعِ گران).
    StarryNightCollectionCard(owned = owned, earned = Badge.COLLECTION_STARRY_NIGHT.code in earnedBadges)
    // کارتِ «قلمِ هفته» به‌خواستِ کاربر حذف شد: «پیشنهادهای ویژه» همان کار را می‌کند و
    // دو پیشنهاد پشتِ‌هم بالای صفحه طرحِ مرجع را شلوغ می‌کرد.
    ShopTabs(tab, onlyMine, { tab = it }) { onlyMine = !onlyMine }
    // ⚠️ هر دو **بیرونِ** `LazyColumn` حساب می‌شوند: `remember` در بدنه‌ی لیستِ تنبل
    // (بیرونِ `item {}`) مجاز نیست - بررسیِ ایستای پروژه همین را گرفت.
    val featuredTrio = remember(catalog) {
        FEATURED_ITEM_IDS.mapNotNull { id -> catalog.firstOrNull { it.id == id } }
    }
    // «تازه» از روی `addedOn` هر قلم حساب می‌شود، نه از ترتیبِ کاتالوگ.
    val fresh = remember(catalog, todayKey) {
        catalog.filter { it.isNew(todayKey) { from, to -> daysBetweenKeys(from, to) } }
    }
    CompositionLocalProvider(LocalOpenProduct provides { detail = it }) {
    LazyColumn(
        contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!embedded) item { BalanceCard(balance) }

        // قلمِ کمیاب **بالای همه‌ی تب‌ها** می‌آید، بیرونِ تب‌بندی: چیزی که مهلت دارد نباید
        // پشتِ یک تپ پنهان شود (`72b`).
        val rare = catalog.filter { it.window != null && it.id !in owned }
        items(rare.size) { index ->
            val shopItem = rare[index]
            RareItemCard(
                item = shopItem,
                state = stateOf(shopItem),
                balance = balance,
                onActivate = activateItem,
                onConfirm = { confirming = it },
            )
        }

        // ═══ پیشنهادهای ویژه و تازه‌رسیده‌ها (بخشِ ۸۳) ═══
        //
        // 🚨 **فقط این دو بخش کارتِ دوتایی‌اند** - تصمیمِ قفل‌شده‌ی کاربر. ۲۸ ردیفِ تم
        // به‌شکلِ کارتِ دوتایی یعنی صفحه‌ای که ته ندارد؛ بقیه‌ی ویترین همان ردیفِ فشرده
        // می‌مانَد.
        //
        // ⚠️ هر دو فقط در تبِ «همه» و بیرونِ حالتِ «مالِ من» دیده می‌شوند: این‌ها
        // **ویترین**اند نه فهرست، و در فهرستِ داشته‌های کاربر معنی ندارند.
        if (tab == null && !onlyMine) {
            if (featuredTrio.isNotEmpty()) {
                item { SectionHeader("پیشنهادهای ویژه", Icons.Filled.AutoAwesome) }
                items(featuredTrio.chunked(2).size) { rowIndex ->
                    ProductCardRow(
                        pair = featuredTrio.chunked(2)[rowIndex],
                        stateOf = ::stateOf,
                        onActivate = activateItem,
                        onConfirm = { confirming = it },
                        leadingOf = { previewFor(it) },
                    )
                }
            }
            // نبودِ قلمِ تازه یعنی **کلِ بخش پنهان** - تصمیمِ قفل‌شده. سرگروهِ خالی
            // بدتر از نبودنش است.
            if (fresh.isNotEmpty()) {
                item { SectionHeader("تازه رسیده‌ها", Icons.Filled.NewReleases) }
                items(fresh.chunked(2).size) { rowIndex ->
                    ProductCardRow(
                        pair = fresh.chunked(2)[rowIndex],
                        stateOf = ::stateOf,
                        onActivate = activateItem,
                        onConfirm = { confirming = it },
                        leadingOf = { previewFor(it) },
                    )
                }
            }
        }

        fun rowsOf(category: ShopCategory) = catalog
            .filter { it.kind.category == category && it.window == null }
            // آیتمِ فعال هرگز نباید با زدنِ «مالِ من» ناپدید شود، حتی اگر نسخه‌ی
            // قدیمیِ برنامه کلیدِ مالکیتش را ثبت نکرده باشد.
            .filter { !onlyMine || it.id in owned || active[category] == it.id }

        if (tab == null || tab == ShopCategory.BACKDROP) {
            val backdrops = rowsOf(ShopCategory.BACKDROP)
            // ⚠️ متنِ سرگروه **قیمتِ بسته‌ای** را می‌گوید نه قیمتِ ردیف: چهار ردیفِ
            // ۲۵۰سکه‌ای پشتِ‌هم یعنی «۱۰۰۰ سکه برای همه»، که غلط است.
            item { GroupHeader("پس‌زمینه‌ی زنده", "${toFa(CoinSpend.LIVE_BACKDROP.price)} سکه · بازکردن هر چهار طرح") }
            items(backdrops.size) { index ->
                val shopItem = backdrops[index]
                ShopRow(
                    shopItem,
                    stateOf(shopItem),
                    balance,
                    activateItem,
                    onConfirm = { confirming = it },
                    // مثلِ قلم، این هم دیدنی است نه خواندنی: «هاله‌ای که نفس می‌کشد» را
                    // با متن نمی‌شود فروخت. پیش‌نمایش **همان انیمیشنِ واقعی** است، در
                    // یک مربعِ ۳۸ - نه یک تصویرِ ثابتِ نماینده.
                    leading = { BackdropPreview(LiveBackground.byId(shopItem.id.removePrefix("bg_"))) },
                )
            }
            // قاعده‌ی «بی راهِ بازگشت نگذار» (بندِ ۵ فریمِ 60d): هر قلمِ فعال‌شدنی باید
            // خاموش‌شدنی هم باشد. این ردیف یک‌بار حذف شده بود و پس‌زمینه‌ی خریداری‌شده
            // دیگر برداشته نمی‌شد.
            if (active[ShopCategory.BACKDROP] != null) {
                item { ResetRow("برداشتنِ پس‌زمینه‌ی زنده", viewModel::resetBackdrop) }
            }
        }


        if (tab == null || tab == ShopCategory.THEME) {
            // ═══ تم - دو سرگروه (`60a`) ═══
            //
            // چهارده ردیفِ پشتِ‌هم بلندترین دسته‌ی فروشگاه است و کاربر نمی‌فهمد کدام از قبل
            // مالِ اوست. `BASE_THEME_IDS` مرزِ همین دو گروه است.
            val themes = rowsOf(ShopCategory.THEME)
            val base = themes.filter { it.id.removePrefix("theme:") in BASE_THEME_IDS }
            val colorful = themes - base.toSet()

            item { GroupHeader("تمِ پایه", "${toFa(base.size)} تمِ اولِ برنامه") }
            items(base.size) { index ->
                val shopItem = base[index]
                ThemeRow(shopItem, stateOf(shopItem), balance, activateItem) { confirming = it }
            }
            item { GroupHeader("تمِ رنگی", "${toFa(CoinSpend.THEME_PALETTE.price)} سکه هرکدام") }
            items(colorful.size) { index ->
                val shopItem = colorful[index]
                ThemeRow(shopItem, stateOf(shopItem), balance, activateItem) { confirming = it }
            }
        }

        if (tab == null || tab == ShopCategory.ICON) {
            // ═══ آیکونِ برنامه (`60b`) ═══
            val icons = rowsOf(ShopCategory.ICON)
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
                    activateItem,
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
        }

        if (tab == null || tab == ShopCategory.SYMBOL) {
            val symbols = rowsOf(ShopCategory.SYMBOL)
            item { GroupHeader("نمادِ دسته‌بندی", "${toFa(CoinSpend.CATEGORY_ICON_SET.price)} سکه") }
            items(symbols.size) { index ->
                val shopItem = symbols[index]
                ShopRow(
                    shopItem,
                    stateOf(shopItem),
                    balance,
                    activateItem,
                    onConfirm = { confirming = it },
                    // یک نماد کافی نیست - **ست** است، پس چهارتا در شبکه (`72b`).
                    leading = {
                        if (shopItem.id.startsWith("coinskin:")) CoinSkinPreview(shopItem.id)
                        else SymbolSetPreview(shopItem.id)
                    },
                )
            }
            if (active[ShopCategory.SYMBOL] != null) {
                item { ResetRow("بازگشت به نمادهای توپر", viewModel::resetSymbolSet) }
            }
        }

        if (tab == null || tab == ShopCategory.FRAME) {
            val frames = rowsOf(ShopCategory.FRAME)
            item { GroupHeader("قابِ آواتار", "${toFa(CoinSpend.AVATAR_FRAME.price)} سکه") }
            items(frames.size) { index ->
                val shopItem = frames[index]
                ShopRow(
                    shopItem,
                    stateOf(shopItem),
                    balance,
                    activateItem,
                    onConfirm = { confirming = it },
                    leading = { AvatarFramePreview(AvatarFrameStyle.fromItemId(shopItem.id)) },
                )
            }
            // 🎨 **رنگِ قاب** (خواسته‌ی کاربر: «هر حلقه را به تعدادِ رنگ‌های تم بگذار»).
            //
            // ⚠️ به‌جای ساختنِ یک ردیفِ ویترین برای هر ترکیبِ قاب×رنگ - که پنج قاب در
            // شانزده رنگ یعنی **هشتاد ردیف** و ویترین را غیرقابلِ‌استفاده می‌کرد - رنگ
            // یک نوارِ افقیِ اسکرول‌شونده‌ی زیرِ همین گروه است. خریدْ قاب است، رنگ تنظیم.
            //
            // فقط وقتی دیده می‌شود که کاربر قابی فعال دارد؛ وگرنه رنگ چیزی برای رنگ‌کردن ندارد.
            if (active[ShopCategory.FRAME] != null) {
                item { FrameColorRow(selected = frameColor, onSelect = viewModel::setFrameColor) }
                item { ResetRow("برداشتنِ قاب", viewModel::resetFrame) }
            }
        }

        if (tab == null || tab == ShopCategory.FONT) {
            val fonts = rowsOf(ShopCategory.FONT)
            item { GroupHeader("قلمِ متن", "${toFa(CoinSpend.FONT_FACE.price)} سکه") }
            items(fonts.size) { index ->
                val shopItem = fonts[index]
                ShopRow(
                    shopItem,
                    stateOf(shopItem),
                    balance,
                    activateItem,
                    onConfirm = { confirming = it },
                    // پیش‌نمایشِ قلم **با خودِ همان قلم** نوشته می‌شود - تنها قلمی که
                    // توضیحِ متنی‌اش بی‌فایده است: «کشیده و باریک» را باید دید نه خواند.
                    leading = { FontPreview(AppFontChoice.fromId(shopItem.id)) },
                )
            }
            if (active[ShopCategory.FONT] != null) {
                item { ResetRow("بازگشت به وزیرمتن", viewModel::resetFont) }
            }
        }

        if (tab == null || tab == ShopCategory.REWARD) {
            val rewards = rowsOf(ShopCategory.REWARD)
            item { GroupHeader("جایزه", "به‌زودی") }
            items(rewards.size) { index ->
                val shopItem = rewards[index]
                ShopRow(
                    shopItem,
                    stateOf(shopItem),
                    balance,
                    activateItem,
                    onConfirm = { confirming = it },
                    leading = { GenericItemPreview(shopItem) },
                )
            }
        }
    }
    }
    }
        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }

    detail?.let { item ->
        ProductDetailSheet(
            item = item,
            state = stateOf(item),
            balance = balance,
            onDismiss = { detail = null },
            onActivate = activateItem,
            onBuy = { confirming = it },
        )
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
    // کارتِ **فشرده**ی یک‌خطی (تصمیمِ فروشگاه): موجودی مهم است ولی ویترین اصل است؛
    // نسخه‌ی سه‌خطیِ قبلی نصفِ صفحه‌ی اول را پیش از رسیدن به هر محصولی می‌گرفت.
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(size = 20.dp)
            Text(
                toFa(balance),
                color = AppText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 7.dp),
            )
            Text("سکه", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.weight(1f))
            // بن‌بست نساز - کوتاه‌ترین راهِ گرفتنِ سکه همین‌جا گفته می‌شود.
            Text("هر روزِ ثبت · ۱۰ سکه", color = AppLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
    // اسمِ رنگ به‌تنهایی کافی نیست؛ توضیحِ کوتاه حسِ واقعیِ هر تم را می‌دهد و مثل
    // پیش‌نمایشِ فروشگاه، انتخاب را قبل از خرید قابل‌فهم می‌کند.
    val displayItem = item.copy(
        blurb = when (paletteId) {
            "aubergine", "plum" -> "بنفشِ عمیق و آرام"
            "crimson", "garnet" -> "قرمزِ عمیق و رسمی"
            "saffron", "copper" -> "گرم و پرانرژی"
            "cobalt", "lapis", "indigo" -> "آبیِ عمیق و خنک"
            "olive", "teal", "turquoise" -> "سبزآبیِ نرم و تازه"
            "graphite" -> "خنثی و مینیمال"
            // هفت تمِ نام‌دارِ بخشِ ۸۲ - توضیحشان حس را می‌گوید نه رنگ را.
            "nature" -> "سبزِ جنگل، آرام و زنده"
            "night" -> "سرمه‌ایِ عمیقِ شبانه"
            "sunset" -> "نارنجیِ گرمِ غروب"
            "minimal" -> "سبزآبیِ ملایم و ساده"
            "luxe" -> "طلاییِ مات و باوقار"
            "calm" -> "بنفشِ نرم و آرام"
            "ice" -> "فیروزه‌ایِ خنک و روشن"
            "gold" -> "طلاییِ ویژه"
            else -> item.blurb
        },
    )
    ShopRow(
        item = displayItem,
        state = state,
        balance = balance,
        onActivate = onActivate,
        onConfirm = onConfirm,
        leading = {
            // خودِ رنگِ واقعیِ تم، در یک کاشیِ درشت؛ نمونه‌ی سه‌خطیِ قبلی در گوشی
            // تقریباً دیده نمی‌شد و کاربر نمی‌فهمید «بادمجانی» واقعاً چه رنگی است.
            val swatch = themeById(paletteId)
            val (dark, primary, light) = swatch?.let {
                Triple(Color(it.dark), Color(it.primary), Color(it.light))
            } ?: when (paletteId) {
                "green" -> Triple(Color(0xFF08734B), Color(0xFF0EA968), Color(0xFF80D6AE))
                "blue" -> Triple(Color(0xFF174A8B), Color(0xFF2878D4), Color(0xFF9BC7F5))
                "purple" -> Triple(Color(0xFF5D3585), Color(0xFF8B55C7), Color(0xFFCBA9EC))
                "gold" -> Triple(Color(0xFF806018), Color(0xFFC99625), Color(0xFFF3D57B))
                else -> Triple(AppMuted, AppIconFrame, AppSurface2)
            }
            // 🚨 **نمای کوچکِ خودِ برنامه، نه یک کاشیِ رنگ** (خواسته‌ی کاربر با طرحِ
            // مرجع: «تم‌ها آن‌طوری بشود»).
            //
            // کاشیِ رنگ می‌گفت تم **چه رنگی** است، ولی نمی‌گفت **برنامه با آن چه شکلی**
            // می‌شود - و کسی که ۱۵۰ سکه می‌دهد دقیقاً همین را می‌خواهد بداند. این‌جا یک
            // صفحه‌ی مینیاتوریِ واقعی کشیده می‌شود: کارتِ قهرمان با گرادیانِ همان تم، دو
            // کارتِ سفید، سه میله‌ی نمودار و نوارِ پایین.
            //
            // ⚠️ هیچ فایلِ تصویری لازم ندارد و با هر تمِ تازه‌ای که به `THEME_CATALOG`
            // اضافه شود خودبه‌خود کار می‌کند - برخلافِ موکاپِ عکسی که باید دستی ساخته شود.
            ThemeMiniPreview(dark = dark, primary = primary, light = light)
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
    val open = LocalOpenProduct.current
    val tap: (() -> Unit)? = if (open != null) ({ open(item) }) else when (state) {
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
                RowState.ACTIVE -> Pill("فعال است", AppPrimaryPill, AppPrimaryInk)
                RowState.OWNED -> Pill("فعال‌سازی", AppPrimaryPill, AppPrimaryInk)
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

/**
 * نوارِ انتخابِ رنگِ قابِ آواتار.
 *
 * رنگ‌ها از `THEME_CATALOG` می‌آیند - همان شانزده رنگی که تم‌های فروشگاه دارند، پس
 * افزودنِ تمِ تازه خودبه‌خود این‌جا هم می‌آید و دو فهرستِ موازی ساخته نمی‌شود.
 *
 * اولین گزینه «هم‌رنگِ تم» است (`null`)، یعنی همان رفتاری که تا امروز بود.
 */
@Composable
private fun FrameColorRow(selected: String?, onSelect: (String?) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("رنگِ قاب", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FrameColorDot(color = AppPrimary, selected = selected == null) { onSelect(null) }
            THEME_CATALOG.forEach { tone ->
                FrameColorDot(
                    color = Color(tone.primary),
                    selected = selected == tone.id,
                ) { onSelect(tone.id) }
            }
        }
    }
}

/** یک نقطه‌ی رنگ. انتخاب‌شده حلقه‌ی دورش را می‌گیرد، نه تیک - تیک روی رنگِ تیره گم می‌شود. */
@Composable
private fun FrameColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    val ring = AppText
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, ring, CircleShape) else Modifier)
            .padding(if (selected) 4.dp else 2.dp)
            .clip(CircleShape)
            .background(color)
            .pressScaleClickable(onClick = onClick),
    )
}

/**
 * مینیاتورِ صفحه‌ی خانه با رنگ‌های یک تم - پیش‌نمایشِ ردیفِ تمِ فروشگاه.
 *
 * عمداً **شبیهِ تبِ خانه** است نه یک شکلِ انتزاعی: کارتِ قهرمانِ رنگی بالا، دو کارتِ
 * روشن، میله‌های نمودار و نوارِ پایین با قرصِ تبِ فعال. همان چیزهایی که تم واقعاً
 * عوضشان می‌کند.
 */
@Composable
private fun ThemeMiniPreview(dark: Color, primary: Color, light: Color) {
    val paper = AppSurface
    val shape = RoundedCornerShape(11.dp)
    Column(
        modifier = Modifier
            .size(width = 46.dp, height = 62.dp)
            .clip(shape)
            .background(paper)
            .border(1.dp, AppLineRow, shape)
            .padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp),
    ) {
        // کارتِ قهرمان - همان گرادیانِ ۱۶۰درجه‌ی `AppHeroCard`، در مقیاسِ کوچک.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.linearGradient(listOf(primary, dark))),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                listOf(4, 7, 5, 9).forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h.dp)
                            .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                            .background(Color.White.copy(alpha = 0.75f)),
                    )
                }
            }
        }
        // دو کارتِ روشن - همان شبکه‌ی «دسترسیِ سریع»ِ تبِ خانه.
        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(light.copy(alpha = 0.38f)),
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        // نوارِ پایین با قرصِ تبِ فعال.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppSurface2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 5.dp else 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index == 0) primary else AppMuted.copy(alpha = 0.45f)),
                )
            }
        }
    }
}

/**
 * فاصله‌ی دو کلیدِ جلالیِ `۱۴۰۵-۰۶-۳۱` به روز.
 *
 * ⚠️ کلیدِ خراب `Int.MAX_VALUE` می‌دهد نه صفر: صفر یعنی «امروز» و یک قلمِ بدتاریخ را
 * برای همیشه «تازه» نگه می‌داشت - دقیقاً همان چیزی که `addedOn = null` جلویش را می‌گیرد.
 */
private fun daysBetweenKeys(from: String, to: String): Int {
    fun parse(key: String): PersianDate? {
        val parts = key.split('-')
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return PersianDate(y, m, d)
    }
    val a = parse(from) ?: return Int.MAX_VALUE
    val b = parse(to) ?: return Int.MAX_VALUE
    return runCatching { JalaliCalendar.daysBetween(a, b) }.getOrDefault(Int.MAX_VALUE)
}

/** سرگروهِ بخش‌های ویترین («پیشنهادهای ویژه»، «تازه رسیده‌ها»). */
@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppAccent, modifier = Modifier.size(14.dp))
        Text(title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * یک ردیفِ **دوتایی** از کارتِ محصول.
 *
 * ⚠️ `LazyVerticalGrid` داخلِ `LazyColumn` ممنوع است (قاعده‌ی ثبت‌شده‌ی پروژه)، پس
 * ردیف‌بندی با `chunked(2)` انجام می‌شود و خانه‌ی خالیِ ردیفِ فرد با `Spacer(weight)`
 * پر می‌شود - وگرنه کارتِ تک، تمام‌عرض می‌شد.
 */
@Composable
private fun ProductCardRow(
    pair: List<ShopItem>,
    stateOf: (ShopItem) -> RowState,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
    leadingOf: @Composable (ShopItem) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        pair.forEach { item ->
            Box(modifier = Modifier.weight(1f)) {
                ProductCard(item, stateOf(item), onActivate, onConfirm) { leadingOf(item) }
            }
        }
        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * کارتِ محصولِ ویترین - پیش‌نمایشِ بزرگ بالا، نام و توضیح، و قیمت پایین.
 *
 * پیش‌نمایش **همان چیزی است که ردیفِ فشرده هم نشان می‌دهد**، فقط بزرگ‌تر؛ دو پیاده‌سازیِ
 * جدا یعنی روزی یکی عوض می‌شود و دیگری جا می‌مانَد.
 */
@Composable
private fun ProductCard(
    item: ShopItem,
    state: RowState,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
    preview: @Composable () -> Unit,
) {
    val open = LocalOpenProduct.current
    val tap: (() -> Unit)? = if (open != null) ({ open(item) }) else when (state) {
        RowState.OWNED -> ({ onActivate(item) })
        RowState.BUY, RowState.POOR -> ({ onConfirm(item) })
        else -> null
    }
    AppCard(
        contentPadding = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tap != null) Modifier.pressScaleClickable(scale = 0.98f, onClick = tap) else Modifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .clip(RoundedCornerShape(AppRadius.card))
                .background(AppSurface2),
            contentAlignment = Alignment.Center,
        ) { preview() }
        Text(
            item.label,
            color = AppText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            item.blurb,
            color = AppLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 1.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (state) {
                RowState.ACTIVE -> Pill("فعال است", AppPrimaryPill, AppPrimaryInk)
                RowState.OWNED -> Pill("فعال‌سازی", AppPrimaryPill, AppPrimaryInk)
                RowState.SOON -> Pill("به‌زودی", AppIconFrame, AppMuted)
                RowState.BADGE_LOCKED -> Pill("نشان لازم است", AppIconFrame, AppMuted)
                // قیمت **یکدست** است: سکه و عدد کنارِ هم در یک قرص، نه دو جای کارت.
                else -> CoinPrice(item.price)
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/** قیمت به شکلِ واحدِ «🪙 ۱۵۰» - تصمیمِ قفل‌شده‌ی کاربر. */
@Composable
private fun CoinPrice(price: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(AppGoldPillSoft)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CoinIcon(size = 11.dp)
        Text(toFa(price), color = AppGoldInk, fontSize = 10.sp, fontWeight = FontWeight.Black)
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
 * پیش‌نمایشِ هر قلم، بر اساسِ نوعش - همان چیزی که ردیفِ فشرده هم می‌گذارد.
 *
 * یک نقطه‌ی انتخاب برای هر دو نما، تا کارت و ردیف هیچ‌وقت دو چیزِ متفاوت نشان ندهند.
 */
@Composable
private fun previewFor(item: ShopItem) {
    when {
        item.id.startsWith("icon:") -> AppIconPreview(item.id)
        item.id.startsWith("symbolset:") -> SymbolSetPreview(item.id)
        item.id.startsWith("coinskin:") -> CoinSkinPreview(item.id)
        item.id.startsWith("frame:") -> AvatarFramePreview(AvatarFrameStyle.fromItemId(item.id))
        item.id.startsWith("bg_") -> BackdropPreview(LiveBackground.byId(item.id.removePrefix("bg_")))
        item.id.startsWith("theme:") -> {
            val tone = themeById(item.id.removePrefix("theme:"))
            if (tone != null) {
                ThemeMiniPreview(Color(tone.dark), Color(tone.primary), Color(tone.light))
            } else {
                GenericItemPreview(item)
            }
        }
        else -> GenericItemPreview(item)
    }
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
        "icon:shop" -> R.drawable.ic_launcher_shop_background to R.drawable.jibak_shop_0
        // نه طرحِ تصویریِ بخشِ ۸۰ - هر کدام پس‌زمینه‌ی **پُرِ** خودش را دارد (رنگ از
        // تیره‌ترین ناحیه‌ی خودِ فایلِ هنری)، چون لایه‌ی شفاف روی بعضی لانچرها
        // صفحه‌ی مشکی می‌گیرد.
        "icon:aqua" -> R.drawable.ic_launcher_aqua_background to R.drawable.jibak_ic_aqua
        "icon:calligraphy" -> R.drawable.ic_launcher_calligraphy_background to R.drawable.jibak_ic_calligraphy
        "icon:fox" -> R.drawable.ic_launcher_fox_background to R.drawable.jibak_ic_fox
        "icon:emerald" -> R.drawable.ic_launcher_emerald_background to R.drawable.jibak_ic_emerald
        "icon:leaf" -> R.drawable.ic_launcher_leaf_background to R.drawable.jibak_ic_leaf
        "icon:orbit" -> R.drawable.ic_launcher_orbit_background to R.drawable.jibak_ic_orbit
        "icon:growth" -> R.drawable.ic_launcher_growth_background to R.drawable.jibak_ic_growth
        "icon:sprout" -> R.drawable.ic_launcher_sprout_background to R.drawable.jibak_ic_sprout
        "icon:neon" -> R.drawable.ic_launcher_neon_background to R.drawable.jibak_ic_neon
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


/**
 * نوارِ تبِ افقیِ ویترین (`72b`). «همه» تبِ پیش‌فرض است تا کاربری که فقط نگاه می‌کند
 * همه‌چیز را ببیند؛ تب‌ها برای کسی‌اند که دنبالِ چیزِ مشخصی آمده.
 */
@Composable
private fun ShopTabs(
    selected: ShopCategory?,
    onlyMine: Boolean,
    onSelect: (ShopCategory?) -> Unit,
    onToggleMine: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 10.dp, end = 16.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabChip("مالِ من", onlyMine, onToggleMine)
        Spacer(modifier = Modifier.width(5.dp))
        TabChip("همه", selected == null && !onlyMine) { onSelect(null) }
        ShopCategory.entries.forEach { category ->
            Spacer(modifier = Modifier.width(5.dp))
            TabChip(category.tab, selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) AppPrimaryInk else AppMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(if (selected) AppPrimaryPill else AppIconFrame)
            .pressScaleClickable(scale = 0.97f, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    )
}

/**
 * کارتِ قلمِ کمیاب - بالای تب‌ها، با شمارشِ روزِ باقی‌مانده.
 *
 * ⚠️ مهلت روی **خودِ کارت** نوشته می‌شود نه در یک بجِ ریز: تنها فوریتِ کلِ فروشگاه همین
 * است، و سکه‌ای که فوریت نداشته باشد جمع می‌شود و خرج نمی‌شود (`72a`).
 */
@Composable
private fun RareItemCard(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
) {
    val left = item.daysLeft(LocalDate.now())
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        ) {
            Text("تا پایانِ بازه", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(8.dp))
            if (left != null) {
                Text(
                    "${toFa(left.toInt())} روز",
                    color = AppAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        ThemeRow(item, state, balance, onActivate, onConfirm)
    }
}

/**
 * پیش‌نمایشِ ستِ نماد - **چهار نماد در شبکه**، نه یکی: ست است و یک نماد جنسش را نمی‌گوید
 * (`72b`). همان قابِ ۳۸ِ بقیه‌ی قلم‌ها.
 */
/** پیش‌نمایشِ قلم: حرفِ «آ» با خودِ همان قلم، هم‌اندازه‌ی بقیه‌ی پیش‌نمایش‌ها. */
@Composable
private fun FontPreview(choice: AppFontChoice) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(AppRadius.icon))
            .background(AppSurface2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "آ",
            color = AppText,
            fontSize = 19.sp,
            fontFamily = choice.family,
        )
    }
}

@Composable
private fun SymbolSetPreview(itemId: String) {
    val style = SymbolStyle.fromItemId(itemId)
    val (background, tint) = when (style) {
        SymbolStyle.FILLED -> AppIconFrame to AppMuted
        SymbolStyle.ROUNDED -> AppPrimaryPill to AppPrimaryInk
        SymbolStyle.OUTLINED -> AppSurface2 to AppText
        SymbolStyle.SHARP -> Color(0xFFFFEEE2) to Color(0xFFB64C19)
        SymbolStyle.TWO_TONE -> Color(0xFFEAE5FF) to Color(0xFF6842B8)
        SymbolStyle.PICTORIAL -> Color(0xFFE0F4EE) to Color(0xFF087D5B)
        SymbolStyle.SOLID -> Color(0xFFFFF4D6) to Color(0xFF7A5A00)
    }
    val keys = listOf("restaurant", "home", "car", "celebration")
    Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            keys.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    pair.forEach { key ->
                        Icon(
                            iconForKey(key, style),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp).padding(1.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinSkinPreview(itemId: String) {
    val accent = if (itemId == "coinskin:ancient") Color(0xFF8A6744) else Color(0xFFC99625)
    Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        CoinIcon(size = 25.dp, modifier = Modifier.alpha(if (itemId == "coinskin:ancient") 0.76f else 1f))
    }
}


/**
 * پیش‌نمایشِ پس‌زمینه‌ی زنده - همان `LiveBackdrop`ِ واقعی در یک مربعِ ۳۸، روی زمینه‌ی
 * سطحِ برنامه تا نسبتش با صفحه‌ی واقعی دیده شود.
 *
 * ⚠️ آلفای لایه در اندازه‌ی ۳۸ تقریباً نامرئی است، پس این پیش‌نمایش عمداً **دو برابرِ**
 * اندازه را در خودش می‌کشد و برش می‌دهد؛ وگرنه ردیفِ ویترین یک مربعِ خالی می‌شد.
 */
@Composable
private fun BackdropPreview(backdrop: LiveBackground?) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurface2),
        contentAlignment = Alignment.Center,
    ) {
        if (backdrop != null) {
            LiveBackgroundLayer(
                background = backdrop,
                primary = AppPrimary,
                primaryLight = AppPrimaryInkLight,
                // پیش‌نمایش همیشه حالتِ تیره را می‌کشد، وگرنه «چرخشِ شب» در تمِ روشن
                // یک مربعِ خالی می‌شد و کاربر فکر می‌کرد ردیف خراب است.
                isDark = true,
                modifier = Modifier.size(76.dp),
            )
        }
    }
}


/**
 * صفحه‌ی اختصاصیِ محصول (تصمیمِ ۵ِ فروشگاه). خرید همچنان از دیالوگِ تاییدِ قبلی می‌گذرد.
 */
@Composable
private fun ProductDetailSheet(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onDismiss: () -> Unit,
    onActivate: (ShopItem) -> Unit,
    onBuy: (ShopItem) -> Unit,
) {
    FullScreenDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Text(
                    item.kind.category.label,
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(AppRadius.card))
                                .background(AppSurface2),
                            contentAlignment = Alignment.Center,
                        ) {
                            // همان پیش‌نمایشِ ردیف، بزرگ‌شده - نه یک تصویرِ جدا که روزی جا بمانَد.
                            Box(modifier = Modifier.scale(2.6f)) { previewFor(item) }
                        }
                        Text(
                            item.label,
                            color = AppText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        Text(
                            item.blurb,
                            color = AppLabel,
                            fontSize = 11.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
                item { DetailFacts(item) }
                if (item.id.startsWith("icon:")) item { IconUsageSample(item.id) }
                if (item.id.startsWith("symbolset:")) item { SymbolSetSample(item.id) }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 18.dp, top = 6.dp)) {
                DetailAction(item, state, balance, onActivate, onBuy)
            }
        }
    }
}

/** جدولِ مشخصات: نوع، تعداد/سبک (برای پک‌ها)، قیمت و وضعیت. */
@Composable
private fun DetailFacts(item: ShopItem) {
    val facts = buildList {
        add("نوع" to item.kind.category.label)
        when {
            item.id.startsWith("symbolset:") -> {
                add("تعدادِ نماد" to "${toFa(categoryIconChoices.size)} نماد")
                add("سبک" to item.label)
            }
            item.id.startsWith("icon:") -> add(
                "حالت‌ها" to if (IconWither.hasAgingStages(item.id)) "۴ حالت (کهنه‌شدن با سرنزدن)" else "یک طرحِ ثابت",
            )
        }
        add("قیمت" to "${toFa(item.price)} سکه")
    }
    AppCard(modifier = Modifier.fillMaxWidth()) {
        facts.forEachIndexed { index, (label, value) ->
            if (index > 0) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppLine))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(value, color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** نمونه‌ی استفاده‌ی آیکونِ برنامه: کنارِ چند آیکونِ خنثی روی صفحه‌ی گوشی. */
@Composable
private fun IconUsageSample(itemId: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("روی صفحه‌ی گوشی", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) { index ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (index == 1) {
                        AppIconPreview(itemId)
                    } else {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(AppIconFrame),
                        )
                    }
                    Text(
                        if (index == 1) "جیبک" else "",
                        color = AppText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** چند نمادِ واقعی از همان پک، در اندازه‌ی واقعیِ فهرستِ دسته‌بندی. */
@Composable
private fun SymbolSetSample(itemId: String) {
    val style = SymbolStyle.fromItemId(itemId)
    val keys = categoryIconChoices.map { it.first }.take(12)
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("نمونه‌ی نمادها", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keys.chunked(6).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(AppIconFrame),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(iconForKey(key, style), contentDescription = null, tint = AppText, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/** دکمه‌ی پایینِ صفحه - وضعیتِ خرید/فعال‌سازی را همان‌جا می‌گوید. */
@Composable
private fun DetailAction(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onBuy: (ShopItem) -> Unit,
) {
    when (state) {
        RowState.BUY -> GradientButton(onClick = { onBuy(item) }, modifier = Modifier.fillMaxWidth()) {
            Text("خرید با ${toFa(item.price)} سکه", fontWeight = FontWeight.Black)
        }
        RowState.OWNED -> GradientButton(onClick = { onActivate(item) }, modifier = Modifier.fillMaxWidth()) {
            Text("خریداری شده · فعال‌سازی", fontWeight = FontWeight.Black)
        }
        else -> Text(
            when (state) {
                RowState.ACTIVE -> "خریداری شده · الان فعال است"
                RowState.POOR -> "${toFa(item.price - balance)} سکه کم داری"
                RowState.BADGE_LOCKED -> "اول باید نشانِ لازم را بگیری"
                else -> "به‌زودی"
            },
            color = if (state == RowState.POOR) AppDangerInk else AppMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}
