package ir.sadteam.loancalc.server.routes

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.Db
import kotlinx.serialization.Serializable

/*
 * آپدیتِ خودکار: نه کافه‌بازار نه مایکت API عمومی‌ای برای «آخرین نسخه چیه» ندارن (برخلافِ Google
 * Play که In-App Update API داره)، برای همین خودِ این سرور منبعِ حقیقتِ نسخه‌ی آخره. اپ هر بار باز
 * می‌شه این رو با BuildConfig.VERSION_CODE خودش مقایسه می‌کنه؛ اگه سرور عددِ بزرگ‌تری داشت، یه بنر
 * «نسخه‌ی جدید موجوده» نشون می‌ده که کاربر رو به cafebazaarUrl یا myketUrl (بسته به BuildConfig.FLAVOR
 * خودِ نصب) می‌بره. عمومی و بدون auth ـه، دیتای کاربر خاصی نیست.
 *
 * *** بعدِ هر انتشارِ واقعیِ نسخه‌ی جدید رو کافه‌بازار/مایکت، این مقدار رو دستی رو VPS آپدیت کن: ***
 *   sqlite3 ~/loan-server/data.sqlite \
 *     "UPDATE app_version SET latest_version_code = <شماره‌ی run_number همون بیلد>,
 *      cafebazaar_url = 'https://cafebazaar.ir/app/ir.sadteam.loancalc',
 *      myket_url = 'https://myket.ir/app/ir.sadteam.loancalc',
 *      updated_at = datetime('now') WHERE id = 1;"
 *   pm2 restart loan-calc-api نیازی نیست - این یه UPDATE ساده‌ست، سرور بلافاصله ردیفِ جدید رو می‌بینه.
 */
@Serializable
private data class AppVersionResponse(
    val latestVersionCode: Int,
    val cafebazaarUrl: String?,
    val myketUrl: String?,
)

fun Route.appVersionRoutes() {
    get("/api/app-version") {
        val result = Db.withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT latest_version_code, cafebazaar_url, myket_url FROM app_version WHERE id = 1").use { rs ->
                    if (rs.next()) {
                        AppVersionResponse(
                            latestVersionCode = rs.getInt("latest_version_code"),
                            cafebazaarUrl = rs.getString("cafebazaar_url"),
                            myketUrl = rs.getString("myket_url"),
                        )
                    } else {
                        null
                    }
                }
            }
        }
        if (result != null) call.respond(result) else call.respond(AppVersionResponse(1, null, null))
    }
}
