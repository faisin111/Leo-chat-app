/** Local cache (JSON in key-value storage), keyed by user and conversation. */
plugins {
    id("leochat.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:platform"))
        }
    }
}
