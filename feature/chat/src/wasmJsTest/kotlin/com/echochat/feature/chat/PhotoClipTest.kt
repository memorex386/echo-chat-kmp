package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import com.echochat.core.testing.TestClipboard
import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
class PhotoClipTest {

    @Test
    fun `ACTION-03 copying a photo puts the picture on the clipboard as a PNG`() = runTest {
        // A JPEG, as a camera takes: browsers only let a page copy PNGs, so it has to be converted.
        val jpeg = assertNotNull(
            Surface.makeRasterN32Premul(40, 30).makeImageSnapshot().encodeToData(EncodedImageFormat.JPEG),
        ).bytes
        val photo = PlatformFile(WebFile.FileWrapper(BrowserFile(listOf<JsAny?>(jpeg.toInt8Array()).toJsArray(), "sunset.jpg")))
        val clipboard = TestClipboard()

        clipboard.copyPhoto(photo, "Message from Echo Chat")

        val items = assertNotNull(clipboard.entry, "Nothing was copied").clipboardItems
        val item = assertNotNull(items[0])
        assertEquals(listOf("image/png"), (0 until item.types.length).map { item.types[it].toString() })
        val png = Int8Array(blobBytes(item.getType("image/png".toJsString()).await()).await()).toByteArray()
        val pasted = Image.makeFromEncoded(png)
        assertEquals(40 to 30, pasted.width to pasted.height)
    }

    // Copying it would otherwise count as done, and the app would say "Copied".
    @Test
    fun `ACTION-02 a photo that can't be read as a picture isn't copied`() = runTest {
        val text = "Not a picture".encodeToByteArray()
        val photo = PlatformFile(WebFile.FileWrapper(BrowserFile(listOf<JsAny?>(text.toInt8Array()).toJsArray(), "not-a-photo.jpg")))
        val clipboard = TestClipboard()

        assertFails { clipboard.copyPhoto(photo, "Message from Echo Chat") }

        assertNull(clipboard.entry)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun blobBytes(blob: JsAny): Promise<ArrayBuffer> = js("blob.arrayBuffer()")
