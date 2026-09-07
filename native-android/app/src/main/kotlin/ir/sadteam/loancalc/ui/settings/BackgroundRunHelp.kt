package ir.sadteam.loancalc.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * **کمکِ «چرا برنامه تو پس‌زمینه کار نمی‌کنه؟»**
 *
 * خواسته‌ی کاربر این بود: «می‌خوام برنامه همیشه در حال اجرا باشه، بعد از روشن‌کردنِ گوشی
 * خودش بالا بیاد، و وقتی می‌بندمش بسته نشه.»
 *
 * ⚠️ **اندروید اجازه‌ی «همیشه باز ماندن» نمی‌دهد** و هیچ اپی نمی‌تواند آن را از داخلِ خودش
 * تضمین کند. کاری که واقعاً شدنی است این است که بخش‌های پس‌زمینه (خواندنِ پیامک/اعلانِ بانکی
 * و یادآورِ سررسید) بعد از بستنِ اپ و بعد از ری‌استارتِ گوشی زنده بمانند. سه شرط دارد و
 * **هر سه از تنظیماتِ خودِ گوشی داده می‌شوند، نه از داخلِ برنامه**:
 *
 * ۱. معافیت از بهینه‌سازیِ باتری ([batteryUnrestricted]) - دیالوگِ استانداردِ اندروید دارد.
 * ۲. «اجرای خودکار» (autostart) - فقط روی گوشی‌های چینی هست و **هیچ API عمومی‌ای ندارد**؛
 *    تنها راهش باز کردنِ صفحه‌ی مخصوصِ همان سازنده است ([autostartIntent]).
 * ۳. قفل‌کردنِ اپ در فهرستِ اپ‌های اخیر - کارِ دستیِ کاربر است، مسیرِ نرم‌افزاری ندارد.
 *
 * نامِ کلاس‌های زیر از مستنداتِ عمومیِ همین سازنده‌ها است و ممکن است در نسخه‌های تازه‌تر عوض
 * شده باشد - برای همین همه‌ی مسیرها `resolveActivity` می‌شوند و اگر نبود، صفحه‌ی اطلاعاتِ
 * خودِ برنامه باز می‌شود؛ هیچ‌وقت کرش نمی‌کند و هیچ‌وقت به صفحه‌ی خالی نمی‌رسد.
 */
object BackgroundRunHelp {

    /** آیا اپ از بهینه‌سازیِ باتری معاف شده؟ (شرطِ اول) */
    fun batteryUnrestricted(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** آیا این گوشی از آن‌هایی است که پس‌زمینه را تهاجمی می‌کشد و «اجرای خودکار» جدا دارد؟ */
    fun needsAutostartSetting(): Boolean =
        oemKey() != null

    /** دیالوگِ استانداردِ معافیت از بهینه‌سازیِ باتری. */
    fun batteryIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.parse("package:${context.packageName}"))

    /**
     * صفحه‌ی «اجرای خودکار»ِ خودِ سازنده. اگر پیدا نشد `null` برمی‌گرداند تا فراخوان به
     * [appDetailsIntent] برگردد - حدس‌زدنِ کورِ یک اکتیویتی یعنی `ActivityNotFoundException`.
     */
    fun autostartIntent(context: Context): Intent? {
        val candidates = when (oemKey()) {
            "xiaomi" -> listOf(
                "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            )
            "huawei" -> listOf(
                "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
            )
            "oppo" -> listOf(
                "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
            )
            "vivo" -> listOf(
                "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            )
            "samsung" -> listOf(
                "com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity",
            )
            else -> emptyList()
        }
        for ((pkg, cls) in candidates) {
            val intent = Intent().setComponent(ComponentName(pkg, cls))
            if (intent.resolveActivity(context.packageManager) != null) return intent
        }
        return null
    }

    /** فال‌بکِ همیشه‌موجود: صفحه‌ی اطلاعاتِ خودِ برنامه در تنظیماتِ اندروید. */
    fun appDetailsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))

    /** نامِ فارسیِ مسیرِ «اجرای خودکار» روی همین گوشی، برای اینکه کاربر بداند دنبالِ چه بگردد. */
    fun autostartHint(): String = when (oemKey()) {
        "xiaomi" -> "امنیت ← مجوزها ← اجرای خودکار (Autostart) ← «جیبک» را روشن کن."
        "huawei" -> "مدیرِ گوشی ← راه‌اندازیِ برنامه‌ها ← «جیبک» را دستی کن و هر سه گزینه را روشن بگذار."
        "oppo" -> "تنظیمات ← مدیریتِ برنامه ← اجرای خودکار ← «جیبک» را روشن کن."
        "vivo" -> "iManager ← مدیریتِ برنامه ← اجرای پس‌زمینه ← «جیبک» را روشن کن."
        "samsung" -> "باتری ← محدودیتِ استفاده در پس‌زمینه ← «جیبک» را روی «نامحدود» بگذار."
        else -> "در تنظیماتِ باتریِ گوشی، «جیبک» را روی حالتِ بدونِ محدودیت بگذار."
    }

    private fun oemKey(): String? {
        val brand = (Build.MANUFACTURER + " " + Build.BRAND).lowercase()
        return when {
            listOf("xiaomi", "redmi", "poco").any { brand.contains(it) } -> "xiaomi"
            listOf("huawei", "honor").any { brand.contains(it) } -> "huawei"
            listOf("oppo", "realme", "oneplus").any { brand.contains(it) } -> "oppo"
            brand.contains("vivo") -> "vivo"
            brand.contains("samsung") -> "samsung"
            else -> null
        }
    }
}
