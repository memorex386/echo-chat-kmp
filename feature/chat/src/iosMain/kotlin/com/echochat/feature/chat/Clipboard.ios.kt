package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import platform.UIKit.UIImage

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.copyText(text: String, label: String) =
    setClipEntry(ClipEntry.withPlainText(text))

// Compose's clip holds only text on iOS, so the picture goes on the pasteboard directly.
// Compose 1.12 deprecates nativeClipboard for platform extensions, but has none for iOS yet.
// FileKit's absolutePath() is a file:// URL on iOS; UIImage needs the plain path.
@Suppress("DEPRECATION")
internal actual suspend fun Clipboard.copyPhoto(photo: PlatformFile, label: String) {
    nativeClipboard.image = UIImage.imageWithContentsOfFile(photo.path) ?: return
}

internal actual val systemConfirmsCopy: Boolean = false
