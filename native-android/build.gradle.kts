plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    // تستِ اسکرین‌شاتِ Compose (بدونِ نیاز به گوشی/امولاتور - رو JVM رندر می‌کنه با layoutlib) -
    // برای گرفتنِ باگ‌های ظاهریِ ساده (سایه/گوشه/همپوشانی) قبل از رسیدن به دستِ کاربر، رجوع کن به
    // app/src/test/kotlin/.../ScreenshotTest.kt.
    id("app.cash.paparazzi") version "1.3.5" apply false
}
