package ir.sadteam.loancalc.ui.components

import ir.sadteam.loancalc.ui.theme.AppMuted
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.sadteam.loancalc.ui.theme.AppDanger
import java.io.File

/**
 * پورت «پیوست عکس رسید» اپ رقیب (VAMMAN) - کارت قابل‌استفاده‌ی مجدد رو جزئیات وام/چک. از
 * `ActivityResultContracts.PickVisualMedia` (Photo Picker مدرن اندروید) استفاده می‌کنه، نه یه
 * Intent گالری خام - نیازی به مجوز READ_EXTERNAL_STORAGE نداره. کپی‌کردن عکس به فضای داخلی اپ
 * (نه نگه‌داشتن خودِ Uri موقتی) تو [AttachmentStorage] انجام می‌شه، اینجا فقط UI انتخاب/نمایش/حذفه.
 */
@Composable
fun PhotoAttachmentCard(
    photoPath: String?,
    onPick: (Uri) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    /** false یعنی بدونِ کارتِ دورش - وقتی خودش داخلِ کارتِ دیگری (تبِ یادداشت/رسید) می‌نشیند. */
    withCard: Boolean = true,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPick(uri) }

    // حذفِ عکس قابلِ برگشت نیست - اول می‌پرسیم (قاعده‌ی کاربر: «هر چیزی که پاک می‌شه هشدار بده»).
    var confirmRemove by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    if (confirmRemove) {
        ConfirmDeleteDialog(
            title = "حذفِ عکس",
            text = "این عکس برای همیشه پاک می‌شود.",
            onConfirm = { confirmRemove = false; onRemove() },
            onDismiss = { confirmRemove = false },
        )
    }

    fun launchPicker() {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val photoFile = remember(photoPath) { photoPath?.let { File(it) }?.takeIf { it.exists() } }

    val content: @Composable () -> Unit = {
        if (photoFile != null) {
            AsyncImage(
                model = photoFile,
                contentDescription = "عکس رسید",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { launchPicker() }, modifier = Modifier.weight(1f)) {
                    Text("تعویض عکس")
                }
                OutlinedButton(
                    onClick = { confirmRemove = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("حذف عکس")
                }
            }
        } else {
            OutlinedButton(onClick = { launchPicker() }, modifier = Modifier.fillMaxWidth()) {
                Text("+ افزودن عکس رسید")
            }
        }
    }
    if (withCard) {
        AppCard(label = "عکس رسید", modifier = modifier) { content() }
    } else {
        Column(modifier = modifier) {
            Text("عکس رسیدهای مربوط به این مورد را اضافه کنید.", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}
