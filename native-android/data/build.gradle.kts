plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "ir.sadteam.loancalc.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    api("androidx.room:room-runtime:2.6.1")
    api("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // رمزنگاری دیتابیس Room (SQLCipher) - وام/چک/حساب اطلاعات مالی/شخصی‌ان، اگه گوشی روت باشه یکی
    // می‌تونه فایل خام دیتابیس رو بخونه؛ این‌ها همون فایل رو با AES-256 رمزنگاری می‌کنن. عمداً
    // android-database-sqlcipher (نه بازنویسیِ جدیدترِ sqlcipher-android) چون فقط این یکی
    // net.sqlcipher.database.SupportFactory (پیاده‌سازیِ آماده‌ی SupportSQLiteOpenHelper.Factory
    // برای Room) رو داره؛ نسخه‌ی جدید فقط درایورِ خامه، بدونِ پُلِ Room.
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite:2.4.0")
    // پسورد رمزنگاری رو خودش نمی‌سازیم/جایی هاردکد نمی‌کنیم - یه کلید تصادفی تولید و با
    // Android Keystore (سخت‌افزاری، هیچ‌وقت از دستگاه خارج نمی‌شه) نگه‌داری می‌شه.
    implementation("androidx.security:security-crypto:1.0.0")
}
