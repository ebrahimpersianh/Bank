package ir.sadteam.loancalc.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.categoryIconChoices
import ir.sadteam.loancalc.data.db.CustomCategoryEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

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

    pendingDelete?.let { entity ->
        ConfirmDeleteDialog(
            title = "حذفِ دسته‌بندی",
            text = "«${entity.name}» حذف بشه؟ تراکنش‌های قبلی که این دسته رو دارن، دسته‌شون فقط از لیست خارج می‌شه.",
            onConfirm = { viewModel.deleteCustomCategory(entity) },
            onDismiss = { pendingDelete = null },
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
            AppCard(modifier = Modifier.animateItem()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                        if (!parentName.isNullOrBlank()) {
                            Text("زیرمجموعه‌ی $parentName", color = AppMuted, fontSize = 10.5.sp)
                        }
                    }
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
                    if (isCustom) {
                        IconButton(onClick = { pendingDelete = custom.first { it.name == cat.name && it.type == type.name } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryForm(
    parentChoices: List<String>,
    onCancel: () -> Unit,
    onSubmit: (name: String, color: Color, iconKey: String, parentName: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(categoryColorChoices.first()) }
    var selectedIconKey by remember { mutableStateOf(categoryIconChoices.first().first) }
    // null یعنی «دسته‌ی سطحِ اول» - حالتِ پیش‌فرض و همون رفتارِ قبلی.
    var parentName by remember { mutableStateOf<String?>(null) }

    AppCard(label = "دسته‌بندیِ جدید") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
                onClick = { if (name.isNotBlank()) onSubmit(name.trim(), selectedColor, selectedIconKey, parentName) },
                modifier = Modifier.weight(1f),
            ) { Text("افزودن") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("انصراف") }
        }
    }
}
