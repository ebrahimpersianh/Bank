package ir.sadteam.loancalc.ui.inbox

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface2
import java.io.File
import javax.inject.Inject

/** عکسِ همراهِ جوابِ پشتیبانی - یک بار دانلود و در پوشه‌ی موقتِ برنامه نگه داشته می‌شود. */
@HiltViewModel
class MessageImageViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val repo: AuthRepository,
) : ViewModel() {
    suspend fun load(id: String): ByteArray? = repo.announcementImage(context.cacheDir, id)
}

/**
 * عکسِ داخلِ «پیام‌های جیبک» (۴ آبان، خواسته‌ی کاربر: جوابِ پشتیبانی با عکسِ توضیحی).
 * لمس → در گالریِ گوشی بزرگ باز می‌شود. تا رسیدنِ عکس یک قابِ خاکستری با آیکون.
 */
@Composable
internal fun MessageImage(imageId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: MessageImageViewModel = hiltViewModel()
    var bytes by remember(imageId) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(imageId) { bytes = viewModel.load(imageId) }
    val bmp = remember(bytes) { bytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .heightIn(min = 90.dp, max = 320.dp)
            .clip(shape)
            .background(AppSurface2)
            .pressScaleClickable(scale = 0.98f) {
                if (bytes == null) return@pressScaleClickable
                val f = File(context.cacheDir, "support_view/msg_$imageId.jpg")
                if (!f.isFile) return@pressScaleClickable
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).setDataAndType(uri, "image/jpeg")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bmp != null) {
            Image(bmp, "عکس", contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth())
        } else {
            Icon(Icons.Filled.Image, contentDescription = null, tint = AppMuted, modifier = Modifier.size(28.dp))
        }
    }
}
