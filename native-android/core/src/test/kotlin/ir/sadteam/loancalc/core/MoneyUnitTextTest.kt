package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyUnitTextTest {
    @Test
    fun convertsOldMessagesToCurrentUnit() {
        assertEquals("۶۰۰٬۰۰۰ ریال از «Blu»", localizeMoneyUnits("۶۰٬۰۰۰ تومان از «Blu»", rial = true))
        assertEquals("۶۰٬۰۰۰ تومان از «Blu»", localizeMoneyUnits("۶۰۰٬۰۰۰ ریال از «Blu»", rial = false))
        assertEquals("۶۰٬۰۰۰ تومان", localizeMoneyUnits("۶۰٬۰۰۰ تومان", rial = false))
        assertEquals("پنج میلیون تومان", localizeMoneyUnits("پنج میلیون تومان", rial = true))
    }
}
