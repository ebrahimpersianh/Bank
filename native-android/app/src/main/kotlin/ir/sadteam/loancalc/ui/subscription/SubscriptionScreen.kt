package ir.sadteam.loancalc.ui.subscription

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
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
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

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(
                    "اشتراک",
                    color = AppText,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }

        // ── مرورِ اشتراک - **همیشه** نشون داده می‌شه ────────────────────────────────────────
        // خواسته‌ی صریحِ کاربر (تسکِ #31): «تعرفه‌ها رو *همون صفحه* باز بشه، نه صفحه‌ی جدا». پس
        // به‌جای دو نمای جایگزینِ هم، لیستِ تعرفه‌ها زیرِ همین محتوا **باز می‌شه**.
        run {
            item {
                val expiry = remember(subscribedUntil) { parseSubscribedUntil(subscribedUntil) }
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AppGoldFrom),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = AppAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(PLUS_NAME, color = AppText, fontSize = 15.sp)
                            Text(
                                "بدونِ محدودیت از همه‌ی امکاناتِ اپ استفاده کن",
                                color = AppMuted,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    // وضعیتِ فعلی: نوعِ اشتراک + تاریخِ انقضا با شمارشِ روزِ باقی‌مونده.
                    val tierLabel = when (subscriptionTier) {
                        "1m" -> "اشتراکِ یک‌ماهه"
                        "3m" -> "اشتراکِ سه‌ماهه"
                        "6m" -> "اشتراکِ شش‌ماهه"
                        "1y" -> "اشتراکِ یک‌ساله"
                        else -> null
                    }
                    Text(
                        when {
                            !subscribed -> "الان اشتراکِ فعالی نداری"
                            tierLabel != null -> tierLabel
                            else -> "اشتراکِ فعال"
                        },
                        color = AppText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    if (subscribed) {
                        if (expiry != null) {
                            Text(
                                "تا ${toFa(expiry.date.d)} ${persianMonthName(expiry.date.m)} ${toFa(expiry.date.y)} " +
                                    "(${toFa(expiry.daysLeft)} روزِ دیگه)",
                                color = AppAccent,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        } else {
                            Text("اشتراکِ دائمی", color = AppAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }

            item {
                // باکسِ «وقتی اشتراکم تموم بشه چی می‌شه؟» - محتواش عمداً دقیقاً همون چیزیه که تو
                // کد واقعاً پشتِ اشتراکه (MyLoansScreen.canSaveAnotherLoan و AutoBackupWorker)،
                // نه یه متنِ تبلیغاتیِ کلی.
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("وقتی اشتراکم تموم بشه چی می‌شه؟", color = AppText, fontSize = 13.5.sp)
                    Text(
                        "نگران نباش، هیچ داده‌ای پاک نمی‌شه. کلِ حسابداری (دخل و خرج، بودجه، " +
                            "دسته‌بندی‌ها، گزارش‌ها و طلب و بدهی) بدونِ محدودیت باز می‌مونه.\n\n" +
                            "فقط دو چیز محدود می‌شه: از وام‌های ذخیره‌شده‌ت اولی باز می‌مونه و بقیه " +
                            "قفل می‌شن (قفل، نه حذف - با تمدید دوباره باز می‌شن)، و پشتیبان‌گیریِ " +
                            "خودکار رو سرور متوقف می‌شه.",
                        color = AppMuted,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            item {
                // تاریخچه‌ی خریدها - از GET /api/subscription/history میاد. سرور فقط از مرداد ۱۴۰۵
                // خریدها رو ثبت می‌کنه، پس برای کاربرِ قدیمی می‌تونه خالی باشه و این طبیعیه.
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("اشتراک‌های خریداری‌شده", color = AppText, fontSize = 13.5.sp)
                    val history = purchaseHistory
                    when {
                        gateState != GateState.LOGGED_IN -> Text(
                            "برای دیدنِ تاریخچه‌ی خرید باید وارد حسابت بشی.",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        history == null -> Text(
                            "در حال دریافت…",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        history.isEmpty() -> Text(
                            "هنوز خریدی ثبت نشده.",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        else -> Column(modifier = Modifier.padding(top = 8.dp)) {
                            history.forEach { p ->
                                val bought = parseServerDate(p.createdAt)
                                val until = parseServerDate(p.subscribedUntil)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = AppAccent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                                        Text(tierDisplayName(p.tier), color = AppText, fontSize = 13.sp)
                                        if (until != null) {
                                            Text(
                                                "اعتبار تا ${toFa(until.d)} ${persianMonthName(until.m)} ${toFa(until.y)}",
                                                color = AppMuted,
                                                fontSize = 11.sp,
                                            )
                                        }
                                    }
                                    if (bought != null) {
                                        Text(
                                            "${toFa(bought.d)} ${persianMonthName(bought.m)} ${toFa(bought.y)}",
                                            color = AppMuted,
                                            fontSize = 11.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("با اشتراک چی گیرت میاد؟", color = AppText, fontSize = 13.5.sp)
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        SubscriptionBenefit(
                            icon = Icons.Filled.WorkspacePremium,
                            title = "وامِ نامحدود",
                            subtitle = "هر تعداد وام که خواستی ذخیره کن، بدونِ قفل شدن",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.CloudDone,
                            title = "پشتیبان‌گیریِ خودکارِ ابری",
                            subtitle = "اگه گوشیت گم شد یا عوض کردی، داده‌هات برمی‌گردن",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.Sms,
                            title = "ثبتِ خودکار از پیامک و اعلانِ بانک",
                            subtitle = "برداشت و واریز خودکار ثبت می‌شه، بدونِ تایپِ دستی",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.Receipt,
                            title = "چک و دسته‌چکِ نامحدود",
                            subtitle = "با استعلامِ صیادی، یادآوری و بایگانی",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.Sync,
                            title = "همگام‌سازی بینِ گوشی‌هات",
                            subtitle = "با شماره‌ت هرجا وارد شی، همون اطلاعات رو داری",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.Block,
                            title = "بدونِ تبلیغات",
                            subtitle = "تا وقتی اشتراک داری هیچ تبلیغی تو برنامه نمی‌بینی",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.Description,
                            title = "خروجیِ PDF و اکسل",
                            subtitle = "گزارشِ کاملِ تراکنش‌ها برای چاپ یا آرشیو",
                        )
                        SubscriptionBenefit(
                            icon = Icons.Filled.SupportAgent,
                            title = "پشتیبانیِ مستقیم",
                            subtitle = "پیامت اولویت‌دار به تیمِ پشتیبانی می‌رسه",
                        )
                    }
                }
            }

            item {
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    SubscriptionRow(
                        icon = Icons.Filled.LocalOffer,
                        title = if (showPlans) "بستنِ تعرفه‌ها" else "مشاهده‌ی تعرفه‌ها",
                        subtitle = "قیمتِ پلن‌ها و خریدِ اشتراک",
                        onClick = { showPlans = !showPlans },
                    )
                }
            }
        }

        // ── تعرفه‌ها - همین‌جا زیرِ همون ردیف باز می‌شه ────────────────────────────────────
        if (showPlans && subscriptionManager == null) {
            item {
                Text(
                    "این قابلیت فقط رو نسخه‌ی نصبی اپ (از کافه‌بازار) کار می‌کنه.",
                    color = AppMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        } else if (showPlans) {
            if (gateState != GateState.LOGGED_IN) {
                item {
                    Text(
                        "قیمت‌های زیر رو ببین؛ برای تکمیل خرید، اول باید وارد حساب بشی.",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    )
                }
            }
            items(subscriptionTiers, key = { it.first }) { (productId, label) ->
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label, color = AppText, fontSize = 14.sp)
                                discountPercentByProductId[productId]?.let { pct ->
                                    Text(
                                        "٪${toFa(pct)} تخفیف",
                                        color = AppPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .background(AppPrimaryPill, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(prices[productId] ?: "…", color = AppMuted, fontSize = 12.sp)
                        }
                        GradientButton(
                            enabled = purchasingProductId == null,
                            onClick = {
                                if (gateState != GateState.LOGGED_IN) {
                                    onNeedsLogin()
                                    return@GradientButton
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
                            },
                        ) {
                            if (purchasingProductId == productId) {
                                LottieSpinner(modifier = Modifier.size(18.dp))
                            } else {
                                Text("خرید")
                            }
                        }
                    }
                }
            }
        }

        if (showPlans) {
            item {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showPlans = false },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("بازگشت به وضعیتِ اشتراک", fontSize = 12.5.sp)
                }
            }
        }

        if (error != null) {
            item {
                Text(
                    text = error ?: "",
                    color = AppDanger,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** یه ردیفِ قابلِ‌تپِ صفحه‌ی اشتراک (تعرفه‌ها/تاریخچه) - آیکون + عنوان + زیرعنوان + فلش. */
@Composable
private fun SubscriptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(title, color = AppText, fontSize = 13.sp)
                Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}

/** یه مزیتِ اشتراک تو لیستِ «با اشتراک چی گیرت میاد؟». */
@Composable
private fun SubscriptionBenefit(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(title, color = AppText, fontSize = 12.5.sp)
            Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
