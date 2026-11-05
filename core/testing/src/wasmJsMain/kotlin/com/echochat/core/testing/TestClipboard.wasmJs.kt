package com.echochat.core.testing

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import kotlinx.coroutines.await
import kotlin.js.Promise

// A browser keeps a clip as ClipboardItems, whose text comes out of a Blob later.
@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
actual suspend fun ClipEntry.plainText(): String? {
    if (clipboardItems.length == 0) return null
    val item = clipboardItems[0] ?: return null
    val types = (0 until item.types.length).map { item.types[it].toString() }
    if ("text/plain" !in types) return null
    return blobText(item.getType("text/plain".toJsString()).await()).await<JsString>().toString()
}

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun blobText(blob: JsAny): Promise<JsString> = js("blob.text()")
