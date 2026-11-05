import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// The web app: a thin launcher for the shared code, run in the browser through Kotlin/Wasm (D35).
// wasmJsBrowserDistribution builds its page into build/dist/wasmJs/productionExecutable.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("echochat.web.tools")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig { outputFileName = "echochat.js" }
            testTask {
                useKarma {
                    useChromeHeadless()
                    useConfigDirectory(rootDir.resolve("gradle/karma.config.d"))
                }
            }
        }
        binaries.executable()
    }
}
