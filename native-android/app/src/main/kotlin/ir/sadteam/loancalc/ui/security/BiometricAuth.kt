package ir.sadteam.loancalc.ui.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

/** چک می‌کنه گوشی اصلاً سخت‌افزار/داده‌ی بایومتریک ثبت‌شده داره یا نه - اگه نداشته باشه، سوییچ
 * «قفل با اثر انگشت» تو تنظیمات اصلاً فعال نمی‌شه. */
fun biometricAvailable(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/** پورت BiometricPrompt استاندارد اندروید - نیاز به FragmentActivity داره (نه ComponentActivity
 * ساده)، برای همین MainActivity به FragmentActivity تغییر کرد. */
fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("ورود با اثر انگشت")
        .setSubtitle("برای ادامه هویتت رو تایید کن")
        .setNegativeButtonText("لغو")
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("تایید هویت ناموفق بود")
            }
        },
    )
    prompt.authenticate(promptInfo)
}
