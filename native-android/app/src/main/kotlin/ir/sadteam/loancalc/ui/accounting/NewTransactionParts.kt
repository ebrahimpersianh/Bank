@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package ir.sadteam.loancalc.ui.accounting

import androidx.compose.ui.graphics.graphicsLayer
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppTxOut
import ir.sadteam.loancalc.ui.theme.AppTxIn
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import kotlinx.coroutines.launch
import ir.sadteam.loancalc.ui.components.GradientButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.runtime.MutableState

@Composable
internal fun NewTxTemplatesRow(
    accounts: List<ir.sadteam.loancalc.data.db.AccountEntity>,
    templates: List<ir.sadteam.loancalc.data.db.TxTemplateEntity>,
    accent: Color,
    categories: List<CategoryEntry>,
    deletingTemplateState: MutableState<ir.sadteam.loancalc.data.db.TxTemplateEntity?>,
    kindState: MutableState<NewTxKind>,
    amountTextState: MutableState<String>,
    descriptionState: MutableState<String>,
    accountIdState: MutableState<Long?>,
    categoryState: MutableState<String?>,
) {
    var deletingTemplate by deletingTemplateState
    var kind by kindState
    var amountText by amountTextState
    var description by descriptionState
    var accountId by accountIdState
    var category by categoryState
        if (kind != NewTxKind.TRANSFER) {
            val kindType = if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT.name else TransactionType.WITHDRAWAL.name
            val mine = templates.filter { it.type == kindType }
            if (mine.isNotEmpty()) {
                Text("الگوهای من", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    mine.forEach { t ->
                        val catEntry = categories.firstOrNull { it.name == t.category }
                        val applied = description == t.name && category == t.category
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (applied) accent.copy(alpha = 0.14f) else AppSurface)
                                .border(if (applied) 1.5.dp else 1.dp, if (applied) accent else AppLine, RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (t.amount > 0) amountText = rialToToman(t.amount.toLong()).toString()
                                        category = t.category
                                        if (t.accountId != null && accounts.any { it.id == t.accountId }) accountId = t.accountId
                                        description = t.name
                                    },
                                    onLongClick = { deletingTemplate = t },
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            if (catEntry != null) {
                                Icon(catEntry.icon, contentDescription = null, tint = catEntry.color, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(t.name, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                if (t.amount > 0) {
                                    Text(
                                        rialToToman(t.amount.toLong()).toFaMoney() + " تومان",
                                        color = AppMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
internal fun NewTxAmountCard(
    accent: Color,
    amountTextState: MutableState<String>,
    errorState: MutableState<String?>,
) {
    var amountText by amountTextState
    var error by errorState
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ⚠️ برچسب «ریال» بود و کپشنِ زیرش «تومان» - یعنی کاربر در یک کارت دو واحد
                // می‌دید و باید حدس می‌زد عددی که تایپ می‌کند کدام است. این تنها جای
                // باقی‌مانده‌ی برنامه بود که ورودی ریالی می‌گرفت. ستونِ دیتابیس ریال می‌ماند؛
                // `tomanToRial` در لبه‌ی ثبت تبدیل می‌کند.
                Text("مبلغ · تومان", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                BasicTextField(
                    value = amountText,
                    // خطا با اولین اصلاح پاک می‌شود، نه با ثبتِ بعدی: پیامِ «مبلغ رو وارد کن»
                    // زیرِ فیلدی که دارد پر می‌شود، نویز است.
                    onValueChange = { amountText = cleanNum(it); error = null },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = accent,
                        letterSpacing = (-1).sp,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (amountText.isEmpty()) {
                                Text(
                                    "۰",
                                    color = AppMuted.copy(alpha = 0.5f),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            inner()
                        }
                    },
                )
                val toman = amountText.toLongOrNull() ?: 0L
                if (toman > 0) {
                    Text(
                        // دیگر تقسیم بر ده لازم نیست - خودِ فیلد تومان است.
                        "${numberToWordsFa(toman.toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
}

@Composable
internal fun NewTxDetailsSection(
    onDismiss: () -> Unit,
    txPremium: Boolean,
    splits: androidx.compose.runtime.snapshots.SnapshotStateList<Pair<String?, String>>,
    pickReceipt: androidx.activity.compose.ManagedActivityResultLauncher<String, android.net.Uri?>,
    accent: Color,
    categories: List<CategoryEntry>,
    tagsTextState: MutableState<String>,
    reimbursableState: MutableState<Boolean>,
    receiptPathState: MutableState<String?>,
    splitModeState: MutableState<Boolean>,
    showSaveTemplateState: MutableState<Boolean>,
    kindState: MutableState<NewTxKind>,
    amountTextState: MutableState<String>,
    descriptionState: MutableState<String>,
) {
    var tagsText by tagsTextState
    var reimbursable by reimbursableState
    var receiptPath by receiptPathState
    var splitMode by splitModeState
    var showSaveTemplate by showSaveTemplateState
    var kind by kindState
    var amountText by amountTextState
    var description by descriptionState
        if (kind != NewTxKind.TRANSFER) {
            AppCard {
                // فریمِ `40`: گزینه‌های اختیاری پشتِ «گزینه‌های بیشتر» - فرمِ اصلی سبک می‌ماند.
                // اگر یکی از آن‌ها از قبل پر است (مثلاً الگو)، باز شروع می‌شود تا پنهان نماند.
                var moreOpen by remember { mutableStateOf(splitMode || reimbursable || receiptPath != null || tagsText.isNotBlank()) }
                val moreTurn by androidx.compose.animation.core.animateFloatAsState(if (moreOpen) 180f else 0f, label = "moreArrow")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { moreOpen = !moreOpen }.padding(vertical = 6.dp),
                ) {
                    Text("گزینه‌های بیشتر", color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("تقسیم · برچسب · رسید", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = moreTurn },
                    )
                }
                androidx.compose.animation.AnimatedVisibility(visible = moreOpen) {
                Column {
                // 💱 مبلغ به ارز (۸ مهر): تبدیل به تومان با نرخِ روز؛ ارزِ اصلی در شرح می‌ماند.
                var showCurrency by remember { mutableStateOf(false) }
                if (showCurrency) {
                    CurrencyAmountDialog(
                        onDismiss = { showCurrency = false },
                        onConfirm = { t, note ->
                            amountText = t.toString()
                            description = if (description.isBlank()) note else "$description · $note"
                            showCurrency = false
                        },
                    )
                }
                // ۱۴ مهر: از کادرِ مبلغ به این‌جا آمد - برای بیشترِ کاربران لازم نیست.
                Text(
                    "مبلغ به دلار/یورو/درهم",
                    color = AppPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showCurrency = true }.padding(horizontal = 8.dp, vertical = 6.dp),
                )
                // ذخیره به‌عنوانِ الگو - از کنارِ دکمه‌ی ثبت به این‌جا آمد (۱۴ مهر).
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { showSaveTemplate = true }.padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                    Text("ذخیره به‌عنوانِ الگو", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
                // تقسیمِ یک خرید بینِ چند دسته - هر ردیف یک تراکنشِ جدا با شناسه‌ی مشترک.
                if (kind == NewTxKind.EXPENSE) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("تقسیم بینِ چند دسته", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        ir.sadteam.loancalc.ui.settings.AppSwitch(checked = splitMode, onCheckedChange = {
                            if (it && !txPremium) {
                                ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("split", "تقسیمِ خرید")
                                return@AppSwitch
                            }
                            splitMode = it
                            if (it && splits.isEmpty()) { splits.add(null to ""); splits.add(null to "") }
                        })
                    }
                    if (splitMode) {
                        // فریمِ `41`ِ ChatGPT: خلاصه‌ی «جمع / باقی‌مانده» بالای ردیف‌ها + نوارِ پیشرفت،
                        // هر ردیف = دسته (آیکون و رنگِ خودش) + مبلغ + حذف.
                        val totalToman = amountText.toLongOrNull() ?: 0L
                        val splitSum = splits.sumOf { it.second.toLongOrNull() ?: 0L }
                        val remaining = totalToman - splitSum
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppChipBg)
                                .padding(12.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("جمعِ ردیف‌ها", color = AppMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("باقی‌مانده برای تقسیم", color = AppMuted, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                Text(
                                    splitSum.toFaMoney() + " تومان",
                                    color = AppText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    if (totalToman == 0L) "—" else remaining.toFaMoney() + " تومان",
                                    color = when {
                                        totalToman == 0L -> AppMuted
                                        remaining == 0L -> AppTxIn
                                        remaining < 0 -> AppTxOut
                                        else -> accent
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            if (totalToman > 0) {
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { (splitSum.toFloat() / totalToman).coerceIn(0f, 1f) },
                                    color = if (remaining < 0) AppTxOut else AppTxIn,
                                    trackColor = AppLine,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                )
                            } else {
                                Text("مبلغِ کل را بالا بنویس تا باقی‌مانده حساب شود.", color = AppMuted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                        splits.forEachIndexed { i, (cat, amt) ->
                            val entry = categories.firstOrNull { it.name == cat }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Box(modifier = Modifier.weight(1.15f)) {
                                    var open by remember { mutableStateOf(false) }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, if (cat == null) accent.copy(alpha = 0.6f) else AppLine, RoundedCornerShape(12.dp))
                                            .clickable { open = true }
                                            .padding(horizontal = 10.dp, vertical = 14.dp),
                                    ) {
                                        if (entry != null) {
                                            Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(18.dp).padding(end = 2.dp))
                                        }
                                        Text(
                                            cat ?: "انتخابِ دسته",
                                            color = if (cat == null) accent else AppText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                                        )
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                                    }
                                    androidx.compose.material3.DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                        categories.forEach { c ->
                                            androidx.compose.material3.DropdownMenuItem(
                                                leadingIcon = { Icon(c.icon, contentDescription = null, tint = c.color) },
                                                text = { Text(c.name) },
                                                onClick = { splits[i] = c.name to amt; open = false },
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = amt,
                                    onValueChange = { v -> splits[i] = cat to cleanNum(v).take(13) },
                                    placeholder = { Text("تومان", fontSize = 11.sp) },
                                    visualTransformation = ThousandsSeparatorTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppTxOut.copy(alpha = 0.14f))
                                        .clickable { splits.removeAt(i); if (splits.isEmpty()) splitMode = false },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذفِ ردیف", tint = AppTxOut, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Text(
                            "+ ردیفِ دیگر",
                            color = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { splits.add(null to "") }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = {
                        if (txPremium) tagsText = it.take(80)
                        else ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("tags", "برچسب")
                    },
                    placeholder = { Text("برچسب (مثلاً سفرِ شمال، عروسی) - با «،» جدا کن", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                if (kind == NewTxKind.EXPENSE) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("بازپرداخت می‌شود (خرجِ کاری و…)", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        ir.sadteam.loancalc.ui.settings.AppSwitch(checked = reimbursable, onCheckedChange = { reimbursable = it })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        if (receiptPath == null) "📎 افزودنِ عکسِ رسید" else "✓ رسید پیوست شد",
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).clickable {
                            // عکسِ رسید مالِ اشتراک است (جدولِ ۷ مهر) - این‌جا بی‌قفل مانده بود.
                            if (txPremium) pickReceipt.launch("image/*")
                            else ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("receipt_photo", "عکسِ رسید")
                        }.padding(vertical = 10.dp),
                    )
                }
                }
                }
            }
        }
}

@Composable
internal fun NewTxSaveTemplateDialog(
    extrasViewModel: ir.sadteam.loancalc.ui.extras.ExtrasViewModel,
    showSaveTemplateState: MutableState<Boolean>,
    kindState: MutableState<NewTxKind>,
    amountTextState: MutableState<String>,
    descriptionState: MutableState<String>,
    accountIdState: MutableState<Long?>,
    categoryState: MutableState<String?>,
) {
    var showSaveTemplate by showSaveTemplateState
    var kind by kindState
    var amountText by amountTextState
    var description by descriptionState
    var accountId by accountIdState
    var category by categoryState
        if (showSaveTemplate) {
            var tName by remember { mutableStateOf(description.ifBlank { category ?: "" }) }
            ir.sadteam.loancalc.ui.components.JibakAlertDialog(
                onDismissRequest = { showSaveTemplate = false },
                title = { Text("ذخیره به‌عنوانِ الگو") },
                text = {
                    OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),value = tName, onValueChange = { tName = it.take(30) }, singleLine = true, placeholder = { Text("مثلاً نون، بنزین") }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                },
                confirmButton = {
                    GradientButton(onClick = {
                        if (tName.isNotBlank()) {
                            extrasViewModel.saveTemplate(
                                tName,
                                if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT.name else TransactionType.WITHDRAWAL.name,
                                tomanToRial(amountText.toLongOrNull() ?: 0L).toDouble(),
                                category,
                                accountId,
                            )
                        }
                        showSaveTemplate = false
                    }, enabled = tName.isNotBlank()) { Text("ذخیره") }
                },
                dismissButton = { androidx.compose.material3.TextButton(onClick = { showSaveTemplate = false }) { Text("انصراف") } },
            )
        }
}

@Composable
internal fun NewTxBottomBar(
    onDismiss: () -> Unit,
    accountViewModel: AccountViewModel,
    monthTxCount: Int,
    txPremium: Boolean,
    splits: androidx.compose.runtime.snapshots.SnapshotStateList<Pair<String?, String>>,
    accent: Color,
    tagsTextState: MutableState<String>,
    reimbursableState: MutableState<Boolean>,
    receiptPathState: MutableState<String?>,
    splitModeState: MutableState<Boolean>,
    kindState: MutableState<NewTxKind>,
    amountTextState: MutableState<String>,
    dateState: MutableState<ir.sadteam.loancalc.core.PersianDate>,
    descriptionState: MutableState<String>,
    accountIdState: MutableState<Long?>,
    fromAccountIdState: MutableState<Long?>,
    toAccountIdState: MutableState<Long?>,
    categoryState: MutableState<String?>,
    errorState: MutableState<String?>,
) {
    var tagsText by tagsTextState
    var reimbursable by reimbursableState
    var receiptPath by receiptPathState
    var splitMode by splitModeState
    var kind by kindState
    var amountText by amountTextState
    var date by dateState
    var description by descriptionState
    var accountId by accountIdState
    var fromAccountId by fromAccountIdState
    var toAccountId by toAccountIdState
    var category by categoryState
    var error by errorState
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
        // ⚠️ منطقِ ثبت قبلاً **دو بار** نوشته شده بود، یک‌بار در هر دکمه (سی خطِ یکسان).
        // یک اصلاح در یکی به دیگری نمی‌رسید. حالا یک تابعِ محلی، و هر دکمه فقط می‌گوید بعدش
        // چه کند.
        val submit: (andClose: Boolean) -> Unit = submit@{ andClose ->
            // فیلد تومان است، ستونِ دیتابیس ریال - تبدیل فقط همین‌جا، در لبه.
            val splitRows = if (splitMode && kind == NewTxKind.EXPENSE) {
                splits.mapNotNull { (c, a) -> a.toLongOrNull()?.takeIf { it > 0 }?.let { c to tomanToRial(it).toDouble() } }
            } else emptyList()
            val toman = amountText.toLongOrNull() ?: 0L
            val amount = if (splitRows.isNotEmpty()) splitRows.sumOf { it.second } else tomanToRial(toman).toDouble()
            error = validate(kind, amount, accountId, fromAccountId, toAccountId)
            // بازبینیِ ۹ مهر: جمعِ ردیف‌های تقسیم باید با مبلغِ کل یکی باشد، وگرنه بی‌صدا عددِ دیگری ثبت می‌شد.
            if (error == null && splitRows.isNotEmpty() && toman > 0 && tomanToRial(toman).toDouble() != amount) {
                error = "جمعِ ردیف‌های تقسیم با مبلغِ کل یکی نیست."
            }
            // «ثبت و بعدی» سقفِ ماهانه‌ی نسخه‌ی رایگان را دور می‌زد (قفل فقط موقعِ باز شدن بود).
            if (error == null && !txPremium && monthTxCount >= ir.sadteam.loancalc.ui.subscription.FreeLimits.TX_PER_MONTH) {
                ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("tx_month", "ثبتِ بیش از ${ir.sadteam.loancalc.core.toFa(ir.sadteam.loancalc.ui.subscription.FreeLimits.TX_PER_MONTH)} تراکنش در ماه")
                return@submit
            }
            val tags = tagsText.split('،', ',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",").ifBlank { null }
            if (error == null) {
                val onSaved = {
                    if (andClose) {
                        onDismiss()
                    } else {
                        // فقط مبلغ و توضیح پاک می‌شوند؛ حساب/تاریخ/دسته می‌مانند چون معمولاً
                        // تراکنش‌های پشتِ‌هم همان حساب و همان روزند.
                        amountText = ""
                        description = ""
                        receiptPath = null
                        splits.clear()
                        splitMode = false
                    }
                }
                // ثبتِ مالی نباید با یک exceptionِ پس‌زمینه‌ای برنامه را ببندد. تا وقتی DAO
                // موفق نشده، فرم باز می‌ماند؛ در خطا هم کاربر همان‌جا پیام می‌بیند و اطلاعاتش
                // را از دست نمی‌دهد.
                val onSaveFailure: (Throwable) -> Unit = {
                    error = "ثبت تراکنش ناموفق بود؛ دوباره تلاش کن."
                }
                if (kind == NewTxKind.TRANSFER) {
                    accountViewModel.addTransfer(
                        fromAccountId = fromAccountId!!,
                        toAccountId = toAccountId!!,
                        amount = amount,
                        description = description.trim(),
                        year = date.y,
                        month = date.m,
                        day = date.d,
                        onSuccess = onSaved,
                        onFailure = onSaveFailure,
                    )
                } else if (splitRows.isNotEmpty()) {
                    // هر ردیف یک تراکنش؛ شناسه‌ی صریح و پشتِ‌هم (قاعده‌ی حلقه در CLAUDE.md).
                    val base = System.currentTimeMillis()
                    splitRows.forEachIndexed { i, (c, a) ->
                        accountViewModel.addTransaction(
                            accountId = accountId!!,
                            type = TransactionType.WITHDRAWAL,
                            amount = a,
                            description = description.trim(),
                            year = date.y,
                            month = date.m,
                            day = date.d,
                            category = c ?: category,
                            sourceType = "split",
                            sourceId = base.toString(),
                            id = base + i,
                            receiptPath = receiptPath,
                            tags = tags,
                            reimbursable = reimbursable,
                            onSuccess = if (i == splitRows.lastIndex) onSaved else ({}),
                            onFailure = onSaveFailure,
                        )
                    }
                } else {
                    accountViewModel.addTransaction(
                        accountId = accountId!!,
                        type = if (kind == NewTxKind.INCOME) TransactionType.DEPOSIT else TransactionType.WITHDRAWAL,
                        amount = amount,
                        description = description.trim(),
                        year = date.y,
                        month = date.m,
                        day = date.d,
                        category = category,
                        receiptPath = receiptPath,
                        tags = tags,
                        reimbursable = reimbursable && kind == NewTxKind.EXPENSE,
                        onSuccess = onSaved,
                        onFailure = onSaveFailure,
                    )
                }
            }
        }

        AccentPillButton(
            text = "ثبت · ${toFa(GamificationRepository.Reward.DAILY_LOG)} سکه",
            accent = accent,
            onClick = { submit(true) },
            modifier = Modifier.weight(1f),
        )

            // «ثبت و بعدی» - ثبت می‌کند و فرم را برای تراکنشِ بعدی خالی نگه می‌دارد (قبلاً «+ باز»ِ نامفهوم).
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.5.dp, AppLine, RoundedCornerShape(999.dp))
                    .pressScaleClickable { submit(false) }
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("ثبت و بعدی", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
        }
}
