package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.db.CounterpartyEntity
import ir.sadteam.loancalc.ui.text.Text
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * انتخاب/ساختِ دوست - جوابِ سوالِ ۶ی `design/ANSWERS-chequecounterpartydang.md` (انتخاب اجباری؛
 * بستن = منصرف شدن از کلِ فرم، تصمیمش با فراخوان).
 *
 * 🚨 یافته‌ی ۴۸ (کاربر: «اصلاً خوشم نمیاد»): پنجره‌ی شناورِ کوچک وسطِ صفحه بود، تا تایپ نمی‌کردی
 * دوستی نشان نمی‌داد، واژه‌ی حسابداریِ «طرف‌حساب» در فرمی که خودش «دوست» می‌گوید، و فقط یک
 * «انصراف»ِ کم‌رنگ. حالا **برگه‌ی پایین**: دوستانِ اخیر از همان اول (دایره‌ی حرفِ اول)، جستجو،
 * و ردیفِ سبزِ «+ افزودنِ …» که با اولین حرف بالای فهرست می‌آید. بستن = کشیدن به پایین / برگشت.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterpartyPickerDialog(
    counterparties: List<CounterpartyEntity>,
    onSelect: (CounterpartyEntity) -> Unit,
    onCreateNew: (name: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "انتخابِ دوست",
) {
    var query by remember { mutableStateOf("") }
    // جدیدترین بالا - تاریخِ استفاده نداریم؛ تازه‌ساخته‌ها همان‌هایی‌اند که الان به کار می‌آیند.
    val filtered = remember(query, counterparties) {
        val sorted = counterparties.sortedByDescending { it.id }
        if (query.isBlank()) sorted else sorted.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    val exactMatch = counterparties.any { it.name.trim() == query.trim() }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = AppSurface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding()) {
            Text(title, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(
                "از فهرست انتخاب کن یا اسمِ تازه بنویس",
                color = AppMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            PillSearchField(value = query, onValueChange = { query = it }, placeholder = "جستجو یا اسمِ تازه")

            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(top = 8.dp, bottom = 12.dp)) {
                if (query.isNotBlank() && !exactMatch) {
                    item(key = "new") {
                        PickerRow(onClick = { onCreateNew(query.trim()) }) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(AppPrimary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = AppSurface, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                "افزودنِ «${query.trim()}»",
                                color = AppPrimaryInk,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                items(filtered, key = { it.id }) { counterparty ->
                    PickerRow(onClick = { onSelect(counterparty) }) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AppPrimaryPill),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                counterparty.name.trim().take(1).ifEmpty { "؟" },
                                color = AppPrimaryInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Text(
                            counterparty.name,
                            color = AppText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (filtered.isEmpty() && query.isBlank()) {
                    item(key = "empty") {
                        Text(
                            "هنوز کسی را اضافه نکردی - اسمش را بالا بنویس.",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerRow(onClick: () -> Unit, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .pressScaleClickable(scale = 0.99f, onClick = onClick)
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
        Box(Modifier.fillMaxWidth().padding(start = 56.dp).height(1.dp).background(AppLineRow))
    }
}
