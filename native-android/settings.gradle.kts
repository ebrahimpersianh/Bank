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
        // خرید درون‌برنامه‌ای هر دو استور از JitPack میاد، نه Maven Central/Google: Poolakey
        // (کافه‌بازار) و myket-billing-client (مایکت) - رجوع کن به app/build.gradle.kts.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "loan-calculator-native"
include(":app")
include(":core")
include(":data")
