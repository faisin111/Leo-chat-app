import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

android {
    namespace = "com.leochat.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.leochat.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    val keystorePropsFile = rootProject.file("keystore.properties")
    val hasReleaseKeystore = keystorePropsFile.exists()
    signingConfigs {
        if (hasReleaseKeystore) {
            val props = Properties().apply { keystorePropsFile.inputStream().use(::load) }
            create("release") {
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation(project(":composeApp"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:media"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    testImplementation(kotlin("test"))
}
