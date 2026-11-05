plugins {
    id("echochat.web.application")
}

kotlin {
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.navigationevent.compose)
        }
        wasmJsTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(project(":core:testing"))
            implementation(project(":feature:chat"))
            implementation(libs.koin.core)
        }
    }
}
