package com.echochat.core.testing

// Android, iOS and desktop read resources from the app itself, with nothing to fetch.
internal actual fun fetchResourcesWhereTestsServeThem() = Unit
