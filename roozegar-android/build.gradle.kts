// نسخه‌ها عمداً همون نسخه‌های تاییدشده‌ی پروژه‌ی native-android (وام من) هستن که CI همین ریپو
// باهاشون سبز می‌شه - قبل از ارتقای هر کدوم، اول رو CI تست بگیر.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
