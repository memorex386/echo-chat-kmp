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

rootProject.name = "EchoChat"

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":feature:chat")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:model")
include(":core:network")
include(":core:notifications")
include(":core:testing")
