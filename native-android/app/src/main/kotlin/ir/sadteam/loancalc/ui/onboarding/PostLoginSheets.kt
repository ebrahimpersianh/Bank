package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * دو صفحه‌ای که **فقط یه‌بار، بلافاصله بعد از اولین ورودِ موفق** نشون داده می‌شن (تسکِ #30، طبقِ
 * اسکرین‌شات‌های اپِ مرجع):
 *
 * 1. **هدیه‌ی اشتراک** - عددِ روزها از سرور میاد ([trialDaysLeft])، هاردکد نیست. اگه این شماره از
 *    قبل تو سرور بوده ([legacyGift])، جمله‌ی اضافه‌ی «چون از قبل وارد برنامه شده بودی، ۱۵ روز
 *    بیشتر گرفتی» هم نشون داده می‌شه.
 *    اینجا **یه‌بار و به‌ملایمت** اسمِ کاربر هم پرسیده می‌شه (تسکِ #37): کاملاً اختیاری، رد کردنش
 *    یعنی دیگه هیچ‌وقت پرسیده نمی‌شه، و **هیچ‌وقت اولِ برنامه** پرسیده نمی‌شه.
 * 2. **خیالت راحت** - پیامِ اطمینان که اطلاعات روی سرور امن نگه داشته می‌شه و با عوض‌کردنِ گوشی
 *    از دست نمی‌ره.
 */
@Composable
fun PostLoginSheets(
    trialDaysLeft: Int?,
    legacyGift: Boolean,
    currentName: String?,
    onSaveName: (String?) -> Unit,
    onFinished: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
    ) {
        if (page == 0) {
            GiftPage(
                trialDaysLeft = trialDaysLeft,
                legacyGift = legacyGift,
                currentName = currentName,
                onSaveName = onSaveName,
                onNext = { page = 1 },
            )
        } else {
            BackupReassurancePage(onDone = onFinished)
        }
    }
}

@Composable
private fun GiftPage(
    trialDaysLeft: Int?,
    legacyGift: Boolean,
    currentName: String?,
    onSaveName: (String?) -> Unit,
    onNext: () -> Unit,
) {
    // عدد فقط وقتی نشون داده می‌شه که سرور واقعاً فرستاده باشه - وگرنه متنِ بدونِ عدد، نه یه عددِ
    // حدسیِ هاردکد (خواسته‌ی صریحِ کاربر: «عددها از سرور»).
    val daysText = trialDaysLeft?.let { "${toFa(it)} روز" }
    var name by remember { mutableStateOf(currentName.orEmpty()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(24.dp))
        HeroIcon(Icons.Default.CardGiftcard, AppAccent)
        Text(
            if (daysText != null) "$daysText اشتراک هدیه گرفتی 🎉" else "اشتراک هدیه گرفتی 🎉",
            color = AppText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
        )
        Text(
            "تو این مدت همه‌ی امکاناتِ برنامه بدونِ محدودیت برات بازه.",
            color = AppMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        )
        if (legacyGift) {
            AppCard(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    "چون از قبل وارد برنامه شده بودی، ۱۵ روز هدیه‌ی اضافه‌تر هم برات لحاظ شد. ممنون که همراهمون بودی 💚",
                    color = AppText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "اسمت چیه؟ (اختیاری)",
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "فقط برای سربرگِ خروجی‌های PDF و اکسل استفاده می‌شه. می‌تونی رد کنی.",
            color = AppMuted,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))
        GradientButton(
            onClick = {
                onSaveName(name.trim().ifBlank { null })
                onNext()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ادامه")
        }
        TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("رد کردن", color = AppMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BackupReassurancePage(onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(24.dp))
        HeroIcon(Icons.Default.CloudDone, AppPrimary)
        Text(
            "نگرانِ از دست رفتنِ اطلاعاتت نباش",
            color = AppText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
        )
        Text(
            "حالا که با شماره‌ت وارد شدی، اطلاعاتت روی سرورِ امنِ ما هم نگه داشته می‌شه.",
            color = AppMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        )
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReassureRow("گوشیت رو عوض کنی، اطلاعاتت برمی‌گرده")
            ReassureRow("برنامه رو پاک و دوباره نصب کنی، چیزی گم نمی‌شه")
            ReassureRow("هر وقت خواستی خودت هم فایلِ پشتیبان بگیر")
        }
        Spacer(Modifier.weight(1f))
        GradientButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("فهمیدم، بریم")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReassureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AppPrimary),
        )
        Text(text, color = AppText, fontSize = 13.5.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun HeroIcon(icon: ImageVector, tint: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
        }
    }
}
