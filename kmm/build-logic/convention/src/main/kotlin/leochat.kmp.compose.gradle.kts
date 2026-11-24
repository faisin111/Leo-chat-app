/** KMP library with Compose Multiplatform UI, lifecycle ViewModels, and Koin Compose. */
plugins {
    id("leochat.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(lib("compose-runtime"))
            implementation(lib("compose-foundation"))
            implementation(lib("compose-material3"))
            implementation(lib("compose-material-icons-extended"))
            implementation(lib("compose-ui"))
            implementation(lib("compose-components-resources"))
            implementation(lib("lifecycle-viewmodel-compose"))
            implementation(lib("lifecycle-runtime-compose"))
            implementation(lib("koin-compose"))
            implementation(lib("koin-compose-viewmodel"))
        }
    }
}
