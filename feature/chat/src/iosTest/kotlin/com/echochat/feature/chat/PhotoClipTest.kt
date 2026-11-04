package com.echochat.feature.chat

import com.echochat.core.testing.TestClipboard
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIPasteboard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalForeignApi::class)
class PhotoClipTest {

    @Test
    fun `ACTION-03 copying a photo puts the picture itself on the clipboard`() = runTest {
        val path = NSTemporaryDirectory() + "sunset.png"
        val format = UIGraphicsImageRendererFormat().apply { setScale(1.0) }
        val picture = UIGraphicsImageRenderer(CGSizeMake(40.0, 30.0), format).imageWithActions { }
        assertNotNull(UIImagePNGRepresentation(picture)).writeToFile(path, atomically = true)
        // A pasteboard of its own, so the test leaves the simulator's clipboard alone.
        val pasteboard = UIPasteboard.pasteboardWithUniqueName()

        TestClipboard(pasteboard).copyPhoto(path, "Message from Echo Chat")

        val pasted = assertNotNull(pasteboard.image, "Nothing was copied")
        assertEquals(40.0 to 30.0, pasted.size.useContents { width to height })
    }
}
