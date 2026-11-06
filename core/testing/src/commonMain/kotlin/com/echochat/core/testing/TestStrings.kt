package com.echochat.core.testing

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Loads [strings] before a UI test shows its screen. In a browser, Compose fetches each string the
 * first time it's shown, so a screen's first frames have no text, and Compose's UI tests don't
 * wait for those fetches. A string that's already loaded shows straight away. Elsewhere strings
 * load at once anyway, and this changes nothing.
 */
suspend fun loadStrings(strings: Map<String, StringResource>) {
    fetchResourcesWhereTestsServeThem()
    strings.values.forEach { getString(it) }
}

/** In a browser test, points Compose's resource fetches at where the test server keeps the files. */
internal expect fun fetchResourcesWhereTestsServeThem()
