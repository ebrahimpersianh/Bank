plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ir.sadteam.loancalc"
    compileSdk = 34

    defaultConfig {
        applicationId = "ir.sadteam.loancalc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // برای BuildConfig.VERSION_NAME تو crash/CrashReporter.kt - از AGP 8 به بعد پیش‌فرض خاموشه.
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))

    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    // اینترو/اسپلش‌اسکرین واقعی موقع باز شدن اپ (قبلاً اصلاً وجود نداشت - فقط یه صفحه‌ی خالی).
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("com.google.android.material:material:1.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.navigation:navigation-compose:2.8.3")

    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // یادآوری سررسید (نوتیفیکیشن واقعی) - WorkManager برای یه چک روزانه‌ی پس‌زمینه که با ری‌استارت
    // گوشی هم زنده می‌مونه (بدون نیاز به BroadcastReceiver دستی برای BOOT_COMPLETED)، hilt-work برای
    // تزریق LoanRepository داخل Worker.
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // خرید درون‌برنامه‌ای واقعی کافه‌بازار (اشتراک) - SDK بومی رسمی، همون‌ کتابخونه‌ای که پلاگین
    // Capacitor نسخه‌ی وب (www/) هم زیرش استفاده می‌کنه. از JitPack میاد (settings.gradle.kts).
    implementation("com.github.cafebazaar.Poolakey:poolakey:2.2.0")
}
