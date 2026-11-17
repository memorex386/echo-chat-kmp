package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipboardItem
import androidx.compose.ui.platform.Clipboard
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toInt8Array

internal actual suspend fun Clipboard.copyText(text: String, label: String) =
    setClipEntry(ClipEntry.withPlainText(text))

// Browsers let a page copy a PNG and no other kind of picture, so the photo is converted first.
// A photo that can't be read as a picture fails like a refused copy, so the app doesn't say "Copied".
@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.copyPhoto(photo: PlatformFile, label: String) {
    val png = Image.makeFromEncoded(photo.readBytes()).encodeToData(EncodedImageFormat.PNG)?.bytes
        ?: error("The photo couldn't be turned into a PNG")
    setClipEntry(ClipEntry(pngClip(png.toInt8Array())))
}

internal actual val systemConfirmsCopy: Boolean = false

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun pngClip(png: Int8Array): JsArray<ClipboardItem> =
    js("[new ClipboardItem({ 'image/png': new Blob([png], { type: 'image/png' }) })]")
