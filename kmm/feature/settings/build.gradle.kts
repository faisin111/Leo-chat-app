plugins {
    id("leochat.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:auth"))
            implementation(project(":feature:media"))
            implementation(project(":feature:users"))
        }
    }
}
