package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.ParsedBankSms
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDisabledText
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.core.fmt

/**
 * صفحه‌ی **آزمایشِ تشخیص** - زیرصفحه‌ی خودش، نه شیت (خروجیش خونده می‌شه و شیت جا کم داره).
 *
 * ⚠️ حالتِ «ج»ی طرح (این متن اصلاً پیامکِ بانکی نیست) پیاده **نشد**، چون
 * `BankSmsParser.parse` برای هر دو حالت `null` برمی‌گردونه و از هم تفکیکشون نمی‌کنه -
 * خودِ طراح گفت اگه موتور تفکیک نمی‌کنه ولش کن.
 */
@Composable
internal fun SmsParseTestScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ParsedBankSms?>(null) }
    var checked by remember { mutableStateOf(false) }

    SettingsSubPageScaffold(title = "آزمایشِ تشخیص", onBack = onBack) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; checked = false },
            placeholder = { Text("متنِ پیامکِ بانک را اینجا بچسبان") },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, lineHeight = 20.sp), colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
        Text(
            "متن ذخیره نمی‌شود.",
            color = AppLabel,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
        )
        GradientButton(
            onClick = { result = BankSmsParser.parse(text); checked = true },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("بررسی")
        }
        if (checked) {
            val parsed = result
            if (parsed != null) {
                AppCard(
                    backgroundColor = AppPrimaryPill,
                    borderColor = AppPrimaryBorder,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = AppPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "شناسایی شد",
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    ParseResultRow(
                        label = "مبلغ",
                        value = "${fmt((parsed.amountRial) / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
                        valueColor = if (parsed.type == TransactionType.WITHDRAWAL) AppDangerInk else AppPrimaryInk,
                    )
                    ParseResultRow(
                        label = "نوع",
                        value = if (parsed.type == TransactionType.WITHDRAWAL) "برداشت" else "واریز",
                    )
                    ParseResultRow(
                        label = "کارت",
                        value = parsed.cardSuffix?.let { toFa(it) },
                    )
                }
            } else {
                AppCard(
                    variant = AppCardVariant.URGENT,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text("شناسایی نشد", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(
                        "موتور از این متن مبلغ و نوعِ تراکنش درنیاورد. اگه این پیامکِ واقعیِ " +
                            "بانکته، متنش رو برای ما بفرست تا موتور بهترش کنیم.",
                        color = AppDangerInk,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
/** یه ردیفِ کلید-مقدارِ نتیجه‌ی آزمایش. مقدارِ درنیامده «—»ی کم‌رنگ می‌شه، نه خالی. */
@Composable
private fun ParseResultRow(label: String, value: String?, valueColor: Color = AppText) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            value ?: "—",
            color = if (value == null) AppDisabledText else valueColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
        )
    }
}
