package ir.sadteam.loancalc.ui.widget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ir.sadteam.loancalc.core.ActiveStreak
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * **پژمردگیِ آیکونِ اپ** (فریمِ `49b`/`45c`) - هرچه بیشتر به اپ سر نزنی، آیکون بی‌جان‌تر می‌شود.
 *
 * ⚠️ **چرا هشت فایلِ ایستا و نه یک فیلتر**: فرمولِ `saturate = 1 − 0.10·n` در زمانِ اجرا روی
 * آیکونِ لانچر **اجراشدنی نیست** - آیکون یک ریسورسِ ایستاست و `VectorDrawable` فیلترِ اشباع
 * ندارد. پس هشت پله پیش از بیلد ساخته می‌شوند (`tools/icons/generate-wither.py`) و این‌جا فقط
 * `activity-alias`ِ درست فعال می‌شود.
 *
 * ⚠️ **ترتیب مهم است**: اول پله‌ی تازه فعال می‌شود، بعد بقیه خاموش. برعکسش یک لحظه‌ی بی‌آیکون
 * می‌سازد و بعضی لانچرها همان لحظه آیکون را از صفحه‌ی خانه برمی‌دارند.
 *
 * ⚠️ **لحظه‌ی اعمال، رفتنِ اپ به پس‌زمینه است نه باز شدنش** (قاعده‌ی صریحِ `49`): کاربر موقعِ
 * باز کردن دقیقاً به آیکون نگاه می‌کند و پرشِ آیکون زیرِ انگشتش دیده می‌شود.
 */
@Singleton
class IconWither @Inject constructor() {

    /**
     * پله‌ی پژمردگی از روی آخرین روزِ **سرزدن** به برنامه.
     *
     * جدولِ صریحِ کاربر (۳۱ شهریور):
     *
     *     همین امروز  → پله‌ی ۰ (سالم)
     *     ۱ روز نرفته → پله‌ی ۱ (~۲۰٪ کهنه)
     *     ۲ روز       → پله‌ی ۲ (~۴۰٪ کهنه)
     *     ۳ روز و بیشتر → پله‌ی ۳ (آخرین حالت)
     *
     * ⚠️ **دیگر یک روز ارفاق ندارد.** پیش از این `away - 1` بود («یک روز نرفتن هنوز
     * غیبت نیست») و با هشت پله معنی داشت؛ با چهار پله همان ارفاق یعنی پله‌ی دوم
     * عملاً هیچ‌وقت دیده نمی‌شد.
     */
    fun stepFor(lastActiveDay: PersianDate?, today: PersianDate = JalaliCalendar.today()): Int {
        if (lastActiveDay == null) return 0
        val away = runCatching { JalaliCalendar.daysBetween(lastActiveDay, today) }.getOrDefault(0)
        return away.coerceIn(0, STEPS)
    }

    /**
     * پله‌ی متناظر با آخرین روزِ ثبت‌شده در دفترِ سکه (`DAILY_LOG`) را اعمال می‌کند.
     *
     * [activeIcon] آیکونِ خریداری‌شده‌ی فعال (`icon:piggy`…) یا `null` برای پیش‌فرض.
     * پنج طرحِ وکتوری و نه طرحِ تصویری هر کدام چهار حالت دارند.
     */
    fun applyFromDateKeys(context: Context, dateKeys: Collection<String>, activeIcon: String? = null) {
        apply(context, stepFor(lastDay(dateKeys)), activeIcon)
    }

    /** دقیقاً **یک** الیاس از ۵۴ تا روشن می‌ماند. */
    fun apply(context: Context, step: Int, activeIcon: String? = null) {
        val target = aliasFor(activeIcon, step)
        val pm = context.packageManager
        enable(pm, context, target)
        for (n in 0..LAST_ALIAS) {
            if (n != target) disable(pm, context, n)
        }
    }

