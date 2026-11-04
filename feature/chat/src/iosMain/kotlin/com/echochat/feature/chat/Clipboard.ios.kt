package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import platform.UIKit.UIImage

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.copyText(text: String, label: String) =
    setClipEntry(ClipEntry.withPlainText(text))

// Compose's clip holds only text on iOS, so the picture goes on the pasteboard directly.
// Compose 1.12 deprecates nativeClipboard for platform extensions, but has none for iOS yet.
@Suppress("DEPRECATION")
internal actual suspend fun Clipboard.copyPhoto(path: String, label: String) {
    nativeClipboard.image = UIImage.imageWithContentsOfFile(path) ?: return
}

internal actual val systemConfirmsCopy: Boolean = false
