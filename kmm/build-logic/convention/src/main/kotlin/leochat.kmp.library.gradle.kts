import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/** Base Kotlin Multiplatform library: Android + iOS targets, serialization, coroutines, test deps. */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(lib("kotlinx-coroutines-core"))
            implementation(lib("kotlinx-serialization-json"))
            implementation(lib("kotlinx-datetime"))
            implementation(lib("koin-core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(lib("kotlinx-coroutines-test"))
        }
        androidMain.dependencies {
            implementation(lib("kotlinx-coroutines-android"))
        }
    }
}

android {
    namespace = leoNamespace
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
