import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootEnvSpec

// How the build uses Kotlin's web tools. Applied to the root project and by the web conventions.

// Kotlin's web target downloads Node.js, Yarn and Binaryen, and would add their download sites to
// the root project and to each project that builds for the web. settings.gradle.kts lets no
// project add its own, so it lists them instead, and this tells Kotlin not to.
plugins.withType<WasmNodeJsRootPlugin> { extensions.findByType<WasmNodeJsEnvSpec>()?.let { it.downloadBaseUrl = null } }
plugins.withType<WasmNodeJsPlugin> { extensions.findByType<WasmNodeJsEnvSpec>()?.let { it.downloadBaseUrl = null } }
plugins.withType<WasmYarnPlugin> { extensions.findByType<WasmYarnRootEnvSpec>()?.let { it.downloadBaseUrl = null } }
plugins.withType<BinaryenPlugin> { extensions.findByType<BinaryenEnvSpec>()?.let { it.downloadBaseUrl = null } }

// Browser tests take turns, even though the build otherwise runs modules in parallel.
val oneBrowserAtATime = gradle.sharedServices.registerIfAbsent("oneBrowserAtATime", OneBrowserAtATime::class) {
    maxParallelUsages = 1
}
tasks.withType<KotlinJsTest>().configureEach { usesService(oneBrowserAtATime) }
