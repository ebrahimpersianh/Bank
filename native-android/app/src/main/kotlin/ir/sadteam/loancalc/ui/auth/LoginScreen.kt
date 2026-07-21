package ir.sadteam.loancalc.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

private enum class LoginStep { PHONE, OTP }

private const val OTP_LENGTH = 5

// دقیقاً هم‌قدم با OTP_RESEND_COOLDOWN_MS سمتِ سرور (AuthRoutes.kt) - یعنی وقتی دکمه‌ی «ارسالِ
// مجدد» فعال می‌شه، سرور هم واقعاً درخواستِ جدید رو قبول می‌کنه (نه یه شمارش‌معکوسِ حدسی/جدا).
private const val RESEND_COOLDOWN_SECONDS = 60

private val requestOtpErrors = mapOf("too_soon" to "کمی صبر کن، کد قبلی هنوز معتبره")
private val verifyOtpErrors = mapOf(
    "wrong_code" to "کد اشتباهه",
    "code_expired" to "کد منقضی شده، دوباره درخواست بده",
    "too_many_attempts" to "تعداد تلاش‌ها بیش از حده، دوباره کد بگیر",
)

/**
 * پورت مو‌به‌موی گیت ورود اجباری تو www/index.html (phoneLoginModal با کلاس mandatory):
 * شماره موبایل -> کد تایید پیامکی -> ورود، یا «ادامه به‌عنوان مهمان».
 *
 * وقتی [onDismiss] پاس داده بشه (یعنی این دیگه گیت اجباری شروع اپ نیست، بلکه یه دعوت اختیاری به
 * ورود - مثلاً از رو محدودیت «۱ وام رایگان»)، دکمه‌ی «مهمان» جاش رو به یه دکمه‌ی بازگشت می‌ده (چون
 * از قبل مهمونه، دوباره پرسیدن معنی نداره) - دقیقاً معادل نسخه‌ی غیرمجبوریِ openPhoneLoginModal تو وب.
 *
 * **بازطراحیِ چیدمان** (خواسته‌ی صریحِ کاربر، با عکسِ رفرنس از یه اپِ دیگه): مرحله‌ی شماره‌موبایل
 * الان تویِ یه [AppCard] (شیشه‌ای، رجوع کن به همون کامپوننت) قرار داره - همون حسِ «کارتِ خوش‌آمد
 * روی صفحه» که رفرنس داشت. مرحله‌ی کد هم کاملاً بازطراحی شد: به‌جای یه `OutlinedTextField` ساده،
 * [OtpBoxRow] پنج تا باکسِ جداگانه نشون می‌ده (دقیقاً مثلِ رفرنس)، بالاش شماره‌ی واردشده با یه
 * آیکونِ مداد نشون داده می‌شه که با تپ‌کردنش برمی‌گردی به مرحله‌ی شماره برای ویرایش (به‌جای یه دکمه‌ی
 * برگشتِ جدا)، و پایینش یه دکمه‌ی «ارسالِ مجدد» با شمارش‌معکوس (کنارِ دکمه‌ی اصلیِ تایید) اضافه شده -
 * قبلاً اصلاً راهی برای ارسالِ دوباره‌ی کد نبود.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onDismiss: (() -> Unit)? = null,
    onLoginSuccess: (() -> Unit)? = null,
) {
    var step by remember { mutableStateOf(LoginStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var resendSecondsLeft by remember { mutableIntStateOf(0) }
    // هر بار کد (اول یا دوباره) با موفقیت درخواست بشه یکی زیاد می‌شه - کلیدِ LaunchedEffectِ
    // شمارش‌معکوسِ ارسالِ مجدد پایین‌تر، تا هم اولین ارسال هم هر ارسالِ مجددی از نو ۶۰ثانیه بشمره.
    var otpSentTick by remember { mutableIntStateOf(0) }
    val syncConflict by viewModel.syncConflict.collectAsState()

    if (syncConflict != null) {
        SyncConflictPrompt(
            onKeepCloud = { viewModel.resolveSyncConflict(useServer = true) { onLoginSuccess?.invoke() } },
            onKeepDevice = { viewModel.resolveSyncConflict(useServer = false) { onLoginSuccess?.invoke() } },
        )
        return
    }

    // شماره‌ی کامل همیشه به فرم ۰۹xxxxxxxxx (فرمتی که سرور/پیامک انتظار داره) نگه داشته می‌شه؛ فقط
    // نمایش عوض شده - کاربر پیشوندِ ثابتِ +۹۸ رو می‌بینه و فقط ۱۰ رقمِ بعدش (که با ۹ شروع می‌شه) رو
    // تایپ می‌کنه، دقیقاً مثل اپ‌های ایرانیِ مشابه.
    val fullPhone = "0$phone"

    fun sendOtp() {
        if (phone.length != 10 || !phone.startsWith("9")) {
            error = "شماره رو به‌صورت ۹xxxxxxxxx (بعد از +۹۸) وارد کن"
            return
        }
        error = null
        loading = true
        viewModel.requestOtp(
            phone = fullPhone,
            onSuccess = {
                loading = false
                otp = ""
                step = LoginStep.OTP
                otpSentTick++
            },
            onError = { code ->
                loading = false
                error = requestOtpErrors[code] ?: "ارسال کد ناموفق بود"
            },
        )
    }

    fun verify() {
        if (otp.length != OTP_LENGTH || loading) return
        error = null
        loading = true
        viewModel.verifyOtp(
            phone = fullPhone,
            code = otp,
            onSuccess = {
                loading = false
                // اگه تعارض سینک پیش اومده باشه، viewModel.syncConflict همین الان پر شده و
                // recomposition بالای این تابع خودش UI تعارض رو نشون می‌ده؛ onLoginSuccess اونجا
                // (بعد از تصمیم کاربر) صدا زده می‌شه، نه اینجا.
                if (viewModel.syncConflict.value == null) {
                    onLoginSuccess?.invoke()
                }
            },
            onError = { code ->
                loading = false
                error = verifyOtpErrors[code] ?: "تایید ناموفق بود"
            },
        )
    }

    // شمارش‌معکوسِ ارسالِ مجدد - با تغییرِ otpSentTick (هم اولین ارسال هم هر ارسالِ مجددی) از نو
    // شروع می‌شه.
    LaunchedEffect(otpSentTick) {
        if (otpSentTick > 0) {
            var left = RESEND_COOLDOWN_SECONDS
            resendSecondsLeft = left
            while (left > 0) {
                delay(1000)
                left--
                resendSecondsLeft = left
            }
        }
    }

    // پس‌زمینه‌ی صریح رو تمِ فعلی (قبلاً نداشت، پس رنگِ زمینه‌ی خودِ ویندو - که تیره‌ست - از زیرش رد
    // می‌شد و صفحه‌ی ورود همیشه مشکی دیده می‌شد، حتی تو تمِ روشن).
    // statusBarsPadding: قبلاً نبود، پس فلشِ بازگشت (align TopStart) دقیقاً زیرِ نوارِ وضعیت/آنتن
    // گوشی می‌رفت (خواسته‌ی کاربر: «فلش عقب می‌ره تو آنتن»، «بالای صفحه رو بیار پایین‌تر») - چون
    // MainActivity با enableEdgeToEdge محتوا رو زیرِ نوارِ وضعیت هم می‌کشه، بدونِ این پدینگ صریح.
    Box(modifier = Modifier.fillMaxSize().background(AppSurface).statusBarsPadding()) {
        if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (step == LoginStep.PHONE) {
                Text("به «وام من» خوش اومدی!", color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(
                    "برای ذخیره‌ی «وام‌های من» در سرور ابری و همگام‌سازی بین گوشی‌ها، شماره‌موبایلت رو وارد کن",
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                )

                AppCard {
                    val focusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    // کیبرد عددی خودکار باز می‌شه (کاربر: «خودکار کیبرد بیاد رو عدد») - بدون تپِ دستی.
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                    // تو RTL، اولین چیزِ توی Row سمتِ راست میاد؛ برای این‌که «+۹۸» سمتِ چپ باشه
                    // (خواسته‌ی کاربر)، فیلدِ شماره باید اول تو کد بیاد، بعد چیپِ +۹۸.
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { raw ->
                                val cleaned = cleanNum(raw).removePrefix("0")
                                phone = cleaned.take(10)
                            },
                            placeholder = { Text("۹xxxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .focusRequester(focusRequester),
                            singleLine = true,
                        )
                        Box(
                            modifier = Modifier
                                .background(AppSurface2, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 16.dp),
                        ) {
                            Text("+۹۸", color = AppText, fontSize = 15.sp)
                        }
                    }

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = AppDanger,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    GradientButton(
                        enabled = !loading,
                        onClick = { sendOtp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    ) {
                        if (loading) {
                            LottieSpinner(modifier = Modifier.size(18.dp))
                        } else {
                            Text("ارسال کد تایید")
                        }
                    }
                }

                if (onDismiss == null) {
                    OutlinedButton(
                        onClick = { viewModel.continueAsGuest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    ) {
                        Text("ادامه به‌عنوان مهمان")
                    }
                }
            } else {
                // پرکردنِ خودکارِ کد از رو پیامک، کاملاً بی‌صدا (بدونِ هیچ دیالوگ/مجوزی) - رجوع کن
                // به SmsOtpAutofill.kt.
                SmsRetrieverEffect(
                    active = true,
                    onCodeReceived = { code ->
                        otp = code
                        verify()
                    },
                )

                // شماره‌ی واردشده + آیکونِ مداد برای برگشتن و ویرایشش - جایگزینِ یه دکمه‌ی برگشتِ
                // جدا (دقیقاً حسِ رفرنس). تو RTL اولین چیزِ توی Row سمتِ راست میاد؛ برای این‌که
                // آیکون سمتِ چپِ شماره باشه (مثلِ رفرنس)، متن باید اول تو کد بیاد، بعد آیکون.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { step = LoginStep.PHONE; error = null }
                        .padding(bottom = 6.dp),
                ) {
                    Text(
                        toFa(fullPhone),
                        color = AppText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایشِ شماره", tint = AppPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    "کدِ تاییدی که پیامک شد رو وارد کن",
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(bottom = 18.dp),
                )

                OtpBoxRow(
                    value = otp,
                    onValueChange = { new ->
                        otp = new
                        if (new.length == OTP_LENGTH) verify()
                    },
                    length = OTP_LENGTH,
                )

                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = AppDanger,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GradientButton(
                        enabled = !loading && otp.length == OTP_LENGTH,
                        onClick = { verify() },
                        modifier = Modifier.weight(1f),
                    ) {
                        if (loading) {
                            LottieSpinner(modifier = Modifier.size(18.dp))
                        } else {
                            Text("تایید و ورود")
                        }
                    }
                    OutlinedButton(
                        enabled = resendSecondsLeft == 0 && !loading,
                        onClick = { sendOtp() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (resendSecondsLeft > 0) formatCountdown(resendSecondsLeft) else "ارسال مجدد")
                    }
                }
            }
        }
    }
}

private fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return toFa("$minutes:${seconds.toString().padStart(2, '0')}")
}

/**
 * ورودیِ کدِ تایید به‌صورتِ [length] باکسِ جداگانه (دقیقاً مثلِ رفرنسِ کاربر) - یه `BasicTextField`
 * نامرئی زیرِ کاره که فوکوس/کیبرد/مکان‌نما رو مدیریت می‌کنه، `decorationBox`ش به‌جای متنِ خطی، همون
 * تعداد باکس رو رسم می‌کنه. تو RTL (که کلِ اپ باهاشه)، باکسِ اول (اولین رقمِ تایپ‌شده) خودش‌به‌خود
 * سمتِ راست می‌شینه - نیازی به معکوس‌کردنِ دستی نیست.
 */
