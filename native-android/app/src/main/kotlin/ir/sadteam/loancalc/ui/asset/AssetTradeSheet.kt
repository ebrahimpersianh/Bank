package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * شیتِ «تراکنشِ دارایی» - خرید (سبز) / فروش (قرمز)، طبقِ اپِ مرجع.
 *
 * بعد از ثبت، یه پیام میاد و توضیح می‌ده که **خرید و فروشِ دارایی رو دخل‌وخرج اثر نداره** و اگه
 * می‌خوای تو حساب‌کتاب هم حساب بشه، باید براش تراکنشِ جدا ثبت کنی - تاییدِ صریحِ کاربر.
 */
@Composable
fun AssetTradeSheet(
    onDismiss: () -> Unit,
    viewModel: AssetViewModel,
    presetSymbol: String? = null,
    presetName: String? = null,
    presetCategory: String? = null,
    startWithSell: Boolean = false,
) {
    var isBuy by remember { mutableStateOf(!startWithSell) }
    var totalText by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(JalaliCalendar.today()) }
    var description by remember { mutableStateOf("") }
    var picked by remember {
        mutableStateOf(
            if (presetSymbol != null && presetName != null && presetCategory != null) {
                AssetCatalogEntry(presetSymbol, presetName, presetCategory)
            } else {
                null
            },
        )
    }
    var showTypePicker by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showAfterNote by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showCalendar) {
        CalendarPickerScreen(
            initialDate = date,
            onDateSelected = { date = it; showCalendar = false },
            onBack = { showCalendar = false },
        )
        return
    }

    if (showTypePicker) {
        AssetTypePickerSheet(
            onPick = { picked = it; showTypePicker = false },
            onDismiss = { showTypePicker = false },
        )
    }

    if (showAfterNote) {
        AlertDialog(
            onDismissRequest = { showAfterNote = false; onDismiss() },
            title = {
                Text(
                    if (isBuy) "ثبتِ تراکنشِ مرتبط با خرید" else "ثبتِ تراکنشِ مرتبط با فروش",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "خرید و فروشِ دارایی تاثیری تو دخل‌وخرج نداره. اگه می‌خوای این مبلغ تو حساب‌کتابت هم حساب بشه، براش یه تراکنش ثبت کن.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAfterNote = false; onDismiss() }) { Text("باشه، برای بعد") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("تراکنشِ دارایی", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        AppCard {
            SegmentedToggle(
                options = listOf("خرید", "فروش"),
                selectedIndex = if (isBuy) 0 else 1,
                onSelect = { isBuy = it == 0 },
                selectedColor = if (isBuy) AppPrimary else AppDanger,
            )
        }

        AppCard(label = "مبلغِ کل") {
            OutlinedTextField(
                value = totalText,
                onValueChange = { totalText = cleanNum(it) },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
            )
            val toman = totalText.toLongOrNull() ?: 0L
            if (toman > 0) {
                Text(
                    "${numberToWordsFa(toman.toDouble())} تومان",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth().pressScaleClickable(onClick = { showTypePicker = true }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    picked?.name ?: "نوع دارایی",
                    color = if (picked != null) AppText else AppMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
            }
        }

        AppCard(label = "مقدار") {
            OutlinedTextField(
                value = qtyText,
                // اعشار لازمه (۱.۲ بیت‌کوین)، پس cleanNumDecimal نه cleanNum.
                onValueChange = { new -> qtyText = new.filter { it.isDigit() || it == '.' } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً ۱٫۲ یا ۳", color = AppMuted, fontSize = 12.sp) },
            )
        }

        AppCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                Text(
                    "${toFa(date.d)} ${persianMonthName(date.m)} ${toFa(date.y)}",
                    color = AppText,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .pressScaleClickable(onClick = { showCalendar = true }),
                )
            }
        }

        AppCard {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("توضیحات", color = AppMuted, fontSize = 13.sp) },
            )
        }

        if (error != null) Text(error ?: "", color = AppDanger, fontSize = 12.sp)

        GradientButton(
            onClick = {
                val toman = totalText.toLongOrNull() ?: 0L
                val qty = qtyText.toDoubleOrNull() ?: 0.0
                val entry = picked
                error = when {
                    toman <= 0L -> "مبلغ رو وارد کن"
                    entry == null -> "نوعِ دارایی رو انتخاب کن"
                    qty <= 0.0 -> "مقدار رو وارد کن"
                    else -> null
                }
                if (error == null && entry != null) {
                    viewModel.recordTrade(
                        symbol = entry.symbol,
                        name = entry.name,
                        category = entry.category,
                        isBuy = isBuy,
                        quantity = qty,
                        totalRial = tomanToRial(toman).toDouble(),
                        year = date.y,
                        month = date.m,
                        day = date.d,
                        description = description.trim(),
                    )
                    showAfterNote = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ثبت")
        }
    }
}

/** انتخابگرِ «نوع دارایی» - سه دسته‌ی آماده + گزینه‌ی عنوانِ دلخواه. */
@Composable
private fun AssetTypePickerSheet(
    onPick: (AssetCatalogEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نوع دارایی", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                assetCatalogGroups.forEach { (groupTitle, entries) ->
                    item(key = "h_$groupTitle") {
                        Text(
                            groupTitle,
                            color = AppMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        )
                    }
                    items(entries.size, key = { "e_${entries[it].symbol}" }) { i ->
                        val entry = entries[i]
                        TextButton(
                            onClick = { onPick(entry) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(entry.name, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp)
                        }
                    }
                }
                item(key = "custom") {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text("عنوانِ دلخواه", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "دارایی‌هایی غیر از طلا و ارز و سکه (ملک، اوراق و…)",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                        )
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            singleLine = true,
                            placeholder = { Text("اسمِ دارایی", fontSize = 12.sp) },
                        )
                        TextButton(
                            onClick = {
                                val n = customName.trim()
                                if (n.isNotEmpty()) {
                                    // نماد از رو خودِ اسم ساخته می‌شه تا یکتا بمونه و دوباره ساخته نشه.
                                    onPick(AssetCatalogEntry("CUSTOM_$n", n, ASSET_CATEGORY_CUSTOM))
                                }
                            },
                            enabled = customName.isNotBlank(),
                        ) { Text("افزودن") }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
