plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.notifications"
        androidResources.enable = true
        // Android's notifications are checked on the JVM with Robolectric. This module has no
        // Compose UI tests, which Android local tests can't run.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.koin.core)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.atomicfu)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.ext.junit)
        }
    }
}

tasks.withType<Test>().configureEach {
    // Robolectric's Android 16 runtime reaches into this JDK package.
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
