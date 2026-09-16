package ir.sadteam.loancalc.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.categoryIconChoices
import ir.sadteam.loancalc.data.db.SavingsGoalEntity
import ir.sadteam.loancalc.data.iconForKey
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **هدفِ پس‌انداز** - تنها نشانِ برنامه که تا امروز راهِ گرفتن نداشت (`goal_reached` با
 * برچسبِ «به‌زودی») حالا مقصد دارد.
 *
 * 🚨 قاعده‌ی مرکزی: **هدف پول جابه‌جا نمی‌کند.** واریز به هدف هیچ تراکنشی در حساب‌کتاب‌ها
 * نمی‌سازد و از موجودی کم نمی‌کند - یک برچسب روی پولی است که از قبل داری. اگر واریزِ هدف
 * تراکنش می‌ساخت، همان «دوباره‌حسابی»ای می‌شد که یک‌بار موجودیِ منفیِ ۵ میلیاردی ساخت.
 * متنِ زیرِ هیرو همین را به کاربر می‌گوید، چون فرضِ طبیعیِ کاربر خلافِ این است.
 *
 * ⚠️ این صفحه `LazyColumn` دارد پس **هرگز داخلِ `SettingsSubPageScaffold` نرود** (قاعده‌ی
 * کرشِ اسکرولِ تودرتو).
 */
