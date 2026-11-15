package ir.sadteam.loancalc.server

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * شماره‌ی کاربریِ نمایشی (خواسته‌ی کاربر، ۶ مهر): `Uid:` + ماه + سالِ بدونِ «۱» + روز (دو رقم)
 * + نفرِ چندمِ همان روز. مثال: نفرِ چهارمِ ۲ مهر ۱۴۰۵ → `Uid:7405024`.
 * همه از تاریخِ ساختِ حساب (اولین ورود با کد) به **وقتِ ایران**.
 * شناسه‌ی عددیِ داخلی ([MeResponse.userId]) برای ورک‌فلوهای مدیریتی سرِ جایش می‌ماند.
 */
object UserCode {
    private val SQL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun of(uid: Long, createdAtUtc: String): String {
        val iran = LocalDateTime.parse(createdAtUtc.take(19), SQL_FMT).plusMinutes(210)
        val (jy, jm, jd) = toJalali(iran.year, iran.monthValue, iran.dayOfMonth)
        // نفرِ چندمِ همان روزِ ایران: شمارشِ حساب‌های ساخته‌شده در همان بازه تا همین شناسه.
        val dayStartUtc = iran.toLocalDate().atStartOfDay().minusMinutes(210).format(SQL_FMT)
        val dayEndUtc = iran.toLocalDate().plusDays(1).atStartOfDay().minusMinutes(210).format(SQL_FMT)
        val nth = Db.withConnection { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE created_at >= ? AND created_at < ? AND id <= ?",
            ).use { ps ->
                ps.setString(1, dayStartUtc)
                ps.setString(2, dayEndUtc)
                ps.setLong(3, uid)
                ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 1 }
            }
        }
        return "Uid:$jm${jy - 1000}${jd.toString().padStart(2, '0')}${nth.coerceAtLeast(1)}"
    }

    /** الگوریتمِ استانداردِ میلادی → شمسی. */
    internal fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return Triple(jy, jm, jd)
    }
}
