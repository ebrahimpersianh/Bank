package ir.sadteam.loancalc.ui.stats

import ir.sadteam.loancalc.data.db.LoanEntity
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * خروجی اکسل (.xlsx) گزارش آمار، کنار PDF - عمداً بدون هیچ کتابخونه‌ی جدید (نه Apache POI، نه
 * مشابه؛ اونا برای اندروید سنگین/پیچیده‌ان). فرمت xlsx در واقع فقط یه فایل zip با چندتا XML ثابت
 * (OOXML) هست، پس با `java.util.zip.ZipOutputStream` خودِ جاوا (بدون هیچ وابستگی جدید) دستی ساخته
 * می‌شه - نسخه‌ی حداقلیِ معتبر: یه شیت با ردیف‌های inline-string/عددی، بدون sharedStrings.xml (که
 * برای این حجم داده لازم نیست).
 */
object StatsXlsxExporter {
    fun export(summary: StatsSummary, loans: List<LoanEntity>, out: OutputStream) {
        val rows = buildList<List<Any>> {
            add(listOf("نام وام", "بانک", "مبلغ قسط", "تعداد اقساط", "پرداخت‌شده", "مانده"))
            loans.forEach { loan ->
                add(
                    listOf(
                        loan.name,
                        loan.bank,
                        loan.installment,
                        loan.n,
                        loan.paidCount,
                        loan.installment * (loan.n - loan.paidCount),
                    ),
                )
            }
            add(emptyList())
            add(listOf("تعداد وام‌ها", summary.loanCount))
            add(listOf("مجموع مبلغ وام‌ها", summary.totalAmount))
            add(listOf("مجموع پرداخت‌شده", summary.paidAmount))
            add(listOf("مانده‌ی کل", summary.remainingAmount))
            add(listOf("اقساط پرداخت‌شده", summary.paidInstallments))
            add(listOf("تعداد کل اقساط", summary.totalInstallments))
        }

        ZipOutputStream(out).use { zip ->
            writeEntry(zip, "[Content_Types].xml", CONTENT_TYPES_XML)
            writeEntry(zip, "_rels/.rels", RELS_XML)
            writeEntry(zip, "xl/workbook.xml", WORKBOOK_XML)
            writeEntry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            writeEntry(zip, "xl/worksheets/sheet1.xml", buildSheetXml(rows))
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildSheetXml(rows: List<List<Any>>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            val rowNumber = rowIndex + 1
            sb.append("<row r=\"$rowNumber\">")
            row.forEachIndexed { colIndex, value ->
                val cellRef = "${columnLetter(colIndex)}$rowNumber"
                when (value) {
                    is Number -> sb.append("<c r=\"$cellRef\"><v>$value</v></c>")
                    else -> {
                        val text = escapeXml(value.toString())
                        sb.append("<c r=\"$cellRef\" t=\"inlineStr\"><is><t xml:space=\"preserve\">$text</t></is></c>")
                    }
                }
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun columnLetter(index: Int): String {
        var n = index
        val sb = StringBuilder()
        while (true) {
            sb.insert(0, ('A' + (n % 26)))
            n = n / 26 - 1
            if (n < 0) break
        }
        return sb.toString()
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private const val RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private const val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="گزارش وام‌ها" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private const val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
}
