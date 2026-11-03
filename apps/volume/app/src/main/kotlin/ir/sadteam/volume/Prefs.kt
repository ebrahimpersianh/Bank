package ir.sadteam.volume

import android.content.Context

/** چند تنظیمِ کوچک که بعد از خاموش‌وروشن‌شدنِ گوشی هم بمانند. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("volume", Context.MODE_PRIVATE)

    /** کاربر دکمه را روشن کرده؟ (بعد از روشن‌شدنِ گوشی بر همین اساس خودکار بالا می‌آید) */
    var enabled: Boolean
        get() = sp.getBoolean("enabled", true)
        set(v) = sp.edit().putBoolean("enabled", v).apply()

    /** کدام لبه: راست یا چپ. */
    var onLeft: Boolean
        get() = sp.getBoolean("left", false)
        set(v) = sp.edit().putBoolean("left", v).apply()

    /** فاصله از بالای صفحه به پیکسل؛ ۱- یعنی هنوز جابه‌جا نشده. */
    var y: Int
        get() = sp.getInt("y", -1)
        set(v) = sp.edit().putInt("y", v).apply()
}
