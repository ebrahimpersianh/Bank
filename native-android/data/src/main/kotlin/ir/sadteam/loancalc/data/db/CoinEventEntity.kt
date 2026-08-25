package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * **دفترِ سکه** - بخشِ «اقتصادِ سکه»ی فایلِ طراحی (کارتِ `20e`).
 *
 * قاعده‌ی صریحِ طرح: «موجودی همیشه از جمعِ دفتر بازسازی می‌شود، پس با همگام‌سازیِ چند دستگاه از
 * هم نمی‌پاشد». برای همین **هیچ ستونِ `balance` جایی ذخیره نمی‌شه** - هر جا موجودی لازم بود از
 * `SUM(amount)` خونده می‌شه.
 *
 * ⚠️ [dateKey] کلیدِ ضدِتکراره: طرح می‌گه «رویدادِ تکراری با `unique(type, dateKey)` جلوگیری
 * می‌شود؛ مثلاً "ثبتِ روزانه" در یک روز فقط یک‌بار». برای رویدادهای یک‌باره (هدیه‌ی شماره‌ی تازه،
 * وصل‌کردنِ پیامک) [dateKey] خالی می‌مونه تا همون یگانگی روی خودِ [type] بیفته.
 *
 * ⚠️ **قاعده‌ی ماندگارِ پروژه**: ایندکسِ زیر باید عیناً تو `MIGRATION_23_24` هم `CREATE` بشه -
 * یه‌بار نبودِ همین تطابق نسخه‌ی ۱.۰.۳۱۵ رو برای هر آپدیت‌کننده کرش داد.
 */
@Entity(
    tableName = "coin_events",
    indices = [Index(value = ["type", "dateKey"], unique = true)],
)
data class CoinEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** یکی از مقادیرِ `CoinEventType` - به‌صورتِ متن ذخیره می‌شه تا نوعِ تازه مقدارِ قدیمی رو خراب نکنه. */
    val type: String,
    /** مثبت = کسب، منفی = خرج (تخفیفِ اشتراک). */
    val amount: Int,
    /** «۱۴۰۵-۰۵-۲۹» برای رویدادهای روزانه، رشته‌ی خالی برای رویدادهای یک‌باره. */
    val dateKey: String,
    val createdAt: Long,
    /** شناسه‌ی رویدادِ مرتبط (مثلاً idِ تراکنش یا کدِ نشان) - اختیاری. */
    val refId: String? = null,
)
