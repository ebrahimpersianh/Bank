package ir.sadteam.loancalc.server.routes

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.Db
import kotlinx.serialization.Serializable

/*
 * نرخِ خدمات اعتباری (دیجی‌پی، اسنپ‌پی و ...) - قبلاً فقط تو کدِ اپ بومی hardcode بود، هر بار یکی
 * از این سرویس‌ها نرخش رو عوض می‌کرد باید یه نسخه‌ی جدیدِ اپ منتشر می‌شد. عمومی و بدون auth ـه چون
 * دیتای کاربر خاصی نیست، یه لیستِ عمومیه که همه‌ی نصب‌ها یکسان می‌بینن.
 */
@Serializable
private data class CreditRateDto(
    val key: String,
    val name: String,
    val colorHex: String,
    val logoAsset: String,
    val ratePct: Double,
    val months: Int,
    val minAmount: Long,
    val maxAmount: Long,
)

@Serializable
private data class CreditRatesResponse(val rates: List<CreditRateDto>)

fun Route.creditRatesRoutes() {
    get("/api/credit-rates") {
        val rates = Db.withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT * FROM credit_rates ORDER BY sort_order ASC").use { rs ->
                    val list = mutableListOf<CreditRateDto>()
                    while (rs.next()) {
                        list.add(
                            CreditRateDto(
                                key = rs.getString("key"),
                                name = rs.getString("name"),
                                colorHex = rs.getString("color_hex"),
                                logoAsset = rs.getString("logo_asset"),
                                ratePct = rs.getDouble("rate_pct"),
                                months = rs.getInt("months"),
                                minAmount = rs.getLong("min_amount"),
                                maxAmount = rs.getLong("max_amount"),
                            ),
                        )
                    }
                    list
                }
            }
        }
        call.respond(CreditRatesResponse(rates))
    }
}
