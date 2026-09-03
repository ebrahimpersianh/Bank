package ir.sadteam.loancalc.ui.category

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.CategoryRepository
import ir.sadteam.loancalc.data.db.CustomCategoryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** لیستِ نهاییِ (ثابت + دلخواهِ کاربر، با ترتیبِ دلخواه) دسته‌بندی‌ها - رجوع کن به
 * [CategoryRepository]/CLAUDE.md، مدیریتِ کاملِ دسته‌بندی‌ها. */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val expenseCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.WITHDRAWAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.DEPOSIT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCategories: StateFlow<List<CustomCategoryEntity>> = categoryRepository.observeCustomCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun categoriesFor(type: TransactionType): List<CategoryEntry> =
        if (type == TransactionType.DEPOSIT) incomeCategories.value else expenseCategories.value

    fun addCustomCategory(
        name: String,
        color: Color,
        iconKey: String,
        type: TransactionType,
        parentName: String? = null,
    ) {
        viewModelScope.launch { categoryRepository.addCustomCategory(name, color, iconKey, type, parentName) }
    }

    /** چند زیرمجموعه به این دسته وصل‌اند - متنِ دیالوگِ تایید باید بگوید. */
    suspend fun childCountOf(entity: CustomCategoryEntity): Int =
        categoryRepository.childCountOf(entity)

    /** تغییرِ نام؛ زیرمجموعه‌ها هم‌قدم به‌روز می‌شوند (رجوع کن به مخزن). */
    fun renameCustomCategory(entity: CustomCategoryEntity, newName: String) {
        viewModelScope.launch { categoryRepository.renameCustomCategory(entity, newName) }
    }

    fun deleteCustomCategory(entity: CustomCategoryEntity) {
        viewModelScope.launch { categoryRepository.deleteCustomCategory(entity) }
    }

    /** چند تراکنشِ ثبت‌شده به این دسته وصل‌ان؟ - قبل از حذف باید به کاربر گفته بشه. */
    suspend fun transactionCount(categoryName: String): Int =
        accountRepository.observeTransactions().first().count { it.category == categoryName }

    /**
     * حذفِ دسته‌ی **باتراکنش**: اول همه‌ی تراکنش‌هاش به [target] منتقل می‌شن، بعد خودِ دسته
     * حذف می‌شه. قاعده‌ی صریحِ طراح: «دیالوگ باید دسته‌ی مقصد بپرسه، نه فقط تایید» -
     * وگرنه تراکنش‌های گذشته بی‌دسته می‌مونن.
     */
    fun reassignAndDelete(entity: CustomCategoryEntity, target: String) {
        viewModelScope.launch {
            accountRepository.observeTransactions().first()
                .filter { it.category == entity.name }
                .forEach { accountRepository.updateTransaction(it.copy(category = target)) }
            categoryRepository.deleteCustomCategory(entity)
        }
    }

    fun saveOrder(type: TransactionType, orderedNames: List<String>) {
        viewModelScope.launch { categoryRepository.saveOrder(type, orderedNames) }
    }

    fun moveUp(type: TransactionType, name: String) = reorder(type, name, -1)
    fun moveDown(type: TransactionType, name: String) = reorder(type, name, 1)

    private fun reorder(type: TransactionType, name: String, delta: Int) {
        val current = categoriesFor(type).map { it.name }.toMutableList()
        val index = current.indexOf(name)
        val target = index + delta
        if (index < 0 || target < 0 || target >= current.size) return
        current[index] = current[target].also { current[target] = current[index] }
        saveOrder(type, current)
    }
}
