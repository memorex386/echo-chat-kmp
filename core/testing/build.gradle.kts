plugins {
    id("echochat.kmp.library")
}

kotlin {
    android {
        namespace = "com.echochat.core.testing"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
        }
    }
}
