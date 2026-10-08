package ir.sadteam.loancalc

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

// ترتیب/محتوای کاملِ تورِ راهنمای اولین ورود (AppTourOverlay) - رجوع کن به همون کامپوننت پایین‌تر
// برای جزئیاتِ فنیِ اسپاتلایت. هر مرحله یه المانِ *واقعیِ* رو صفحه رو هدف می‌گیره (مختصاتش تو
// tourBounds تو LoanCalcApp اندازه‌گیری می‌شه) - نه یه توضیحِ مستقلِ بدونِ هدف.
internal enum class TourTarget(val title: String, val hint: String) {
    // ⚠️ قدم‌های «حالت خصوصی»/«تمِ روشن-تاریک»/«تنظیمات» حذف شدن: بعدِ بازطراحیِ Duolingo دیگه
    // نوارِ بالای ثابتی وجود نداره که این سه آیکون توش بشینن (هر تب هدرِ خودشو داره، تم رفته
    // داخلِ تنظیمات، و درِ ورودیِ تنظیمات آدمکِ هدرِ خانه‌ست). قدمِ توری که المانِ واقعی نداره
    // فقط یه اسپاتلایتِ خالی می‌شه.
    ASSETS(
        "دارایی",
        "حساب‌ها و تراکنش‌هات رو اینجا ثبت و پیگیری کن.",
    ),
    REPORT(
        "گزارش",
        "با فیلترِ حساب و بازه‌ی دلخواه، گزارشِ دخل‌وخرجت رو ببین و PDF/اکسل بگیر.",
    ),
    BUDGET(
        "بودجه",
        "برای هر دسته‌بندی یه سقفِ ماهانه بذار تا هزینه‌هات دستت باشه.",
    ),

    // جای راهنمای متنیِ زیرِ نوار. یک‌بار، همان‌جایی که کاربر تازه با نوار آشنا شده، و بعد
    // دیگر هیچ‌وقت - به‌جای متنی که همیشه آن پایین بماند.
    REORDER(
        "نوار رو خودت بچین",
        "روی هر دکمه‌ی نوارِ پایین نگه‌دار تا جایش رو عوض کنی - آخرین قدمِ تور!",
    ),
}
/** مختصاتِ آیکونِ نوارِ پایینِ یه تب رو تویِ [tourBounds] برای قدمِ تورِ مربوط به همون تب ثبت
 * می‌کنه. */
