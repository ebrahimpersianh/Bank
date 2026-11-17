package ir.sadteam.loancalc.ui.rating

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import ir.sadteam.loancalc.BuildConfig

/**
 * بازکردنِ صفحه‌ی جیبک در استوری که اپ از آن نصب شده - **نه** Google Play In-App Review.
 *
 * منبعِ نصب از `PackageManager` خوانده می‌شود (همان روشی که `LoanCalcApplication`
 * برای پروفایلِ استفاده می‌کند): کافه‌بازار خودش را `com.farsitel.bazaar` معرفی می‌کند
 * و مایکت `ir.myket` (نامِ قدیمیِ `com.myket` هم پذیرفته می‌شود). اگر منبع نامعلوم بود
 * (نصبِ مستقیمِ APK و...)، فلیورِ بیلد تصمیم می‌گیرد - همان قاعده‌ای که بقیه‌ی جاهای
 * برنامه (صفحه‌ی اشتراک، بنرِ آپدیت) استفاده می‌کنند.
 *
 * اول `market://details` امتحان می‌شود تا استورِ نصب‌شده مستقیم باز شود؛ اگر هیچ
 * برنامه‌ای آن را نگیرد، نشانیِ وبِ همان استور باز می‌شود.
 */
fun openStorePage(context: Context) {
    val webUrl = storeWebUrl(context)
    val opened = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ir.sadteam.loancalc")),
        )
        true
    }.getOrDefault(false)
    if (!opened) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))) }
    }
}

/** نشانیِ وبِ صفحه‌ی جیبک در استورِ نصب‌کننده (برای fallbackِ [openStorePage]). */
fun storeWebUrl(context: Context): String {
    val installer = runCatching {
        if (Build.VERSION.SDK_INT >= 30) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    }.getOrNull()
    return when (installer) {
        "com.farsitel.bazaar" -> "https://cafebazaar.ir/app/ir.sadteam.loancalc"
        "com.myket", "ir.myket" -> "https://myket.ir/app/ir.sadteam.loancalc"
        else -> if (BuildConfig.FLAVOR == "myket") {
            "https://myket.ir/app/ir.sadteam.loancalc"
        } else {
            "https://cafebazaar.ir/app/ir.sadteam.loancalc"
        }
    }
}
