// ماژول خالص Kotlin (بدون هیچ وابستگی اندرویدی) - منطق تقویم جلالی، گرید ماه و مناسبت‌ها.
// چون اندرویدی نیست، تو سندباکس (که dl.google.com مسدوده) هم قابل تست واقعیه.
plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
