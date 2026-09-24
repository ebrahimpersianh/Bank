package ir.sadteam.loancalc.ui.profile

import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.graphics.graphicsLayer
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.data.coin.CoinReason
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.CoinEventEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * **کیفِ سکه** - کارتِ `20d` فایلِ طراحی.
 *
 * قاعده‌های صریحِ طرح:
 * - «سکه‌ی بزرگِ بالا موجودی را مثلِ یک سکه‌ی واقعی نشان می‌دهد، نه یک عددِ کنارِ آیکون».
 * - «زیرش معادلِ ریالی نوشته می‌شود تا سکه عددِ بی‌معنا نباشد» - نرخ: **هر ۱۰ سکه = ۱٬۰۰۰ ریال**.
 * - «خرج با ردیفِ قرمز و علامتِ منفی از کسب جدا می‌شود».
 */
@Composable
fun CoinWalletScreen(
    onBack: () -> Unit,
    /** داخلِ «سکه»ی ادغام‌شده (`75b`)؟ هدر و کارتِ بزرگِ موجودی از میزبان می‌آیند. */
    embedded: Boolean = false,
    /**
     * امروز تراکنشی ثبت شده یا نه - همان ورودیِ `HomeScreen` (بخشِ ۵۵).
     *
     * ردیفِ «فعال» بی این نمی‌داند رشته **زنده** است یا **امشب می‌میرد**، و همان دو حالت
     * تنها تفاوتِ معنادارِ آن ردیف‌اند.
     */
    todayHasEntry: Boolean,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val coins by viewModel.coins.collectAsState()
    val events by viewModel.events.collectAsState()
    val activeDays by viewModel.activeDays.collectAsState()
    val repairable by viewModel.repairable.collectAsState()
    var confirmRepair by remember { mutableStateOf(false) }
    /** فیلترِ تاریخچه: `null` همه، `true` فقط دریافت‌ها، `false` فقط خرج‌ها. */
    var earnedOnly by remember { mutableStateOf<Boolean?>(null) }

    // ⚠️ `repairable` تا وقتی `refreshRepairable()` صدا زده نشود **همیشه null** است، پس
    // کارتِ ترمیم هیچ‌وقت دیده نمی‌شد. کامنتِ خودِ ViewModel می‌گفت «کارتِ خانه فقط وقتی
    // این پر باشه دیده می‌شه» - ولی چنین کارتی در خانه نبود و هیچ‌کس صدایش نمی‌زد.
    LaunchedEffect(Unit) { viewModel.refreshRepairable() }

    // این صفحه هم از تنظیمات باز می‌شود هم به‌صورتِ روکش از قرصِ سکه‌ی تبِ خانه، پس
    // خودش هم پس‌زمینه‌ی مات لازم دارد هم بازگشتِ سیستمی - وگرنه در حالتِ روکش
    // صفحه‌ی زیرش پیدا می‌شود و back کلِ برنامه را می‌بندد.
    if (!embedded) BackHandler(onBack = onBack)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!embedded) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                    Text("کیفِ سکه", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
            item {
                AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CoinIcon(size = 84.dp)
                    Text(
                        toFa(coins),
                        color = AppText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text("سکه", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        // ⚠️ `fmt()` جداکننده‌ی **لاتین** می‌دهد و عددش **ریال** بود:
                        // ۱٬۲۴۰ سکه «124,000 ریال» چاپ می‌کرد. همان باگی که در هیرویِ
                        // بودجه و گزارش رفع شد.
                        "معادلِ ${rialToToman(coinsToRial(coins).toLong()).toString().faDigits()} تومان تخفیف",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    }
                }
            }
        }
        // کارتِ ترمیم **بالای** ردیفِ «فعال» می‌نشیند: خبرِ فوری‌تری است و مهلت دارد.
        repairable?.let { repair ->
            item {
                StreakRepairCard(
                    days = repair.lostDays,
                    hoursLeft = repair.hoursLeft,
                    price = GamificationRepository.Reward.STREAK_REPAIR,
                    balance = coins,
                    onRepair = { confirmRepair = true },
                )
            }
        }
        item {
            // ردیفِ «فعال» - فریمِ `56a`. از هدرِ خانه به این‌جا آمد (بخشِ ۵۵): آن‌جا
            // کنش نبود و مقصد نداشت، این‌جا کنارِ بقیه‌ی بازی‌سازی معنی دارد.
            StreakRow(
                days = activeDays,
                todayHasEntry = todayHasEntry,
                // رشته‌ی پاره هنوز مالِ کاربر است تا وقتی ترمیم ممکن است، پس عددِ
                // `repairable` جای صفر می‌نشیند.
                brokenDays = repairable?.lostDays,
            )
        }
        item {
            // سرگروهِ تاریخچه + فیلترِ «همه موارد» (طرحِ مرجعِ کاربر، ۱ مهر).
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("تاریخچه", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                HistoryFilterChip(earnedOnly) { earnedOnly = it }
            }
        }
        if (events.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = "هنوز سکه‌ای جمع نکردی",
                    description = "با ثبتِ روزانه‌ی تراکنش، وصل‌کردنِ پیامکِ بانک و گرفتنِ نشان، سکه جمع می‌شود.",
                )
            }
        }
        val shownEvents = when (earnedOnly) {
            true -> events.filter { it.amount > 0 }
            false -> events.filter { it.amount < 0 }
            null -> events
        }
        items(shownEvents, key = { it.id }) { event ->
            CoinEventRow(event)
        }
    }

    // ترمیم **دیالوگِ تایید می‌گیرد** (`46a`): صد سکه از دفتر کم می‌شود و واگرد ندارد.
    // لحنِ خنثی، نه خطر - کارِ بدی نیست، فقط برگشت‌ناپذیر است.
    if (confirmRepair) {
        ConfirmDialog(
            title = "رشته را ترمیم کنم؟",
            consequence = "${toFa(GamificationRepository.Reward.STREAK_REPAIR)} سکه کم می‌شود و برگشت ندارد. این ماه دوباره نمی‌شود.",
            actionLabel = "ترمیم کن",
            tone = ConfirmTone.HEAVY_CHANGE,
            onConfirm = {
                confirmRepair = false
                viewModel.repairStreak()
            },
            onDismiss = { confirmRepair = false },
        )
    }
}

