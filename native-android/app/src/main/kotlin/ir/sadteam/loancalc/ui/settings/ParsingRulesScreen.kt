package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Rule
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.jibak.faCardTail
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ParsingRuleEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **قاعده‌های تشخیص** - زیرصفحه‌ی ۸ از بخشِ ج.
 *
 * تنها زیرصفحه‌ای که **داده‌ی ساختنیِ کاربر** داره، پس دو خطر داره و طرح برای هر کدوم یه
 * جوابِ صریح گذاشته:
 * - «قاعده‌ی غلط ساختم و نمی‌فهمم چرا کار نمی‌کنه» → **شمارنده‌ی تطبیق** رو هر قاعده +
 *   پیش‌نمایشِ زنده‌ی لحظه‌ی ساخت.
 * - «دو قاعده با هم تضاد دارن، کدوم برنده شد؟» → قاعده‌ها **مرتب**ن و **اولین تطبیق
 *   برنده**ست؛ شماره‌ی ترتیب رو خودِ کارت نوشته می‌شه.
 *
 * الگو عمداً فقط «شامل است»ه - بی regex، بی wildcard.
 */
@Composable
fun ParsingRulesScreen(viewModel: ParsingRulesViewModel = hiltViewModel()) {
    val rules by viewModel.rules.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    var editing by remember { mutableStateOf<ParsingRuleEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "قاعده‌ها از بالا به پایین بررسی می‌شن و اولین تطبیق برنده‌ست.",
            color = AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        // دکمه‌ی اصلیِ واضح (فریمِ `29a`) به‌جای دایره‌ی ۳۲dpِ گوشه که دیده نمی‌شد.
        GradientButton(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("قاعده‌ی تازه", fontWeight = FontWeight.Black)
        }
        GradientButton(
            onClick = { testing = true },
            variant = AppButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Icon(Icons.Outlined.Science, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("آزمایشِ تشخیص با یه پیامک", fontWeight = FontWeight.Black)
        }

        if (rules.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Rule,
                title = "هنوز قاعده‌ای نیست",
                description = "لازم نیست خودت بسازی. وقتی سه بار یه فروشنده رو دستی " +
                    "دسته‌بندی کنی، جیبک خودش قاعده‌ش رو می‌سازه.",
            )
        } else {
            rules.forEachIndexed { index, rule ->
                RuleCard(
                    rule = rule,
                    order = index + 1,
                    onEdit = { editing = rule },
                    onDelete = { viewModel.delete(rule) },
                )
            }
        }
    }

    if (testing) {
        SmsTestDialog(onDismiss = { testing = false }, runTest = { viewModel.testSms(it) })
    }

    if (creating || editing != null) {
        RuleSheet(
            rule = editing,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            onDismiss = { creating = false; editing = null },
            onSave = { pattern, category, type ->
                viewModel.save(editing, pattern, category, type, rules.size)
                creating = false
                editing = null
            },
            previewCount = { pattern -> viewModel.previewMatchCount(pattern) },
        )
    }
}

