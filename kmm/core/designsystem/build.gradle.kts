/** LeoChat theme, responsive layout, bundled fonts/images, and every reusable Compose component. */
plugins {
    id("leochat.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:platform"))
            implementation(libs.coil.compose)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.leochat.resources"
    generateResClass = always
}
