// A feature module: Compose UI, ViewModels from Koin and serializable Navigation 3 keys.
plugins {
    id("echochat.kmp.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("lifecycle-runtime-compose").get())
            implementation(libs.findLibrary("lifecycle-viewmodel-compose").get())
            implementation(libs.findLibrary("navigation3-runtime").get())
            implementation(libs.findLibrary("kotlinx-serialization-core").get())
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
