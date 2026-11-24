plugins {
    id("leochat.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:auth"))
            implementation(project(":feature:users"))
        }
    }
}
