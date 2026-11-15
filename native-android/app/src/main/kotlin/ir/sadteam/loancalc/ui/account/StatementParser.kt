package ir.sadteam.loancalc.ui.account

import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toEnDigits
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream

/** یک ردیفِ صورت‌حساب. `amount` به ریال و همیشه مثبت؛ جهت با [deposit]. */
data class StatementRow(val date: PersianDate, val amount: Double, val deposit: Boolean, val description: String)

/**
 * 📄 **خواندنِ صورت‌حسابِ بانکی** (۸ مهر، خواسته‌ی کاربر): CSV یا Excel (xlsx) که از اینترنت‌بانک
 * گرفته شده. ستون‌ها از روی سرتیتر حدس زده می‌شوند (تاریخ، شرح، برداشت/بدهکار، واریز/بستانکار، یا یک
 * ستونِ «مبلغ» با علامت). تاریخِ میلادی هم به شمسی برمی‌گردد. ردیفی که تاریخ یا مبلغش خوانده نشود
 * بی‌صدا کنار می‌رود.
 */
object StatementParser {

    fun parse(fileName: String, input: InputStream): List<StatementRow> {
        val table = if (fileName.lowercase().endsWith(".xlsx")) readXlsx(input) else readCsv(input.readBytes())
        return toRows(table)
    }

    // ───────── جدولِ خام ─────────

    private fun readCsv(bytes: ByteArray): List<List<String>> {
        var text = String(bytes, Charsets.UTF_8).removePrefix("﻿")
        // بعضی بانک‌ها هنوز Windows-1256 می‌دهند؛ اگر UTF-8 پر از علامتِ خراب بود، دوباره بخوان.
        if (text.count { it == '�' } > 3) text = String(bytes, charset("windows-1256"))
        val firstLine = text.lineSequence().firstOrNull().orEmpty()
        val sep = listOf(',', ';', '\t', '|').maxByOrNull { c -> firstLine.count { it == c } } ?: ','
        return text.lineSequence().filter { it.isNotBlank() }.map { splitCsv(it, sep) }.toList()
    }

