package ir.sadteam.loancalc.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman

/*
 * ویجتِ صفحه‌ی گوشی - فریم‌های `57a`..`57c`.
 *
 * Glance گرادیان، سایه، `Canvas` و شکلِ دلخواه ندارد و فونتِ Vazirmatn هم رویش نمی‌نشیند،
 * پس طرح فقط روی سه چیزی می‌ایستد که واقعاً هست: **وزن، رنگ، و فاصله**.
 *
 * توکن‌های `@Composable`ِ اپ (`AppText`، `AppPrimary`، ...) این‌جا کار نمی‌کنند - هر رنگ
 * باید جفتِ خامِ روشن/تیره باشد.
 */

// ─────────────────────────── پالت ───────────────────────────

/**
 * سطحِ ویجت. تیره **`AppSurface`ِ اپ** است نه سیاه: سیاهِ مطلق روی هر تصویرِ زمینه‌ای یک
 * سوراخ می‌شود، و همین چیزی بود که کاربر «ویجت کلاً سیاهه» گزارشش کرد.
 */
private val WidgetSurface = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF16221C))
private val WidgetInk = ColorProvider(day = Color(0xFF16221C), night = Color(0xFFF3F7F5))
private val WidgetMuted = ColorProvider(day = Color(0xFF6B7A73), night = Color(0xFF8B9A94))
private val WidgetLine = ColorProvider(day = Color(0xFFEEF3F0), night = Color(0xFF24332C))

private val WidgetBrand = ColorProvider(day = Color(0xFF0EA968), night = Color(0xFF4FD49A))
private val WidgetBrandPill = ColorProvider(day = Color(0xFFE9F7F0), night = Color(0xFF12352A))

private val WidgetDanger = ColorProvider(day = Color(0xFFC4342F), night = Color(0xFFFF8E8E))
private val WidgetDangerPill = ColorProvider(day = Color(0xFFFBE9E8), night = Color(0xFF3A1F1E))

/** فریمِ `57b`: مرزِ «کوچک». پایین‌ترش نامِ وام و تاریخ حذف می‌شوند. */
private val SMALL = DpSize(140.dp, 110.dp)
private val MEDIUM = DpSize(250.dp, 110.dp)

// ─────────────────────────── داده ───────────────────────────

private data class NextInstallment(
    val loanName: String,
    val amount: Double,
    val due: PersianDate,
    /** مثبت = آینده، صفر = امروز، منفی = گذشته. */
    val daysLeft: Int,
)

private fun compareDates(a: PersianDate, b: PersianDate): Int {
    if (a.y != b.y) return a.y - b.y
    if (a.m != b.m) return a.m - b.m
    return a.d - b.d
}

/**
 * چند روز تا سررسید. عددِ منفی یعنی عقب‌افتاده.
 *
 * ⚠️ نسخه‌ی تقریبیِ بسته (`jalaliDayIndex` با ماهِ ۳۱/۳۰ و کبیسه‌ی ۳۳ساله) **جایگزین شد**:
 * `JalaliCalendar.daysBetween` از قبل در `:core` هست، دقیق است و تست دارد. کپیِ چهارم
 * لازم نبود؛ خودِ همین تابع هم فقط یک نام‌گذاری روی آن است.
 */
private fun daysUntilToday(date: PersianDate): Int =
    JalaliCalendar.daysBetween(JalaliCalendar.today(), date)

private suspend fun findNextInstallment(repo: LoanRepository): NextInstallment? {
    var best: NextInstallment? = null
    repo.getLoans().forEach { loan ->
        repo.getRows(loan).forEach { row ->
            if (row["paid"] == true) return@forEach
            val due = row["dueDate"] as? Map<*, *> ?: return@forEach
            val y = (due["y"] as? Number)?.toInt() ?: return@forEach
            val m = (due["m"] as? Number)?.toInt() ?: return@forEach
            val d = (due["d"] as? Number)?.toInt() ?: return@forEach
            val amount = (row["installment"] as? Number)?.toDouble() ?: loan.installment
            val candidate = PersianDate(y, m, d)
            val current = best
            if (current == null || compareDates(candidate, current.due) < 0) {
                best = NextInstallment(loan.name, amount, candidate, daysUntilToday(candidate))
            }
        }
    }
    return best
}

// ─────────────────────────── ویجت ───────────────────────────

object LoanWidget : GlanceAppWidget() {

    /** فریمِ `57b`: دو اندازه، و Glance خودش نزدیک‌ترین را انتخاب می‌کند. */
    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, LoanWidgetEntryPoint::class.java)
            .loanRepository()
        val next = findNextInstallment(repository)

        provideContent { WidgetContent(next) }
    }
}

