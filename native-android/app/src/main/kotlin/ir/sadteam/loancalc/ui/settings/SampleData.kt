package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.AssetRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * داده‌ی نمونه برای عکس‌های استور (۹ مهر، خواسته‌ی کاربر). در «درباره‌ی جیبک» با ۷ ضربه روی
 * نامِ «جیبک» باز می‌شود. **فقط روی برنامه‌ی خالی** (بی حساب و بی وام) کار می‌کند تا هیچ‌وقت با
 * داده‌ی واقعی قاطی نشود - با حسابِ تست وارد شو، پر کن، عکس بگیر، بعد خارج شو.
 * همه‌ی مبلغ‌ها ریال (قاعده‌ی دیتابیس).
 */
@HiltViewModel
class SampleDataViewModel @Inject constructor(
    private val accounts: AccountRepository,
    private val loans: LoanRepository,
    private val cheques: ChequeRepository,
    private val assets: AssetRepository,
) : ViewModel() {

    suspend fun isEmpty(): Boolean =
        accounts.observeAccounts().first().isEmpty() && loans.getLoans().isEmpty()

    /** `false` یعنی برنامه خالی نبود و هیچ چیزی ثبت نشد. */
    suspend fun fill(): Boolean {
        if (!isEmpty()) return false
        val t = JalaliCalendar.today()
        fun day(offset: Int) = PersianCalendar.addDays(t, -offset)

        accounts.addAccount("حسابِ جاری", "بانک ملت", 285_000_000.0)
        delay(5)
        accounts.addAccount("پس‌انداز", "بانک ملی", 123_000_000.0)
        delay(5)
        accounts.addAccount("کیفِ پول", "نقد", 8_500_000.0, type = "other")
        val list = accounts.observeAccounts().first()
        val main = list.first { it.bankName == "بانک ملت" }.id
        val cash = list.first { it.bankName == "نقد" }.id

        data class Tx(val acc: Long, val type: TransactionType, val amount: Double, val desc: String, val cat: String, val ago: Int)
        val txs = listOf(
            Tx(main, TransactionType.DEPOSIT, 350_000_000.0, "حقوقِ این ماه", "حقوق", (t.d - 1).coerceAtLeast(0)),
            Tx(main, TransactionType.WITHDRAWAL, 120_000_000.0, "اجاره‌ی خانه", "خانه", (t.d - 2).coerceAtLeast(0)),
            Tx(main, TransactionType.WITHDRAWAL, 24_000_000.0, "خریدِ هفتگی", "خوراک", 6),
            Tx(cash, TransactionType.WITHDRAWAL, 8_500_000.0, "رستوران", "خوراک", 4),
            Tx(cash, TransactionType.WITHDRAWAL, 6_000_000.0, "بنزین", "رفت‌وآمد", 3),
            Tx(main, TransactionType.WITHDRAWAL, 2_800_000.0, "قبضِ برق", "قبض", 3),
            Tx(main, TransactionType.WITHDRAWAL, 4_500_000.0, "اینترنت", "قبض", 2),
            Tx(main, TransactionType.WITHDRAWAL, 18_000_000.0, "لباس", "خرید", 1),
            Tx(main, TransactionType.WITHDRAWAL, 9_000_000.0, "سینما و کافه", "تفریح", 1),
            Tx(main, TransactionType.DEPOSIT, 25_000_000.0, "پروژه‌ی فریلنس", "سایر درآمد", 0),
        )
        val base = System.currentTimeMillis()
        txs.forEachIndexed { i, x ->
            val d = day(x.ago)
            accounts.addTransaction(x.acc, x.type, x.amount, x.desc, d.y, d.m, d.d, category = x.cat, id = base + i)
        }

        accounts.setBudget("خوراک", 60_000_000.0, existingId = base + 100)
        accounts.setBudget("رفت‌وآمد", 20_000_000.0, existingId = base + 101)
        accounts.setBudget("تفریح", 30_000_000.0, existingId = base + 102)

        val loanStart = PersianCalendar.addMonths(t, -8)
        loans.addManualLoan("وامِ خرید خودرو", "بانک ملت", 74_000_000.0, 36, 8,
            mapOf("y" to loanStart.y, "m" to loanStart.m, "d" to loanStart.d))
        delay(5)
        val qStart = PersianCalendar.addMonths(t, -3)
        loans.addManualLoan("قرض‌الحسنه", "بانک ملی", 21_000_000.0, 24, 3,
            mapOf("y" to qStart.y, "m" to qStart.m, "d" to qStart.d))

        val due = PersianCalendar.addDays(t, 6)
        cheques.addCheque(ChequeType.PAID, 150_000_000.0, "۱۲۳۴۵۶", "", "بانک ملت", "مرکزی",
            "شرکتِ نمونه", due.y, due.m, due.d, "", null)
        delay(5)
        val due2 = PersianCalendar.addDays(t, 20)
        cheques.addCheque(ChequeType.RECEIVED, 80_000_000.0, "۶۵۴۳۲۱", "", "بانک ملی", "ونک",
            "علی رضایی", due2.y, due2.m, due2.d, "", null)

        val bought = day(40)
        assets.recordTrade("GOLD_18", "یک گرم طلای ۱۸ عیار", "gold", true, 10.0, 2_300_000_000.0, bought.y, bought.m, bought.d)
        assets.recordTrade("SEKKE_EMAMI", "سکه امامی", "gold", true, 2.0, 4_800_000_000.0, bought.y, bought.m, bought.d)
        assets.recordTrade("USD", "دلار", "fiat", true, 500.0, 1_200_000_000.0, bought.y, bought.m, bought.d)
        return true
    }
}
