package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppDanger

/**
 * پورت ژستِ «کشیدن برای حذف» (swipe-to-delete) - قبلاً تو هیچ‌جای اپ (چک‌ها، تاریخچه، تراکنش‌های
 * حساب) این حرکت نبود، فقط دکمه‌ی سطلِ زباله. این کامپوننت هر ردیفِ لیستی رو با یه پس‌زمینه‌ی
 * قرمزِ حاویِ آیکونِ سطل می‌پیچه که با کشیدن (از هر دو سمت - چون اپ RTLه و کاربر ممکنه عادتِ
 * چپ‌به‌راستِ اپ‌های دیگه رو داشته باشه) نمایان می‌شه.
 *
 * [onDelete] دقیقاً موقعِ *تصمیمِ* حذف (وسطِ ژست، نه بعدِ اتمامِ انیمیشن) صدا زده می‌شه - چون
 * `SwipeToDismissBox` خودش جمع‌شدنِ ردیف رو انیمیشن می‌کنه و بلافاصله بعدش خودِ آیتم از لیستِ
 * واقعی (Room) حذف می‌شه، پس نیازی به ریست‌کردنِ دستیِ state نیست؛ کامپوزیبل خودش از کامپوزیشن
 * بیرون می‌ره.
 *
 * [confirmDismiss]=false (مثلاً برای چک‌ها، رجوع کن به مورد ۹ تو CLAUDE.md) یعنی [onDelete] فقط
 * *قصدِ* حذف رو اعلام می‌کنه (مثلاً بازکردنِ یه دیالوگِ تایید) نه خودِ حذفِ واقعی - ردیف خودکار به
 * جای اولش برمی‌گرده (چون `confirmValueChange` false برمی‌گردونه) و حذفِ واقعی/خروجِ ردیف از
 * لیست فقط بعد از تاییدِ کاربر، از رو حذف‌شدنِ آیتم از دیتای واقعی اتفاق می‌افته.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    // شکلِ پس‌زمینه‌ی قرمز باید هم‌شکلِ خودِ کارتی باشه که این ردیف رو می‌پیچه، وگرنه گوشه‌های
    // تیزِ قرمز از زیرِ کارتِ گردگوشه بیرون می‌زنن.
    shape: Shape = RoundedCornerShape(14.dp),
    confirmDismiss: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            confirmDismiss
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppDanger, shape)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                },
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color.White)
            }
        },
    ) {
        content()
    }
}
