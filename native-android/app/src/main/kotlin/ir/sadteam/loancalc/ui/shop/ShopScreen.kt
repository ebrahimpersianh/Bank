package ir.sadteam.loancalc.ui.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NewReleases
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.res.painterResource
import ir.sadteam.loancalc.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.coin.BASE_THEME_IDS
import ir.sadteam.loancalc.data.coin.FEATURED_ITEM_IDS
import ir.sadteam.loancalc.data.coin.BuyResult
import ir.sadteam.loancalc.data.coin.CoinSpend
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppBg
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import ir.sadteam.loancalc.ui.components.GradientButton

/*
 * فروشگاهِ سکه - فریم‌های `60a`..`60d` (تکمیلِ `45b` روی کاتالوگِ واقعی).
 *
 * ترتیبِ دسته‌ها و قیمت‌ها از `SHOP_CATALOG` می‌آیند، پس این فایل هیچ قیمت و شناسه‌ای
 * **نمی‌داند** - همان قاعده‌ی `CoinEconomy.kt` که تنها مرجعِ اعداد باشد.
 */
/** تکه‌های کالکشنِ «شب پرستاره» - همان دو قلمی که `ShopViewModel` برای نشان می‌شمارد. */
internal val STARRY_IDS = listOf("theme:vangogh", "bg_night_swirl")
/** حالتِ یک ردیف. از موجودی و مالکیت و نشان‌ها مشتق می‌شود، جایی ذخیره نمی‌شود. */
/**
 * بازکردنِ صفحه‌ی اختصاصیِ محصول (تصمیمِ ۵ِ فروشگاه). از ردیف و کارت خوانده می‌شود تا
 * لازم نباشد یک پارامترِ تازه از همه‌ی محل‌های فراخوانی رد شود.
 */
