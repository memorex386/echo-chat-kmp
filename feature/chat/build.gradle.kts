import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("echochat.kmp.feature")
}

kotlin {
    android {
        namespace = "com.echochat.feature.chat"
        androidResources.enable = true
    }

    // Adds mobileMain, shared by Android and iOS only, for the camera: desktop has none.
    // withAndroidTarget() only matches the old Android target, not AGP 9's library target.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withIos()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.filekit.dialogs.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "com.echochat.feature.chat.resources"
}
