package ir.sadteam.loancalc.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.ColorTheme
import ir.sadteam.loancalc.ui.theme.ThemeViewModel

/**
 * **تمِ رنگی** - فریمِ `18b`.
 *
 * ⚠️ عنوانِ صفحه عمداً «فروشگاهِ ظاهر» **نیست**: ردیفِ سه‌تبِ فریم (تم · آیکونِ دسته ·
 * نشانِ نام) حذف شد چون دو تبِ آخر جنس ندارن، و «چیزی رو وعده نده که یه قلمه».
 * وقتی جنسِ تبِ دوم آماده شد، ردیفِ تب عیناً طبقِ فریم برمی‌گرده.
 *
 * قاعده‌ی مهمِ فریم که پیاده شده: **پیش‌نمایش تمِ زیرِ انگشته، نه تمِ فعال.**
 * ضربه‌ی اول فقط پیش‌نمایش می‌ده (کاربر می‌تونه بی‌خرید ببینه)، ضربه‌ی دوم شیت رو باز می‌کنه.
 */
@Composable
fun ThemeShopScreen(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel(),
) {
    val active by themeViewModel.colorTheme.collectAsState()
    val owned by themeViewModel.ownedThemes.collectAsState()
    val coins by gamificationViewModel.coins.collectAsState()
    var preview by remember { mutableStateOf<ColorTheme?>(null) }
    var sheetFor by remember { mutableStateOf<ColorTheme?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // کارتِ موجودی
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("سکه‌های تو", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                        Text(toFa(coins), color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        CoinIcon(size = 18.dp, modifier = Modifier.padding(start = 5.dp))
                    }
                }
            }
            Text(
                "هر روزِ ثبت ۱۰ سکه · هفت روزِ پشتِ‌سرهم ۵۰ · نشانِ تازه ۲۵ تا ۱۵۰",
                color = AppLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        ColorTheme.entries.forEach { theme ->
            val isOwned = theme.price == 0 || theme.id in owned
            val isActive = theme == active
            ThemeRow(
                theme = theme,
                isActive = isActive,
                isOwned = isOwned,
                previewing = preview == theme,
                onTap = {
                    when {
                        isActive -> Unit
                        isOwned -> themeViewModel.selectColorTheme(theme)
                        // ضربه‌ی اول پیش‌نمایش، ضربه‌ی دوم شیت.
                        preview != theme -> preview = theme
                        else -> sheetFor = theme
                    }
                },
            )
        }
    }

    sheetFor?.let { theme ->
        val price = theme.price ?: 0
        val short = price - coins
        AlertDialog(
            onDismissRequest = { sheetFor = null },
            confirmButton = {
                TextButton(
                    enabled = theme.price != null && short <= 0,
                    onClick = {
                        gamificationViewModel.spendCoins(price, theme.id)
                        themeViewModel.ownAndSelect(theme)
                        sheetFor = null
                    },
                ) {
                    Text("بخر و روشن کن")
                }
            },
            dismissButton = { TextButton(onClick = { sheetFor = null }) { Text("بعداً") } },
            title = { Text(theme.label, fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    ThemeSwatch(theme, width = 104.dp, height = 56.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Text(toFa(price), color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        CoinIcon(size = 18.dp, modifier = Modifier.padding(start = 5.dp))
                    }
                    // این خط مهمه: کاربر باید **قبلِ** خرید بدونه چی می‌مونه.
                    Text(
                        if (short > 0) "${toFa(short)} سکه کم داری" else "بعد از خرید ${toFa(coins - price)} سکه می‌مانَد.",
                        color = if (short > 0) AppDangerInk else AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (short > 0) {
                        // بن‌بست نساز - تنها جاییه که کاربر انگیزه داره بدونه سکه از کجا میاد.
                        Text(
                            "هر روزِ ثبت ۱۰ سکه\nهفت روزِ پشتِ‌سرهم ۵۰ سکه\nنشانِ تازه ۲۵ تا ۱۵۰ سکه",
                            color = AppMuted,
                            fontSize = 10.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun ThemeRow(
    theme: ColorTheme,
    isActive: Boolean,
    isOwned: Boolean,
    previewing: Boolean,
    onTap: () -> Unit,
) {
    val lockedByBadge = theme.price == null && !isOwned
    AppCard(
        modifier = Modifier.padding(top = 10.dp).pressScaleClickable(scale = 0.99f, onClick = onTap),
        borderColor = if (isActive || previewing) AppPrimary else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeSwatch(theme, width = 52.dp, height = 38.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(theme.label, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                Text(
                    when {
                        isActive -> "روشن است"
                        lockedByBadge -> "با نشانِ «ماهِ منظم» باز می‌شود"
                        isOwned -> "روشن کن"
                        previewing -> "دوباره بزن تا بخری"
                        else -> "بزن تا ببینی"
                    },
                    color = if (isActive) AppPrimaryInk else AppMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            when {
                lockedByBadge -> Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = AppLabel,
                    modifier = Modifier.size(16.dp),
                )
                // قیمت روی کارتِ نخریده **همیشه دیده می‌شه** - خاکستری/قفل نمی‌شه، چون
                // کاربر باید قیمت رو ببینه.
                !isOwned -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        toFa(theme.price ?: 0),
                        color = AppPrimaryInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                    CoinIcon(size = 14.dp, modifier = Modifier.padding(start = 4.dp))
                }
                isActive -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppPrimaryPill)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text("فعال", color = AppPrimaryInk, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                else -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppSurface2)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text("روشن کن", color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/** سه نوارِ پیش‌نمایشِ تم - پررنگ · روشن · تهرنگ (فریمِ `18b`). */
@Composable
private fun ThemeSwatch(theme: ColorTheme, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(AppRadius.icon))
            .border(1.dp, AppLine, RoundedCornerShape(AppRadius.icon)),
    ) {
        theme.swatch.forEach { color ->
            Box(modifier = Modifier.weight(1f).height(height).background(color))
        }
    }
}
