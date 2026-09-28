package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.NoteDao
import ir.sadteam.loancalc.data.db.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** یادداشتِ مستقل (نه وابسته به یه وام/چکِ خاص) - رجوع کن به CLAUDE.md، تبِ «سررسید». */
class NoteRepository(private val noteDao: NoteDao) {
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeAll()

    suspend fun addNote(text: String, year: Int, month: Int, day: Int, reminderDayOffsets: String?) {
        noteDao.upsert(
            NoteEntity(
                id = System.currentTimeMillis(),
                text = text,
                year = year,
                month = month,
                day = day,
                reminderDayOffsets = reminderDayOffsets,
                createdAt = isoNow(),
            ),
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.upsert(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

    suspend fun clearLocal() {
        noteDao.clear()
    }

    private fun isoNow(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}
