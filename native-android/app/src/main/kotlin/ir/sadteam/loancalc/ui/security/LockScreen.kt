package ir.sadteam.loancalc.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

/**
 * قفل امنیتی اپ - وقتی PIN تنظیم شده یا قفل اثر انگشت فعاله نشون داده می‌شه (رجوع کن به
 * [AppLockViewModel]). اگه بایومتریک فعاله، خودکار موقع نمایش این صفحه پرامپت باز می‌شه؛ در غیر
 * این صورت یا اگه بایومتریک لغو/ناموفق شد، ورود با PIN جایگزینه.
 */
@Composable
fun LockScreen(
    pinHash: String?,
    biometricEnabled: Boolean,
    attemptPin: (String) -> PinAttemptResult,
    onUnlock: () -> Unit,
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var lockedOutUntil by remember { mutableLongStateOf(0L) }
    var secondsLeft by remember { mutableStateOf(0L) }

    LaunchedEffect(lockedOutUntil) {
        while (lockedOutUntil > System.currentTimeMillis()) {
            secondsLeft = (lockedOutUntil - System.currentTimeMillis()) / 1000 + 1
            delay(1000)
        }
        secondsLeft = 0
    }

    fun tryBiometric() {
        val activity = context as? FragmentActivity ?: return
        showBiometricPrompt(
            activity = activity,
            onSuccess = onUnlock,
            onError = { message -> error = message },
        )
    }

    LaunchedEffect(Unit) {
        if (biometricEnabled) tryBiometric()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))
        Text(
            "قفل امنیتی",
            color = AppText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "برای دیدن وام‌هات هویتت رو تایید کن",
            color = AppMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp),
        )

        if (pinHash != null) {
            val lockedOut = secondsLeft > 0
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    val cleaned = cleanNum(it)
                    if (cleaned.length <= 8) {
                        pin = cleaned
                        error = null
                    }
                },
                enabled = !lockedOut,
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            GradientButton(
                onClick = {
                    when (val result = attemptPin(pin)) {
                        is PinAttemptResult.Success -> {
                            error = null
                            onUnlock()
                        }
                        is PinAttemptResult.WrongPin -> {
                            error = "PIN اشتباهه (${result.attemptsLeft} تلاش دیگه مونده)"
                        }
                        is PinAttemptResult.LockedOut -> {
                            error = "به دلیل تلاش‌های ناموفق زیاد، موقتاً قفل شدی"
                            lockedOutUntil = System.currentTimeMillis() + result.secondsLeft * 1000
                        }
                    }
                },
                enabled = !lockedOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(if (lockedOut) "امتحان دوباره بعد از ${secondsLeft} ثانیه" else "ورود")
            }
        }

        if (biometricEnabled) {
            OutlinedButton(
                onClick = { tryBiometric() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("ورود با اثر انگشت")
            }
        }

        error?.let {
            Text(it, color = AppDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp))
        }
    }
}
