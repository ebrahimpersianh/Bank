package ir.sadteam.loancalc.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.SymbolStyle
import ir.sadteam.loancalc.data.categoryIconChoices
import ir.sadteam.loancalc.data.iconForKey
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import ir.sadteam.loancalc.ui.components.LocalHeroChartStyle
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppBg
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.settings.FullScreenDialog
import ir.sadteam.loancalc.ui.widget.IconWither
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.content.pm.PackageManager
import android.content.ComponentName

/**
 * صفحه‌ی اختصاصیِ محصول (تصمیمِ ۵ِ فروشگاه). خرید همچنان از دیالوگِ تاییدِ قبلی می‌گذرد.
 */
@Composable
internal fun ProductDetailSheet(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onDismiss: () -> Unit,
    onActivate: (ShopItem) -> Unit,
    onBuy: (ShopItem) -> Unit,
    onTry: (ShopItem) -> Unit,
    isGoal: Boolean,
    onToggleGoal: () -> Unit,
) {
    FullScreenDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Text(
                    item.kind.category.label,
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(AppRadius.card))
                                .background(AppSurface2),
                            contentAlignment = Alignment.Center,
                        ) {
                            // همان پیش‌نمایشِ ردیف، بزرگ‌شده - نه یک تصویرِ جدا که روزی جا بمانَد.
                            Box(modifier = Modifier.scale(2.6f)) { previewFor(item) }
                        }
                        Text(
                            item.label,
                            color = AppText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        Text(
                            item.blurb,
                            color = AppLabel,
                            fontSize = 11.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
                item { DetailFacts(item) }
                if (item.id.startsWith("icon:")) item { IconUsageSample(item.id) }
                if (item.id.startsWith("symbolset:")) item { SymbolSetSample(item.id) }
                if (item.id.startsWith("chart:")) item { ChartStyleSample(item.id) }
                // بازطراحیِ ۸ مهر: دکمه‌ها زیرِ کارت‌ها (نه تهِ صفحه با فضای خالیِ وسط)؛ «امتحان کن» پُر.
                item {
                Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val notMine = state == RowState.BUY || state == RowState.POOR
                if (notMine) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canTry(item)) {
                            GradientButton(
                                onClick = { onTry(item) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("امتحان کن · ${toFa(TRIAL_SECONDS)} ثانیه", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        GradientButton(
                            onClick = onToggleGoal,
                            variant = AppButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (isGoal) "برداشتنِ هدف" else "هدفِ سکه‌ام کن", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                DetailAction(item, state, balance, onActivate, onBuy)
            }
                }
            }
        }
    }
}
/** جدولِ مشخصات: نوع، تعداد/سبک (برای پک‌ها)، قیمت و وضعیت. */
@Composable
private fun DetailFacts(item: ShopItem) {
    val facts = buildList {
        add("نوع" to item.kind.category.label)
        when {
            item.id.startsWith("symbolset:") -> {
                add("تعدادِ نماد" to "${toFa(categoryIconChoices.size)} نماد")
                add("سبک" to item.label)
            }
            item.id.startsWith("icon:") -> add(
                "حالت‌ها" to if (IconWither.hasAgingStages(item.id)) "۴ حالت (کهنه‌شدن با سرنزدن)" else "یک طرحِ ثابت",
            )
        }
        add("قیمت" to "${toFa(item.price)} سکه")
    }
    AppCard(modifier = Modifier.fillMaxWidth()) {
        facts.forEachIndexed { index, (label, value) ->
            if (index > 0) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppLine))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(value, color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
/** نمونه‌ی استفاده‌ی آیکونِ برنامه: چهار حالتِ کهنه‌شدن روی صفحه‌ی گوشی. */
@Composable
private fun IconUsageSample(itemId: String) {
    val context = LocalContext.current
    val stages = remember(itemId) {
        IconWither.stageAliases(itemId)?.map { alias ->
            runCatching {
                val pm = context.packageManager
                pm.getActivityInfo(
                    ComponentName(context, "ir.sadteam.loancalc.LauncherAlias$alias"),
                    PackageManager.MATCH_DISABLED_COMPONENTS,
                ).loadIcon(pm).toBitmap(144, 144).asImageBitmap()
            }.getOrNull()
        }
    }
    val labels = listOf("تازه", "چند روز", "یک هفته", "رهاشده")
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            if (stages != null) "روی صفحه‌ی گوشی · کهنه‌شدن با سرنزدن" else "روی صفحه‌ی گوشی",
            color = AppMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) { index ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bitmap = stages?.getOrNull(index)
                    when {
                        bitmap != null -> Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)),
                        )
                        index == 1 -> AppIconPreview(itemId)
                        else -> Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppIconFrame),
                        )
                    }
                    Text(
                        when {
                            stages != null -> labels[index]
                            index == 1 -> "جیبک"
                            else -> ""
                        },
                        color = AppText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
/** چند نمادِ واقعی از همان پک، در اندازه‌ی واقعیِ فهرستِ دسته‌بندی. */
@Composable
private fun SymbolSetSample(itemId: String) {
    val style = SymbolStyle.fromItemId(itemId)
    val keys = categoryIconChoices.map { it.first }.take(12)
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("نمونه‌ی نمادها", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keys.chunked(6).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppIconFrame),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(iconForKey(key, style), contentDescription = null, tint = AppText, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
/** داده‌ی نمونه‌ی پیش‌نمایش (بسته‌ی ChatGPT) - آخرین نقطه «امروز» است. */
private val chartSampleValues = listOf(0.28, 0.42, 0.36, 0.58, 0.50, 0.74, 0.88)
private val chartSampleLabels = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "امروز")
/**
 * پیش‌نمایشِ سبکِ نمودار - **همان [HeroChart]ِ کارت‌های واقعی**، نه عکس، تا چیزی که
 * کاربر می‌خرد دقیقاً همانی باشد که روی کارت‌ها می‌نشیند.
 */
@Composable
internal fun ChartStylePreview(itemId: String) {
    val style = HeroChartStyle.fromItemId(itemId) ?: return
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPrimary).padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalHeroChartStyle provides style) {
            HeroChart(
                values = chartSampleValues,
                labels = emptyList(),
                valueLabel = { "" },
                currentIndex = chartSampleValues.lastIndex,
                natural = style,
                height = 26.dp,
            )
        }
    }
}
/** نمونه‌ی بزرگِ صفحه‌ی محصول - روی همان کارتِ رنگیِ بالای صفحه‌ها، و لمس‌پذیر. */
@Composable
private fun ChartStyleSample(itemId: String) {
    val style = HeroChartStyle.fromItemId(itemId) ?: return
    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
        Text("نمونه روی کارت", color = Color.White.copy(alpha = 0.8f), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
        Box(modifier = Modifier.padding(top = 26.dp)) {
            CompositionLocalProvider(LocalHeroChartStyle provides style) {
                HeroChart(
                    values = chartSampleValues,
                    labels = chartSampleLabels,
                    valueLabel = { "${toFa((it * 100).toInt())}٪" },
                    currentIndex = chartSampleValues.lastIndex,
                    natural = style,
                    height = 40.dp,
                )
            }
        }
    }
}
/** دکمه‌ی پایینِ صفحه - وضعیتِ خرید/فعال‌سازی را همان‌جا می‌گوید. */
@Composable
private fun DetailAction(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onBuy: (ShopItem) -> Unit,
) {
    when (state) {
        RowState.BUY -> GradientButton(onClick = { onBuy(item) }, modifier = Modifier.fillMaxWidth()) {
            Text("خرید با ${toFa(item.price)} سکه", fontWeight = FontWeight.Black)
        }
        RowState.OWNED -> GradientButton(onClick = { onActivate(item) }, modifier = Modifier.fillMaxWidth()) {
            Text("خریداری شده · فعال‌سازی", fontWeight = FontWeight.Black)
        }
        RowState.POOR -> AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${toFa(balance)} از ${toFa(item.price)} سکه", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text("${toFa(item.price - balance)} سکه کم داری", color = AppDangerInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            androidx.compose.material3.LinearProgressIndicator(
                progress = { (balance.toFloat() / item.price.coerceAtLeast(1)).coerceIn(0f, 1f) },
                color = AppPrimary,
                trackColor = AppPrimary.copy(alpha = 0.15f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(8.dp),
            )
        }
        else -> Text(
            when (state) {
                RowState.ACTIVE -> "خریداری شده · الان فعال است"
                RowState.POOR -> "${toFa(item.price - balance)} سکه کم داری"
                RowState.BADGE_LOCKED -> "اول باید نشانِ لازم را بگیری"
                else -> "به‌زودی"
            },
            color = if (state == RowState.POOR) AppDangerInk else AppMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}
