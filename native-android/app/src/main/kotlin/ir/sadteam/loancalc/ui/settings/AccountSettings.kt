package ir.sadteam.loancalc.ui.settings

import androidx.compose.material.icons.filled.PhoneIphone
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppStroke
import androidx.compose.material.icons.filled.WorkspacePremium
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.components.AppHeroCard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.AvatarPicker
import ir.sadteam.loancalc.ui.components.FramedAvatar
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerState
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.subscription.parseSubscribedUntil
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.components.persianMonthName

@Composable
internal fun AccountSettings(
    authViewModel: AuthViewModel,
    banner: InAppBannerState,
    onShowLoginPrompt: () -> Unit,
    onShowSubscription: () -> Unit,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
    val subscribedUntil by authViewModel.subscribedUntil.collectAsState()
    val subscriptionTier by authViewModel.subscriptionTier.collectAsState()
    val savedName by authViewModel.userName.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val userCode by authViewModel.userCode.collectAsState()
    val idClipboard = LocalClipboardManager.current
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var deleteAccountInProgress by remember { mutableStateOf(false) }

    var showAvatarSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }

    if (gateState == GateState.LOGGED_IN) {
        // ── کارتِ هویت (فریمِ حساب کاربری) ────────────────────────────────────
        // دکمه‌ی تمام‌عرضِ «تغییرِ آدمک» عمداً حذف شد - مدادِ روی خودِ آدمک همون کاره و
        // دکمه‌ی تمام‌عرض بالای صفحه وزنِ بی‌دلیل می‌گیره.
        val avatarViewModel: AvatarViewModel = hiltViewModel()
        val avatar by avatarViewModel.avatar.collectAsState()
        val avatarFrame by avatarViewModel.frame.collectAsState()
        // کارتِ هویتِ فشرده: صفحه با «خودِ کاربر» شروع می‌شود، نه یک فضای خالیِ بزرگ.
        // همان کارتِ رنگیِ بالای بقیه‌ی صفحه‌ها (با تم عوض می‌شود)؛ آدمک و قاب همان هدرِ خانه.
        AppHeroCard(modifier = Modifier.padding(top = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FramedAvatar(avatar, size = 68.dp, frame = avatarFrame)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppSurface)
                            .border(1.5.dp, AppPrimaryBorder, CircleShape)
                            .pressScaleClickable { showAvatarSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "تغییرِ آدمک",
                            tint = AppPrimaryInk,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 13.dp)) {
                    Text(
                        if (savedName.isNullOrBlank()) "بی‌نام" else savedName!!,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Ltr {
                        Text(
                            toFa(phone ?: ""),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    // قرصِ طلایی مثلِ «اشتراکی»ِ کارتِ تنظیمات - متنِ طلاییِ تیره روی سبز خوانا نبود.
                    Text(
                        if (subscribed) "اشتراک فعال" else "حساب معمولی",
                        color = if (subscribed) AppGoldInk else Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (subscribed) AppGoldPillSoft else Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(AppRadius.icon)).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }

        // ── گروهِ مشخصات ─────────────────────────────────────────────────────
        // ⚠️ ردیفِ **ایمیل** پیاده نشد: نه اپ و نه سرور هیچ‌جا ایمیل نگه نمی‌دارن و بازیابی
        // فقط با شماره‌ی موبایله. فیلدی که هیچ‌جا استفاده نمی‌شه بدتر از نبودنشه.
        SettingsGroupLabel("مشخصات")
        SettingsGroup {
            SettingsRowItem(
                title = "نام",
                icon = Icons.Filled.Badge,
                tone = SettingsTone.NEUTRAL,
                status = if (savedName.isNullOrBlank()) "ثبت نشده" else savedName,
                onClick = { showNameSheet = true },
            )
            SettingsDivider()
            SettingsRowItem(
                title = "شماره‌ی موبایل",
                icon = Icons.Filled.PhoneIphone,
                tone = SettingsTone.BLUE,
                status = toFa(phone ?: ""),
                statusTone = StatusTone.HEALTHY,
                value = "تأییدشده",
            )
            // شماره‌ی کاربری برای پشتیبانی (پیام/هدیه‌ی اختصاصی). تپ = کپی.
            (userCode ?: userId?.toString())?.let { id ->
                SettingsDivider()
                SettingsRowItem(
                    title = "شماره‌ی کاربری",
                    icon = Icons.Filled.Tag,
                    tone = SettingsTone.NEUTRAL,
                    status = id,
                    value = "کپی",
                    onClick = {
                        idClipboard.setText(androidx.compose.ui.text.AnnotatedString(id.filter { it.isDigit() }))
                        banner.show("شماره‌ی کاربری کپی شد", isSuccess = true)
                    },
                )
            }
        }

        if (showAvatarSheet) {
            JibakAlertDialog(
                onDismissRequest = { showAvatarSheet = false },
                confirmButton = { TextButton(onClick = { showAvatarSheet = false }) { Text("تمام") } },
                title = { Text("آدمکت را انتخاب کن", fontWeight = FontWeight.Black) },
                text = { AvatarPicker(avatar = avatar, onChange = { avatarViewModel.save(it) }) },
            )
        }
        if (showNameSheet) {
            var nameDraft by remember(savedName) { mutableStateOf(savedName ?: "") }
            JibakAlertDialog(
                onDismissRequest = { showNameSheet = false },
                confirmButton = {
                    TextButton(
                        onClick = { authViewModel.updateName(nameDraft); showNameSheet = false },
                    ) {
                        Text("ذخیره")
                    }
                },
                dismissButton = { TextButton(onClick = { showNameSheet = false }) { Text("بی‌خیال") } },
                title = { Text("نام", fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                            value = nameDraft,
                            onValueChange = { if (it.length <= 30) nameDraft = it },
                            singleLine = true,
                            placeholder = { Text("مثلاً ابراهیم") },
                            modifier = Modifier.fillMaxWidth(), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                        Text(
                            "رو سربرگِ خروجیِ PDF و اکسل نوشته می‌شه. خالی گذاشتنش هیچ مشکلی نداره.",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                },
            )
        }
        AppCard(
            backgroundColor = AppPrimaryPill,
            borderColor = AppPrimaryBorder,
            modifier = Modifier.padding(top = AppSpacing.betweenCards),
            contentPadding = 16.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(if (subscribed) AppGoldPillSoft else AppSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = if (subscribed) AppGoldInk else AppMuted,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    // متنِ نوعِ اشتراک - رجوع کن به توضیحِ کاملِ همین منطق تو AuthViewModel/سرور:
                    // subscriptionTier فقط برای خریدِ واقعیِ زمان‌دار پر می‌شه.
                    val tierLabel = when (subscriptionTier) {
                        "1m" -> "اشتراک یک‌ماهه"
                        "3m" -> "اشتراک سه‌ماهه"
                        "6m" -> "اشتراک شش‌ماهه"
                        "1y" -> "اشتراک یک‌ساله"
                        else -> null
                    }
                    Text(
                        when {
                            !subscribed -> "نسخه‌ی عادی"
                            tierLabel != null -> tierLabel
                            else -> "مشترک"
                        },
                        color = AppText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                    )
                    // تاریخِ انقضا + شمارشِ روزِ باقی‌مونده (خواسته‌ی صریحِ کاربر، هم‌الگو با
                    // اپِ رفرنس: «تا ۱۳ شهریور ۱۴۰۵ (۲۸ روز دیگر)») - قبلاً فقط بجِ آزمایشی/دائمی بود.
                    val expiry = remember(subscribedUntil) { parseSubscribedUntil(subscribedUntil) }
                    if (subscribed && trialDaysLeft != null && trialDaysLeft in 1..7) {
                        Text(
                            "دوره‌ی آزمایشی رایگان: ${toFa(trialDaysLeft.toString())} روز مانده",
                            color = AppAccent,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else if (subscribed && expiry != null) {
                        Text(
                            "تا ${toFa(expiry.date.d)} ${persianMonthName(expiry.date.m)} ${toFa(expiry.date.y)} " +
                                "(${toFa(expiry.daysLeft)} روزِ دیگه)",
                            color = AppAccent,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else if (subscribed && subscribedUntil == null) {
                        Text("اشتراک دائمی", color = AppAccent, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.dp, AppPrimaryBorder, RoundedCornerShape(999.dp))
                    .pressScaleClickable(scale = 0.98f, onClick = onShowSubscription)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (subscribed) "مدیریت اشتراک" else "مشاهده پلن‌ها",
                    color = AppPrimaryInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(20.dp))
                }
            }
        }
        // ── خروج ─────────────────────────────────────────────────────────────
        // فاصله‌ی **دو برابرِ** فاصله‌ی معمولِ کارت‌ها - تنها جای برنامه که فاصله‌ی
        // غیرِتوکن مجازه، چون دکمه‌ی مخرب نباید تو ریتمِ عادیِ صفحه بشینه.
        var showLogoutConfirm by remember { mutableStateOf(false) }
        var showLogoutRisk by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.betweenCards * 2)
                .height(76.dp)
                .clip(RoundedCornerShape(AppRadius.card))
                .background(AppDangerPill)
                .border(AppStroke.card, AppDanger.copy(alpha = 0.35f), RoundedCornerShape(AppRadius.card))
                .pressScaleClickable { showLogoutConfirm = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppDanger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = AppDanger,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                "خروج از حساب",
                color = AppDanger,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(AppDanger.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppDanger, modifier = Modifier.size(18.dp))
            }
        }
        // الزامِ فروشگاه‌ها: راهِ داخل‌برنامه‌ای برای حذفِ کاملِ حساب. **ظاهرِ کم‌وزن،
        // مسیرِ سخت** - برعکسِ خروج که ظاهرِ پروزن و مسیرِ آسون داره.
        Text(
            "حذفِ کاملِ حساب کاربری",
            color = AppMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .pressScaleClickable { showDeleteAccountConfirm = true },
        )
        if (showLogoutRisk) {
            JibakAlertDialog(
                onDismissRequest = { showLogoutRisk = false },
                confirmButton = {
                    TextButton(onClick = { showLogoutRisk = false; authViewModel.logout(force = true) }) {
                        Text("باز هم خارج شو", color = AppDanger)
                    }
                },
                dismissButton = { TextButton(onClick = { showLogoutRisk = false }) { Text("بمانم") } },
                title = { Text("نسخه‌ی ابری به‌روز نشد", fontWeight = FontWeight.Black) },
                text = {
                    Text(
                        "نشد قبل از خروج از اطلاعاتت نسخه‌ی ابری بگیریم (اینترنت قطعه یا اشتراک نداری). " +
                            "با خروج، هرچی فقط روی این گوشیه پاک می‌شه. اگه مطمئن نیستی بمون و اول از " +
                            "«پشتیبان‌گیری» یه فایلِ پشتیبان بگیر.",
                        lineHeight = 21.sp,
                    )
                },
            )
        }
        if (showLogoutConfirm) {
            JibakAlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                confirmButton = {
                    TextButton(onClick = { showLogoutConfirm = false; authViewModel.logout(onBackupFailed = { showLogoutRisk = true }) }) {
                        Text("خروج", color = AppDanger)
                    }
                },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("بمانم") } },
                title = { Text("از حساب خارج می‌شوی؟", fontWeight = FontWeight.Black) },
                // جمله‌ی دوم لازمه: کاربری که برای رفعِ یه اشکال خارج می‌شه باید بدونه تا
                // ورودِ بعدی تراکنش‌هاش خودکار ثبت نمی‌شن.
                text = {
                    Text(
                        "داده‌هات رو سرور می‌مونه و با ورودِ دوباره برمی‌گرده. ثبتِ خودکارِ " +
                            "پیامک تا وقتی خارج باشی کار نمی‌کنه.",
                        lineHeight = 21.sp,
                    )
                },
            )
        }
    } else {
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Text("ورود به حساب انجام نشده", color = AppText, fontSize = 15.sp)
            Text(
                "برای همگام‌سازی ابری وارد شو",
                color = AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            GradientButton(onClick = onShowLoginPrompt, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("ورود")
            }
        }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedButton(onClick = onShowSubscription, modifier = Modifier.fillMaxWidth()) {
                Text("مشاهده پلن‌های اشتراک")
            }
        }
    }

    if (showDeleteAccountConfirm) {
        JibakAlertDialog(
            onDismissRequest = { if (!deleteAccountInProgress) showDeleteAccountConfirm = false },
            title = { Text("حذف حساب کاربری") },
            text = {
                Text(
                    "شماره‌ی حساب و وام‌ها/پشتیبان‌های ابری‌ای که سمت سرور ذخیره شدن برای همیشه " +
                        "پاک می‌شن و قابل بازگشت نیستن. داده‌های محلیِ همین گوشی (وام‌ها/چک‌های " +
                        "ذخیره‌شده) دست‌نخورده می‌مونه. مطمئنی؟",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteAccountInProgress = true
                        authViewModel.deleteAccount(
                            onSuccess = {
                                deleteAccountInProgress = false
                                showDeleteAccountConfirm = false
                                banner.show("حساب کاربری حذف شد")
                            },
                            onError = {
                                deleteAccountInProgress = false
                                banner.show("حذف حساب ناموفق بود؛ دوباره امتحان کن")
                            },
                        )
                    },
                    enabled = !deleteAccountInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = AppDanger),
                ) {
                    if (deleteAccountInProgress) {
                        LottieSpinner(modifier = Modifier.size(18.dp))
                    } else {
                        Text("حذف کن")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }, enabled = !deleteAccountInProgress) {
                    Text("انصراف")
                }
            },
        )
    }
}
