package ir.sadteam.volume

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

/**
 * ۱۴ آبان (گوشیِ کاربر: «وسطِ تماس باریکه نبود»): صفحه‌ی تماسِ گوشی پنجره‌های معمولیِ
 * برنامه‌ها را پنهان می‌کند، ولی پنجره‌ی «دسترسی‌پذیری» را نه. وقتی کاربر این را در تنظیماتِ
 * دسترسی‌پذیری روشن کند، باریکه از این‌جا کشیده می‌شود: وسطِ تماس و روی صفحه‌ی قفل هم هست،
 * اعلانِ ثابت لازم ندارد و گوشی هم نمی‌بندَدش.
 * هیچ چیزی از صفحه خوانده نمی‌شود؛ فقط پنجره‌ی باریکه.
 */
class VolumeA11yService : AccessibilityService() {

    private var host: PillHost? = null
    val pillShowing get() = host?.added == true

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        running = true
        // باریکه‌ی پنجره‌ی معمولی کنار برود تا دوتا نشود.
        runCatching { stopService(Intent(this, OverlayService::class.java)) }
        ensurePill()
    }

    fun ensurePill() {
        if (!Prefs(this).enabled) return
        val h = host ?: PillHost(this, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY).also { host = it }
        h.add()
    }

    fun showPill() = host?.show()

    fun hidePill() {
        host?.remove()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        host?.onConfigurationChanged()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    /** کاربر دسترسی‌پذیری را خاموش کرد → برگشت به پنجره‌ی معمولی. */
    private fun cleanup() {
        if (!running) return
        running = false
        instance = null
        host?.remove()
        host = null
        if (Prefs(this).enabled && Settings.canDrawOverlays(this)) OverlayService.start(this)
    }

    companion object {
        @Volatile var running = false
        @Volatile var instance: VolumeA11yService? = null
    }
}
