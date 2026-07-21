package ir.sadteam.roozegar.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ir.sadteam.roozegar.MainActivity
import ir.sadteam.roozegar.R
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.core.MonthGrid
import ir.sadteam.roozegar.core.Occasions
import ir.sadteam.roozegar.core.PersianDate
import ir.sadteam.roozegar.core.PersianNames
import ir.sadteam.roozegar.core.toPersianDigits

private val TextColor = ColorProvider(Color(0xFFEAF2F8))
private val MutedColor = ColorProvider(Color(0xFF8FA3B8))
private val HolidayColor = ColorProvider(Color(0xFFFF8585))
private val GoldColor = ColorProvider(Color(0xFFF2B04B))

/** ویجت ماه کامل: گرید ماه جاری با امروزِ برجسته و تعطیلات قرمز - مثل ویجت ماه روزگار iOS. */
class MonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val today = JalaliCalendar.today()
        val grid = MonthGrid.of(today.y, today.m)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_bg))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${PersianNames.months[today.m - 1]} ${today.y.toPersianDigits()}",
                style = TextStyle(color = GoldColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
            )
            // سطر حروف هفته - Glance تو RTL خودش ترتیب Row رو برمی‌گردونه
            Row(GlanceModifier.fillMaxWidth()) {
                PersianNames.weekdayInitials.forEachIndexed { i, init ->
                    Box(GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                        Text(
                            init,
                            style = TextStyle(
                                color = if (i == 6) HolidayColor else MutedColor,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }
            for (week in 0 until grid.weeks) {
                Row(GlanceModifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayNum = week * 7 + col - grid.leadingBlanks + 1
                        Box(GlanceModifier.defaultWeight().padding(1.dp), contentAlignment = Alignment.Center) {
                            if (dayNum in 1..grid.daysInMonth) {
                                val date = PersianDate(today.y, today.m, dayNum)
                                val isToday = dayNum == today.d
                                val isHoliday = col == 6 || Occasions.isHoliday(date)
                                Box(
                                    modifier = if (isToday) {
                                        GlanceModifier.background(ImageProvider(R.drawable.widget_today_bg)).padding(horizontal = 5.dp)
                                    } else {
                                        GlanceModifier
                                    },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        dayNum.toPersianDigits(),
                                        style = TextStyle(
                                            color = when {
                                                isToday -> ColorProvider(Color(0xFF0B1626))
                                                isHoliday -> HolidayColor
                                                else -> TextColor
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class MonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthWidget()
}
