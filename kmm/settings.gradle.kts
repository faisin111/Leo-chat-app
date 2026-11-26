import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LeoChatKmp"

include(":androidApp")
include(":composeApp")

include(":core:common")
include(":core:platform")
include(":core:network")
include(":core:datastore")
include(":core:designsystem")
include(":core:navigation")
include(":core:testing")

include(":feature:auth")
include(":feature:splash")
include(":feature:onboarding")
include(":feature:users")
include(":feature:media")
include(":feature:chat")
include(":feature:ai")
include(":feature:calls")
include(":feature:notifications")
include(":feature:settings")
include(":feature:home")
