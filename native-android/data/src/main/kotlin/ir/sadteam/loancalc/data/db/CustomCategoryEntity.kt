package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** دسته‌بندیِ دلخواهِ کاربر (کنارِ لیستِ ثابتِ [ir.sadteam.loancalc.data.CategoryEntry] تو :app) -
 * رنگ به‌صورتِ ARGBِ خام و آیکون با یه کلیدِ ثابت (رجوع کن به CategoryIcons.kt تو :app) ذخیره
 * می‌شه چون Room نمی‌تونه Color/ImageVector رو مستقیم ذخیره کنه. */
@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val iconKey: String,
    val type: String,
)
