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

    signingConfigs {
        // امضای دیباگِ ثابت و قطعی: مستقیم از فایل کامیت‌شده‌ی `ci-debug.keystore` تو ریشه‌ی مخزن
        // امضا می‌شه، نه از `~/.android/debug.keystore` که بین محیط‌ها/رانرها فرق می‌کرد و باعث می‌شد
        // نصب یه بیلد جدید رو بیلد قبلی با «App not installed»/«conflicts with an existing package»
        // رد بشه (چون امضاها فرق داشتن). چون کلید ثابته، همه‌ی بیلدها امضای یکسان دارن و رو هم به‌روز
        // نصب می‌شن. این همون کلید استاندارد دیباگ اندروید (alias=androiddebugkey، پسورد عمومی
        // «android») هست، پس رازی توش نیست - عمداً کامیت شده (رجوع کن به CLAUDE.md).
        getByName("debug") {
            storeFile = rootProject.file("../ci-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // کلید واقعیِ release: از متغیرهای محیطی خونده می‌شه (نه هاردکد تو گیت - این‌ها واقعاً محرمانه‌ان،
        // برخلاف کلید دیباگِ عمومیِ بالا)، چون بیلد release قبلاً با همون کلید دیباگِ عمومی امضا می‌شد که
        // برای انتشار واقعی رو کافه‌بازار خطرناکه (هرکسی می‌تونه یه آپدیتِ جعلی با همون کلید امضا کنه).
        // اگه RELEASE_KEYSTORE_PATH ست نشده باشه (لوکال، یا CI بدون سکرت‌ها) از همین signingConfig
        // استفاده نمی‌شه - رجوع کن به buildTypes.release پایین که فال‌بک می‌زنه رو کلید دیباگ.
        create("release") {
            val path = System.getenv("RELEASE_KEYSTORE_PATH")
            if (path != null) {
                storeFile = file(path)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "ir.sadteam.loancalc"
        minSdk = 24
        targetSdk = 34
        // versionCode تو workflow از github.run_number تزریق می‌شه (تا نصب بیلد جدید رو قبلی downgrade
        // حساب نشه)؛ این مقدار پیش‌فرض فقط برای build لوکاله.
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // ریشه‌ی «لگ» گزارش‌شده‌ی کاربر، نصبِ بیلد debug بود: تو بیلد debug فلگ debuggable
            // بهینه‌سازی ART/AOT رو خاموش می‌کنه و runtime کامپوز هم چک‌های اضافه داره، پس اسکرول و
            // تعویض تب همیشه سنگینه. بیلد release (debuggable=false) همون کد رو روان اجرا می‌کنه.
            // minify طبق خواسته‌ی کاربر روشنه. قوانینِ proguard-rules.pro همه‌ی جاهایی که این اپ
            // مستقیم و از رو reflection با Gson کار می‌کنه رو پوشش می‌دن: Room entities + core
            // (data.db.**, core.**) و DTOهای Retrofit (data.network.** - قبل از این تغییر جا افتاده
            // بود و اضافه شد، چون هیچ‌کدوم @SerializedName نداشتن و بدون keep همه‌ی درخواست‌های
            // شبکه‌ای بی‌صدا می‌شکستن). چون این‌جا (sandbox) امکان تست runtime رو گوشی واقعی نیست، حتماً
            // بعد از نصبِ این بیلد رو گوشی: ذخیره‌ی وام، بستن/بازکردنِ اپ (دیتا بمونه)، ورود با OTP،
            // همگام‌سازی/پشتیبان‌گیریِ ابری، و بازیابی از بکاپ رو دستی تست کن.
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // اگه کلید واقعیِ release در دسترس بود (RELEASE_KEYSTORE_PATH ست شده - رجوع کن به
            // signingConfigs بالا) باهاش امضا می‌شه؛ وگرنه (لوکال/CI بدون سکرت‌ها) فال‌بک می‌زنه رو
            // همون کلید دیباگِ عمومی، فقط برای این‌که بیلد لوکال/تستی خراب نشه - این بیلد هیچ‌وقت نباید
            // مستقیم رو کافه‌بازار آپلود بشه.
            signingConfig = if (System.getenv("RELEASE_KEYSTORE_PATH") != null) {
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
    // ProcessLifecycleOwner برای قفل امنیتی: تشخیص «کل اپ رفت پس‌زمینه» (ON_STOP) تا با برگشت به
    // اپ دوباره قفل بشه، نه فقط قفل‌شدن یه‌بار موقع باز شدن.
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation("com.google.android.material:material:1.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // قفل امنیتی PIN+اثر انگشت (برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست صریح جدید کاربر) -
    // BiometricPrompt نیاز به FragmentActivity داره (نه ComponentActivity ساده)، برای همین
    // MainActivity هم به FragmentActivity تغییر کرد.
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.8.3")

    implementation("androidx.navigation:navigation-compose:2.8.3")

    // پرکردنِ خودکارِ کدِ OTP از رو پیامک با SMS User Consent API - بدونِ مجوزِ READ_SMS، فقط با
    // تاییدِ دستیِ کاربر رو دیالوگِ سیستمی (رجوع کن به ui/auth/SmsOtpAutofill.kt).
    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")

    // ویجت صفحه اصلی (قسط بعدی) - Jetpack Glance، بدون RemoteViews دستی.
    implementation("androidx.glance:glance-appwidget:1.1.1")

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
