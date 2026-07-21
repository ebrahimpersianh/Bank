package ir.sadteam.roozegar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.notif.DayNotification
import ir.sadteam.roozegar.prefs.Prefs
import ir.sadteam.roozegar.ui.calendar.CalendarScreen
import ir.sadteam.roozegar.ui.glass.AuroraGlassBackground
import ir.sadteam.roozegar.ui.onboarding.OnboardingScreen
import ir.sadteam.roozegar.ui.settings.SettingsScreen
import ir.sadteam.roozegar.ui.theme.RoozegarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoozegarTheme { App() }
        }
    }

    override fun onResume() {
        super.onResume()
        // برگشت به اپ = بهترین لحظه برای همگام‌کردن اعلان (مثلاً بعد از دادن مجوز اعلان تو آنبوردینگ)
        lifecycleScope.launch(Dispatchers.Default) {
            DayNotification.refresh(this@MainActivity)
        }
    }
}

private enum class Screen { Calendar, Settings }

@Composable
private fun App() {
    val context = LocalContext.current
    val settings by Prefs.flow(context).collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    // «امروز» به‌صورت state - با هر برگشت به اپ (ON_RESUME) دوباره حساب می‌شه که اگه نیمه‌شب رد شده،
    // کل UI (سربرگ، حلقه‌ی امروز، پیجر) خودش جابه‌جا بشه.
    var today by remember { mutableStateOf(JalaliCalendar.today()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) today = JalaliCalendar.today()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var screen by remember { mutableStateOf(Screen.Calendar) }

    when (settings?.onboardingDone) {
        null -> Box(Modifier.fillMaxSize()) { AuroraGlassBackground {} } // یه فریم تا لود DataStore
        false -> OnboardingScreen(
            onDone = {
                scope.launch {
                    Prefs.setOnboardingDone(context)
                    launch(Dispatchers.Default) { DayNotification.refresh(context) }
                }
            },
        )

        true -> {
            BackHandler(enabled = screen == Screen.Settings) { screen = Screen.Calendar }
            Crossfade(targetState = screen, label = "screen") { s ->
                when (s) {
                    Screen.Calendar -> CalendarScreen(
                        today = today,
                        effectsEnabled = settings?.effectsEnabled ?: true,
                        onOpenSettings = { screen = Screen.Settings },
                    )

                    Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Calendar })
                }
            }
        }
    }
}