/**
 * قیمتِ ترمیمِ زنجیر.
 *
 * ⚠️ در `CoinSpend` ردیفی برای ترمیم نیست و اضافه‌اش نکردم، چون آن enum فروشگاه است و
 * ترمیم قلمِ فروشگاهی نیست (دسته ندارد، `timeGated` نیست، مالکیت نمی‌آورد). ولی عدد باید
 * **یک‌جا** باشد - اگر ترجیح می‌دهید در `CoinEconomy.kt` بنشیند، ببریدش و این را حذف کنید.
 */

/** جوهرِ رشته. */
private val StreakInk: Color @Composable get() = AppDangerInk

/**
 * ردیفِ «فعال» - فریمِ `56a`.
 *
 * عددِ تنها همان چیزی بود که حذفش را از هدر توجیه کرد؛ اگر این‌جا هم فقط عدد باشد، فقط
 * جایش عوض شده. پس نوارِ هفته و پاداشِ `WEEK_COMPLETE` هم می‌آیند: رشته وقتی معنی دارد
 * که بگوید چه چیزی روی میز است.
 */
@Composable
private fun StreakRow(days: Int, todayHasEntry: Boolean, brokenDays: Int?) {
    val shown = brokenDays ?: days
    if (shown <= 0) {
        AppCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StreakIcon(tint = AppMuted, bg = AppIconFrame)
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("هنوز رشته‌ای نداری", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(
                        "دو روزِ پشتِ‌هم شروعش می‌کند",
                        color = AppLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        return
    }

    // نوارِ هفته از `WEEK_COMPLETE` می‌آید نه از رشته‌ی کل: رشته‌ی ۱۲روزه یعنی هفته‌ی
    // اول تمام شده و پنج روز از هفته‌ی دوم مانده.
    val inWeek = shown % 7
    val toBonus = if (inWeek == 0) 7 else 7 - inWeek
    val atRisk = !todayHasEntry && brokenDays == null

    AppCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreakIcon(tint = StreakInk, bg = AppGoldPillSoft)
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        "${toFa(shown)} روزِ پیاپی",
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        when {
                            brokenDays != null -> "پاره شده - هنوز قابلِ برگشت"
                            atRisk -> "امروز هنوز چیزی ثبت نکرده‌ای"
                            else -> "امروز ثبت کرده‌ای"
                        },
                        color = if (atRisk || brokenDays != null) StreakInk else AppLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                // پاداشِ بعدی فقط وقتی می‌آید که رشته زنده باشد - وعده به کسی که رشته‌اش
                // پاره شده، طعنه است.
                if (brokenDays == null) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp).width(1.dp).height(30.dp).background(AppLineRow))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppPrimaryPill)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${toFa(toBonus)} روز تا ${toFa(CoinReason.FULL_WEEK.amount)} سکه",
                            color = AppPrimaryInk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Icon(
                            Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = AppPrimaryInk,
                            modifier = Modifier.padding(start = 6.dp).size(18.dp),
                        )
                    }
                }
            }
            if (brokenDays == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    repeat(7) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (i < inWeek) AppPrimary else AppLineRow),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakIcon(tint: Color, bg: Color) {
    Box(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(AppRadius.row)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(17.dp),
        )
    }
}

/**
 * کارتِ ترمیمِ زنجیر - فریمِ `56b`.
 *
 * منطقش از قبل در `GamificationViewModel` بود (`repairable` / `repairStreak()`) و فقط
 * جایی برای دیده‌شدن نداشت.
 */
