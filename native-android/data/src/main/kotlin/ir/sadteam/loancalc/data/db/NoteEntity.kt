package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** یادداشتِ مستقل (نه وابسته به یه وام/چکِ خاص - رجوع کن به CLAUDE.md، تبِ «سررسید»). */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val reminderDayOffsets: String?,
    val createdAt: String,
)
