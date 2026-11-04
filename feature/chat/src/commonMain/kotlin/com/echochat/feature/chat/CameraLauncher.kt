package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * Opens the device's camera app to save a photo into the given file, or is null where there
 * isn't one, as on desktop. [onResult] says whether the camera saved a photo.
 */
@Composable
internal expect fun rememberTakePhoto(onResult: (saved: Boolean) -> Unit): ((into: PlatformFile) -> Unit)?
