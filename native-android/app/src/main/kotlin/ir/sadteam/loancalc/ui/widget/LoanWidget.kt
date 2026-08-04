package ir.sadteam.loancalc.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import dagger.hilt.android.EntryPointAccessors
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.LoanRepository

private data class NextInstallment(val loanName: String, val amount: Double, val due: PersianDate)

private fun compareDates(a: PersianDate, b: PersianDate): Int {
    if (a.y != b.y) return a.y - b.y
    if (a.m != b.m) return a.m - b.m
    return a.d - b.d
}

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
                best = NextInstallment(loan.name, amount, candidate)
            }
        }
    }
    return best
}

/**
 * ویجتِ صفحه اصلیِ گوشی - نزدیک‌ترین قسطِ پرداخت‌نشده (از همه‌ی وام‌های ذخیره‌شده) رو بدونِ باز کردنِ
 * اپ نشون می‌ده. چون GlanceAppWidget بخشِ درختِ Activity نیست، LoanRepository رو از طریق
 * [LoanWidgetEntryPoint] (نه hiltViewModel) می‌گیره.
 */
object LoanWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, LoanWidgetEntryPoint::class.java)
            .loanRepository()
        val next = findNextInstallment(repository)

        provideContent {
            WidgetContent(next)
        }
    }
}

@Composable
private fun WidgetContent(next: NextInstallment?) {
    val context = androidx.glance.LocalContext.current
    // خودِ پس‌زمینه رنگِ تیره‌ی ثابت داره (مستقل از تمِ سیستم)، ولی قبلاً هیچ رنگِ متنی صریح
    // ست نشده بود - رنگِ پیش‌فرضِ Glance.Text رو خیلی گوشی‌ها تیره/مشکیه، که رو این پس‌زمینه‌ی
    // تیره عملاً غیرقابل‌خوندن می‌شه (دقیقاً همون «ویجت کلاً سیاهه» که کاربر گزارش داد).
    // چون خودِ پس‌زمینه (پایین‌تر) مستقل از تمِ سیستمه، رنگِ متن هم باید همیشه ثابت باشه - برای
    // همین day/night هردو رو یه مقدار می‌گیرن (ColorProvider تک‌آرگومانی برای رنگِ ثابت وجود نداره).
    val textStyle = TextStyle(color = ColorProvider(day = Color(0xFFEEF1F8), night = Color(0xFFEEF1F8)))
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Text("وام من", style = textStyle)
        if (next == null) {
            Text("قسطِ پرداخت‌نشده‌ای ثبت نشده", style = textStyle)
        } else {
            Text("قسط بعدی: ${next.loanName}", style = textStyle)
            Text("${fmt(next.amount)} ریال", style = textStyle)
            Text("سررسید: ${toFa(next.due.d)}/${toFa(next.due.m)}/${toFa(next.due.y)}", style = textStyle)
        }
    }
}

class LoanWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoanWidget
}
