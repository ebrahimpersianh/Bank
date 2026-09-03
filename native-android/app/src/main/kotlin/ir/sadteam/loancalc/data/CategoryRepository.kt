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

    /** [parentName] اگه پر باشه یعنی این یه **زیرمجموعه**ی همون دسته‌ست (تسکِ #32). والد می‌تونه
     * هم یه دسته‌ی ثابتِ اپ باشه هم یه دسته‌ی دلخواهِ دیگه - چون کلیدِ هر دو «نام»ه. */
    suspend fun addCustomCategory(
        name: String,
        color: Color,
        iconKey: String,
        type: TransactionType,
        parentName: String? = null,
    ) {
        categoryDao.insertCustom(
            CustomCategoryEntity(
                name = name,
                colorArgb = color.toArgb(),
                iconKey = iconKey,
                type = type.name,
                parentName = parentName,
            ),
        )
    }

    /** چند زیرمجموعه به این دسته وصل‌اند - برای متنِ دیالوگِ تایید. */
    suspend fun childCountOf(entity: CustomCategoryEntity): Int =
        categoryDao.childCountOf(entity.type, entity.name)

    /**
     * تغییرِ نامِ یک دسته. **زیرمجموعه‌ها هم‌قدم به‌روز می‌شوند** چون `parentName` نام است
     * نه id؛ بی این، بچه‌ها به نامی وصل می‌مانند که دیگر وجود ندارد و نامرئی می‌شوند.
     *
     * ترتیب مهم است: اول بچه‌ها، بعد خودِ ردیف. اگر برعکس باشد و کارِ دوم شکست بخورد،
     * بچه‌ها به نامِ تازه‌ای اشاره می‌کنند که هنوز ثبت نشده.
     */
    suspend fun renameCustomCategory(entity: CustomCategoryEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == entity.name) return
        categoryDao.renameParentOfChildren(entity.type, entity.name, trimmed)
        categoryDao.updateCustom(entity.copy(name = trimmed))
        categoryDao.deleteOrder(entity.type, entity.name)
    }

    /** حذف: زیرمجموعه‌ها اول **ترفیع** می‌گیرند تا یتیمِ نامرئی نشوند. */
    suspend fun deleteCustomCategory(entity: CustomCategoryEntity) {
        categoryDao.promoteChildrenOf(entity.type, entity.name)
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
