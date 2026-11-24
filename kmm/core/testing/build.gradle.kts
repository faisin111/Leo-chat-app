/** Test-only helpers shared by every module's commonTest. */
plugins {
    id("leochat.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            api(project(":core:platform"))
            api(libs.kotlinx.coroutines.test)
            api(kotlin("test"))
        }
    }
}
