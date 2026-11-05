package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import com.echochat.core.testing.TestClipboard
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.awt.Image as AwtImage

@OptIn(ExperimentalComposeUiApi::class)
class PhotoClipTest {

    @Test
    fun `ACTION-03 copying a photo puts the picture itself on the clipboard`() = runTest {
        val photo = File.createTempFile("sunset", ".png").apply { deleteOnExit() }
        ImageIO.write(BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", photo)
        val clipboard = TestClipboard()

        clipboard.copyPhoto(PlatformFile(photo), "Message from Echo Chat")

        val copied = assertNotNull(clipboard.entry, "Nothing was copied").nativeClipEntry as Transferable
        val pasted = copied.getTransferData(DataFlavor.imageFlavor) as AwtImage
        assertEquals(40 to 30, pasted.getWidth(null) to pasted.getHeight(null))
    }
}