internal val LocalOpenProduct = staticCompositionLocalOf<((ShopItem) -> Unit)?> { null }
/** «تازه رسیده‌ها» پیش از «همه را ببین» دو ردیفِ دوتایی نشان می‌دهد. */
private const val FRESH_PREVIEW = 4
internal enum class RowState { BUY, POOR, OWNED, ACTIVE, BADGE_LOCKED, SOON }
@OptIn(ExperimentalFoundationApi::class)
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
    val dailyDeal by viewModel.dailyDeal.collectAsState()
    val coinGoalId by viewModel.coinGoal.collectAsState()
    var confirming by remember { mutableStateOf<ShopItem?>(null) }
    var showWallet by remember { mutableStateOf(false) }
    if (showWallet && !embedded) {
        androidx.activity.compose.BackHandler { showWallet = false }
        val gam: ir.sadteam.loancalc.ui.profile.GamificationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
        ir.sadteam.loancalc.ui.coin.CoinHubScreen(onBack = { showWallet = false }, todayHasEntry = gam.todayLogged.collectAsState().value)
        return
    }
    var detail by remember { mutableStateOf<ShopItem?>(null) }
    var showCollection by remember { mutableStateOf(false) }
    var freshExpanded by rememberSaveable { mutableStateOf(false) }
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
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.jibak_shop_store_3d),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                Text("فروشگاه", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(
                    "با سکه‌ها، امکاناتِ بیشتری باز کن",
                    color = AppLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier.padding(end = 12.dp).size(40.dp).clip(RoundedCornerShape(12.dp)).background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(20.dp))
            }
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
    // کارتِ «قلمِ هفته» به‌خواستِ کاربر حذف شد: «پیشنهادهای ویژه» همان کار را می‌کند و
    // دو پیشنهاد پشتِ‌هم بالای صفحه طرحِ مرجع را شلوغ می‌کرد.
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
        if (!embedded) item { BalanceCard(balance, onClick = { showWallet = true }) }

        // 🚨 بنر و نوارِ تب **داخلِ فهرست** آمدند (خواسته‌ی کاربر، ۱ مهر: «وقتی به بالا
        // می‌کشم، بالا محو شود و آیکون‌ها دیده شوند؛ الان دو-سه تا بیشتر پیدا نیست»).
        // بنر با فهرست بالا می‌رود؛ نوارِ تب **می‌چسبد** تا دسته‌ها همیشه در دسترس باشند.
        item {
            StarryNightCollectionCard(
                owned = owned,
                earned = Badge.COLLECTION_STARRY_NIGHT.code in earnedBadges,
                onView = { showCollection = true },
            )
        }
        dailyDeal?.let { deal ->
            item(key = "deal") { DailyDealCard(deal) { detail = deal.item } }
        }
        catalog.firstOrNull { it.id == coinGoalId && it.id !in owned }?.let { goal ->
            item(key = "goal") {
                CoinGoalCard(goal, balance, onOpen = { detail = goal }, onClear = { viewModel.setCoinGoal(null) })
            }
        }
        stickyHeader {
            Box(modifier = Modifier.fillMaxWidth().background(AppBg).padding(vertical = 4.dp)) {
                ShopTabs(tab, onlyMine, { tab = it; if (it == null) onlyMine = false }) { onlyMine = !onlyMine }
            }
        }

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
                        compact = true,
                        leadingOf = { previewFor(it) },
                    )
                }
            }
            // نبودِ قلمِ تازه یعنی **کلِ بخش پنهان** - تصمیمِ قفل‌شده. سرگروهِ خالی
            // بدتر از نبودنش است.
            if (fresh.isNotEmpty()) {
                item {
                    SectionHeader(
                        "تازه رسیده‌ها",
                        Icons.Filled.NewReleases,
                        seeAll = if (fresh.size > FRESH_PREVIEW) ({ freshExpanded = !freshExpanded }) else null,
                        expanded = freshExpanded,
                    )
                }
                val shownFresh = if (freshExpanded) fresh else fresh.take(FRESH_PREVIEW)
                items(shownFresh.chunked(2).size) { rowIndex ->
                    ProductCardRow(
                        pair = shownFresh.chunked(2)[rowIndex],
                        stateOf = ::stateOf,
                        onActivate = activateItem,
                        onConfirm = { confirming = it },
                        compact = true,
                        leadingOf = { previewFor(it) },
                    )
                }
            }
        }

        // خواسته‌ی کاربر (۳ مهر): همه‌ی دسته‌ها هم مثلِ «پیشنهادهای ویژه» دوتایی کنارِ هم،
        // تا فضای کمتری بگیرند (تصمیمِ قبلیِ «فقط دو بخش دوتایی» را خودش عوض کرد).
        fun grid(list: List<ShopItem>, compact: Boolean = true) {
            val rows = list.chunked(2)
            items(rows.size) { rowIndex ->
                ProductCardRow(
                    pair = rows[rowIndex],
                    stateOf = ::stateOf,
                    onActivate = activateItem,
                    onConfirm = { confirming = it },
                    compact = compact,
                    leadingOf = { previewFor(it) },
                )
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
            item { GroupHeader("پس‌زمینه‌ی زنده", "${toFa(CoinSpend.LIVE_BACKDROP.price)} سکه · بازکردنِ همه‌ی طرح‌ها") }
            grid(backdrops)
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
            grid(base.map(::themeDisplay), compact = true)
            item { GroupHeader("تمِ رنگی", "${toFa(CoinSpend.THEME_PALETTE.price)} سکه هرکدام") }
            grid(colorful.map(::themeDisplay), compact = true)
        }

        if (tab == null || tab == ShopCategory.ICON) {
            // ═══ آیکونِ برنامه (`60b`) ═══
            val icons = rowsOf(ShopCategory.ICON)
            item { GroupHeader("آیکونِ برنامه", "${toFa(CoinSpend.APP_ICON.price)} سکه") }
            grid(icons)
            // بندِ ۵ی `60d`: بی این ردیف، آیکونِ پیش‌فرض بی‌راهِ‌بازگشت است - «کیفِ پول» در
            // کاتالوگ نیست چون فروشی نیست، پس ردیفی هم ندارد که فعالش کند.
            if (active[ShopCategory.ICON] != null) {
                item { ResetRow("بازگشت به آیکونِ پیش‌فرض", viewModel::resetIcon) }
            }
        }

        if (tab == null || tab == ShopCategory.SYMBOL) {
            // شکلِ سکه در همین دسته ذخیره شده ولی نمادِ دسته نیست - گروهِ جدا (۸ مهر).
            val all = rowsOf(ShopCategory.SYMBOL)
            val symbols = all.filterNot { it.id.startsWith("coinskin:") }
            val coinSkins = all.filter { it.id.startsWith("coinskin:") }
            item { GroupHeader("نمادِ دسته‌بندی", "${toFa(CoinSpend.CATEGORY_ICON_SET.price)} سکه") }
            grid(symbols)
            if (coinSkins.isNotEmpty()) {
                item { GroupHeader("شکلِ سکه", "به‌زودی") }
                grid(coinSkins)
            }
            if (active[ShopCategory.SYMBOL] != null) {
                item { ResetRow("بازگشت به نمادهای توپر", viewModel::resetSymbolSet) }
            }
        }

        if (tab == null || tab == ShopCategory.CHART) {
            val charts = rowsOf(ShopCategory.CHART)
            item { GroupHeader("سبکِ نمودار", "۱۵۰ تا ۵۰۰ سکه") }
            grid(charts)
            if (active[ShopCategory.CHART] != null) {
                item { ResetRow("بازگشت به نمودارِ پیش‌فرض", viewModel::resetChartStyle) }
            }
        }

        if (tab == null || tab == ShopCategory.FRAME) {
            val frames = rowsOf(ShopCategory.FRAME)
            item { GroupHeader("قابِ آواتار", "${toFa(CoinSpend.AVATAR_FRAME.price)} سکه") }
            grid(frames)
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
            grid(fonts)
            if (active[ShopCategory.FONT] != null) {
                item { ResetRow("بازگشت به وزیرمتن", viewModel::resetFont) }
            }
        }

        if (tab == null || tab == ShopCategory.REWARD) {
            val rewards = rowsOf(ShopCategory.REWARD)
            item { GroupHeader("جایزه", "به‌زودی") }
            grid(rewards)
        }
    }
    }
    }
        ShopTrial.item?.let { trialItem ->
            TrialBar(
                item = trialItem,
                secondsLeft = ShopTrial.secondsLeft,
                modifier = Modifier.align(Alignment.BottomCenter),
                onBuy = {
                    ShopTrial.stop()
                    confirming = catalog.firstOrNull { it.id == trialItem.id } ?: trialItem
                },
                onBack = { ShopTrial.stop() },
            )
        }
        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (showCollection) {
        StarryCollectionSheet(
            items = STARRY_IDS.mapNotNull { id -> catalog.firstOrNull { it.id == id } },
            owned = owned,
            earned = Badge.COLLECTION_STARRY_NIGHT.code in earnedBadges,
            balance = balance,
            stateOf = ::stateOf,
            onDismiss = { showCollection = false },
            onOpen = { detail = it },
        )
    }

    // شمارشِ معکوس سراسری است (`TrialHost` در MainActivity) تا بیرون‌رفتن از فروشگاه تمِ
    // امتحانی را نیمه‌کاره گیر نیندازد. این‌جا فقط «خرید» از پرسشِ پایانی را می‌گیریم.
    val pendingBuy = ShopTrial.pendingBuyId
    LaunchedEffect(pendingBuy, catalog) {
        val id = pendingBuy ?: return@LaunchedEffect
        catalog.firstOrNull { it.id == id }?.let { confirming = it; ShopTrial.pendingBuyId = null }
    }

    detail?.let { item ->
        ProductDetailSheet(
            item = item,
            state = stateOf(item),
            balance = balance,
            onDismiss = { detail = null },
            onActivate = activateItem,
            onBuy = { confirming = it },
            onTry = {
                detail = null
                ShopTrial.start(it)
            },
            isGoal = item.id == coinGoalId,
            onToggleGoal = { viewModel.setCoinGoal(if (item.id == coinGoalId) null else item.id) },
        )
    }

    // خرید دیالوگِ تایید می‌گیرد (`46a`)؛ فعال‌کردنِ چیزی که داری نه - اولی واگرد ندارد،
    // دومی یک تپ برمی‌گردد.
    confirming?.takeIf { balance < it.price }?.let { item ->
        // سکه کم است: به‌جای تاییدِ خرید (که موجودیِ منفی نشان می‌داد) پیشنهادِ هدف‌گذاری.
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { confirming = null },
            title = { Text("سکه‌ات کافی نیست") },
            text = {
                Text(
                    "«${item.label}» ${toFa(item.price)} سکه است و تو ${toFa(balance)} سکه داری - " +
                        "${toFa(item.price - balance)} سکه‌ی دیگر لازم است.",
                )
            },
            confirmButton = {
                GradientButton(onClick = {
                    confirming = null
                    viewModel.setCoinGoal(item.id)
                }) { Text("هدفِ سکه‌ام کن") }
            },
            dismissButton = {
                GradientButton(onClick = { confirming = null }, variant = AppButtonVariant.SECONDARY) { Text("باشه") }
            },
        )
    }
    confirming?.takeIf { balance >= it.price }?.let { item ->
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
private fun BalanceCard(balance: Int, onClick: () -> Unit = {}) {
    // کارتِ **فشرده**ی یک‌خطی (تصمیمِ فروشگاه): موجودی مهم است ولی ویترین اصل است؛
    // نسخه‌ی سه‌خطیِ قبلی نصفِ صفحه‌ی اول را پیش از رسیدن به هر محصولی می‌گرفت.
    // تپ ← کیفِ سکه (زنجیره، نشان‌ها، راه‌های گرفتنِ سکه) - درِ سکه از هدرِ خانه به این‌جا آمد.
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), contentPadding = 12.dp) {
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
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabChip("مجموعه من", onlyMine, Icons.Filled.Inventory2, onClick = onToggleMine)
        Spacer(modifier = Modifier.width(5.dp))
        TabChip("همه", selected == null && !onlyMine, Icons.Filled.GridView) { onSelect(null) }
        ShopCategory.entries.forEach { category ->
            Spacer(modifier = Modifier.width(5.dp))
            TabChip(category.tab, selected == category, tabIcon(category)) { onSelect(category) }
        }
    }
}
@Composable
private fun TabChip(label: String, selected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    val ink = if (selected) AppPrimaryInk else AppMuted
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(if (selected) AppPrimaryPill else AppIconFrame)
            .pressScaleClickable(scale = 0.97f, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(label, color = ink, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
    }
}
/** آیکونِ هر تبِ دسته - طبقِ طرح، هر دکمه یک نشانه‌ی کوچک کنارِ نامش دارد. */
private fun tabIcon(category: ShopCategory): ImageVector = when (category) {
    ShopCategory.BACKDROP -> Icons.Filled.Wallpaper
    ShopCategory.THEME -> Icons.Filled.Palette
    ShopCategory.ICON -> Icons.Filled.Apps
    ShopCategory.SYMBOL -> Icons.Filled.Category
    ShopCategory.FRAME -> Icons.Filled.AccountCircle
    ShopCategory.CHART -> Icons.Filled.ShowChart
    ShopCategory.FONT -> Icons.Filled.TextFields
    ShopCategory.REWARD -> Icons.Filled.CardGiftcard
}
/**
 * شمارشِ معکوسِ «امتحان کن» + پرسشِ پایانی، **هر جای برنامه**. بیرونِ فروشگاه زندگی می‌کند تا
 * رفتن به صفحه‌ی دیگر امتحان را قطع نکند و کاربر در تمِ نخریده گیر نیفتد.
 */
@Composable
fun TrialHost(onOpenShop: () -> Unit) {
    val trialId = ShopTrial.item?.id
    LaunchedEffect(trialId) {
        if (trialId == null) return@LaunchedEffect
        while (ShopTrial.secondsLeft > 0) {
            kotlinx.coroutines.delay(1_000)
            ShopTrial.secondsLeft -= 1
        }
        ShopTrial.finish()
    }
    ShopTrial.askBuy?.let { item ->
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { ShopTrial.askBuy = null },
            title = { Text("از «${item.label}» خوشت اومد؟") },
            text = { Text("امتحان تمام شد و همه‌چیز به حالتِ قبل برگشت. اگر بخواهی همین حالا می‌توانی بخری‌اش.") },
            confirmButton = {
                GradientButton(onClick = {
                    ShopTrial.askBuy = null
                    ShopTrial.pendingBuyId = item.id
                    onOpenShop()
                }) { Text("خرید") }
            },
            dismissButton = {
                GradientButton(onClick = { ShopTrial.askBuy = null }, variant = AppButtonVariant.SECONDARY) { Text("نه، ممنون") }
            },
        )
    }
}
