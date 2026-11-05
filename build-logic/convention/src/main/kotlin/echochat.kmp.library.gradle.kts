import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// A Kotlin Multiplatform library for Android, desktop (the JVM), iOS and the web. Code in
// commonMain is compiled once per target; commonTest runs once per target that can run on this
// machine. The web's tests run in headless Chrome.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("echochat.web.tools")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        compileSdk = 37
        minSdk = 26
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useConfigDirectory(rootDir.resolve("gradle/karma.config.d"))
                }
            }
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
