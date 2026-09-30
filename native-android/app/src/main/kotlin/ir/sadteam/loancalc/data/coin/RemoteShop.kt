package ir.sadteam.loancalc.data.coin

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson

/** تمِ تازه از سرور: فقط چهار رنگ (hex مثلِ `#1F4E8C`)، پس هیچ آپدیتی لازم ندارد. */
data class RemoteTheme(
    val id: String = "",
    val label: String = "",
    val dark: String = "",
    val primary: String = "",
    val light: String = "",
    val ink: String = "",
    val price: Int = 150,
    val addedOn: String? = null,
)

/**
 * 🛍 **فروشگاه از روی سرور** (۸ مهر، خواسته‌ی کاربر). همه‌ی فیلدها اختیاری‌اند؛ `{}` یعنی همان
 * کاتالوگِ داخلِ برنامه.
 * - `hidden`: شناسه‌هایی که از ویترین برداشته می‌شوند (کسی که خریده نگهش می‌دارد).
 * - `prices`: قیمتِ تازه‌ی هر شناسه (سکه).
 * - `labels`: نامِ تازه‌ی هر شناسه.
 * - `newOn`: تاریخِ «تازه‌رسیده» (کلیدِ جلالیِ ۱۴۰۵-۰۷-۰۸).
 * - `themes`: تم‌های رنگیِ تازه.
 * - `dealId` + `dealPercent`: تخفیفِ امروز به انتخابِ ادمین (به‌جای قرعه‌ی روزانه).
 */
data class RemoteShopConfig(
    val hidden: List<String> = emptyList(),
    val prices: Map<String, Int> = emptyMap(),
    val labels: Map<String, String> = emptyMap(),
    val newOn: Map<String, String> = emptyMap(),
    val themes: List<RemoteTheme> = emptyList(),
    val dealId: String? = null,
    val dealPercent: Int? = null,
)

object RemoteShop {
    private val gson = Gson()
    private const val PREFS = "remote_config"

    var config by mutableStateOf(RemoteShopConfig())
        private set

    /** آخرین نسخه‌ی ذخیره‌شده (برای بی‌اینترنت) - یک‌بار در شروعِ برنامه. */
    fun loadCached(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("shop", null)?.let { apply(it) }
    }

    /** JSONِ تازه از سرور؛ نامعتبر = نسخه‌ی قبلی می‌ماند. */
    fun update(context: Context, json: String) {
        if (apply(json)) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("shop", json).apply()
    }

    fun toJson(c: RemoteShopConfig): String = gson.toJson(c)

    private fun apply(json: String): Boolean = runCatching {
        // Gson فیلدهای نیامده را null می‌گذارد، نه پیش‌فرضِ Kotlin - پس دوباره پر می‌کنیم.
        val raw = gson.fromJson(json, RemoteShopConfig::class.java) ?: return false
        config = RemoteShopConfig(
            hidden = raw.hidden ?: emptyList(),
            prices = raw.prices ?: emptyMap(),
            labels = raw.labels ?: emptyMap(),
            newOn = raw.newOn ?: emptyMap(),
            themes = (raw.themes as List<RemoteTheme>?).orEmpty().filter { t: RemoteTheme -> t.id.isNotBlank() && parseHex(t.primary) != null },
            dealId = raw.dealId,
            dealPercent = raw.dealPercent,
        )
        true
    }.getOrDefault(false)

    fun parseHex(h: String?): Long? =
        h?.trim()?.removePrefix("#")?.takeIf { it.length == 6 }?.toLongOrNull(16)?.let { 0xFF000000L or it }

    val remotePalettes: List<ThemePalette>
        get() = config.themes.mapNotNull { t ->
            val p = parseHex(t.primary) ?: return@mapNotNull null
            ThemePalette(
                id = "r_${t.id}",
                label = t.label,
                dark = parseHex(t.dark) ?: p,
                primary = p,
                light = parseHex(t.light) ?: p,
                inkLight = parseHex(t.ink) ?: p,
            )
        }

    /** کاتالوگِ نهایی: کاتالوگِ برنامه + تغییراتِ سرور. */
    fun catalog(): List<ShopItem> {
        val c = config
        val base = SHOP_CATALOG.filter { it.id !in c.hidden }.map { item ->
            var x = item
            c.prices[item.id]?.let { if (it >= 0 && item.unlockBadge == null) x = x.copy(priceOverride = it) }
            c.labels[item.id]?.let { x = x.copy(label = it) }
            c.newOn[item.id]?.let { x = x.copy(addedOn = it) }
            x
        }
        val themes = c.themes.filter { "theme:r_${it.id}" !in c.hidden }.map { t ->
            ShopItem(
                id = "theme:r_${t.id}",
                kind = CoinSpend.THEME_PALETTE,
                label = "تمِ ${t.label}",
                blurb = "رنگِ اصلیِ برنامه را عوض می‌کند",
                addedOn = t.addedOn,
                priceOverride = t.price,
            )
        }
        // تم‌های تازه کنارِ بقیه‌ی تم‌ها، نه تهِ فهرست.
        val lastTheme = base.indexOfLast { it.kind == CoinSpend.THEME_PALETTE }
        return if (lastTheme < 0) base + themes else base.take(lastTheme + 1) + themes + base.drop(lastTheme + 1)
    }

    /** همه‌ی قلم‌ها، حتی پنهان‌ها - برای «مجموعه‌ی من» و صفحه‌ی ادمین. */
    fun catalogWithHidden(): List<ShopItem> {
        val shown = catalog()
        val hiddenOwnedCandidates = SHOP_CATALOG.filter { it.id in config.hidden }
        return shown + hiddenOwnedCandidates
    }
}