@Composable
private fun WidgetContent(next: NextInstallment?) {
    val context = LocalContext.current
    val compact = LocalSize.current.width < MEDIUM.width

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetSurface)
            // گوشه‌ی گرد روی اندروید ۱۲+ از همین می‌آید؛ پایین‌ترها تیز می‌مانند و طرح
            // نمی‌شکند. اگر یک‌دستیِ کامل خواستید، `background(ImageProvider(R.drawable
            // .widget_surface))` جایش می‌نشیند (فریمِ `57c` بندِ ۶).
            .cornerRadius(20.dp)
            .padding(horizontal = if (compact) 13.dp else 15.dp, vertical = if (compact) 13.dp else 14.dp)
            // 🚨 باگِ «تپ رو ویجت اپ رو باز نمی‌کنه»: کانتکستِ ویجت اکتیویتی نیست، پس
            // بدونِ `FLAG_ACTIVITY_NEW_TASK` اندروید اصلاً اکتیویتی رو بالا نمی‌آره.
            //
            // فریمِ `57c` بندِ ۳: مقصد **تبِ سررسید** است نه خانه - ویجت درباره‌ی سررسید
            // حرف می‌زند، و تپی که به خانه برود کاربر را یک قدم عقب می‌اندازد.
            // ⚠️ `EXTRA_OPEN_DUE_TAB` را در `MainActivity` بخوانید و به همان پلِ
            // `DeepLinkTarget`ِ اعلان‌ها بدهید؛ من فقط می‌فرستمش.
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_OPEN_DUE_TAB, true)
                    },
                ),
            ),
    ) {
        if (next == null) {
            HeaderRow(label = "جیبک", pill = null, late = false)
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                "قسطِ پرداخت‌نشده‌ای نداری",
                style = TextStyle(
                    color = WidgetMuted,
                    fontSize = if (compact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            return@Column
        }

        // تنها حالتی که رنگ عوض می‌شود. **فقط جوهر**، نه پس‌زمینه: ویجتِ تمام‌قرمز روی
        // صفحه‌ی گوشی هشدارِ سیستمی خوانده می‌شود، نه یادآورِ قسط (`57c` بندِ ۱).
        val late = next.daysLeft < 0
        val deadline = when {
            next.daysLeft < 0 -> "${toFa(-next.daysLeft)} روز گذشته"
            next.daysLeft == 0 -> "امروز"
            else -> "${toFa(next.daysLeft)} روز مانده"
        }

        HeaderRow(label = if (late) "عقب‌افتاده" else "قسطِ بعدی", pill = if (compact) null else deadline, late = late)

        Spacer(modifier = GlanceModifier.defaultWeight())

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                // ⚠️ `fmt()` جداکننده‌ی **لاتین** می‌دهد، پس خطِ مبلغ لاتین بود و خطِ
                // تاریخِ زیرش فارسی - یک ویجت، دو الفبا. همان الگوی `amountToman`ِ
                // بسته‌ی وام.
                fmt(rialToToman(next.amount.toLong()).toDouble()).faDigits(),
                style = TextStyle(
                    color = if (late) WidgetDanger else WidgetInk,
                    fontSize = if (compact) 21.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.width(5.dp))
            Text(
                "تومان",
                style = TextStyle(color = WidgetMuted, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Medium),
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        if (compact) {
            // فریمِ `57b`: نامِ وام و تاریخ **می‌روند**. نامِ «وامِ مسکن · بانکِ مسکن» در
            // ۱۴۰dp به سه حرف و سه‌نقطه می‌رسد، که از نبودنش بدتر است؛ و تاریخ حرفِ دومِ
            // مهلت است - کسی که «۵ روز مانده» را خوانده «۲۰ مهر» را لازم ندارد.
            Text(
                deadline,
                style = TextStyle(
                    color = if (late) WidgetDanger else WidgetBrand,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        } else {
            Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(WidgetLine)) {}
            Spacer(modifier = GlanceModifier.height(9.dp))
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    next.loanName,
                    maxLines = 1,
                    style = TextStyle(color = WidgetInk, fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    "${toFa(next.due.d)} ${persianMonthName(next.due.m)}",
                    style = TextStyle(color = WidgetMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(label: String, pill: String?, late: Boolean) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // نشانِ تک‌رنگِ اعلان‌ها - همان `ic_notification`ِ بخشِ ۵۰. سکه ندارد و همین درست
        // است: در این اندازه سکه از بدنه جدا نمی‌شود و سیلوئت «آدم» می‌دهد.
        Image(
            provider = ImageProvider(R.drawable.ic_notification),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (late) WidgetDanger else WidgetBrand),
            modifier = GlanceModifier.size(15.dp),
        )
        Spacer(modifier = GlanceModifier.width(7.dp))
        Text(
            label,
            style = TextStyle(color = WidgetMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium),
            modifier = GlanceModifier.defaultWeight(),
        )
        if (pill != null) {
            Text(
                pill,
                style = TextStyle(
                    color = if (late) WidgetDanger else WidgetBrand,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = GlanceModifier
                    .background(if (late) WidgetDangerPill else WidgetBrandPill)
                    .cornerRadius(8.dp)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}


/** کلیدِ اکسترای «تبِ سررسید را باز کن» - در `MainActivity` خوانده می‌شود. */
const val EXTRA_OPEN_DUE_TAB = "ir.sadteam.loancalc.OPEN_DUE_TAB"

class LoanWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoanWidget
}
