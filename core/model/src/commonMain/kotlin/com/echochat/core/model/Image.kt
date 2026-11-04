package com.echochat.core.model

/** A picture in a message. [path] is a file in the app's own storage, as a plain string so this module needs no platform types. */
data class Image(
    val path: String,
)