    private fun splitCsv(line: String, sep: Char): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { sb.append('"'); i++ }
                c == '"' -> quoted = !quoted
                c == sep && !quoted -> { out += sb.toString().trim(); sb.clear() }
                else -> sb.append(c)
            }
            i++
        }
        out += sb.toString().trim()
        return out
    }

    private fun readXlsx(input: InputStream): List<List<String>> {
        var shared = emptyList<String>()
        var sheet: ByteArray? = null
        ZipInputStream(input).use { zip ->
            generateSequence { zip.nextEntry }.forEach { e ->
                when {
                    e.name == "xl/sharedStrings.xml" -> shared = readShared(zip.readBytes())
                    e.name == "xl/worksheets/sheet1.xml" -> sheet = zip.readBytes()
                }
            }
        }
        val data = sheet ?: return emptyList()
        val rows = mutableListOf<List<String>>()
        val p = XmlPullParserFactory.newInstance().newPullParser()
        p.setInput(data.inputStream(), "UTF-8")
        var row = sortedMapOf<Int, String>()
        var col = 0
        var type: String? = null
        var inValue = false
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            when (p.eventType) {
                XmlPullParser.START_TAG -> when (p.name) {
                    "row" -> row = sortedMapOf()
                    "c" -> {
                        col = colIndex(p.getAttributeValue(null, "r") ?: "")
                        type = p.getAttributeValue(null, "t")
                    }
                    "v", "t" -> inValue = true
                }
                XmlPullParser.TEXT -> if (inValue) {
                    val raw = p.text
                    row[col] = (row[col] ?: "") + if (type == "s") shared.getOrElse(raw.trim().toIntOrNull() ?: -1) { "" } else raw
                }
                XmlPullParser.END_TAG -> when (p.name) {
                    "v", "t" -> inValue = false
                    "row" -> if (row.isNotEmpty()) {
                        val max = row.lastKey()
                        rows += (0..max).map { row[it]?.trim().orEmpty() }
                    }
                }
            }
        }
        return rows
    }

    private fun readShared(bytes: ByteArray): List<String> {
        val out = mutableListOf<String>()
        val p = XmlPullParserFactory.newInstance().newPullParser()
        p.setInput(bytes.inputStream(), "UTF-8")
        val sb = StringBuilder()
        var inT = false
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            when (p.eventType) {
                XmlPullParser.START_TAG -> if (p.name == "si") sb.clear() else if (p.name == "t") inT = true
                XmlPullParser.TEXT -> if (inT) sb.append(p.text)
                XmlPullParser.END_TAG -> if (p.name == "t") inT = false else if (p.name == "si") out += sb.toString()
            }
        }
        return out
    }

    private fun colIndex(ref: String): Int {
        var n = 0
        for (ch in ref) { if (ch.isLetter()) n = n * 26 + (ch.uppercaseChar() - 'A' + 1) else break }
        return (n - 1).coerceAtLeast(0)
    }

    // ───────── ستون‌ها ─────────

    private fun find(header: List<String>, vararg keys: String): Int =
        header.indexOfFirst { h -> keys.any { h.contains(it, ignoreCase = true) } }

    private fun toRows(table: List<List<String>>): List<StatementRow> {
        // سرتیتر: اولین ردیف (از ۱۰ ردیفِ اول) که ستونِ تاریخ دارد - بعضی خروجی‌ها چند خطِ عنوان دارند.
        val hIdx = table.take(10).indexOfFirst { r -> r.any { it.contains("تاریخ") || it.contains("date", true) } }
        if (hIdx < 0) return emptyList()
        val h = table[hIdx].map { toEnDigits(it) }
        val date = find(h, "تاریخ", "date")
        val desc = find(h, "شرح", "توضیح", "description", "narrative", "عملیات")
        val out = find(h, "برداشت", "بدهکار", "debit", "withdraw")
        val inn = find(h, "واریز", "بستانکار", "credit", "deposit")
        val amt = find(h, "مبلغ", "amount")
        return table.drop(hIdx + 1).mapNotNull { r ->
            val d = parseDate(r.getOrNull(date) ?: return@mapNotNull null) ?: return@mapNotNull null
            val outV = if (out >= 0) money(r.getOrNull(out)) else null
            val inV = if (inn >= 0) money(r.getOrNull(inn)) else null
            val (value, deposit) = when {
                (inV ?: 0.0) > 0 -> inV!! to true
                (outV ?: 0.0) > 0 -> outV!! to false
                amt >= 0 -> {
                    val raw = r.getOrNull(amt).orEmpty()
                    val v = money(raw) ?: return@mapNotNull null
                    val neg = raw.contains('-') || raw.contains('−') || raw.contains('(')
                    v to !neg
                }
                else -> return@mapNotNull null
            }
            if (value <= 0) return@mapNotNull null
            StatementRow(d, value, deposit, r.getOrNull(desc)?.take(120).orEmpty().ifBlank { "صورت‌حساب" })
        }
    }

    private fun money(s: String?): Double? {
        if (s == null) return null
        return toEnDigits(s).filter { it.isDigit() || it == '.' }.toDoubleOrNull()
    }

    private fun parseDate(raw: String): PersianDate? {
        val nums = Regex("\\d+").findAll(toEnDigits(raw)).map { it.value.toInt() }.toList()
        if (nums.size < 3) {
            // عددِ سریالِ تاریخِ Excel (روز از ۱۸۹۹/۱۲/۳۰)
            val serial = toEnDigits(raw).toDoubleOrNull() ?: return null
            if (serial < 20_000 || serial > 80_000) return null
            val c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                set(1899, 11, 30, 0, 0, 0); add(java.util.Calendar.DAY_OF_MONTH, serial.toInt())
            }
            return JalaliCalendar.fromGregorian(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
        }
        val (a, b, c) = Triple(nums[0], nums[1], nums[2])
        return when {
            a in 1300..1499 -> PersianDate(a, b, c)
            c in 1300..1499 -> PersianDate(c, b, a)
            a in 1900..2100 -> JalaliCalendar.fromGregorian(a, b, c)
            c in 1900..2100 -> JalaliCalendar.fromGregorian(c, b, a)
            a in 0..99 && b in 1..12 -> PersianDate(1400 + a, b, c) // «۰۵/۰۷/۰۸»
            else -> null
        }?.takeIf { it.m in 1..12 && it.d in 1..31 }
    }
}
