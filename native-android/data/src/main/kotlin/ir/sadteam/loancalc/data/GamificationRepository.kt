package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.db.AchievementDao
import ir.sadteam.loancalc.data.db.AchievementEntity
import ir.sadteam.loancalc.data.db.CoinDao
import ir.sadteam.loancalc.data.db.CoinEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * **اقتصادِ سکه و نشانِ «فعال»** - کارتِ `20e` فایلِ طراحی.
 *
 * سه قاعده‌ی صریحِ طرح که این کلاس نگهبانشونه:
 * 1. **موجودی از جمعِ دفتر بازسازی می‌شه** - هیچ ستونِ موجودیِ جدا وجود نداره.
 * 2. **رویدادِ تکراری با `unique(type, dateKey)` جلو گرفته می‌شه** - «ثبتِ روزانه» تو یه روز
 *    فقط یک‌بار سکه می‌ده.
 * 3. **سکه خریدنی نیست و به پول تبدیل نمی‌شه** - هیچ مسیرِ خریدِ درون‌برنامه‌ایِ سکه اینجا نیست.
 *
 * ⚠️ «استریک» عمداً **جایی نوشته نشده**: خواسته‌ی صریحِ کاربر اینه که همه‌جا واژه‌ی **«فعال»**
 * به کار بره.
 */
class GamificationRepository(
    private val coinDao: CoinDao,
    private val achievementDao: AchievementDao,
) {
    /** جدولِ «راهِ کسب» عیناً از کارتِ `20e`. */
    object Reward {
        const val NEW_PHONE_GIFT = 50
        const val DAILY_LOG = 10
        const val WEEK_COMPLETE = 50
        const val CONNECT_SMS = 75
        const val CONNECT_NOTIFICATION = 75
        const val COMPLETE_PROFILE = 50
        const val FIRST_BUDGET = 25
        const val FIRST_BACKUP = 25
    }

    /**
     * نوعِ رویداد. مقادیر **متنی**ن و تو دیتابیس ذخیره می‌شن، پس **هیچ‌وقت اسمشون رو عوض نکن** -
     * فقط مقدارِ تازه اضافه کن.
     */
    object Type {
        /** خریدِ تمِ رنگی - `refId` شناسه‌ی همون تمه. */
        const val SPEND_THEME = "spend_theme"

        /** ترمیمِ استریک - ماهی یک‌بار، `refId` کلیدِ ماهِ شمسیه. */
        const val STREAK_REPAIR = "streak_repair"

        const val NEW_PHONE_GIFT = "new_phone_gift"
        const val DAILY_LOG = "daily_log"
        const val WEEK_COMPLETE = "week_complete"
        const val CONNECT_SMS = "connect_sms"
        const val CONNECT_NOTIFICATION = "connect_notification"
        const val COMPLETE_PROFILE = "complete_profile"
        const val FIRST_BUDGET = "first_budget"
        const val FIRST_BACKUP = "first_backup"
        const val BADGE = "badge"
        const val SPEND_SUBSCRIPTION = "spend_subscription"
    }

    val balance: Flow<Int> = coinDao.observeBalance()

    val events: Flow<List<CoinEventEntity>> = coinDao.observeAll()

    val achievements: Flow<List<AchievementEntity>> = achievementDao.observeAll()

    /**
     * شمارِ روزهای **پشتِ‌سرهمِ** فعال، از امروز به عقب.
     *
     * ⚠️ اگه کاربر امروز هنوز چیزی ثبت نکرده ولی دیروز کرده، زنجیر **پاره نیست** - از دیروز
     * شمرده می‌شه. زنجیر فقط وقتی صفر می‌شه که دیروز هم خالی باشه.
     */
    val activeDays: Flow<Int> = coinDao.observeDateKeys(Type.DAILY_LOG).map { keys ->
        countActiveDays(keys.toSet(), JalaliCalendar.today())
    }

    /**
     * سکه‌ی «هر روزِ ثبتِ تراکنش». هر بار که کاربر تراکنشی ثبت می‌کنه صدا زده می‌شه؛ تکرارِ
     * همون روز خودبه‌خود نادیده گرفته می‌شه (ایندکسِ یکتا).
     *
     * @return `true` اگه واقعاً سکه‌ی تازه‌ای ثبت شد (برای پخشِ انیمیشنِ گرفتنِ سکه).
     */
    suspend fun awardDailyLog(today: PersianDate = JalaliCalendar.today()): Boolean {
        val granted = award(Type.DAILY_LOG, Reward.DAILY_LOG, dateKey(today))
        if (granted) {
            // «هر هفت‌روزِ کاملِ فعال بودن ۵۰ سکه» - با کلیدِ هفته‌ای جدا تا هر هفته یک‌بار.
            val days = countActiveDays(coinDao.getDateKeys(Type.DAILY_LOG).toSet(), today)
            if (days > 0 && days % 7 == 0) {
                award(Type.WEEK_COMPLETE, Reward.WEEK_COMPLETE, "${dateKey(today)}#${days / 7}")
            }
        }
        return granted
    }

    /** رویدادِ **یک‌باره** - [dateKey] خالی می‌مونه تا یگانگی روی خودِ نوع بیفته. */
    suspend fun awardOnce(type: String, amount: Int): Boolean = award(type, amount, "")

    /** خرجِ سکه (تخفیفِ اشتراک). مقدار **منفی** ثبت می‌شه تا جمعِ دفتر همچنان موجودی بده. */
    /**
     * خرجِ سکه با نوعِ دلخواه - `refId` کلیدِ ضدِتکراره، پس دوبار زدنِ یه دکمه دوبار خرج نمی‌کنه.
     *
     * ⚠️ نوع رو **جدا** نگه دار (خریدِ تم، ترمیمِ استریک، تخفیفِ اشتراک): اگه روزی لازم شد
     * سکه‌ی روزانه بازحساب بشه، دستاوردِ کاربر نباید با اون قاطی شه.
     */
    suspend fun spend(amount: Int, refId: String, type: String = Type.SPEND_SUBSCRIPTION) {
        coinDao.award(
            CoinEventEntity(
                type = type,
                amount = -amount,
                dateKey = refId,
                createdAt = System.currentTimeMillis(),
                refId = refId,
            ),
        )
    }

    suspend fun unlock(code: String, coins: Int) {
        val inserted = achievementDao.unlock(AchievementEntity(code, System.currentTimeMillis()))
        if (inserted != -1L) award(Type.BADGE, coins, code)
    }

    private suspend fun award(type: String, amount: Int, dateKey: String): Boolean =
        coinDao.award(
            CoinEventEntity(
                type = type,
                amount = amount,
                dateKey = dateKey,
                createdAt = System.currentTimeMillis(),
            ),
        ) != -1L

    companion object {
        /** هر دو تابع به `:core` منتقل شدن تا `:core:test` رو JVM بتونه تستشون کنه. */
        fun dateKey(date: PersianDate): String = ActiveStreak.dateKey(date)

        fun countActiveDays(days: Set<String>, today: PersianDate): Int =
            ActiveStreak.countActiveDays(days, today)
    }
}
