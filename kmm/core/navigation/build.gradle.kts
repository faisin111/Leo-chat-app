/** Type-safe routes, transitions, and navigation helpers. The NavHost itself lives in :composeApp. */
plugins {
    id("leochat.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.navigation.compose)
        }
    }
}
