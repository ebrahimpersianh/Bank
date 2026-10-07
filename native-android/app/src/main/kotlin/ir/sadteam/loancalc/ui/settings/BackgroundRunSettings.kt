package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **اجرا در پس‌زمینه** - جوابِ خواسته‌ی «برنامه همیشه باز باشه و بعدِ ری‌استارت خودش بیاد».
 *
 * ⚠️ عمداً چیزی را که شدنی نیست وعده نمی‌دهد: اندروید اجازه‌ی «همیشه باز ماندن» به هیچ اپی
 * نمی‌دهد. این صفحه دقیقاً همان سه شرطی را نشان می‌دهد که پس‌زمینه را زنده نگه می‌دارند و هر
 * کدام را با یک دکمه به صفحه‌ی مربوطه‌ی خودِ گوشی می‌برد - رجوع کن به [BackgroundRunHelp].
 */
@Composable
internal fun BackgroundRunSettings() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // وضعیت باید بعدِ برگشتن از تنظیماتِ گوشی تازه بشه، وگرنه کاربر اجازه را می‌دهد و اینجا
    // هنوز «داده نشده» می‌بیند - همان قاعده‌ی «سوئیچ دروغ نمی‌گوید».
    var batteryOk by remember { mutableStateOf(BackgroundRunHelp.batteryUnrestricted(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryOk = BackgroundRunHelp.batteryUnrestricted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ═══ بازطراحی (دورِ ۹) ═══
    //
    // سه کارتِ بلندِ متنی بودند با دکمه‌ی جدا زیرشان؛ کاربر گفت «بهتر طراحی کن و تمامِ
    // باکس‌ها کلیک‌شدنی باشند». حالا هر مرحله یک ردیفِ فشرده است: شماره در دایره،
    // عنوان، یک خطِ توضیح، و قرصِ وضعیت - و **خودِ کارت** مقصدش را باز می‌کند.
    //
    // ⚠️ مرحله‌ی سوم عمداً کلیک‌پذیر **نیست**: قفلِ فهرستِ برنامه‌های اخیر از داخلِ هیچ
    // برنامه‌ای شدنی نیست و هیچ صفحه‌ای در گوشی برایش وجود ندارد. کارتی که تپ را قبول
    // کند و هیچ اتفاقی نیفتد بدتر از کارتِ بی‌تپ است، پس نشانه‌ی تپ هم نمی‌گیرد.
    // خلاصه‌ی وضعیت: قبل از متنِ راهنما، کاربر در یک نگاه می‌فهمد چه چیزی باقی مانده.
    // «آماده» فقط یعنی معافیتِ باتری داده شده - تضمینِ رسیدنِ هر پیامک نیست، و متن هم همین را می‌گوید.
    SettingsHero(
        Icons.Filled.BatterySaver,
        if (batteryOk) "پس‌زمینه برای کار آماده است" else "برای اجرای مطمئن، دو دقیقه وقت بگذار",
        if (batteryOk) "معافیتِ باتری فعال است؛ مراحلِ دستی را هم یک‌بار بررسی کن."
        else "چند مرحله‌ی کوتاه تا یادآورها و ثبتِ خودکار پایدار بمانند.",
        badge = if (batteryOk) "آماده" else "نیازمندِ اقدام",
    )
    SettingsGroupLabel("مراحل")
    // طرحِ Claude Design (۸ مهر): هر سه مرحله در یک کارت با خطِ جداکننده.
    var batteryTapCount by remember { mutableStateOf(0) }
    AppCard(modifier = Modifier.padding(top = 10.dp)) {
    BackgroundStepCard(
        step = 1,
        title = "معافیت از بهینه‌سازیِ باتری",
        body = "بدونِ این، گوشی بعد از چند دقیقه کارهای پس‌زمینه را متوقف می‌کند و یادآورِ " +
            "سررسید دیر می‌رسد یا اصلاً نمی‌رسد.",
        done = batteryOk,
        onClick = { BackgroundRunHelp.openBatterySettings(context, preferList = batteryTapCount++ > 0) },
    )

    if (BackgroundRunHelp.needsAutostartSetting()) {
        SettingsDivider()
        BackgroundStepCard(
            step = 2,
            title = "اجرای خودکار بعد از روشن‌شدنِ گوشی",
            body = "سازنده‌ی این گوشی «اجرای خودکار» را پیش‌فرض خاموش می‌گذارد؛ تا روشن نشود، " +
                "بعد از خاموش‌وروشن‌کردنِ گوشی پیامکِ بانکی خودکار ثبت نمی‌شود.\n" +
                BackgroundRunHelp.autostartHint(),
            hintBox = true,
            // وضعیتش از بیرون خواندنی نیست (هر سازنده جای خودش را دارد)، پس ادعای
            // «انجام شده» نمی‌کنیم - قرص خاکستریِ «باز کن» می‌مانَد.
            done = false,
            onClick = {
                // اگر صفحه‌ی مخصوصِ سازنده پیدا نشد، صفحه‌ی اطلاعاتِ خودِ برنامه باز می‌شود -
                // بن‌بست بهتر از کرشِ ActivityNotFound.
                val intent = BackgroundRunHelp.autostartIntent(context)
                    ?: BackgroundRunHelp.appDetailsIntent(context)
                runCatching { context.startActivity(intent) }
            },
        )
    }

    SettingsDivider()
    // بندِ ۷ِ دورِ ۹: این مرحله از داخلِ هیچ برنامه‌ای شدنی نیست، پس مقصد ندارد - ولی
    // کارتی که تپ نمی‌گیرد باید **بگوید** دستورالعمل است، وگرنه کاربر تپ می‌زند و فکر
    // می‌کند خراب است. پس بجِ «دستی»، و متن با **فعل** شروع می‌شود.
    BackgroundStepCard(
        step = 3,
        title = "در فهرستِ برنامه‌های اخیر، جیبک را قفل کن",
        body = "کلیدِ مربع (برنامه‌های اخیر) را بزن، روی کارتِ جیبک نگه دار و گزینه‌ی قفل را بزن. " +
            "بعد از آن، بستنِ همه‌ی برنامه‌ها دیگر جیبک را نمی‌بندد.",
        done = false,
        onClick = null,
        manualBadge = true,
    )
    }
}
/**
 * یک مرحله‌ی «اجرا در پس‌زمینه» - خودِ کارت مقصد را باز می‌کند (دورِ ۹).
 *
 * [onClick] اگر `null` باشد کارت هیچ نشانه‌ی تپی نمی‌گیرد؛ مرحله‌ای که از داخلِ برنامه
 * شدنی نیست نباید وانمود کند دکمه است.
 */
@Composable
private fun BackgroundStepCard(
    step: Int,
    title: String,
    body: String,
    done: Boolean,
    onClick: (() -> Unit)?,
    /** مرحله‌ای که خودِ کاربر باید بیرونِ برنامه انجامش بدهد - بجِ «دستی» می‌گیرد. */
    manualBadge: Boolean = false,
    /** خطِ آخرِ [body] (مسیرِ تنظیمات) در جعبه‌ی خاکستریِ جدا. */
    hintBox: Boolean = false,
) {
    val mainBody = if (hintBox) body.substringBeforeLast('\n') else body
    val hint = if (hintBox && body.contains('\n')) body.substringAfterLast('\n') else null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScaleClickable(scale = 0.99f, onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Top)
                .size(32.dp)
                .clip(CircleShape)
                .background(if (done) AppPrimary else AppIconFrame),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(toFa(step), color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, color = if (done) AppPrimaryInk else AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
            Text(mainBody, color = AppMuted, fontSize = 11.5.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
            if (hint != null) {
                Text(
                    hint,
                    color = AppText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppIconFrame)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                )
            }
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.padding(start = 8.dp).size(13.dp),
            )
        } else if (manualBadge) {
            Text(
                "دستی",
                color = AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppIconFrame)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
