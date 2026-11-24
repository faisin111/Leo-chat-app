/**
 * Feature module: Compose UI plus every core module a feature may use.
 * Features depend on core; a feature may depend on another feature's public API only when declared explicitly.
 */
plugins {
    id("leochat.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:platform"))
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:navigation"))
            implementation(lib("ktor-client-core"))
            implementation(lib("coil-compose"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(lib("ktor-client-mock"))
        }
    }
}
