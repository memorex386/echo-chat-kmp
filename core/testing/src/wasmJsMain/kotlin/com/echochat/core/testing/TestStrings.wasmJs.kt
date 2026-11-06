package com.echochat.core.testing

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

// Karma serves the test build's files under /base/. Fetched from there directly, rather than
// through a proxy back to Karma itself, they don't go missing on a busy machine: Karma drops a
// proxied request it can't finish, and Compose reports a dropped fetch as a missing file (D38).
@OptIn(ExperimentalResourceApi::class)
internal actual fun fetchResourcesWhereTestsServeThem() =
    configureWebResources { resourcePathMapping { path -> "/base/kotlin/$path" } }
