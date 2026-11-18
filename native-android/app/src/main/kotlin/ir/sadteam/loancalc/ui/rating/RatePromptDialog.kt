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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * ═══════════ درخواستِ امتیاز + نظر ═══════════
 *
 * مینی‌مال و قشنگ (۲۷ آبان، تأییدِ کاربر): فقط عنوان، پنج ستاره، باکسِ نظر،
 * یک دکمه‌ی «ارسال نظر» و دو لینکِ ساده‌ی «بعداً» / «دیگر نپرس» + یک خطِ
 * راهنما پایینِ دیالوگ. کاشیِ ستاره و دکمه‌ی اضافه‌ی «بله! امتیاز می‌دم»
 * برداشته شد - ستاره‌ها خودشان استور را باز می‌کنند.
 *
 * **نظر به پشتیبانی نمی‌رود - هدف زیادشدنِ نظرهای استور است.** کاربر متن را
 * همین‌جا می‌نویسد؛ با «ارسال نظر» متن تو کلیپ‌بورد کپی می‌شود و صفحه‌ی
 * نظرِ استور باز می‌شود (مایکت: intent رسمیِ `myket://comment`؛ بازار:
 * صفحه‌ی اپ)؛ تو استور کاربر با یک لمس «چسباندن» را می‌زند و نظر را ثبت
 * می‌کند. هیچ استوری متنِ نظر را از برنامه قبول نمی‌کند - ثبتِ نهایی همیشه
 * دستِ خودِ کاربر تو استور است.
 *
 * بعد از ثبت، دیالوگ برای همیشه دیگر نمی‌آید.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatePromptDialog(
    onRateNow: () -> Unit,
    onLater: () -> Unit,
    onDismissForever: () -> Unit,
    onSubmitReview: (String) -> Unit,
) {
    // ستاره‌ای که انگشت رویش است تا همان‌جا پر می‌شود - بازخوردِ لمسی، نه امتیازِ ذخیره‌شده.
    var hovered by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onLater) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(AppRadius.sheet))
                .background(AppSurface)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "لذت بردی از «جیبک»؟",
                color = AppText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
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

            // ── نظرِ متنی، همین‌جا (باکسِ نرم و قشنگ) ──
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("نظرت رو همین‌جا بنویس... (اختیاری)", color = AppMuted, fontSize = 13.5.sp) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                    focusedIndicatorColor = Color.White.copy(alpha = 0.10f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.10f),
                    cursorColor = AppPrimary,
                ),
            )

            GradientButton(
                onClick = { onSubmitReview(feedbackText.trim()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ارسال نظر", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            // «بعداً» و «دیگر نپرس» - لینکِ ساده، نه دکمه.
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    "بعداً",
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.pressScaleClickable(onClick = onLater),
                )
                Text(
                    "دیگر نپرس",
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.pressScaleClickable(onClick = onDismissForever),
                )
            }

            // راهنما: کاربر تو استور گیج نشود.
            Text(
                buildAnnotatedString {
                    append("بعد از ثبت، نظرت کپی می‌شه؛ تو استور تو کادرِ نظر ")
                    withStyle(SpanStyle(color = AppPrimary, fontWeight = FontWeight.Bold)) {
                        append("«چسباندن»")
                    }
                    append(" رو بزن.")
                },
                color = AppMuted,
                fontSize = 11.5.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}


private val StarGold = androidx.compose.ui.graphics.Color(0xFFD69E2E)
