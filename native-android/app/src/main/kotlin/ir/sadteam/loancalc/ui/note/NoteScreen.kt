package ir.sadteam.loancalc.ui.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.TextButton
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppPrimary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.jibak.faMonthName
import ir.sadteam.loancalc.data.db.NoteEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/** یادداشتِ مستقل (نه وابسته به یه وام/چکِ خاص) - رجوع کن به تبِ «سررسید» تو CLAUDE.md. */
@Composable
fun NoteScreen(onBack: () -> Unit, viewModel: NoteViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    val today = remember { JalaliCalendar.today() }
    var year by rememberSaveable { mutableIntStateOf(today.y) }
    var month by rememberSaveable { mutableIntStateOf(today.m) }
    var day by rememberSaveable { mutableIntStateOf(today.d) }
    var pendingDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visibleNotes = remember(notes, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) notes else notes.filter { it.text.contains(q, ignoreCase = true) }
    }

    pendingDelete?.let { note ->
        ConfirmDeleteDialog(
            title = "حذفِ یادداشت",
            text = "این یادداشت حذف بشه؟",
            onConfirm = { viewModel.deleteNote(note) },
            onDismiss = { pendingDelete = null },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                Text("یادداشت‌ها", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
        // فیلدِ جست‌وجو از سه یادداشت به بالا می‌آید. با یک یادداشت، فیلد از خودِ لیست
        // بلندتر است.
        if (notes.size >= 3) {
            item {
                ir.sadteam.loancalc.ui.components.PillSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "جست‌وجو در یادداشت‌ها",
                )
            }
        }
        item {
            if (showAdd) {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(NoteAmber.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.EditNote, contentDescription = null, tint = NoteAmber, modifier = Modifier.size(22.dp)) }
                        Text("یادداشتِ تازه", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 12.dp))
                    }
                    // عنوان ستونِ جدا در دیتابیس ندارد: خطِ اولِ `text` است (همان چیزی که
                    // جستجوی کلی و فهرست هم عنوان حساب می‌کنند) - بی migration.
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.replace("\n", " ") },
                        label = { Text("عنوان") },
                        placeholder = { Text("مثلاً اجاره‌ی خونه", color = AppMuted) },
                        singleLine = true,
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = appFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("توضیح (اختیاری)") },
                        minLines = 3,
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        colors = appFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                        Text("تاریخِ یادآوری", color = AppLabel, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                    }
                    InlineJalaliDateRow(
                        year = year,
                        month = month,
                        day = day,
                        onDateChange = { y, m, d -> year = y; month = m; day = d },
                    )
                    val canSave = title.isNotBlank() || text.isNotBlank()
                    GradientButton(
                        onClick = {
                            if (canSave) {
                                val full = listOf(title.trim(), text.trim()).filter { it.isNotEmpty() }.joinToString("\n")
                                viewModel.addNote(full, year, month, day, null)
                                title = ""
                                text = ""
                                showAdd = false
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    ) { Text("ثبتِ یادداشت") }
                    // ⚠️ کارتِ «یادداشتِ جدید» راهِ بستن نداشت - «بی‌خیال» ماند، ولی کم‌رنگ‌تر.
                    TextButton(
                        onClick = { title = ""; text = ""; showAdd = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("بی‌خیال", color = AppMuted, fontWeight = FontWeight.Bold) }
                }
            } else if (notes.isNotEmpty()) {
                // وقتی خالی است، دکمه داخلِ کارتِ خالی است (هم‌سبکِ Claude Design).
                GradientButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ افزودنِ یادداشت")
                }
            }
        }
        if (notes.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.EditNote,
                    title = "هنوز یادداشتی نداری",
                    description = "پرداخت‌های مهم مثلِ اجاره و قسط رو یادداشت کن تا سرِ موعد یادآوری کنیم.",
                    actionLabel = if (showAdd) null else "+ افزودنِ یادداشت",
                    onAction = { showAdd = true },
                )
            }
        } else if (visibleNotes.isEmpty()) {
            // 🚨 حالتِ «جست‌وجو نتیجه نداشت» **وجود نداشت**، چون شرطِ بالا `notes.isEmpty()`
            // است نه `visibleNotes`. کاربری که واژه‌ای می‌نوشت که هیچ یادداشتی نداشت،
            // لیستِ کاملِ یادداشت‌ها را می‌دید - یعنی فکر می‌کرد جست‌وجو کار نمی‌کند (و
            // درست فکر می‌کرد، رجوع کن به بندِ بعدی).
            item {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = "چیزی پیدا نشد",
                    description = "یادداشتی با «${searchQuery.trim()}» نبود.",
                )
            }
        } else {
            // 🚨 **باگِ اصلیِ این صفحه**: این‌جا `notes` بود نه `visibleNotes`.
            // `visibleNotes` محاسبه می‌شد و **هیچ‌جا استفاده نمی‌شد**، پس فیلدِ جست‌وجو
            // متن می‌گرفت، فیلتر واقعاً حساب می‌شد، و لیست همان لیستِ کامل می‌ماند.
            items(visibleNotes, key = { it.id }) { note ->
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // کاشیِ آیکونِ کهربایی (هم‌سبکِ Claude Design).
                        Box(
                            Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(NoteAmber.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.EditNote, contentDescription = null, tint = NoteAmber, modifier = Modifier.size(22.dp)) }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            val noteTitle = note.text.lineSequence().firstOrNull().orEmpty()
                            val noteBody = note.text.substringAfter('\n', "").trim()
                            Text(noteTitle, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            if (noteBody.isNotEmpty()) {
                                Text(noteBody, color = AppMuted, fontSize = 12.5.sp, lineHeight = 20.sp, maxLines = 3, modifier = Modifier.padding(top = 3.dp))
                            }
                            // ⚠️ قبلاً `۱۴۰۵/۷/۹` بود: قالبِ پنجم، بی صفرِ ابتدایی و
                            // بیرونِ چهار قالبِ مصوب. `۹ مهر ۱۴۰۵` همان قالبی است که
                            // ردیفِ سررسید و تقویمِ مالی هم می‌نویسند.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(NoteAmber.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Icon(Icons.Filled.Event, contentDescription = null, tint = NoteAmber, modifier = Modifier.size(13.dp))
                                Text(
                                    "${toFa(note.day)} ${faMonthName(note.month)} ${toFa(note.year)}",
                                    color = NoteAmber,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp),
                                )
                            }
                        }
                        IconButton(onClick = { pendingDelete = note }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
                        }
                    }
                }
            }
        }
    }
}

private val NoteAmber = androidx.compose.ui.graphics.Color(0xFFB7791F)
