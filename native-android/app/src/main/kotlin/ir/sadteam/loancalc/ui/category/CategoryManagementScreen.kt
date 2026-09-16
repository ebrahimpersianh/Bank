package ir.sadteam.loancalc.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.categoryIconChoices
import ir.sadteam.loancalc.data.db.CustomCategoryEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/** ذخیره و محاسبه ریال، نمایش تومان - قاعده‌ی واحدِ برنامه. */
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

private val categoryColorChoices = listOf(
    Color(0xFFE53935), Color(0xFFF4511E), Color(0xFFFB8C00), Color(0xFFF9A825),
    Color(0xFF43A047), Color(0xFF00897B), Color(0xFF00ACC1), Color(0xFF1E88E5),
    Color(0xFF3949AB), Color(0xFF8E24AA), Color(0xFFAD1457), Color(0xFF6D4C41),
    Color(0xFF757575),
)

/** مدیریتِ کاملِ دسته‌بندی‌ها - جابه‌جاییِ ترتیب (دکمه‌ی بالا/پایین، نه درگ - رجوع کن به CLAUDE.md
 * برای دلیلِ انتخابِ این روش) + افزودن/حذفِ دسته‌ی دلخواه. دسته‌های ثابتِ اپ قابلِ‌حذف نیستن. */
