package ir.sadteam.loancalc

import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.countsInReports
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.netDangShares
import ir.sadteam.loancalc.ui.accounting.ReportPeriod
import ir.sadteam.loancalc.ui.accounting.buildReportStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **قفلِ قاعده‌های پولِ گزارش** (۱۵ مهر). هر بار که کسی «چه چیزی خرج/درآمد حساب می‌شود» یا
 * کم‌شدنِ سهمِ دنگ را عوض کند، این تست‌ها در بیلد می‌گویند عددِ گزارش جابه‌جا شد.
 *
 * واحدِ همه‌ی مبلغ‌ها **ریال** است، مثلِ دیتابیس.
 */
class ReportMoneyTest {

    private val today = PersianDate(1405, 7, 15)

    private fun tx(
        id: Long,
        type: String,
        amount: Double,
        sourceType: String? = null,
        sourceId: String? = null,
        day: Int = 10,
        month: Int = today.m,
        confirmed: Boolean = true,
    ) = AccountTransactionEntity(
        id = id,
        accountId = 1L,
        type = type,
        amount = amount,
        description = "t$id",
        year = today.y,
        month = month,
        day = day,
        createdAt = "2026-10-07T00:00:00Z",
        category = "خوراک",
        sourceType = sourceType,
        sourceId = sourceId,
        confirmed = confirmed,
    )

    private val million = 1_000_000.0

    // ─── چه چیزی در گزارش حساب می‌شود ─────────────────────────────────────────────

    @Test
    fun `ordinary, loan and cheque payments count in reports`() {
        assertTrue(countsInReports(tx(1, "WITHDRAWAL", million)))
        assertTrue(countsInReports(tx(2, "WITHDRAWAL", million, sourceType = "loan", sourceId = "5:3")))
        assertTrue(countsInReports(tx(3, "WITHDRAWAL", million, sourceType = "cheque", sourceId = "9")))
        assertTrue(countsInReports(tx(4, "WITHDRAWAL", million, sourceType = "recurring", sourceId = "2:1405-7")))
    }

    @Test
    fun `transfers, assets, goals, dong shares and debt settlements never count`() {
        for (src in listOf("transfer", "asset", "goal", "dang", "debt")) {
            assertFalse("$src باید نه خرج باشد نه درآمد", countsInReports(tx(1, "DEPOSIT", million, sourceType = src)))
            assertFalse("$src باید نه خرج باشد نه درآمد", countsInReports(tx(2, "WITHDRAWAL", million, sourceType = src)))
        }
    }

    // ─── دنگ: سهمِ دوست‌ها از خرجِ شام کم می‌شود ───────────────────────────────────

    @Test
    fun `three friends' shares shrink a 4M dinner to my own 1M`() {
        val list = listOf(
            tx(1, "WITHDRAWAL", 4 * million),
            tx(2, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-11@1"),
            tx(3, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-12@1"),
            tx(4, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-13@1"),
        ).netDangShares()
        assertEquals(million, list.first { it.id == 1L }.amount, 0.0)
    }

    @Test
    fun `a share with no linked dinner changes nothing`() {
        val list = listOf(
            tx(1, "WITHDRAWAL", 4 * million),
            tx(2, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-11@0"),
        )
        assertEquals(list, list.netDangShares())
    }

    @Test
    fun `old dong deposits without a link are left alone`() {
        val list = listOf(
            tx(1, "WITHDRAWAL", 4 * million),
            tx(2, "DEPOSIT", million, sourceType = "debt", sourceId = "dang-11"),
        )
        assertEquals(list, list.netDangShares())
    }

    @Test
    fun `dinner never goes below zero`() {
        val list = listOf(
            tx(1, "WITHDRAWAL", million),
            tx(2, "DEPOSIT", 3 * million, sourceType = "dang", sourceId = "dang-11@1"),
        ).netDangShares()
        assertEquals(0.0, list.first { it.id == 1L }.amount, 0.0)
    }

    @Test
    fun `netting only touches the report copy, not other rows`() {
        val salary = tx(5, "DEPOSIT", 10 * million)
        val list = listOf(
            tx(1, "WITHDRAWAL", 4 * million),
            salary,
            tx(2, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-11@1"),
        ).netDangShares()
        assertEquals(salary, list.first { it.id == 5L })
        assertEquals(3, list.size)
    }

    // ─── کلِ گزارشِ ماه، با همه‌ی قاعده‌ها کنارِ هم ──────────────────────────────

    @Test
    fun `month report shows only real spending and real income`() {
        val txs = listOf(
            // شام ۴ میلیون + سه سهمِ برگشتی → خرجِ واقعی ۱ میلیون
            tx(1, "WITHDRAWAL", 4 * million),
            tx(2, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-11@1"),
            tx(3, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-12@1"),
            tx(4, "DEPOSIT", million, sourceType = "dang", sourceId = "dang-13@1"),
            // جابه‌جایی بینِ دو حسابِ خودم - هیچ
            tx(5, "WITHDRAWAL", 2 * million, sourceType = "transfer", sourceId = "x"),
            tx(6, "DEPOSIT", 2 * million, sourceType = "transfer", sourceId = "x"),
            // قرض دادم و پس گرفتم - هیچ
            tx(7, "WITHDRAWAL", 5 * million, sourceType = "debt", sourceId = "70"),
            tx(8, "DEPOSIT", 5 * million, sourceType = "debt", sourceId = "71"),
            // پول به هدفِ پس‌انداز - هیچ
            tx(9, "WITHDRAWAL", 3 * million, sourceType = "goal", sourceId = "g1"),
            // قسطِ وام - خرجِ واقعی
            tx(10, "WITHDRAWAL", 2 * million, sourceType = "loan", sourceId = "5:3"),
            // حقوق - درآمدِ واقعی
            tx(11, "DEPOSIT", 10 * million),
        )
        val stats = buildReportStats(txs, emptyList(), today, ReportPeriod.MONTH)
        assertEquals(3 * million, stats.periodSpend, 0.0)
        assertEquals(10 * million, stats.periodIncome, 0.0)
    }

    @Test
    fun `last month's spending stays out of this month`() {
        val txs = listOf(
            tx(1, "WITHDRAWAL", million),
            tx(2, "WITHDRAWAL", 7 * million, month = today.m - 1),
        )
        val stats = buildReportStats(txs, emptyList(), today, ReportPeriod.MONTH)
        assertEquals(million, stats.periodSpend, 0.0)
    }
}
