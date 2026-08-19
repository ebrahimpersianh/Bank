package ir.sadteam.loancalc.ui.stats

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.data.db.LoanEntity
import java.io.OutputStream

/**
 * خروجی PDF گزارش آمار - عمداً از `android.graphics.pdf.PdfDocument` (بخشی از خودِ Android SDK)
 * استفاده می‌کنه، نه یه کتابخونه‌ی PDF شخص‌ثالث جدید (iText/PdfBox و مشابه) - یه گزارش متنی ساده،
 * نیازی به کتابخونه‌ی سنگین نداره. متن با `StaticLayout` (نه `Canvas.drawText` خام) رسم می‌شه، چون
 * `drawText` خام bidi فارسی/عربی رو درست shape/reorder نمی‌کنه و متن برعکس/بهم‌ریخته درمیاد.
 */
object StatsPdfExporter {
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

    fun export(summary: StatsSummary, loans: List<LoanEntity>, out: OutputStream) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint().apply { textSize = 20f; isFakeBoldText = true }
        val subTitlePaint = TextPaint().apply { textSize = 16f; isFakeBoldText = true }
        val labelPaint = TextPaint().apply { textSize = 13f }

        var y = 30f
        y += drawRtlLine(canvas, "گزارش آمار وام‌ها - جیبک", titlePaint, y) + 16f

        val lines = listOf(
            "تعداد وام‌ها: ${summary.loanCount}",
            "مجموع مبلغ وام‌ها: ${fmt(summary.totalAmount)} ریال",
            "مجموع پرداخت‌شده: ${fmt(summary.paidAmount)} ریال",
            "مانده‌ی کل: ${fmt(summary.remainingAmount)} ریال",
            "اقساط پرداخت‌شده: ${summary.paidInstallments} از ${summary.totalInstallments}",
            "درصد پیشرفت: ${(summary.progressRatio * 100).toInt()}٪",
        )
        for (line in lines) {
            y += drawRtlLine(canvas, line, labelPaint, y) + 8f
        }

        y += 16f
        y += drawRtlLine(canvas, "جزئیات هر وام:", subTitlePaint, y) + 12f

        for (loan in loans) {
            if (y > PAGE_HEIGHT - 60f) break
            val line = "${loan.name} (${loan.bank}) — ${fmt(loan.installment)} ریال × ${loan.n} قسط، " +
                "${loan.paidCount} پرداخت‌شده"
            y += drawRtlLine(canvas, line, labelPaint, y) + 6f
        }

        document.finishPage(page)
        document.writeTo(out)
        document.close()
    }
}