internal fun registerTabTourBounds(
    route: String,
    rect: Rect,
    tourBounds: MutableMap<TourTarget, Rect>,
) {
    // ⚠️ از **route** کلید می‌گیره نه از `BottomTab`، چون بعدِ بخشِ ۴۱ نوار دیگه لزوماً همون پنج
    // تبِ ثابت نیست؛ تبی که کاربر برداشته باشه اصلاً رندر نمی‌شه و مختصاتش ثبت نمی‌شه (تور هم
    // برای همون قدم به‌درستی چیزی اسپاتلایت نمی‌کنه، به‌جای اینکه یه مستطیلِ کهنه نشون بده).
    when (route) {
        BottomTab.ASSETS.route -> tourBounds[TourTarget.ASSETS] = rect
        else -> {}
    }
    ir.sadteam.loancalc.ui.components.Guide.bounds["tab_$route"] = rect
    when (route) {
        BottomTab.REPORT.route -> tourBounds[TourTarget.REPORT] = rect
        BottomTab.BUDGET.route -> tourBounds[TourTarget.BUDGET] = rect
    }
}
/** قدم‌های راهنمای تعاملی؛ آخرش مرورِ سریعِ چند قابلیت (خواسته‌ی کاربر). */
internal fun guideSteps() = listOf(
    ir.sadteam.loancalc.ui.components.GuideStep("tab_assets", "همه‌چیز از حساب شروع می‌شود", "روی «دارایی» بزن."),
    ir.sadteam.loancalc.ui.components.GuideStep("add_account", "اولین حسابت را بساز", "روی + بزن، اسمِ حساب (نقدی، کارت یا بانک) و موجودیِ امروزش را بنویس و ذخیره کن."),
    ir.sadteam.loancalc.ui.components.GuideStep("tab_home", "برگردیم خانه", "روی «خانه» بزن."),
    ir.sadteam.loancalc.ui.components.GuideStep("add_tx", "اولین خرج یا درآمدت", "این دکمه را بزن و یک خرج یا درآمد ثبت کن؛ از حسابت کم یا به آن اضافه می‌شود."),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "ثبتِ خودکار با پیامکِ بانک", "اگر بخواهی خرج‌ها خودشان ثبت شوند: تنظیمات ← پیامک‌های بانکی. متنِ پیامک فقط روی گوشی خوانده می‌شود.", optional = true),
    ir.sadteam.loancalc.ui.components.GuideStep("tab_loan", "وام‌ها و چک‌ها", "روی «وام» بزن؛ وام‌هایت را بگذار تا سررسیدِ هر قسط یادت بیاید.", optional = true),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "افزودنِ وام", "در «وام‌های من» با دکمه‌ی + وام را اضافه کن؛ پرداختِ هر قسط از حسابت کم می‌شود.", optional = true),
    ir.sadteam.loancalc.ui.components.GuideStep("shortcuts", "همه‌ی ابزارها این‌جاست", "این دستگیره را بالا بکش یا بزن: چک، دنگ، قبض، تسویه‌ی بدهی و بقیه.", optional = true),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "بودجه و هشدار", "برای هر دسته سقفِ ماهانه بگذار؛ در ۸۰٪ خبرت می‌کنیم."),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "گزارش و دارایی", "نمودارِ خرج‌ها در «گزارش»؛ طلا، ارز و رمزارز با قیمتِ روز در «دارایی»."),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "چک، قبض، دنگ", "سررسیدِ چک و قبض یادآوری می‌شود؛ خرجِ مشترک را با دنگ تقسیم کن."),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "امن و همیشه همراه", "قفل با رمز یا اثرِ انگشت، حالتِ خصوصی، و ذخیره‌ی ابری روی هر گوشیِ تازه."),
    ir.sadteam.loancalc.ui.components.GuideStep(null, "آماده‌ای! +۵۰ سکه", "۵۰ سکه‌ی جایزه به کیفت رفت (فقط بارِ اول). هر وقت خواستی این راهنما را از تنظیمات ← «راهنمای برنامه» دوباره ببین."),
)
/**
 * تورِ راهنمای اولین ورود، به‌صورتِ یه اورلیِ spotlight واقعی رو خودِ چیدمانِ اپ - نه یه صفحه‌ی
 * جدای قبل از ورود. یه لایه‌ی تیره‌ی نیمه‌شفاف کلِ صفحه رو می‌پوشونه با یه «سوراخِ» گردگوشه دقیقاً
 * دورِ هدفِ فعلی (از رو [bounds]، مختصاتِ واقعیِ اندازه‌گیری‌شده - نه حدسی)، + یه کارتِ توضیح که
 * همیشه بالای نوارِ تب می‌شینه (موقعیتش ثابته، فقط سوراخ جابه‌جا می‌شه). قدم‌هایی که رو یه تبِ
 * خاص زندگی می‌کنن (رجوع کن به [TourTarget.asBottomTab]) موقعِ رسیدن، [onStepChanged] رو صدا
 * می‌زنن تا LoanCalcApp خودش به همون تب ناوبری کنه - این‌جوری المانِ هدف (مثلاً دکمه‌ی افزودنِ
 * دستی که فقط رو تبِ «وام‌های من» وجود داره) همیشه واقعاً رندر و قابل‌اندازه‌گیریه. لمسِ هرجای
 * دیگه‌ی صفحه (به‌جز دکمه‌های خودِ کارت) قدمِ بعد رو فعال می‌کنه؛ آخرین قدم «متوجه شدم» تور رو تموم
 * می‌کنه.
 */
@Composable
internal fun AppTourOverlay(
    steps: List<TourTarget>,
    bounds: Map<TourTarget, Rect>,
    onStepChanged: (TourTarget) -> Unit,
    onDone: () -> Unit,
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps.getOrNull(stepIndex) ?: return
    LaunchedEffect(currentStep) { onStepChanged(currentStep) }
    val rect = bounds[currentStep]
    val isLastStep = stepIndex == steps.lastIndex
    val advance: () -> Unit = { if (isLastStep) onDone() else stepIndex++ }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { advance() },
        ) {
            val scrimPath = Path().apply { addRect(Rect(Offset.Zero, size)) }
            if (rect != null) {
                val holePath = Path().apply {
                    addRoundRect(RoundRect(rect.inflate(8f), CornerRadius(18f, 18f)))
                }
                scrimPath.op(scrimPath, holePath, PathOperation.Difference)
            }
            drawPath(scrimPath, color = Color.Black.copy(alpha = 0.72f))
        }

        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 110.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(currentStep.title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    currentStep.hint,
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDone) {
                        Text("رد کن", color = AppMuted, fontSize = 12.5.sp)
                    }
                    GradientButton(onClick = advance) {
                        Text(if (isLastStep) "متوجه شدم" else "بعدی")
                    }
                }
            }
        }
    }
}
