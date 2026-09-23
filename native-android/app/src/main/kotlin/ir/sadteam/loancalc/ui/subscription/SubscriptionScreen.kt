package ir.sadteam.loancalc.ui.subscription

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.settings.SettingsGroup
import ir.sadteam.loancalc.ui.settings.SettingsRowItem
import ir.sadteam.loancalc.ui.settings.SettingsDivider
import ir.sadteam.loancalc.ui.settings.SettingsTone
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.subscription.LocalSubscriptionManager
import ir.sadteam.loancalc.subscription.subscriptionTiers
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText

/** درصدِ صرفه‌جویی نسبت به N× قیمتِ پلنِ ۱ماهه (۳۰۰٬۰۰۰/۸۱۰٬۰۰۰/۱٬۴۴۰٬۰۰۰/۲٬۵۲۰٬۰۰۰ ریال) - تاییدشده
 * تو CLAUDE.md. مورد ۲۱: قبلاً این بجِ تخفیف اصلاً هیچ‌جای این صفحه نبود، الان رو هر سه پلنِ
 * چندماهه (نه فقط سه‌ماهه) نشون داده می‌شه. */
private val discountPercentByProductId = mapOf(
    "unlimited_loans_3m" to 10,
    "unlimited_loans_6m" to 20,
    "unlimited_loans_1y" to 30,
)

