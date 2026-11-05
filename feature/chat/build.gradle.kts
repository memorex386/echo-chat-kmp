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

    // Adds mobileMain, shared by Android and iOS only, for the camera: desktop and the web have
    // none. nonMobileMain is the other half: desktop and the web, which share a computer's
    // keyboard and have no camera or status bar.
    // withAndroidTarget() only matches the old Android target, not AGP 9's library target.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withIos()
            }
            group("nonMobile") {
                withJvm()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.navigationevent.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    // Public so the whole-app tests in shared and webApp can load the chat's strings first (D37).
    publicResClass = true
    packageOfResClass = "com.echochat.feature.chat.resources"
}
