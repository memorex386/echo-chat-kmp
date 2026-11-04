plugins {
    id("echochat.kmp.compose")
}

kotlin {
    android {
        namespace = "com.echochat.shared"
    }

    // iOS gets one framework holding all the shared code. Swift imports it as `Shared`.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":feature:chat"))
            implementation(libs.koin.core)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.navigation3.ui)
            implementation(libs.kotlinx.serialization.core)
        }
    }
}
