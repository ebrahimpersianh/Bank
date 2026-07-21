package ir.sadteam.roozegar

import android.app.Application
import ir.sadteam.roozegar.notif.DayNotification
import ir.sadteam.roozegar.notif.Scheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoozegarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DayNotification.ensureChannel(this)
        Scheduler.scheduleMidnight(this)
        // اعلان تاریخ رو با هر بیدارشدن پروسه تازه می‌کنیم (شامل بازشدن اپ، آپدیت ویجت و ...) -
        // خوندن DataStore بلاک‌کننده‌ست، پس نه رو ترد اصلی.
        CoroutineScope(Dispatchers.Default).launch {
            DayNotification.refresh(this@RoozegarApp)
        }
    }
}
