plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ir.sadteam.roozegar"
    compileSdk = 34

    signingConfigs {
        // همون الگوی تاییدشده‌ی پروژه‌ی «وام من»: امضای دیباگِ ثابت از فایل کامیت‌شده‌ی
        // ci-debug.keystore تو ریشه‌ی مخزن (کلید استاندارد دیباگ اندروید، راز نیست) تا نصب هر بیلد
        // جدید رو بیلد قبلی خطای «conflicts with an existing package» نده.
        getByName("debug") {
            storeFile = rootProject.file("../ci-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // کلید واقعی release از متغیرهای محیطی (سکرت‌های CI) - اگه ست نشده باشن، release با کلید
        // دیباگ امضا می‌شه (فقط برای تست؛ اون APK رو استور آپلود نکن). ⚠️ برای «تقویم من» باید یه کلید
        // release جدا از «وام من» ساخته بشه - از اولین انتشار به بعد دیگه قابل تغییر نیست.
        create("release") {
            val path = System.getenv("ROOZEGAR_KEYSTORE_PATH")
            if (path != null) {
                storeFile = file(path)
                storePassword = System.getenv("ROOZEGAR_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ROOZEGAR_KEY_ALIAS")
                keyPassword = System.getenv("ROOZEGAR_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "ir.sadteam.mycalendar"
        minSdk = 24
        targetSdk = 34
        // versionCode/versionName تو workflow از github.run_number تزریق می‌شن - این‌ها فقط پیش‌فرضِ
        // بیلد لوکال‌ان.
        versionCode = 1
        versionName = "1.0"
    }

    // دو استور ایرانی - فعلاً (فاز ۱) هیچ فرقی بین دو فلیور نیست؛ تو فاز ۴ (پرداخت/تبلیغ) SDK
    // مخصوص هر استور به فلیور خودش اضافه می‌شه. از الان جدا شدن که CI و ساختار آرتیفکت‌ها از روز
    // اول درست باشه.
    flavorDimensions += "store"
    productFlavors {
        create("cafebazaar") { dimension = "store" }
        create("myket") { dimension = "store" }
    }

    buildTypes {
        release {
            // بیلد release (debuggable=false) برای روون‌بودن انیمیشن‌ها/شیشه ضروریه - بیلد debug رو
            // گوشی واقعی همیشه لگ داره (درس پروژه‌ی قبلی). minify فعلاً خاموشه تا وقتی رو گوشی واقعی
            // تست بشه؛ کد ما reflection نداره ولی Glance/Compose بعد از فعال‌کردن باید دستی تست بشن.
            isMinifyEnabled = false
            signingConfig = if (System.getenv("ROOZEGAR_KEYSTORE_PATH") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core"))

    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // تنظیمات کاربر (آنبوردینگ انجام‌شده، شخصی‌سازی نوتیفیکیشن، افکت‌ها) - سبک، بدون Room (دیتابیس
    // رویدادهای کاربر مال فاز ۲ه).
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ویجت‌های صفحه‌ی اصلی (مهم‌ترین بخش ظاهری طبق پرامپت) - Jetpack Glance.
    implementation("androidx.glance:glance-appwidget:1.1.1")
}
