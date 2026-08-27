package ir.sadteam.loancalc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * **تمِ رنگیِ خریدنی** - فریمِ `18b`.
 *
 * قاعده‌ی قفل‌شده‌ی طراحی: تم **فقط** خانواده‌ی `AppPrimary` رو عوض می‌کنه. بستر، سطح، خط،
 * متن و رنگ‌های معنایی (قرمز، نارنجی، آبیِ اطلاع) **هرگز** - وگرنه هر تم یعنی یه سیستمِ
 * طراحیِ جدید و همه‌ی فریم‌ها باید دوباره کشیده بشن.
 *
 * پس هر تم = **شش مقدار × دو حالت** و تمام.
 *
 * ⚠️ **تیره‌تر همیشه `primary`ه و روشن‌تر `primaryLight`.** دکمه‌ی اصلی متنِ سفید داره و
 * رنگِ روشنِ هر پالت براش کنتراست نداره (`#1CB0F6` با سفید حدودِ ۲٫۴:۱ه - خونا نیست).
 *
 * ⚠️ سکه، نشانِ جیبک و آدمک‌ها با تم عوض **نمی‌شن** - نشانِ برندن نه رنگِ تم.
 */
enum class ColorTheme(
    val id: String,
    val label: String,
    /** قیمت به سکه؛ `0` یعنی رایگان/پیش‌فرض، `null` یعنی با **نشان** باز می‌شه نه با سکه. */
    val price: Int?,
    /** اگه با نشان باز می‌شه، کدِ همون نشان. */
    val badgeCode: String? = null,
    private val lightPrimary: Long,
    private val lightDim: Long,
    private val lightInk: Long,
    private val lightLight: Long,
    private val lightPill: Long,
    private val lightBorder: Long,
    private val darkInk: Long,
    private val darkLight: Long,
    private val darkPill: Long,
    private val darkBorder: Long,
) {
    /** سبزِ جیبک - پیش‌فرض، از `TOKENS.md`. دست نزن. */
    GREEN(
        id = "green",
        label = "سبزِ جیبک",
        price = 0,
        lightPrimary = 0xFF0EA968, lightDim = 0xFF0B8C57, lightInk = 0xFF0B8C57,
        lightLight = 0xFF3DDC96, lightPill = 0xFFE9F7EF, lightBorder = 0xFF9FE0BC,
        darkInk = 0xFF3DDC96, darkLight = 0xFF3DDC96, darkPill = 0x243DDC96, darkBorder = 0x593DDC96,
    ),
    BLUE(
        id = "blue",
        label = "آبیِ آرام",
        price = 150,
        lightPrimary = 0xFF1381B8, lightDim = 0xFF0F6892, lightInk = 0xFF1381B8,
        lightLight = 0xFF1CB0F6, lightPill = 0xFFE7F4FC, lightBorder = 0xFFA9D6F7,
        darkInk = 0xFF6FC8F2, darkLight = 0xFF1CB0F6, darkPill = 0x241CB0F6, darkBorder = 0x591CB0F6,
    ),
    PURPLE(
        id = "purple",
        label = "بنفشِ شب",
        price = 150,
        lightPrimary = 0xFF7440C9, lightDim = 0xFF5C2FA8, lightInk = 0xFF7440C9,
        lightLight = 0xFFA56EFF, lightPill = 0xFFF2EDFC, lightBorder = 0xFFCDB6F2,
        darkInk = 0xFFBE97FF, darkLight = 0xFFA56EFF, darkPill = 0x24A56EFF, darkBorder = 0x59A56EFF,
    ),

    /** با **نشانِ «ماهِ منظم»** باز می‌شه نه با سکه - قاعده‌ی خوبیه که سکه همه‌چیز رو نمی‌خره. */
    GOLD(
        id = "gold",
        label = "طلاییِ کهنه",
        price = null,
        badgeCode = "steady_month",
        lightPrimary = 0xFFA87A16, lightDim = 0xFF8B6314, lightInk = 0xFF8B6F3D,
        // اصلاحیه‌ی طراح: `#C99A2E` جهشِ کافی از `#A87A16` نداشت و تو گرادیانِ نوارِ بودجه دو
        // رنگ تقریباً یکی دیده می‌شدن. سبز جهشِ سه‌چهارپله‌ای داره، طلایی هم باید داشته باشه.
        lightLight = 0xFFE3B94F, lightPill = 0xFFFDF4E0, lightBorder = 0xFFEBD9B4,
        darkInk = 0xFFC99A2E, darkLight = 0xFFE3B94F, darkPill = 0x24C99A2E, darkBorder = 0x59C99A2E,
    ),
    ;

    /** سه نوارِ پیش‌نمایش: پررنگ · روشن · تهرنگ. */
    val swatch: List<Color>
        get() = listOf(Color(lightPrimary), Color(lightLight), Color(lightPill))

    /**
     * شش مقدارِ خانواده‌ی primary رو رو یه پالتِ موجود می‌نشونه.
     * **بقیه‌ی پالت دست‌نخورده می‌مونه** - همون قاعده‌ی بالا.
     */
    fun applyTo(base: AppColorPalette): AppColorPalette = if (base.isDark) {
        base.copy(
            // سبزِ **دکمه** تو تمِ تیره هم عوض نمی‌شه (قاعده‌ی TOKENS: «سبزِ دکمه عوض
            // نمی‌شود؛ فقط سایه») - ولی جوهر و قرص و حاشیه آره.
            primary = Color(lightPrimary),
            primaryDim = Color(lightDim),
            primaryInk = Color(darkInk),
            primaryLight = Color(darkLight),
            primaryPill = Color(darkPill),
            primaryBorder = Color(darkBorder),
        )
    } else {
        base.copy(
            primary = Color(lightPrimary),
            primaryDim = Color(lightDim),
            primaryInk = Color(lightInk),
            primaryLight = Color(lightLight),
            primaryPill = Color(lightPill),
            primaryBorder = Color(lightBorder),
        )
    }

    companion object {
        fun fromId(id: String?): ColorTheme = entries.firstOrNull { it.id == id } ?: GREEN
    }
}
