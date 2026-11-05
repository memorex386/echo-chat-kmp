package com.echochat.feature.chat

import androidx.compose.ui.platform.Clipboard
import io.github.vinceglb.filekit.PlatformFile

/** Puts [text] on the clipboard. [label] names the clip where the platform shows one. */
internal expect suspend fun Clipboard.copyText(text: String, label: String)

/** Puts [photo] on the clipboard, so it pastes into apps that take pictures. */
internal expect suspend fun Clipboard.copyPhoto(photo: PlatformFile, label: String)

/** Whether the system confirms a copy itself, so the app needn't. */
internal expect val systemConfirmsCopy: Boolean
