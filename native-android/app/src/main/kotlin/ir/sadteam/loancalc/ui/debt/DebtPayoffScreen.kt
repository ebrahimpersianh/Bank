package ir.sadteam.loancalc.ui.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** یک بدهی برای برنامه‌ی تسویه: مانده، حداقلِ ماهانه (قسط) و نرخِ سالانه (برای «گران‌ترین اول»). */
data class PayoffDebt(val name: String, val balance: Double, val minPayment: Double, val ratePct: Double)

data class PayoffResult(val months: Int, val order: List<Pair<String, Int>>)

/**
 * شبیه‌سازیِ ماه‌به‌ماه: هر بدهی حداقلش را می‌گیرد؛ مبلغِ اضافه + قسطِ بدهی‌های تمام‌شده
 * (گلوله‌برفی) به اولین بدهیِ ترتیب می‌رود. سودِ آینده حساب نمی‌شود چون مانده‌ی وام همان جمعِ
 * قسط‌های باقی است (سودش از قبل داخلش است) - پس خروجی «چند ماه زودتر» است، نه «چقدر سود کمتر».
 */
fun simulatePayoff(all: List<PayoffDebt>, extra: Double, avalanche: Boolean): PayoffResult {
    // بدونِ مبلغِ اضافه، بدهیِ بی‌قسط (به افراد) هیچ‌وقت تمام نمی‌شود - از شبیه‌سازی بیرون.
    val debts = if (extra > 0) all else all.filter { it.minPayment > 0 }
    val sorted = if (avalanche) debts.sortedWith(compareByDescending<PayoffDebt> { it.ratePct }.thenBy { it.balance })
    else debts.sortedBy { it.balance }
    val left = sorted.map { it.balance }.toMutableList()
    val doneAt = IntArray(sorted.size)
    var month = 0
    while (left.any { it > 0.5 } && month < 600) {
        month++
        var pool = extra
        sorted.forEachIndexed { i, d ->
            if (left[i] <= 0.5) pool += d.minPayment // قسطِ آزادشده
        }
        sorted.forEachIndexed { i, d ->
            if (left[i] > 0.5) {
                val pay = minOf(d.minPayment, left[i])
                left[i] -= pay
                pool += d.minPayment - pay
            }
        }
        for (i in sorted.indices) {
            if (pool <= 0) break
            if (left[i] > 0.5) {
                val pay = minOf(pool, left[i])
                left[i] -= pay
                pool -= pay
            }
        }
        sorted.indices.forEach { if (left[it] <= 0.5 && doneAt[it] == 0) doneAt[it] = month }
    }
    return PayoffResult(month, sorted.mapIndexed { i, d -> d.name to doneAt[i] })
}

@HiltViewModel
class DebtPayoffViewModel @Inject constructor(
    private val loans: LoanRepository,
    debts: DebtRepository,
) : ViewModel() {
    val items: StateFlow<List<PayoffDebt>> = combine(
        loans.observeLoans(),
        debts.observeDebts(),
        debts.observeCounterparties(),
    ) { ls, ds, cps ->
        val fromLoans = ls.filter { it.paidCount < it.n && it.installment > 0 }.map {
            PayoffDebt(it.name.ifBlank { it.bank }, it.installment * (it.n - it.paidCount), it.installment, runCatching { loans.getRatePct(it) }.getOrDefault(0.0))
        }
        val fromPeople = ds.filter { !it.settled }.groupBy { it.counterpartyId }.mapNotNull { (cp, rows) ->
            val owe = -rows.sumOf { if (it.type == DebtType.OWED_TO_ME.name) it.amount else -it.amount }
            if (owe <= 0) null else PayoffDebt("بدهی به ${cps.firstOrNull { it.id == cp }?.name ?: "طرفِ حساب"}", owe, 0.0, 0.0)
        }
        fromLoans + fromPeople
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** 🏁 **برنامه‌ی تسویه‌ی بدهی‌ها** (۸ مهر، خواسته‌ی کاربر). */
@Composable
fun DebtPayoffScreen(onBack: () -> Unit, vm: DebtPayoffViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState()
    val privacy = LocalPrivacyMode.current
    var extraRaw by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    // فیلد تومان است (قاعده‌ی برنامه)، محاسبه ریال.
    val extra = (extraRaw.toDoubleOrNull() ?: 0.0) * 10
    val base = remember(items) { simulatePayoff(items, 0.0, false) }
    val plan = remember(items, extra, mode) { simulatePayoff(items, extra, mode == 1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText) }
                Text("برنامه‌ی تسویه‌ی بدهی‌ها", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
        if (items.isEmpty()) {
            item {
                AppCard {
                    Text("بدهیِ فعالی نداری", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text("وام‌های در حالِ پرداخت و بدهی‌هایت به دیگران این‌جا جمع می‌شوند.", color = AppMuted, fontSize = 12.5.sp)
                }
            }
            return@LazyColumn
        }
        item {
            AppCard {
                Text("کلِ بدهی", color = AppMuted, fontSize = 12.sp)
                Text(maskIfPrivate(privacy, items.sumOf { it.balance }.rialToFaCompact()) + " تومان", color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("قسطِ ماهانه‌ی فعلی ${maskIfPrivate(privacy, items.sumOf { it.minPayment }.rialToFaCompact())} تومان · بدونِ مبلغِ اضافه ${toFa(base.months)} ماه تا تسویه", color = AppMuted, fontSize = 12.sp)
            }
        }
        item {
            AppCard {
                Text("هر ماه چقدر بیشتر می‌توانی بدهی؟", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = extraRaw,
                    onValueChange = { extraRaw = cleanNum(it).take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppFieldShape,
                    colors = appFieldColors(),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("تومان") },
                    placeholder = { Text("مثلاً ۲٬۰۰۰٬۰۰۰") },
                )
                if (extra > 0) Text(numberToWordsFa(extra / 10) + " تومان", color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(10.dp))
                SegmentedToggle(options = listOf("کوچک‌ترین اول", "گران‌ترین اول"), selectedIndex = mode, onSelect = { mode = it })
                Text(
                    if (mode == 0) "بدهیِ کوچک زود تمام می‌شود و انگیزه می‌دهد؛ قسطش آزاد می‌شود و می‌رود سراغِ بعدی."
                    else "اول وامی که سودش بیشتر است - از نظرِ پولی به‌صرفه‌تر.",
                    color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Flag, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${toFa(plan.months)} ماه تا بی‌بدهی شدن", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
                val saved = base.months - plan.months
                if (saved > 0) Text("${toFa(saved)} ماه زودتر از حالتِ فعلی", color = AppPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                plan.order.forEachIndexed { i, (name, month) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(AppSurface2), contentAlignment = Alignment.Center) {
                            Text(toFa(i + 1), color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(name, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(if (month > 0) "ماهِ ${toFa(month)}" else "با مبلغِ اضافه", color = AppMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppSurface2).padding(10.dp)) {
                    Text("مبلغِ اضافه را به بانکِ همان وام بده و بگو «از اقساطِ آخر کم شود». بدهی به افراد قسط ندارد و فقط با مبلغِ اضافه تسویه می‌شود.", color = AppMuted, fontSize = 11.5.sp)
                }
            }
        }
    }
}
