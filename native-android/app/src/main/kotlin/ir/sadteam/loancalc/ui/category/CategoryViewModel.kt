package ir.sadteam.loancalc.ui.category

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.CategoryRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.db.CustomCategoryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** لیستِ نهاییِ (ثابت + دلخواهِ کاربر، با ترتیبِ دلخواه) دسته‌بندی‌ها - رجوع کن به
 * [CategoryRepository]/CLAUDE.md، مدیریتِ کاملِ دسته‌بندی‌ها. */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val parsingRuleRepository: ParsingRuleRepository,
) : ViewModel() {
    val expenseCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.WITHDRAWAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<CategoryEntry>> = categoryRepository.orderedCategories(TransactionType.DEPOSIT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCategories: StateFlow<List<CustomCategoryEntity>> = categoryRepository.observeCustomCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * جمعِ **ماهِ جاری** به ازای نامِ هر دسته، ریال - فریمِ `74a` بندِ ۳.
     *
     * کارتِ راهنمای پایینِ صفحه از این عدد حرف می‌زد ولی هیچ ردیفی نشانش نمی‌داد، و این
     * تنها عددی است که مدیریتِ دسته را از یک فهرستِ اسم به یک ابزار تبدیل می‌کند: کاربر
     * می‌خواهد بداند کدام دسته‌ی دلخواهش واقعاً استفاده می‌شود.
     *
     * ⚠️ زیرمجموعه‌ها **جمع نمی‌شوند** در والد. گزارش‌ها این کار را می‌کنند (`36b`) ولی
     * این‌جا نه: ردیفِ والد و ردیفِ زیرمجموعه پشتِ‌هم‌اند، و اگر والد جمعِ خودش+بچه‌ها را
     * نشان بدهد، دو عدد جلوی چشم کاربر جمع نمی‌خوانند.
     */
    val monthTotals: StateFlow<Map<String, Double>> = accountRepository.observeTransactions()
        .map { all ->
            val today = JalaliCalendar.today()
            all.asSequence()
                .filter { it.year == today.y && it.month == today.m }
                .groupBy { it.category }
                .mapValues { (_, rows) -> rows.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    /**
     * تغییرِ نام. زیرمجموعه‌ها را مخزن هم‌قدم به‌روز می‌کند، و **تراکنش‌ها را این‌جا**.
     *
     * 🚨 باگی که رفع شد: `renameCustomCategory`ِ مخزن فقط ردیفِ دسته و `parentName`ِ
     * بچه‌ها را عوض می‌کند و به تراکنش‌ها دست نمی‌زند. ولی
     * `AccountTransactionEntity.category` خودِ **نام** را ذخیره می‌کند - پس هر تغییرِ نام
     * تمامِ تراکنش‌های گذشته‌ی آن دسته را **بی‌دسته** می‌کرد.
     *
     * یعنی کاربری که یک غلطِ املایی را اصلاح می‌کرد، بی هیچ هشداری گزارشِ چند ماهش را
     * خالی می‌کرد. `deleteCustomCategory` برای همین خطر یک دیالوگِ کاملِ «به کدام دسته
     * منتقل شود» دارد؛ تغییرِ نام همان عمل روی همان کلید است و هیچ‌چیز نداشت.
     *
     * ترتیب مهم است: **اول** تراکنش‌ها، بعد خودِ دسته. برعکسش اگر بین دو مرحله چیزی
     * بشکند، دسته‌ی تازه بی تراکنش می‌مانَد و تراکنش‌ها به نامی اشاره می‌کنند که نیست.
     */
    fun renameCustomCategory(entity: CustomCategoryEntity, newName: String) {
        viewModelScope.launch {
            // ⚠️ رفعِ طراح فقط تراکنش‌ها را می‌گرفت. همان ریشه **سه جای دیگر** هم دارد و
            // هر سه بی‌صدا خراب می‌کنند: بودجه (سقف دیگر هیچ خرجی نمی‌شمارد و کاربر فکر
            // می‌کند رعایتش کرده)، پرداختِ تکراری، و قاعده‌ی تشخیصِ پیامک (تراکنشِ خودکارِ
            // بعدی بی‌دسته می‌نشیند). رجوع کن به `renameCategoryEverywhere`.
            accountRepository.renameCategoryEverywhere(entity.name, newName)
            parsingRuleRepository.getRules()
                .filter { it.category == entity.name }
                .forEach { parsingRuleRepository.save(it.copy(category = newName)) }
            categoryRepository.renameCustomCategory(entity, newName)
        }
    }

    /**
     * آیا دسته‌ای با این نام از قبل هست؟ - فرمِ ساخت و دیالوگِ تغییرِ نام هر دو باید
     * بپرسند.
     *
     * 🚨 هیچ‌کدام نمی‌پرسیدند. چون **نام کلیدِ تراکنش‌هاست**، دو دسته‌ی هم‌نام یعنی
     * تراکنش‌هایشان قابلِ تفکیک نیست و در دونات یکی می‌شوند؛ و تغییرِ نام به نامِ یک
     * دسته‌ی موجود، دو دسته را **بی اجازه ادغام** می‌کند.
     *
     * دسته‌های ثابتِ برنامه هم حساب می‌شوند (`categoriesFor`)، نه فقط ردیف‌های دلخواه -
     * وگرنه کاربر می‌تواند دسته‌ای هم‌نامِ «خوراک»ِ ثابت بسازد.
     */
    fun nameTaken(name: String, type: TransactionType, excluding: String? = null): Boolean {
        val target = name.trim()
        if (target.isBlank()) return false
        return categoriesFor(type).any { it.name != excluding && it.name.equals(target, ignoreCase = true) }
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
