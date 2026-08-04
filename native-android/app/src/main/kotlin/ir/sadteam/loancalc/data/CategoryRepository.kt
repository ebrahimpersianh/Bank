package ir.sadteam.loancalc.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.db.CategoryDao
import ir.sadteam.loancalc.data.db.CategoryOrderEntity
import ir.sadteam.loancalc.data.db.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * ترکیبِ لیستِ ثابتِ دسته‌بندی‌ها ([expenseCategories]/[incomeCategories]) با دسته‌های دلخواهِ
 * کاربر ([CustomCategoryEntity]) و ترتیبِ دلخواهِ جابه‌جاشده ([CategoryOrderEntity]) - رجوع کن به
 * CLAUDE.md، مدیریتِ کاملِ دسته‌بندی‌ها. دسته‌هایی که کاربر ترتیبشون رو دستکاری نکرده، با همون
 * ترتیبِ پیش‌فرض (ثابت‌ها اول، بعد دلخواه‌ها به‌ترتیبِ ساخت) نمایش داده می‌شن.
 */
class CategoryRepository(private val categoryDao: CategoryDao) {
    fun orderedCategories(type: TransactionType): Flow<List<CategoryEntry>> {
        val typeName = type.name
        return combine(categoryDao.observeCustom(), categoryDao.observeOrder()) { custom, order ->
            val staticList = categoriesFor(type)
            val customForType = custom.filter { it.type == typeName }
                .map { CategoryEntry(it.name, Color(it.colorArgb), iconForKey(it.iconKey), type) }
            val combined = staticList + customForType
            val orderMap = order.filter { it.type == typeName }.associate { it.name to it.sortOrder }
            combined.sortedWith(
                compareBy(
                    { orderMap[it.name] ?: Int.MAX_VALUE },
                    { combined.indexOf(it) },
                ),
            )
        }
    }

    fun observeCustomCategories(): Flow<List<CustomCategoryEntity>> = categoryDao.observeCustom()

    suspend fun addCustomCategory(name: String, color: Color, iconKey: String, type: TransactionType) {
        categoryDao.insertCustom(
            CustomCategoryEntity(name = name, colorArgb = color.toArgb(), iconKey = iconKey, type = type.name),
        )
    }

    suspend fun deleteCustomCategory(entity: CustomCategoryEntity) {
        categoryDao.deleteCustom(entity)
        categoryDao.deleteOrder(entity.type, entity.name)
    }

    /** ترتیبِ جدید رو برای یه نوع (هزینه/درآمد) ذخیره می‌کنه - [orderedNames] دقیقاً به‌همون
     * ترتیبی‌یه که کاربر تو UI جابه‌جا کرده. */
    suspend fun saveOrder(type: TransactionType, orderedNames: List<String>) {
        val entries = orderedNames.mapIndexed { index, name -> CategoryOrderEntity(type.name, name, index) }
        categoryDao.upsertOrder(entries)
    }
}
