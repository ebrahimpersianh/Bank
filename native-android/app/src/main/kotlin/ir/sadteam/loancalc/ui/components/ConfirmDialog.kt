package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * ═══════════ تاییدِ کنش، سراسری ═══════════
 *
 * فریمِ `46a`. پانزده جای برنامه دیالوگِ تایید دارند و شکلشان یکی نبود. این مرجعِ
 * واحد است: **یک قالب، سه لحن** که فقط در رنگِ آیکون و برچسبِ دکمه فرق دارند، نه در
 * ساختار.
 *
 * ── کدام کنش کدام را می‌گیرد (فریمِ `46c`) ──
 *
 * **دیالوگ** فقط برای کاری که **با یک تپ برنمی‌گردد**: حذف · پرداخت · تغییرِ رقمی که
 * تاریخچه را جابه‌جا می‌کند · خروج از حساب · پاک‌کردنِ داده.
 *
 * **واگرد** ([UndoBar]) برای کاری که حالتش برگشت‌پذیر است: تسویه‌شد · آرشیو ·
 * بی‌صداکردنِ یادآور · جابه‌جاییِ دسته.
 *
 * **هیچ‌کدام** برای افزودن: ثبتِ تراکنش، ساختِ دسته، افزودنِ حساب. پرسیدن قبل از افزودن
 * کاری است که هیچ برنامه‌ای نمی‌کند و کاربر آن را کندی می‌فهمد، نه احتیاط.
 */
enum class ConfirmTone { DESTRUCTIVE, PAYMENT, HEAVY_CHANGE }

/**
 * قالبِ متن - سه سطر و نه بیشتر:
 *
 * [title] کنش + مفعول + علامتِ سوال: «حذفِ این تراکنش؟»
 * [consequence] یک جمله با **عددِ واقعی و نامِ حسابِ واقعی**. کلمه‌ی «برنمی‌گردد» فقط
 *   وقتی که واقعاً برنمی‌گردد - در جای دیگر بی‌اثرش می‌کند.
 * [actionLabel] فعل، نه «بله/خیر».
 *
 * ⚠️ دکمه‌ها **هم‌عرض**اند و کنش سمتِ راست است (در RTL شست همان‌جاست) - و همین دلیل
 * است که دکمه‌ی مخرب هیچ‌وقت نباید برجسته‌ترِ پیش‌فرض باشد. «بی‌خیال» سفیدِ حاشیه‌دار
 * است ولی **هم‌اندازه**: کوچک‌کردنش یعنی سختِ‌زدن‌کردنِ راهِ فرار.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    title: String,
    consequence: String,
    actionLabel: String,
    tone: ConfirmTone,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "بی‌خیال",
    /** غیرفعال تا وقتی انتخابی که دیالوگ می‌خواهد انجام نشده - مثلِ `44b`. */
    confirmEnabled: Boolean = true,
) {
    val icon: ImageVector
    val pill: Color
    val ink: Color
    // ⚠️ `GradientButton` پارامترِ `background` ندارد و رنگش را از `variant` می‌گیرد -
    // قاعده‌ی پروژه: رنگِ دکمه از توکن می‌آید نه از فراخوان. پس لحن به گونه نگاشت می‌شود،
    // نه به یک رنگِ دستی.
    val actionVariant: AppButtonVariant
    when (tone) {
        ConfirmTone.DESTRUCTIVE -> {
            icon = Icons.Filled.DeleteOutline; pill = AppDangerPill
            ink = AppDangerInk; actionVariant = AppButtonVariant.DESTRUCTIVE
        }
        ConfirmTone.PAYMENT -> {
            icon = Icons.Filled.Check; pill = AppPrimaryPill
            ink = AppPrimaryInk; actionVariant = AppButtonVariant.PRIMARY
        }
        ConfirmTone.HEAVY_CHANGE -> {
            icon = Icons.Filled.ErrorOutline; pill = AppLine
            ink = AppMuted; actionVariant = AppButtonVariant.NEUTRAL
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(AppSurface)
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(19.dp))
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                    Text(
                        consequence,
                        color = AppMuted,
                        fontSize = 11.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    modifier = Modifier.weight(1f),
                    variant = actionVariant,
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppSurface)
                        .border(2.dp, AppLine, RoundedCornerShape(999.dp))
                        .pressScaleClickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(dismissLabel, color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * نوارِ واگرد - فریمِ `46b`، جوابِ سوالِ «کنشِ بازگشت‌پذیر چه بگیرد».
 *
 * «تسویه‌شد» دیالوگ نمی‌گیرد. دلیلش صرفه‌جوییِ یک تپ نیست، **نگه‌داشتنِ ارزشِ دیالوگ**
 * است: کاربری که روزی ده بار «بله» می‌زند یاد می‌گیرد بی‌خواندن بزند، و همان عادت روی
 * دیالوگِ حذف هم اجرا می‌شود. آن‌وقت پانزده دیالوگ داریم و هیچ‌کدام کار نمی‌کند.
 *
 * [message] نتیجه را با عدد می‌گوید («تسویه شد · ۵٫۹ میلیون کم شد») - چون کاربر کنش را
 * دیده ولی مقدارش را ندیده.
 *
 * ۵ ثانیه می‌ماند. کوتاه‌تر برای خواندن و رساندنِ دست کافی نیست؛ بلندتر روی محتوا
 * می‌نشیند.
 */
@Composable
fun UndoBar(message: String, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppText)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            color = AppBg,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            "واگرد",
            color = AppPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .pressScaleClickable(onClick = onUndo)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

const val UNDO_BAR_MILLIS = 5_000L

/**
 * دو پوششِ سازگاری روی [ConfirmDialog]ی تازه.
 *
 * سیزده صفحه‌ی موجود این دو نام را صدا می‌زنند. بازنویسیِ هر سیزده‌تا در همین نوبت یعنی
 * سیزده جای تازه برای اشتباه، بی این‌که کاربر تفاوتی ببیند — قالب و لحن از همین‌جا می‌آید،
 * پس ظاهرشان همان قالبِ واحدِ `46a` است.
 *
 * ⚠️ اینها **جایگزینِ دائمی نیستند**: صفحه‌های تازه مستقیم [ConfirmDialog] را با
 * `consequence`ی که پیامد را صریح می‌گوید صدا بزنند، نه یک `text`ی که ممکن است فقط
 * پرسش باشد.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "حذف",
) = ConfirmDialog(
    title = title,
    consequence = text,
    actionLabel = confirmLabel,
    tone = ConfirmTone.DESTRUCTIVE,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
)

@Composable
fun ConfirmPayDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "بله، پرداخت شد",
) = ConfirmDialog(
    title = title,
    consequence = text,
    actionLabel = confirmLabel,
    tone = ConfirmTone.PAYMENT,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
)
