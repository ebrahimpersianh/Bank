package ir.sadteam.loancalc.ui.accounting

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import java.io.OutputStream

/** خروجی PDF گزارشِ حسابداری (بازه‌ی دلخواه + فیلترِ حساب‌کتاب) - هم‌الگو با
 * `ui/stats/StatsPdfExporter.kt` (همون `PdfDocument`ِ خودِ اندروید + `StaticLayout` برای شکلِ
 * درستِ متنِ فارسی). */
object AccountingPdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    private fun drawRtlLine(canvas: Canvas, text: String, paint: TextPaint, y: Float): Float {
        val width = (PAGE_WIDTH - MARGIN * 2).toInt()
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        canvas.translate(MARGIN, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    fun export(
        rangeLabel: String,
        accountLabel: String,
        income: Double,
        expense: Double,
        categoryBreakdown: List<Pair<String, Double>>,
        transactions: List<AccountTransactionEntity>,
        out: OutputStream,
    ) {
        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var pageNumber = 1

        val titlePaint = TextPaint().apply { textSize = 20f; isFakeBoldText = true }
        val subTitlePaint = TextPaint().apply { textSize = 16f; isFakeBoldText = true }
        val labelPaint = TextPaint().apply { textSize = 13f }

        fun newPageIfNeeded(neededHeight: Float, y: Float): Float {
            if (y + neededHeight <= PAGE_HEIGHT - 40f) return y
            document.finishPage(page)
            pageNumber += 1
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            return 30f
        }

        var y = 30f
        y += drawRtlLine(canvas, "گزارشِ حسابداری - حسابدار من", titlePaint, y) + 10f
        y += drawRtlLine(canvas, "بازه: $rangeLabel — حساب: $accountLabel", labelPaint, y) + 16f

        val summaryLines = listOf(
            "جمعِ درآمد: ${fmt(income)} ریال",
            "جمعِ هزینه: ${fmt(expense)} ریال",
            "مانده: ${fmt(income - expense)} ریال",
        )
        for (line in summaryLines) {
            y = newPageIfNeeded(30f, y)
            y += drawRtlLine(canvas, line, labelPaint, y) + 8f
        }

        if (categoryBreakdown.isNotEmpty()) {
            y += 12f
            y = newPageIfNeeded(30f, y)
            y += drawRtlLine(canvas, "تفکیکِ دسته‌بندی:", subTitlePaint, y) + 10f
            for ((name, amount) in categoryBreakdown) {
                y = newPageIfNeeded(24f, y)
                y += drawRtlLine(canvas, "$name: ${fmt(amount)} ریال", labelPaint, y) + 6f
            }
        }

        y += 16f
        y = newPageIfNeeded(30f, y)
        y += drawRtlLine(canvas, "تراکنش‌ها:", subTitlePaint, y) + 10f

        for (tx in transactions) {
            y = newPageIfNeeded(24f, y)
            val sign = if (tx.type == "DEPOSIT") "+" else "-"
            val line = "${toFa(tx.year)}/${toFa(tx.month)}/${toFa(tx.day)} — " +
                "${tx.category ?: "—"} — $sign${fmt(tx.amount)} ریال" +
                if (tx.description.isNotBlank()) " (${tx.description})" else ""
            y += drawRtlLine(canvas, line, labelPaint, y) + 6f
        }

        document.finishPage(page)
        document.writeTo(out)
        document.close()
    }
}
