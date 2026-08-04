package ir.sadteam.loancalc.data.db

import androidx.room.Entity

/** ترتیبِ دلخواهِ کاربر برای دسته‌بندی‌ها (چه ثابت چه دلخواه) - با [type]+[name] شناسایی می‌شه، نه
 * idِ عددی، چون دسته‌های ثابت اصلاً ردیفِ دیتابیس ندارن. فقط دسته‌هایی که کاربر واقعاً جابه‌جاشون
 * کرده اینجا ردیف دارن؛ بقیه با ترتیبِ پیش‌فرضِ لیستِ ثابت (و بعدش دسته‌های دلخواهِ بدونِ ترتیب،
 * به‌ترتیبِ ساخت) نمایش داده می‌شن - رجوع کن به CategoryRepository.orderedCategories. */
@Entity(tableName = "category_order", primaryKeys = ["type", "name"])
data class CategoryOrderEntity(
    val type: String,
    val name: String,
    val sortOrder: Int,
)