@Composable
private fun RuleCard(
    rule: ParsingRuleEntity,
    order: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    SwipeToDeleteRow(
        onDelete = { confirmDelete = true },
        confirmDismiss = false,
        modifier = Modifier.padding(top = 10.dp),
    ) {
        AppCard(
            contentPadding = 13.dp,
            modifier = Modifier.pressScaleClickable(scale = 0.99f, onClick = onEdit),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(AppSurface2),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(toFa(order), color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Text(
                    rule.pattern,
                    color = AppText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = AppLabel,
                    modifier = Modifier.size(13.dp),
                )
            }
            Row(
                modifier = Modifier.padding(top = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppPrimaryPill)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(rule.category, color = AppPrimaryInk, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                }
                if (rule.txType != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSurface2)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Text(
                            if (rule.txType == TransactionType.WITHDRAWAL.name) "خرج" else "دخل",
                            color = AppMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (rule.matchCount > 0) "${toFa(rule.matchCount)} تراکنش" else "هنوز چیزی نگرفته",
                    color = if (rule.matchCount > 0) AppMuted else AppDangerInk,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
                // قاعده‌ی خودکار باید از دستی پیدا باشه، وگرنه کاربر قاعده‌ای رو که خودش
                // نساخته می‌بینه و گیج می‌شه.
                if (rule.auto) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppInfoPill)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text("خودکار", color = AppInfo, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "حذفِ قاعده",
            text = "قاعده‌ی «${rule.pattern}» حذف بشه؟",
            onConfirm = onDelete,
            onDismiss = { confirmDelete = false },
        )
    }
}

/** شیتِ ساخت/ویرایش - چهار چیز: الگو، دسته، نوع، و **پیش‌نمایشِ زنده**. */
@Composable
private fun RuleSheet(
    rule: ParsingRuleEntity?,
    expenseCategories: List<CategoryEntry>,
    incomeCategories: List<CategoryEntry>,
    onDismiss: () -> Unit,
    onSave: (pattern: String, category: String, type: String?) -> Unit,
    previewCount: suspend (String) -> Int,
) {
    var pattern by remember { mutableStateOf(rule?.pattern ?: "") }
    var category by remember { mutableStateOf(rule?.category ?: "") }
    var typeIndex by remember {
        mutableStateOf(
            when (rule?.txType) {
                TransactionType.WITHDRAWAL.name -> 1
                TransactionType.DEPOSIT.name -> 2
                else -> 0
            },
        )
    }
    var matches by remember { mutableStateOf<Int?>(null) }
    // debounce ۳۵۰ms - وگرنه با هر حرف یه پرسِ دیتابیس می‌ره.
    LaunchedEffect(pattern) {
        matches = null
        if (pattern.isBlank()) return@LaunchedEffect
        kotlinx.coroutines.delay(350)
        matches = previewCount(pattern)
    }

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pattern.isNotBlank() && category.isNotBlank(),
                onClick = {
                    onSave(
                        pattern.trim(),
                        category.trim(),
                        when (typeIndex) {
                            1 -> TransactionType.WITHDRAWAL.name
                            2 -> TransactionType.DEPOSIT.name
                            else -> null
                        },
                    )
                },
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("بی‌خیال") } },
        title = { Text(if (rule == null) "قاعده‌ی تازه" else "ویرایشِ قاعده", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("اگه تو متنِ پیامک این بود") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "بخشی از نامِ فروشنده کافیه - «رفاه» هم «فروشگاه رفاه» رو می‌گیره.",
                    color = AppLabel,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                SegmentedToggle(
                    options = listOf("مهم نیست", "خرج", "دخل"),
                    selectedIndex = typeIndex,
                    onSelect = { typeIndex = it },
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    "دسته‌ی مقصد",
                    color = AppLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                )
                // انتخابگرِ آیکون‌دار (فریمِ `29b`) به‌جای فیلدِ متنی - اسمِ تایپی با هیچ دسته‌ای
                // جور درنمی‌اومد و قاعده بی‌صدا بی‌اثر می‌شد.
                val shownCats = when (typeIndex) {
                    1 -> expenseCategories
                    2 -> incomeCategories
                    else -> expenseCategories + incomeCategories
                }
                val options = if (category.isNotBlank() && shownCats.none { it.name == category }) {
                    shownCats + CategoryEntry(category, AppMutedColorFallback, "other", TransactionType.WITHDRAWAL)
                } else shownCats
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { c ->
                                CategoryOption(
                                    entry = c,
                                    selected = c.name == category,
                                    onClick = { category = c.name },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppSurface2)
                        .padding(12.dp),
                ) {
                    val count = matches
                    Text(
                        when {
                            pattern.isBlank() -> "متنی بنویس تا بگم با چند تراکنش می‌خوره"
                            count == null -> "دارم می‌شمرم..."
                            count > 0 -> "با ${toFa(count)} تراکنشِ گذشته‌ت می‌خوره"
                            else -> "با هیچ تراکنشی نخورد. مطمئنی؟"
                        },
                        color = when {
                            pattern.isBlank() || matches == null -> AppLabel
                            (matches ?: 0) > 0 -> AppPrimaryInk
                            else -> AppDangerInk
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        },
    )
}

private val AppMutedColorFallback = androidx.compose.ui.graphics.Color(0xFF757575)

@Composable
private fun CategoryOption(entry: CategoryEntry, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (selected) AppPrimaryPill else AppSurface2)
            .border(1.dp, if (selected) AppPrimary else AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(entry.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(15.dp))
        }
        Text(
            entry.name,
            color = if (selected) AppPrimaryInk else AppText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** آزمایشِ تشخیص (فریمِ `29c`): نتیجه در کارتِ چهارخانه، نه متنِ خام. */
@Composable
private fun SmsTestDialog(onDismiss: () -> Unit, runTest: suspend (String) -> SmsTestResult?) {
    var body by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SmsTestResult?>(null) }
    var tested by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = body.isNotBlank(),
                onClick = { scope.launch { result = runTest(body); tested = true } },
            ) { Text("آزمایش") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("بستن") } },
        title = { Text("آزمایشِ تشخیص", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it; tested = false },
                    label = { Text("متنِ پیامکِ بانکی رو اینجا بچسبون") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "فقط آزمایشه - هیچ تراکنشی ثبت نمی‌شه.",
                    color = AppLabel,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                AnimatedVisibility(visible = tested) {
                    val r = result
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppSurface2)
                            .border(1.dp, if (r != null) AppPrimary else AppDangerInk, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                if (r != null) Icons.Filled.CheckCircle else Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = if (r != null) AppPrimary else AppDangerInk,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                if (r != null) "نتیجه‌ی تشخیص" else "این پیامک تراکنش شناخته نشد",
                                color = AppText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        if (r != null) {
                            TestResultRow("مبلغ", "${rialToToman(r.amountRial).toFaMoney()} تومان")
                            TestResultRow("نوع", if (r.isWithdrawal) "خرج" else "دخل", if (r.isWithdrawal) AppDangerInk else AppPrimaryInk)
                            TestResultRow(
                                "حساب",
                                r.accountName ?: r.cardSuffix?.let { "کارتِ ${faCardTail(it)} (ثبت‌نشده)" } ?: "نامشخص",
                            )
                            TestResultRow(
                                "دسته",
                                when {
                                    r.category == null -> "نامشخص - خودت انتخاب می‌کنی"
                                    r.byRule != null -> "${r.category} (قاعده‌ی «${r.byRule}»)"
                                    else -> "${r.category} (حدسِ خودکار)"
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun TestResultRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = AppText) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
        Text(value, color = valueColor, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
    }
}