@Composable
fun CategoryManagementScreen(onBack: () -> Unit, viewModel: CategoryViewModel = hiltViewModel()) {
    var type by rememberSaveable { mutableStateOf(TransactionType.WITHDRAWAL) }
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val categories = if (type == TransactionType.WITHDRAWAL) expenseCategories else incomeCategories
    val custom by viewModel.customCategories.collectAsState()
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CustomCategoryEntity?>(null) }
    var renaming by remember { mutableStateOf<CustomCategoryEntity?>(null) }
    val monthTotals by viewModel.monthTotals.collectAsState()
    // مبلغِ ماهِ هر دسته مبلغ است، پس از حالتِ خصوصی عبور می‌کند.
    val privacyMode = LocalPrivacyMode.current

    // ⚠️ **خطرناک‌ترین کارِ این صفحه**: تراکنش‌های گذشته به دسته وصل‌ان. اگه دسته تراکنش
    // داشته باشه، دیالوگ **دسته‌ی مقصد می‌پرسه**، نه فقط تایید (قاعده‌ی صریحِ طراح) -
    // وگرنه اون تراکنش‌ها بی‌دسته می‌مونن و کاربر بعداً گمشون می‌کنه.
    renaming?.let { entity ->
        var childCount by remember(entity.name) { mutableStateOf(0) }
        var txCount by remember(entity.name) { mutableStateOf<Int?>(null) }
        LaunchedEffect(entity.name) {
            childCount = viewModel.childCountOf(entity)
            txCount = viewModel.transactionCount(entity.name)
        }
        RenameCategoryDialog(
            entity = entity,
            childCount = childCount,
            transactionCount = txCount,
            // فریمِ `74b`: حالا که تراکنش‌ها هم‌قدم مهاجرت می‌کنند، تکراری‌بودنِ نام تنها
            // چیزی است که می‌تواند کار را خراب کند - و ادغامِ بی‌اجازه‌ی دو دسته است.
            isTaken = { candidate -> viewModel.nameTaken(candidate, type, excluding = entity.name) },
            onRename = { newName -> viewModel.renameCustomCategory(entity, newName); renaming = null },
            onDismiss = { renaming = null },
        )
    }

    pendingDelete?.let { entity ->
        var count by remember(entity) { mutableStateOf<Int?>(null) }
        var target by remember(entity) { mutableStateOf<String?>(null) }
        LaunchedEffect(entity) { count = viewModel.transactionCount(entity.name) }
        val siblings = remember(entity, expenseCategories, incomeCategories) {
            val all = if (entity.type == TransactionType.WITHDRAWAL.name) expenseCategories else incomeCategories
            all.map { it.name }.filter { it != entity.name }
        }
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(
                    enabled = count != null && (count == 0 || target != null),
                    onClick = {
                        val chosen = target
                        if (count == 0 || chosen == null) {
                            viewModel.deleteCustomCategory(entity)
                        } else {
                            viewModel.reassignAndDelete(entity, chosen)
                        }
                        pendingDelete = null
                    },
                ) {
                    Text("حذف", color = AppDanger)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("بی‌خیال") } },
            title = { Text("حذفِ «${entity.name}»", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        when (count) {
                            null -> "دارم می‌شمرم..."
                            0 -> "این دسته هیچ تراکنشی نداره، پس بی‌دردسر حذف می‌شه."
                            else -> "${toFa(count ?: 0)} تراکنش این دسته رو دارن. اول باید بگی " +
                                "به کدوم دسته منتقل بشن."
                        },
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                    )
                    if ((count ?: 0) > 0) {
                        Text(
                            "منتقل شود به:",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                        )
                        Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                            siblings.forEach { name ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pressScaleClickable(scale = 0.99f) { target = name }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(selected = target == name, onClick = { target = name })
                                    Text(name, color = AppText, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(
                    "دسته‌بندی‌ها",
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                // دکمه‌ی + بالای صفحه (خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع) - قبلاً یه دکمه‌ی پهنِ
                // وسطِ لیست بود که هر بار باید تا بالای لیست اسکرول می‌کردی.
                IconButton(onClick = { showAddForm = !showAddForm }) {
                    Icon(
                        if (showAddForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "افزودنِ دسته‌بندی",
                        tint = AppPrimary,
                    )
                }
            }
        }
        item {
            // تبِ خرج/دخل با رنگِ متفاوت (خواسته‌ی صریحِ کاربر): خرج قرمز، دخل سبز. رنگِ تاگل با
            // خودِ تبِ فعال عوض می‌شه - همون الگوی شیتِ «تراکنش جدید».
            SegmentedToggle(
                options = listOf("خرج", "دخل"),
                selectedIndex = if (type == TransactionType.WITHDRAWAL) 0 else 1,
                onSelect = { type = if (it == 0) TransactionType.WITHDRAWAL else TransactionType.DEPOSIT },
                selectedColor = if (type == TransactionType.WITHDRAWAL) AppDanger else AppPrimary,
            )
        }
        if (showAddForm) {
            item {
                AddCategoryForm(
                    parentChoices = categories.map { it.name },
                    isTaken = { candidate -> viewModel.nameTaken(candidate, type) },
                    onCancel = { showAddForm = false },
                    onSubmit = { name, color, iconKey, parentName ->
                        viewModel.addCustomCategory(name, color, iconKey, type, parentName)
                        showAddForm = false
                    },
                )
            }
        }
        itemsIndexed(categories, key = { _, cat -> cat.name }) { index, cat ->
            val row = custom.firstOrNull { it.name == cat.name && it.type == type.name }
            val isCustom = row != null
            // زیرمجموعه‌ها (تسکِ #32) با یه تورفتگی و نامِ والد زیرشون نشون داده می‌شن - ساختارِ
            // خودِ لیست تخت می‌مونه تا جابه‌جایی/ترتیبِ دستیِ موجود دست‌نخورده کار کنه.
            val parentName = row?.parentName
            val isChild = !parentName.isNullOrBlank()
            AppCard(modifier = Modifier.animateItem()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // فریمِ `74a` بندِ ۱: تورفتگی + خطِ عمودیِ نازک جای خطِ «زیرمجموعه‌ی X».
                    //
                    // آن خط نامِ والد را در هر ردیف تکرار می‌کرد و **خطِ دومِ ردیف** را
                    // می‌گرفت - جایی که مبلغِ ماه باید باشد. و چون زیرمجموعه بلافاصله زیرِ
                    // والد است، تورفتگی همان را بی هیچ کلمه‌ای می‌گوید.
                    if (isChild) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(width = 2.dp, height = 22.dp)
                                .background(AppLineRow, CircleShape),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(cat.color.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(cat.name, color = AppText, fontSize = 13.sp)
                        // فریمِ `74a` بندِ ۳: مبلغِ ماهِ جاری - از `viewModel.monthTotals`.
                        //
                        // کارتِ راهنمای پایینِ صفحه از این عدد حرف می‌زد ولی هیچ ردیفی
                        // نشانش نمی‌داد.
                        monthTotals[cat.name]?.takeIf { it > 0 }?.let { total ->
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, amountToman(total))} تومان",
                                    color = AppMuted,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                    // دو تپِ جابه‌جایی از زیرمجموعه **حذف می‌شوند**: ترتیب داخلِ والد معنا
                    // دارد نه در فهرستِ تخت (`moveUp` روی فهرستِ تخت کار می‌کند و
                    // زیرمجموعه را از زیرِ والدش بیرون می‌برد)، و چهار آیکون در یک ردیف
                    // هدفِ لمسیِ ۴۴ را می‌شکند.
                    if (!isChild) {
                        IconButton(onClick = { viewModel.moveUp(type, cat.name) }, enabled = index > 0) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "جابه‌جایی به بالا", tint = if (index > 0) AppText else AppMuted)
                        }
                        IconButton(onClick = { viewModel.moveDown(type, cat.name) }, enabled = index < categories.lastIndex) {
                            Icon(
                                Icons.Filled.ArrowDownward,
                                contentDescription = "جابه‌جایی به پایین",
                                tint = if (index < categories.lastIndex) AppText else AppMuted,
                            )
                        }
                    }
                    if (isCustom) {
                        // تغییرِ نام تا حالا اصلاً راهی نداشت: تنها راهِ اصلاحِ یک غلطِ املایی
                        // حذف و ساختِ دوباره بود، که ترتیب و زیرمجموعه‌ها را هم می‌برد.
                        IconButton(onClick = { renaming = row }) {
                            Icon(Icons.Filled.Edit, contentDescription = "تغییرِ نام", tint = AppMuted)
                        }
                        IconButton(onClick = { pendingDelete = custom.first { it.name == cat.name && it.type == type.name } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                        }
                    }
                }
            }
        }
        item {
            // قاعده‌ی صریحِ فریمِ `36b` که باید به کاربر گفته بشه، وگرنه دنبالِ عمقِ دوم می‌گرده.
            AppCard(backgroundColor = AppSurface2, borderColor = AppLineRow, shadow = false) {
                Text(
                    "فقط یک پله عمق داریم - زیرمجموعه‌ی زیرمجموعه نمی‌شه ساخت. مبلغ‌های " +
                        "کنارِ هر دسته مالِ ماهِ جاری‌ان.",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun AddCategoryForm(
    parentChoices: List<String>,
    /** نامِ تکراری دو دسته‌ی هم‌نام می‌سازد و نام کلیدِ تراکنش‌هاست - فریمِ `74b`. */
    isTaken: (String) -> Boolean,
    onCancel: () -> Unit,
    onSubmit: (name: String, color: Color, iconKey: String, parentName: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(categoryColorChoices.first()) }
    var selectedIconKey by remember { mutableStateOf(categoryIconChoices.first().first) }
    // null یعنی «دسته‌ی سطحِ اول» - حالتِ پیش‌فرض و همون رفتارِ قبلی.
    var parentName by remember { mutableStateOf<String?>(null) }

    val taken = isTaken(name)
    AppCard(label = "دسته‌بندیِ جدید") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = taken,
            supportingText = if (taken) {
                { Text("دسته‌ای با این نام هست", color = AppDanger, fontSize = 11.sp) }
            } else {
                null
            },
        )
        Text("رنگ", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            items(categoryColorChoices) { color ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .pressScaleClickable { selectedColor = color }
                        .background(color, CircleShape)
                        .border(if (color == selectedColor) 2.dp else 0.dp, AppText, CircleShape),
                )
            }
        }
        Text("آیکون", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            items(categoryIconChoices) { (key, icon) ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .pressScaleClickable { selectedIconKey = key }
                        .background(
                            if (key == selectedIconKey) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape,
                        )
                        .border(if (key == selectedIconKey) 1.dp else 0.dp, selectedColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = if (key == selectedIconKey) selectedColor else AppMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
        Text("زیرمجموعه‌ی…", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            item {
                AppChip(
                    label = "بدونِ والد",
                    selected = parentName == null,
                    onClick = { parentName = null },
                )
            }
            items(parentChoices) { candidate ->
                AppChip(
                    label = candidate,
                    selected = parentName == candidate,
                    onClick = { parentName = if (parentName == candidate) null else candidate },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
            GradientButton(
                onClick = { if (name.isNotBlank() && !taken) onSubmit(name.trim(), selectedColor, selectedIconKey, parentName) },
                // دکمه‌ای که با تپ هیچ نمی‌کند، خودش باید خاموش باشد.
                enabled = name.isNotBlank() && !taken,
                modifier = Modifier.weight(1f),
            ) { Text("افزودن") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("انصراف") }
        }
    }
}

/**
 * تغییرِ نامِ دسته. زیرمجموعه‌ها را خودِ مخزن هم‌قدم به‌روز می‌کند، پس اینجا فقط نامِ تازه
 * گرفته می‌شود؛ ولی به کاربر گفته می‌شود که این کار زیرمجموعه‌ها را هم لمس می‌کند، وگرنه
 * تغییرِ بی‌صدای چند ردیفِ دیگر غافلگیرکننده است.
 */
@Composable
private fun RenameCategoryDialog(
    entity: CustomCategoryEntity,
    childCount: Int,
    /** `null` = هنوز شمرده نشده. */
    transactionCount: Int?,
    isTaken: (String) -> Boolean,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(entity.name) }
    val taken = isTaken(name)
    val valid = name.isNotBlank() && name.trim() != entity.name && !taken
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("تغییرِ نامِ دسته", color = AppText, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = taken,
                )
                if (taken) {
                    Text(
                        "دسته‌ای با این نام هست - دو دسته با هم ادغام می‌شن.",
                        color = AppDanger,
                        fontSize = 11.sp,
                    )
                }
                // فریمِ `74b`: دو خطِ خبر، نه هشدار. تا وقتی تراکنش‌ها مهاجرت نمی‌کردند
                // این کار واقعاً خراب می‌کرد و متن باید هشدار می‌داد؛ حالا که بی‌خطر شده،
                // متن باید **اطمینان** بدهد - وگرنه کاربر از کارِ درست می‌ترسد.
                if ((transactionCount ?: 0) > 0) {
                    Text(
                        "${toFa(transactionCount ?: 0)} تراکنش با نامِ تازه ذخیره می‌شن.",
                        color = AppMuted,
                        fontSize = 11.sp,
                    )
                }
                if (childCount > 0) {
                    Text(
                        "${toFa(childCount)} زیرمجموعه هم وصل می‌مونن.",
                        color = AppMuted,
                        fontSize = 11.sp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onRename(name.trim()) }, enabled = valid) {
                Text("ذخیره", color = if (valid) AppPrimaryInk else AppMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = AppMuted) } },
    )
}
