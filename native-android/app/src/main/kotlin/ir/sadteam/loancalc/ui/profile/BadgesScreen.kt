package ir.sadteam.loancalc.ui.profile

import ir.sadteam.loancalc.ui.settings.SettingsHero
import ir.sadteam.loancalc.ui.settings.SettingsGroupLabel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.data.BadgeProgress
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.SettledMedal
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.roundToInt

/**
 * **دستاوردها** - فریمِ `18a`.
 *
 * سه گروه به همین ترتیب: **در جریان · باز شده · قفل**. گروهِ «در جریان» حلقه‌ی درصد
 * می‌گیره، «باز شده» مدالِ روبان‌دار، و «قفل» مدالِ خاکستریِ بی‌روبان.
 *
 * ⚠️ **الان هیچ نشانی `comingSoon` نیست** (هدفِ پس‌انداز تنها موردش بود و ساخته شد)،
 * پس این شاخه فعلاً مرده است. عمداً نگه داشته شد به‌عنوانِ نقطه‌ی اتصالِ نشانِ بعدی که
 * قابلیتش هنوز نیامده - حذفش یعنی دفعه‌ی بعد دوباره همین سه جا را بنویسیم.
 *
 * نشانی که شرطش هنوز قابلِ سنجش نیست (`comingSoon`) تو گروهِ قفل با متنِ «به‌زودی»
 * می‌شینه، نه اینکه قایم بشه - کاربر باید بدونه چند تا نشان هست.
 */
@Composable
fun BadgesScreen(viewModel: GamificationViewModel = hiltViewModel()) {
    val badges by viewModel.badges.collectAsState()
    val coins by viewModel.coins.collectAsState()
    LaunchedEffect(Unit) { viewModel.syncBadges() }

    val unlocked = badges.filter { it.unlocked }
    val inProgress = badges.filter { !it.unlocked && (it.progress ?: 0f) > 0f }
    val locked = badges.filter { !it.unlocked && (it.progress ?: 0f) <= 0f }

    Column(modifier = Modifier.fillMaxWidth()) {
        // کارتِ رنگیِ بالا (با تم عوض می‌شود): پیشرفت و سکه‌ها، هر دو از state واقعی.
        SettingsHero(
            Icons.Filled.MilitaryTech,
            "نشان‌های من",
            "${toFa(unlocked.size)} از ${toFa(badges.size)} نشان باز شده · هر نشان یک‌بار",
            badge = "${toFa(coins)} سکه",
        ) {
            val fraction = if (badges.isEmpty()) 0f else unlocked.size.toFloat() / badges.size
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White),
                )
            }
        }

        if (inProgress.isNotEmpty()) {
            SettingsGroupLabel("در جریان", accent = AppPrimary)
            inProgress.forEach { BadgeRow(it) }
        }
        if (unlocked.isNotEmpty()) {
            SettingsGroupLabel("باز شده", accent = AppAccent)
            unlocked.forEach { BadgeRow(it) }
        }
        if (locked.isNotEmpty()) {
            SettingsGroupLabel("قفل", accent = AppMuted)
            locked.forEach { BadgeRow(it) }
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text,
        color = AppLabel,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp, end = 4.dp),
    )
}

@Composable
private fun BadgeRow(item: BadgeProgress) {
    val badge = item.badge
    val progress = item.progress ?: 0f
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                // وامِ بسته مدالِ روبان‌دارِ خودش را نگه می‌دارد - کاربر همان را در مرکزِ وام دیده.
                item.unlocked && badge == Badge.LOAN_CLOSED -> SettledMedal(diskSize = 34.dp)
                // باز شده یا قفل → مدالِ مشترک؛ تمایز از نمادِ داخل می‌آید نه از رنگ.
                // `BadgeMedal` خودش دیسکِ طلاییِ باز، چیپِ قفل، و حالتِ «به‌زودی» را می‌گیرد.
                item.unlocked || progress <= 0f -> BadgeMedal(badge, item.unlocked, size = 34.dp)
                // در جریان → حلقه‌ی درصد. مدال نمی‌گیرد: درصد خبرِ تازه است و نمادِ نشان
                // در همین ردیف تکرارِ برچسبِ کنارش می‌شد.
                else -> Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    val track = AppSurface2
                    val arc = AppPrimary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = androidx.compose.ui.geometry.Size(
                            size.width - stroke,
                            size.height - stroke,
                        )
                        drawArc(
                            color = track,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = arc,
                            startAngle = -90f,
                            sweepAngle = -360f * progress.coerceIn(0f, 1f),
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                    Text(
                        "${toFa((progress * 100).roundToInt())}٪",
                        color = AppPrimaryInk,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(badge.label, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                Text(
                    if (badge.comingSoon) "به‌زودی" else badge.hint,
                    color = AppMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                // نوارِ پیشرفتِ واقعی - فقط برای نشانِ در جریان.
                if (!item.unlocked && progress > 0f) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSurface2),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(AppPrimary),
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(if (item.unlocked) Modifier else Modifier.alpha(0.6f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppGoldPillSoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    toFa(badge.coins),
                    color = if (item.unlocked) AppPrimaryInk else AppMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                )
                CoinIcon(size = 13.dp, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}
