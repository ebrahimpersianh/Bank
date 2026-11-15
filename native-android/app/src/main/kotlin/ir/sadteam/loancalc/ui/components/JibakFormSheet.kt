package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.text.Text
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * برگه‌ی پایینِ یک‌دستِ همه‌ی فرم‌های کوچک (۱۳ آبان، کاربر: «این پنجره‌ها چقدر نافرم بودن، خیلی
 * جاهای برنامه هم این‌طوری‌اند»). همان قالبِ «قبضِ جدید» و «بودجه‌ی تازه»: عنوانِ درشت، زیرعنوانِ
 * کم‌رنگِ اختیاری، کادرها، و **یک دکمه‌ی تمام‌عرض** پایین - جای پنجره‌ی شناورِ تنگ با «ذخیره»ِ ریزِ گوشه.
 *
 * محتوا خودش اسکرول می‌خورد و با بازشدنِ صفحه‌کلید بالا می‌آید. پس داخلش `LazyColumn` نگذار.
 *
 * @param headerAction مثلاً آیکونِ حذف کنارِ عنوان.
 * @param secondaryLabel کنشِ دومِ متنی زیرِ دکمه‌ی اصلی (مثلاً «حذف» یا «پیش‌فرض»)؛ «انصراف» لازم نیست -
 *   کشیدنِ برگه به پایین/برگشت همان کار را می‌کند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JibakFormSheet(
    onDismissRequest: () -> Unit,
    title: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    subtitle: String? = null,
    primaryVariant: AppButtonVariant = AppButtonVariant.PRIMARY,
    headerAction: (@Composable () -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    secondaryColor: androidx.compose.ui.graphics.Color = AppMuted,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheet, containerColor = AppSurface) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                headerAction?.invoke()
            }
            if (subtitle != null) {
                Text(subtitle, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
            }
            CompositionLocalProvider(
                LocalContentColor provides AppMuted,
                LocalTextStyle provides TextStyle(color = AppMuted, fontSize = 12.sp, lineHeight = 21.sp),
            ) { content() }
            GradientButton(
                onClick = onPrimary,
                enabled = primaryEnabled,
                variant = primaryVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) { Text(primaryLabel, fontWeight = FontWeight.Black) }
            if (secondaryLabel != null && onSecondary != null) {
                TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
                    Text(secondaryLabel, color = secondaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * برگه‌ی پایینِ «یکی را انتخاب کن» (حساب، دسته، خرج…) - هم‌خانواده‌ی [JibakFormSheet] ولی بی دکمه:
 * لمسِ یک ردیف خودش انتخاب است و کشیدن/برگشت یعنی انصراف. اسکرولِ بیرونی ندارد تا محتوا بتواند
 * خودش `LazyColumn` باشد.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JibakListSheet(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheet, containerColor = AppSurface) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(title, color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
            if (subtitle != null) Text(subtitle, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
            content()
        }
    }
}
