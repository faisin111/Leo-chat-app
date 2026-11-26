/** App composition: root composable, Koin graph, NavHost, iOS framework. No feature business logic. */
plugins {
    id("leochat.kmp.compose")
}

kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:platform"))
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:navigation"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:users"))
            implementation(project(":feature:media"))
            implementation(project(":feature:chat"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:calls"))
            implementation(project(":feature:notifications"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:home"))
            implementation(libs.ktor.client.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
