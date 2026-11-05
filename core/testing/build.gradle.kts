import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.testing"
    }

    // Adds nonWebMain, as in core:data, for test files made from a path: a browser has none.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withJvm()
                group("ios") { withIos() }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
            // For TestClipboard: Compose's clipboard type, not its UI.
            api(libs.compose.ui)
            // For loadStrings: Compose's string resources.
            api(libs.compose.components.resources)
        }
    }
}
