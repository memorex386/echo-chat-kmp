package com.echochat.core.testing

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.NativeClipboard

/**
 * Keeps what the app copies, instead of putting it on the system clipboard. [native] stands in
 * for the platform's own clipboard, for code that writes to it directly.
 */
class TestClipboard(private val native: NativeClipboard? = null) : Clipboard {
    var entry: ClipEntry? = null

    override suspend fun getClipEntry(): ClipEntry? = entry

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        entry = clipEntry
    }

    @Deprecated("Deprecated in Compose's Clipboard, but iOS's photo copy still needs it")
    override val nativeClipboard: NativeClipboard
        get() = native ?: error("This test clipboard has no platform clipboard behind it")
}

/** The text in a clip, read the way each platform keeps it. */
expect fun ClipEntry.plainText(): String?
