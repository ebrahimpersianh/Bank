package ir.sadteam.loancalc.data

import java.util.concurrent.atomic.AtomicLong

/**
 * شناسه‌ی یکتای صعودی بر پایه‌ی میلی‌ثانیه، برای ردیف‌هایی که شناسه‌شان را خودمان
 * می‌سازیم (وام، بدهی، طرفِ‌حساب، تراکنش، انتقال).
 *
 * `System.currentTimeMillis()` به‌تنهایی برای این کار کافی نیست: دو فراخوانیِ
 * پشتِ‌هم می‌توانند یک میلی‌ثانیه را بدهند و چون DAOها از `@Upsert` استفاده
 * می‌کنند، ردیفِ دوم بی‌صدا جای ردیفِ اول می‌نشیند (همین هشدار در KDoc
 * `addTransaction` هم آمده). این هلپر تضمین می‌کند هر شناسه از قبلی بزرگ‌تر است،
 * حتی اگر ساعت همان میلی‌ثانیه باشد — پس دو وام/بدهی/تراکنش هیچ‌وقت شناسه‌ی مشترک
 * نمی‌گیرند و حذفِ بازپرداختِ یک وام نمی‌تواند پرداختِ وامِ دیگر را بزند (باگِ
 * ۲۳ نوامبر ۲۰۲۶).
 */
object UniqueIds {
    private val lastIssued = AtomicLong(0L)

    fun next(): Long {
        while (true) {
            val now = System.currentTimeMillis()
            val prev = lastIssued.get()
            val next = maxOf(now, prev + 1)
            if (lastIssued.compareAndSet(prev, next)) return next
        }
    }
}
