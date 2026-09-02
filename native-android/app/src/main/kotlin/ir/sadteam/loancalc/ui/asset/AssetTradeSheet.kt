package ir.sadteam.loancalc.ui.asset

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.toFaCompact
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * شیتِ «تراکنشِ دارایی» — خرید (سبز) / فروش (قرمز).
 *
 * **ترتیبِ فیلدها عوض شد و این هسته‌ی بازطراحی است.** قبلاً «مبلغِ کل» اول بود، بعد
 * «نوع دارایی»، بعد «مقدار» — یعنی کاربر ۲۰ میلیون تومان می‌زد، طلا را انتخاب می‌کرد، و
 * بعد باید خودش حساب می‌کرد ۲۰ میلیون چند گرم است. کارِ ماشین را به آدم داده بودیم.
 *
 * ```
 * ۱ خرید / فروش
 * ۲ نوع دارایی  ← اول، چون قیمتِ روزش مبنای همه‌ی حساب‌هاست
 * ۳ مبلغِ کل (تومان)
 * ۴ مقدار  ← خودکار حساب می‌شود؛ دکمه‌ی ویرایشِ دستی هم دارد
 * ۵ تاریخ
 * ۶ توضیحات
 * ```
 *
 * **ویرایشِ دستیِ مقدار** برای خریدِ قدیمی است: کسی که پارسال دلار را ۵۰ هزار تومان
 * خریده، مقدار را دستی می‌زند و برنامه قیمتِ واحدِ **خودش** را از مبلغ و مقدار حساب و
 * ثبت می‌کند — نه قیمتِ امروز. بی این، سودِ محاسبه‌شده غلط می‌شد.
 *
 * بعد از ثبت یک پیام می‌آید و توضیح می‌دهد که خرید و فروشِ دارایی روی دخل‌وخرج اثر ندارد.
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
    val prices by viewModel.marketPrices.collectAsState()
    val changes by viewModel.monthChange.collectAsState()

    var isBuy by remember { mutableStateOf(!startWithSell) }
    var totalText by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("") }
    var manualQty by remember { mutableStateOf(false) }
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
    var showPicker by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showAfterNote by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val toman = totalText.toLongOrNull() ?: 0L
    val unitPriceRial = picked?.let { prices[it.symbol] }
    val unitToman = unitPriceRial?.let { rialToToman(it.toLong()) }?.takeIf { it > 0L }

    // مقدارِ خودکار: مبلغ ÷ قیمتِ واحد. عنوانِ دلخواه قیمتِ روز ندارد، پس همیشه دستی است.
    val autoQty = if (unitToman != null && toman > 0L) toman.toDouble() / unitToman else null
    val qtyManual = qtyText.toDoubleOrNull()
    val effectiveQty = if (manualQty) qtyManual else autoQty

    // قیمتِ واحدی که **واقعاً ثبت می‌شود**: در حالتِ دستی از مبلغ و مقدارِ خودِ کاربر
    // حساب می‌شود (خریدِ قدیمی)، وگرنه همان قیمتِ روز.
    val recordedUnitRial = if (manualQty && qtyManual != null && qtyManual > 0.0) {
        tomanToRial(toman).toDouble() / qtyManual
    } else {
        unitPriceRial
    }

    // تقویم بازگشتِ سیستمی می‌گیرد، وگرنه دکمه‌ی back کلِ فرمِ نیمه‌پرشده را می‌بست.
    // انتخابگر BackHandlerِ خودش را دارد و چون بعد از این ثبت می‌شود، اولویت با اوست.
    BackHandler(enabled = showCalendar) { showCalendar = false }

    // تقویم و انتخابگر **روی** فرم می‌نشینند، نه به‌جایش (رجوع کن به باگِ `return`).
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeaderSquareButton(
                    icon = Icons.Filled.Close,
                    description = "بستن",
                    fill = AppIconFrame,
                    border = AppLine,
                    ink = AppMuted,
                    onClick = onDismiss,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (isBuy) "ثبتِ خرید" else "ثبتِ فروش",
                    color = AppText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(32.dp))
            }

            AppCard {
                SegmentedToggle(
                    options = listOf("خرید", "فروش"),
                    selectedIndex = if (isBuy) 0 else 1,
                    onSelect = { isBuy = it == 0 },
                    selectedColor = if (isBuy) AppPrimary else AppDanger,
                )
            }

            // ═══ ۲ · نوع دارایی — **اول** ═══
            AssetPickField(
                picked = picked,
                unitPriceRial = unitPriceRial,
                changePercent = picked?.let { changes[it.symbol] },
                onClick = { showPicker = true },
            )

            // ═══ ۳ · مبلغِ کل ═══
            AppCard(label = "مبلغِ کل") {
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    // فیلد تا وقتی دارایی انتخاب نشده باز است ولی راهنما می‌گوید ترتیب چیست —
                    // قفل‌کردنش کاربر را بی توضیح سرِ جا نگه می‌داشت.
                    enabled = picked != null,
                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                )
                if (picked == null) {
                    Text(
                        "اول نوعِ دارایی را انتخاب کن تا مقدار خودکار حساب شود.",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                } else if (toman > 0L) {
                    Text(
                        "${numberToWordsFa(toman.toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            // ═══ ۴ · مقدار — خودکار، با ویرایشِ دستی ═══
            QuantityField(
                entryName = picked?.name,
                autoQty = autoQty,
                unitToman = unitToman,
                manual = manualQty,
                qtyText = qtyText,
                onQtyChange = { new -> qtyText = new.filter { it.isDigit() || it == '.' } },
                onToggleManual = {
                    // ورود به حالتِ دستی مقدارِ خودکار را به‌عنوانِ نقطه‌ی شروع می‌گذارد،
                    // تا کاربر از صفر تایپ نکند.
                    if (!manualQty) qtyText = autoQty?.let { formatQuantity(it) }.orEmpty()
                    manualQty = !manualQty
                },
                recordedUnitRial = recordedUnitRial,
                marketUnitRial = unitPriceRial,
            )

            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "${toFa(date.d)} ${persianMonthName(date.m)} ${toFa(date.y)}",
                        color = AppText,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .pressScaleClickable { showCalendar = true },
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

            if (error != null) Text(error ?: "", color = AppDangerInk, fontSize = 12.sp)

            GradientButton(
                onClick = {
                    val entry = picked
                    val qty = effectiveQty ?: 0.0
                    error = when {
                        entry == null -> "نوعِ دارایی را انتخاب کن"
                        toman <= 0L -> "مبلغ را وارد کن"
                        qty <= 0.0 && !manualQty -> "قیمتِ روزِ این دارایی نرسیده — مقدار را دستی وارد کن"
                        qty <= 0.0 -> "مقدار را وارد کن"
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
                            // قیمتِ خریدِ خودِ کاربر ثبت می‌شود، نه همیشه قیمتِ امروز.
                            // این پارامتر از قبل وجود داشت و هیچ‌وقت پر نمی‌شد.
                            unitPriceRial = recordedUnitRial,
                        )
                        showAfterNote = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isBuy) "ثبتِ خرید" else "ثبتِ فروش")
            }
        }

        if (showPicker) {
            AssetPickerSheet(
                prices = prices,
                changes = changes,
                selectedSymbol = picked?.symbol,
                onPick = { entry ->
                    picked = entry
                    showPicker = false
                    // عنوانِ دلخواه قیمتِ روز ندارد، پس مقدار فقط دستی معنا دارد.
                    if (entry.category == ASSET_CATEGORY_CUSTOM) manualQty = true
                },
                onDismiss = { showPicker = false },
            )
        }

        if (showCalendar) {
            // زمینه اجباری است — CalendarPickerScreen خودش زمینه ندارد.
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                CalendarPickerScreen(
                    initialDate = date,
                    onDateSelected = { date = it; showCalendar = false },
                    onBack = { showCalendar = false },
                )
            }
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
                        "خرید و فروشِ دارایی تاثیری تو دخل‌وخرج نداره. اگه می‌خوای این مبلغ تو " +
                            "حساب‌کتابت هم حساب بشه، براش یه تراکنش ثبت کن.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showAfterNote = false; onDismiss() }) {
                        Text("باشه، برای بعد")
                    }
                },
            )
        }
    }
}

