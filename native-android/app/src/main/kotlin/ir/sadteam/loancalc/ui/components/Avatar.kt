package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * **آدمکِ پروفایل** - بخشِ ۳۲ فایلِ طراحی (کارت‌های `32a`..`32c`).
 *
 * به‌جای حرفِ اولِ اسم یا دایره‌ی خالی، یه آدمکِ ساده‌ی **بدونِ چهره** («اینجا هویت مهم است نه
 * چهره» - متنِ خودِ طرح). دو شکل (پسر/دختر)، پنج رنگ، به‌علاوه‌ی یه حالتِ **خاکستریِ خنثی** که
 * پیش‌فرضِ کاربرِ انتخاب‌نکرده‌ست («شکل تصادفی نیست»).
 *
 * ⚠️ **همه‌ی نسبت‌ها ضریبی از قطرن** (خواسته‌ی صریحِ `32c`: «یک `Composable`ِ واحد با پارامترِ
 * `size`») تا تو هر اندازه‌ای - ۳۲ نوارِ بالای خانه، ۴۴ طرفِ حساب، ۵۶ سرِ پروفایل، ۹۶ شروعِ
 * برنامه - دقیقاً یک‌شکل دیده بشه. عددها از خودِ کارتِ `32a` که رو پایه‌ی ۶۴ پیکسل کشیده شده
 * برداشته شدن.
 */
enum class AvatarShape { BOY, GIRL }

/**
 * پنج رنگِ طرح + [NEUTRAL]. `ink` رنگِ سر و شانه‌ست و `tint` پس‌زمینه‌ی روشنِ **همون** رنگ
 * (قاعده‌ی `32a`: «سر و شانه هم‌رنگ، پس‌زمینه‌ی روشنِ همان رنگ»).
 *
 * این رنگ‌ها عمداً **ثابت**ن و از پالتِ تم‌آگاه نمیان: آدمک یه هویتِ شخصیه که کاربر انتخاب کرده،
 * نباید با عوض‌کردنِ تم رنگش عوض بشه.
 */
enum class AvatarColor(val ink: Color, val tint: Color) {
    GREEN(Color(0xFF0B8C57), Color(0xFFE9F7EF)),
    PURPLE(Color(0xFF7C4DD1), Color(0xFFF3EAFE)),
    BLUE(Color(0xFF1E6FD9), Color(0xFFEAF1FE)),
    ORANGE(Color(0xFFB45F00), Color(0xFFFFF1DC)),
    RED(Color(0xFFD93838), Color(0xFFFFECEC)),

    /** پیش‌فرضِ «هنوز انتخاب نشده» - خاکستریِ خنثی. */
    NEUTRAL(Color(0xFF5B6A63), Color(0xFFF1F5F3)),
}

/** حالتِ ذخیره‌شده‌ی آدمک. `photoPath` اگه پر باشه **جای آدمک رو می‌گیره** (قاعده‌ی `32c`). */
data class Avatar(
    val shape: AvatarShape = AvatarShape.BOY,
    val color: AvatarColor = AvatarColor.NEUTRAL,
    val photoPath: String? = null,
)

/**
 * خودِ آدمک. اگه [Avatar.photoPath] پر باشه، تصمیمِ نمایشِ عکس با فراخوانه‌ست ([photoContent]) -
 * این فایل عمداً به کتابخانه‌ی بارگذاریِ عکس وابسته نیست.
 */
@Composable
fun AvatarView(
    avatar: Avatar,
    size: Dp,
    modifier: Modifier = Modifier,
    photoContent: (@Composable (String) -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatar.color.tint),
        contentAlignment = Alignment.Center,
    ) {
        val photo = avatar.photoPath
        if (photo != null && photoContent != null) {
            photoContent(photo)
            return@Box
        }
        // ⚠️ توکن‌های رنگِ اپ `@Composable`ان و داخلِ `Canvas` (که `DrawScope`ه) صدا زده
        // نمی‌شن؛ اینجا مشکلی نیست چون رنگِ آدمک ثابته و از `enum` میاد، نه از پالت.
        val ink = avatar.color.ink
        val tint = avatar.color.tint
        val girl = avatar.shape == AvatarShape.GIRL
        Canvas(modifier = Modifier.fillMaxSize()) {
            val d = this.size.minDimension
            fun px(ratio: Float) = d * ratio
            val cx = this.size.width / 2f

            // شانه‌ها - هر دو شکل یکی‌ان: عرض ۰٫۶۲۵ و ارتفاع ۰٫۳۴۴ قطر، گوشه‌ی بالا کاملاً گرد.
            val shoulderW = px(0.625f)
            val shoulderH = px(0.344f)
            drawRoundRect(
                color = ink,
                topLeft = Offset(cx - shoulderW / 2f, this.size.height - shoulderH),
                size = Size(shoulderW, shoulderH * 2f),
                cornerRadius = CornerRadius(shoulderW / 2f, shoulderW / 2f),
            )

            if (!girl) {
                // پسر: فقط یه سرِ دایره‌ای.
                val headD = px(0.328f)
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(cx - headD / 2f, px(0.203f)),
                    size = Size(headD, headD),
                    cornerRadius = CornerRadius(headD / 2f, headD / 2f),
                )
            } else {
                // دختر: مو (گوشه‌ی بالا گردتر از پایین) → صورت به رنگِ پس‌زمینه → چترِ مو.
                val hairW = px(0.469f)
                val hairH = px(0.453f)
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(cx - hairW / 2f, px(0.172f)),
                    size = Size(hairW, hairH),
                    cornerRadius = CornerRadius(px(0.25f), px(0.25f)),
                )
                val faceD = px(0.297f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(cx - faceD / 2f, px(0.219f)),
                    size = Size(faceD, faceD),
                    cornerRadius = CornerRadius(faceD / 2f, faceD / 2f),
                )
                val fringeH = px(0.156f)
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(cx - faceD / 2f, px(0.219f)),
                    size = Size(faceD, fringeH * 2f),
                    cornerRadius = CornerRadius(fringeH, fringeH),
                )
            }
        }
    }
}

/**
 * ردیفِ انتخابِ آدمک - محتوای شیتِ `32b`.
 *
 * قاعده‌ی صریحِ طرح: **حالتِ انتخاب‌شده حلقه‌ی دوجداره داره، نه تیک.**
 */
@Composable
fun AvatarPicker(
    avatar: Avatar,
    onChange: (Avatar) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // شکل
        Row {
            AvatarShape.entries.forEach { shape ->
                val selected = avatar.shape == shape
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .then(
                            if (selected) {
                                Modifier.border(3.dp, avatar.color.ink, CircleShape)
                            } else {
                                Modifier
                            },
                        )
                        .pressScaleClickable { onChange(avatar.copy(shape = shape)) },
                    contentAlignment = Alignment.Center,
                ) {
                    AvatarView(avatar.copy(shape = shape), size = if (selected) 60.dp else 66.dp)
                }
            }
        }
        // رنگ
        Row(modifier = Modifier.padding(top = 14.dp)) {
            AvatarColor.entries.forEach { color ->
                val selected = avatar.color == color
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(
                            if (selected) Modifier.border(3.dp, color.ink, CircleShape) else Modifier,
                        )
                        .pressScaleClickable { onChange(avatar.copy(color = color)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (selected) 26.dp else 32.dp)
                            .clip(CircleShape)
                            .background(color.ink),
                    )
                }
            }
        }
    }
}
