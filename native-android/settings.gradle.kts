pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // پلاگین Poolakey (خرید درون‌برنامه‌ای کافه‌بازار) از JitPack میاد، نه Maven Central/Google -
        // دقیقاً همون دلیلی که وب‌ورژن (www/) هم تو build-apk.yml همین ریپو رو نیاز داره.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "loan-calculator-native"
include(":app")
include(":core")
include(":data")