@Composable
private fun StreakRepairCard(days: Int, hoursLeft: Int, price: Int, balance: Int, onRepair: () -> Unit) {
    val affordable = balance >= price
    AppCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(AppRadius.row)).background(AppGoldPillSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = null,
                        tint = StreakInk,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        "رشته‌ی ${toFa(days)}روزه‌ات پاره شد",
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "با ${toFa(price)} سکه برمی‌گردد، سرِ همان عدد.",
                        color = AppMuted,
                        fontSize = 10.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            ) {
                // `GradientButton` پارامترِ `text` ندارد؛ محتوا را به‌صورتِ بلوک می‌گیرد.
                GradientButton(onClick = onRepair, enabled = affordable) {
                    Text(
                        "${toFa(price)} سکه · ترمیم کن",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    // مهلت **به ساعت**، نه «۴۸ ساعت»: عددِ ثابت هر بار همان است و کاربر
                    // عجله نمی‌کند؛ عددی که پایین می‌آید می‌گوید فرصت دارد تمام می‌شود.
                    Text(
                        "${toFa(hoursLeft)} ساعت مانده",
                        color = AppLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (affordable) "این ماه یک‌بار" else "${toFa(price - balance)} سکه کم داری",
                        color = if (affordable) AppLabel else AppDangerInk,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinEventRow(event: CoinEventEntity) {
    // طرحِ مرجعِ کاربر (۱ مهر): فلش سمتِ راست، عنوان و زمان، مبلغ با «سکه» زیرش، و دایره‌ی
    // +/− رنگی سمتِ چپ - تا دریافت و خرج از دور و بی خواندنِ عدد فرق کنند.
    val spent = event.amount < 0
    val ink = if (spent) AppDangerInk else AppPrimaryInk
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(coinEventLabel(event.type), color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text(
                    formatEventTime(event.createdAt),
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    (if (spent) "−" else "+") + toFa(kotlin.math.abs(event.amount)),
                    color = ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    // 🚨 بی این، «+۱۰» در ستونِ باریک دو خط می‌شد و «۰» زیرش می‌افتاد.
                    maxLines = 1,
                    softWrap = false,
                )
                Text("سکه", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (spent) AppDangerInk.copy(alpha = 0.12f) else AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (spent) Icons.Filled.Remove else Icons.Filled.Add,
                    contentDescription = if (spent) "خرج" else "دریافت",
                    tint = ink,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** چیپِ «همه موارد ▾» با منوی سه‌گزینه‌ای. */
@Composable
private fun HistoryFilterChip(earnedOnly: Boolean?, onChange: (Boolean?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val label = when (earnedOnly) { true -> "دریافت‌ها"; false -> "خرج‌ها"; null -> "همه موارد" }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AppIconFrame)
                .pressScaleClickable { open = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AppMuted, modifier = Modifier.padding(start = 6.dp).size(16.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf<Pair<Boolean?, String>>(null to "همه موارد", true to "دریافت‌ها", false to "خرج‌ها").forEach { (value, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onChange(value); open = false })
            }
        }
    }
}

/** برچسبِ فارسیِ هر نوعِ رویداد - عیناً واژه‌های جدولِ `20e`. */
private fun coinEventLabel(type: String): String = when (type) {
    GamificationRepository.Type.NEW_PHONE_GIFT -> "هدیه‌ی شروع"
    GamificationRepository.Type.DAILY_LOG -> "ثبتِ روزانه"
    GamificationRepository.Type.WEEK_COMPLETE -> "هفته‌ی کاملِ فعال بودن"
    GamificationRepository.Type.CONNECT_SMS -> "وصل‌کردنِ پیامکِ بانکی"
    GamificationRepository.Type.CONNECT_NOTIFICATION -> "وصل‌کردنِ اعلانِ بانک"
    GamificationRepository.Type.COMPLETE_PROFILE -> "تکمیلِ پروفایل"
    GamificationRepository.Type.FIRST_BUDGET -> "اولین بودجه"
    GamificationRepository.Type.FIRST_BACKUP -> "اولین پشتیبان‌گیری"
    GamificationRepository.Type.BADGE -> "نشانِ تازه"
    GamificationRepository.Type.SPEND_SUBSCRIPTION -> "تخفیفِ تمدیدِ اشتراک"
    else -> "سکه"
}

/** نرخِ تبدیلِ صریحِ طرح: **هر ۱۰ سکه = ۱٬۰۰۰ ریال تخفیف**. */
internal fun coinsToRial(coins: Int): Double = coins * 100.0

/** «۲۶ مرداد · ۲۱:۱۴» - تاریخِ شمسی، دقیقاً مثلِ ردیف‌های کارتِ `20d`. */
private fun formatEventTime(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val jalali = JalaliCalendar.fromGregorian(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
    )
    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(millis))
    return "${toFa(jalali.d)} ${persianMonthName(jalali.m)} · ${toFa(time)}"
}
