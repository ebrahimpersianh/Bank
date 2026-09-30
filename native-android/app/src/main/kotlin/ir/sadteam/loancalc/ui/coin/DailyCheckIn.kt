package ir.sadteam.loancalc.ui.coin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface2
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DailyCheckInViewModel @Inject constructor(
    private val gamification: GamificationRepository,
) : ViewModel() {
    private val _result = MutableStateFlow<GamificationRepository.DailyOpen?>(null)
    val result: StateFlow<GamificationRepository.DailyOpen?> = _result

    init {
        viewModelScope.launch { _result.value = runCatching { gamification.claimDailyOpen() }.getOrNull() }
    }

    fun dismiss() { _result.value = null }
}

/**
 * 🪙 جشنِ سر زدنِ روزانه (۸ مهر، خواسته‌ی کاربر): روزی یک‌بار، هفت سکه در یک ردیف و سکه‌ی
 * امروز با فنر پر می‌شود؛ روزِ هفتم جایزه‌ی بزرگ‌تر. سکه همان لحظه در دفتر ثبت شده است.
 */
@Composable
fun DailyCheckInHost(viewModel: DailyCheckInViewModel = hiltViewModel()) {
    val r by viewModel.result.collectAsState()
    val res = r ?: return
    JibakAlertDialog(
        onDismissRequest = viewModel::dismiss,
        title = {
            Text(
                if (res.dayInWeek == 7) "هفت روزِ کامل! 🎉" else "خوش برگشتی!",
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    (1..7).forEach { d -> DaySlot(d, res.dayInWeek, Modifier.weight(1f)) }
                }
                Text(
                    "+${toFa(res.coins)} سکه",
                    color = AppPrimaryInk,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    if (res.dayInWeek == 7) "جایزه‌ی هفته هم گرفتی. از فردا هفته‌ی تازه شروع می‌شود."
                    else "روزِ ${toFa(res.dayInWeek)} از ۷ · ${toFa(7 - res.dayInWeek)} روزِ دیگر تا ${toFa(GamificationRepository.Reward.DAILY_OPEN_WEEK)} سکه‌ی جایزه",
                    color = AppMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            GradientButton(onClick = viewModel::dismiss, modifier = Modifier.fillMaxWidth()) { Text("گرفتم") }
        },
    )
}

@Composable
private fun DaySlot(day: Int, today: Int, modifier: Modifier) {
    val filled = day <= today
    val isToday = day == today
    val pop = remember { Animatable(if (isToday) 0f else 1f) }
    LaunchedEffect(isToday) {
        if (isToday) {
            delay(250)
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (filled) AppPrimary.copy(alpha = 0.14f) else AppSurface2)
                .border(if (isToday) 2.dp else 1.dp, if (isToday) AppPrimary else AppLine, CircleShape),
        ) {
            if (filled) {
                Box(Modifier.scale(if (isToday) pop.value else 1f)) {
                    CoinIcon(size = if (day == 7) 24.dp else 20.dp)
                }
            } else {
                Text(if (day == 7) "🎁" else toFa(day), color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            if (day == 7) "جایزه" else "روز ${toFa(day)}",
            color = if (isToday) AppPrimaryInk else AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
