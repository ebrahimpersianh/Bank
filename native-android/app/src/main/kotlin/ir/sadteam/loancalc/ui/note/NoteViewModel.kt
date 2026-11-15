package ir.sadteam.loancalc.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.NoteRepository
import ir.sadteam.loancalc.data.db.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val attachmentStorage: ir.sadteam.loancalc.data.AttachmentStorage,
) : ViewModel() {
    /** عکس همان لحظه‌ی انتخاب به حافظه‌ی داخلی کپی می‌شود (Uriِ گالری موقتی است). */
    fun copyPhoto(uri: android.net.Uri, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val path = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { attachmentStorage.copyToInternalStorage(uri) }
            onDone(path)
        }
    }

    fun discardPhoto(path: String?) = attachmentStorage.delete(path)

    val notes: StateFlow<List<NoteEntity>> = noteRepository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(text: String, year: Int, month: Int, day: Int, reminderDayOffsets: String?, photoPath: String? = null) {
        ir.sadteam.loancalc.data.UsageStats.action("note_added")
        viewModelScope.launch { noteRepository.addNote(text, year, month, day, reminderDayOffsets, photoPath) }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.updateNote(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.deleteNote(note) }
        attachmentStorage.delete(note.photoPath)
    }
}
