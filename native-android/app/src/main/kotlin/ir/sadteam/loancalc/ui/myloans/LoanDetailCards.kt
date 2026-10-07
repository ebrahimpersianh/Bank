package ir.sadteam.loancalc.ui.myloans

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppPurpleInk
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import androidx.compose.material.icons.filled.Eco
import kotlin.math.roundToInt

/**
 * **کارتِ «مشخصات»** — فریمِ `80a`، جانشینِ فهرستِ شش‌ردیفیِ `29p`.
 *
 * 🚨 **سه سلول در سطح، بقیه زیرِ «بیشتر»**: مبلغِ وام · نرخ · پایان، چون هر سه در جمله‌ی
 * «این چه وامی است» می‌آیند. تعدادِ قسط و سودِ کل و ضامن یک پله پایین‌ترند — همان الگوی
 * فیلدهای اضافه‌ی فرمِ چک، پس الگوی تازه‌ای به سیستم اضافه نشد.
 *
 * ⚠️ **بانک این‌جا نیست**، به سرصفحه رفت: نامِ بانک هویتِ وام است نه یکی از مشخصاتش.
 * ⚠️ **وامِ بی‌نرخ سلولِ نرخ را حذف می‌کند، صفر نمی‌گذارد** (قاعده‌ی ۳): «۰٪» گمراه‌کننده است.
 */
