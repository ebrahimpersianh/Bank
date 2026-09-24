package ir.sadteam.loancalc.ui.myloans

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText
import java.io.File

// ذخیره ریال است و نمایش تومان (بندِ ۲ی README): تنها نقطه‌ی تبدیلِ این فایل.
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

/** سقفِ انتخابِ یک‌بارِ گالری. رسیدِ یک قسط عملاً یکی‌دوتاست؛ ده تا سخاوتمندانه است. */
private const val MAX_RECEIPT_PHOTOS = 10

/**
 * **جزئیاتِ هر قسط** - کارتِ `36d` فایلِ طراحی (بخشِ ۳۶، «کنترل‌های گم‌شده»).
 *
 * «صفحه‌ی تازه که با تپ روی هر ردیفِ اقساط (29p) باز می‌شود. سه کارتِ یادداشت، عکسِ رسید و
 * شماره‌ی پیگیری زیرِ کارتِ مبلغ. عکس از دوربین یا گالری، **چندتایی**. شماره‌ی پیگیری دکمه‌ی
 * کپی دارد. اگر قسط پرداخت نشده باشد، هر سه کارت خالی و قابلِ پرکردن‌اند.»
 *
 * ⚠️ جایگزینِ دیالوگِ کوچیکِ «رسید قسط»ِ قبلیه که فقط **یک** عکس می‌گرفت.
 */
@Composable
internal fun InstallmentDetailScreen(
    loan: LoanEntity,
    m: Int,
    amount: Double,
    paid: Boolean,
    paidDateLabel: String?,
    photoPaths: List<String>,
    note: String?,
    trackingNumber: String?,
    onBack: () -> Unit,
    onPickPhoto: (Uri) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onSaveDetails: (note: String, trackingNumber: String) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var noteDraft by remember(note) { mutableStateOf(note ?: "") }
    var trackingDraft by remember(trackingNumber) { mutableStateOf(trackingNumber ?: "") }
    val dirty = noteDraft.trim() != (note ?: "") || trackingDraft.trim() != (trackingNumber ?: "")

    // طرحِ 36d می‌گه عکس **چندتایی**ه. `PickVisualMedia` تک‌عکسیه، پس کاربر برای سه
    // رسید سه بار باید گالری رو باز کنه.
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_RECEIPT_PHOTOS),
    ) { uris -> uris.forEach(onPickPhoto) }
    // حذفِ عکس فایل رو از دیسک می‌بره و برنمی‌گرده، پس طبقِ ۴۶c دیالوگ می‌گیره.
    var pendingPhotoDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(
                    "قسط ${toFa(m)} · ${loan.name}",
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        // کارتِ مبلغ - بالای هر سه کارتِ دیگه، طبقِ چیدمانِ طرح.
        item {
            AppCard {
                Text("مبلغِ قسط", color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${amountToman(amount)} تومان",
                    color = AppText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    when {
                        paid && paidDateLabel != null -> "پرداخت‌شده $paidDateLabel"
                        paid -> "پرداخت‌شده"
                        else -> "هنوز پرداخت نشده"
                    },
                    color = if (paid) AppPrimaryInk else AppMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        item {
            AppCard(label = "یادداشت") {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("مثلاً: از حسابِ ملی پرداخت شد", color = AppMuted, fontSize = 12.sp)
                    },
                    minLines = 2,
                    maxLines = 4,
                )
            }
        }
        item {
            AppCard(label = "عکسِ رسید") {
                if (photoPaths.isEmpty()) {
                    Text(
                        "هنوز رسیدی برای این قسط نگذاشتی.",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                photoPaths.forEach { path ->
                    val file = remember(path) { File(path).takeIf { it.exists() } }
                    if (file != null) {
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            AsyncImage(
                                model = file,
                                contentDescription = "عکسِ رسید",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(AppRadius.row)),
                            )
                            IconButton(
                                onClick = { pendingPhotoDelete = path },
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "حذفِ عکس")
                            }
                        }
                    }
                }
                GradientButton(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    variant = AppButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("افزودنِ عکس", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        item {
            AppCard(label = "شماره‌ی پیگیری") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // شماره‌ی پیگیری ذاتاً چپ‌به‌راسته - قاعده‌ی `Ltr.kt`.
                    Ltr {
                        OutlinedTextField(
                            value = trackingDraft,
                            onValueChange = { trackingDraft = cleanNum(it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            // شماره‌ی پیگیری استثنای دومِ لایه‌ی ارقامه: رقمْ لاتین می‌مونه،
                            // چون کاربر کپی‌ش می‌کنه و جایی می‌چسبونه که ماشین می‌خونه.
                            placeholder = { Text("8492037715", color = AppMuted, fontSize = 12.sp) },
                        )
                    }
                    if (trackingDraft.isNotBlank()) {
                        IconButton(onClick = { clipboard.setText(AnnotatedString(trackingDraft)) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "کپی")
                        }
                    }
                }
            }
        }
        if (dirty) {
            item {
                GradientButton(
                    onClick = { onSaveDetails(noteDraft, trackingDraft) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("ذخیره") }
            }
        }
    }

    pendingPhotoDelete?.let { path ->
        ConfirmDialog(
            tone = ConfirmTone.DESTRUCTIVE,
            title = "حذفِ عکسِ رسید",
            consequence = "این عکس از گوشی پاک می‌شه و برنمی‌گرده.",
            actionLabel = "حذفِ عکس",
            onConfirm = { pendingPhotoDelete = null; onRemovePhoto(path) },
            onDismiss = { pendingPhotoDelete = null },
        )
    }
}