@Composable
private fun OtpBoxRow(value: String, onValueChange: (String) -> Unit, length: Int) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(cleanNum(raw).take(length)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.focusRequester(focusRequester),
        // خودِ innerTextField نامرئی (اندازه‌ی صفر) نگه داشته می‌شه - فقط برای این‌که کرسر/IMEِ
        // واقعی جایی تو درختِ کامپوز داشته باشه؛ چیزی که کاربر واقعاً می‌بینه همون Rowِ باکس‌هاست.
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.size(0.dp)) { innerTextField() }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusRequester.requestFocus() },
            ) {
                repeat(length) { index ->
                    val digit = value.getOrNull(index)?.toString()
                    val filled = digit != null
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (filled) AppSurface2 else AppSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, if (filled) AppPrimary else AppLine, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(toFa(digit ?: ""), color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    )
}

/** پورت openConfirmModal تو syncAfterLogin: هم گوشی هم سرور «وام‌های من» دارن و فرق می‌کنن،
 * کاربر باید انتخاب کنه کدوم بمونه. */
@Composable
private fun SyncConflictPrompt(onKeepCloud: () -> Unit, onKeepDevice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "یه نسخه‌ی دیگه از «وام‌های من» تو فضای ابری ذخیره شده. می‌خوای همون نسخه جایگزین اطلاعات این گوشی بشه؟",
            color = AppText,
            fontSize = 14.sp,
        )
        GradientButton(
            onClick = onKeepCloud,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            Text("بله، نسخه‌ی ابری رو بیار")
        }
        OutlinedButton(
            onClick = onKeepDevice,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            Text("نه، همین گوشی بمونه")
        }
    }
}
