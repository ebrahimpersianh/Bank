package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * **هدفِ پس‌انداز** - «چقدر جمع کرده‌ام برای چه».
 *
 * 🚨 دو تصمیمِ بنیادی که نباید بی دلیل عوض شوند:
 *
 * ۱. **هدف پولِ تازه نمی‌سازد و پولی جابه‌جا نمی‌کند.** یک برچسب روی پولی است که از قبل
 *    در حساب‌کتاب‌هایت هست. پس واریز به هدف **تراکنشِ حسابداری نمی‌سازد** - وگرنه همان
 *    باگِ «دوباره‌حسابی» تکرار می‌شود که یک‌بار موجودیِ منفیِ ۵ میلیاردی ساخت.
 *    (`MIGRATION_17_18` را ببین.)
 * ۲. **[savedRial] تنها شمارنده‌ی ذخیره‌شده‌ی این فیچر است و فقط از یک جا نوشته می‌شود**
 *    (`SavingsGoalRepository.contribute`). ریزتراکنش‌های هدف جدولِ جدا ندارند - اگر روزی
 *    تاریخچه خواستی، آن جدول باید منبعِ حقیقت شود و این ستون **مشتق**، نه هر دو با هم.
 *    (درسِ `paidCount`: یک عدد، دو منبعِ حقیقت = فاجعه.)
 *
 * مبلغ‌ها **ریال**اند مثلِ کلِ دیتابیس؛ تبدیل به تومان فقط روی لبه‌ی UI.
 */
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val targetRial: Double,
    val savedRial: Double = 0.0,
    /** نمادِ کارت - کلیدِ همان جدولِ `CategoryIcons`، پس با ستِ نمادِ خریداری‌شده هم‌قدم می‌شود. */
    val iconKey: String = "star",
    /** سررسیدِ اختیاریِ جلالی. هر سه با هم `null`اند یا هر سه پرند. */
    val deadlineYear: Int? = null,
    val deadlineMonth: Int? = null,
    val deadlineDay: Int? = null,
    /** تاریخِ رسیدن (ISO). `null` یعنی هنوز نرسیده - نشانِ `goal_reached` از همین می‌آید. */
    val achievedAt: String? = null,
    val createdAt: String,
) {
    /** `0f`..`1f`. هدفِ با مبلغِ صفر ممکن نیست ولی تقسیم‌بر‌صفر باید محال باشد نه نامحتمل. */
    val progress: Float
        get() = if (targetRial <= 0.0) 0f else (savedRial / targetRial).toFloat().coerceIn(0f, 1f)

    val remainingRial: Double get() = (targetRial - savedRial).coerceAtLeast(0.0)

    val reached: Boolean get() = savedRial >= targetRial && targetRial > 0.0
}
