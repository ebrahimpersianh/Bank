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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.res.painterResource
import ir.sadteam.loancalc.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import androidx.compose.foundation.border
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppBg
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.settings.FullScreenDialog

/**
 * بنرِ «کالکشن شب پرستاره» - طبقِ طرحِ فروشگاه: کارتِ بزرگِ سرمه‌ای، برچسبِ «ویژه»،
 * دکمه‌ی «مشاهده» و تصویرِ جعبه‌ی هدیه (کارِ ChatGPT، بی متن - متن‌ها همین‌جا نوشته می‌شوند
 * تا فارسی و قابلِ‌تغییر بمانند). سه نقطه‌ی اسلایدر عمداً نیست: فقط یک بنر داریم.
 */
@Composable
internal fun StarryNightCollectionCard(owned: Set<String>, earned: Boolean, onView: () -> Unit) {
    val required = STARRY_IDS.toSet()
    val collected = required.count(owned::contains)
    val gold = Color(0xFFF2C14E)
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        accentGradient = Brush.linearGradient(listOf(Color(0xFF1B3A7A), Color(0xFF0B1A3A))),
        contentPadding = 14.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Pill(if (earned) "نشان گرفتی" else "ویژه", gold, Color(0xFF3A2A00))
                Text(
                    "کالکشن شب پرستاره",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    if (collected == 0) "تم‌های خاص، حال و هوای جدید!" else "${toFa(collected)} از ۲ تکه را داری",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(Color.White)
                        .pressScaleClickable(scale = 0.97f, onClick = onView)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("مشاهده", color = Color(0xFF0B1A3A), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = Color(0xFF0B1A3A),
                        modifier = Modifier.padding(start = 4.dp).size(16.dp),
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.banner_starry_gift),
                contentDescription = null,
                modifier = Modifier.size(118.dp),
            )
        }
    }
}
/** کارتِ «تخفیفِ امروز» - قیمتِ اصلیِ خط‌خورده، قیمتِ تازه و زمانِ باقی‌مانده تا نیمه‌شب. */
@Composable
internal fun DailyDealCard(deal: DailyDeal, onOpen: () -> Unit) {
    var now by remember { mutableStateOf(java.time.LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            now = java.time.LocalTime.now()
        }
    }
    val minutesLeft = 24 * 60 - (now.hour * 60 + now.minute)
    AppCard(modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.98f, onClick = onOpen), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(AppSurface2),
                contentAlignment = Alignment.Center,
            ) { previewFor(deal.item) }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("تخفیفِ امروز", color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Pill("${toFa(DEAL_PERCENT)}٪", AppGoldPillSoft, AppGoldInk)
                }
                Text(
                    deal.item.label,
                    color = AppLabel,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    "${toFa(minutesLeft / 60)} ساعت و ${toFa(minutesLeft % 60)} دقیقه مانده",
                    color = AppMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    toFa(deal.originalPrice),
                    color = AppMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                )
                CoinPrice(deal.item.price)
            }
        }
    }
}
/** کارتِ «هدفِ سکه» - پیشرفتِ موجودی تا قیمتِ قلمِ نشان‌شده. سکه رزرو نمی‌شود. */
@Composable
internal fun CoinGoalCard(item: ShopItem, balance: Int, onOpen: () -> Unit, onClear: () -> Unit) {
    val price = item.price.coerceAtLeast(1)
    val fraction = (balance.toFloat() / price).coerceIn(0f, 1f)
    val left = item.price - balance
    AppCard(modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.98f, onClick = onOpen), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(AppSurface2),
                contentAlignment = Alignment.Center,
            ) { previewFor(item) }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    "هدفِ سکه · ${item.label}",
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppIconFrame),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppPrimary),
                    )
                }
                Text(
                    if (left > 0) "${toFa(left)} سکه‌ی دیگر مانده" else "سکه‌ات کافی است - می‌توانی بخری!",
                    color = if (left > 0) AppMuted else AppPrimaryInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = "برداشتنِ هدف", tint = AppMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
/** نوارِ شناورِ «امتحان کن»: شمارشِ معکوس + «بخر» + «برگرد». */
@Composable
internal fun TrialBar(item: ShopItem, secondsLeft: Int, modifier: Modifier, onBuy: () -> Unit, onBack: () -> Unit) {
    AppCard(modifier = modifier.padding(horizontal = 14.dp, vertical = 14.dp), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "در حالِ امتحانِ «${item.label}»",
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${toFa(secondsLeft)} ثانیه · بعدش خودش برمی‌گردد",
                    color = AppMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            GradientButton(onClick = onBuy, modifier = Modifier.padding(start = 8.dp)) {
                Text("بخر", fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            GradientButton(onClick = onBack, variant = AppButtonVariant.SECONDARY, modifier = Modifier.padding(start = 6.dp)) {
                Text("برگرد", fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
/**
 * صفحه‌ی کالکشن (بسته‌ی طراحیِ ChatGPT، ۳ مهر): تصویرِ جعبه، پیشرفت، کارتِ نشانِ ویژه و
 * فهرستِ تکه‌ها. تپ روی هر تکه صفحه‌ی همان محصول را باز می‌کند؛ خرید همان مسیرِ قبلی است.
 */
@Composable
internal fun StarryCollectionSheet(
    items: List<ShopItem>,
    owned: Set<String>,
    earned: Boolean,
    balance: Int,
    stateOf: (ShopItem) -> RowState,
    onDismiss: () -> Unit,
    onOpen: (ShopItem) -> Unit,
) {
    val collected = items.count { it.id in owned }
    val total = items.size.coerceAtLeast(1)
    val gold = Color(0xFFF2C14E)
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
                    "مجموعه‌ی شب پرستاره",
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
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGradient = Brush.linearGradient(listOf(Color(0xFF1B3A7A), Color(0xFF0B1A3A))),
                        contentPadding = 16.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.banner_starry_gift),
                                contentDescription = null,
                                modifier = Modifier.size(132.dp),
                            )
                            Text(
                                "کالکشن شب پرستاره",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                "${toFa(collected)} از ${toFa(items.size)} تکه را داری",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.15f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(collected.toFloat() / total)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(gold),
                                )
                            }
                        }
                    }
                }
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(gold.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = gold, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (earned) "نشانِ ویژه را گرفتی" else "نشانِ ویژه",
                                    color = AppText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    if (earned) "این مجموعه کامل است." else "با کامل‌کردنِ این مجموعه، نشانِ اختصاصی می‌گیری.",
                                    color = AppLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }
                items(items.size) { index ->
                    val item = items[index]
                    CompositionLocalProvider(LocalOpenProduct provides onOpen) {
                        ShopRow(
                            item = item,
                            state = stateOf(item),
                            balance = balance,
                            onActivate = onOpen,
                            onConfirm = onOpen,
                            leading = { previewFor(item) },
                        )
                    }
                }
            }
        }
    }
}
/** سرگروهِ بخش‌های ویترین («پیشنهادهای ویژه»، «تازه رسیده‌ها»). */
@Composable
internal fun SectionHeader(title: String, icon: ImageVector, seeAll: (() -> Unit)? = null, expanded: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AppAccent, modifier = Modifier.size(14.dp))
        Text(title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.weight(1f))
        // «همه را ببین» فقط وقتی می‌آید که واقعاً چیزی پنهان مانده - دکمه‌ی بی‌اثر نمی‌سازیم.
        if (seeAll != null) {
            Text(
                if (expanded) "کمتر" else "همه را ببین",
                color = AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppRadius.button))
                    .background(AppPrimaryPill)
                    .pressScaleClickable(scale = 0.97f, onClick = seeAll)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
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
internal fun ProductCardRow(
    pair: List<ShopItem>,
    stateOf: (ShopItem) -> RowState,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
    compact: Boolean = false,
    leadingOf: @Composable (ShopItem) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        pair.forEach { item ->
            Box(modifier = Modifier.weight(1f)) {
                if (compact) {
                    CompactProductCard(item, stateOf(item), onActivate, onConfirm) { leadingOf(item) }
                } else {
                    ProductCard(item, stateOf(item), onActivate, onConfirm) { leadingOf(item) }
                }
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
        borderColor = if (state == RowState.ACTIVE) AppPrimary else null,
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
                RowState.ACTIVE -> ActivePill()
                RowState.OWNED -> UsePill()
                RowState.SOON -> Pill("به‌زودی", AppIconFrame, AppMuted)
                RowState.BADGE_LOCKED -> Pill("نشان لازم است", AppIconFrame, AppMuted)
                // قیمت **یکدست** است: سکه و عدد کنارِ هم در یک قرص، نه دو جای کارت.
                else -> CoinPrice(item.price)
            }
            Spacer(modifier = Modifier.weight(1f))
            // دکمه‌ی «خرید» طبقِ طرح. تپ روی خودِ کارت صفحه‌ی محصول را باز می‌کند؛ این دکمه
            // یک‌راست به دیالوگِ تاییدِ خرید می‌رود.
            if (state == RowState.BUY) {
                Text(
                    "خرید",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(AppPrimary)
                        .pressScaleClickable(scale = 0.95f) { onConfirm(item) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
    }
}
/**
 * کارتِ دوتاییِ **کوتاه**: پیش‌نمایش کنارِ متن، نه بالای آن، تا همه‌ی دسته‌های فروشگاه
 * با یک زبانِ بصری فشرده و قابل‌مرور نمایش داده شوند.
 */
@Composable
private fun CompactProductCard(
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
        borderColor = if (state == RowState.ACTIVE) AppPrimary else null,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (state == RowState.BADGE_LOCKED || state == RowState.SOON) 0.62f else 1f)
            .then(if (tap != null) Modifier.pressScaleClickable(scale = 0.98f, onClick = tap) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            preview()
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label,
                    color = AppText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.blurb,
                    color = AppLabel,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                    // همیشه جای دو خط: توضیحِ یک‌خطی کارت را از جفتش کوتاه‌تر می‌کرد.
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp),
                )
                Box(modifier = Modifier.padding(top = 7.dp)) {
                    when (state) {
                        RowState.ACTIVE -> ActivePill()
                        RowState.OWNED -> UsePill()
                        RowState.SOON -> Pill("به‌زودی", AppIconFrame, AppMuted)
                        RowState.BADGE_LOCKED -> Pill("نشان لازم است", AppIconFrame, AppMuted)
                        else -> CoinPrice(item.price)
                    }
                }
            }
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
            .height(PILL_H)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CoinIcon(size = 11.dp)
        Text(toFa(price), color = AppGoldInk, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
/** ارتفاعِ ثابتِ همه‌ی برچسب‌های کارت (قیمت/استفاده/فعال) تا با فعال‌شدن اندازه‌ی کارت نپرد. */
private val PILL_H = 28.dp
/** قلمِ فعال: برچسبِ سبزِ پُر با تیک - نه شبیهِ دکمه (خواسته‌ی کاربر ۱۰ مهر: «فعال خوب معلوم نیست»). */
@Composable
internal fun ActivePill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(AppPrimary)
            .height(PILL_H)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        Text("فعال", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
    }
}
/** قلمِ خریده‌شده ولی غیرفعال: دکمه‌ی خطیِ «استفاده». */
@Composable
internal fun UsePill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .border(1.5.dp, AppPrimary, RoundedCornerShape(AppRadius.button))
            .height(PILL_H)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) { Text("استفاده", color = AppPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Black) }
}
@Composable
internal fun Pill(text: String, bg: Color, ink: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(bg)
            .height(PILL_H)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = ink, fontSize = 9.5.sp, fontWeight = FontWeight.Black) }
}
/**
 * کارتِ قلمِ کمیاب - بالای تب‌ها، با شمارشِ روزِ باقی‌مانده.
 *
 * ⚠️ مهلت روی **خودِ کارت** نوشته می‌شود نه در یک بجِ ریز: تنها فوریتِ کلِ فروشگاه همین
 * است، و سکه‌ای که فوریت نداشته باشد جمع می‌شود و خرج نمی‌شود (`72a`).
 */
@Composable
internal fun RareItemCard(
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