/**
 * پورت #subscriptionModal تو www/index.html - ۴ پلن پلکانی، خرید واقعی با SDK بومیِ استور (Poolakey
 * رو فلیورِ cafebazaar، myket-billing-client رو فلیورِ myket - رجوع کن به app/src/cafebazaar و
 * app/src/myket)، تایید سمت سرور قبل از فعال‌شدن. اگه استورِ موردنظر رو گوشی نصب نباشه یا سرویس وصل
 * نشه، [LocalSubscriptionManager] پیام صادقانه‌ی «فقط رو نسخه‌ی نصبی کار می‌کنه» می‌ده.
 *
 * قیمت‌ها (رجوع کن به [subscriptionTiers]) بی‌قید و شرط از استور خونده می‌شن - این صفحه دیگه
 * پشتِ گیتِ ورود نیست (خواسته‌ی کاربر: «می‌خوام اشتراک‌ها قیمتشون معلوم باشه، نیاز نباشه حتما ورود
 * کرد»)؛ فقط دکمه‌ی خرید، چون سمت سرور نیاز به توکن داره (AuthRepository.verifySubscription)، اگه
 * کاربر لاگین نباشه به‌جای شروع خرید [onNeedsLogin] رو صدا می‌زنه.
 */
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onSubscribed: () -> Unit,
    onNeedsLogin: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val subscriptionManager = LocalSubscriptionManager.current
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val subscribedUntil by authViewModel.subscribedUntil.collectAsState()
    val subscriptionTier by authViewModel.subscriptionTier.collectAsState()
    var prices by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var purchasingProductId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    // کدِ هدیه: متنِ فیلد، در حالِ ارسال، و پیامِ موفقیت.
    var giftCode by rememberSaveable { mutableStateOf("") }
    var redeeming by remember { mutableStateOf(false) }
    var giftMessage by remember { mutableStateOf<String?>(null) }
    // این صفحه دو نما داره (خواسته‌ی صریحِ کاربر، هم‌الگو با اپِ رفرنس): «مرورِ اشتراک» که وضعیت و
    // مزیت‌ها رو نشون می‌ده، و «تعرفه‌ها» که همون لیستِ خریدِ قبلیه. پیش‌فرض مرورـه.
    var showPlans by remember { mutableStateOf(false) }
    val purchaseHistory by authViewModel.purchaseHistory.collectAsState()

    LaunchedEffect(gateState) {
        if (gateState == GateState.LOGGED_IN) authViewModel.loadPurchaseHistory()
    }

    LaunchedEffect(subscriptionManager) {
        subscriptionManager?.getPrices(
            productIds = subscriptionTiers.map { it.first },
            onResult = { prices = it },
            onError = { },
        )
    }

    // بازیابیِ خودکارِ خریدهایی که پول‌شون گرفته شده ولی (به‌خاطرِ باگِ قبلیِ فلیورِ myket، رجوع کن
    // به SubscriptionManager.handleActivityResult) هیچ‌وقت به سرور اعلام نشدن - کاربر مجبور نیست
    // دوباره پول بده یا با پشتیبانی تماس بگیره، فقط با بازکردنِ همین صفحه خودش حل می‌شه.
    LaunchedEffect(subscriptionManager, gateState) {
        if (gateState != GateState.LOGGED_IN) return@LaunchedEffect
        subscriptionManager?.restorePurchases()?.forEach { (productId, purchaseToken) ->
            authViewModel.verifySubscriptionPurchase(
                productId = productId,
                purchaseToken = purchaseToken,
                onSuccess = { onSubscribed() },
                onError = { },
            )
        }
    }

    // پلنِ انتخاب‌شده (بسته‌ی ChatGPT): پیش‌فرض یک‌ساله، همان که دکمه‌ی بزرگ می‌خرد.
    var selectedProductId by rememberSaveable { mutableStateOf(subscriptionTiers.last().first) }
    // سه ردیفِ بازشونده‌ی پایینِ صفحه.
    var showHistory by remember { mutableStateOf(false) }
    var showExpiryInfo by remember { mutableStateOf(false) }
    var showGift by remember { mutableStateOf(false) }

    fun startPurchase(productId: String) {
        if (gateState != GateState.LOGGED_IN) {
            onNeedsLogin()
            return
        }
        error = null
        purchasingProductId = productId
        subscriptionManager?.purchase(
            productId = productId,
            onSucceed = { purchaseToken ->
                authViewModel.verifySubscriptionPurchase(
                    productId = productId,
                    purchaseToken = purchaseToken,
                    onSuccess = { purchasingProductId = null; onSubscribed() },
                    onError = {
                        purchasingProductId = null
                        error = "تایید خرید ناموفق بود؛ اگه پول کم شده با پشتیبانی تماس بگیر"
                    },
                )
            },
            onFailed = { purchasingProductId = null; error = "خرید ناموفق بود" },
            onCanceled = { purchasingProductId = null },
        )
    }

    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 24.dp)) {
        // ── سربرگ ─────────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppSurface)
                        .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                        .pressScaleClickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("اشتراک", color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("امکاناتِ بیشتر، تجربه‌ی کامل‌تر", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── کارتِ «جیبک پلاس» - همان کارتِ رنگیِ بالای بقیه‌ی صفحه‌ها (با تم عوض می‌شود) ──
        item {
            val expiry = remember(subscribedUntil) { parseSubscribedUntil(subscribedUntil) }
            AppHeroCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(AppAccent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = AppGoldInk, modifier = Modifier.size(22.dp))
                            }
                            Text(
                                PLUS_NAME,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                        Text(
                            "بدونِ محدودیت از همه‌ی امکاناتِ اپ استفاده کن",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(64.dp)
                            .background(Color.White.copy(alpha = 0.25f)),
                    )
                    Column(modifier = Modifier.width(96.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val tierLabel = when (subscriptionTier) {
                            "1m" -> "یک‌ماهه"
                            "3m" -> "سه‌ماهه"
                            "6m" -> "شش‌ماهه"
                            "1y" -> "یک‌ساله"
                            else -> null
                        }
                        Text(
                            if (subscribed) "اشتراک فعال" else "بدونِ اشتراک",
                            color = if (subscribed) AppAccent else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                        if (subscribed && tierLabel != null) {
                            Text(tierLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        if (subscribed) {
                            Text(
                                if (expiry != null) {
                                    "تا ${toFa(expiry.date.d)} ${persianMonthName(expiry.date.m)} ${toFa(expiry.date.y)}"
                                } else {
                                    "اشتراکِ دائمی"
                                },
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            if (expiry != null) {
                                Text(
                                    "(${toFa(expiry.daysLeft)} روزِ دیگه)",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HeroChip(Icons.Filled.AllInclusive, "بدونِ محدودیت")
                    HeroChip(Icons.Filled.Lock, "امن و مطمئن")
                    HeroChip(Icons.Filled.Bolt, "دسترسیِ کامل")
                }
            }
        }

        // ── مزایا - فقط چیزهایی که واقعاً پشتِ اشتراک‌اند ──────────────────────────────────
        item {
            AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("مزایای $PLUS_NAME", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(
                    "با $PLUS_NAME بدونِ محدودیت و راحت‌تر کار کن",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
                val benefits = listOf(
                    Benefit(Icons.Filled.WorkspacePremium, "وامِ نامحدود", "هر تعداد وام، بدونِ قفل شدن", SettingsTone.GREEN),
                    Benefit(Icons.Filled.CloudDone, "پشتیبان‌گیریِ ابری", "داده‌هات همیشه امن و در دسترس", SettingsTone.BLUE),
                    Benefit(Icons.Filled.Sms, "ثبتِ خودکار از پیامک", "برداشت و واریز بدونِ تایپِ دستی", SettingsTone.PURPLE),
                    Benefit(Icons.Filled.Receipt, "چک و دسته‌چکِ نامحدود", "با استعلامِ صیادی و یادآوری", SettingsTone.ORANGE),
                    Benefit(Icons.Filled.Sync, "همگام‌سازی بینِ گوشی‌ها", "با شماره‌ت هرجا همون اطلاعات", SettingsTone.BLUE),
                    Benefit(Icons.Filled.Description, "خروجیِ PDF و اکسل", "گزارشِ کامل برای چاپ یا آرشیو", SettingsTone.RED),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    benefits.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { BenefitTile(it, Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        // ── انتخابِ پلن ──────────────────────────────────────────────────────────────
        item {
            AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("انتخابِ اشتراک", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(
                    if (gateState == GateState.LOGGED_IN) "مدتِ موردِ نظرت را انتخاب کن" else "قیمت‌ها را ببین؛ برای خرید اول باید وارد حساب بشی",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
                if (subscriptionManager == null) {
                    Text(
                        "خرید فقط رو نسخه‌ی نصبی اپ (از کافه‌بازار یا مایکت) کار می‌کنه.",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                } else {
                    val monthly = priceRial(prices[subscriptionTiers.first().first])
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        // چهار کارت کنارِ هم فقط وقتی جا می‌شوند؛ گوشیِ باریک دوتادوتا.
                        val perRow = if (maxWidth >= 320.dp) 4 else 2
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            subscriptionTiers.chunked(perRow).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    row.forEach { (productId, label) ->
                                        PlanCard(
                                            label = label.removePrefix("اشتراک "),
                                            priceText = prices[productId],
                                            months = monthsOf(productId),
                                            monthlyBase = monthly,
                                            fallbackDiscount = discountPercentByProductId[productId],
                                            selected = productId == selectedProductId,
                                            onClick = { selectedProductId = productId },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── دکمه‌ی خرید - به پلنِ انتخاب‌شده وصل است ─────────────────────────────────────
        if (subscriptionManager != null) {
            item {
                val label = subscriptionTiers.firstOrNull { it.first == selectedProductId }?.second ?: ""
                val busy = purchasingProductId != null
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(10.dp, RoundedCornerShape(32.dp), ambientColor = AppPrimary.copy(alpha = 0.3f), spotColor = AppPrimary.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(32.dp))
                        .background(AppPrimary)
                        .pressScaleClickable(scale = 0.98f) { if (!busy) startPurchase(selectedProductId) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (busy) {
                            LottieSpinner(modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                if (subscribed) "تمدید با $label" else "خریدِ $label",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                if (subscribed) "روزها به انتهای اشتراکِ فعلی اضافه می‌شوند" else "دسترسیِ کامل و بدونِ محدودیت",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        if (error != null) {
            item {
                Text(
                    text = error ?: "",
                    color = AppDanger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                )
            }
        }

        // ── ردیف‌های پایین: اشتراک‌های قبلی، «وقتی تموم بشه»، کدِ هدیه ───────────────────
        item {
            SettingsGroup(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                SettingsRowItem(
                    title = "اشتراک‌های قبلی",
                    icon = Icons.Filled.Receipt,
                    tone = SettingsTone.PURPLE,
                    status = "تاریخچه‌ی خریدهای تو",
                    onClick = { showHistory = !showHistory },
                )
                if (showHistory) {
                    // تاریخچه از GET /api/subscription/history؛ سرور فقط از مرداد ۱۴۰۵ ثبت می‌کند.
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                        val history = purchaseHistory
                        when {
                            gateState != GateState.LOGGED_IN -> InfoText("برای دیدنِ تاریخچه‌ی خرید باید وارد حسابت بشی.")
                            history == null -> InfoText("در حال دریافت…")
                            history.isEmpty() -> InfoText("هنوز خریدی ثبت نشده.")
                            else -> history.forEach { p ->
                                val bought = parseServerDate(p.createdAt)
                                val until = parseServerDate(p.subscribedUntil)
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = AppAccent, modifier = Modifier.size(18.dp))
                                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                                        Text(tierDisplayName(p.tier), color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        if (until != null) {
                                            Text(
                                                "اعتبار تا ${toFa(until.d)} ${persianMonthName(until.m)} ${toFa(until.y)}",
                                                color = AppMuted,
                                                fontSize = 10.5.sp,
                                            )
                                        }
                                    }
                                    if (bought != null) {
                                        Text("${toFa(bought.d)} ${persianMonthName(bought.m)} ${toFa(bought.y)}", color = AppMuted, fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                SettingsDivider()
                SettingsRowItem(
                    title = "وقتی اشتراکم تموم بشه چی می‌شه؟",
                    icon = Icons.Filled.Info,
                    tone = SettingsTone.BLUE,
                    status = "هیچ داده‌ای پاک نمی‌شه",
                    onClick = { showExpiryInfo = !showExpiryInfo },
                )
                if (showExpiryInfo) {
                    // محتوا عمداً همان چیزی است که در کد واقعاً پشتِ اشتراک است
                    // (MyLoansScreen.canSaveAnotherLoan و AutoBackupWorker).
                    Text(
                        "کلِ حسابداری (دخل و خرج، بودجه، دسته‌بندی‌ها، گزارش‌ها و طلب و بدهی) بدونِ محدودیت " +
                            "باز می‌مونه. فقط دو چیز محدود می‌شه: از وام‌های ذخیره‌شده‌ت اولی باز می‌مونه و بقیه " +
                            "قفل می‌شن (قفل، نه حذف - با تمدید دوباره باز می‌شن)، و پشتیبان‌گیریِ خودکار رو سرور متوقف می‌شه.",
                        color = AppMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    )
                }
                SettingsDivider()
                // 🎁 کدِ هدیه - روزها به انتهای اشتراکِ فعلی اضافه می‌شوند.
                SettingsRowItem(
                    title = "کدِ هدیه داری؟",
                    icon = Icons.Filled.CardGiftcard,
                    tone = SettingsTone.GREEN,
                    status = "کدِ جایزه یا هدیه‌ی گزارشِ باگ",
                    onClick = { showGift = !showGift },
                )
                if (showGift || giftCode.isNotEmpty() || giftMessage != null) {
                    Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // ⚠️ کد ذاتاً چپ‌به‌راست است، پس در `Ltr` پیچیده می‌شود.
                            Ltr {
                                OutlinedTextField(
                                    value = giftCode,
                                    onValueChange = { giftCode = it.uppercase() },
                                    placeholder = { Text("JIBAK-XXXXX-XXXXX", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            GradientButton(
                                enabled = giftCode.isNotBlank() && !redeeming,
                                onClick = {
                                    if (gateState != GateState.LOGGED_IN) {
                                        onNeedsLogin()
                                        return@GradientButton
                                    }
                                    error = null
                                    giftMessage = null
                                    redeeming = true
                                    authViewModel.redeemGiftCode(
                                        code = giftCode,
                                        onSuccess = {
                                            redeeming = false
                                            giftCode = ""
                                            giftMessage = "هدیه فعال شد 🎁"
                                        },
                                        onError = { code ->
                                            redeeming = false
                                            error = when (code) {
                                                "already_used" -> "این کد قبلاً استفاده شده"
                                                "expired" -> "مهلتِ این کد تمام شده"
                                                "not_found" -> "کد پیدا نشد؛ دوباره نگاهش کن"
                                                else -> "فعال‌سازی ناموفق بود؛ اینترنت را بررسی کن"
                                            }
                                        },
                                    )
                                },
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                if (redeeming) LottieSpinner(modifier = Modifier.size(18.dp)) else Text("فعال کن")
                            }
                        }
                        if (giftMessage != null) {
                            Text(
                                giftMessage ?: "",
                                color = AppPrimaryInk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Benefit(val icon: ImageVector, val title: String, val hint: String, val tone: SettingsTone)

@Composable
private fun BenefitTile(benefit: Benefit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface2)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(benefit.tone.fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(benefit.icon, contentDescription = null, tint = benefit.tone.ink, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(benefit.title, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black, lineHeight = 15.sp)
            Text(benefit.hint, color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp)
        }
    }
}

/** برچسبِ کوچکِ سفیدِ روی کارتِ رنگی. */
@Composable
private fun HeroChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun InfoText(text: String) = Text(text, color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(vertical = 4.dp))

/** کارتِ یک پلن. قیمت متنِ خودِ استور است؛ «٪ تخفیف» و «≈ ماهی» از روی همان عدد حساب می‌شوند. */
@Composable
private fun PlanCard(
    label: String,
    priceText: String?,
    months: Int,
    monthlyBase: Long?,
    fallbackDiscount: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rial = priceRial(priceText)
    val discount = if (rial != null && monthlyBase != null && monthlyBase > 0 && months > 1) {
        (100 - rial * 100 / (monthlyBase * months)).toInt().takeIf { it > 0 }
    } else {
        fallbackDiscount.takeIf { months > 1 }
    }
    val shape = RoundedCornerShape(18.dp)
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) AppPrimaryPill else AppSurface)
                .border(if (selected) 2.dp else 1.dp, if (selected) AppPrimary else AppLine, shape)
                .pressScaleClickable(scale = 0.97f, onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Box(modifier = Modifier.padding(top = 5.dp).height(18.dp), contentAlignment = Alignment.Center) {
                if (discount != null) {
                    Text(
                        "٪${toFa(discount)} تخفیف",
                        color = AppPrimaryInk,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppInfoPill)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(
                if (rial != null) groupedFa(rial) else (priceText ?: "…"),
                color = AppText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (rial != null) {
                Text("ریال", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    "≈ ماهی ${groupedFa(rial / months)}",
                    color = AppMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AppPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
    }
}

private fun monthsOf(productId: String): Int = when {
    productId.endsWith("_1y") -> 12
    productId.endsWith("_6m") -> 6
    productId.endsWith("_3m") -> 3
    else -> 1
}

/**
 * عددِ ریالیِ متنِ قیمتِ استور («۳۰۰٬۰۰۰ ریال»). اگر واحد تومان باشد ×۱۰. نامعلوم ← `null`
 * تا به‌جای عددِ غلط همان متنِ خامِ استور نشان داده شود.
 */
private fun priceRial(text: String?): Long? {
    if (text.isNullOrBlank()) return null
    val digits = text.map { c ->
        when (c) {
            in '۰'..'۹' -> '0' + (c - '۰')
            in '٠'..'٩' -> '0' + (c - '٠')
            else -> c
        }
    }.filter { it.isDigit() }.joinToString("")
    val n = digits.toLongOrNull()?.takeIf { it > 0 } ?: return null
    return if ("تومان" in text) n * 10 else n
}

private fun groupedFa(n: Long): String = fmt(n.toDouble())
