package com.echochat.core.model

/**
 * A picture in a message. [path] says where the app keeps it: a file in its own storage, or, in a
 * browser, a key to the photos it holds in memory. It's a plain string so this module needs no
 * platform types.
 */
data class Image(
    val path: String,
)
