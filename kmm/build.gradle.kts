// Load every Gradle plugin once, in the root classloader. Without this each module loads the Kotlin
// plugin separately and shared build services (e.g. the Kotlin/Native commonizer) fail to bind.
// Modules apply them through the build-logic convention plugins.
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.gms.google-services") version "4.6.0" apply false
}
