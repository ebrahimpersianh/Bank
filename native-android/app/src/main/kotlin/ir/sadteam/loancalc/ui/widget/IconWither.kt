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
     * پله‌ی پژمردگی از روی آخرین روزِ فعالیت.
     *
     * **از روزِ دوم شروع می‌شود** (تصحیحِ صریحِ کاربر: «نزدیک هشتا طراحیه و دوتا نیست») -
     * یک روز نرفتن هنوز غیبت نیست.
     */
    fun stepFor(lastActiveDay: PersianDate?, today: PersianDate = JalaliCalendar.today()): Int {
        if (lastActiveDay == null) return 0
        val away = runCatching { JalaliCalendar.daysBetween(lastActiveDay, today) }.getOrDefault(0)
        return (away - 1).coerceIn(0, STEPS)
    }

    /**
     * پله‌ی متناظر با آخرین روزِ ثبت‌شده در دفترِ سکه (`DAILY_LOG`) را اعمال می‌کند.
     *
     * [activeIcon] آیکونِ خریداری‌شده‌ی فعال (`icon:piggy`…) یا `null` برای پیش‌فرض.
     * هر پنج طرح هشت پله دارند (بخشِ ۶۱)، پس پژمردگی روی آیکونِ خریدنی هم اجرا می‌شود.
     */
    fun applyFromDateKeys(context: Context, dateKeys: Collection<String>, activeIcon: String? = null) {
        apply(context, stepFor(lastDay(dateKeys)), activeIcon)
    }

    /** دقیقاً **یک** الیاس از ۴۵ تا روشن می‌ماند. */
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
        /** هشت پله - تصحیحِ صریحِ کاربر، نه دو حالت. */
        const val STEPS = 8

        /**
         * آیکونِ خریدنی → شماره‌ی الیاسِ **پله‌ی صفر**ش. شناسه‌ها همان `ShopItem.id`ِ
         * کاتالوگ‌اند. «کیفِ پول» این‌جا نیست چون پیش‌فرض است و الیاسِ ۰..۸ مالِ اوست.
         */
        val ICON_ALIAS =
            mapOf(
                "icon:coin" to 9,
                "icon:letter" to 10,
                "icon:piggy" to 11,
                "icon:shop" to 36,
            )

        /** پله‌ی ۱..۸ِ هر طرحِ خریدنی (بخشِ ۶۱) - ۳۲ پله‌ی ساخته‌شده‌ی `generate-wither.py`. */
        private val WITHER_BASE =
            mapOf(
                "icon:coin" to 12,
                "icon:letter" to 20,
                "icon:piggy" to 28,
                "icon:shop" to 37,
            )

        private const val LAST_ALIAS = 44

        /**
         * کدام الیاس روشن شود.
         *
         * پله‌ی صفرِ هر طرح الیاسِ جدا دارد (۰ و ۹ و ۱۰ و ۱۱ و ۳۶) چون فایلِ «تازه»ی طرح است، و
         * پله‌های ۱..۸ پشتِ‌هم می‌آیند. پس نگاشت **دو تکه** است، نه یک جمعِ ساده.
         */
        fun aliasFor(activeIcon: String?, step: Int): Int {
            val safe = step.coerceIn(0, STEPS)
            val zero = ICON_ALIAS[activeIcon] ?: return safe
            if (safe == 0) return zero
            val base = WITHER_BASE[activeIcon] ?: return zero
            return base + safe - 1
        }
    }
}
