package ir.sadteam.loancalc.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private enum class LoginStep { PHONE, OTP }

private val requestOtpErrors = mapOf("too_soon" to "کمی صبر کن، کد قبلی هنوز معتبره")
private val verifyOtpErrors = mapOf(
    "wrong_code" to "کد اشتباهه",
    "code_expired" to "کد منقضی شده، دوباره درخواست بده",
    "too_many_attempts" to "تعداد تلاش‌ها بیش از حده، دوباره کد بگیر",
)

/** پورت مو‌به‌موی گیت ورود اجباری تو www/index.html (phoneLoginModal با کلاس mandatory):
 * شماره موبایل -> کد تایید پیامکی -> ورود، یا «ادامه به‌عنوان مهمان». */
@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    var step by remember { mutableStateOf(LoginStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("ورود با شماره موبایل", color = AppText, fontSize = 18.sp)
        Text(
            "برای ذخیره‌ی «وام‌های من» رو ابر و همگام‌سازی بین گوشی‌ها وارد شو",
            color = AppMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
        )

        if (step == LoginStep.PHONE) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = cleanNum(it) },
                placeholder = { Text("۰۹xxxxxxxxx") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        } else {
            Text(
                "کد تایید برای ${toFa(phone)} پیامک شد",
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

        Button(
            enabled = !loading,
            onClick = {
                error = null
                if (step == LoginStep.PHONE) {
                    if (phone.length != 11 || !phone.startsWith("09")) {
                        error = "شماره رو به‌صورت ۰۹xxxxxxxxx وارد کن"
                        return@Button
                    }
                    loading = true
                    viewModel.requestOtp(
                        phone = phone,
                        onSuccess = { loading = false; otp = ""; step = LoginStep.OTP },
                        onError = { code ->
                            loading = false
                            error = requestOtpErrors[code] ?: "ارسال کد ناموفق بود"
                        },
                    )
                } else {
                    if (otp.length != 5) {
                        error = "کد ۵ رقمی رو کامل وارد کن"
                        return@Button
                    }
                    loading = true
                    viewModel.verifyOtp(
                        phone = phone,
                        code = otp,
                        onSuccess = { loading = false },
                        onError = { code ->
                            loading = false
                            error = verifyOtpErrors[code] ?: "تایید ناموفق بود"
                        },
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(if (step == LoginStep.PHONE) "ارسال کد تایید" else "تایید و ورود")
        }

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
