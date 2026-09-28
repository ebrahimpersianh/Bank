package ir.sadteam.loancalc.ui.cheque

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeEntity
import java.io.OutputStream

/**
 * خروجی PDF لیست چک‌ها - هم‌الگو با ui/stats/StatsPdfExporter (همون تکنیک: PdfDocument خودِ
 * Android + StaticLayout برای raste-درست فارسی، بدون کتابخونه‌ی شخص‌ثالث).
 */
object ChequePdfExporter {
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

    fun export(cheques: List<ChequeEntity>, out: OutputStream) {
        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply { textSize = 20f; isFakeBoldText = true }
        val labelPaint = TextPaint().apply { textSize = 12.5f }

        var y = 30f
        y += drawRtlLine(canvas, "گزارش چک‌ها - جیبک", titlePaint, y) + 16f

        val active = cheques.filter { !it.archived }
        val received = active.count { it.type == "RECEIVED" }
        val paid = active.count { it.type == "PAID" }
        y += drawRtlLine(canvas, "تعداد چک‌های دریافتی: ${toFa(received)} — پرداختی: ${toFa(paid)}", labelPaint, y) + 16f

        for (cheque in active) {
            if (y > PAGE_HEIGHT - 80f) {
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 30f
            }
            val typeLabel = if (cheque.type == "RECEIVED") "دریافتی" else "پرداختی"
            val statusLabel = chequeStatusLabel(cheque.status)
            val dateLabel = "${toFa(cheque.dueDay)}/${toFa(cheque.dueMonth)}/${toFa(cheque.dueYear)}"
            val line = "${cheque.ownerName} (${cheque.bankName}) — $typeLabel، ${fmt(cheque.amount)} ریال، " +
                "سررسید $dateLabel، $statusLabel"
            y += drawRtlLine(canvas, line, labelPaint, y) + 6f
        }

        document.finishPage(page)
        document.writeTo(out)
        document.close()
    }
}
