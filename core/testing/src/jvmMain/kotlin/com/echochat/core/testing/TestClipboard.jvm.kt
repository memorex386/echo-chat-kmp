package com.echochat.core.testing

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry.plainText(): String? {
    val transferable = nativeClipEntry as? Transferable ?: return null
    if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) return null
    return transferable.getTransferData(DataFlavor.stringFlavor) as String
}
