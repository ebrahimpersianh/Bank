package ir.sadteam.loancalc.ui.note

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.NoteEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/** یادداشتِ مستقل (نه وابسته به یه وام/چکِ خاص) - رجوع کن به تبِ «سررسید» تو CLAUDE.md. */
@Composable
fun NoteScreen(onBack: () -> Unit, viewModel: NoteViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val today = remember { JalaliCalendar.today() }
    var year by rememberSaveable { mutableStateOf(today.y) }
    var month by rememberSaveable { mutableStateOf(today.m) }
    var day by rememberSaveable { mutableStateOf(today.d) }
    var pendingDelete by remember { mutableStateOf<NoteEntity?>(null) }

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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("یادداشت‌ها", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            if (showAdd) {
                AppCard(label = "یادداشتِ جدید") {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("متن") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    InlineJalaliDateRow(
                        year = year,
                        month = month,
                        day = day,
                        onDateChange = { y, m, d -> year = y; month = m; day = d },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    GradientButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                viewModel.addNote(text.trim(), year, month, day, null)
                                text = ""
                                showAdd = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) { Text("ثبت") }
                }
            } else {
                GradientButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("افزودنِ یادداشت")
                }
            }
        }
        if (notes.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.EditNote,
                    title = "هنوز یادداشتی نداری",
                    description = "پرداخت‌های مهم مثلِ اجاره و قسط رو یادداشت کن تا سرِ موعد یادآوری کنیم.",
                )
            }
        } else {
            items(notes, key = { it.id }) { note ->
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.text, color = AppText, fontSize = 14.sp)
                            Text(
                                "${toFa(note.year)}/${toFa(note.month)}/${toFa(note.day)}",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
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
