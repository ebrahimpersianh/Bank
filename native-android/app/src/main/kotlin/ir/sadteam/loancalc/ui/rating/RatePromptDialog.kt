package ir.sadteam.loancalc.ui.rating

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * ═══════════ درخواستِ امتیاز + نظر ═══════════
 *
 * بازطراحی‌شده (۲۳ شهریور، خواسته‌ی صریحِ کاربر). نسخه‌ی قبلی یک `AlertDialog`ِ خامِ
 * متریال بود - سطحِ صورتیِ پیش‌فرضِ متریال، دکمه‌های غیرِ سیستمِ طراحی، و «دیگه نپرس»
 * وسطِ متن. حالا هم‌شکلِ [ir.sadteam.loancalc.ui.components.ConfirmDialog] است:
 * سطحِ `AppSurface`، گوشه‌ی ۲۴، و دکمه‌ی اصلی `GradientButton`.
 *
 * **پنج ستاره خودشان دکمه‌اند.** کاربر ستاره‌ها را می‌بیند و همان‌جا می‌زند؛ زدنِ هر
 * ستاره یعنی «بله» - همان [onRateNow]. این تپِ اضافه را حذف می‌کند بی‌آن‌که چیزی از
 * دست برود، چون امتیازِ واقعی را خودِ استور می‌گیرد نه ما.
 *
 * **نظرِ متنی همین‌جاست.** کاربر می‌تواند نظرش را تو همین دیالوگ بنویسد و با
 * «ارسال نظر» بفرستد - متن به پشتیبانی می‌رسد (دسته‌ی «پیشنهاد»)، نه به استور؛
 * استور متن را فقط از خودِ کاربر تو صفحه‌ی خودش قبول می‌کند. بعد از فرستادنِ
 * موفق، دیگر هیچ‌وقت این دیالوگ نمی‌آید.
 *
 * دو راهِ خروج **هم‌وزن نیستند**: «بعداً» عادی است و «دیگه نپرس» کم‌رنگ‌تر - چون
 * انتخابِ دائمی نباید به‌اشتباه زده شود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatePromptDialog(
    onRateNow: () -> Unit,
    onLater: () -> Unit,
    onDismissForever: () -> Unit,
    onSendFeedback: (String, (Boolean) -> Unit) -> Unit,
) {
    // ستاره‌ای که انگشت رویش است تا همان‌جا پر می‌شود - بازخوردِ لمسی، نه امتیازِ ذخیره‌شده.
    var hovered by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    // null = هنوز نفرستاده؛ true = رسید؛ false = نشد.
    var feedbackResult by remember { mutableStateOf<Boolean?>(null) }
    var sending by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onLater) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(AppRadius.sheet))
                .background(AppSurface)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (feedbackResult == true) {
                // ── تشکر بعد از فرستادنِ موفقِ نظر ──
                Box(
                    Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF2F9E6E).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2F9E6E), modifier = Modifier.size(34.dp)) }
                Text(
                    "ممنون! نظرت رسید.",
                    color = AppText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "نظرت رو خوندیم؛ اگه چیزی لازم بود از پشتیبانی جواب می‌دیم.",
                    color = AppMuted,
                    fontSize = 13.5.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )
                GradientButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
                    Text("باشه", fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                return@Column
            }

            // طرحِ Claude Design (۸ مهر): کاشیِ ستاره‌ی طلایی بالای عنوان، ستاره‌های طلایی.
            Box(
                Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(StarGold.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(34.dp)) }
            Text(
                "لذت بردی از «جیبک»؟",
                color = AppText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "یک امتیازِ ۵ ستاره تو استور خیلی به ما کمک می‌کند.",
                color = AppMuted,
                fontSize = 13.5.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // ⚠️ در RTL ستاره‌ی اول سمتِ راست است؛ چون همه‌شان یک کار می‌کنند،
                // ترتیبشان رفتاری عوض نمی‌کند و فقط پرشدنِ لمسی را می‌سازد.
                repeat(5) { index ->
                    val filled = index < hovered
                    val scale by animateFloatAsState(if (filled) 1.12f else 1f, label = "star")
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .pressScaleClickable {
                                hovered = index + 1
                                onRateNow()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "${index + 1} ستاره",
                            tint = if (filled) StarGold else AppLine,
                            modifier = Modifier.size(38.dp).scale(scale),
                        )
                    }
                }
            }

            // ── نظرِ متنی، همین‌جا ──
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("نظرت رو همین‌جا بنویس... (اختیاری)", color = AppMuted, fontSize = 13.5.sp) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = AppPrimary,
                    unfocusedIndicatorColor = AppLine,
                    cursorColor = AppPrimary,
                ),
            )
            if (feedbackResult == false) {
                Text(
                    "فرستاده نشد — دوباره تلاش کن، یا از بخش پشتیبانی بفرست.",
                    color = Color(0xFFE5484D),
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                )
            }

            GradientButton(
                onClick = {
                    if (feedbackText.isBlank() || sending) return@GradientButton
                    sending = true
                    onSendFeedback(feedbackText.trim()) { ok ->
                        sending = false
                        feedbackResult = ok
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (sending) "در حالِ فرستادن..." else "ارسال نظر", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }

            // دکمه‌ی ثانویه: باز کردنِ صفحه‌ی استور برای امتیاز.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppPrimaryPill)
                    .pressScaleClickable(onClick = onRateNow)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("بله! امتیاز می‌دم", color = AppPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppPrimaryPill)
                        .pressScaleClickable(onClick = onLater)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("بعداً", color = AppPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .pressScaleClickable(onClick = onDismissForever)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("دیگر نپرس", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


private val StarGold = androidx.compose.ui.graphics.Color(0xFFD69E2E)
