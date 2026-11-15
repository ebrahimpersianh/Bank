plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.sadteam.volume"
    compileSdk = 34

    defaultConfig {
        applicationId = "ir.sadteam.volume"
        // TYPE_APPLICATION_OVERLAY از اندروید ۸ است.
        minSdk = 26
        targetSdk = 34
        versionCode = (project.findProperty("volumeVersionCode") as String?)?.toInt() ?: 1
        versionName = "1.0.$versionCode"
    }

    signingConfigs {
        // کلیدِ ثابت داخلِ مخزن: برنامه‌ی شخصی است و فقط برای این‌که نسخه‌ی بعدی روی قبلی نصب شود.
        create("fixed") {
            storeFile = file("volume.keystore")
            storePassword = "volume123"
            keyAlias = "volume"
            keyPassword = "volume123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("fixed")
        }
        debug {
            signingConfig = signingConfigs.getByName("fixed")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
