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
) : ViewModel() {
    val notes: StateFlow<List<NoteEntity>> = noteRepository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(text: String, year: Int, month: Int, day: Int, reminderDayOffsets: String?) {
        viewModelScope.launch { noteRepository.addNote(text, year, month, day, reminderDayOffsets) }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.updateNote(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.deleteNote(note) }
    }
}
