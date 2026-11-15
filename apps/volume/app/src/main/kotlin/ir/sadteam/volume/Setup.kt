package ir.sadteam.volume

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * اجازه‌هایی که برای «همیشه روی صفحه، هیچ‌وقت بسته نشو، بعد از روشن‌شدنِ گوشی خودکار بالا بیا»
 * لازم است. هر کدام یا از اندروید خوانده می‌شود، یا (اجرای خودکارِ گوشی‌های چینی) قابلِ خواندن نیست
 * و فقط صفحه‌اش باز می‌شود.
 */
object Setup {

    fun overlayOk(c: Context) = Settings.canDrawOverlays(c)

    fun notifOk(c: Context) = Build.VERSION.SDK_INT < 33 ||
        c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun batteryOk(c: Context): Boolean {
        val pm = c.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(c.packageName)
    }

    fun overlayIntent(c: Context) =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${c.packageName}"))

    /** پنجره‌ی استاندارد؛ اگر گوشی نشانش نداد، فهرستِ باتری؛ آخر صفحه‌ی برنامه. */
    fun openBattery(c: Context, preferList: Boolean) {
        val tries = buildList {
            if (!preferList) add(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${c.packageName}")))
            add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            add(appDetails(c))
        }
        openFirst(c, tries)
    }

    private fun oem(): String? {
        val m = (Build.MANUFACTURER ?: "").lowercase()
        return when {
            "xiaomi" in m || "redmi" in m || "poco" in m -> "xiaomi"
            "huawei" in m || "honor" in m -> "huawei"
            "oppo" in m || "realme" in m || "oneplus" in m -> "oppo"
            "vivo" in m || "iqoo" in m -> "vivo"
            "samsung" in m -> "samsung"
            else -> null
        }
    }

    fun isXiaomi() = oem() == "xiaomi"

    /** صفحه‌ی «اجرای خودکار»ِ خودِ سازنده؛ نبود → صفحه‌ی برنامه. */
    fun openAutostart(c: Context) {
        val list = when (oem()) {
            "xiaomi" -> listOf("com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity")
            "huawei" -> listOf(
                "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
            )
            "oppo" -> listOf(
                "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
            )
            "vivo" -> listOf("com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            "samsung" -> listOf("com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity")
            else -> emptyList()
        }
        openFirst(c, list.map { (p, k) -> Intent().setComponent(ComponentName(p, k)) } + appDetails(c))
    }

    /** شیائومی جدا اجازه‌ی «نمایشِ پنجره هنگامِ اجرا در پس‌زمینه» و «نمایش روی صفحه‌ی قفل» می‌خواهد. */
    fun openXiaomiPopup(c: Context) {
        openFirst(
            c,
            listOf(
                Intent("miui.intent.action.APP_PERM_EDITOR")
                    .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    .putExtra("extra_pkgname", c.packageName),
                Intent("miui.intent.action.APP_PERM_EDITOR").putExtra("extra_pkgname", c.packageName),
                appDetails(c),
            ),
        )
    }

    /** تنظیماتِ اعلانِ همین برنامه. */
    fun openNotificationSettings(c: Context) {
        openFirst(
            c,
            listOf(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, c.packageName),
                appDetails(c),
            ),
        )
    }

    /** سرویسِ دسترسی‌پذیریِ «ولوم» روشن است؟ */
    fun a11yOk(c: Context): Boolean {
        if (VolumeA11yService.running) return true
        val list = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val me = ComponentName(c, VolumeA11yService::class.java)
        return list.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    fun openA11y(c: Context) = openFirst(c, listOf(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), appDetails(c)))

    fun openAppDetails(c: Context) = openFirst(c, listOf(appDetails(c)))

    private fun appDetails(c: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${c.packageName}"))

    private fun openFirst(c: Context, tries: List<Intent>) {
        for (i in tries) {
            val ok = runCatching {
                if (i.resolveActivity(c.packageManager) == null) error("none")
                c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
            if (ok) return
        }
    }
}