    private fun enable(pm: PackageManager, context: Context, n: Int) {
        pm.setComponentEnabledSetting(
            aliasName(context, n),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun disable(pm: PackageManager, context: Context, n: Int) {
        pm.setComponentEnabledSetting(
            aliasName(context, n),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun aliasName(context: Context, n: Int) =
        ComponentName(context, "ir.sadteam.loancalc.LauncherAlias$n")

    /** تازه‌ترین روزِ فعال از کلیدهای `۱۴۰۵-۰۵-۰۹`ی دفترِ سکه. */
    private fun lastDay(dateKeys: Collection<String>): PersianDate? =
        dateKeys.mapNotNull { key ->
            val parts = key.split('-')
            if (parts.size != 3) return@mapNotNull null
            val y = parts[0].toIntOrNull() ?: return@mapNotNull null
            val m = parts[1].toIntOrNull() ?: return@mapNotNull null
            val d = parts[2].toIntOrNull() ?: return@mapNotNull null
            PersianDate(y, m, d)
        }.maxByOrNull { ActiveStreak.dateKey(it) }

    companion object {
        /**
         * **چهار حالت** (`۰..۳`) - خواسته‌ی صریحِ کاربر، جایگزینِ هشت پله‌ی قبلی.
         *
         * هشت پله از دیدِ کاربر تفاوتِ دیدنی نمی‌ساخت: هر پله ۱۰٪ اشباع کم می‌کرد و دو
         * پله‌ی کنارِ هم عملاً یکی دیده می‌شدند. چهار حالتِ «سالم / ۲۰٪ / ۴۰٪ / ۶۰٪»
         * همان قصه را با گام‌هایی می‌گوید که واقعاً از هم جدا دیده می‌شوند.
         */
        const val STEPS = 3

        /**
         * آیکون → **چهار الیاسِ** پله‌ی ۰..۳ آن.
         *
         * 🚨 جدولِ قبلی دو تکه بود (`ICON_ALIAS` برای پله‌ی صفر و `WITHER_BASE` برای
         * ۱..۸ِ پشتِ‌هم) و همان دوتکه‌بودن یک‌بار باعثِ نگاشتِ اشتباه شد. حالا هر طرح
         * فهرستِ صریحِ خودش را دارد؛ شماره‌ها پشتِ‌هم نیستند و لازم هم نیست باشند.
         *
         * ⚠️ الیاس‌های میانی (پله‌های استفاده‌نشده‌ی ستِ هشت‌تاییِ قدیمی) در مانیفست
         * می‌مانند و خاموش‌اند - حذفشان یعنی کسی که همین حالا آن پله رویش فعال است،
         * آیکونش از صفحه‌ی گوشی ناپدید شود.
         */
        private val STAGES: Map<String, IntArray> =
            mapOf(
                // طرحِ پیش‌فرضِ «کیف»: پله‌ی صفر خودِ `ic_launcher` است.
                DEFAULT_ICON to intArrayOf(0, 2, 4, 6),
                "icon:coin" to intArrayOf(9, 13, 15, 17),
                "icon:letter" to intArrayOf(10, 21, 23, 25),
                "icon:piggy" to intArrayOf(11, 29, 31, 33),
                "icon:shop" to intArrayOf(36, 38, 40, 42),
                // ۹ طرحِ تصویری (بسته‌ی ChatGPT، ۴ حالتِ آماده): s0 الیاسِ قبلی، s1..s3 الیاس‌های ۵۴..۸۰.
                "icon:aqua" to intArrayOf(45, 54, 55, 56),
                "icon:calligraphy" to intArrayOf(46, 57, 58, 59),
                "icon:fox" to intArrayOf(47, 60, 61, 62),
                "icon:emerald" to intArrayOf(48, 63, 64, 65),
                "icon:leaf" to intArrayOf(49, 66, 67, 68),
                "icon:orbit" to intArrayOf(50, 69, 70, 71),
                "icon:growth" to intArrayOf(51, 72, 73, 74),
                "icon:sprout" to intArrayOf(52, 75, 76, 77),
                "icon:neon" to intArrayOf(53, 78, 79, 80),
            )

        /** کلیدِ داخلیِ طرحِ پیش‌فرض - `activeIcon`ِ `null` به این نگاشت می‌شود. */
        private const val DEFAULT_ICON = "icon:wallet"

        /**
         * آیکونِ خریدنی → الیاسِ پله‌ی صفرش. `LoanCalcApplication` با همین تشخیص می‌دهد
         * ترجیحِ ذخیره‌شده‌ی کاربر هنوز وجود دارد یا باید پاک شود.
         *
         * نه طرحِ تصویریِ بخشِ ۸۰ حالا چهار حالتِ رستریِ آماده دارند (بالاتر در [STAGES]).
         */
        val ICON_ALIAS =
            mapOf(
                "icon:coin" to 9,
                "icon:letter" to 10,
                "icon:piggy" to 11,
                "icon:shop" to 36,
                "icon:aqua" to 45,
                "icon:calligraphy" to 46,
                "icon:fox" to 47,
                "icon:emerald" to 48,
                "icon:leaf" to 49,
                "icon:orbit" to 50,
                "icon:growth" to 51,
                "icon:sprout" to 52,
                "icon:neon" to 53,
            )

        private const val LAST_ALIAS = 80

        /** این طرح چهار حالتِ کهنگی دارد؟ صفحه‌ی محصول همین را به خریدار می‌گوید. */
        fun hasAgingStages(iconId: String): Boolean = STAGES.containsKey(iconId)

        /** کدام الیاس روشن شود - رجوع کن به [STAGES]. */
        fun aliasFor(activeIcon: String?, step: Int): Int {
            val safe = step.coerceIn(0, STEPS)
            val stages = STAGES[activeIcon ?: DEFAULT_ICON]
            // طرحی که پله ندارد (نه آیکونِ تصویری) همیشه پله‌ی صفرِ خودش است.
            if (stages == null) return ICON_ALIAS[activeIcon] ?: 0
            return stages[safe]
        }
    }
}
