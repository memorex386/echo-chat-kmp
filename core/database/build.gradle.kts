plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.database"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