@Composable
internal fun LoanSpecsCard(
    amount: Double,
    ratePct: Double,
    n: Int,
    borrower: String,
    endLabel: String?,
    totalInterest: Double?,
    privacyMode: Boolean,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCell("مبلغِ وام", null, amount, privacyMode, Modifier.weight(1f))
            // بی‌نرخ: سلول **حذف** می‌شود و دو سلولِ دیگر با همان `weight` پهن‌تر می‌شوند —
            // چیدمانِ تازه‌ای لازم نشد.
            if (ratePct > 0.0) {
                SpecCell("نرخ", "${toFa(fmtRate(ratePct))}٪ سالانه", null, privacyMode, Modifier.weight(1f))
            }
            SpecCell("پایان", endLabel ?: "—", null, privacyMode, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .defaultMinSize(minHeight = AppSpacing.minTouchTarget)
                .pressScaleClickable(onClick = { expanded = !expanded }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (expanded) "کمتر" else "بیشتر",
                color = AppPrimary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SpecRow("تعدادِ قسط", "${toFa(n)} قسط")
                if (totalInterest != null && totalInterest > 0.0) {
                    SpecRow("سودِ کل", null, totalInterest, privacyMode)
                }
                SpecRow("ضامن", if (borrower.isBlank() || borrower == "—") "ندارد" else borrower)
            }
        }
    }
}
/** یک سلولِ سه‌تاییِ بالای کارتِ مشخصات. */
@Composable
private fun SpecCell(
    label: String,
    value: String?,
    amount: Double?,
    privacyMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        if (value != null) {
            Text(value, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        } else {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(amount ?: 0.0)),
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}
/** یه ردیفِ «برچسبِ راست ← مقدارِ چپ» تو کارتِ مشخصات. مبلغ با حالتِ خصوصی ماسک می‌شه. */
@Composable
private fun SpecRow(
    label: String,
    value: String?,
    amount: Double? = null,
    privacyMode: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        if (value != null) {
            Text(value, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        } else {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(amount ?: 0.0)),
                    color = AppText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}
/**
 * **کارتِ هویتِ وام** - طرحِ مرجعِ کاربر (۳۱ شهریور).
 *
 * یک کارتِ رنگیِ بالای صفحه که در یک نگاه می‌گوید «این کدام وام است و چه شکلی است»:
 * نشانِ بانک در یک دایره، نامِ وام، بجِ وضعیت، و نوارِ چهار عددِ ثابتِ وام.
 *
 * ⚠️ **چهار عددِ این نوار هیچ‌وقت عوض نمی‌شوند** (مبلغِ وام، مدت، نرخ، تاریخِ شروع) -
 * برعکسِ کارتِ زیرش که همه‌چیزش با هر پرداخت تغییر می‌کند. همین مرز دلیلِ دو کارت
 * جدا بودن است، نه سلیقه.
 */
@Composable
internal fun LoanIdentityCard(
    name: String,
    bank: String,
    settled: Boolean,
    overdue: Boolean,
    amount: Double,
    months: Int,
    ratePct: Double,
    startLabel: String,
    paidFraction: Float,
    privacyMode: Boolean,
) {
    val statusLabel = when {
        settled -> "تسویه‌شده"
        overdue -> "معوق"
        else -> "فعال"
    }
    // وضعیت فقط روی **قرصِ سفید** رنگ می‌گیرد؛ خودِ کارت همیشه رنگِ تم است (خواسته‌ی کاربر:
    // «باکسِ بالا با تم عوض بشه») - قرمزِ کلِ کارت همان شلوغی‌ای بود که طرحِ تازه برداشت.
    val statusColor = if (overdue && !settled) AppDanger else AppPrimary
    AppHeroCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // کاشیِ برگ - همان نشانِ کارت‌های قهرمانِ دیگر (کیف، بودجه) تا این کارت هم خانواده‌شان باشد.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp)) {
                Text(
                    name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (bank.isNotBlank() && bank != "—") {
                    Text(
                        bank,
                        color = HeroMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusColor),
                    )
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
            // حلقه‌ی «٪ پرداخت‌شده» گوشه‌ی چپِ کارت (طرحِ ChatGPT).
            PaidRing(
                fraction = paidFraction,
                ringColor = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                centerTop = "${toFa((paidFraction * 100).roundToInt())}٪",
                centerBottom = "پرداخت شده",
                centerTopColor = Color.White,
                centerBottomColor = HeroMuted,
                size = 70.dp,
                stroke = 7.dp,
                centerTopSize = 15,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoanIdentityStat(
                label = "مبلغِ وام",
                value = maskIfPrivate(privacyMode, amountToman(amount)),
                unit = "تومان",
                modifier = Modifier.weight(1.3f),
            )
            HeroStatDivider()
            LoanIdentityStat(label = "مدتِ کل", value = toFa(months), unit = "ماه", modifier = Modifier.weight(1f))
            HeroStatDivider()
            LoanIdentityStat(label = "نرخِ سود", value = "${fmtRate(ratePct).faDigits()}٪", unit = "سالانه", modifier = Modifier.weight(1f))
            HeroStatDivider()
            LoanIdentityStat(label = "تاریخِ شروع", value = startLabel, unit = "", modifier = Modifier.weight(1.2f))
        }
    }
}
@Composable
private fun HeroStatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .fillMaxHeight(0.8f)
            .background(Color.White.copy(alpha = 0.25f)),
    )
}
/**
 * باکسِ سه‌عددیِ زیرِ کارتِ هویت (طرحِ ChatGPT): تعدادِ کلِ اقساط · مبلغِ هر قسط · سررسیدِ بعدی.
 * جانشینِ سه کارتِ جدای قبلی - **یک** کارت با دو خطِ جداکننده.
 */
