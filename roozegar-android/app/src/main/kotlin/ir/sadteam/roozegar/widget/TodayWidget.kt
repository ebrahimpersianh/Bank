package ir.sadteam.roozegar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ir.sadteam.roozegar.MainActivity
import ir.sadteam.roozegar.R
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.core.Occasions
import ir.sadteam.roozegar.core.PersianNames
import ir.sadteam.roozegar.core.toPersianDigits
import androidx.compose.runtime.Composable

/**
 * ویجت کوچیک «امروز»: عدد بزرگ روز + روز هفته + ماه و سال + مناسبت. پس‌زمینه‌ی شیشه‌ای از
 * widget_bg.xml میاد (drawable، پس رو همه‌ی نسخه‌های اندروید گرد و نیمه‌شفافه).
 * آپدیت روزانه از DateChangeReceiver (نیمه‌شب/بوت/تغییر ساعت) push می‌شه.
 */
class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val today = JalaliCalendar.today()
        val weekday = PersianNames.weekdays[JalaliCalendar.dayOfWeekSaturdayFirst(today)]
        val occasion = Occasions.titlesOf(today)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_bg))
                .padding(10.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                weekday,
                style = TextStyle(color = ColorProvider(Color(0xFF2DD4BF)), fontSize = 13.sp, textAlign = TextAlign.Center),
            )
            Text(
                today.d.toPersianDigits(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                "${PersianNames.months[today.m - 1]} ${today.y.toPersianDigits()}",
                style = TextStyle(color = ColorProvider(Color(0xFFB8C7D9)), fontSize = 13.sp, textAlign = TextAlign.Center),
            )
            if (occasion != null) {
                Text(
                    occasion,
                    maxLines = 1,
                    style = TextStyle(color = ColorProvider(Color(0xFFF2B04B)), fontSize = 11.sp, textAlign = TextAlign.Center),
                )
            }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}
