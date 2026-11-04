package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

// Shared by Android and iOS. A cancelled shot, or a phone with no camera app, saves nothing.
@Composable
internal actual fun rememberTakePhoto(onResult: (saved: Boolean) -> Unit): ((into: PlatformFile) -> Unit)? {
    val camera = rememberCameraPickerLauncher { photo -> onResult(photo != null) }
    return { into -> camera.launch(destinationFile = into) }
}
