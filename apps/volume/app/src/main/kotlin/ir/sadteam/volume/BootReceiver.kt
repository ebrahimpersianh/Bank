package ir.sadteam.volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** بعد از روشن‌شدنِ گوشی (یا به‌روزرسانیِ برنامه) دکمه خودکار بالا می‌آید. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Prefs(context).enabled && Settings.canDrawOverlays(context)) {
            OverlayService.start(context)
        }
    }
}
