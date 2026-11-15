package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * **نشان‌ها (بج)** - کارت‌های دستاوردِ فایلِ طراحی.
 *
 * فقط نشانِ **بازشده** ردیف می‌گیره؛ لیستِ کاملِ نشان‌ها و شرط‌هاشون تو کد (`Achievement`)
 * تعریف شده نه تو دیتابیس، تا اضافه‌کردنِ نشانِ تازه مایگریشن نخواد.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val code: String,
    val unlockedAt: Long,
)