@Composable
internal fun LoanKeyStatsCard(
    total: Int,
    installment: Double,
    installmentLabel: String,
    nextDueLabel: String?,
    dueInDays: Int?,
    privacyMode: Boolean,
) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyStat(
                icon = Icons.Filled.EventNote,
                tint = AppPrimaryInk,
                bg = AppPrimaryPill,
                label = "تعدادِ کلِ اقساط",
                value = toFa(total),
                unit = "قسط",
                modifier = Modifier.weight(1f),
            )
            KeyStatDivider()
            KeyStat(
                icon = Icons.Filled.Payments,
                tint = AppInfo,
                bg = AppInfoPill,
                label = installmentLabel,
                value = maskIfPrivate(privacyMode, amountToman(installment)),
                unit = "تومان",
                modifier = Modifier.weight(1.2f),
            )
            KeyStatDivider()
            KeyStat(
                icon = Icons.Filled.CalendarMonth,
                tint = AppPurpleInk,
                bg = AppPurplePill,
                label = "سررسیدِ بعدی",
                value = nextDueLabel ?: "—",
                unit = "",
                modifier = Modifier.weight(1f),
            ) {
                if (dueInDays != null) {
                    val late = dueInDays < 0
                    Text(
                        when {
                            late -> "${toFa(-dueInDays)} روز گذشته"
                            dueInDays == 0 -> "امروز"
                            else -> "${toFa(dueInDays)} روز مانده"
                        },
                        color = if (late || dueInDays == 0) AppDangerInk else AppInfo,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (late || dueInDays == 0) AppDangerPill else AppInfoPill)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}
@Composable
private fun KeyStatDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .fillMaxHeight(0.75f)
            .background(AppLine),
    )
}
@Composable
private fun KeyStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(999.dp)).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            color = AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        AutoShrinkText(
            value,
            color = AppText,
            maxFontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(unit, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        extra()
    }
}
/** یک دکمه‌ی نوارِ چسبانِ پایین (پرداخت / تقویم / حذف) - کپسولِ رنگی با آیکون. */
@Composable
internal fun LoanActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    ink: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .alpha(if (enabled) 1f else 0.6f)
            .pressScaleClickable { if (enabled) onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
        Text(
            label,
            color = ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
@Composable
private fun LoanIdentityStat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = HeroMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        AutoShrinkText(
            value,
            color = Color.White,
            maxFontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (unit.isNotBlank()) {
            Text(unit, color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
/**
 * **ریتمِ پرداخت** — فریمِ `80a`، جانشینِ حلقه‌ی درصد.
 *
 * 🚨 حلقه **جابه‌جا نشد، حذف شد**: همان ۱۷٪ حالا نوارِ تهِ هیروست، و یک درصد دو بار کشیدن
 * یعنی دو گرافیک برای یک عدد. جایش المانی آمد که **داده‌ی تازه** دارد: یک میله برای هر
 * قسط. سه میله‌ی قرمزِ پشتِ‌هم یعنی دیرکرد **پیوسته** بوده نه پراکنده — چیزی که هیچ عددِ
 * این صفحه نمی‌گوید.
 *
 * ⚠️ **میله‌ها ماسکِ حالتِ خصوصی نمی‌گیرند**: مبلغ نشان نمی‌دهند، فقط وضعیت.
 * ⚠️ بالای **۶۰** قسط میله‌ها به یک لکه می‌رسند، پس آن‌جا هر میله **یک سال** می‌شود و
 *    زیرنویس هم عوض می‌شود.
 * ⚠️ `ProgressRing.kt` حذف نشد — جای دیگری استفاده می‌شود؛ فقط از این صفحه برداشته شد.
 */
@Composable
internal fun PaymentRhythm(
    rows: List<Map<String, Any?>>,
    today: PersianDate,
    onOpenAll: () -> Unit,
) {
    if (rows.isEmpty()) return
    val paidColor = AppPrimary
    val lateColor = AppDanger
    val nextColor = AppAccent
    val emptyColor = AppSurface2

    // چهار حالتِ هر قسط، از همان داده‌ی فهرستِ اقساط. `MyLoansViewModel` تغییری لازم نداشت.
    val states = remember(rows, today) {
        var nextMarked = false
        rows.map { row ->
            val paid = row["paid"] == true
            val due = row["dueDate"] as? Map<*, *>
            val y = (due?.get("y") as? Number)?.toInt()
            val mo = (due?.get("m") as? Number)?.toInt()
            val d = (due?.get("d") as? Number)?.toInt()
            val past = y != null && mo != null && d != null &&
                (y < today.y || (y == today.y && (mo < today.m || (mo == today.m && d < today.d))))
            when {
                paid -> 0
                past -> 1
                !nextMarked -> {
                    nextMarked = true
                    2
                }
                else -> 3
            }
        }
    }
    val byYear = states.size > 60
    // بالای ۶۰ قسط هر میله یک سال است و **بدترین** حالتِ همان سال را می‌گیرد، چون خبرِ بد
    // نباید زیرِ میانگین گم شود.
    val allBars = if (!byYear) states else states.chunked(12).map { chunk -> chunk.minOrNull() ?: 3 }
    val allLabels = remember(rows, byYear) {
        if (byYear) {
            allBars.indices.map { "سالِ ${toFa(it + 1)}" }
        } else {
            rows.map { row ->
                val due = row["dueDate"] as? Map<*, *>
                (due?.get("m") as? Number)?.toInt()?.let { persianMonthName(it) } ?: ""
            }
        }
    }
    // 🎨 طرحِ مرجعِ کاربر (۳ مهر): ده میله‌ی کپسولی با نقطه روی خطِ پایه و نامِ ماه. با
    // قسط‌های زیاد یک **پنجره‌ی ده‌تایی** دورِ قسطِ جاری نشان داده می‌شود؛ «دیدنِ همه»
    // کلِ جدول را باز می‌کند.
    val window = 10
    val focus = allBars.indexOfFirst { it == 2 }.let { if (it < 0) allBars.lastIndex else it }
    val start = (focus - 5).coerceIn(0, (allBars.size - window).coerceAtLeast(0))
    val bars = allBars.drop(start).take(window)
    val labels = allLabels.drop(start).take(window)
    val futureColor = AppLine

    fun colorOf(state: Int) = when (state) {
        0 -> paidColor
        1 -> lateColor
        2 -> nextColor
        else -> futureColor
    }

    AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ریتمِ پرداخت", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(
                    if (byYear) "هر میله یک سال" else "هر میله یک قسط",
                    color = AppMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "دیدنِ همه",
                color = AppPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.pressScaleClickable(onClick = onOpenAll),
            )
        }
        val lineColor = AppLine
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .pressScaleClickable(scale = 0.99f, onClick = onOpenAll),
        ) {
            bars.forEachIndexed { index, state ->
                val current = state == 2
                val color = colorOf(state)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        // خطِ پایه‌ی نازک از وسطِ نقطه‌ها می‌گذرد.
                        .drawBehind {
                            val y = 58.dp.toPx() + 4.dp.toPx()
                            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(0.86f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (current) nextColor.copy(alpha = 0.16f) else Color.Transparent),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .width(16.dp)
                                .height(if (state == 3) 38.dp else if (current) 46.dp else 44.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.verticalGradient(
                                        0f to color.copy(alpha = 0.35f),
                                        0.28f to color.copy(alpha = 0.55f),
                                        0.3f to color,
                                        1f to color,
                                    ),
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier.padding(top = 2.dp).size(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (current) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(AppSurface)
                                    .border(2.dp, nextColor, CircleShape),
                            )
                        } else {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
                        }
                    }
                    Text(
                        labels.getOrElse(index) { "" },
                        color = if (current) AppText else AppMuted,
                        fontSize = 9.5.sp,
                        fontWeight = if (current) FontWeight.Black else FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // جمله‌ی «هر میله یک قسط» جدا بالای راهنما نشست تا در گوشیِ باریک راهنما جا شود.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, AppLine, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(0 to "پرداخت‌شده", 2 to "قسطِ جاری", 1 to "پرداخت‌نشده", 3 to "آینده").forEach { (st, name) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(colorOf(st)))
                        Text(
                            name,
                            color = AppMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 3.dp),
                        )
                    }
                }
            }
        }
    }
}
/** نرخ بدونِ اعشارِ اضافه: ۱۸ نه ۱۸٫۰، ولی ۴٫۵ سرِ جاش می‌مونه. */
private fun fmtRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()
