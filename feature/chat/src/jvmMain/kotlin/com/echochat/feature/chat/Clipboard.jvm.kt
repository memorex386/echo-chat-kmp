package com.echochat.feature.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.copyText(text: String, label: String) =
    setClipEntry(ClipEntry(StringSelection(text)))

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.copyPhoto(path: String, label: String) {
    val file = File(path)
    val picture = withContext(Dispatchers.IO) { ImageIO.read(file) }
    setClipEntry(ClipEntry(PhotoTransferable(picture, file)))
}

internal actual val systemConfirmsCopy: Boolean = false

/**
 * The picture itself, for apps like Paint and Word, and the file, for File Explorer. Java can't
 * read every format as a picture (HEIC, for one), so the file is also there to fall back on.
 */
private class PhotoTransferable(private val picture: BufferedImage?, private val file: File) : Transferable {
    private val flavors = listOfNotNull(DataFlavor.imageFlavor.takeIf { picture != null }, DataFlavor.javaFileListFlavor)

    override fun getTransferDataFlavors(): Array<DataFlavor> = flavors.toTypedArray()

    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor in flavors

    override fun getTransferData(flavor: DataFlavor): Any = when {
        flavor == DataFlavor.imageFlavor && picture != null -> picture
        flavor == DataFlavor.javaFileListFlavor -> listOf(file)
        else -> throw UnsupportedFlavorException(flavor)
    }
}
