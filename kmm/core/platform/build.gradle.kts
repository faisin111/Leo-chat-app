/** Platform services behind interfaces: secure storage (Keychain / EncryptedSharedPreferences) and key-value storage. */
plugins {
    id("leochat.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
            implementation(libs.koin.android)
        }
    }
}
