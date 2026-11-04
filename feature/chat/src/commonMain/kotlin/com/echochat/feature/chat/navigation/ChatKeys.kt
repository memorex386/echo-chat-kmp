package com.echochat.feature.chat.navigation

import androidx.navigation3.runtime.NavKey
import com.echochat.core.model.Author
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** The conversation with Echo. */
@Serializable
data object ChatKey : NavKey

/** One photo, full screen. */
@Serializable
data class PhotoKey(
    val path: String,
    val from: Author,
) : NavKey

/**
 * Tells the saved back stack how to save this module's keys. Android could find them by
 * reflection, but iOS can't, so every platform lists them here.
 */
val chatNavKeys = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(ChatKey::class)
        subclass(PhotoKey::class)
    }
}
