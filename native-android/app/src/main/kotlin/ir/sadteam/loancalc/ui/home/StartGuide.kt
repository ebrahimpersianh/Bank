package ir.sadteam.loancalc.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * «چهار قدمِ شروع» بالای خانه (۱۳ مهر، کاربر: «برنامه پیچیده است»). هر قدم وقتی واقعاً انجام شد
 * خودش تیک می‌خورد؛ وقتی هر چهار تا تمام شد یا کاربر بستش، دیگر نشان داده نمی‌شود.
 */
data class StartGuideState(
    val hasAccount: Boolean = false,
    val hasTransaction: Boolean = false,
    val hasLoan: Boolean = false,
    val bankLinked: Boolean = false,
    val dismissed: Boolean = true,
) {
    val doneCount get() = listOf(hasAccount, hasTransaction, hasLoan, bankLinked).count { it }
    val visible get() = !dismissed && doneCount < 4
}

@HiltViewModel
class StartGuideViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    accountRepository: AccountRepository,
    loanRepository: LoanRepository,
) : ViewModel() {
    private val bankLinked = combine(uiPrefs.smsAutoImportEnabled, uiPrefs.notifAutoImportEnabled) { a, b -> a || b }

    val state: StateFlow<StartGuideState> = combine(
        accountRepository.observeAccounts(),
        accountRepository.observeTransactions(),
        loanRepository.observeLoans(),
        bankLinked,
        // فقط برای کاربرِ تازه (حالتِ ساده روشن) - کاربرِ قدیمی با سه قدمِ انجام‌شده آزار نبیند.
        combine(uiPrefs.startGuideDismissed, uiPrefs.simpleMode) { d, simple -> d || simple != true },
    ) { accounts, txs, loans, linked, dismissed ->
        StartGuideState(accounts.isNotEmpty(), txs.isNotEmpty(), loans.isNotEmpty(), linked, dismissed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StartGuideState())

    fun dismiss() {
        ir.sadteam.loancalc.data.UsageStats.action("start_guide_dismissed")
        viewModelScope.launch { uiPrefs.setStartGuideDismissed(true) }
    }
}

@Composable
fun StartGuideCard(
    state: StartGuideState,
    onAddAccount: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddLoan: () -> Unit,
    onLinkBank: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppCard { Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("چهار قدم تا شروع", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(
                    "${toFa(state.doneCount)} از ۴ انجام شده",
                    color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                )
            }
            Box(
                Modifier.size(44.dp).pressScaleClickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted, modifier = Modifier.size(20.dp))
            }
        }
        LinearProgressIndicator(
            progress = { state.doneCount / 4f },
            color = AppPrimary,
            trackColor = AppLine,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp).clip(CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            GuideStep(1, "یک حساب بساز", "کارتِ بانکی یا کیفِ پولِ نقدی", state.hasAccount, onAddAccount)
            GuideStep(2, "اولین خرج یا درآمدت را ثبت کن", "با دکمه‌ی + پایینِ صفحه", state.hasTransaction, onAddTransaction)
            GuideStep(3, "وامت را اضافه کن", "تا سررسیدِ قسط‌ها یادت بیاید", state.hasLoan, onAddLoan)
            GuideStep(4, "پیامک یا اعلانِ بانک را وصل کن", "تا خرج‌ها خودشان ثبت شوند", state.bankLinked, onLinkBank)
        }
    } }
}

@Composable
private fun GuideStep(n: Int, title: String, sub: String, done: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .pressScaleClickable(scale = 0.98f, onClick = { if (!done) onClick() }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) AppPrimary else AppLine),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = "انجام شد", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(17.dp))
            } else {
                Text(toFa(n), color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                title,
                color = if (done) AppMuted else AppText,
                fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold,
                textDecoration = if (done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            )
            if (!done) Text(sub, color = AppMuted, fontSize = 11.sp)
        }
        if (!done) Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(20.dp))
    }
}