/**
 * ردیفِ «نوع دارایی» — نشان، اسم و **قیمتِ روز** یک‌جا.
 *
 * ردیفِ قبلی یک متنِ خاکستریِ «نوع دارایی» با یک فلش بود: نه معلوم بود چه چیزهایی هست،
 * نه بعد از انتخاب قیمتش دیده می‌شد.
 */
@Composable
private fun AssetPickField(
    picked: AssetCatalogEntry?,
    unitPriceRial: Double?,
    changePercent: Double?,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            // حاشیه‌ی سبز وقتی خالی است: این اولین کاری است که کاربر باید بکند.
            .border(2.dp, if (picked == null) AppPrimaryBorder else AppLineRow, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (picked != null) {
            AssetBadge(picked.symbol, picked.category, 34.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Text("؟", color = AppPrimaryInk, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                picked?.name ?: "چه چیزی خریدی؟",
                color = if (picked != null) AppText else AppPrimaryInk,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 3.dp),
            ) {
                Text(
                    when {
                        picked == null -> "طلا، ارز، رمز ارز یا عنوانِ دلخواه"
                        unitPriceRial != null -> "قیمتِ روز ${unitPriceRial.rialToFaCompactLocal()} تومان"
                        else -> "قیمتِ روز —"
                    },
                    color = AppMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (changePercent != null) PriceChangeBadge(changePercent)
            }
        }
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * فیلدِ مقدار در دو حالت.
 *
 * **خودکار (پیش‌فرض):** عدد فقط خوانده می‌شود و زیرش می‌گوید از چه قیمتی درآمده. یک
 * کارتِ خواندنی است نه فیلد، چون تایپ در آن کاربر را گیج می‌کرد.
 *
 * **دستی:** فیلدِ معمولی، و زیرش قیمتِ واحدی که از مبلغ و مقدار درمی‌آید — همراهِ
 * مقایسه با قیمتِ روز، تا کسی که اشتباه تایپ کرده خودش ببیند.
 */
@Composable
private fun QuantityField(
    entryName: String?,
    autoQty: Double?,
    unitToman: Long?,
    manual: Boolean,
    qtyText: String,
    onQtyChange: (String) -> Unit,
    onToggleManual: () -> Unit,
    recordedUnitRial: Double?,
    marketUnitRial: Double?,
) {
    AppCard(label = "مقدار") {
        if (manual) {
            OutlinedTextField(
                value = qtyText,
                // اعشار لازم است (۱٫۲ بیت‌کوین)، پس فیلترِ رقم و نقطه نه cleanNum.
                onValueChange = onQtyChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً ۱٫۲ یا ۳", color = AppMuted, fontSize = 12.sp) },
            )
            if (recordedUnitRial != null && recordedUnitRial > 0.0) {
                val yours = rialToToman(recordedUnitRial.toLong())
                val market = marketUnitRial?.let { rialToToman(it.toLong()) }
                Text(
                    buildString {
                        append("قیمتِ واحدِ تو: ${yours.toFaCompact()} تومان")
                        // مقایسه فقط وقتی می‌آید که قیمتِ روز هم داشته باشیم؛ وگرنه
                        // «۰٪ اختلاف» چاپ می‌شد.
                        if (market != null && market > 0L) {
                            val diff = (yours - market) * 100.0 / market
                            append(if (diff >= 0) " · گران‌تر از قیمتِ روز" else " · ارزان‌تر از قیمتِ روز")
                        }
                    },
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        // «—» یعنی یا مبلغ خالی است یا قیمتِ روز نرسیده. حالتِ عادی است.
                        autoQty?.let { formatQuantity(it) } ?: "—",
                        color = if (autoQty == null) AppMuted else AppPrimaryInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        when {
                            autoQty != null && entryName != null -> "$entryName · بر اساسِ قیمتِ روز"
                            unitToman == null && entryName != null -> "قیمتِ روزِ این دارایی نرسیده"
                            else -> "مبلغ را وارد کن"
                        },
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.button))
                        .background(AppIconFrame)
                        .border(1.5.dp, AppLine, RoundedCornerShape(AppRadius.button))
                        .pressScaleClickable(onClick = onToggleManual)
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(13.dp),
                    )
                    Text("دستی", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        if (manual) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(AppRadius.button))
                    .background(AppIconFrame)
                    .border(1.5.dp, AppLine, RoundedCornerShape(AppRadius.button))
                    .pressScaleClickable(onClick = onToggleManual)
                    .padding(horizontal = 11.dp, vertical = 8.dp),
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    "برگرد به محاسبه‌ی خودکار",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** میان‌برِ محلی تا این فایل به import دوم نیاز نداشته باشد. */
private fun Double.rialToFaCompactLocal(): String = rialToToman(toLong()).toFaCompact()
