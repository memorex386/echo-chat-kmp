package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberTakePhoto(onResult: (saved: Boolean) -> Unit): ((into: PlatformFile) -> Unit)? = null
