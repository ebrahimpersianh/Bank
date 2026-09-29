package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.creditServices
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * انتخابِ بانک - همه‌جا یکی (خواسته‌ی کاربر، ۷ مهر): اسمِ بانک تایپ نمی‌شود، از فهرستِ
 * جستجودار انتخاب می‌شود و لوگوی رسمیِ همان بانک ([BankBadge]) کنارش می‌آید.
 * اگر اسمِ جستجوشده در فهرست نبود (مؤسسه‌ی کوچک)، ردیفِ «استفاده از «…»» آن را می‌پذیرد.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    includeCreditServices: Boolean = false,
    placeholder: String = "انتخابِ بانک",
) {
    var open by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AppLine, RoundedCornerShape(14.dp))
            .clickable { open = true }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        BankBadge(bankName = value, size = 38.dp)
        Text(
            value.ifBlank { placeholder },
            color = if (value.isBlank()) AppMuted else AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted)
    }

    if (open) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var query by remember { mutableStateOf("") }
        val all = remember(includeCreditServices) { if (includeCreditServices) banks + creditServices else banks }
        val shown = all.filter { query.isBlank() || it.name.contains(query.trim()) }
        ModalBottomSheet(onDismissRequest = { open = false }, sheetState = sheetState, containerColor = AppSurface) {
            Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp)) {
                Text("انتخابِ بانک", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("جستجوی بانک") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 460.dp)) {
                    items(shown, key = { it.name }) { b ->
                        BankRow(b.name, selected = b.name == value) { onValueChange(b.name); open = false }
                    }
                    val custom = query.trim()
                    if (custom.isNotEmpty() && all.none { it.name == custom }) {
                        item(key = "custom") {
                            BankRow("استفاده از «$custom»", selected = false, badgeName = custom) { onValueChange(custom); open = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankRow(label: String, selected: Boolean, badgeName: String = label, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        BankBadge(bankName = badgeName, size = 36.dp)
        Text(label, color = AppText, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Filled.Check, contentDescription = "انتخاب‌شده", tint = AppPrimary, modifier = Modifier.size(20.dp))
    }
}
