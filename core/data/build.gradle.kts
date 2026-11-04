plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.data"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)
            implementation(project(":core:database"))
            implementation(project(":core:network"))
        }
    }
}
