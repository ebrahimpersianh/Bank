package ir.sadteam.loancalc.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ویبره‌ی واقعی (نه فقط هپتیک ظریفِ `TextHandleMove` که Compose می‌ده و رو خیلی گوشی‌ها اصلاً حس
 * نمی‌شه) - خواسته‌ی کاربر «برنامه ویبره نداره». طبق خواسته‌ی صریح کاربر این یه ویژگیِ اشتراکیه:
 * روشن/خاموش‌کردنش تو تنظیمات فقط برای کاربر مشترک در دسترسه (رجوع کن به SettingsScreen).
 */
@HiltViewModel
class HapticsViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setEnabled(value: Boolean) {
        viewModelScope.launch { uiPrefs.setVibrationEnabled(value) }
    }
}

private fun vibrate(context: Context, durationMs: Long) {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() == true) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * یه لرزشِ کوتاه و واقعی برمی‌گردونه که هر جای اپ می‌شه صداش زد - فقط اگه کاربر مشترک باشه و تو
 * تنظیمات روشنش کرده باشه، وگرنه کاری نمی‌کنه (کاربر عادی/مهمان اصلاً ویبره نمی‌بینه).
 */
@Composable
fun rememberBuzz(durationMs: Long = 16): () -> Unit {
    val context = LocalContext.current
    val hapticsViewModel: HapticsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val vibrationEnabled by hapticsViewModel.enabled.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    return {
        if (subscribed && vibrationEnabled) vibrate(context, durationMs)
    }
}
