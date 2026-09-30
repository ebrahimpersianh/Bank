package ir.sadteam.loancalc.ui.cheque

import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import ir.sadteam.loancalc.ui.theme.AppPrimary
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

// آدرس/کد/شماره‌ها عیناً از رو عکسی که کاربر از سایت بانک مرکزی/راهنمای استعلام صیادی فرستاد -
// طبق قانون پروژه هیچ URL/شماره‌ی حدسی این‌جا گذاشته نشده.
private const val SAYAD_WEB_URL = "https://cbi.ir/EstelamSayad/24090.aspx"
private const val SAYAD_SMS_NUMBER = "701701"

private data class ChequeColorMeaning(val name: String, val color: Color, val meaning: String)

private val chequeColorLegend = listOf(
    ChequeColorMeaning("قرمز", Color(0xFFE53935), "خطرناک"),
    ChequeColorMeaning("قهوه‌ای", Color(0xFF6D4C41), "پرریسک"),
    ChequeColorMeaning("نارنجی", Color(0xFFFB8C00), "ریسک بالا"),
    ChequeColorMeaning("زرد", Color(0xFFFDD835), "احتیاط"),
    ChequeColorMeaning("سفید", Color(0xFFFFFFFF), "ایمن"),
)

/**
 * استعلام چک صیادی (بانک مرکزی) - قبلاً فقط یه دکمه با URL جای‌گیرنده بود؛ الان محتوای واقعی سه راه
 * استعلام (سایت/پیامک/اپلیکیشن) + راهنمای رنگ‌های وضعیت چک، عیناً از رو عکسی که کاربر فرستاد.
 * [sayadId] اختیاریه - وقتی از جزئیاتِ یه چک مشخص باز می‌شه، متن پیامک از قبل با شناسه‌ی همون چک پر
 * می‌شه؛ وقتی از منوی کلیِ «امور چک» باز می‌شه (بدون چک مشخص)، جای شناسه خالی می‌مونه تا کاربر خودش
 * پر کنه.
 */
@Composable
fun SayadInquiryScreen(sayadId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val banner = rememberInAppBanner()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                    }
                    Text("استعلام چک صیادی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }

            item {
                SayadCard(Icons.Filled.Language, Color(0xFF2B7BD6), "استعلام از طریق سایت") {
                    ir.sadteam.loancalc.ui.components.Ltr {
                        Text(
                            SAYAD_WEB_URL.removePrefix("https://"),
                            color = AppMuted, fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().background(AppSurface2, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
                        )
                    }
                    GradientButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SAYAD_WEB_URL)))
                            }.onFailure { banner.show("مرورگری برای باز کردن لینک پیدا نشد") }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) {
                        Text("ورود به صفحه استعلام")
                    }
                }
            }

            item {
                SayadCard(Icons.Filled.Sms, AppPrimary, "استعلام از طریق پیامک") {
                    val smsBody = "*1*1*${sayadId.orEmpty()}#"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppSurface2, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("ارسال به شماره $SAYAD_SMS_NUMBER", color = AppMuted, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                        ir.sadteam.loancalc.ui.components.Ltr {
                            Text(smsBody, color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SmsInfoStat(label = "هزینه", value = "۳۵۰ تومان", modifier = Modifier.weight(1f))
                        SmsInfoStat(label = "سقف روزانه", value = "۴ استعلام", modifier = Modifier.weight(1f))
                        SmsInfoStat(label = "زمان پاسخ", value = "حداکثر ۱۵ دقیقه", modifier = Modifier.weight(1f))
                    }
                    GradientButton(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$SAYAD_SMS_NUMBER")).apply {
                                    putExtra("sms_body", smsBody)
                                }
                                context.startActivity(intent)
                            }.onFailure { banner.show("اپ پیامکی پیدا نشد") }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) {
                        Text(if (sayadId.isNullOrBlank()) "باز کردن پیامک (شناسه رو خودت پر کن)" else "ارسال پیامک استعلام")
                    }
                }
            }

            item {
                // اسم اپ‌ها از رو عکسِ کاربر (تاپ/آپ/ساد۲۴) - چون شناسه‌ی واقعی پکیج/دیپ‌لینک هرکدوم
                // رو نداریم، دکمه‌ها فقط جستجوی اسمِ اپ رو کافه‌بازار/گوگل‌پلی رو باز می‌کنن (بدون
                // هیچ پکیج‌نیم حدسی)، نه یه Intent مستقیم به اپ مشخص.
                SayadCard(Icons.Filled.QrCodeScanner, Color(0xFF7C4DDB), "استعلام از طریق اپلیکیشن") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("تاپ", "آپ", "ساد ۲۴").forEach { appName ->
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$appName")),
                                        )
                                    }.onFailure { banner.show("کافه‌بازار/گوگل‌پلی پیدا نشد") }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(appName, fontSize = 13.sp)
                            }
                        }
                    }
                    Text(
                        "امکان اسکن کد QR چک و استعلام سریع (لطفاً اپلیکیشن مورد نظر رو نصب کنید)",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            item {
                SayadCard(null, AppPrimary, "معنی رنگ‌های وضعیت چک") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chequeColorLegend.forEach { legend ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LegendSwatch(legend.color)
                                Text(legend.name, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 10.dp).width(60.dp))
                                Text(legend.meaning, color = AppMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SmsInfoStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.border(1.5.dp, AppLine, RoundedCornerShape(14.dp)).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
        Text(label, color = AppMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun LegendSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, AppLine, RoundedCornerShape(6.dp)),
    )
}


/** کارتِ هر روشِ استعلام با کاشیِ آیکونِ رنگی کنارِ عنوان (طرحِ Claude Design، ۸ مهر). */
@Composable
private fun SayadCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    color: Color,
    title: String,
    content: @Composable () -> Unit,
) {
    AppCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                if (icon != null) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(21.dp)) }
                    Spacer(Modifier.width(10.dp))
                }
                Text(title, color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            content()
        }
    }
}
