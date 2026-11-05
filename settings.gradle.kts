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
        // Tools Kotlin's web target downloads: Node.js runs the browser tests and the build,
        // Yarn fetches their JavaScript packages, and Binaryen shrinks the release build.
        // Kotlin would add these itself, which this file doesn't allow (echochat.web.tools).
        ivy("https://nodejs.org/dist") {
            name = "Node.js"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen"
            patternLayout { artifact("version_[revision]/binaryen-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "EchoChat"

include(":androidApp")
include(":desktopApp")
include(":webApp")
include(":shared")
include(":feature:chat")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:model")
include(":core:network")
include(":core:notifications")
include(":core:testing")
