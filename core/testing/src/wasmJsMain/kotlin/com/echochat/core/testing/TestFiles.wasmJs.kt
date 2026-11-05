package com.echochat.core.testing

import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile

// A browser has no paths, so the test photo is an empty browser file with the path's name.
@OptIn(ExperimentalWasmJsInterop::class)
actual fun testPhotoFile(path: String): PlatformFile =
    PlatformFile(WebFile.FileWrapper(BrowserFile(JsArray(), path.substringAfterLast('/'))))
