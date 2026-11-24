plugins {
    id("leochat.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:media"))
        }
        androidMain.dependencies {
            implementation(libs.webrtc.android)
            implementation(libs.koin.android)
        }
    }
}
