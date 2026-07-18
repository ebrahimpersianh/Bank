package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.formatReminderOffsets
import ir.sadteam.loancalc.core.parseReminderOffsets
import ir.sadteam.loancalc.core.reminderOffsetLabel

/**
 * کارتِ یادآوریِ اختصاصیِ یه وام/چک - رو LoanDetailScreen/ChequeDetailScreen استفاده می‌شه.
 * [currentOffsets] دقیقاً همون فیلدِ خامِ روی Entity ـه: null یعنی «از پیش‌فرضِ سراسری استفاده کن»،
 * رشته‌ی خالی یعنی «برای این مورد کاملاً خاموش»، وگرنه CSVِ روزهای انتخاب‌شده. [onChange] با یکی
 * از همین سه شکل صدا زده می‌شه تا مستقیم رو Entity ذخیره بشه.
 */
@Composable
fun ReminderOverrideCard(
    currentOffsets: String?,
    onChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = when {
        currentOffsets == null -> Mode.DEFAULT
        currentOffsets.isBlank() -> Mode.OFF
        else -> Mode.CUSTOM
    }
    val customSet = if (mode == Mode.CUSTOM) parseReminderOffsets(currentOffsets!!) else emptySet()

    AppCard(label = "یادآوری این مورد", modifier = modifier) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AppChip(label = "پیش‌فرض", selected = mode == Mode.DEFAULT, onClick = { onChange(null) })
                AppChip(label = "خاموش", selected = mode == Mode.OFF, onClick = { onChange("") })
                AppChip(
                    label = "اختصاصی",
                    selected = mode == Mode.CUSTOM,
                    onClick = {
                        if (mode != Mode.CUSTOM) onChange(formatReminderOffsets(setOf(REMINDER_OFFSET_OPTIONS.first())))
                    },
                )
            }
            if (mode == Mode.CUSTOM) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    REMINDER_OFFSET_OPTIONS.forEach { offset ->
                        AppChip(
                            label = reminderOffsetLabel(offset),
                            selected = offset in customSet,
                            onClick = {
                                val updated = if (offset in customSet) customSet - offset else customSet + offset
                                onChange(formatReminderOffsets(updated))
                            },
                        )
                    }
                }
            }
        }
    }
}

private enum class Mode { DEFAULT, OFF, CUSTOM }
