package ir.sadteam.loancalc.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.text.Text
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch

/** یک صفحه‌ی اینترو: تصویر، عنوان، یک جمله، و سه امکانِ واقعی. */
private data class IntroPage(@DrawableRes val image: Int, val title: String, val body: String, val points: List<String>)

/**
 * اینتروی تازه (۱۳ آبان) - از طرحِ Claude Design «اینتروی تازه‌ی جیبک» (۷ صفحه، ۱۰ آبان).
 *
 * خواسته‌ی کاربر (یافته‌ی ۴): اینتروی قبلی ناقص بود و خیلی از قابلیت‌ها را نمی‌گفت. هر صفحه یک
 * کارِ اصلیِ برنامه. ⚠️ تصویرها **همان تصویرهای موجودِ برنامه‌اند** - حجمِ APK دست نمی‌خورد
 * (تصمیمِ قفل). رنگ‌ها توکنِ تم‌اند، پس در روشن و تاریک هر دو درست است (طرح فقط تاریک بود).
 */
private val introPages = listOf(
    IntroPage(
        R.drawable.jibak_onb_welcome,
        "همه‌ی پولت، یک‌جا",
        "درآمد و خرج، وام و قسط، چک و قبض، بودجه و دارایی‌هات - ساده، فارسی و امن.",
        listOf("ثبتِ خودکار از پیامکِ بانک", "یادآورِ وام، چک و قبض", "بودجه، دنگ و گزارش"),
    ),
    IntroPage(
        R.drawable.sms_illu_phone,
        "خرج‌ها خودشان ثبت می‌شوند",
        "پیامک یا اعلانِ بانک که بیاید، جیبک مبلغ و دسته را می‌خواند و ثبت می‌کند؛ تو فقط تأیید کن.",
        listOf("هم پیامک، هم اعلانِ اپِ بانک", "موجودی با بانک یکی می‌شود، با یک لمس", "فقط پیامِ بانک خوانده می‌شود؛ بقیه نه"),
    ),
    IntroPage(
        R.drawable.jibak_onb_account,
        "همه‌ی حساب‌ها، کنارِ هم",
        "نقدی، کارت‌ها و حساب‌های بانکی؛ هر کدام موجودیِ خودش، همه با هم در یک نگاه.",
        listOf("درآمد و خرج با دسته‌بندی", "انتقال بینِ حساب‌های خودت", "تومان یا ریال"),
    ),
    IntroPage(
        R.drawable.jibak_home_overdue, // ۳۷: تصویرِ قبلی (کاغذِ وام) در حالتِ تاریک هاله‌ی سفید داشت
        "وام، چک و قبض؛ یادت می‌ماند",
        "قبل از هر سررسید یادت می‌اندازیم. قسطِ عقب‌افتاده جا نمی‌ماند.",
        listOf("ماشین‌حسابِ وام و سپرده", "جدولِ کاملِ اقساط", "یادآورِ چک و قبض"),
    ),
    IntroPage(
        R.drawable.empty_illu_budget,
        "بودجه بگذار، دنگ حساب کن",
        "برای هر دسته سقفِ ماهانه بگذار و قبل از زیاده‌روی خبردار شو. خرجِ جمعی را هم با دوستانت تقسیم کن.",
        listOf("هشدار در ۸۰٪ِ سقف", "سهمِ دوستان در «طلب و بدهی»", "تسویه با یک لمس"),
    ),
    IntroPage(
        R.drawable.empty_illu_report,
        "ببین پولت کجا می‌رود",
        "گزارشِ هفته، ماه، فصل و سال با نمودار؛ ارزشِ کلِ دارایی‌ها با قیمتِ روزِ طلا، دلار و رمزارز.",
        listOf("سهمِ هر دسته از خرج", "روندِ کلِ دارایی", "خروجیِ اکسل و PDF"),
    ),
    IntroPage(
        R.drawable.empty_illu_wallet,
        "امن، همیشه همراهت",
        "اطلاعاتت رمزگذاری‌شده روی گوشیِ خودت است و هر روز خودکار پشتیبان گرفته می‌شود.",
        listOf("قفل با رمز و اثرِ انگشت", "پنهان‌کردنِ مبلغ‌ها با یک لمس", "تقویمِ شمسی"),
    ),
)

@Composable
internal fun IntroPager(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == introPages.lastIndex
    LaunchedEffect(pager.currentPage) { ir.sadteam.loancalc.data.UsageStats.action("intro_page_${pager.currentPage}") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                repeat(introPages.size) { i ->
                    val w by animateDpAsState(if (i == pager.currentPage) 22.dp else 6.dp, label = "introDot")
                    Box(
                        Modifier.height(6.dp).width(w).clip(CircleShape)
                            .background(if (i <= pager.currentPage) AppPrimary else AppLine),
                    )
                }
            }
            if (!last) TextButton(onClick = onDone) { Text("رد شو", color = AppMuted, fontSize = 14.sp) }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = introPages[index]
            // تستِ ۱۳ آبان: محتوا بالا می‌چسبید و نیمه‌ی پایین خالی بود - حالا وسطِ صفحه.
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painterResource(page.image),
                    contentDescription = null,
                    modifier = Modifier.size(if (index == 0) 200.dp else 170.dp),
                )
                Text(
                    page.title,
                    color = AppText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )
                Text(
                    page.body,
                    color = AppMuted,
                    fontSize = 14.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    page.points.forEach { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(24.dp).clip(CircleShape).background(AppPrimaryPill),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(14.dp))
                            }
                            Text(point, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                }
            }
        }

        GradientButton(
            onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (last) "شروع کن" else if (pager.currentPage == 0) "بزن بریم" else "بعدی", fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(24.dp))
    }
}
