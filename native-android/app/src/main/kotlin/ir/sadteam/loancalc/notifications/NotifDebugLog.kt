package ir.sadteam.loancalc.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🔎 دفترچه‌ی عیب‌یابیِ اعلان‌های بانکی (۱۴ مهر): ۱۵ اعلانِ آخرِ اپ‌های بانکیِ انتخاب‌شده،
 * با متن و **دلیلِ** ثبت‌شدن/نشدن. فقط روی همین گوشی می‌ماند و جایی فرستاده نمی‌شود؛ برای این‌که
 * وقتی کاربر می‌گوید «باز هم نخواند» بدانیم کجای مسیر رد شده.
 */
object NotifDebugLog {
    private const val PREFS = "notif_debug"
    private const val KEY = "log"
    private const val MAX = 15

    data class Entry(val at: Long, val pkg: String, val text: String, val result: String)

    fun record(context: Context, pkg: String, text: String, result: String) {
        runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val arr = JSONArray(prefs.getString(KEY, "[]"))
            val out = JSONArray()
            out.put(JSONObject().put("at", System.currentTimeMillis()).put("pkg", pkg).put("text", text.take(300)).put("r", result))
            for (i in 0 until minOf(arr.length(), MAX - 1)) out.put(arr.get(i))
            prefs.edit().putString(KEY, out.toString()).apply()
        }
    }

    /** آخرین باری که سرویسِ خواندنِ اعلان به سیستم وصل شد (برای نمایش در تنظیمات). */
    fun markConnected(context: Context) {
        runCatching { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("connected_at", System.currentTimeMillis()).apply() }
    }

    fun connectedAt(context: Context): Long =
        runCatching { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("connected_at", 0L) }.getOrDefault(0L)

    fun read(context: Context): List<Entry> = runCatching {
        val arr = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Entry(o.getLong("at"), o.getString("pkg"), o.getString("text"), o.getString("r"))
        }
    }.getOrDefault(emptyList())

    /** متنِ فارسیِ هر نتیجه برای نمایش. */
    fun label(result: String): String = when (result) {
        "ok" -> "✓ ثبت شد"
        "off" -> "✗ کلیدِ خواندنِ اعلان خاموش است"
        "not_subscribed" -> "✗ اشتراک فعال نیست"
        "parse_fail" -> "✗ مبلغ یا نوعِ تراکنش در متن پیدا نشد"
        "duplicate" -> "– تکراری (همین اعلان قبلاً دیده شده)"
        "twin" -> "– همین تراکنش از پیامک/پرداختِ دستی ثبت شده بود"
        "no_account" -> "✗ معلوم نبود مالِ کدام حساب است"
        else -> result
    }
}
