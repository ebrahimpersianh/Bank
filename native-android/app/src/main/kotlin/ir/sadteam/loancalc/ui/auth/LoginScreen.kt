package ir.sadteam.loancalc.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

private enum class LoginStep { PHONE, OTP }

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
    val syncConflict by viewModel.syncConflict.collectAsState()

    if (syncConflict != null) {
        SyncConflictPrompt(
            onKeepCloud = { viewModel.resolveSyncConflict(useServer = true) { onLoginSuccess?.invoke() } },
            onKeepDevice = { viewModel.resolveSyncConflict(useServer = false) { onLoginSuccess?.invoke() } },
        )
        return
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
            Text("ورود با شماره موبایل", color = AppText, fontSize = 18.sp)
            Text(
                "برای ذخیره‌ی «وام‌های من» در سرور ابری و همگام‌سازی بین گوشی‌ها وارد شو",
                color = AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )

            // شماره‌ی کامل همیشه به فرم ۰۹xxxxxxxxx (فرمتی که سرور/پیامک انتظار داره) نگه داشته
            // می‌شه؛ فقط نمایش عوض شده - کاربر پیشوندِ ثابتِ +۹۸ رو می‌بینه و فقط ۱۰ رقمِ بعدش
            // (که با ۹ شروع می‌شه) رو تایپ می‌کنه، دقیقاً مثل اپ‌های ایرانیِ مشابه.
            val fullPhone = "0$phone"
            if (step == LoginStep.PHONE) {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                // کیبرد عددی خودکار باز می‌شه (کاربر: «خودکار کیبرد بیاد رو عدد») - بدون تپِ دستی.
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                // تو RTL، اولین چیزِ توی Row سمتِ راست میاد؛ برای این‌که «+۹۸» سمتِ چپ باشه (خواسته‌ی
                // کاربر)، فیلدِ شماره باید اول تو کد بیاد، بعد چیپِ +۹۸.
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
                            .background(AppSurface2, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                    ) {
                        Text("+۹۸", color = AppText, fontSize = 15.sp)
                    }
                }
            } else {
                // پرکردنِ خودکارِ کد از رو پیامک (بدونِ مجوزِ خواندنِ پیامک، فقط با تاییدِ دستیِ
                // کاربر رو دیالوگِ سیستمی) - رجوع کن به SmsOtpAutofill.kt.
                SmsUserConsentEffect(active = true, onCodeReceived = { code -> otp = code })
                Text(
                    "کد تایید برای ${toFa(fullPhone)} پیامک شد",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = cleanNum(it).take(5) },
                    placeholder = { Text("کد ۵ رقمی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
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
                onClick = {
                    error = null
                    if (step == LoginStep.PHONE) {
                        if (phone.length != 10 || !phone.startsWith("9")) {
                            error = "شماره رو به‌صورت ۹xxxxxxxxx (بعد از +۹۸) وارد کن"
                            return@GradientButton
                        }
                        loading = true
                        viewModel.requestOtp(
                            phone = fullPhone,
                            onSuccess = { loading = false; otp = ""; step = LoginStep.OTP },
                            onError = { code ->
                                loading = false
                                error = requestOtpErrors[code] ?: "ارسال کد ناموفق بود"
                            },
                        )
                    } else {
                        if (otp.length != 5) {
                            error = "کد ۵ رقمی رو کامل وارد کن"
                            return@GradientButton
                        }
                        loading = true
                        viewModel.verifyOtp(
                            phone = fullPhone,
                            code = otp,
                            onSuccess = {
                                loading = false
                                // اگه تعارض سینک پیش اومده باشه، viewModel.syncConflict همین الان
                                // پر شده و recomposition بالای این تابع خودش UI تعارض رو نشون می‌ده؛
                                // onLoginSuccess اونجا (بعد از تصمیم کاربر) صدا زده می‌شه، نه اینجا.
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                // قبلاً موقعِ loading هیچ نشونه‌ی بصری‌ای نبود (فقط enabled=false، بدون اسپینر)،
                // برای همین تاخیرِ چندثانیه‌ایِ درخواستِ شبکه (request-otp/verify-otp) حسِ هنگ‌کردن
                // می‌داد - همون الگوی LottieSpinner که تو SubscriptionScreen/حذفِ حساب هست، اینجا هم.
                if (loading) {
                    LottieSpinner(modifier = Modifier.size(18.dp))
                } else {
                    Text(if (step == LoginStep.PHONE) "ارسال کد تایید" else "تایید و ورود")
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
        }
    }
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
