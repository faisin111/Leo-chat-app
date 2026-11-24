plugins {
    id("leochat.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }
    }
}
