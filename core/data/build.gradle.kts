import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.data"
    }

    // Adds nonWebMain, shared by every platform but the web, for the photo store: FileKit's
    // files by path and its cache folder don't exist in a browser.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withJvm()
                // The whole ios group, not just its targets, so code in iosMain sees nonWebMain.
                group("ios") { withIos() }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)
            api(libs.filekit.core)
            api(project(":core:notifications"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
        }
    }
}