@Composable
fun SavingsGoalScreen(
    onBack: () -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel(),
) {
    val goals by viewModel.goals.collectAsState()
    val justReached by viewModel.justReached.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    var contributeTo by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val totalTarget = goals.sumOf { it.targetRial }
    val totalSaved = goals.sumOf { it.savedRial }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "هدف‌های پس‌انداز",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (goals.isNotEmpty()) {
                item {
                    AppHeroCard {
                        Text("جمعِ پس‌اندازت", color = HeroMuted, fontSize = 11.sp)
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                "${maskIfPrivate(masked, amountToman(totalSaved))} تومان",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Text(
                            "از ${amountToman(totalTarget)} تومانِ ${toFa(goals.size)} هدف",
                            color = HeroMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                item {
                    // فرضِ طبیعیِ کاربر این است که واریزِ هدف از حسابش کم می‌کند. نمی‌کند -
                    // و چیزی که کاربر اشتباه فرض می‌کند باید نوشته شود، نه حدس زده.
                    Text(
                        "این‌جا فقط نشانه‌گذاری می‌کنی؛ پولی از حساب‌کتاب‌هایت کم یا زیاد نمی‌شود.",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            if (showAddForm) {
                item {
                    AddGoalForm(
                        onCancel = { showAddForm = false },
                        onSubmit = { title, targetRial, iconKey ->
                            viewModel.addGoal(title, targetRial, iconKey)
                            showAddForm = false
                        },
                    )
                }
            } else {
                item {
                    GradientButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("هدفِ تازه") }
                }
            }

            if (goals.isEmpty() && !showAddForm) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Savings,
                        title = "هنوز هدفی نداری",
                        description = "یک هدف بساز - مثلِ «سفر» یا «گوشیِ نو» - و هر بار که " +
                            "پولی کنار گذاشتی همین‌جا ثبتش کن.",
                    )
                }
            }

            items(goals, key = { it.id }) { goal ->
                GoalRow(
                    goal = goal,
                    privacyMode = privacyMode,
                    onContribute = { contributeTo = goal },
                    onDelete = { pendingDelete = goal },
                )
            }
        }
    }

    contributeTo?.let { goal ->
        ContributeDialog(
            goal = goal,
            onDismiss = { contributeTo = null },
            onConfirm = { deltaRial ->
                viewModel.contribute(goal.id, deltaRial)
                contributeTo = null
            },
        )
    }

    pendingDelete?.let { goal ->
        ConfirmDialog(
            tone = ConfirmTone.DESTRUCTIVE,
            title = "«${goal.title}» حذف شود؟",
            consequence = "خودِ هدف و عددی که برایش ثبت کرده‌ای پاک می‌شود. " +
                "هیچ تراکنشی در حساب‌کتاب‌هایت دست نمی‌خورد.",
            actionLabel = "حذف کن",
            onConfirm = { viewModel.deleteGoal(goal); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }

    justReached?.let { goal ->
        ConfirmDialog(
            tone = ConfirmTone.HEAVY_CHANGE,
            title = "به «${goal.title}» رسیدی 🎉",
            consequence = "نشانِ «هدف‌رس» هم باز شد. هدف در فهرست می‌مانَد تا خودت پاکش کنی.",
            actionLabel = "عالی",
            onConfirm = { viewModel.consumeJustReached() },
            onDismiss = { viewModel.consumeJustReached() },
        )
    }
}

/** ذخیره ریال، نمایش تومان - قاعده‌ی واحدِ برنامه. */
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

@Composable
private fun GoalRow(
    goal: SavingsGoalEntity,
    privacyMode: Boolean,
    onContribute: () -> Unit,
    onDelete: () -> Unit,
) {
    // توکنِ رنگ داخلِ `Canvas`/`drawBehind` قابلِ خواندن نیست، پس قبلش خوانده می‌شود.
    val barColor = if (goal.reached) AppAccent else AppPrimary
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).background(AppSurface2, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    iconForKey(goal.iconKey),
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(goal.title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        if (goal.reached) {
                            "${maskIfPrivate(masked, amountToman(goal.savedRial))} تومان - رسیدی"
                        } else {
                            "${maskIfPrivate(masked, amountToman(goal.savedRial))} از " +
                                "${maskIfPrivate(masked, amountToman(goal.targetRial))} تومان"
                        },
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Text(
                "٪${toFa((goal.progress * 100).toInt())}",
                color = barColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
        // ریلِ پیشرفت: دو باکسِ تودرتو، نه `LinearProgressIndicator` - تا شعاع و رنگش با
        // بقیه‌ی برنامه یکی باشد.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .background(AppChipBg, RoundedCornerShape(AppRadius.button)),
        ) {
            if (goal.progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress)
                        .height(6.dp)
                        .background(barColor, RoundedCornerShape(AppRadius.button)),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (goal.reached) "ثبتِ تغییر" else "${amountToman(goal.remainingRial)} تومان مانده",
                color = AppMuted,
                fontSize = 10.5.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                "ثبتِ پس‌انداز",
                color = AppPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .pressScaleClickable(onClick = onContribute)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
            Text(
                "حذف",
                color = AppMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .pressScaleClickable(onClick = onDelete)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun AddGoalForm(
    onCancel: () -> Unit,
    onSubmit: (title: String, targetRial: Double, iconKey: String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var targetText by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf("star") }
    val targetToman = targetText.toDoubleOrNull() ?: 0.0
    val valid = title.isNotBlank() && targetToman > 0.0

    AppCard(label = "هدفِ تازه") {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("برای چه؟") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = targetText,
            onValueChange = { targetText = cleanNum(it) },
            label = { Text("مبلغِ هدف") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            visualTransformation = ThousandsSeparatorTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("تومان") },
        )
        if (targetToman > 0.0) {
            Text(
                "${numberToWordsFa(targetToman)} تومان",
                color = AppMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text("نماد", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            items(categoryIconChoices) { (key, vector) ->
                val selected = key == iconKey
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (selected) AppPrimary.copy(alpha = 0.22f) else AppSurface2,
                            CircleShape,
                        )
                        .pressScaleClickable { iconKey = key },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        vector,
                        contentDescription = null,
                        tint = if (selected) AppPrimary else AppMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            GradientButton(
                onClick = { if (valid) onSubmit(title.trim(), tomanToRial(targetToman.toLong()).toDouble(), iconKey) },
                enabled = valid,
                modifier = Modifier.weight(1f),
            ) { Text("بساز") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("انصراف") }
        }
    }
}

/**
 * ثبتِ پس‌انداز. **هم واریز هم برداشت** - کسی که اشتباه عددی زده باید بتواند برش گرداند،
 * و کسی که از قلکش برداشته باید بتواند صادق بماند.
 *
 * `ConfirmDialog` جای ورودی ندارد، پس این‌جا `AlertDialog`ِ خام است - همان الگوی
 * `RenameCategoryDialog`، با همان توکن‌ها.
 */
@Composable
private fun ContributeDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (deltaRial: Double) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var isWithdraw by remember { mutableStateOf(false) }
    val toman = amountText.toDoubleOrNull() ?: 0.0
    val valid = toman > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text(goal.title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SegmentedToggle(
                    options = listOf("گذاشتم", "برداشتم"),
                    selectedIndex = if (isWithdraw) 1 else 0,
                    onSelect = { isWithdraw = it == 1 },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanNum(it) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("تومان") },
                )
                if (valid) {
                    Text(
                        "${numberToWordsFa(toman)} تومان",
                        color = AppMuted,
                        fontSize = 10.sp,
                    )
                }
                Text(
                    "هیچ تراکنشی ساخته نمی‌شود و موجودیِ حساب‌کتاب‌هایت دست نمی‌خورد.",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 18.sp,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rial = tomanToRial(toman.toLong()).toDouble()
                    onConfirm(if (isWithdraw) -rial else rial)
                },
                enabled = valid,
            ) {
                Text("ثبت", color = if (valid) AppPrimaryInk else AppMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = AppMuted) } },
    )
}
