package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

// Desktop and the web have no camera app to hand off to, so they offer only the gallery.
@Composable
internal actual fun rememberTakePhoto(onResult: (saved: Boolean) -> Unit): ((into: PlatformFile) -> Unit)? = null
