// A multiplatform library with Compose UI. UI tests in commonTest run on the desktop JVM here,
// and on the iOS simulator on a Mac. Android can't run them as local tests.
plugins {
    id("echochat.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-ui").get())
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-components-resources").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("compose-ui-test").get())
        }
        jvmTest.dependencies {
            // Skia's native renderer for this computer, so UI tests can draw on the desktop JVM.
            implementation(compose.desktop.currentOs)
        }
    }
}
